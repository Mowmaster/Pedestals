package com.mowmaster.pedestals.Configs;

import com.mowmaster.pedestals.pedestals;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

public class PedestalConfig
{
    public static class Common {

        public final ForgeConfigSpec.IntValue augment_t1CapacityItem;
        public final ForgeConfigSpec.IntValue augment_t1CapacityFluid;
        public final ForgeConfigSpec.IntValue augment_t1CapacityEnergy;
        public final ForgeConfigSpec.IntValue augment_t1CapacityXp;
        public final ForgeConfigSpec.IntValue augment_t1CapacityInsertSize;

        public final ForgeConfigSpec.IntValue augment_t2CapacityItem;
        public final ForgeConfigSpec.IntValue augment_t2CapacityFluid;
        public final ForgeConfigSpec.IntValue augment_t2CapacityEnergy;
        public final ForgeConfigSpec.IntValue augment_t2CapacityXp;
        public final ForgeConfigSpec.IntValue augment_t2CapacityInsertSize;

        public final ForgeConfigSpec.IntValue augment_t3CapacityItem;
        public final ForgeConfigSpec.IntValue augment_t3CapacityFluid;
        public final ForgeConfigSpec.IntValue augment_t3CapacityEnergy;
        public final ForgeConfigSpec.IntValue augment_t3CapacityXp;
        public final ForgeConfigSpec.IntValue augment_t3CapacityInsertSize;

        public final ForgeConfigSpec.IntValue augment_t4CapacityItem;
        public final ForgeConfigSpec.IntValue augment_t4CapacityFluid;
        public final ForgeConfigSpec.IntValue augment_t4CapacityEnergy;
        public final ForgeConfigSpec.IntValue augment_t4CapacityXp;
        public final ForgeConfigSpec.IntValue augment_t4CapacityInsertSize;



        public final ForgeConfigSpec.IntValue augment_t1StorageItem;
        public final ForgeConfigSpec.IntValue augment_t1StorageFluid;
        public final ForgeConfigSpec.IntValue augment_t1StorageEnergy;
        public final ForgeConfigSpec.IntValue augment_t1StorageXp;
        public final ForgeConfigSpec.IntValue augment_t1StorageInsertSize;

        public final ForgeConfigSpec.IntValue augment_t2StorageItem;
        public final ForgeConfigSpec.IntValue augment_t2StorageFluid;
        public final ForgeConfigSpec.IntValue augment_t2StorageEnergy;
        public final ForgeConfigSpec.IntValue augment_t2StorageXp;
        public final ForgeConfigSpec.IntValue augment_t2StorageInsertSize;

        public final ForgeConfigSpec.IntValue augment_t3StorageItem;
        public final ForgeConfigSpec.IntValue augment_t3StorageFluid;
        public final ForgeConfigSpec.IntValue augment_t3StorageEnergy;
        public final ForgeConfigSpec.IntValue augment_t3StorageXp;
        public final ForgeConfigSpec.IntValue augment_t3StorageInsertSize;

        public final ForgeConfigSpec.IntValue augment_t4StorageItem;
        public final ForgeConfigSpec.IntValue augment_t4StorageFluid;
        public final ForgeConfigSpec.IntValue augment_t4StorageEnergy;
        public final ForgeConfigSpec.IntValue augment_t4StorageXp;
        public final ForgeConfigSpec.IntValue augment_t4StorageInsertSize;


        public final ForgeConfigSpec.IntValue pedestal_maxTicksToTransfer;


        public final ForgeConfigSpec.IntValue augment_t1SpeedReduction;
        public final ForgeConfigSpec.IntValue augment_t1SpeedInsertable;
        public final ForgeConfigSpec.IntValue augment_t2SpeedReduction;
        public final ForgeConfigSpec.IntValue augment_t2SpeedInsertable;
        public final ForgeConfigSpec.IntValue augment_t3SpeedReduction;
        public final ForgeConfigSpec.IntValue augment_t3SpeedInsertable;
        public final ForgeConfigSpec.IntValue augment_t4SpeedReduction;
        public final ForgeConfigSpec.IntValue augment_t4SpeedInsertable;

