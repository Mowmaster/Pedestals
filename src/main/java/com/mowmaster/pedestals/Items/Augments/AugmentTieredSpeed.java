package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.mowlib.MowLibUtils.TooltipUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class AugmentTieredSpeed extends AugmentBase{

    private int ticksReduced;
    private int allowedToInsert;
    public AugmentTieredSpeed(Properties p_41383_, int amount, int allowedInsert) {
        super(p_41383_);
        this.ticksReduced = amount;
        this.allowedToInsert = allowedInsert;
    }

    public int getTicksReducedPerItem()
    {
        return ticksReduced;
    }

    public int getAllowedInsertAmount()
    {
        return allowedToInsert;
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
            listed.add(""+speedAugment.getTicksReducedPerItem()+"");

            colors.add(ChatFormatting.LIGHT_PURPLE);
            listed.add(MODID + ".augments_insertable");
            colors.add(ChatFormatting.GOLD);
            listed.add(""+speedAugment.getAllowedInsertAmount()+"");


            TooltipUtils.addTooltipShiftMessageMultiWithStyle(MODID+".augments",p_41423_,listed,colors);
        }
    }


}
