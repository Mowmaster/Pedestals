package com.mowmaster.pedestals.Items.Filters;

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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


import net.minecraft.world.item.Item.Properties;

public class FilterDurability extends BaseFilter
{
    public FilterDurability(Properties p_41383_) {
        super(p_41383_, IPedestalFilter.FilterDirection.INSERT);
    }

    public int getPercentDamaged(ItemStack itemIn)
    {
        if(itemIn.isDamageableItem())
        {
            int maxDamage = itemIn.getMaxDamage();
            int damage = itemIn.getDamageValue();
            int durabilityCurrent = maxDamage-damage;
            int percentDurability = Math.floorDiv((durabilityCurrent*100),maxDamage);
            return percentDurability;
        }
        return 100;
    }

    public int getDurabilityTarget(ItemStack filter)
    {
        int returner = 0;
        List<ItemStack> filterQueue = readFilterQueueFromNBT(filter, PedestalModesAndTypes.getModeFromStack(filter));
        if(filterQueue.size()>0)
        {
            for(int i=0;i<filterQueue.size();i++)
            {
                ItemStack stackGet = filterQueue.get(i);
                returner += getPercentDamaged(stackGet);
                if(returner>100)break;
            }
        }

        return (returner>100)?(100):(returner);
    }

    @Override
    public boolean canAcceptItem(BasePedestalBlockEntity pedestal, ItemStack itemStackIn, int mode) {
        boolean filterBool = getFilterType(pedestal.getFilterInPedestal(),mode);
        int durabilityTarget = getDurabilityTarget(pedestal.getFilterInPedestal());

        int percentDurabilityCurrent = getPercentDamaged(itemStackIn);
        int percentToBeat = durabilityTarget;

        if(mode==0)
        {
            if(filterBool)
            {
                return (itemStackIn.isDamageableItem())?((percentDurabilityCurrent<=percentToBeat)?(true):(false)):(false);
            }
            else
            {
                return (itemStackIn.isDamageableItem())?((percentDurabilityCurrent>=percentToBeat)?(true):(false)):(false);
            }
        }
        else return !filterBool;
    }

    //Overrides needed for the InteractionResultHolder<ItemStack> use() method in the base class.
    @Override
    public boolean canModeUseInventoryAsFilter(int mode)
    {
        return mode<=0;
    }

    @Override
    public void setFilterType(Player player, ItemStack heldItem)
    {
        setFilterTypeCustom(player, heldItem, ".filter_type_below", ".filter_type_above", ChatFormatting.BLACK, ChatFormatting.WHITE, ".filter_type_changed");
    }

    @Override
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        if(!filterStack.getItem().equals(DeferredRegisterItems.FILTER_BASE.get()))
        {
            MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.WHITE,filterStack.getDisplayName().getString());

            boolean filterType = getFilterType(filterStack,PedestalModesAndTypes.getModeFromStack(filterStack));
            String above = MODID + ".filters.tooltip_filterabove";
            String below = MODID + ".filters.tooltip_filterbelow";
            List<String> listed = new ArrayList<>();
            listed.add((filterType)?(below):(above));
            MowLibMessageUtils.messagePlayerChatWithAppend(MODID,player,ChatFormatting.GOLD,MODID + ".filters.tooltip_filtertype",listed);

            List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,PedestalModesAndTypes.getModeFromStack(filterStack));
            if(filterQueue.size()>0)
            {
                MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,MODID + ".filters.tooltip_filterlist");

                MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.GRAY,""+getDurabilityTarget(pedestal.getFilterInPedestal())+"");
            }
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.DARK_RED,MODID + ".baseItem");
        }
    }

    @Override
    public Component filterTypeTooltip(int filterMode, boolean filterType)
    {
        MutableComponent filterList = Component.translatable(MODID + ".filter_type");
        MutableComponent white = Component.translatable(MODID + ".filter_type_above");
        MutableComponent black = Component.translatable(MODID + ".filter_type_below");
        filterList.append((filterType)?(black):(white));
        filterList.withStyle(ChatFormatting.WHITE);

        return filterList;
    }


    /*@Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        if(!p_41421_.getItem().equals(DeferredRegisterItems.FILTER_BASE))
        {
            boolean filterType = getFilterType(p_41421_,PedestalModesAndTypes.getModeFromStack(p_41421_));
            int filterMode = PedestalModesAndTypes.getModeFromStack(p_41421_);

            MutableComponent filterList = Component.translatable(MODID + ".filter_type");
            MutableComponent white = Component.translatable(MODID + ".filter_type_above");
            MutableComponent black = Component.translatable(MODID + ".filter_type_below");
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
