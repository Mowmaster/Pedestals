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

public class AugmentTool_Diffuser extends BaseTool implements IPedestalTool
{
    public AugmentTool_Diffuser(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_AUGMENTS_DIFFUSER.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_AUGMENTS_COLLIDE.get().getDefaultInstance(); }

    @Override
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {

        if(pedestal.hasRenderAugment())
        {
            MutableComponent hasAugment = Component.translatable(MODID + ".tool.augment.diffuser");
            hasAugment.withStyle(ChatFormatting.LIGHT_PURPLE);
            player.displayClientMessage(hasAugment, false);

            int type = pedestal.getRendererType();
/*
        // 0 - No Particles
        // 1 - No Render Item
        // 2 - No Render upgrade
        // 3 - No Particles/No Render Item
        // 4 - No Particles/No Render upgrade
        // 5 - No Render Item/No Render upgrade
        // 6 - No Particles/No Render Item/No Render upgrade
        // 7 - No augment exists and thus all rendering is fine.
*/
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.DARK_PURPLE,MODID + ".tool.augment.diffuser_not");
        }
    }
}
