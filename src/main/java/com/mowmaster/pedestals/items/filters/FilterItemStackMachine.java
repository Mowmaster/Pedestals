package com.mowmaster.pedestals.items.filters;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibFluidUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.stream.IntStream;

public class FilterItemStackMachine extends BaseFilter{
    public FilterItemStackMachine(Properties p_41383_) {
        super(p_41383_, FilterDirection.NEUTRAL);
    }

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
                .filter(itemStack -> doItemsMatch(itemStack,incomingStack))
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
                .filter(itemStack -> MowLibFluidUtils.getFluidStackFromItemStack(itemStack).equals(incomingFluidStack))
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
