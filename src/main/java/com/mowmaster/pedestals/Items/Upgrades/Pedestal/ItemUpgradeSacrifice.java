package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mojang.serialization.Codec;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.ItemRegistry;
import com.mowmaster.pedestals.Items.PedestalDataComponents;
import com.mowmaster.pedestals.Items.SacrificeItem;
import com.mowmaster.pedestals.Items.Storage.StoragePacketItem;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.Recipes.AnvilCost;
import com.mowmaster.pedestals.Recipes.AnvilOverlevelRecipe;
import com.mowmaster.pedestals.Recipes.PedestalRecipes;
import com.mowmaster.pedestals.Transport.EnergyTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import com.mowmaster.pedestals.api.upgrade.GhostItem;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

// Makes enchantment sacrifices (and, later, mob sacrifices). Switch type with the upgrade in the OFFHAND: right-click.
//
// Enchantment type: needs a chiseled bookshelf under the pedestal, and input pedestals selected with a pedestals work
// card. The first enchanted book in the bookshelf decides the inputs: what its first enchantment's
// pedestals:anvil_overlevel recipe asks for at the next level (Sharpness V -> VI). They're shown as ghost items over
// Whenever this pedestal is empty, the next input (picked at random in advance, shown as a comparator signal: 1 for the
// first input, up to 15) is used:
//  - if the input pedestals have enough of it, that's used up, its amount doubles for next time, and a sacrifice
//    with the book's enchantments, the first raised to the next level, goes into this pedestal (the book stays);
//  - if not, the book itself is burned into a sacrifice at its own level, and the inputs' amounts reset (all of
//    them, or only that input with upgrade_sacrifice_reset_all_inputs = false).
// A potion is always one (a pedestal slot holds a single potion, so its amount never doubles). Fluid and energy come
// from input pedestals holding a storage packet of that type. The recipe's experience isn't charged.
//
// Mob type: not made yet; the upgrade does nothing in this mode.
public class ItemUpgradeSacrifice extends ItemUpgradeBase
{
    private static final String MODID = PedestalReferences.MODID;
    // What the multipliers belong to (enchantment and level), and the multiplier of each input
    private static final String KEY = "sacrifice_key";
    private static final String MULTIPLIERS = "sacrifice_multipliers";
    // A doubling cap, so the amounts can't overflow
    private static final int MAX_MULTIPLIER = 1 << 20;
    // The input the next sacrifice will use (picked at random in advance, so the comparator can show it)
    private static final String NEXT_INPUT = "sacrifice_next_input";

