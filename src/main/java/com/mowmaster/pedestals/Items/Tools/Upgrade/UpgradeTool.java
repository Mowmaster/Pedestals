package com.mowmaster.pedestals.Items.Tools.Upgrade;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Tools.BaseTool;
import com.mowmaster.pedestals.Items.Tools.IPedestalTool;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class UpgradeTool extends BaseTool implements IPedestalTool {
    public UpgradeTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_UPGRADETOOL.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_TOOLSWAPPER.get().getDefaultInstance(); }

    @Override
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {
        if(pedestal.hasCoin()) {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            MowLibMessageUtils.messagePlayerChat(player, ChatFormatting.LIGHT_PURPLE, MODID + ".tool_coininpedestal");
            MowLibMessageUtils.messagePlayerChatText(player, ChatFormatting.WHITE, coinInPedestal.getDisplayName().getString());
            if(coinInPedestal.getItem() instanceof ItemUpgradeBase upgrade)
            {
                upgrade.sendUpgradeCustomChat(player,coinInPedestal);
            }
        } else {
            MowLibMessageUtils.messagePlayerChat(player, ChatFormatting.LIGHT_PURPLE, MODID + ".tool_coininpedestal_not");
        }
    }
}
