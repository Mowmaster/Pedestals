package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import com.mowmaster.mowlib.Items.Filters.IPedestalFilter;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraftforge.fluids.FluidStack;

public class FilterRestricted extends BaseFilter{
    public FilterRestricted(Properties p_41383_) {
        super(p_41383_, IPedestalFilter.FilterDirection.INSERT);
    }

    public static int getColor(ItemStack filterIn)
    {
        return 65280;
    }

    @Override
    public int canAcceptCountItems(MowLibBaseBlockEntity filterableBlockEntity, ItemStack filterStack, int maxSpaceSize, int spaceAvailable, ItemStack itemStackIncoming) {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.ITEMS);

        int count = stackCurrent.stream()
                .map(itemStack -> itemStack.getCount())
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (a,b) -> a + b);

        if(count > 0)
        {
            return (count > maxSpaceSize)?(maxSpaceSize):(count);
            //return (count > itemStackIncoming.getMaxStackSize())?(itemStackIncoming.getMaxStackSize()):(count);
        }

        return (stackCurrent.size()<=0)?(super.canAcceptCountItems(filterableBlockEntity,filterStack,maxSpaceSize,spaceAvailable, itemStackIncoming)):(0);
    }

    @Override
    public int canAcceptCountFluids(MowLibBaseBlockEntity filterableBlockEntity, ItemStack filterStack, int maxSpaceSize, int spaceAvailable, FluidStack incomingFluidStack) {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.FLUIDS);

        int count = stackCurrent.stream()
                .map(itemStack -> itemStack.getCount())
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (a,b) -> a + b);

        if(count > 0)
        {
            return (count > maxSpaceSize)?(maxSpaceSize):(count);
            //return (count > filterableBlockEntity.getFluidCapacity())?(filterableBlockEntity.getFluidCapacity()):(count);
        }

        return (stackCurrent.size()<=0)?(super.canAcceptCountFluids(filterableBlockEntity,filterStack,maxSpaceSize,spaceAvailable, incomingFluidStack)):(0);
    }

    @Override
    public int canAcceptCountEnergy(MowLibBaseBlockEntity filterableBlockEntity, ItemStack filterStack, int maxSpaceSize, int spaceAvailable, int incomingEnergyAmount) {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.ENERGY);

        int count = stackCurrent.stream()
                .map(itemStack -> itemStack.getCount())
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (a,b) -> a + b);

        if(count > 0)
        {
            return (count > maxSpaceSize)?(maxSpaceSize):(count);
            //return (count > filterableBlockEntity.getEnergyCapacity())?(filterableBlockEntity.getEnergyCapacity()):(count);
        }

        return (stackCurrent.size()<=0)?(super.canAcceptCountEnergy(filterableBlockEntity,filterStack,maxSpaceSize,spaceAvailable, incomingEnergyAmount)):(0);
    }

    @Override
    public int canAcceptCountExperience(MowLibBaseBlockEntity filterableBlockEntity, ItemStack filterStack, int maxSpaceSize, int spaceAvailable, int incomingExperienceAmount) {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.EXPERIENCE);

        int count = stackCurrent.stream()
                .map(itemStack -> itemStack.getCount())
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (a,b) -> a + b);

        if(count > 0)
        {
            return (count > maxSpaceSize)?(maxSpaceSize):(count);
            //return (count > filterableBlockEntity.getExperienceCapacity())?(filterableBlockEntity.getExperienceCapacity()):(count);
        }

        return (stackCurrent.size()<=0)?(super.canAcceptCountExperience(filterableBlockEntity,filterStack,maxSpaceSize,spaceAvailable, incomingExperienceAmount)):(0);
    }

    @Override
    public int canAcceptCountDust(MowLibBaseBlockEntity filterableBlockEntity, ItemStack filterStack, int maxSpaceSize, int spaceAvailable, DustMagic incomingDust) {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.DUST);

        int count = stackCurrent.stream()
                .map(itemStack -> itemStack.getCount())
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (a,b) -> a + b);

        if(count > 0)
        {
            return (count > maxSpaceSize)?(maxSpaceSize):(count);
            //return (count > filterableBlockEntity.getDustCapacity())?(filterableBlockEntity.getDustCapacity()):(count);
        }

        return (stackCurrent.size()<=0)?(super.canAcceptCountDust(filterableBlockEntity,filterStack,maxSpaceSize,spaceAvailable, incomingDust)):(0);
    }

    /*
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
    }*/

    //Allows all mode to use inventory as a filter for the counter to check
    @Override
    public boolean canModeUseInventoryAsFilter(ItemTransferMode mode) {
        return true;
    }

    //Cant set white or blacklist for any mode if false
    @Override
    public boolean canSetFilterType(ItemTransferMode mode) {
        return false;
    }

    @Override
    public void chatDetails(Player player, MowLibBaseBlockEntity pedestal, ItemStack filterStack) {
        MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.GOLD,filterStack.getDisplayName().getString());

        MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,MODID + ".filters.tooltip_filterlist");

        MutableComponent enchants = Component.literal("1");
        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,getItemTransportMode(filterStack));
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
}
