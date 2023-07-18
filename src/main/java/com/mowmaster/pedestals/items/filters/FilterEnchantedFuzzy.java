package com.mowmaster.pedestals.items.filters;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import com.mowmaster.mowlib.Items.Filters.IPedestalFilter;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;
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
import java.util.stream.IntStream;

public class FilterEnchantedFuzzy extends BaseFilter{
    public FilterEnchantedFuzzy(Properties p_41383_) {
        super(p_41383_, IPedestalFilter.FilterDirection.INSERT);
    }

    @Override
    public boolean canModeUseInventoryAsFilter(ItemTransferMode mode) {
        switch (mode)
        {
            case ITEMS:         return true;
            case FLUIDS:        return false;
            case ENERGY:        return false;
            case EXPERIENCE:    return false;
            case DUST:          return false;
            default:            return false;
        }
    }

    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) {
        boolean filterBool = super.canAcceptItems(filter, incomingStack);

        if(incomingStack.isEnchanted() || incomingStack.getItem().equals(Items.ENCHANTED_BOOK))
        {
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.ITEMS);
            int range = stackCurrent.size();

            Map<Enchantment, Integer> mapIncomming = EnchantmentHelper.getEnchantments(incomingStack);

            for(Map.Entry<Enchantment, Integer> entry : mapIncomming.entrySet()) {
                Enchantment enchantment = entry.getKey();
                ItemStack itemFromInv = ItemStack.EMPTY;
                itemFromInv = IntStream.range(0,range)
                        .mapToObj((stackCurrent)::get)
                        .filter(itemStack -> itemStack.isEnchanted() || itemStack.getItem().equals(Items.ENCHANTED_BOOK))
                        .filter(itemStack -> EnchantmentHelper.getEnchantments(itemStack).containsKey(enchantment))
                        .findFirst().orElse(ItemStack.EMPTY);

                if(!itemFromInv.isEmpty())
                {
                    return filterBool;
                }
            }
        }

        return !filterBool;
    }

    @Override
    public void chatDetails(Player player, MowLibBaseBlockEntity pedestal, ItemStack filterStack) {
        MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.GOLD,filterStack.getDisplayName().getString());

        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,getItemTransportMode(filterStack));
        if(filterQueue.size()>0)
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,MODID + ".filters.tooltip_filterlist");

            for(int i=0;i<filterQueue.size();i++) {

                if(!filterQueue.get(i).isEmpty())
                {
                    Map<Enchantment, Integer> mapIncomming = EnchantmentHelper.getEnchantments(filterQueue.get(i));
                    for(Map.Entry<Enchantment, Integer> entry : mapIncomming.entrySet()) {
                        MutableComponent enchants = Component.literal(entry.getKey().getDescriptionId());
                        enchants.withStyle(ChatFormatting.GRAY);
                        player.displayClientMessage(enchants, false);
                    }
                }
                MutableComponent enchants = Component.literal("--------------------");
                enchants.withStyle(ChatFormatting.GRAY);
                player.displayClientMessage(enchants, false);
            }
        }
    }
}
