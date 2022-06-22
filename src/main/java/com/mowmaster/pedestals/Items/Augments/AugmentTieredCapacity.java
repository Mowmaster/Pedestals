package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.mowlib.MowLibUtils.TooltipUtils;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class AugmentTieredCapacity extends AugmentBase{

    public AugmentTieredCapacity(Properties p_41383_) {
        super(p_41383_);
    }

    public int getAdditionalItemTransferRatePerItem(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get()))return PedestalConfig.COMMON.augment_t1CapacityItem.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get()))return PedestalConfig.COMMON.augment_t2CapacityItem.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get()))return PedestalConfig.COMMON.augment_t3CapacityItem.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get()))return PedestalConfig.COMMON.augment_t4CapacityItem.get();
        return 0;
    }
    public int getAdditionalFluidTransferRatePerItem(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get()))return PedestalConfig.COMMON.augment_t1CapacityFluid.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get()))return PedestalConfig.COMMON.augment_t2CapacityFluid.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get()))return PedestalConfig.COMMON.augment_t3CapacityFluid.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get()))return PedestalConfig.COMMON.augment_t4CapacityFluid.get();
        return 0;
    }
    public int getAdditionalEnergyTransferRatePerItem(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get()))return PedestalConfig.COMMON.augment_t1CapacityEnergy.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get()))return PedestalConfig.COMMON.augment_t2CapacityEnergy.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get()))return PedestalConfig.COMMON.augment_t3CapacityEnergy.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get()))return PedestalConfig.COMMON.augment_t4CapacityEnergy.get();
        return 0;
    }
    public int getAdditionalXpTransferRatePerItem(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get()))return PedestalConfig.COMMON.augment_t1CapacityXp.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get()))return PedestalConfig.COMMON.augment_t2CapacityXp.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get()))return PedestalConfig.COMMON.augment_t3CapacityXp.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get()))return PedestalConfig.COMMON.augment_t4CapacityXp.get();
        return 0;
    }
    public int getAllowedInsertAmount(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get()))return PedestalConfig.COMMON.augment_t1CapacityInsertSize.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get()))return PedestalConfig.COMMON.augment_t2CapacityInsertSize.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get()))return PedestalConfig.COMMON.augment_t3CapacityInsertSize.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get()))return PedestalConfig.COMMON.augment_t4CapacityInsertSize.get();
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        if(p_41421_.getItem() instanceof AugmentTieredCapacity capacityAugment)
        {
            List<String> listed = new ArrayList<>();
            List<ChatFormatting> colors = new ArrayList<>();

            colors.add(ChatFormatting.YELLOW);
            listed.add(MODID + ".augments_capacity_itemrate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+capacityAugment.getAdditionalItemTransferRatePerItem(p_41421_.getItem())+"");

            colors.add(ChatFormatting.AQUA);
            listed.add(MODID + ".augments_capacity_fluidrate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+capacityAugment.getAdditionalFluidTransferRatePerItem(p_41421_.getItem())+"");

            colors.add(ChatFormatting.RED);
            listed.add(MODID + ".augments_capacity_energyrate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+capacityAugment.getAdditionalEnergyTransferRatePerItem(p_41421_.getItem())+"");

            colors.add(ChatFormatting.GREEN);
            listed.add(MODID + ".augments_capacity_xprate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+capacityAugment.getAdditionalXpTransferRatePerItem(p_41421_.getItem())+"");

            colors.add(ChatFormatting.LIGHT_PURPLE);
            listed.add(MODID + ".augments_insertable");
            colors.add(ChatFormatting.GOLD);
            listed.add(""+capacityAugment.getAllowedInsertAmount(p_41421_.getItem())+"");

            TooltipUtils.addTooltipShiftMessageMultiWithStyle(MODID+".augments",p_41423_,listed,colors);
        }
    }


}
