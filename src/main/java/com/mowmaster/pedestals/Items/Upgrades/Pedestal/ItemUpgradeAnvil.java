package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Storage.PacketContents;
import com.mowmaster.pedestals.Items.Storage.StoragePacketItem;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.PedestalTags;
import com.mowmaster.pedestals.Recipes.AnvilConflictRecipe;
import com.mowmaster.pedestals.Recipes.AnvilCost;
import com.mowmaster.pedestals.Recipes.AnvilOverlevelRecipe;
import com.mowmaster.pedestals.Recipes.PedestalRecipes;
import com.mowmaster.pedestals.Transport.EnergyTransportType;
import com.mowmaster.pedestals.Transport.ExperienceTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.AmountResource;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import com.mowmaster.pedestals.api.upgrade.GhostItem;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

// 1.16's XP anvil: an anvil without the level cap, paid from this pedestal's experience. The item to work on comes
// from the inventory below and the result goes into this pedestal. The other inputs sit on pedestals selected with
// a pedestals work card ("input pedestals"), which are used in the order they were selected. Any number can be
// selected, but materials (repair materials, recipe items, fluid, energy) from the 10th one on cost more: double
// from the 10th, triple from the 11th, and so on (a diamond taken from the 10th pedestal uses 2 diamonds).
//
// What it does with the item, the first that applies:
//  - Mending (with the mending modifier, damaged items only): repairs 2 durability per experience point, like the
//    enchantment. No materials, and the repair cost isn't used or raised.
//  - Repair (damaged items): each repair material (as the anvil: iron for iron tools...) restores a quarter of the
//    durability, using as many as needed. Costs the item's repair cost + 1 level per material; the repair cost
//    then goes up like the anvil's, with no cap.
//  - Combine: the first enchanted book or enchanted item on each input pedestal is merged in, in order. Inputs merge
//    with each other first (two Sharpness V books make Sharpness VI), then onto the item (equal levels go up one,
//    otherwise the higher level wins). Costs what the anvil would, with no 40 level cap. Inputs that end up adding
//    nothing are dropped above the anvil. If none of them can go on, the anvil pauses (see below).
//    - Past the maximum level needs the enchantment's pedestals:anvil_overlevel recipe, multiplied by how far past
//      the maximum it ends up (without a recipe it stops at the maximum).
//    - Any item takes any enchantment, even ones it normally can't (a stick with Sharpness), except items in
//      #pedestals:pedestals_anvil_blacklist, which follow the normal anvil's rules.
//    - A new enchantment that conflicts with ones on the item (Sharpness and Smite) needs the
//      pedestals:anvil_conflict recipe for each pair, added together; with a pair missing its recipe it isn't added.
//      Raising an enchantment the item already has never counts as a conflict.
//  - Any item with a custom name on an input pedestal renames the result (the first one; it's used up, for free).
//
// Taking the inputs: a plan is made first, then each operation takes everything the plan needs from one input
// pedestal, with particles from it to the anvil. Fluid and energy only come from input pedestals that have a
// storage packet of that type in their item storage (the packet isn't used up). When everything is collected the
// result is made. If something goes missing on the way (or the card, tool or upgrade changes), everything collected
// so far is dropped above the anvil (fluid and energy as packets).
//
// When the item can't be made yet, the anvil pauses on it (the next item waits) and shows what's wrong as ghost
// items; a redstone signal skips it (it comes out unchanged, with a villager's "no").
// Missing inputs are shown as ghost items with the amount needed (see GhostItem): above empty input pedestals first,
// then circling the anvil when there aren't enough input pedestals. Fluid and energy show as their storage packet
// (a pedestal needs one to give them), and missing experience as an experience packet circling the anvil.
public class ItemUpgradeAnvil extends ItemUpgradeBase
{
    // Input pedestals past this many make their materials cost more
    public static final int FREE_INPUTS = 9;
    private static final String PLAN = "anvil_plan";
    // The item the anvil is paused on (showing what it's missing), for a redstone signal to skip
    private static final String STUCK = "anvil_stuck";

