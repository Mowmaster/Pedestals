package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.mowlib.MowLibUtils.TooltipUtils;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class AugmentTieredRange extends AugmentBase{


    public AugmentTieredRange(Properties p_41383_) {
        super(p_41383_);
    }

    public int getRangeIncreasePerItem(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_RANGE.get()))return PedestalConfig.COMMON.augment_t1RangeIncrease.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_RANGE.get()))return PedestalConfig.COMMON.augment_t2RangeIncrease.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_RANGE.get()))return PedestalConfig.COMMON.augment_t3RangeIncrease.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_RANGE.get()))return PedestalConfig.COMMON.augment_t4RangeIncrease.get();
        return 0;
    }

    public int getAllowedInsertAmount(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_RANGE.get()))return PedestalConfig.COMMON.augment_t1RangeInsertable.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_RANGE.get()))return PedestalConfig.COMMON.augment_t2RangeInsertable.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_RANGE.get()))return PedestalConfig.COMMON.augment_t3RangeInsertable.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_RANGE.get()))return PedestalConfig.COMMON.augment_t4RangeInsertable.get();
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        if(p_41421_.getItem() instanceof AugmentTieredRange rangeAugment)
        {
            List<String> listed = new ArrayList<>();
            List<ChatFormatting> colors = new ArrayList<>();

            colors.add(ChatFormatting.YELLOW);
            listed.add(MODID + ".augments_range_increase");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+rangeAugment.getRangeIncreasePerItem(p_41421_.getItem())+"");
            colors.add(ChatFormatting.LIGHT_PURPLE);
            listed.add(MODID + ".augments_insertable");
            colors.add(ChatFormatting.GOLD);
            listed.add(""+rangeAugment.getAllowedInsertAmount(p_41421_.getItem())+"");


            TooltipUtils.addTooltipShiftMessageMultiWithStyle(MODID,p_41423_,listed,colors);
        }
    }


}
