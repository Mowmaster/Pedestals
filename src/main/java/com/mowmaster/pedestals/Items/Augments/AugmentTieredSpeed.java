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

public class AugmentTieredSpeed extends AugmentBase{

    public AugmentTieredSpeed(Properties p_41383_) {
        super(p_41383_);
    }

    public int getTicksReducedPerItem(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_SPEED.get()))return PedestalConfig.COMMON.augment_t1SpeedReduction.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_SPEED.get()))return PedestalConfig.COMMON.augment_t2SpeedReduction.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_SPEED.get()))return PedestalConfig.COMMON.augment_t3SpeedReduction.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_SPEED.get()))return PedestalConfig.COMMON.augment_t4SpeedReduction.get();
        return 0;
    }

    public int getAllowedInsertAmount(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_SPEED.get()))return PedestalConfig.COMMON.augment_t1SpeedInsertable.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_SPEED.get()))return PedestalConfig.COMMON.augment_t2SpeedInsertable.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_SPEED.get()))return PedestalConfig.COMMON.augment_t3SpeedInsertable.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_SPEED.get()))return PedestalConfig.COMMON.augment_t4SpeedInsertable.get();
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        if(p_41421_.getItem() instanceof AugmentTieredSpeed speedAugment)
        {
            List<String> listed = new ArrayList<>();
            List<ChatFormatting> colors = new ArrayList<>();

            colors.add(ChatFormatting.YELLOW);
            listed.add(MODID + ".augments_speed_ticksreduced");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+speedAugment.getTicksReducedPerItem(p_41421_.getItem())+"");

            colors.add(ChatFormatting.LIGHT_PURPLE);
            listed.add(MODID + ".augments_insertable");
            colors.add(ChatFormatting.GOLD);
            listed.add(""+speedAugment.getAllowedInsertAmount(p_41421_.getItem())+"");


            TooltipUtils.addTooltipShiftMessageMultiWithStyle(MODID,p_41423_,listed,colors);
        }
    }


}
