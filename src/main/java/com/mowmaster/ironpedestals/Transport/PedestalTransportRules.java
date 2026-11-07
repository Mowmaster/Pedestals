package com.mowmaster.ironpedestals.Transport;

import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import com.mowmaster.mowlib.util.MowLibXpUtils;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.Config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.IntSupplier;

// What a PEDESTAL does with each transport type: how much a link moves, how much it holds (both depend on the
// pedestal's tier, see PedestalTier / Config.TIERS) and how upgrades scale. The types themselves (MowLib's
// TransportType) only know what the thing is; these numbers are Pedestals' own (they were PedestalTransportType's getTransferRate, capacity modifier...).
//
// Add-on types: register rules for your type in FMLCommonSetupEvent with PedestalTransportRules.register(...).
// A type without rules is stored but never moved (transfer rate 0, capacity 0). pedestal.getTier() tells your rules
// which tier the pedestal is.
public interface PedestalTransportRules
{
    // How much one link transfer may move per transfer tick, for the pedestal's tier (the type's units)
    int transferRate(BasePedestalBlockEntity pedestal);

    // How big the pedestal's storage is, for its tier (items: stacks, fluids: mB per tank, energy: FE, xp: points)
    int capacity(BasePedestalBlockEntity pedestal);

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

    // Pedestals' own rules for MowLib's four types, from the tier's config values.
    // Called from Pedestals' FMLCommonSetupEvent (types are registered by then).
    static void registerBuiltIn()
    {
        register(TransportTypes.ITEMS.get(), new BuiltIn("upgradeitemcapacity", Config.TierValues::itemRate, Config.TierValues::itemStacks, false));
        register(TransportTypes.FLUIDS.get(), new BuiltIn("upgradefluidcapacity", Config.TierValues::fluidRate, Config.TierValues::fluidCapacity, false));
        register(TransportTypes.ENERGY.get(), new BuiltIn("upgradeenergycapacity", Config.TierValues::energyRate, Config.TierValues::energyCapacity, false));
        register(TransportTypes.EXPERIENCE.get(), new BuiltIn("upgradexpcapacity", Config.TierValues::xpRate, Config.TierValues::xpCapacity, true));
    }

    // One built-in type: which of the tier's values are its rate and its storage. levels = the config counts
    // experience levels, turned into points (as in 1.20: the rate is counted from level 0, 1 level = 7 points).
    // Function<TierValues, IntSupplier>: given a tier's values, picks one of them (Config.TierValues::itemRate is
    // the record's accessor used as a function).
    record BuiltIn(String modifier, Function<Config.TierValues, IntSupplier> rate, Function<Config.TierValues, IntSupplier> storage,
                   boolean levels) implements PedestalTransportRules
    {
        @Override
        public int transferRate(BasePedestalBlockEntity pedestal)
        {
            int amount = rate.apply(pedestal.getTierValues()).getAsInt();
            return levels ? MowLibXpUtils.pointsForLevel(amount) : amount;
        }

        @Override
        public int capacity(BasePedestalBlockEntity pedestal)
        {
            int amount = storage.apply(pedestal.getTierValues()).getAsInt();
            return levels ? MowLibXpUtils.pointsForLevel(amount) : amount;
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

        // Items, fluids and energy: tier 0's rate, whatever the pedestal's tier (tiers speed up links, not upgrades);
        // experience: the pedestal's link rate, as before
        @Override
        public int defaultUpgradeRate(BasePedestalBlockEntity pedestal)
        {
            return levels ? transferRate(pedestal) : rate.apply(Config.TIERS.getFirst()).getAsInt();
        }
    }
}
