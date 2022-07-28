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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.List;


import net.minecraft.world.item.Item.Properties;

public class FilterRestricted extends BaseFilter{
    public FilterRestricted(Properties p_41383_) {
        super(p_41383_);
    }

    public static int getColor(ItemStack filterIn)
    {
        return 65280;
    }

    @Override
    public int canAcceptCount(BasePedestalBlockEntity pedestal, Level world, BlockPos pos, ItemStack itemInPedestal, ItemStack itemStackIncoming, int mode) {
        if(itemInPedestal.isEmpty())
        {
            ItemStack filter = pedestal.getFilterInPedestal();
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,mode);
            int range = stackCurrent.size();
            int count = 0;
            int maxIncomming = itemStackIncoming.getMaxStackSize();
            for(int i=0;i<range;i++)
            {
                count +=stackCurrent.get(i).getCount();
                //if(count>=maxIncomming)break;
            }
            if(mode==0)
            {
                return (count>0)?((count>maxIncomming)?(maxIncomming):(count)):(1);
            }
            else return count;
        }
        return 0;
    }

    @Override
    public boolean canModeUseInventoryAsFilter(int mode)
    {
        return mode<=4;
    }

    @Override
    public boolean canSetFilterType(int mode)
    {
        return false;
    }

    @Override
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.GOLD,filterStack.getDisplayName().getString());

        MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,MODID + ".filters.tooltip_filterlist");

        MutableComponent enchants = Component.literal("1");
        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,PedestalModesAndTypes.getModeFromStack(filterStack));
        int range = filterQueue.size();
        if(range>0)
        {
            int count = 0;
            for(int i=0;i<range;i++)
            {
                count +=filterQueue.get(i).getCount();
                if(count>=64)break;
            }

            enchants = Component.literal(""+((count>0)?((count>64)?(64):(count)):(1))+"");
        }
        enchants.withStyle(ChatFormatting.GRAY);
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
            changed.withStyle(ChatFormatting.GOLD);
            MutableComponent type = Component.translatable(MODID + PedestalModesAndTypes.getModeLocalizedString(filterMode));
            changed.append(type);
            p_41423_.add(changed);
        }
    }*/
}