    public ItemUpgradeSacrifice(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.RANGE);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.PEDESTALS;
    }

    public static boolean isMobMode(ItemStack upgrade)
    {
        return upgrade.has(PedestalDataComponents.SACRIFICE_MOB.get());
    }

    // In the OFFHAND: right-click switches between enchantment and mob type
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        if (hand != InteractionHand.OFF_HAND) return super.use(level, player, hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack upgrade = player.getItemInHand(hand);
        if (isMobMode(upgrade)) upgrade.remove(PedestalDataComponents.SACRIFICE_MOB.get());
        else upgrade.set(PedestalDataComponents.SACRIFICE_MOB.get(), Unit.INSTANCE);
        player.sendOverlayMessage(Component.translatable(MODID + ".mode_changed").append(modeName(upgrade)));
        return InteractionResult.SUCCESS;
    }

    private static Component modeName(ItemStack upgrade)
    {
        return Component.translatable(MODID + (isMobMode(upgrade) ? ".sacrifice_mode.mob" : ".sacrifice_mode.enchantment"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(MODID + ".upgrade_tooltip_mode").append(modeName(stack)).withStyle(ChatFormatting.GOLD));
    }

    // ---- Working ----

    // One input: an item ingredient, a fluid or energy, and how much of it the recipe asks for (before multipliers)
    private record Input(ItemStack display, @Nullable Ingredient item, @Nullable SizedFluidIngredient fluid, int base, boolean potion)
    {
        // What it needs with this multiplier; potions don't stack, so one is always the most a pedestal can give
        int needed(int multiplier)
        {
            return potion ? 1 : (int) Math.min(Integer.MAX_VALUE, (long) base * multiplier);
        }
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        if (isMobMode(upgrade) || !level.getBlockState(pedestal.getPosBelow()).is(Blocks.CHISELED_BOOKSHELF))
        {
            stop(level, pedestal);
            return;
        }
        ResourceHandler<ItemResource> shelf = getInventoryBelow(level, pedestal);
        int bookSlot = -1;
        for (int slot = 0; shelf != null && slot < shelf.size(); slot++)
        {
            ItemResource resource = shelf.getResource(slot);
            if (resource.getItem() == Items.ENCHANTED_BOOK && !bookEnchantments(resource.toStack(1)).isEmpty())
            {
                bookSlot = slot;
                break;
            }
        }
        if (bookSlot < 0)
        {
            stop(level, pedestal);
            return;
        }
        ItemResource bookResource = shelf.getResource(bookSlot);
        ItemStack book = bookResource.toStack(1);
        ItemEnchantments enchantments = bookEnchantments(book);
        Object2IntMap.Entry<Holder<Enchantment>> first = enchantments.entrySet().iterator().next();
        Holder<Enchantment> enchantment = first.getKey();
        int nextLevel = first.getIntValue() + 1;

        Optional<RecipeHolder<AnvilOverlevelRecipe>> recipe = level.recipeAccess()
                .getRecipeFor(PedestalRecipes.ANVIL_OVERLEVEL.get(), new AnvilOverlevelRecipe.Input(enchantment), level);
        if (recipe.isEmpty())
        {
            stop(level, pedestal);
            return;
        }
        List<Input> inputs = inputs(level, recipe.get().value().cost(), nextLevel, Math.max(1, nextLevel - enchantment.value().getMaxLevel()));
        if (inputs.isEmpty())
        {
            stop(level, pedestal);
            return;
        }

        // The multipliers start over for a different book (enchantment and level)
        String key = enchantment.unwrapKey().map(k -> k.identifier().toString()).orElse("?") + "@" + first.getIntValue();
        List<Integer> multipliers = new ArrayList<>(pedestal.getUpgradeData().read(MULTIPLIERS, Codec.INT.listOf()).orElse(List.of()));
        boolean newBook = !key.equals(pedestal.getUpgradeData().getStringOr(KEY, "")) || multipliers.size() != inputs.size();
        if (newBook)
        {
            multipliers.clear();
            for (int index = 0; index < inputs.size(); index++) multipliers.add(1);
        }
        int next = pedestal.getUpgradeData().getIntOr(NEXT_INPUT, -1);
        if (newBook || next < 0 || next >= inputs.size()) next = level.getRandom().nextInt(inputs.size());

        List<BasePedestalBlockEntity> pedestals = inputPedestals(level, pedestal);
        if (!pedestal.hasItem())
        {
            sacrifice(level, pedestal, pedestals, inputs, next, multipliers, shelf, bookSlot, bookResource, enchantments, enchantment, nextLevel);
            next = level.getRandom().nextInt(inputs.size());
        }

        pedestal.getUpgradeData().putString(KEY, key);
        pedestal.getUpgradeData().store(MULTIPLIERS, Codec.INT.listOf(), multipliers);
        pedestal.upgradeDataChanged();
        setNextInput(level, pedestal, next);
        pedestal.setGhosts(ghosts(pedestals, inputs, multipliers));
    }

    // Nothing to do: no ghosts, no signal
    private static void stop(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        pedestal.setGhosts(List.of());
        setNextInput(level, pedestal, -1);
    }

    private static void setNextInput(ServerLevel level, BasePedestalBlockEntity pedestal, int next)
    {
        if (pedestal.getUpgradeData().getIntOr(NEXT_INPUT, -1) == next) return;
        pedestal.getUpgradeData().putInt(NEXT_INPUT, next);
        pedestal.upgradeDataChanged();
        // Comparators only re-read their signal when told it changed
        level.updateNeighbourForOutputSignal(pedestal.getBlockPos(), pedestal.getBlockState().getBlock());
    }

    // The comparator shows which input the next sacrifice will use: 1 for the first, up to 15 (0 = none, or the 16th
    // input on). Without this the pedestal would show how full it is, which is always 15 with a sacrifice in it.
    @Override
    public int getComparatorSignal(BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        int next = pedestal.getUpgradeData().getIntOr(NEXT_INPUT, -1);
        return next >= 0 && next < 15 ? next + 1 : 0;
    }

    // One go: a random input pays for an overleveled sacrifice, or the book burns
    private static void sacrifice(ServerLevel level, BasePedestalBlockEntity pedestal, List<BasePedestalBlockEntity> pedestals, List<Input> inputs,
                                  int chosen, List<Integer> multipliers, ResourceHandler<ItemResource> shelf, int bookSlot, ItemResource bookResource,
                                  ItemEnchantments enchantments, Holder<Enchantment> enchantment, int nextLevel)
    {
        Input input = inputs.get(chosen);
        int needed = input.needed(multipliers.get(chosen));

        // Particles wait until the transaction commits, so nothing is shown for a take that gets undone
        List<Runnable> particles = new ArrayList<>();
        try (Transaction transaction = Transaction.openRoot())
        {
            ItemStack result;
            BasePedestalBlockEntity source = take(level, pedestal, pedestals, input, needed, transaction, particles);
            if (source != null)
            {
                ItemEnchantments.Mutable raised = new ItemEnchantments.Mutable(enchantments);
                raised.set(enchantment, nextLevel);
                result = SacrificeItem.create(ItemRegistry.SACRIFICE.get(), raised.toImmutable());
                multipliers.set(chosen, Math.min(MAX_MULTIPLIER, multipliers.get(chosen) * 2));
            }
            else
            {
                // Not enough: the book goes instead, at its own level
                if (shelf.extract(bookSlot, bookResource, 1, transaction) != 1) return;
                result = SacrificeItem.create(ItemRegistry.SACRIFICE.get(), enchantments);
                if (Config.SACRIFICE_RESET_ALL.getAsBoolean()) multipliers.replaceAll(multiplier -> 1);
                else multipliers.set(chosen, 1);
            }
            if (ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(result), 1, transaction) != 1) return;
            transaction.commit();
            if (source != null && pedestal.canSpawnParticles()) particles.forEach(Runnable::run);
            level.playSound(null, pedestal.getBlockPos(), source != null ? SoundEvents.ENCHANTMENT_TABLE_USE : SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
    }

    // Takes needed of the input from the input pedestals (as part of the transaction), adding the particles to show
    // to the list. Returns the first pedestal it took from, or null if there isn't enough in all of them together.
    // (A lambda like "() -> type.sendTransferParticles(...)" is a Runnable: code kept to run later.)
    private static @Nullable BasePedestalBlockEntity take(ServerLevel level, BasePedestalBlockEntity pedestal, List<BasePedestalBlockEntity> pedestals,
                                                          Input input, int needed, TransactionContext transaction, List<Runnable> particles)
    {
        int found = 0;
        BasePedestalBlockEntity firstSource = null;
        Vec3 to = topOf(pedestal);
        for (BasePedestalBlockEntity source : pedestals)
        {
            if (found >= needed) break;
            int taken = 0;
            if (input.item() != null)
            {
                for (ItemStack stack : source.getItemStacks())
                {
                    if (found + taken >= needed) break;
                    if (StoragePacketItem.isPacket(stack) || !input.item().test(stack)) continue;
                    ItemResource resource = ItemResource.of(stack);
                    int got = source.getItemStorage().extract(resource, Math.min(needed - found - taken, stack.getCount()), transaction);
                    Vec3 from = topOf(source);
                    if (got > 0) particles.add(() -> PedestalTransportTypes.ITEMS.get().sendTransferParticles(level, from, to, resource));
                    taken += got;
                }
            }
            else if (input.fluid() != null)
            {
                if (!hasPacket(source, PedestalTransportTypes.FLUIDS.get())) continue;
                PedestalStorage<FluidResource> fluids = source.getStorage(PedestalTransportTypes.FLUIDS.get());
                FluidResource stored = fluids.getResource(0);
                if (stored.isEmpty() || !input.fluid().ingredient().test(stored.toStack(1))) continue;
                taken = fluids.extract(stored, needed - found, transaction);
                Vec3 from = topOf(source);
                if (taken > 0) particles.add(() -> PedestalTransportTypes.FLUIDS.get().sendTransferParticles(level, from, to, stored));
            }
            else
            {
                if (!hasPacket(source, PedestalTransportTypes.ENERGY.get())) continue;
                taken = source.getStorage(PedestalTransportTypes.ENERGY.get()).extract(EnergyTransportType.ENERGY, needed - found, transaction);
                Vec3 from = topOf(source);
                if (taken > 0) particles.add(() -> PedestalTransportTypes.ENERGY.get().sendTransferParticles(level, from, to, EnergyTransportType.ENERGY));
            }
            if (taken > 0 && firstSource == null) firstSource = source;
            found += taken;
        }
        return found >= needed ? firstSource : null;
    }

    // ---- Inputs and their ghosts ----

    // The recipe's ingredients, fluid and energy at this level, times how far past the maximum it is
    private static List<Input> inputs(ServerLevel level, AnvilCost cost, int enchantmentLevel, int times)
    {
        List<Input> inputs = new ArrayList<>();
        for (AnvilCost.AnvilIngredient ingredient : cost.ingredientsAt(enchantmentLevel))
        {
            ItemStack display = ingredient.ingredient().display().resolveForFirstStack(SlotDisplayContext.fromLevel(level));
            if (display.isEmpty()) display = new ItemStack(Items.BARRIER);
            boolean potion = display.has(DataComponents.POTION_CONTENTS);
            inputs.add(new Input(display.copyWithCount(1), ingredient.ingredient(), null, potion ? 1 : ingredient.count() * times, potion));
        }
        if (cost.fluid().isPresent())
        {
            inputs.add(new Input(packet(PedestalTransportTypes.FLUIDS.get()), null, cost.fluid().get(), cost.fluid().get().amount() * times, false));
        }
        if (cost.energy() > 0) inputs.add(new Input(packet(PedestalTransportTypes.ENERGY.get()), null, null, cost.energy() * times, false));
        return inputs;
    }

    // Each input over the first input pedestal that has some of it, else the next empty input pedestal, else
    // circling this pedestal; labelled with what it needs right now
    private static List<GhostItem> ghosts(List<BasePedestalBlockEntity> pedestals, List<Input> inputs, List<Integer> multipliers)
    {
        List<BasePedestalBlockEntity> empty = new ArrayList<>();
        for (BasePedestalBlockEntity source : pedestals) if (source.getItemStacks().isEmpty()) empty.add(source);
        int nextEmpty = 0;
        List<GhostItem> ghosts = new ArrayList<>();
        for (int index = 0; index < inputs.size(); index++)
        {
            Input input = inputs.get(index);
            int needed = input.needed(multipliers.get(index)) - available(pedestals, input);
            if (needed <= 0) continue;
            BasePedestalBlockEntity at = supplier(pedestals, input);
            if (at == null && nextEmpty < empty.size()) at = empty.get(nextEmpty++);
            Optional<BlockPos> above = at == null ? Optional.empty() : Optional.of(at.getBlockPos());
            if (input.item() != null)
            {
                MutableComponent label = Component.literal("x" + needed);
                Component effects = potionEffects(input.display());
                if (effects != null) label.append(" ").append(effects);
                ghosts.add(new GhostItem(above, input.display(), label, Optional.empty()));
            }
            else if (input.fluid() != null)
            {
                List<Holder<Fluid>> fluids = input.fluid().ingredient().fluids();
                Component name = fluids.isEmpty() ? PedestalTransportTypes.FLUIDS.get().getName() : fluids.getFirst().value().getFluidType().getDescription();
                ghosts.add(new GhostItem(above, input.display(), name, Optional.of(Component.literal(needed + " mB"))));
            }
            else
            {
                ghosts.add(new GhostItem(above, input.display(), PedestalTransportTypes.ENERGY.get().getName(), Optional.of(Component.literal(needed + " FE"))));
            }
        }
        return ghosts;
    }

    // How much of this input the input pedestals have between them
    private static int available(List<BasePedestalBlockEntity> pedestals, Input input)
    {
        long total = 0;
        for (BasePedestalBlockEntity source : pedestals)
        {
            if (input.item() != null)
            {
                for (ItemStack stack : source.getItemStacks())
                {
                    if (!StoragePacketItem.isPacket(stack) && input.item().test(stack)) total += stack.getCount();
                }
            }
            else if (input.fluid() != null)
            {
                FluidResource stored = source.getStorage(PedestalTransportTypes.FLUIDS.get()).getResource(0);
                if (hasPacket(source, PedestalTransportTypes.FLUIDS.get()) && !stored.isEmpty() && input.fluid().ingredient().test(stored.toStack(1)))
                {
                    total += source.getStorage(PedestalTransportTypes.FLUIDS.get()).getAmountAsInt(0);
                }
            }
            else if (hasPacket(source, PedestalTransportTypes.ENERGY.get()))
            {
                total += source.getStorage(PedestalTransportTypes.ENERGY.get()).getAmountAsInt(0);
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    // The first input pedestal that has some of this input
    private static @Nullable BasePedestalBlockEntity supplier(List<BasePedestalBlockEntity> pedestals, Input input)
    {
        for (BasePedestalBlockEntity source : pedestals)
        {
            if (input.item() != null)
            {
                for (ItemStack stack : source.getItemStacks())
                {
                    if (!StoragePacketItem.isPacket(stack) && input.item().test(stack)) return source;
                }
            }
            else if (input.fluid() != null)
            {
                FluidResource stored = source.getStorage(PedestalTransportTypes.FLUIDS.get()).getResource(0);
                if (hasPacket(source, PedestalTransportTypes.FLUIDS.get()) && !stored.isEmpty() && input.fluid().ingredient().test(stored.toStack(1))) return source;
            }
            else if (hasPacket(source, PedestalTransportTypes.ENERGY.get())) return source;
        }
        return null;
    }

    // A potion's effects as its tooltip names them ("Harming II"), or null
    private static @Nullable Component potionEffects(ItemStack stack)
    {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return null;
        MutableComponent text = null;
        for (MobEffectInstance effect : contents.getAllEffects())
        {
            Component name = effect.getEffect().value().getDisplayName();
            MutableComponent line = effect.getAmplifier() > 0
                    ? Component.translatable("potion.withAmplifier", name, Component.translatable("potion.potency." + effect.getAmplifier()))
                    : name.copy();
            if (text == null) text = line;
            else text.append(", ").append(line);
        }
        return text;
    }

    // ---- Small helpers ----

    private static ItemEnchantments bookEnchantments(ItemStack book)
    {
        return book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    // The input pedestals, in the order they were selected
    private static List<BasePedestalBlockEntity> inputPedestals(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        List<BasePedestalBlockEntity> pedestals = new ArrayList<>();
        for (BlockPos pos : pedestal.getWorkPositions())
        {
            if (!pos.equals(pedestal.getBlockPos()) && level.getBlockEntity(pos) instanceof BasePedestalBlockEntity input) pedestals.add(input);
        }
        return pedestals;
    }

    private static boolean hasPacket(BasePedestalBlockEntity pedestal, PedestalTransportType<?> type)
    {
        for (ItemStack stack : pedestal.getItemStacks())
        {
            if (stack.getItem() instanceof StoragePacketItem packet && packet.getType() == type) return true;
        }
        return false;
    }

    private static ItemStack packet(PedestalTransportType<?> type)
    {
        StoragePacketItem item = StoragePacketItem.forType(type);
        return item != null ? new ItemStack(item) : new ItemStack(Items.BARRIER);
    }

    private static Vec3 topOf(BasePedestalBlockEntity pedestal)
    {
        return Vec3.atCenterOf(pedestal.getBlockPos()).add(pedestal.getFacing().getUnitVec3().scale(0.5));
    }
}
