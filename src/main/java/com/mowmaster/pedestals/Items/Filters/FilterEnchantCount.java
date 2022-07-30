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
import java.util.List;
import java.util.Map;

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
    public boolean canAcceptItem(BasePedestalBlockEntity pedestal, ItemStack itemStackIn, int mode) {
        //This only works one way regardless of white or blacklist

        ItemStack filter = pedestal.getFilterInPedestal();
        if(mode==0)
        {
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,mode);
            int range = stackCurrent.size();


            int count = 0;
            for(int i=0;i<range;i++)
            {
                ItemStack stackGet = stackCurrent.get(i);
                if(stackGet.isEnchanted() || stackGet.getItem().equals(Items.ENCHANTED_BOOK))
                {
                    Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stackGet);
                    count +=map.size();
                }
            }
            count = (count>0)?(count):(1);

            if(itemStackIn.isEnchanted() || itemStackIn.getItem().equals(Items.ENCHANTED_BOOK))
            {
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemStackIn);
                if(map.size() == count)
                {
                    return true;
                }
            }
        }
        else return !getFilterType(filter,PedestalModesAndTypes.getModeFromStack(filter));

        return false;
    }

    //Overrides needed for the InteractionResultHolder<ItemStack> use() method in the base class.
    @Override
    public boolean canModeUseInventoryAsFilter(int mode)
    {
        return mode<=1;
    }

    @Override
    public boolean canSetFilterType(int mode)
    {
        if(mode!=0)return true;

        return false;
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
            int count = 0;
            for(int j=0;j<filterQueue.size();j++) {

                ItemStack stackGet = filterQueue.get(j);
                if(stackGet.isEnchanted() || stackGet.getItem().equals(Items.ENCHANTED_BOOK))
                {
                    Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stackGet);
                    count +=map.size();
                }
            }
            enchants = Component.literal(""+((count>0)?(count):(1))+"");
        }
        enchants.withStyle();
        player.displayClientMessage(enchants, false);
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
