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
        public final ForgeConfigSpec.IntValue augment_t1CapacityDust;
        public final ForgeConfigSpec.IntValue augment_t1CapacityInsertSize;

        public final ForgeConfigSpec.IntValue augment_t2CapacityItem;
        public final ForgeConfigSpec.IntValue augment_t2CapacityFluid;
        public final ForgeConfigSpec.IntValue augment_t2CapacityEnergy;
        public final ForgeConfigSpec.IntValue augment_t2CapacityXp;
        public final ForgeConfigSpec.IntValue augment_t2CapacityDust;
        public final ForgeConfigSpec.IntValue augment_t2CapacityInsertSize;

        public final ForgeConfigSpec.IntValue augment_t3CapacityItem;
        public final ForgeConfigSpec.IntValue augment_t3CapacityFluid;
        public final ForgeConfigSpec.IntValue augment_t3CapacityEnergy;
        public final ForgeConfigSpec.IntValue augment_t3CapacityXp;
        public final ForgeConfigSpec.IntValue augment_t3CapacityDust;
        public final ForgeConfigSpec.IntValue augment_t3CapacityInsertSize;

        public final ForgeConfigSpec.IntValue augment_t4CapacityItem;
        public final ForgeConfigSpec.IntValue augment_t4CapacityFluid;
        public final ForgeConfigSpec.IntValue augment_t4CapacityEnergy;
        public final ForgeConfigSpec.IntValue augment_t4CapacityXp;
        public final ForgeConfigSpec.IntValue augment_t4CapacityDust;
        public final ForgeConfigSpec.IntValue augment_t4CapacityInsertSize;



        public final ForgeConfigSpec.IntValue augment_t1StorageItem;
        public final ForgeConfigSpec.IntValue augment_t1StorageFluid;
        public final ForgeConfigSpec.IntValue augment_t1StorageEnergy;
        public final ForgeConfigSpec.IntValue augment_t1StorageXp;
        public final ForgeConfigSpec.IntValue augment_t1StorageDust;
        public final ForgeConfigSpec.IntValue augment_t1StorageInsertSize;

        public final ForgeConfigSpec.IntValue augment_t2StorageItem;
        public final ForgeConfigSpec.IntValue augment_t2StorageFluid;
        public final ForgeConfigSpec.IntValue augment_t2StorageEnergy;
        public final ForgeConfigSpec.IntValue augment_t2StorageXp;
        public final ForgeConfigSpec.IntValue augment_t2StorageDust;
        public final ForgeConfigSpec.IntValue augment_t2StorageInsertSize;

        public final ForgeConfigSpec.IntValue augment_t3StorageItem;
        public final ForgeConfigSpec.IntValue augment_t3StorageFluid;
        public final ForgeConfigSpec.IntValue augment_t3StorageEnergy;
        public final ForgeConfigSpec.IntValue augment_t3StorageXp;
        public final ForgeConfigSpec.IntValue augment_t3StorageDust;
        public final ForgeConfigSpec.IntValue augment_t3StorageInsertSize;

        public final ForgeConfigSpec.IntValue augment_t4StorageItem;
        public final ForgeConfigSpec.IntValue augment_t4StorageFluid;
        public final ForgeConfigSpec.IntValue augment_t4StorageEnergy;
        public final ForgeConfigSpec.IntValue augment_t4StorageXp;
        public final ForgeConfigSpec.IntValue augment_t4StorageDust;
        public final ForgeConfigSpec.IntValue augment_t4StorageInsertSize;



        public final ForgeConfigSpec.IntValue pedestal_maxTicksToTransfer;
        public final ForgeConfigSpec.IntValue pedestal_baseItemStacks;
        public final ForgeConfigSpec.IntValue pedestal_baseFluidStorage;
        public final ForgeConfigSpec.IntValue pedestal_baseEnergyStorage;
        public final ForgeConfigSpec.IntValue pedestal_baseXpStorage;
        public final ForgeConfigSpec.IntValue pedestal_baseDustStorage;
        public final ForgeConfigSpec.IntValue pedestal_baseItemTransferRate;
        public final ForgeConfigSpec.IntValue pedestal_baseFluidTransferRate;
        public final ForgeConfigSpec.IntValue pedestal_baseEnergyTransferRate;
        public final ForgeConfigSpec.IntValue pedestal_baseXpTransferRate;
        public final ForgeConfigSpec.IntValue pedestal_baseDustTransferRate;






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


        public final ForgeConfigSpec.BooleanValue cobbleGeneratorDamageTools;

        public final ForgeConfigSpec.BooleanValue blockBreakerBreakEntities;
        public final ForgeConfigSpec.BooleanValue blockBreakerDamageTools;
        public final ForgeConfigSpec.IntValue upgrade_blockbreaker_baseEnergyCost;
        public final ForgeConfigSpec.BooleanValue upgrade_blockbreaker_energy_distance_multiplier;
        public final ForgeConfigSpec.DoubleValue upgrade_blockbreaker_energyMultiplier;
        public final ForgeConfigSpec.IntValue upgrade_blockbreaker_baseXpCost;
        public final ForgeConfigSpec.BooleanValue upgrade_blockbreaker_xp_distance_multiplier;
        public final ForgeConfigSpec.DoubleValue upgrade_blockbreaker_xpMultiplier;
        public final ForgeConfigSpec.IntValue upgrade_blockbreaker_dustColor;
        public final ForgeConfigSpec.IntValue upgrade_blockbreaker_baseDustAmount;
        public final ForgeConfigSpec.BooleanValue upgrade_blockbreaker_dust_distance_multiplier;
        public final ForgeConfigSpec.DoubleValue upgrade_blockbreaker_dustMultiplier;
        public final ForgeConfigSpec.BooleanValue upgrade_blockbreaker_selectedAllowed;
        public final ForgeConfigSpec.DoubleValue upgrade_blockbreaker_selectedMultiplier;

        public final ForgeConfigSpec.BooleanValue quarryDamageTools;

        public final ForgeConfigSpec.IntValue upgrade_quarry_baseEnergyCost;
        public final ForgeConfigSpec.DoubleValue upgrade_quarry_energyMultiplier;
        public final ForgeConfigSpec.BooleanValue upgrade_quarry_energy_distance_multiplier;
        public final ForgeConfigSpec.IntValue upgrade_quarry_baseXpCost;
        public final ForgeConfigSpec.BooleanValue upgrade_quarry_xp_distance_multiplier;
        public final ForgeConfigSpec.DoubleValue upgrade_quarry_xpMultiplier;
        public final ForgeConfigSpec.IntValue upgrade_quarry_dustColor;
        public final ForgeConfigSpec.IntValue upgrade_quarry_baseDustAmount;
        public final ForgeConfigSpec.BooleanValue upgrade_quarry_dust_distance_multiplier;
        public final ForgeConfigSpec.DoubleValue upgrade_quarry_dustMultiplier;
        public final ForgeConfigSpec.BooleanValue upgrade_quarry_selectedAllowed;
        public final ForgeConfigSpec.DoubleValue upgrade_quarry_selectedMultiplier;
        public final ForgeConfigSpec.IntValue upgrade_quarry_baseBlocksMined;

        public final ForgeConfigSpec.BooleanValue chopperDamageTools;
        public final ForgeConfigSpec.IntValue upgrade_chopper_baseEnergyCost;
        public final ForgeConfigSpec.BooleanValue upgrade_chopper_energy_distance_multiplier;
        public final ForgeConfigSpec.DoubleValue upgrade_chopper_energyMultiplier;
        public final ForgeConfigSpec.IntValue upgrade_chopper_baseXpCost;
        public final ForgeConfigSpec.BooleanValue upgrade_chopper_xp_distance_multiplier;
        public final ForgeConfigSpec.DoubleValue upgrade_chopper_xpMultiplier;
        public final ForgeConfigSpec.IntValue upgrade_chopper_dustColor;
        public final ForgeConfigSpec.IntValue upgrade_chopper_baseDustAmount;
        public final ForgeConfigSpec.BooleanValue upgrade_chopper_dust_distance_multiplier;
        public final ForgeConfigSpec.DoubleValue upgrade_chopper_dustMultiplier;
        public final ForgeConfigSpec.BooleanValue upgrade_chopper_selectedAllowed;
        public final ForgeConfigSpec.DoubleValue upgrade_chopper_selectedMultiplier;

        public final ForgeConfigSpec.IntValue upgrade_magnet_baseEnergyCost;
        public final ForgeConfigSpec.DoubleValue upgrade_magnet_energyMultiplier;
        public final ForgeConfigSpec.IntValue upgrade_magnet_baseXpCost;
        public final ForgeConfigSpec.DoubleValue upgrade_magnet_xpMultiplier;
        public final ForgeConfigSpec.IntValue upgrade_magnet_dustColor;
        public final ForgeConfigSpec.IntValue upgrade_magnet_baseDustAmount;
        public final ForgeConfigSpec.DoubleValue upgrade_magnet_dustMultiplier;
        public final ForgeConfigSpec.BooleanValue upgrade_magnet_selectedAllowed;
        public final ForgeConfigSpec.DoubleValue upgrade_magnet_selectedMultiplier;

        public final ForgeConfigSpec.BooleanValue upgrade_require_sized_selectable_area;

        public final ForgeConfigSpec.IntValue bulkstorage_fluidDischarge;
        public final ForgeConfigSpec.BooleanValue bulkstorage_fluidDischarge_toggle;
        public final ForgeConfigSpec.IntValue bulkstorage_energyDischarge;
        public final ForgeConfigSpec.BooleanValue bulkstorage_energyDischarge_toggle;
        public final ForgeConfigSpec.IntValue bulkstorage_xpDischarge;
        public final ForgeConfigSpec.BooleanValue bulkstorage_xpDischarge_toggle;
        public final ForgeConfigSpec.IntValue bulkstorage_dustDischarge;
        public final ForgeConfigSpec.BooleanValue bulkstorage_dustDischarge_toggle;

        Common(ForgeConfigSpec.Builder builder) {

            builder.comment("Tiered Augments").push("Augment_Tiers");

            augment_t1CapacityItem = builder.comment("Tier 1, Capacity Item Transfer Increase").defineInRange("t1_increaseItemTransfer", 2, 0, Integer.MAX_VALUE);
            augment_t1CapacityFluid = builder.comment("Tier 1, Capacity Fluid Transfer Increase").defineInRange("t1_increaseFluidTransfer", 2000, 0, Integer.MAX_VALUE);
            augment_t1CapacityEnergy = builder.comment("Tier 1, Capacity Energy Transfer Increase").defineInRange("t1_increaseEnergyTransfer", 5000, 0, Integer.MAX_VALUE);
            augment_t1CapacityXp = builder.comment("Tier 1, Capacity Xp Transfer Increase (Levels)").defineInRange("t1_increaseXpTransfer", 1, 0, Integer.MAX_VALUE);
            augment_t1CapacityDust = builder.comment("Tier 1, Capacity Dust Transfer Increase").defineInRange("t1_increaseDustTransfer", 10, 0, Integer.MAX_VALUE);
            augment_t1CapacityInsertSize = builder.comment("Tier 1, Max Allowed To Insert of this Tier").defineInRange("t1_increaseInsertableAmount", 4, 1, 64);

            augment_t2CapacityItem = builder.comment("Tier 2, Capacity Item Transfer Increase").defineInRange("t2_increaseItemTransfer", 4, 0, Integer.MAX_VALUE);
            augment_t2CapacityFluid = builder.comment("Tier 2, Capacity Fluid Transfer Increase").defineInRange("t2_increaseFluidTransfer", 4000, 0, Integer.MAX_VALUE);
            augment_t2CapacityEnergy = builder.comment("Tier 2, Capacity Energy Transfer Increase").defineInRange("t2_increaseEnergyTransfer", 10000, 0, Integer.MAX_VALUE);
            augment_t2CapacityXp = builder.comment("Tier 2, Capacity Xp Transfer Increase (Levels)").defineInRange("t2_increaseXpTransfer", 5, 0, Integer.MAX_VALUE);
            augment_t2CapacityDust = builder.comment("Tier 2, Capacity Dust Transfer Increase (Levels)").defineInRange("t2_increaseDustTransfer", 10, 0, Integer.MAX_VALUE);
            augment_t2CapacityInsertSize = builder.comment("Tier 2, Max Allowed To Insert of this Tier").defineInRange("t2_increaseInsertableAmount", 8, 1, 64);

            augment_t3CapacityItem = builder.comment("Tier 3, Capacity Item Transfer Increase").defineInRange("t3_increaseItemTransfer", 8, 0, Integer.MAX_VALUE);
            augment_t3CapacityFluid = builder.comment("Tier 3, Capacity Fluid Transfer Increase").defineInRange("t3_increaseFluidTransfer", 8000, 0, Integer.MAX_VALUE);
            augment_t3CapacityEnergy = builder.comment("Tier 3, Capacity Energy Transfer Increase").defineInRange("t3_increaseEnergyTransfer", 100000, 0, Integer.MAX_VALUE);
            augment_t3CapacityXp = builder.comment("Tier 3, Capacity Xp Transfer Increase (Levels)").defineInRange("t3_increaseXpTransfer", 10, 0, Integer.MAX_VALUE);
            augment_t3CapacityDust = builder.comment("Tier 3, Capacity Dust Transfer Increase").defineInRange("t3_increaseDustTransfer", 10, 0, Integer.MAX_VALUE);
            augment_t3CapacityInsertSize = builder.comment("Tier 3, Max Allowed To Insert of this Tier").defineInRange("t3_increaseInsertableAmount", 12, 1, 64);

            augment_t4CapacityItem = builder.comment("Tier 4, Capacity Item Transfer Increase").defineInRange("t4_increaseItemTransfer", 16, 0, Integer.MAX_VALUE);
            augment_t4CapacityFluid = builder.comment("Tier 4, Capacity Fluid Transfer Increase").defineInRange("t4_increaseFluidTransfer", 16000, 0, Integer.MAX_VALUE);
            augment_t4CapacityEnergy = builder.comment("Tier 4, Capacity Energy Transfer Increase").defineInRange("t4_increaseEnergyTransfer", 1000000, 0, Integer.MAX_VALUE);
            augment_t4CapacityXp = builder.comment("Tier 4, Capacity Xp Transfer Increase (Levels)").defineInRange("t4_increaseXpTransfer", 15, 0, Integer.MAX_VALUE);
            augment_t4CapacityDust = builder.comment("Tier 4, Capacity Dust Transfer Increase").defineInRange("t4_increaseDustTransfer", 10, 0, Integer.MAX_VALUE);
            augment_t4CapacityInsertSize = builder.comment("Tier 4, Max Allowed To Insert of this Tier").defineInRange("t4_increaseInsertableAmount", 16, 1, 64);



            augment_t1StorageItem = builder.comment("Tier 1, Storage Item Increase").defineInRange("t1_increaseItemStorage", 1, 0, Integer.MAX_VALUE);
            augment_t1StorageFluid = builder.comment("Tier 1, Storage Fluid Increase").defineInRange("t1_increaseFluidStorage", 4000, 0, Integer.MAX_VALUE);
            augment_t1StorageEnergy = builder.comment("Tier 1, Storage Energy Increase").defineInRange("t1_increaseEnergyStorage", 20000, 0, Integer.MAX_VALUE);
            augment_t1StorageXp = builder.comment("Tier 1, Storage Xp Increase (Levels)").defineInRange("t1_increaseXpStorage", 10, 0, Integer.MAX_VALUE);
            augment_t1StorageDust = builder.comment("Tier 1, Storage Dust Increase").defineInRange("t1_increaseDustStorage", 50, 0, Integer.MAX_VALUE);
            augment_t1StorageInsertSize = builder.comment("Tier 1, Max Allowed To Insert of this Tier").defineInRange("t1_increaseInsertableStorageAmount", 3, 1, 64);

            augment_t2StorageItem = builder.comment("Tier 2, Storage Item Increase").defineInRange("t2_increaseItemStorage", 1, 0, Integer.MAX_VALUE);
            augment_t2StorageFluid = builder.comment("Tier 2, Storage Fluid Increase").defineInRange("t2_increaseFluidStorage", 16000, 0, Integer.MAX_VALUE);
            augment_t2StorageEnergy = builder.comment("Tier 2, Storage Energy Increase").defineInRange("t2_increaseEnergyStorage", 100000, 0, Integer.MAX_VALUE);
            augment_t2StorageXp = builder.comment("Tier 2, Storage Xp Increase (Levels)").defineInRange("t2_increaseXpStorage", 15, 0, Integer.MAX_VALUE);
            augment_t2StorageDust = builder.comment("Tier 2, Storage Dust Increase").defineInRange("t2_increaseDustStorage", 100, 0, Integer.MAX_VALUE);
            augment_t2StorageInsertSize = builder.comment("Tier 2, Max Allowed To Insert of this Tier").defineInRange("t2_increaseInsertableStorageAmount", 7, 1, 64);

            augment_t3StorageItem = builder.comment("Tier 3, Storage Item Increase").defineInRange("t3_increaseItemStorage", 1, 0, Integer.MAX_VALUE);
            augment_t3StorageFluid = builder.comment("Tier 3, Storage Fluid Increase").defineInRange("t3_increaseFluidStorage", 64000, 0, Integer.MAX_VALUE);
            augment_t3StorageEnergy = builder.comment("Tier 3, Storage Energy Increase").defineInRange("t3_increaseEnergyStorage", 1000000, 0, Integer.MAX_VALUE);
            augment_t3StorageXp = builder.comment("Tier 3, Storage Xp Increase (Levels)").defineInRange("t3_increaseXpStorage", 20, 0, Integer.MAX_VALUE);
            augment_t3StorageDust = builder.comment("Tier 3, Storage Dust Increase").defineInRange("t3_increaseDustStorage", 200, 0, Integer.MAX_VALUE);
            augment_t3StorageInsertSize = builder.comment("Tier 3, Max Allowed To Insert of this Tier").defineInRange("t3_increaseInsertableStorageAmount", 11, 1, 64);

            augment_t4StorageItem = builder.comment("Tier 4, Storage Item Increase").defineInRange("t4_increaseItemStorage", 1, 0, Integer.MAX_VALUE);
            augment_t4StorageFluid = builder.comment("Tier 4, Storage Fluid Increase").defineInRange("t4_increaseFluidStorage", 265000, 0, Integer.MAX_VALUE);
            augment_t4StorageEnergy = builder.comment("Tier 4, Storage Energy Increase").defineInRange("t4_increaseEnergyStorage", 10000000, 0, Integer.MAX_VALUE);
            augment_t4StorageXp = builder.comment("Tier 4, Storage Xp Increase (Levels)").defineInRange("t4_increaseXpStorage", 25, 0, Integer.MAX_VALUE);
            augment_t4StorageDust = builder.comment("Tier 4, Storage Dust Increase").defineInRange("t4_increaseDustStorage", 500, 0, Integer.MAX_VALUE);
            augment_t4StorageInsertSize = builder.comment("Tier 4, Max Allowed To Insert of this Tier").defineInRange("t4_increaseInsertableStorageAmount", 15, 1, 64);

            //default speed is 4:40 ticks per send or 1:10 ticks(same as hopper rate)
            pedestal_maxTicksToTransfer = builder.comment("The max number of ticks needed to send items (before augments)").defineInRange("pedestal_ticksToTransfer", 40, 1, Integer.MAX_VALUE);
            pedestal_baseItemStacks = builder.comment("The initial number of stacks a pedestal holds (max values are definitive max values including with upgrades)").defineInRange("pedestal_baseItemStacks", 1, 1, 27);
            pedestal_baseFluidStorage = builder.comment("The initial fluid storage for pedestals (max values are definitive max values including with upgrades)").defineInRange("pedestal_baseFluidStorage", 16000, 1, Integer.MAX_VALUE);
            pedestal_baseEnergyStorage = builder.comment("The initial energy storage for pedestals (max values are definitive max values including with upgrades)").defineInRange("pedestal_baseEnergyStorage", 20000, 1, Integer.MAX_VALUE);
            pedestal_baseXpStorage = builder.comment("The initial xp storage for pedestals [In levels] (max values are definitive max values including with upgrades)").defineInRange("pedestal_baseXpStorage", 30, 1, 21000);
            pedestal_baseDustStorage = builder.comment("The initial dust storage for pedestals (max values are definitive max values including with upgrades)").defineInRange("pedestal_baseDustStorage", 200, 1,  Integer.MAX_VALUE);

            pedestal_baseItemTransferRate = builder.comment("Base Item Transfer Rate").defineInRange("pedestal_baseItemTransferRate", 4, 1, Integer.MAX_VALUE);
            pedestal_baseFluidTransferRate = builder.comment("Base Fluid Transfer Rate").defineInRange("pedestal_baseFluidTransferRate", 1000, 1, Integer.MAX_VALUE);
            pedestal_baseEnergyTransferRate = builder.comment("Base Energy Transfer Rate").defineInRange("pedestal_baseEnergyTransferRate", 5000, 1, Integer.MAX_VALUE);
            pedestal_baseXpTransferRate = builder.comment("Base Xp Transfer Rate [In Levels]").defineInRange("pedestal_baseXpTransferRate", 1, 1, Integer.MAX_VALUE);
            pedestal_baseDustTransferRate = builder.comment("Base Dust Transfer Rate").defineInRange("pedestal_baseDustTransferRate", 10, 1, Integer.MAX_VALUE);



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

            cobbleGeneratorDamageTools = builder
                    .comment("Cobble Gen Damages Inserted Tools")
                    .define("cobbleGenToolDamage",false);
            blockBreakerBreakEntities = builder
                    .comment("Block Breaker Breaks Block Entities")
                    .define("blockBreakerBreakEntities",true);
            blockBreakerDamageTools = builder
                    .comment("Block Breaker Damages Inserted Tools")
                    .define("blockBreakerDamageTools",false);
            quarryDamageTools = builder
                    .comment("Quarry Damages Inserted Tools")
                    .define("quarryDamageTools",false);
            chopperDamageTools = builder
                    .comment("Chopper Damages Inserted Tools")
                    .define("chopperDamageTools",false);
            builder.pop();


            builder.comment("Upgrade Toggle-ables").push("Upgrade_Toggles");

            upgrade_require_sized_selectable_area = builder
                    .comment("Restrict the Selectable Area Size based on the Upgrades AOE modifier")
                    .define("upgrade_toggleable_require_selectable_size",  false);
            builder.pop();




            builder.comment("Upgrade 'Energy' Costs").push("Upgrade_Costs");

            upgrade_magnet_baseEnergyCost = builder
                    .comment("Base RF cost per upgrade operation [Magnet]")
                    .defineInRange("upgrade_magnet_base_energy_cost", 0, 0, Integer.MAX_VALUE);
            upgrade_magnet_energyMultiplier = builder
                    .comment("Energy Multiplier, total cost x multiplier [Magnet]")
                    .defineInRange("upgrade_magnet_base_energy_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_magnet_baseXpCost = builder
                    .comment("Base XP cost per upgrade operation [Magnet]")
                    .defineInRange("upgrade_magnet_base_xp_cost", 0, 0, Integer.MAX_VALUE);
            upgrade_magnet_xpMultiplier = builder
                    .comment("XP Multiplier, total cost x multiplier [Magnet]")
                    .defineInRange("upgrade_magnet_base_xp_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_magnet_dustColor = builder
                    .comment("Dust Color Required to do action [Magnet]")
                    .defineInRange("upgrade_magnet_base_dust_color", -1, -1, Integer.MAX_VALUE);
            upgrade_magnet_baseDustAmount = builder
                    .comment("Base Dust amount needed per upgrade operation [Magnet]")
                    .defineInRange("upgrade_magnet_base_dust_amount", 0, 0, Integer.MAX_VALUE);
            upgrade_magnet_dustMultiplier = builder
                    .comment("Dust Amount Multiplier, total cost x multiplier [Magnet]")
                    .defineInRange("upgrade_magnet_base_dust_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_magnet_selectedAllowed = builder
                    .comment("Additional Modifier based on distance of work(breaking a block or picking up items as an example) from pedestal [Magnet]")
                    .define("upgrade_magnet_selected_allowed",  false);
            upgrade_magnet_selectedMultiplier = builder
                    .comment("Modifier Amount, Distance x Modifier + Other 'Energy' BaseCost (this is the 'total cost' formula) [Magnet]")
                    .defineInRange("upgrade_magnet_selected_modifier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);



            upgrade_blockbreaker_baseEnergyCost = builder
                    .comment("Base RF cost per upgrade operation [Block Breaker]")
                    .defineInRange("upgrade_blockbreaker_base_energy_cost", 0, 0, Integer.MAX_VALUE);
            upgrade_blockbreaker_energy_distance_multiplier = builder
                    .comment("Distance of Block Broken Used as a modifier (Requires selected_allowed = True) [Chopper]")
                    .define("upgrade_blockbreaker_energy_distance_multiplier",  true);
            upgrade_blockbreaker_energyMultiplier = builder
                    .comment("Energy Multiplier, total cost x multiplier [Block Breaker]")
                    .defineInRange("upgrade_blockbreaker_energy_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_blockbreaker_baseXpCost = builder
                    .comment("Base XP cost per upgrade operation [Block Breaker]")
                    .defineInRange("upgrade_blockbreaker_base_xp_cost", 0, 0, Integer.MAX_VALUE);
            upgrade_blockbreaker_xp_distance_multiplier = builder
                    .comment("Distance of Block Broken Used as a modifier (Requires selected_allowed = True) [Chopper]")
                    .define("upgrade_blockbreaker_xp_distance_multiplier",  true);
            upgrade_blockbreaker_xpMultiplier = builder
                    .comment("XP Multiplier, total cost x multiplier [Block Breaker]")
                    .defineInRange("upgrade_blockbreaker_xp_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_blockbreaker_dustColor = builder
                    .comment("Dust Color Required to do action [Block Breaker]")
                    .defineInRange("upgrade_blockbreaker_dust_color", -1, -1, Integer.MAX_VALUE);
            upgrade_blockbreaker_baseDustAmount = builder
                    .comment("Base Dust amount needed per upgrade operation [Block Breaker]")
                    .defineInRange("upgrade_blockbreaker_base_dust_amount", 0, 0, Integer.MAX_VALUE);
            upgrade_blockbreaker_dust_distance_multiplier = builder
                    .comment("Distance of Block Broken Used as a modifier (Requires selected_allowed = True) [Chopper]")
                    .define("upgrade_blockbreaker_dust_distance_multiplier",  true);
            upgrade_blockbreaker_dustMultiplier = builder
                    .comment("Dust Amount Multiplier, total cost x multiplier [Block Breaker]")
                    .defineInRange("upgrade_blockbreaker_dust_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_blockbreaker_selectedAllowed = builder
                    .comment("Additional Modifier based on distance of work(breaking a block or picking up items as an example) from pedestal [Block Breaker]")
                    .define("upgrade_blockbreaker_selected_allowed",  false);
            upgrade_blockbreaker_selectedMultiplier = builder
                    .comment("Modifier Amount, Distance x Modifier + Other 'Energy' BaseCost (this is the 'total cost' formula) [Block Breaker]")
                    .defineInRange("upgrade_blockbreaker_selected_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);



            upgrade_quarry_baseEnergyCost = builder
                    .comment("Base RF cost per upgrade operation [Quarry]")
                    .defineInRange("upgrade_quarry_base_energy_cost", 0, 0, Integer.MAX_VALUE);
            upgrade_quarry_energy_distance_multiplier = builder
                    .comment("Distance of Block Broken Used as a modifier (Requires selected_allowed = True) [Chopper]")
                    .define("upgrade_quarry_energy_distance_multiplier",  true);
            upgrade_quarry_energyMultiplier = builder
                    .comment("Energy Multiplier, total cost x multiplier [Quarry]")
                    .defineInRange("upgrade_quarry_energy_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_quarry_baseXpCost = builder
                    .comment("Base XP cost per upgrade operation [Quarry]")
                    .defineInRange("upgrade_quarry_base_xp_cost", 0, 0, Integer.MAX_VALUE);
            upgrade_quarry_xp_distance_multiplier = builder
                    .comment("Distance of Block Broken Used as a modifier (Requires selected_allowed = True) [Chopper]")
                    .define("upgrade_quarry_xp_distance_multiplier",  true);
            upgrade_quarry_xpMultiplier = builder
                    .comment("XP Multiplier, total cost x multiplier [Quarry]")
                    .defineInRange("upgrade_quarry_xp_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_quarry_dustColor = builder
                    .comment("Dust Color Required to do action [Quarry]")
                    .defineInRange("upgrade_quarry_dust_color", -1, -1, Integer.MAX_VALUE);
            upgrade_quarry_baseDustAmount = builder
                    .comment("Base Dust amount needed per upgrade operation [Quarry]")
                    .defineInRange("upgrade_quarry_based_dust_amount", 0, 0, Integer.MAX_VALUE);
            upgrade_quarry_dust_distance_multiplier = builder
                    .comment("Distance of Block Broken Used as a modifier (Requires selected_allowed = True) [Chopper]")
                    .define("upgrade_quarry_dust_distance_multiplier",  true);
            upgrade_quarry_dustMultiplier = builder
                    .comment("Dust Amount Multiplier, total cost x multiplier [Quarry]")
                    .defineInRange("upgrade_quarry_dust_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_quarry_selectedAllowed = builder
                    .comment("Additional Modifier based on distance of work(breaking a block or picking up items as an example) from pedestal [Quarry]")
                    .define("upgrade_quarry_selected_allowed",  false);
            upgrade_quarry_selectedMultiplier = builder
                    .comment("Modifier Amount, Distance x Modifier + Other 'Energy' BaseCost (this is the 'total cost' formula) [Quarry]")
                    .defineInRange("upgrade_quarry_selected_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_quarry_baseBlocksMined = builder
                    .comment("Base Amount of Blocks the Quarry will Mine at a Time. [Quarry]")
                    .defineInRange("upgrade_quarry_base_blocks_mined", 4, 0, Integer.MAX_VALUE);



            upgrade_chopper_baseEnergyCost = builder
                    .comment("Base RF cost per upgrade operation [Chopper]")
                    .defineInRange("upgrade_chopper_base_energy_cost", 0, 0, Integer.MAX_VALUE);
            upgrade_chopper_energy_distance_multiplier = builder
                    .comment("Distance of Block Broken Used as a modifier (Requires selected_allowed = True) [Chopper]")
                    .define("upgrade_chopper_energy_distance_multiplier",  true);
            upgrade_chopper_energyMultiplier = builder
                    .comment("Energy Multiplier, total cost x multiplier [Chopper]")
                    .defineInRange("upgrade_chopper_energy_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_chopper_baseXpCost = builder
                    .comment("Base XP cost per upgrade operation [Chopper]")
                    .defineInRange("upgrade_chopper_base_xp_cost", 1, 0, Integer.MAX_VALUE);
            upgrade_chopper_xp_distance_multiplier = builder
                    .comment("Distance of Block Broken Used as a modifier (Requires selected_allowed = True) [Chopper]")
                    .define("upgrade_chopper_xp_distance_multiplier",  true);
            upgrade_chopper_xpMultiplier = builder
                    .comment("XP Multiplier, total cost x multiplier [Chopper]")
                    .defineInRange("upgrade_chopper_xp_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_chopper_dustColor = builder
                    .comment("Dust Color Required to do action [Chopper]")
                    .defineInRange("upgrade_chopper_dust_color", -1, -1, Integer.MAX_VALUE);
            upgrade_chopper_baseDustAmount = builder
                    .comment("Base Dust amount needed per upgrade operation [Chopper]")
                    .defineInRange("upgrade_chopper_base_dust_amount", 0, 0, Integer.MAX_VALUE);
            upgrade_chopper_dust_distance_multiplier = builder
                    .comment("Distance of Block Broken Used as a modifier (Requires selected_allowed = True) [Chopper]")
                    .define("upgrade_chopper_dust_distance_multiplier",  true);
            upgrade_chopper_dustMultiplier = builder
                    .comment("Dust Amount Multiplier, total cost x multiplier [Chopper]")
                    .defineInRange("upgrade_chopper_dust_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);
            upgrade_chopper_selectedAllowed = builder
                    .comment("Additional Modifier based on distance of work(breaking a block or picking up items as an example) from pedestal [Chopper]")
                    .define("upgrade_chopper_selected_allowed",  false);
            upgrade_chopper_selectedMultiplier = builder
                    .comment("Modifier Amount, Distance x Modifier + Other 'Energy' BaseCost (this is the 'total cost' formula) [Chopper]")
                    .defineInRange("upgrade_chopper_selected_multiplier", 1.0D, 0.0D, (double)Integer.MAX_VALUE);

            builder.pop();


            builder.comment("Bulk Storage Options").push("Bulk_Storage");

            bulkstorage_fluidDischarge = builder
                    .comment("Time (In Seconds) for energy to Discharge from Bulk Storage (Fluid)")
                    .defineInRange("bulkstorage_fluid_discharge_time", 5, 1, Integer.MAX_VALUE);

            bulkstorage_fluidDischarge_toggle = builder
                    .comment("Weather bulk storage discharge is allowed to trigger or not (Fluid)")
                    .define("bulkstorage_fluid_discharge_toggle",  true);

            bulkstorage_energyDischarge = builder
                    .comment("Time (In Seconds) for energy to Discharge from Bulk Storage (Energy)")
                    .defineInRange("bulkstorage_energy_discharge_time", 5, 1, Integer.MAX_VALUE);

            bulkstorage_energyDischarge_toggle = builder
                    .comment("Weather bulk storage discharge is allowed to trigger or not (Energy)")
                    .define("bulkstorage_energy_discharge_toggle",  true);

            bulkstorage_xpDischarge = builder
                    .comment("Time (In Seconds) for energy to Discharge from Bulk Storage (XP)")
                    .defineInRange("bulkstorage_xp_discharge_time", 5, 1, Integer.MAX_VALUE);

            bulkstorage_xpDischarge_toggle = builder
                    .comment("Weather bulk storage discharge is allowed to trigger or not (XP)")
                    .define("bulkstorage_xp_discharge_toggle",  true);

            bulkstorage_dustDischarge = builder
                    .comment("Time (In Seconds) for energy to Discharge from Bulk Storage (Dust)")
                    .defineInRange("bulkstorage_dust_discharge_time", 5, 1, Integer.MAX_VALUE);

            bulkstorage_dustDischarge_toggle = builder
                    .comment("Weather bulk storage discharge is allowed to trigger or not (Dust)")
                    .define("bulkstorage_dust_discharge_toggle",  true);

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
