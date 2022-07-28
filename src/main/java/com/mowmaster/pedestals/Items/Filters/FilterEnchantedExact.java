package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.PedestalUtils.PedestalModesAndTypes;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;
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
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;


import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item.Properties;

public class FilterEnchantedExact extends BaseFilter{
    public FilterEnchantedExact(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public boolean canAcceptItem(BasePedestalBlockEntity pedestal, ItemStack itemStackIn, int mode) {
        boolean filterBool=getFilterType(pedestal.getFilterInPedestal(),mode);

        if(mode==0)
        {
            int countAnyMatches = 0;
            if(itemStackIn.isEnchanted() || itemStackIn.getItem().equals(Items.ENCHANTED_BOOK))
            {
                ItemStack filter = pedestal.getFilterInPedestal();
                List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,mode);
                int range = stackCurrent.size();

                Map<Enchantment, Integer> mapIncomming = EnchantmentHelper.getEnchantments(itemStackIn);
                for(Map.Entry<Enchantment, Integer> entry : mapIncomming.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    int level = entry.getValue();

                    ItemStack itemFromInv = ItemStack.EMPTY;
                    itemFromInv = IntStream.range(0,range)//Int Range
                            .mapToObj((stackCurrent)::get)//Function being applied to each interval
                            //Check to make sure filter item is enchanted
                            .filter(itemStack -> itemStack.isEnchanted() || itemStack.getItem().equals(Items.ENCHANTED_BOOK))
                            //Check to see if any have matching enchant sizes
                            .filter(itemStack -> EnchantmentHelper.getEnchantments(itemStack).size()==mapIncomming.size())
                            //Check if filter item has any enchant that the item in the pedestal has
                            .filter(itemStack -> EnchantmentHelper.getEnchantments(itemStack).containsKey(enchantment))
                            .filter(itemStack -> EnchantmentHelper.getEnchantments(itemStack).get(enchantment).intValue() == level)
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!itemFromInv.isEmpty())
                    {
                        countAnyMatches ++;
                    }
                }
                if(countAnyMatches==mapIncomming.size())
                {
                    return !filterBool;
                }
            }
        }
        else return !filterBool;

        return filterBool;
    }

    //Overrides needed for the InteractionResultHolder<ItemStack> use() method in the base class.
    @Override
    public boolean canModeUseInventoryAsFilter(int mode)
    {
        return mode<=0;
    }

    @Override
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();

        MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.GOLD,filterStack.getDisplayName().getString());

        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,PedestalModesAndTypes.getModeFromStack(filterStack));
        if(filterQueue.size()>0)
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,MODID + ".filters.tooltip_filterlist");

            for(int i=0;i<filterQueue.size();i++) {

                if(!filterQueue.get(i).isEmpty())
                {
                    Map<Enchantment, Integer> mapIncomming = EnchantmentHelper.getEnchantments(filterQueue.get(i));
                    for(Map.Entry<Enchantment, Integer> entry : mapIncomming.entrySet()) {
                        Enchantment enchantment = entry.getKey();
                        int level = entry.getValue();

                        MutableComponent enchants = Component.literal(enchantment.getDescriptionId());
                        enchants.append(" "+level+"");
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

    /*@Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        if(!p_41421_.getItem().equals(DeferredRegisterItems.FILTER_BASE))
        {
            boolean filterType = getFilterType(p_41421_,PedestalModesAndTypes.getModeFromStack(p_41421_));
            int filterMode = PedestalModesAndTypes.getModeFromStack(p_41421_);

            MutableComponent filterList = Component.translatable(MODID + ".filter_type");
MutableComponent white = Component.translatable(MODID + ".filter_type_whitelist");
MutableComponent black = Component.translatable(MODID + ".filter_type_blacklist");
filterList.append((filterType)?(black):(white));
filterList.withStyle(ChatFormatting.WHITE);
p_41423_.add(filterList);

            MutableComponent changed = Component.translatable(MODID + ".tooltip_mode");
            String typeString = "";
            switch(filterMode)
            {
                case 0: typeString = ".mode_items"; break;
                case 1: typeString = ".mode_fluids"; break;
                case 2: typeString = ".mode_energy"; break;
                case 3: typeString = ".mode_experience"; break;
                default: typeString = ".error"; break;
            }
            changed.withStyle(ChatFormatting.GOLD);
            MutableComponent type = Component.translatable(MODID + typeString);
            changed.append(type);
            p_41423_.add(changed);
        }
    }*/
}
