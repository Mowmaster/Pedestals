package com.mowmaster.pedestals.Transport;

import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import com.mowmaster.mowlib.util.MowLibXpUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

// What a PEDESTAL does with each transport type: how much a link moves, how much it holds, what augments add and
// how upgrades scale. The types themselves (MowLib's TransportType) only know what the thing is; these numbers are
// Pedestals' own (they were PedestalTransportType's getTransferRate, augment bonuses, capacity modifier...).
//
// Add-on types: register rules for your type in FMLCommonSetupEvent with PedestalTransportRules.register(...).
// A type without rules is stored but never moved (transfer rate 0, capacity 0).
public interface PedestalTransportRules
{
    // How much one link transfer may move per transfer tick, capacity augments included (the type's units)
    int transferRate(BasePedestalBlockEntity pedestal);

    // How big the pedestal's storage is, storage augments included (items: stacks, fluids: mB, energy: FE, xp: points)
    int capacity(BasePedestalBlockEntity pedestal);

    // What one capacity augment of this tier (1-4) adds to the transfer rate, and one storage augment to the
    // capacity, in the units transferRate/capacity add them in (experience: levels)
    default int capacityAugmentBonus(int tier)
    {
        return 0;
    }

    default int storageAugmentBonus(int tier)
    {
        return 0;
    }

    // The modifier that raises upgrades' rates for this type. Default "upgrade<path>capacity".
    default String capacityModifier(TransportType<?> type)
    {
        return "upgrade" + type.getId().getPath() + "capacity";
    }

    // How much an upgrade moves per operation: its base rate plus the capacity modifier
    default int upgradeRate(int baseRate, int capacityModifier)
    {
        return baseRate + capacityModifier;
    }

    // The base rate for upgrades that have no config for this type: the link transfer rate
    default int defaultUpgradeRate(BasePedestalBlockEntity pedestal)
    {
        return transferRate(pedestal);
    }

    // ---- Lookup ----

    Map<TransportType<?>, PedestalTransportRules> RULES = new ConcurrentHashMap<>();

    PedestalTransportRules NONE = new PedestalTransportRules()
    {
        @Override
        public int transferRate(BasePedestalBlockEntity pedestal)
        {
            return 0;
        }

        @Override
        public int capacity(BasePedestalBlockEntity pedestal)
        {
            return 0;
        }
    };

    static PedestalTransportRules of(TransportType<?> type)
    {
        return RULES.getOrDefault(type, NONE);
    }

    static void register(TransportType<?> type, PedestalTransportRules rules)
    {
        RULES.put(type, rules);
    }

    // Pedestals' own rules for MowLib's four types, from the config (same numbers as before the move).
    // Called from Pedestals' FMLCommonSetupEvent (types are registered by then).
    static void registerBuiltIn()
    {
        register(TransportTypes.ITEMS.get(), new BuiltIn("upgradeitemcapacity",
                Config.ITEM_TRANSFER_RATE::getAsInt, Config.ITEM_SLOTS::getAsInt, augment -> augment.items().getAsInt(), false));
        register(TransportTypes.FLUIDS.get(), new BuiltIn("upgradefluidcapacity",
                Config.FLUID_TRANSFER_RATE::getAsInt, Config.FLUID_STORAGE::getAsInt, augment -> augment.fluids().getAsInt(), false));
        register(TransportTypes.ENERGY.get(), new BuiltIn("upgradeenergycapacity",
                Config.ENERGY_TRANSFER_RATE::getAsInt, Config.ENERGY_STORAGE::getAsInt, augment -> augment.energy().getAsInt(), false));
        register(TransportTypes.EXPERIENCE.get(), new BuiltIn("upgradexpcapacity",
                Config.XP_TRANSFER_RATE::getAsInt, Config.XP_STORAGE::getAsInt, augment -> augment.experience().getAsInt(), true));
    }

    // One built-in type. levels = the config and augments count experience levels, turned into points
    // (as in 1.20: the rate is counted from level 0, 1 level = 7 points).
    record BuiltIn(String modifier, Supplier<Integer> rate, Supplier<Integer> storage,
                   Function<Config.AmountAugment, Integer> augmentAmount, boolean levels) implements PedestalTransportRules
    {
        @Override
        public int transferRate(BasePedestalBlockEntity pedestal)
        {
            int amount = rate.get() + pedestal.getTransferBonus(this);
            return levels ? MowLibXpUtils.pointsForLevel(amount) : amount;
        }

        @Override
        public int capacity(BasePedestalBlockEntity pedestal)
        {
            long amount = (long) storage.get() + pedestal.getStorageBonus(this);
            int clamped = (int) Math.min(Integer.MAX_VALUE, amount);
            return levels ? MowLibXpUtils.pointsForLevel(clamped) : clamped;
        }

        @Override
        public int capacityAugmentBonus(int tier)
        {
            return bonus(Config.CAPACITY_AUGMENTS, tier);
        }

        @Override
        public int storageAugmentBonus(int tier)
        {
            return bonus(Config.STORAGE_AUGMENTS, tier);
        }

        private int bonus(List<Config.AmountAugment> augments, int tier)
        {
            return tier < 1 || tier > augments.size() ? 0 : augmentAmount.apply(augments.get(tier - 1));
        }

        @Override
        public String capacityModifier(TransportType<?> type)
        {
            return modifier;
        }

        @Override
        public int upgradeRate(int baseRate, int capacityModifier)
        {
            return levels ? baseRate + MowLibXpUtils.pointsForLevel(capacityModifier) : baseRate + capacityModifier;
        }

        // Items, fluids and energy: the plain config rate (no augments); experience: the link rate, as before
        @Override
        public int defaultUpgradeRate(BasePedestalBlockEntity pedestal)
        {
            return levels ? transferRate(pedestal) : rate.get();
        }
    }
}
