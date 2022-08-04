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
import java.util.stream.Collectors;

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
        List<ItemStack> filterQueue = readFilterQueueFromNBT(filter, getItemTransportMode(filter));
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
    public boolean canModeUseInventoryAsFilter(ItemTransferMode mode) {
        switch (mode)
        {
            case ITEMS: return true;
            case FLUIDS: return false;
            case ENERGY: return false;
            case EXPERIENCE: return false;
            case DUST: return false;
            default: return false;
        }
    }



    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) {
        boolean filterBool = super.canAcceptItems(filter, incomingStack);

        int durabilityTarget = getDurabilityTarget(filter);

        int percentDurabilityCurrent = getPercentDamaged(incomingStack);
        int percentToBeat = durabilityTarget;

        if(filterBool)
        {
            return (incomingStack.isDamageableItem())?((percentDurabilityCurrent>=percentToBeat)?(true):(false)):(false);
        }
        else
        {
            return (incomingStack.isDamageableItem())?((percentDurabilityCurrent<=percentToBeat)?(true):(false)):(false);
        }

    }


    @Override
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        if(!filterStack.getItem().equals(DeferredRegisterItems.FILTER_BASE.get()))
        {
            MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.WHITE,filterStack.getDisplayName().getString());

            boolean filterType = getFilterType(filterStack,getItemTransportMode(filterStack));
            String above = MODID + ".filters.tooltip_filterabove";
            String below = MODID + ".filters.tooltip_filterbelow";
            List<String> listed = new ArrayList<>();
            listed.add((filterType)?(below):(above));
            MowLibMessageUtils.messagePlayerChatWithAppend(MODID,player,ChatFormatting.GOLD,MODID + ".filters.tooltip_filtertype",listed);

            List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,getItemTransportMode(filterStack));
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
    public void setFilterType(Player player, ItemStack heldItem)
    {
        setFilterTypeCustom(player, heldItem, ".filter_type_below", ".filter_type_above", ChatFormatting.BLACK, ChatFormatting.WHITE, ".filter_type_changed");
    }

    @Override
    public Component filterTypeTooltip(ItemTransferMode mode, boolean filterType)
    {
        MutableComponent filterList = Component.translatable(MODID + ".filter_type");
        MutableComponent white = Component.translatable(MODID + ".filter_type_above");
        MutableComponent black = Component.translatable(MODID + ".filter_type_below");
        filterList.append((filterType)?(black):(white));
        filterList.withStyle(ChatFormatting.WHITE);

        return filterList;
    }


}
