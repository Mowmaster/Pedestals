package com.mowmaster.pedestals.items.tools.upgrade;

import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.items.tools.BaseTool;
import com.mowmaster.pedestals.items.tools.IPedestalTool;
import com.mowmaster.pedestals.items.upgrades.pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class UpgradeTool extends BaseTool implements IPedestalTool
{
    public UpgradeTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_UPGRADETOOL.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_TOOLSWAPPER.get().getDefaultInstance(); }

    @Override
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {
        if(pedestal.hasCoin())
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,"Pedestals.tool_coininpedestal");
            MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.WHITE,coinInPedestal.getDisplayName().getString());
            if(coinInPedestal.getItem() instanceof ItemUpgradeBase upgrade)
            {
                upgrade.sendUpgradeCustomChat(player,coinInPedestal);
            }
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,"Pedestals.tool_coininpedestal_not");
        }
    }
}
