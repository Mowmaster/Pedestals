package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.Items.Filters.IPedestalFilter;
import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.pedestals.PedestalUtils.References;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FilterBaseItem extends BaseFilter{
    public FilterBaseItem(Properties p_41383_) {
        super(p_41383_, IPedestalFilter.FilterDirection.INSERT);
    }

    @Override
    public boolean canModeUseInventoryAsFilter(ItemTransferMode mode) {
        return false;
    }

    @Override
    public boolean canSetFilterType(ItemTransferMode mode) {
        return false;
    }

    @Override
    public boolean canSetFilterMode(ItemTransferMode mode) {
        return false;
    }

    @Override
    public boolean showFilterDirection()
    {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,References.MODID + ".filter.tooltip_filterbase",ChatFormatting.DARK_RED);
    }
}
