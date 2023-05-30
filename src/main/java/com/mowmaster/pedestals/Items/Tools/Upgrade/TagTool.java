package com.mowmaster.pedestals.Items.Tools.Upgrade;


import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Tools.BaseTool;
import com.mowmaster.pedestals.Items.Tools.IPedestalTool;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.item.Item.Properties;

public class TagTool extends BaseTool implements IPedestalTool
{
    public TagTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_TAGTOOL.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_UPGRADETOOL.get().getDefaultInstance(); }

    @Override
    public InteractionResultHolder interactTargetAir(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {
        ItemStack mainhand = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        if(mainhand.getItem().equals(getMainTool().getItem()))
        {
            if(!offhand.isEmpty() && offhand.getTags().count()>0)
            {
                MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.GRAY,"-----> " + ForgeRegistries.ITEMS.getKey(offhand.getItem()).toString() + " <-----");
                offhand.getTags().forEach(tagKey -> MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.WHITE,tagKey.location().toString()));
                MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.GRAY,"--------------------");
            }
        }

        return  InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
