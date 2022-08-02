package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.PedestalUtils.PedestalModesAndTypes;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

import net.minecraft.world.item.Item.Properties;

public class FilterEnchantCount extends BaseFilter{
    public FilterEnchantCount(Properties p_41383_) {
        super(p_41383_, IPedestalFilter.FilterDirection.INSERT);
    }

    public static int getColor(ItemStack filterIn)
    {
        return (PedestalModesAndTypes.getModeFromStack(filterIn)==0)?(8388736):(MowLibColorReference.getColorFromItemStackInt(filterIn));
    }

    @Override
    public boolean canSetFilterType(int mode)
    {
        //Makes it so only the item transport cant be swapped, as its the one filtering for enchanted count
        if(mode!=0)return true;

        return false;
    }

    @Override
    public boolean canModeUseInventoryAsFilter(int mode) {
        switch (mode)
        {
            case 0: return true;
            case 1: return false;
            case 2: return false;
            case 3: return false;
            case 4: return false;
            default: return false;
        }
    }

    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) {
        boolean filterBool = super.canAcceptItems(filter, incomingStack);

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,0);

        int count = stackCurrent.stream()
                .filter(itemStack -> itemStack.isEnchanted() || itemStack.getItem().equals(Items.ENCHANTED_BOOK))
                .map(itemStack -> EnchantmentHelper.getEnchantments(itemStack).size())
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (a,b) -> a + b);

        if(count > 0)
        {
            if(incomingStack.isEnchanted() || incomingStack.getItem().equals(Items.ENCHANTED_BOOK))
            {
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(incomingStack);
                if(map.size() == count)
                {
                    return filterBool;
                }
            }
        }

        return !filterBool;
    }

    @Override
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();

        MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.GOLD,filterStack.getDisplayName().getString());

        MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,MODID + ".filters.tooltip_filterlist_count");

        MutableComponent enchants = Component.literal("1");
        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,0);
        if(filterQueue.size()>0)
        {
            int count = filterQueue.stream()
                    .filter(itemStack -> itemStack.isEnchanted() || itemStack.getItem().equals(Items.ENCHANTED_BOOK))
                    .map(itemStack -> EnchantmentHelper.getEnchantments(itemStack).size())
                    .collect(Collectors.toList())
                    .stream()
                    .reduce(0, (a,b) -> a + b);

            enchants = Component.literal(""+((count>0)?(count):(1))+"");
        }
        enchants.withStyle();
        player.displayClientMessage(enchants, false);
    }

}
