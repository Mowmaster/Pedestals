package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;


import net.minecraft.world.item.Item.Properties;
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
    public int canAcceptCountItems(BasePedestalBlockEntity pedestal, ItemStack itemStackIncoming) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.ITEMS);

        int count = stackCurrent.stream()
                .map(itemStack -> itemStack.getCount())
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (a,b) -> a + b);

        if(count > 0)
        {
            return (count > itemStackIncoming.getMaxStackSize())?(itemStackIncoming.getMaxStackSize()):(count);
        }

        return (stackCurrent.size()<=0)?(super.canAcceptCountItems(pedestal, itemStackIncoming)):(0);
    }

    @Override
    public int canAcceptCountFluids(BasePedestalBlockEntity pedestal, FluidStack incomingFluidStack) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.FLUIDS);

        int count = stackCurrent.stream()
                .map(itemStack -> itemStack.getCount())
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (a,b) -> a + b);

        if(count > 0)
        {
            return (count > pedestal.getFluidCapacity())?(pedestal.getFluidCapacity()):(count);
        }

        return (stackCurrent.size()<=0)?(super.canAcceptCountFluids(pedestal, incomingFluidStack)):(0);
    }

    @Override
    public int canAcceptCountEnergy(BasePedestalBlockEntity pedestal, int incomingEnergyAmount) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.ENERGY);

        int count = stackCurrent.stream()
                .map(itemStack -> itemStack.getCount())
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (a,b) -> a + b);

        if(count > 0)
        {
            return (count > pedestal.getEnergyCapacity())?(pedestal.getEnergyCapacity()):(count);
        }

        return (stackCurrent.size()<=0)?(super.canAcceptCountEnergy(pedestal, incomingEnergyAmount)):(0);
    }

    @Override
    public int canAcceptCountExperience(BasePedestalBlockEntity pedestal, int incomingExperienceAmount) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.EXPERIENCE);

        int count = stackCurrent.stream()
                .map(itemStack -> itemStack.getCount())
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (a,b) -> a + b);

        if(count > 0)
        {
            return (count > pedestal.getExperienceCapacity())?(pedestal.getExperienceCapacity()):(count);
        }

        return (stackCurrent.size()<=0)?(super.canAcceptCountExperience(pedestal, incomingExperienceAmount)):(0);
    }

    @Override
    public int canAcceptCountDust(BasePedestalBlockEntity pedestal, DustMagic incomingDust) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.DUST);

        int count = stackCurrent.stream()
                .map(itemStack -> itemStack.getCount())
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (a,b) -> a + b);

        if(count > 0)
        {
            return (count > pedestal.getDustCapacity())?(pedestal.getDustCapacity()):(count);
        }

        return (stackCurrent.size()<=0)?(super.canAcceptCountDust(pedestal, incomingDust)):(0);
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
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
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
