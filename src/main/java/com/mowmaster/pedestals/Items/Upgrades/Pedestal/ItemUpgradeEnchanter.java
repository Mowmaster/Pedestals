package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Transport.ExperienceTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

// An enchanting table that pays with the pedestal's experience (1.16's ItemUpgradeExpEnchanter). Takes the first
// item from the inventory below: enchantable things (and books) are enchanted one at a time and put in the pedestal,
// anything else is passed straight through so it doesn't block the inventory.
//
// Power comes from the blocks on a locations work card instead of a fixed ring around the pedestal: each block gives
// twice its enchanting power bonus (a bookshelf gives 2, so 15 bookshelves = 30, the table's best), up to MAX_POWER.
// Blocks from other mods count too (NeoForge's getEnchantPowerBonus). While there's anything in the inventory below,
// enchanting particles stream from those blocks to the pedestal.
//
// Modifiers: speed, range (how far the card's blocks may be), and two of its own, which do nothing until the upgrade
// has at least one level of them. Each then has a (power / 10 + modifier)% chance per enchant:
//   treasure chance - one enchantment ends up a level past its maximum (Sharpness VI)
//   purified - each curse on the result has that chance to be removed
public class ItemUpgradeEnchanter extends ItemUpgradeBase
{
    public static final int MAX_POWER = 100;
    // Set while the inventory below has items, for the particles
    private static final String HAS_ITEMS = "has_items";

