package com.mowmaster.pedestals.items.tools.upgrade;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.items.tools.BaseTool;
import com.mowmaster.pedestals.items.tools.IPedestalTool;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ToolSwapper extends BaseTool implements IPedestalTool
{
    public ToolSwapper(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_TOOLSWAPPER.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_FILTERTOOL.get().getDefaultInstance(); }

    @Override
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {
        if(pedestal.hasTool())
        {
            ItemStack toolInPedestal = pedestal.getActualToolStack();
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,"Pedestals.tool_toolinpedestal");
            MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.WHITE,toolInPedestal.getDisplayName().getString());
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,"Pedestals.tool_toolinpedestal_not");
        }
    }
}
