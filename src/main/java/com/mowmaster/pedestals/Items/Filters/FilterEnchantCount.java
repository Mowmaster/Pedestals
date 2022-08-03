package com.mowmaster.pedestals.Items.Filters;


import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class FilterEnchantCount extends BaseFilter{
    public FilterEnchantCount(Properties p_41383_) {
        super(p_41383_, IPedestalFilter.FilterDirection.INSERT);
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
