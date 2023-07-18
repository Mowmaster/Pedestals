package com.mowmaster.pedestals.items.tools.augment;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.items.tools.BaseTool;
import com.mowmaster.pedestals.items.tools.IPedestalTool;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;

public class AugmentTool_Robin extends BaseTool implements IPedestalTool
{
    public AugmentTool_Robin(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_AUGMENTS_ROUNDROBIN.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_AUGMENTS_DIFFUSER.get().getDefaultInstance(); }

    @Override
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {

        if(pedestal.hasRRobin())
        {
            MutableComponent hasAugment = Component.translatable(MODID + ".tool.augment.roundrobin");
            hasAugment.withStyle(ChatFormatting.BLUE);
            player.displayClientMessage(hasAugment, false);
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.DARK_BLUE,MODID + ".tool.augment.roundrobin_not");
        }
    }
}