    public ItemUpgradeAnvil(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.MENDING);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.PEDESTALS;
    }

    // While powered the anvil does nothing, except that the item it's paused on is skipped: it comes out unchanged
    @Override
    public void tick(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        if (pedestal.isPowered())
        {
            if (readPlan(level, pedestal) == null) skipStuck(level, pedestal);
            return;
        }
        super.tick(level, pedestal, upgrade);
    }

    private static void skipStuck(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        Optional<ItemStack> stuck = pedestal.getUpgradeData().read(STUCK, ItemStack.CODEC, ops(level));
        if (stuck.isEmpty()) return;
        setStuck(level, pedestal, null);
        pedestal.setGhosts(List.of());
        ResourceHandler<ItemResource> inventory = getInventoryBelow(level, pedestal);
        for (int slot = 0; inventory != null && slot < inventory.size(); slot++)
        {
            ItemResource resource = inventory.getResource(slot);
            if (!ItemStack.isSameItemSameComponents(resource.toStack(1), stuck.get())) continue;
            try (Transaction transaction = Transaction.openRoot())
            {
                if (inventory.extract(slot, resource, 1, transaction) != 1) return;
                if (ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), resource, 1, transaction) != 1) return;
                transaction.commit();
            }
            refused(level, pedestal);
            return;
        }
    }

    private static void setStuck(ServerLevel level, BasePedestalBlockEntity pedestal, @Nullable ItemStack target)
    {
        if (target == null) pedestal.getUpgradeData().remove(STUCK);
        else pedestal.getUpgradeData().store(STUCK, ItemStack.CODEC, ops(level), target);
        pedestal.upgradeDataChanged();
    }

    // An item coming out unchanged
    private static void refused(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        level.playSound(null, pedestal.getBlockPos(), SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 0.5F, 1.0F);
    }

    // ---- The plan ----

    // Everything decided up front: the item it works on, what it becomes, the experience it costs (points), what to
    // take from each input pedestal, how many of those steps are done, and the inputs to drop afterwards
    private record Plan(ItemStack target, ItemStack result, int experience, List<Step> steps, int next, List<ItemStack> leftovers)
    {
        static final Codec<Plan> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("target").forGetter(Plan::target),
                ItemStack.CODEC.fieldOf("result").forGetter(Plan::result),
                Codec.INT.fieldOf("experience").forGetter(Plan::experience),
                Step.CODEC.listOf().fieldOf("steps").forGetter(Plan::steps),
                Codec.INT.fieldOf("next").forGetter(Plan::next),
                ItemStack.CODEC.listOf().fieldOf("leftovers").forGetter(Plan::leftovers))
                .apply(instance, Plan::new));

        Plan advance()
        {
            return new Plan(target, result, experience, steps, next + 1, leftovers);
        }

        boolean collected()
        {
            return next >= steps.size();
        }
    }

    // What to take from one input pedestal: one PacketContents per transport type
    private record Step(BlockPos pedestal, List<PacketContents<?>> take)
    {
        static final Codec<Step> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pedestal").forGetter(Step::pedestal),
                PacketContents.CODEC.listOf().fieldOf("take").forGetter(Step::take))
                .apply(instance, Step::new));
    }

    // Item stacks hold enchantments, which live in a datapack registry, so the plan is saved with the level's
    // registries ("registry ops") instead of plain NBT
    private static DynamicOps<Tag> ops(ServerLevel level)
    {
        return level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
    }

    private static @Nullable Plan readPlan(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        return pedestal.getUpgradeData().read(PLAN, Plan.CODEC, ops(level)).orElse(null);
    }

    private static void savePlan(ServerLevel level, BasePedestalBlockEntity pedestal, @Nullable Plan plan)
    {
        if (plan == null) pedestal.getUpgradeData().remove(PLAN);
        else pedestal.getUpgradeData().store(PLAN, Plan.CODEC, ops(level), plan);
        pedestal.upgradeDataChanged();
    }

    // ---- Working ----

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        Plan plan = readPlan(level, pedestal);
        if (plan == null)
        {
            plan = makePlan(level, pedestal, upgrade);
            if (plan == null) return;
            savePlan(level, pedestal, plan);
            if (!plan.collected()) return; // the first input pedestal is used next time
        }
        if (!plan.collected()) collectStep(level, pedestal, plan);
        else finish(level, pedestal, plan);
    }

    // Takes one input pedestal's share
    private static void collectStep(ServerLevel level, BasePedestalBlockEntity pedestal, Plan plan)
    {
        Step step = plan.steps().get(plan.next());
        if (!(level.getBlockEntity(step.pedestal()) instanceof BasePedestalBlockEntity input))
        {
            abort(level, pedestal, plan);
            return;
        }
        try (Transaction transaction = Transaction.openRoot())
        {
            for (PacketContents<?> contents : step.take())
            {
                if (!extractAll(input, contents, transaction))
                {
                    abort(level, pedestal, plan); // closing the transaction without commit puts back what was taken
                    return;
                }
            }
            transaction.commit();
        }
        Vec3 from = topOf(input);
        Vec3 to = topOf(pedestal);
        for (PacketContents<?> contents : step.take()) sendParticles(level, contents, from, to);
        savePlan(level, pedestal, plan.advance());
    }

    // Everything is collected: swap the item for the result and pay the experience
    private static void finish(ServerLevel level, BasePedestalBlockEntity pedestal, Plan plan)
    {
        ResourceHandler<ItemResource> inventory = getInventoryBelow(level, pedestal);
        int index = -1;
        for (int slot = 0; inventory != null && slot < inventory.size(); slot++)
        {
            if (ItemStack.isSameItemSameComponents(inventory.getResource(slot).toStack(1), plan.target()))
            {
                index = slot;
                break;
            }
        }
        if (index < 0)
        {
            abort(level, pedestal, plan); // the item is gone
            return;
        }
        try (Transaction transaction = Transaction.openRoot())
        {
            // Not enough experience or no room for the result: wait and try again next time
            if (inventory.extract(index, inventory.getResource(index), 1, transaction) != 1) return;
            if (ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(plan.result()), 1, transaction) != 1) return;
            if (plan.experience() > 0 && experience(pedestal).extract(ExperienceTransportType.EXPERIENCE, plan.experience(), transaction) != plan.experience()) return;
            transaction.commit();
        }
        Vec3 above = Vec3.atCenterOf(pedestal.getPosAbove());
        for (ItemStack leftover : plan.leftovers()) Containers.dropItemStack(level, above.x, above.y, above.z, leftover.copy());
        savePlan(level, pedestal, null);
        if (plan.experience() == 0 && ItemStack.matches(plan.result(), plan.target())) refused(level, pedestal); // passed through unchanged
        else
        {
            level.playSound(null, pedestal.getBlockPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.25F, 1.0F);
            pedestal.spawnParticles(level, pedestal.getPosAbove(), 0x00FF00);
        }
    }

    // Gives back everything collected so far (dropped above the anvil) and forgets the plan
    private static void abort(ServerLevel level, BasePedestalBlockEntity pedestal, Plan plan)
    {
        dropCollected(level, pedestal, plan);
        savePlan(level, pedestal, null);
        pedestal.spawnParticles(level, pedestal.getPosAbove(), 0xFF0000);
    }

    private static void dropCollected(ServerLevel level, BasePedestalBlockEntity pedestal, Plan plan)
    {
        Vec3 above = Vec3.atCenterOf(pedestal.getPosAbove());
        for (int step = 0; step < plan.next() && step < plan.steps().size(); step++)
        {
            for (PacketContents<?> contents : plan.steps().get(step).take()) drop(level, above, contents);
        }
    }

    // The card, tool or upgrade changed part way: nothing collected is lost
    @Override
    public void onUpgradeDataReset(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        Plan plan = readPlan(level, pedestal);
        if (plan != null) dropCollected(level, pedestal, plan);
    }

    @Override
    public void onRemoved(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        onUpgradeDataReset(level, pedestal, upgrade);
        super.onRemoved(level, pedestal, upgrade);
    }

    // ---- Making a plan ----

    private @Nullable Plan makePlan(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        ResourceHandler<ItemResource> inventory = getInventoryBelow(level, pedestal);
        if (inventory == null)
        {
            pedestal.setGhosts(List.of());
            return null;
        }

        // The input pedestals, in the order they were selected (anything that isn't a pedestal is skipped)
        List<BasePedestalBlockEntity> inputs = new ArrayList<>();
        for (BlockPos pos : pedestal.getWorkPositions())
        {
            if (!pos.equals(pedestal.getBlockPos()) && level.getBlockEntity(pos) instanceof BasePedestalBlockEntity input) inputs.add(input);
        }
        int stored = experience(pedestal).getAmountAsInt(0);

        // Items are tried in order. An item the anvil can't do anything with is passed over; the first that can be
        // worked on is, unless it's missing something: then the anvil pauses on it and shows what's missing
        // (a redstone signal skips it)
        for (int index = 0; index < inventory.size(); index++)
        {
            ItemResource resource = inventory.getResource(index);
            if (resource.isEmpty()) continue;
            ItemStack target = resource.toStack(1);
            if (!pedestal.machineFilterAllows(target)) continue;

            List<Missing> missing = new ArrayList<>();
            Plan plan;
            if (hasModifier(upgrade, UpgradeModifiers.MENDING) && target.isDamageableItem() && target.isDamaged()) plan = mendingPlan(target, stored, missing);
            else
            {
                List<Missing> repairMissing = new ArrayList<>();
                plan = target.isDamageableItem() && target.isDamaged() ? repairPlan(level, target, sources(inputs), stored, repairMissing) : null;
                if (plan == null) plan = combinePlan(level, target, sources(inputs), stored, missing);
                // Only ask for repair materials when combining wasn't what was meant either
                if (plan == null && missing.isEmpty()) missing = repairMissing;
            }
            if (plan != null)
            {
                pedestal.setGhosts(List.of());
                setStuck(level, pedestal, null);
                return plan;
            }
            if (!missing.isEmpty())
            {
                pedestal.setGhosts(ghosts(level, pedestal, inputs, missing));
                setStuck(level, pedestal, target);
                return null;
            }
        }
        pedestal.setGhosts(List.of());
        setStuck(level, pedestal, null);
        return null;
    }

    private static @Nullable Plan mendingPlan(ItemStack target, int stored, List<Missing> missing)
    {
        int needed = (target.getDamageValue() + 1) / 2;
        int points = Math.min(stored, needed);
        if (points <= 0)
        {
            missing.add(Missing.experience(needed));
            return null;
        }
        ItemStack result = target.copy();
        result.setDamageValue(Math.max(0, target.getDamageValue() - points * 2));
        return new Plan(target, result, points, List.of(), 0, List.of());
    }

    private static @Nullable Plan repairPlan(ServerLevel level, ItemStack target, List<Source> sources, int stored, List<Missing> missing)
    {
        ItemStack result = target.copy();
        // The name first, so a named stack can't be used up as material before it's found
        NamedInput name = findName(sources);
        if (name != null)
        {
            name.source().take(name.stack(), 1);
            result.set(DataComponents.CUSTOM_NAME, name.name());
        }

        int materials = 0;
        int repairAmount = Math.min(result.getDamageValue(), result.getMaxDamage() / 4);
        while (repairAmount > 0 && takeItems(sources, target::isValidRepairItem, 1) == 1)
        {
            result.setDamageValue(result.getDamageValue() - repairAmount);
            materials++;
            repairAmount = Math.min(result.getDamageValue(), result.getMaxDamage() / 4);
        }
        if (materials == 0)
        {
            // Ask for enough material to repair it fully (the item's repairable component lists what it takes)
            Repairable repairable = target.get(DataComponents.REPAIRABLE);
            int quarter = Math.max(1, target.getMaxDamage() / 4);
            if (repairable != null && repairable.items().size() > 0)
            {
                missing.add(Missing.item(new ItemStack(repairable.items().get(0)), (target.getDamageValue() + quarter - 1) / quarter));
            }
            return null;
        }

        int repairCost = repairCost(target);
        result.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(repairCost));
        int levels = repairCost + materials;
        return build(target, result, levels, sources, List.of(), stored, missing);
    }

    // One enchanted input: the pedestal it's on, the stack, and its enchantments
    private record EnchantInput(Source source, ItemStack stack, ItemEnchantments enchantments, boolean book) {}

    // An enchantment while the inputs are merged: its level so far, and which inputs gave that level
    private static final class PoolEntry
    {
        int level;
        final Set<EnchantInput> feeders = new HashSet<>();
    }

    private static @Nullable Plan combinePlan(ServerLevel level, ItemStack target, List<Source> sources, int stored, List<Missing> missing)
    {
        if (target.is(Items.BOOK)) return null;
        // Blacklisted items only take what a normal anvil would allow
        boolean blacklisted = target.is(PedestalTags.ANVIL_BLACKLIST);

        // The first enchanted thing on each input pedestal
        List<EnchantInput> inputs = new ArrayList<>();
        for (Source source : sources)
        {
            for (ItemStack stack : source.items)
            {
                ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
                if (stack.isEmpty() || enchantments.isEmpty()) continue;
                inputs.add(new EnchantInput(source, stack, enchantments, stack.has(DataComponents.STORED_ENCHANTMENTS)));
                break;
            }
        }

        // Merge the inputs with each other, in order
        Map<Holder<Enchantment>, PoolEntry> pool = new LinkedHashMap<>();
        for (EnchantInput input : inputs)
        {
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : input.enchantments().entrySet())
            {
                int incoming = entry.getIntValue();
                PoolEntry pooled = pool.get(entry.getKey());
                if (pooled == null)
                {
                    pooled = new PoolEntry();
                    pooled.level = incoming;
                    pooled.feeders.add(input);
                    pool.put(entry.getKey(), pooled);
                }
                else if (incoming == pooled.level)
                {
                    pooled.level++;
                    pooled.feeders.add(input);
                }
                else if (incoming > pooled.level)
                {
                    pooled.level = incoming;
                    pooled.feeders.clear();
                    pooled.feeders.add(input);
                }
                // lower: adds nothing
            }
        }

        // Then onto the item
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(target));
        boolean bookTarget = target.is(Items.ENCHANTED_BOOK);
        boolean enchantable = bookTarget || EnchantmentHelper.canStoreEnchantments(target);
        Set<EnchantInput> used = new HashSet<>();
        List<Charge> charges = new ArrayList<>();
        int price = 0;
        for (Map.Entry<Holder<Enchantment>, PoolEntry> entry : pool.entrySet())
        {
            Holder<Enchantment> holder = entry.getKey();
            Enchantment enchantment = holder.value();
            int current = enchantments.getLevel(holder);
            int pooled = entry.getValue().level;
            int newLevel = current == pooled ? pooled + 1 : Math.max(current, pooled);
            if (newLevel <= current) continue;

            // Past the maximum: the over-level recipe, times how far past (no recipe: no further than the maximum)
            Charge overLevel = null;
            if (newLevel > enchantment.getMaxLevel())
            {
                Optional<RecipeHolder<AnvilOverlevelRecipe>> recipe = level.recipeAccess()
                        .getRecipeFor(PedestalRecipes.ANVIL_OVERLEVEL.get(), new AnvilOverlevelRecipe.Input(holder), level);
                if (recipe.isPresent()) overLevel = new Charge(recipe.get().value().cost(), newLevel - enchantment.getMaxLevel(), newLevel);
                else
                {
                    newLevel = Math.max(current, enchantment.getMaxLevel());
                    if (newLevel <= current) continue;
                }
            }

            // Conflicts with what's already on the item: none if this is only raising an enchantment the item has
            List<Holder<Enchantment>> conflicts = new ArrayList<>();
            if (current == 0)
            {
                for (Holder<Enchantment> other : enchantments.keySet())
                {
                    if (!other.equals(holder) && !Enchantment.areCompatible(holder, other)) conflicts.add(other);
                }
            }
            boolean supported = enchantable && (bookTarget || target.supportsEnchantment(holder));
            if (blacklisted && (!supported || !conflicts.isEmpty())) continue;

            // Every conflicting pair needs its recipe
            List<Charge> conflictCharges = new ArrayList<>();
            boolean allowed = true;
            for (Holder<Enchantment> other : conflicts)
            {
                Optional<RecipeHolder<AnvilConflictRecipe>> recipe = level.recipeAccess()
                        .getRecipeFor(PedestalRecipes.ANVIL_CONFLICT.get(), new AnvilConflictRecipe.Input(holder, other), level);
                if (recipe.isEmpty())
                {
                    allowed = false;
                    break;
                }
                conflictCharges.add(new Charge(recipe.get().value().cost(), 1, newLevel));
            }
            if (!allowed) continue;

            enchantments.set(holder, newLevel);
            used.addAll(entry.getValue().feeders);
            if (overLevel != null) charges.add(overLevel);
            charges.addAll(conflictCharges);
            int fee = enchantment.getAnvilCost();
            if (entry.getValue().feeders.stream().allMatch(EnchantInput::book)) fee = Math.max(1, fee / 2);
            price += fee * newLevel;
        }

        NamedInput name = findName(sources);
        if (used.isEmpty() && name == null)
        {
            if (inputs.isEmpty()) return null;
            // A blacklisted item comes straight out unchanged
            if (blacklisted) return new Plan(target, target.copy(), 0, List.of(), 0, List.of());
            // Otherwise the anvil pauses, marking every input that can't go on (a lower level, a conflict without a
            // recipe...), until they're changed or a redstone signal skips the item
            for (EnchantInput input : inputs) missing.add(Missing.blocked(input.source().pedestal.getBlockPos()));
            return null;
        }

        ItemStack result = target.copy();
        List<ItemStack> leftovers = new ArrayList<>();
        int highestRepairCost = repairCost(target);
        // Renaming alone is free
        int levels = used.isEmpty() ? 0 : repairCost(target) + price;
        if (!used.isEmpty())
        {
            EnchantmentHelper.setEnchantments(result, enchantments.toImmutable());
            // Every enchanted input is taken; the ones that added nothing come back out afterwards
            for (EnchantInput input : inputs)
            {
                boolean naming = name != null && name.stack() == input.stack();
                if (used.contains(input))
                {
                    levels += repairCost(input.stack());
                    highestRepairCost = Math.max(highestRepairCost, repairCost(input.stack()));
                }
                else if (!naming) leftovers.add(input.stack().copyWithCount(1));
                input.source().take(input.stack(), 1);
                if (naming) name = new NamedInput(name.source(), name.stack(), name.name(), true);
            }
            result.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(highestRepairCost));
        }
        if (name != null)
        {
            if (!name.taken()) name.source().take(name.stack(), 1);
            result.set(DataComponents.CUSTOM_NAME, name.name());
        }

        // The over-level and conflict recipes' materials; if any are missing it waits until they're there (everything
        // missing is noted, so all of it can be shown)
        int before = missing.size();
        for (Charge charge : charges)
        {
            AnvilCost cost = charge.cost();
            for (AnvilCost.AnvilIngredient ingredient : cost.ingredientsAt(charge.level()))
            {
                ItemStack display = ingredient.ingredient().display().resolveForFirstStack(SlotDisplayContext.fromLevel(level));
                // Potions don't stack, so one is always enough: no recipe count, multiplier or extra-pedestal factor
                boolean potion = isPotion(display);
                int needed = potion ? 1 : ingredient.count() * charge.times();
                int found = takeItems(sources, ingredient.ingredient(), needed, potion);
                if (found < needed) missing.add(Missing.item(display, needed - found));
            }
            if (cost.fluid().isPresent())
            {
                SizedFluidIngredient fluid = cost.fluid().get();
                int needed = fluid.amount() * charge.times();
                int found = takeFluid(sources, fluid, needed);
                if (found < needed) missing.add(Missing.fluid(fluid, needed - found));
            }
            int energy = cost.energy() * charge.times();
            int foundEnergy = energy > 0 ? takeEnergy(sources, energy) : 0;
            if (foundEnergy < energy) missing.add(Missing.energy(energy - foundEnergy));
            levels += cost.experienceLevels() * charge.times();
        }
        if (missing.size() > before) return null;

        return build(target, result, levels, sources, leftovers, stored, missing);
    }

    // A recipe cost to pay: times over (how far past the maximum), with ingredients picked for the enchantment's level
    private record Charge(AnvilCost cost, int times, int level) {}

    // The finished plan, if the anvil has the experience: levels are counted from 0, like 1.16 (30 levels = 1395 points)
    private static @Nullable Plan build(ItemStack target, ItemStack result, int levels, List<Source> sources, List<ItemStack> leftovers,
                                        int stored, List<Missing> missing)
    {
        int points = ExperienceTransportType.pointsForLevel(levels);
        if (points > stored)
        {
            missing.add(Missing.experience(points - stored));
            return null;
        }
        List<Step> steps = new ArrayList<>();
        for (Source source : sources)
        {
            List<PacketContents<?>> taken = source.taken();
            if (!taken.isEmpty()) steps.add(new Step(source.pedestal.getBlockPos(), taken));
        }
        return new Plan(target, result, points, steps, 0, leftovers);
    }

    // ---- Names ----

    private record NamedInput(Source source, ItemStack stack, Component name, boolean taken) {}

    // The first item with a custom name on the input pedestals
    private static @Nullable NamedInput findName(List<Source> sources)
    {
        for (Source source : sources)
        {
            for (ItemStack stack : source.items)
            {
                if (!stack.isEmpty() && stack.has(DataComponents.CUSTOM_NAME)) return new NamedInput(source, stack, stack.get(DataComponents.CUSTOM_NAME), false);
            }
        }
        return null;
    }

    // ---- What the input pedestals have ----

    // One input pedestal while a plan is made: copies of what it holds (counts go down as the plan uses them), and
    // what the plan takes from it
    private static final class Source
    {
        final BasePedestalBlockEntity pedestal;
        // How many of a material this pedestal uses up per one needed (1 for the first FREE_INPUTS pedestals)
        final int factor;
        final List<ItemStack> items = new ArrayList<>();
        @Nullable FluidResource fluid;
        int fluidAmount;
        int energy;
        PacketContents<ItemResource> takenItems = new PacketContents<>(PedestalTransportTypes.ITEMS.get(), List.of());
        PacketContents<FluidResource> takenFluid = new PacketContents<>(PedestalTransportTypes.FLUIDS.get(), List.of());
        PacketContents<AmountResource> takenEnergy = new PacketContents<>(PedestalTransportTypes.ENERGY.get(), List.of());

        Source(BasePedestalBlockEntity pedestal, int factor)
        {
            this.pedestal = pedestal;
            this.factor = factor;
            for (ItemStack stack : pedestal.getItemStacks())
            {
                if (!(stack.getItem() instanceof StoragePacketItem packet))
                {
                    items.add(stack);
                    continue;
                }
                // A packet says this pedestal gives that type (the packet itself stays)
                PedestalTransportType<?> type = packet.getType();
                if (type == PedestalTransportTypes.FLUIDS.get())
                {
                    PedestalStorage<FluidResource> fluids = pedestal.getStorage(PedestalTransportTypes.FLUIDS.get());
                    fluid = fluids.getResource(0);
                    fluidAmount = fluids.getAmountAsInt(0);
                }
                else if (type == PedestalTransportTypes.ENERGY.get())
                {
                    energy = pedestal.getStorage(PedestalTransportTypes.ENERGY.get()).getAmountAsInt(0);
                }
            }
        }

        // Takes amount of this exact stack (one of this source's own copies)
        void take(ItemStack stack, int amount)
        {
            if (amount <= 0 || stack.isEmpty()) return;
            takenItems = takenItems.plus(ItemResource.of(stack), amount);
            stack.shrink(amount);
        }

        List<PacketContents<?>> taken()
        {
            List<PacketContents<?>> taken = new ArrayList<>();
            if (!takenItems.isEmpty()) taken.add(takenItems);
            if (!takenFluid.isEmpty()) taken.add(takenFluid);
            if (!takenEnergy.isEmpty()) taken.add(takenEnergy);
            return taken;
        }
    }

    private static List<Source> sources(List<BasePedestalBlockEntity> inputs)
    {
        List<Source> sources = new ArrayList<>();
        for (int index = 0; index < inputs.size(); index++) sources.add(new Source(inputs.get(index), factor(index)));
        return sources;
    }

    // Up to amount of the items that pass the test, from the input pedestals in order; returns how many it found
    private static int takeItems(List<Source> sources, Predicate<ItemStack> test, int amount)
    {
        return takeItems(sources, test, amount, false);
    }

    // ignoreFactor: take exactly amount, even from the 10th input pedestal on (potions)
    private static int takeItems(List<Source> sources, Predicate<ItemStack> test, int amount, boolean ignoreFactor)
    {
        int found = 0;
        for (Source source : sources)
        {
            for (ItemStack stack : source.items)
            {
                if (found >= amount) return found;
                if (stack.isEmpty() || !test.test(stack)) continue;
                int factor = ignoreFactor ? 1 : source.factor;
                int taking = Math.min(amount - found, stack.getCount() / factor);
                if (taking <= 0) continue;
                source.take(stack, taking * factor);
                found += taking;
            }
        }
        return found;
    }

    private static int takeFluid(List<Source> sources, SizedFluidIngredient fluid, int amount)
    {
        int found = 0;
        for (Source source : sources)
        {
            if (found >= amount) break;
            if (source.fluid == null || source.fluid.isEmpty() || source.fluidAmount <= 0 || !fluid.ingredient().test(source.fluid.toStack(1))) continue;
            int taking = Math.min(amount - found, source.fluidAmount / source.factor);
            if (taking <= 0) continue;
            source.fluidAmount -= taking * source.factor;
            source.takenFluid = source.takenFluid.plus(source.fluid, taking * source.factor);
            found += taking;
        }
        return found;
    }

    private static int takeEnergy(List<Source> sources, int amount)
    {
        int found = 0;
        for (Source source : sources)
        {
            if (found >= amount) break;
            int taking = Math.min(amount - found, source.energy / source.factor);
            if (taking <= 0) continue;
            source.energy -= taking * source.factor;
            source.takenEnergy = source.takenEnergy.plus(EnergyTransportType.ENERGY, taking * source.factor);
            found += taking;
        }
        return found;
    }

    // ---- Missing inputs ----

    // Something a plan couldn't find: what to show and how much is needed (before any extra-pedestal factor).
    // perPedestal: whether it comes from input pedestals (and so costs more on the 10th one on); experience doesn't.
    // at: a fixed place to show it (above an input pedestal whose input can't be used); null = wherever there's room
    // detail: extra text after the amount (a fluid's name, a potion's effects)
    private record Missing(ItemStack display, int amount, Unit unit, boolean perPedestal, @Nullable Component detail, @Nullable BlockPos at)
    {
        static Missing item(ItemStack display, int amount)
        {
            // An ingredient with nothing to show (an empty tag) shows a barrier
            if (display.isEmpty()) return new Missing(new ItemStack(Items.BARRIER), amount, Unit.ITEMS, true, null, null);
            boolean potion = isPotion(display);
            return new Missing(display.copyWithCount(1), potion ? 1 : amount, Unit.ITEMS, !potion, potionEffects(display), null);
        }

        // A potion's effects as its tooltip names them: "Harming II", "Speed, Strength"... (null if it has none)
        private static @Nullable Component potionEffects(ItemStack stack)
        {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents == null) return null;
            MutableComponent text = null;
            for (MobEffectInstance effect : contents.getAllEffects())
            {
                Component name = effect.getEffect().value().getDisplayName();
                // Vanilla's own keys: "potion.withAmplifier" is "%s %s", "potion.potency.1" is "II"...
                MutableComponent line = effect.getAmplifier() > 0
                        ? Component.translatable("potion.withAmplifier", name, Component.translatable("potion.potency." + effect.getAmplifier()))
                        : name.copy();
                if (text == null) text = line;
                else text.append(", ").append(line);
            }
            return text;
        }

        // Shown as a fluid packet, since an input pedestal needs one to give fluid
        static Missing fluid(SizedFluidIngredient fluid, int amount)
        {
            List<Holder<Fluid>> fluids = fluid.ingredient().fluids();
            Component name = fluids.isEmpty() ? null : fluids.getFirst().value().getFluidType().getDescription();
            return new Missing(packet(PedestalTransportTypes.FLUIDS.get()), amount, Unit.MILLIBUCKETS, true, name, null);
        }

        static Missing energy(int amount)
        {
            return new Missing(packet(PedestalTransportTypes.ENERGY.get()), amount, Unit.ENERGY, true, null, null);
        }

        static Missing experience(int points)
        {
            return new Missing(packet(PedestalTransportTypes.EXPERIENCE.get()), points, Unit.EXPERIENCE, false, null, null);
        }

        // The input on this pedestal can't be used
        static Missing blocked(BlockPos pedestal)
        {
            return new Missing(new ItemStack(Items.BARRIER), 0, Unit.BLOCKED, false, null, pedestal);
        }

        private static ItemStack packet(PedestalTransportType<?> type)
        {
            StoragePacketItem item = StoragePacketItem.forType(type);
            return item != null ? new ItemStack(item) : new ItemStack(Items.BARRIER);
        }

        // Items: "x3" (and a potion's effects) above. Stored types: what it is above ("Lava", "Energy"), the amount below.
        GhostItem ghost(Optional<BlockPos> above, int factor)
        {
            int total = perPedestal ? amount * factor : amount;
            return switch (unit)
            {
                case BLOCKED -> new GhostItem(above, display, Component.translatable("pedestals.anvil_cannot_apply"), Optional.empty());
                case ITEMS -> new GhostItem(above, display, detail == null ? Component.literal("x" + total)
                        : Component.literal("x" + total + " ").append(detail), Optional.empty());
                case MILLIBUCKETS -> new GhostItem(above, display, detail != null ? detail : PedestalTransportTypes.FLUIDS.get().getName(),
                        Optional.of(Component.literal(total + " mB")));
                case ENERGY -> new GhostItem(above, display, PedestalTransportTypes.ENERGY.get().getName(), Optional.of(Component.literal(total + " FE")));
                case EXPERIENCE -> new GhostItem(above, display, PedestalTransportTypes.EXPERIENCE.get().getName(), Optional.of(Component.literal(total + " XP")));
            };
        }
    }

    private enum Unit { ITEMS, MILLIBUCKETS, ENERGY, EXPERIENCE, BLOCKED }

    // Each missing input goes above an empty input pedestal (its amount scaled for that pedestal); experience, and
    // anything left over when there are no more empty input pedestals, circles the anvil (scaled for the next
    // pedestals that would have to be added)
    private static List<GhostItem> ghosts(ServerLevel level, BasePedestalBlockEntity pedestal, List<BasePedestalBlockEntity> inputs, List<Missing> missing)
    {
        List<Integer> empty = new ArrayList<>();
        for (int index = 0; index < inputs.size(); index++)
        {
            if (inputs.get(index).getItemStacks().isEmpty()) empty.add(index);
        }
        List<GhostItem> ghosts = new ArrayList<>();
        int nextEmpty = 0;
        int extraPedestals = 0;
        for (Missing input : missing)
        {
            if (input.at() != null) ghosts.add(input.ghost(Optional.of(input.at()), 1));
            else if (input.perPedestal() && nextEmpty < empty.size())
            {
                int index = empty.get(nextEmpty++);
                ghosts.add(input.ghost(Optional.of(inputs.get(index).getBlockPos()), factor(index)));
            }
            else
            {
                int factor = input.perPedestal() ? factor(inputs.size() + extraPedestals++) : 1;
                ghosts.add(input.ghost(Optional.empty(), factor));
            }
        }
        return ghosts;
    }

    // ---- Small helpers ----

    private static boolean isPotion(ItemStack stack)
    {
        return stack.has(DataComponents.POTION_CONTENTS);
    }

    // How many of a material the input pedestal at this index (0 = first) uses up per one needed:
    // 1 for the first FREE_INPUTS, then 2, 3, 4...
    private static int factor(int index)
    {
        return index < FREE_INPUTS ? 1 : index - FREE_INPUTS + 2;
    }

    private static int repairCost(ItemStack stack)
    {
        return stack.getOrDefault(DataComponents.REPAIR_COST, 0);
    }

    private static PedestalStorage<AmountResource> experience(BasePedestalBlockEntity pedestal)
    {
        return pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get());
    }

    // The middle of the pedestal's top face (it can face any way)
    private static Vec3 topOf(BasePedestalBlockEntity pedestal)
    {
        return Vec3.atCenterOf(pedestal.getBlockPos()).add(pedestal.getFacing().getUnitVec3().scale(0.5));
    }

    // These take PacketContents<R> so that R (the type's resource class) has a name inside them;
    // called with a PacketContents<?>, Java fills R in ("wildcard capture")
    private static <R extends Resource> boolean extractAll(BasePedestalBlockEntity from, PacketContents<R> contents, TransactionContext transaction)
    {
        PedestalStorage<R> storage = from.getStorage(contents.type());
        for (ResourceStack<R> stack : contents.contents())
        {
            if (storage.extract(stack.resource(), stack.amount(), transaction) != stack.amount()) return false;
        }
        return true;
    }

    private static <R extends Resource> void sendParticles(ServerLevel level, PacketContents<R> contents, Vec3 from, Vec3 to)
    {
        for (ResourceStack<R> stack : contents.contents()) contents.type().sendTransferParticles(level, from, to, stack.resource());
    }

    // Items drop as items, anything else as a storage packet of that type (lost if the type has no packet)
    private static <R extends Resource> void drop(ServerLevel level, Vec3 at, PacketContents<R> contents)
    {
        if (contents.isEmpty()) return;
        if (contents.type() != (Object) PedestalTransportTypes.ITEMS.get())
        {
            Containers.dropItemStack(level, at.x, at.y, at.z, StoragePacketItem.create(contents));
            return;
        }
        for (ResourceStack<R> stack : contents.contents())
        {
            ItemResource item = (ItemResource) stack.resource();
            int left = stack.amount();
            while (left > 0)
            {
                int count = Math.min(left, item.toStack(1).getMaxStackSize());
                Containers.dropItemStack(level, at.x, at.y, at.z, item.toStack(count));
                left -= count;
            }
        }
    }
}
