package com.mowmaster.pedestals.Items.WorkCards;

import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Augments.AugmentBase;
import com.mowmaster.pedestals.Items.ISelectablePoints;
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

public class WorkCardPedestals extends WorkCardBase implements ISelectablePoints {

    //Locations in PedestalBlock class
    //Ln486
    //Ln883

    public WorkCardPedestals(Properties p_41383_) {
        super(p_41383_);
    }

    public int getWorkCardType()
    {
        return 3;
    }



}
