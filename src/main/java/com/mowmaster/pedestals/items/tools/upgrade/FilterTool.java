package com.mowmaster.pedestals.items.tools.upgrade;

import com.mowmaster.mowlib.Items.Filters.IPedestalFilter;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.items.tools.BaseTool;
import com.mowmaster.pedestals.items.tools.IPedestalTool;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FilterTool extends BaseTool implements IPedestalTool
{
    public FilterTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_FILTERTOOL.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_WORKTOOL.get().getDefaultInstance(); }

    @Override
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {
        if(pedestal.hasFilter())
        {
            ItemStack filterInPedestal = pedestal.getFilterInPedestal();
            if(filterInPedestal.getItem() instanceof IPedestalFilter filter)
            {
                filter.chatDetails(player,pedestal,filterInPedestal);
            }
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,"Pedestals.tool_filterinpedestal_not");
        }
    }
}
