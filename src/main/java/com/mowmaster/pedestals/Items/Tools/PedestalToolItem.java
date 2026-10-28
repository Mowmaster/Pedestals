package com.mowmaster.pedestals.Items.Tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

// A tool that only does something from the offhand while left-clicking a pedestal (the pedestal block checks for
// it): the upgrade tool, the tool swapper and the work card tool. Shows how to use it in its tooltip.
public class PedestalToolItem extends Item
{
    public PedestalToolItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    // Lang key item.pedestals.<id>.tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
}
