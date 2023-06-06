package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;
import static com.mowmaster.pedestals.PedestalUtils.References.isDustLoaded;

import net.minecraft.world.item.Item.Properties;

public class AugmentTieredCapacity extends AugmentBase{

    public AugmentTieredCapacity(Properties p_41383_) {
        super(p_41383_);
    }

    public static int getAdditionalItemTransferRatePerItem(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get())) return PedestalConfig.COMMON.augment_t1CapacityItem.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get())) return PedestalConfig.COMMON.augment_t2CapacityItem.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get())) return PedestalConfig.COMMON.augment_t3CapacityItem.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get())) return PedestalConfig.COMMON.augment_t4CapacityItem.get();
        return 0;
    }
    public static int getAdditionalFluidTransferRatePerItem(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get())) return PedestalConfig.COMMON.augment_t1CapacityFluid.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get())) return PedestalConfig.COMMON.augment_t2CapacityFluid.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get())) return PedestalConfig.COMMON.augment_t3CapacityFluid.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get())) return PedestalConfig.COMMON.augment_t4CapacityFluid.get();
        return 0;
    }
    public static int getAdditionalEnergyTransferRatePerItem(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get())) return PedestalConfig.COMMON.augment_t1CapacityEnergy.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get())) return PedestalConfig.COMMON.augment_t2CapacityEnergy.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get())) return PedestalConfig.COMMON.augment_t3CapacityEnergy.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get())) return PedestalConfig.COMMON.augment_t4CapacityEnergy.get();
        return 0;
    }
    public static int getAdditionalXpTransferRatePerItem(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get())) return PedestalConfig.COMMON.augment_t1CapacityXp.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get())) return PedestalConfig.COMMON.augment_t2CapacityXp.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get())) return PedestalConfig.COMMON.augment_t3CapacityXp.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get())) return PedestalConfig.COMMON.augment_t4CapacityXp.get();
        return 0;
    }
    public static int getAdditionalDustTransferRatePerItem(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get())) return PedestalConfig.COMMON.augment_t1CapacityDust.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get())) return PedestalConfig.COMMON.augment_t2CapacityDust.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get())) return PedestalConfig.COMMON.augment_t3CapacityDust.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get())) return PedestalConfig.COMMON.augment_t4CapacityDust.get();
        return 0;
    }
    public static int getAllowedInsertAmount(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get())) return PedestalConfig.COMMON.augment_t1CapacityInsertSize.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get())) return PedestalConfig.COMMON.augment_t2CapacityInsertSize.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get())) return PedestalConfig.COMMON.augment_t3CapacityInsertSize.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get())) return PedestalConfig.COMMON.augment_t4CapacityInsertSize.get();
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        List<String>listed = new ArrayList<String>(
                Arrays.asList(
                        MODID + ".augments_capacity_itemrate"
                        ,String.valueOf(AugmentTieredCapacity.getAdditionalItemTransferRatePerItem(p_41421_))
                        ,MODID + ".augments_capacity_fluidrate"
                        ,String.valueOf(AugmentTieredCapacity.getAdditionalFluidTransferRatePerItem(p_41421_))
                        ,MODID + ".augments_capacity_energyrate"
                        ,String.valueOf(AugmentTieredCapacity.getAdditionalEnergyTransferRatePerItem(p_41421_))
                        ,MODID + ".augments_capacity_xprate"
                        ,String.valueOf(AugmentTieredCapacity.getAdditionalXpTransferRatePerItem(p_41421_))
                        ,MODID + ".augments_insertable"
                        ,String.valueOf(AugmentTieredCapacity.getAllowedInsertAmount(p_41421_))
                ));

        List<ChatFormatting>colors = new ArrayList<ChatFormatting>(
                Arrays.asList(
                        ChatFormatting.YELLOW
                        ,ChatFormatting.WHITE
                        ,ChatFormatting.AQUA
                        ,ChatFormatting.WHITE
                        ,ChatFormatting.RED
                        ,ChatFormatting.WHITE
                        ,ChatFormatting.GREEN
                        ,ChatFormatting.WHITE
                        ,ChatFormatting.GOLD
                        ,ChatFormatting.GOLD
                ));

        if(isDustLoaded())
        {
            listed = new ArrayList<String>(Arrays.asList(MODID + ".augments_capacity_itemrate",String.valueOf(AugmentTieredCapacity.getAdditionalItemTransferRatePerItem(p_41421_)),MODID + ".augments_capacity_fluidrate",String.valueOf(AugmentTieredCapacity.getAdditionalFluidTransferRatePerItem(p_41421_)),MODID + ".augments_capacity_energyrate",String.valueOf(AugmentTieredCapacity.getAdditionalEnergyTransferRatePerItem(p_41421_)),MODID + ".augments_capacity_xprate",String.valueOf(AugmentTieredCapacity.getAdditionalXpTransferRatePerItem(p_41421_)),MODID + ".augments_capacity_dustrate",String.valueOf(AugmentTieredCapacity.getAdditionalDustTransferRatePerItem(p_41421_)),MODID + ".augments_insertable",String.valueOf(AugmentTieredCapacity.getAllowedInsertAmount(p_41421_))));
            colors = new ArrayList<ChatFormatting>(Arrays.asList(ChatFormatting.YELLOW,ChatFormatting.WHITE,ChatFormatting.AQUA,ChatFormatting.WHITE,ChatFormatting.RED,ChatFormatting.WHITE,ChatFormatting.GREEN,ChatFormatting.WHITE,ChatFormatting.LIGHT_PURPLE,ChatFormatting.WHITE,ChatFormatting.GOLD,ChatFormatting.GOLD));
        }


        MowLibTooltipUtils.addTooltipShiftMessageMultiWithStyle(MODID+".augments",p_41423_,listed,colors);
    }
}