    public ItemUpgradeEnchanter(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.RANGE, UpgradeModifiers.TREASURE_CHANCE, UpgradeModifiers.PURIFIED);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.LOCATIONS;
    }

    @Override
    public void tick(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        super.tick(level, pedestal, upgrade);
        if (!pedestal.isPowered() && pedestal.getUpgradeData().getBooleanOr(HAS_ITEMS, false)) streamParticles(level, pedestal);
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        ResourceHandler<ItemResource> inventory = getInventoryBelow(level, pedestal);
        int index = inventory == null ? -1 : firstItem(inventory, pedestal);
        setHasItems(pedestal, index >= 0);
        if (index < 0) return;

        ItemResource resource = inventory.getResource(index);
        ItemStack input = resource.toStack(1);
        if (input.isEnchantable() || input.is(Items.BOOK)) enchant(level, pedestal, upgrade, inventory, index, resource, input);
        else passThrough(pedestal, inventory, index, resource);
    }

    // The first slot with an item the pedestal's filter allows, or -1
    private static int firstItem(ResourceHandler<ItemResource> inventory, BasePedestalBlockEntity pedestal)
    {
        for (int index = 0; index < inventory.size(); index++)
        {
            ItemResource resource = inventory.getResource(index);
            if (!resource.isEmpty() && pedestal.machineFilterAllows(resource.toStack(1))) return index;
        }
        return -1;
    }

    private static void setHasItems(BasePedestalBlockEntity pedestal, boolean hasItems)
    {
        if (pedestal.getUpgradeData().getBooleanOr(HAS_ITEMS, false) == hasItems) return;
        pedestal.getUpgradeData().putBoolean(HAS_ITEMS, hasItems);
        pedestal.upgradeDataChanged();
    }

    // ---- Enchanting ----

    private void enchant(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade, ResourceHandler<ItemResource> inventory,
                         int index, ItemResource resource, ItemStack input)
    {
        int power = Math.max(1, getPower(level, pedestal)); // the table also charges at least 1 level
        int cost = experienceCost(power);
        if (pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get()).getAmountAsInt(0) < cost) return;

        RandomSource random = level.getRandom();
        List<EnchantmentInstance> enchantments = selectEnchantments(level, random, input, power);
        if (enchantments.isEmpty()) return; // nothing it can get at this power: wait (the power may change)

        int chance = power / 10;
        if (hasModifier(upgrade, UpgradeModifiers.TREASURE_CHANCE) && random.nextInt(100) < chance + getModifier(upgrade, UpgradeModifiers.TREASURE_CHANCE))
        {
            addTreasureLevel(random, enchantments);
        }
        if (hasModifier(upgrade, UpgradeModifiers.PURIFIED))
        {
            int purifyChance = chance + getModifier(upgrade, UpgradeModifiers.PURIFIED);
            enchantments.removeIf(instance -> instance.enchantment().is(EnchantmentTags.CURSE) && random.nextInt(100) < purifyChance);
            if (enchantments.isEmpty()) return;
        }

        // Like EnchantmentHelper.enchantItem: books become enchanted books (enchant() then stores them, not applies them)
        ItemStack result = input.is(Items.BOOK) ? new ItemStack(Items.ENCHANTED_BOOK) : input.copyWithCount(1);
        for (EnchantmentInstance instance : enchantments) result.enchant(instance.enchantment(), instance.level());

        // All or nothing: the item out, the result in, the experience paid
        try (Transaction transaction = Transaction.openRoot())
        {
            if (inventory.extract(index, resource, 1, transaction) != 1) return;
            if (ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(result), 1, transaction) != 1) return;
            if (pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get()).extract(ExperienceTransportType.EXPERIENCE, cost, transaction) != cost) return;
            transaction.commit();
        }
        level.playSound(null, pedestal.getBlockPos(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.35F, 1.0F);
    }

    // Not enchantable (or already enchanted): move the whole stack into the pedestal, as much as fits
    private static void passThrough(BasePedestalBlockEntity pedestal, ResourceHandler<ItemResource> inventory, int index, ItemResource resource)
    {
        try (Transaction transaction = Transaction.openRoot())
        {
            int moved = ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), resource, inventory.getAmountAsInt(index), transaction);
            if (moved <= 0 || inventory.extract(index, resource, moved, transaction) != moved) return;
            transaction.commit();
        }
    }

    // Twice the enchanting power of the work card's blocks, at most MAX_POWER
    public int getPower(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        float bonus = 0;
        for (BlockPos pos : pedestal.getWorkPositions())
        {
            if (level.isLoaded(pos)) bonus += level.getBlockState(pos).getEnchantPowerBonus(level, pos);
        }
        return Math.min(MAX_POWER, Math.round(bonus * 2));
    }

    // Experience for one enchant: what a level 30 player would pay for one level per 10 power (at least 1). 30 power
    // costs the 3 levels the table's top slot costs (306 points); 100 power costs 10 levels (845 points), which still
    // fits in a pedestal's default 30 levels of storage.
    public static int experienceCost(int power)
    {
        int levels = Mth.clamp(power / 10, 1, 30);
        return ExperienceTransportType.pointsForLevel(30) - ExperienceTransportType.pointsForLevel(30 - levels);
    }

    // EnchantmentHelper.selectEnchantment with two changes:
    //  - the pool is minecraft:on_random_loot (as 1.16 allowed treasure): the table's enchantments plus mending,
    //    frost walker and the curses
    //  - an enchantment whose top level the power has gone past stays at its top level instead of dropping out
    //    (vanilla never goes past 30 power, so it never needed this)
    private static List<EnchantmentInstance> selectEnchantments(ServerLevel level, RandomSource random, ItemStack stack, int power)
    {
        List<EnchantmentInstance> results = new ArrayList<>();
        Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
        if (enchantable == null) return results;

        int cost = power + 1 + random.nextInt(enchantable.value() / 4 + 1) + random.nextInt(enchantable.value() / 4 + 1);
        float span = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
        cost = Mth.clamp(Math.round(cost + cost * span), 1, Integer.MAX_VALUE);

        List<EnchantmentInstance> available = availableEnchantments(level, stack, cost);
        if (available.isEmpty()) return results;
        WeightedRandom.getRandomItem(random, available, EnchantmentInstance::weight).ifPresent(results::add);
        while (random.nextInt(50) <= cost)
        {
            if (!results.isEmpty()) EnchantmentHelper.filterCompatibleEnchantments(available, results.getLast());
            if (available.isEmpty()) break;
            WeightedRandom.getRandomItem(random, available, EnchantmentInstance::weight).ifPresent(results::add);
            cost /= 2;
        }
        return results;
    }

    private static List<EnchantmentInstance> availableEnchantments(ServerLevel level, ItemStack stack, int cost)
    {
        List<EnchantmentInstance> results = new ArrayList<>();
        boolean book = stack.is(Items.BOOK);
        // get(TagKey) gives the tag's contents, if the tag exists
        Stream<Holder<Enchantment>> pool = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(EnchantmentTags.ON_RANDOM_LOOT).map(HolderSet::stream).orElseGet(Stream::empty);
        // isPrimaryItemFor is NeoForge's (items can add or refuse table enchantments)
        pool.filter(holder -> book || stack.isPrimaryItemFor(holder)).forEach(holder -> {
            Enchantment enchantment = holder.value();
            for (int enchantLevel = enchantment.getMaxLevel(); enchantLevel >= enchantment.getMinLevel(); enchantLevel--)
            {
                boolean topLevel = enchantLevel == enchantment.getMaxLevel();
                if (cost >= enchantment.getMinCost(enchantLevel) && (cost <= enchantment.getMaxCost(enchantLevel) || topLevel))
                {
                    results.add(new EnchantmentInstance(holder, enchantLevel));
                    break;
                }
            }
        });
        return results;
    }

    // One enchantment that has levels (not mending, not a curse) goes to one past its maximum
    private static void addTreasureLevel(RandomSource random, List<EnchantmentInstance> enchantments)
    {
        List<Integer> candidates = new ArrayList<>();
        for (int index = 0; index < enchantments.size(); index++)
        {
            Holder<Enchantment> enchantment = enchantments.get(index).enchantment();
            if (enchantment.value().getMaxLevel() > 1 && !enchantment.is(EnchantmentTags.CURSE)) candidates.add(index);
        }
        if (candidates.isEmpty()) return;
        int chosen = candidates.get(random.nextInt(candidates.size()));
        Holder<Enchantment> enchantment = enchantments.get(chosen).enchantment();
        enchantments.set(chosen, new EnchantmentInstance(enchantment, enchantment.value().getMaxLevel() + 1));
    }

    // ---- Particles ----

    // The enchanting table's particles, from each power block to the top of the pedestal. ENCHANT particles are made
    // with a target and an offset and fly from target + offset to the target; with a count of 0, sendParticles sends
    // exactly one particle and uses the "spread" numbers as that offset.
    private static void streamParticles(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        RandomSource random = level.getRandom();
        Vec3 target = Vec3.atCenterOf(pedestal.getBlockPos()).add(pedestal.getFacing().getUnitVec3().scale(0.5));
        for (BlockPos pos : pedestal.getWorkPositions())
        {
            // About one every two seconds per block, close to the table's rate
            if (random.nextInt(40) != 0 || !level.isLoaded(pos) || level.getBlockState(pos).getEnchantPowerBonus(level, pos) <= 0) continue;
            double startX = pos.getX() + random.nextFloat();
            double startY = pos.getY() + 1.0 - random.nextFloat();
            double startZ = pos.getZ() + random.nextFloat();
            level.sendParticles(ParticleTypes.ENCHANT, target.x, target.y, target.z, 0,
                    startX - target.x, startY - target.y, startZ - target.z, 1.0);
        }
    }

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("pedestals.upgrade_tooltip_enchanter_power", MAX_POWER).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
