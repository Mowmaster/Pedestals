package com.mowmaster.pedestals;

import net.neoforged.neoforge.common.ModConfigSpec;

// Pedestal settings, same names and defaults as 1.20's PedestalConfig (the template's example values are gone).
// Augment values come back with the augments.
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.push("pedestal");
    }

    public static final ModConfigSpec.IntValue TICKS_TO_TRANSFER = BUILDER
            .comment("Ticks between transfers to linked pedestals (before augments)")
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
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