        public final ForgeConfigSpec.IntValue augment_t1RangeIncrease;
        public final ForgeConfigSpec.IntValue augment_t1RangeInsertable;
        public final ForgeConfigSpec.IntValue augment_t2RangeIncrease;
        public final ForgeConfigSpec.IntValue augment_t2RangeInsertable;
        public final ForgeConfigSpec.IntValue augment_t3RangeIncrease;
        public final ForgeConfigSpec.IntValue augment_t3RangeInsertable;
        public final ForgeConfigSpec.IntValue augment_t4RangeIncrease;
        public final ForgeConfigSpec.IntValue augment_t4RangeInsertable;




        public final ForgeConfigSpec.IntValue cobbleGeneratorMultiplier;



        Common(ForgeConfigSpec.Builder builder) {

            builder.comment("Tiered Augments").push("Augment_Tiers");

            augment_t1CapacityItem = builder.comment("Tier 1, Capacity Item Transfer Increase").defineInRange("t1_increaseItemTransfer", 2, 0, Integer.MAX_VALUE);
            augment_t1CapacityFluid = builder.comment("Tier 1, Capacity Fluid Transfer Increase").defineInRange("t1_increaseFluidTransfer", 2000, 0, Integer.MAX_VALUE);
            augment_t1CapacityEnergy = builder.comment("Tier 1, Capacity Energy Transfer Increase").defineInRange("t1_increaseEnergyTransfer", 5000, 0, Integer.MAX_VALUE);
            augment_t1CapacityXp = builder.comment("Tier 1, Capacity Xp Transfer Increase (Levels)").defineInRange("t1_increaseXpTransfer", 1, 0, Integer.MAX_VALUE);
            augment_t1CapacityInsertSize = builder.comment("Tier 1, Max Allowed To Insert of this Tier").defineInRange("t1_increaseInsertableAmount", 4, 1, 64);

            augment_t2CapacityItem = builder.comment("Tier 2, Capacity Item Transfer Increase").defineInRange("t2_increaseItemTransfer", 4, 0, Integer.MAX_VALUE);
            augment_t2CapacityFluid = builder.comment("Tier 2, Capacity Fluid Transfer Increase").defineInRange("t2_increaseFluidTransfer", 4000, 0, Integer.MAX_VALUE);
            augment_t2CapacityEnergy = builder.comment("Tier 2, Capacity Energy Transfer Increase").defineInRange("t2_increaseEnergyTransfer", 10000, 0, Integer.MAX_VALUE);
            augment_t2CapacityXp = builder.comment("Tier 2, Capacity Xp Transfer Increase (Levels)").defineInRange("t2_increaseXpTransfer", 5, 0, Integer.MAX_VALUE);
            augment_t2CapacityInsertSize = builder.comment("Tier 2, Max Allowed To Insert of this Tier").defineInRange("t2_increaseInsertableAmount", 8, 1, 64);

            augment_t3CapacityItem = builder.comment("Tier 3, Capacity Item Transfer Increase").defineInRange("t3_increaseItemTransfer", 8, 0, Integer.MAX_VALUE);
            augment_t3CapacityFluid = builder.comment("Tier 3, Capacity Fluid Transfer Increase").defineInRange("t3_increaseFluidTransfer", 8000, 0, Integer.MAX_VALUE);
            augment_t3CapacityEnergy = builder.comment("Tier 3, Capacity Energy Transfer Increase").defineInRange("t3_increaseEnergyTransfer", 100000, 0, Integer.MAX_VALUE);
            augment_t3CapacityXp = builder.comment("Tier 3, Capacity Xp Transfer Increase (Levels)").defineInRange("t3_increaseXpTransfer", 10, 0, Integer.MAX_VALUE);
            augment_t3CapacityInsertSize = builder.comment("Tier 3, Max Allowed To Insert of this Tier").defineInRange("t3_increaseInsertableAmount", 12, 1, 64);

            augment_t4CapacityItem = builder.comment("Tier 4, Capacity Item Transfer Increase").defineInRange("t4_increaseItemTransfer", 16, 0, Integer.MAX_VALUE);
            augment_t4CapacityFluid = builder.comment("Tier 4, Capacity Fluid Transfer Increase").defineInRange("t4_increaseFluidTransfer", 16000, 0, Integer.MAX_VALUE);
            augment_t4CapacityEnergy = builder.comment("Tier 4, Capacity Energy Transfer Increase").defineInRange("t4_increaseEnergyTransfer", 1000000, 0, Integer.MAX_VALUE);
            augment_t4CapacityXp = builder.comment("Tier 4, Capacity Xp Transfer Increase (Levels)").defineInRange("t4_increaseXpTransfer", 15, 0, Integer.MAX_VALUE);
            augment_t4CapacityInsertSize = builder.comment("Tier 4, Max Allowed To Insert of this Tier").defineInRange("t4_increaseInsertableAmount", 16, 1, 64);



            augment_t1StorageItem = builder.comment("Tier 1, Storage Item Increase").defineInRange("t1_increaseItemStorage", 1, 0, Integer.MAX_VALUE);
            augment_t1StorageFluid = builder.comment("Tier 1, Storage Fluid Increase").defineInRange("t1_increaseFluidStorage", 4000, 0, Integer.MAX_VALUE);
            augment_t1StorageEnergy = builder.comment("Tier 1, Storage Energy Increase").defineInRange("t1_increaseEnergyStorage", 20000, 0, Integer.MAX_VALUE);
            augment_t1StorageXp = builder.comment("Tier 1, Storage Xp Increase (Levels)").defineInRange("t1_increaseXpStorage", 10, 0, Integer.MAX_VALUE);
            augment_t1StorageInsertSize = builder.comment("Tier 1, Max Allowed To Insert of this Tier").defineInRange("t1_increaseInsertableStorageAmount", 3, 1, 64);

            augment_t2StorageItem = builder.comment("Tier 2, Storage Item Increase").defineInRange("t2_increaseItemStorage", 1, 0, Integer.MAX_VALUE);
            augment_t2StorageFluid = builder.comment("Tier 2, Storage Fluid Increase").defineInRange("t2_increaseFluidStorage", 16000, 0, Integer.MAX_VALUE);
            augment_t2StorageEnergy = builder.comment("Tier 2, Storage Energy Increase").defineInRange("t2_increaseEnergyStorage", 100000, 0, Integer.MAX_VALUE);
            augment_t2StorageXp = builder.comment("Tier 2, Storage Xp Increase (Levels)").defineInRange("t2_increaseXpStorage", 15, 0, Integer.MAX_VALUE);
            augment_t2StorageInsertSize = builder.comment("Tier 2, Max Allowed To Insert of this Tier").defineInRange("t2_increaseInsertableStorageAmount", 7, 1, 64);

            augment_t3StorageItem = builder.comment("Tier 3, Storage Item Increase").defineInRange("t3_increaseItemStorage", 1, 0, Integer.MAX_VALUE);
            augment_t3StorageFluid = builder.comment("Tier 3, Storage Fluid Increase").defineInRange("t3_increaseFluidStorage", 64000, 0, Integer.MAX_VALUE);
            augment_t3StorageEnergy = builder.comment("Tier 3, Storage Energy Increase").defineInRange("t3_increaseEnergyStorage", 1000000, 0, Integer.MAX_VALUE);
            augment_t3StorageXp = builder.comment("Tier 3, Storage Xp Increase (Levels)").defineInRange("t3_increaseXpStorage", 20, 0, Integer.MAX_VALUE);
            augment_t3StorageInsertSize = builder.comment("Tier 3, Max Allowed To Insert of this Tier").defineInRange("t3_increaseInsertableStorageAmount", 11, 1, 64);

            augment_t4StorageItem = builder.comment("Tier 4, Storage Item Increase").defineInRange("t4_increaseItemStorage", 1, 0, Integer.MAX_VALUE);
            augment_t4StorageFluid = builder.comment("Tier 4, Storage Fluid Increase").defineInRange("t4_increaseFluidStorage", 265000, 0, Integer.MAX_VALUE);
            augment_t4StorageEnergy = builder.comment("Tier 4, Storage Energy Increase").defineInRange("t4_increaseEnergyStorage", 10000000, 0, Integer.MAX_VALUE);
            augment_t4StorageXp = builder.comment("Tier 4, Storage Xp Increase (Levels)").defineInRange("t4_increaseXpStorage", 25, 0, Integer.MAX_VALUE);
            augment_t4StorageInsertSize = builder.comment("Tier 4, Max Allowed To Insert of this Tier").defineInRange("t4_increaseInsertableStorageAmount", 15, 1, 64);

            //default speed is 4:40 ticks per send or 1:10 ticks(same as hopper rate)
            pedestal_maxTicksToTransfer = builder.comment("The max number of ticks needed to send items (before augments)").defineInRange("pedestal_ticksToTransfer", 40, 1, Integer.MAX_VALUE);

            //4:30 so 2 ticks reduced for 5 upgrades?
            augment_t1SpeedReduction = builder.comment("Tier 1, Number of Ticks Reduced").defineInRange("t1_ticksReduced", 2, 0, Integer.MAX_VALUE);
            augment_t1SpeedInsertable = builder.comment("Tier 1, Max Allowed To Insert of this Tier").defineInRange("t1_speedInsertable", 5, 1, 64);
            //4:20 so 4 ticks reduced for 5 upgrades?
            augment_t2SpeedReduction = builder.comment("Tier 2, Number of Ticks Reduced").defineInRange("t2_ticksReduced", 4, 0, Integer.MAX_VALUE);
            augment_t2SpeedInsertable = builder.comment("Tier 2, Max Allowed To Insert of this Tier").defineInRange("t2_speedInsertable", 5, 1, 64);
            //4:10 so 6 ticks reduced for 5 upgrades?
            augment_t3SpeedReduction = builder.comment("Tier 3, Number of Ticks Reduced").defineInRange("t3_ticksReduced", 6, 0, Integer.MAX_VALUE);
            augment_t3SpeedInsertable = builder.comment("Tier 3, Max Allowed To Insert of this Tier").defineInRange("t3_speedInsertable", 5, 1, 64);
            //4:1 so 8 ticks reduced for 5 upgrades? [Need to make a fail safe so we dont hit 0, so 1 need to be the min]
            augment_t4SpeedReduction = builder.comment("Tier 4, Number of Ticks Reduced").defineInRange("t4_ticksReduced", 8, 0, Integer.MAX_VALUE);
            augment_t4SpeedInsertable = builder.comment("Tier 4, Max Allowed To Insert of this Tier").defineInRange("t4_speedInsertable", 5, 1, 64);


            augment_t1RangeIncrease = builder.comment("Tier 1, Block Range Increase").defineInRange("t1_rangeIncrease", 2, 0, Integer.MAX_VALUE);
            augment_t1RangeInsertable = builder.comment("Tier 1, Max Allowed To Insert of this Tier").defineInRange("t1_rangeInsertable", 4, 1, 64);
            augment_t2RangeIncrease = builder.comment("Tier 2, Block Range Increase").defineInRange("t2_rangeIncrease", 6, 0, Integer.MAX_VALUE);
            augment_t2RangeInsertable = builder.comment("Tier 2, Max Allowed To Insert of this Tier").defineInRange("t2_rangeInsertable", 4, 1, 64);
            augment_t3RangeIncrease = builder.comment("Tier 3, Block Range Increase").defineInRange("t3_rangeIncrease", 8, 0, Integer.MAX_VALUE);
            augment_t3RangeInsertable = builder.comment("Tier 3, Max Allowed To Insert of this Tier").defineInRange("t3_rangeInsertable", 5, 1, 64);
            augment_t4RangeIncrease = builder.comment("Tier 4, Block Range Increase").defineInRange("t4_rangeIncrease", 8, 0, Integer.MAX_VALUE);
            augment_t4RangeInsertable = builder.comment("Tier 4, Max Allowed To Insert of this Tier").defineInRange("t4_rangeInsertable", 7, 1, 64);



            builder.pop();

            builder.comment("Upgrade Machine Configs").push("Upgrade_Machines");

            cobbleGeneratorMultiplier = builder
                    .comment("Cobble Gen Multiplier")
                    .defineInRange("genMultiplier", 16, 1, Integer.MAX_VALUE);
            builder.pop();
        }
    }

    public static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        pedestals.PLOGGER.debug("Loaded Pedestals config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
        pedestals.PLOGGER.debug("Pedestals config just got changed on the file system!");
    }
}
