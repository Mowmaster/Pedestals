package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibFluidUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.stream.IntStream;

public class FilterItemMachine extends BaseFilter
{
    public FilterItemMachine(Properties p_41383_) {
        super(p_41383_, FilterDirection.NEUTRAL);
    }

    /*
        So the filter for count takes into account the item stack (first) and based the count on that and not the
        actually count in the restricted filter.
     */

    @Override
    public boolean canModeUseInventoryAsFilter(ItemTransferMode mode) {
        switch (mode)
        {
            case ITEMS:         return true;
            case FLUIDS:        return true;
            case ENERGY:        return false;
            case EXPERIENCE:    return false;
            case DUST:          return true;
            default:            return false;
        }
    }

    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) {
        boolean filterBool = super.canAcceptItems(filter, incomingStack);

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.ITEMS);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem().equals(incomingStack.getItem()))
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            return filterBool;
        }
        else return !filterBool;

    }

    @Override
    public boolean canAcceptFluids(ItemStack filter, FluidStack incomingFluidStack) {
        boolean filterBool = super.canAcceptFluids(filter, incomingFluidStack);

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.FLUIDS);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> !MowLibFluidUtils.getFluidStackFromItemStack(itemStack).isEmpty())
                .filter(itemStack -> MowLibFluidUtils.getFluidStackFromItemStack(itemStack).getFluid().equals(incomingFluidStack.getFluid()))
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            return filterBool;
        }
        else return !filterBool;
    }

    @Override
    public boolean canAcceptDust(ItemStack filter, DustMagic incomingDust) {
        boolean filterBool = super.canAcceptDust(filter, incomingDust);

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.DUST);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> !DustMagic.getDustMagicInItemStack(itemStack).isEmpty())
                .filter(itemStack -> DustMagic.getDustMagicInItemStack(itemStack).isDustEqual(incomingDust))
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            return filterBool;
        }
        else return !filterBool;
    }
}
