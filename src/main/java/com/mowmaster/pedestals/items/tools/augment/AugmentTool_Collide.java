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

public class AugmentTool_Collide extends BaseTool implements IPedestalTool
{
    public AugmentTool_Collide(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_AUGMENTS_COLLIDE.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_AUGMENTS_SPEED.get().getDefaultInstance(); }

    @Override
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {

        if(pedestal.hasNoCollide())
        {
            MutableComponent hasAugment = Component.translatable(MODID + ".tool.augment.collide");
            hasAugment.withStyle(ChatFormatting.RED);
            player.displayClientMessage(hasAugment, false);
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.DARK_RED,MODID + ".tool.augment.collide_not");
        }
    }
}
