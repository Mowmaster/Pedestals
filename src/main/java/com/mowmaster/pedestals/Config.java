package com.mowmaster.pedestals;

import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalStorageHost;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

// Pedestal and upgrade settings, same names and defaults as 1.20's PedestalConfig (the template's example values
// are gone). Augment values come back with the augments; dust values and the packets' discharge timers are gone.
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.push("pedestal");
    }

    public static final ModConfigSpec.IntValue TICKS_TO_TRANSFER = BUILDER
            .comment("Ticks between transfers to linked pedestals (before augments), and between upgrade operations (before speed modifiers)")
            .defineInRange("pedestal_ticksToTransfer", 40, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue LINKING_RANGE = BUILDER
            .comment("How far (in blocks, on each axis) a pedestal can link (before augments)")
            .defineInRange("pedestal_baseLinkingRange", 8, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue MAX_LINKS = BUILDER
            .comment("How many pedestals one pedestal can send to")
            .defineInRange("pedestal_maxLinks", 8, 1, 64);

    public static final ModConfigSpec.IntValue ITEM_TRANSFER_RATE = BUILDER
            .comment("Items sent per transfer")
            .defineInRange("pedestal_baseItemTransferRate", 4, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue FLUID_TRANSFER_RATE = BUILDER
            .comment("Fluid (mB) sent per transfer")
            .defineInRange("pedestal_baseFluidTransferRate", 1000, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue ENERGY_TRANSFER_RATE = BUILDER
            .comment("Energy sent per transfer")
            .defineInRange("pedestal_baseEnergyTransferRate", 5000, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue XP_TRANSFER_RATE = BUILDER
            .comment("Experience sent per transfer, in levels (counted from level 0)")
            .defineInRange("pedestal_baseXpTransferRate", 1, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ITEM_SLOTS = BUILDER
            .comment("Stacks a pedestal holds (before augments)")
            .defineInRange("pedestal_baseItemStacks", 1, 1, 27);
    public static final ModConfigSpec.IntValue FLUID_STORAGE = BUILDER
            .comment("Fluid a pedestal holds, in mB (before augments)")
            .defineInRange("pedestal_baseFluidStorage", 16000, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue ENERGY_STORAGE = BUILDER
            .comment("Energy a pedestal holds (before augments)")
            .defineInRange("pedestal_baseEnergyStorage", 20000, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue XP_STORAGE = BUILDER
            .comment("Experience a pedestal holds, in levels (before augments)")
            .defineInRange("pedestal_baseXpStorage", 30, 1, 21000);

    static {
        BUILDER.pop();
        BUILDER.push("upgrades");
    }

    // ---- Every upgrade ----

    public static final ModConfigSpec.BooleanValue BREAK_BLOCK_ENTITIES = BUILDER
            .comment("Lets breakers, quarries and pumps break blocks with block entities (chests, machines...)")
            .define("allowBreakEntities", false);
    public static final ModConfigSpec.BooleanValue REQUIRE_SIZED_AREA = BUILDER
            .comment("Limit a work card area's size by the upgrade's area modifier (3x3x3 without modifiers)")
            .define("upgrade_toggleable_require_selectable_size", false);
    public static final ModConfigSpec.IntValue SELECTION_RANGE = BUILDER
            .comment("How far (in blocks, on each axis) a work card's positions can be from the pedestal (before range modifiers)")
            .defineInRange("upgrades_baseSelectionRange", 8, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue PACKAGE_MAX_STACKS = BUILDER
            .comment("Most item stacks the packager puts in one package")
            .defineInRange("bulkstorage_item_max_stacks", 54, 1, Integer.MAX_VALUE);

    // ---- Transfer rates (per operation, before capacity modifiers). Experience is in points. ----

    public static final TransferRates IMPORT_RATES = TransferRates.define("import", 4, 1000, 5000, 7);
    public static final TransferRates EXPORT_RATES = TransferRates.define("export", 4, 1000, 5000, 7);
    public static final TransferRates DROPPER_RATES = TransferRates.define("dropper", 1, 1000, 5000, 7);

    public static final ModConfigSpec.BooleanValue DROPPER_LIGHTNING = BUILDER
            .comment("The dropper can drop lightning bolts with energy (each bolt costs the dropper's energy amount)")
            .define("upgrade_dropper_can_drop_bolt", true);

    // ---- Tools: whether an upgrade needs a tool inserted, and whether it wears it down ----

    public static final ToolRules BREAKER_TOOLS = ToolRules.define("blockBreaker", "Block Breaker");
    public static final ToolRules QUARRY_TOOLS = ToolRules.define("quarry", "Quarry");
    public static final ToolRules CHOPPER_TOOLS = ToolRules.define("chopper", "Chopper");
    public static final ToolRules HARVESTER_TOOLS = ToolRules.define("harvester", "Harvester");
    public static final ToolRules HIVE_HARVESTER_TOOLS = ToolRules.define("hiveharvester", "Hive Harvester");
    public static final ToolRules SHEARER_TOOLS = ToolRules.define("shearer", "Shearer");
    public static final ToolRules ATTACKER_TOOLS = ToolRules.define("attacker", "Attacker");
    public static final ToolRules MATERIAL_GENERATOR_TOOLS = ToolRules.define("materialGen", "Material Generator");

    // ---- Fuel: energy/experience each operation costs ----

    public static final FuelCost MAGNET_FUEL = FuelCost.define("magnet", "Magnet", 0, true);
    public static final FuelCost BREAKER_FUEL = FuelCost.define("blockbreaker", "Block Breaker", 0, true);
    public static final FuelCost PLACER_FUEL = FuelCost.define("blockplacer", "Block Placer", 0, true);
    public static final FuelCost FILLER_FUEL = FuelCost.define("filler", "Filler", 0, true);
    public static final FuelCost QUARRY_FUEL = FuelCost.define("quarry", "Quarry", 500, false);
    public static final FuelCost CHOPPER_FUEL = FuelCost.define("chopper", "Chopper", 0, true);
    public static final FuelCost HARVESTER_FUEL = FuelCost.define("harvester", "Harvester", 0, true);
    public static final FuelCost PLANTER_FUEL = FuelCost.define("planter", "Planter", 0, true);
    public static final FuelCost FERTILIZER_FUEL = FuelCost.define("fertilizer", "Fertilizer", 0, true);
    public static final FuelCost HIVE_HARVESTER_FUEL = FuelCost.define("hiveharvester", "Hive Harvester", 0, true);
    public static final FuelCost PUMP_FUEL = FuelCost.define("pump", "Pump", 0, true);
    public static final FuelCost DRAIN_FUEL = FuelCost.define("drain", "Drain", 0, true);
    public static final FuelCost SHEARER_FUEL = FuelCost.define("sheerer", "Shearer", 0, true);
    public static final FuelCost MILKER_FUEL = FuelCost.define("milker", "Milker", 0, true);
    public static final FuelCost BREEDER_FUEL = FuelCost.define("breeder", "Mob Breeder", 0, true);
    public static final FuelCost ATTACKER_FUEL = FuelCost.define("attacker", "Attacker", 0, true);
    public static final FuelCost FAN_FUEL = FuelCost.define("fan", "Fan", 0, true);

    // ---- Per-upgrade settings ----

    public static final ModConfigSpec.IntValue FILLER_BLOCKS = BUILDER
            .comment("Blocks the filler places per operation (before block capacity modifiers)")
            .defineInRange("upgrade_filler_base_blocks_placed", 4, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue QUARRY_BLOCKS = BUILDER
            .comment("Blocks the quarry mines per operation (before block capacity modifiers)")
            .defineInRange("upgrade_quarry_base_blocks_mined", 4, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue PUMP_BLOCKS = BUILDER
            .comment("Fluid blocks the pump takes per operation (before block capacity modifiers)")
            .defineInRange("upgrade_pump_base_blocks_mined", 4, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue PUMP_WATERLOGGED = BUILDER
            .comment("The pump also takes water out of waterlogged blocks")
            .define("upgrade_pump_waterlogged", false);
    public static final ModConfigSpec.IntValue DRAIN_BLOCKS = BUILDER
            .comment("Fluid blocks the drain places per operation (before block capacity modifiers)")
            .defineInRange("upgrade_drain_base_blocks_placed", 4, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue BREEDER_LIMIT = BUILDER
            .comment("The mob breeder stops when there are too many animals in its area")
            .define("upgrade_breeder_has_limit", false);
    public static final ModConfigSpec.IntValue BREEDER_LIMIT_COUNT = BUILDER
            .comment("How many animals is too many (with the limit on)")
            .defineInRange("upgrade_breeder_limit_count", 20, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue SMELTER_ENERGY = BUILDER
            .comment("Energy the smelter uses per tick of cooking")
            .defineInRange("upgrade_smelter_base_energy_cost", 20, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue BLASTER_ENERGY = BUILDER
            .comment("Energy the blast furnace upgrade uses per tick of cooking")
            .defineInRange("upgrade_blast_base_energy_cost", 20, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue SMOKER_ENERGY = BUILDER
            .comment("Energy the smoker upgrade uses per tick of cooking")
            .defineInRange("upgrade_smoker_base_energy_cost", 20, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue GENERATOR_ENERGY = BUILDER
            .comment("Energy the generator makes per tick of burning fuel")
            .defineInRange("upgrade_generator_base_energy_production", 20, 0, Integer.MAX_VALUE);

    // ---- Sacrifice upgrade and sacrifice reactor ----

    public static final ModConfigSpec.BooleanValue SACRIFICE_RESET_ALL = BUILDER
            .comment("When the sacrifice upgrade uses up its book, reset every input's cost (false: only the input that ran short)")
            .define("upgrade_sacrifice_reset_all_inputs", true);
    public static final ModConfigSpec.IntValue REACTOR_ENERGY_PER_XP = BUILDER
            .comment("Energy the sacrifice reactor makes per experience point it burns, at tier 1 (before the other multipliers)")
            .defineInRange("upgrade_reactor_energy_per_xp", 100, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue REACTOR_TIER_BONUS = BUILDER
            .comment("Percent more energy per experience point for each tier above 1 (100 = tier 2 makes double, tier 3 triple...)")
            .defineInRange("upgrade_reactor_tier_bonus_percent", 100, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue REACTOR_VARIETY_BONUS = BUILDER
            .comment("Percent more energy for each different kind of sacrifice among the recently burned ones, after the first")
            .defineInRange("upgrade_reactor_variety_bonus_percent", 50, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue REACTOR_WEAR = BUILDER
            .comment("Percent less energy each time the same kind of sacrifice is burned again (it recovers when it drops out of the recent ones)")
            .defineInRange("upgrade_reactor_wear_percent", 5, 0, 100);
    public static final ModConfigSpec.IntValue REACTOR_RECENT = BUILDER
            .comment("How many of the last burned sacrifices count as recent (for wear and variety)")
            .defineInRange("upgrade_reactor_recent_count", 10, 1, 1000);
    public static final ModConfigSpec.IntValue REACTOR_BURN_SECONDS = BUILDER
            .comment("Seconds a sacrifice burns per tier (tier 1 = this, tier 2 = twice this...)")
            .defineInRange("upgrade_reactor_burn_seconds_per_tier", 8, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue REACTOR_RANGE_BASE = BUILDER
            .comment("Blocks the reactor's effects reach at tier 0 (like a beacon: base + per tier x tier)")
            .defineInRange("upgrade_reactor_effect_range_base", 10, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue REACTOR_RANGE_PER_TIER = BUILDER
            .comment("Extra blocks of effect range per tier")
            .defineInRange("upgrade_reactor_effect_range_per_tier", 10, 0, Integer.MAX_VALUE);

    // ---- Augments (tiers 1-4, same values as 1.20). Each list is tier 1 first. ----

    public static final List<AmountAugment> CAPACITY_AUGMENTS = List.of(
            AmountAugment.define("capacity", 1, 2, 2000, 5000, 1, 4),
            AmountAugment.define("capacity", 2, 4, 4000, 10000, 5, 8),
            AmountAugment.define("capacity", 3, 8, 8000, 100000, 10, 12),
            AmountAugment.define("capacity", 4, 16, 16000, 1000000, 15, 16));
    public static final List<AmountAugment> STORAGE_AUGMENTS = List.of(
            AmountAugment.define("storage", 1, 1, 4000, 20000, 10, 3),
            AmountAugment.define("storage", 2, 1, 16000, 100000, 15, 7),
            AmountAugment.define("storage", 3, 1, 64000, 1000000, 20, 11),
            AmountAugment.define("storage", 4, 1, 265000, 10000000, 25, 15));
    public static final List<SimpleAugment> SPEED_AUGMENTS = List.of(
            SimpleAugment.define("speed", "Ticks taken off the time between link transfers", 1, 2, 5),
            SimpleAugment.define("speed", "Ticks taken off the time between link transfers", 2, 4, 5),
            SimpleAugment.define("speed", "Ticks taken off the time between link transfers", 3, 6, 5),
            SimpleAugment.define("speed", "Ticks taken off the time between link transfers", 4, 8, 5));
    public static final List<SimpleAugment> RANGE_AUGMENTS = List.of(
            SimpleAugment.define("range", "Blocks added to the linking range", 1, 2, 4),
            SimpleAugment.define("range", "Blocks added to the linking range", 2, 6, 4),
            SimpleAugment.define("range", "Blocks added to the linking range", 3, 8, 5),
            SimpleAugment.define("range", "Blocks added to the linking range", 4, 8, 7));

    static {
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    // ---- Groups of values (records: see AmountResource) ----

    // Base rates for an upgrade that moves every transport type. Add-on types use their default upgrade rate.
    public record TransferRates(ModConfigSpec.IntValue items, ModConfigSpec.IntValue fluids,
                                ModConfigSpec.IntValue energy, ModConfigSpec.IntValue experience)
    {
        static TransferRates define(String upgrade, int items, int fluids, int energy, int experience)
        {
            String prefix = "upgrade_" + upgrade + "_base_";
            return new TransferRates(
                    BUILDER.comment("Items per operation [" + upgrade + "]").defineInRange(prefix + "item_rate", items, 0, Integer.MAX_VALUE),
                    BUILDER.comment("Fluid (mB) per operation [" + upgrade + "]").defineInRange(prefix + "fluid_rate", fluids, 0, Integer.MAX_VALUE),
                    BUILDER.comment("Energy per operation [" + upgrade + "]").defineInRange(prefix + "energy_rate", energy, 0, Integer.MAX_VALUE),
                    BUILDER.comment("Experience points per operation [" + upgrade + "]").defineInRange(prefix + "exp_rate", experience, 0, Integer.MAX_VALUE));
        }

        // The base rate for a type (before its capacity modifier)
        public int get(PedestalTransportType<?> type, PedestalStorageHost host)
        {
            if (type == PedestalTransportTypes.ITEMS.get()) return items.getAsInt();
            if (type == PedestalTransportTypes.FLUIDS.get()) return fluids.getAsInt();
            if (type == PedestalTransportTypes.ENERGY.get()) return energy.getAsInt();
            if (type == PedestalTransportTypes.EXPERIENCE.get()) return experience.getAsInt();
            return type.getDefaultUpgradeRate(host);
        }
    }

    // A capacity or storage augment tier: what one augment adds per built-in transport type (capacity: to the link
    // transfer rate; storage: to the storage, items as extra stacks), experience in levels, and how many can go in.
    // Add-on types give their own values (PedestalTransportType#getCapacityAugmentBonus / getStorageAugmentBonus).
    public record AmountAugment(ModConfigSpec.IntValue items, ModConfigSpec.IntValue fluids, ModConfigSpec.IntValue energy,
                                ModConfigSpec.IntValue experience, ModConfigSpec.IntValue insertable)
    {
        static AmountAugment define(String kind, int tier, int items, int fluids, int energy, int experience, int insertable)
        {
            String prefix = "augment_t" + tier + "_" + kind + "_";
            String name = " [" + kind + " augment, tier " + tier + "]";
            String what = kind.equals("capacity") ? "Added to the transfer rate: " : "Added to the storage: ";
            return new AmountAugment(
                    BUILDER.comment(what + (kind.equals("capacity") ? "items" : "item stacks") + name).defineInRange(prefix + "items", items, 0, Integer.MAX_VALUE),
                    BUILDER.comment(what + "fluid (mB)" + name).defineInRange(prefix + "fluids", fluids, 0, Integer.MAX_VALUE),
                    BUILDER.comment(what + "energy" + name).defineInRange(prefix + "energy", energy, 0, Integer.MAX_VALUE),
                    BUILDER.comment(what + "experience (levels)" + name).defineInRange(prefix + "experience", experience, 0, Integer.MAX_VALUE),
                    BUILDER.comment("How many can go in one pedestal" + name).defineInRange(prefix + "insertable", insertable, 1, 64));
        }
    }

    // A speed or range augment tier: what one augment does, and how many can go in
    public record SimpleAugment(ModConfigSpec.IntValue amount, ModConfigSpec.IntValue insertable)
    {
        static SimpleAugment define(String kind, String comment, int tier, int amount, int insertable)
        {
            String prefix = "augment_t" + tier + "_" + kind + "_";
            String name = " [" + kind + " augment, tier " + tier + "]";
            return new SimpleAugment(
                    BUILDER.comment(comment + name).defineInRange(prefix + "amount", amount, 0, Integer.MAX_VALUE),
                    BUILDER.comment("How many can go in one pedestal" + name).defineInRange(prefix + "insertable", insertable, 1, 64));
        }
    }

    public record ToolRules(ModConfigSpec.BooleanValue require, ModConfigSpec.BooleanValue damage)
    {
        static ToolRules define(String key, String name)
        {
            return new ToolRules(
                    BUILDER.comment(name + " only works with a tool inserted").define(key + "_requires_tools", false),
                    BUILDER.comment(name + " wears down the inserted tool").define(key + "_damages_tools", false));
        }
    }

    // cost = (base + selected * (distance or 1)) * multiplier, for energy and experience separately.
    // selected only counts when "selected_allowed" is on; distance is from the pedestal to the work position.
    public record FuelCost(ModConfigSpec.IntValue energy, ModConfigSpec.BooleanValue energyDistance, ModConfigSpec.DoubleValue energyMultiplier,
                           ModConfigSpec.IntValue experience, ModConfigSpec.BooleanValue experienceDistance, ModConfigSpec.DoubleValue experienceMultiplier,
                           ModConfigSpec.BooleanValue selectedAllowed, ModConfigSpec.DoubleValue selectedMultiplier)
    {
        static FuelCost define(String key, String name, int energy, boolean energyDistance)
        {
            String prefix = "upgrade_" + key + "_";
            return new FuelCost(
                    BUILDER.comment("Base energy cost per operation [" + name + "]").defineInRange(prefix + "base_energy_cost", energy, 0, Integer.MAX_VALUE),
                    BUILDER.comment("Distance to the work counts towards the energy cost (needs selected_allowed) [" + name + "]").define(prefix + "energy_distance_multiplier", energyDistance),
                    BUILDER.comment("Energy cost multiplier [" + name + "]").defineInRange(prefix + "energy_multiplier", 1.0D, 0.0D, Integer.MAX_VALUE),
                    BUILDER.comment("Base experience cost per operation [" + name + "]").defineInRange(prefix + "base_xp_cost", 0, 0, Integer.MAX_VALUE),
                    BUILDER.comment("Distance to the work counts towards the experience cost (needs selected_allowed) [" + name + "]").define(prefix + "xp_distance_multiplier", true),
                    BUILDER.comment("Experience cost multiplier [" + name + "]").defineInRange(prefix + "xp_multiplier", 1.0D, 0.0D, Integer.MAX_VALUE),
                    BUILDER.comment("Add a cost for how far the work is from the pedestal [" + name + "]").define(prefix + "selected_allowed", false),
                    BUILDER.comment("Cost per block of distance [" + name + "]").defineInRange(prefix + "selected_multiplier", 1.0D, 0.0D, Integer.MAX_VALUE));
        }

        public int energyCost(int distance)
        {
            return cost(energy.getAsInt(), energyDistance.getAsBoolean(), energyMultiplier.getAsDouble(), distance);
        }

        public int experienceCost(int distance)
        {
            return cost(experience.getAsInt(), experienceDistance.getAsBoolean(), experienceMultiplier.getAsDouble(), distance);
        }

        private int cost(int base, boolean useDistance, double multiplier, int distance)
        {
            if (base <= 0) return 0; // as in 1.20: no base cost means the upgrade doesn't use this fuel at all
            double selected = selectedAllowed.getAsBoolean() ? selectedMultiplier.getAsDouble() * (useDistance ? distance : 1) : 0;
            return (int) Math.round((base + selected) * multiplier);
        }
    }
}
