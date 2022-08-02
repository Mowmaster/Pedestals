package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;

import com.mowmaster.pedestals.PedestalUtils.PedestalModesAndTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.fluids.FluidStack;

public class FilterItemStack extends BaseFilter{
    public FilterItemStack(Properties p_41383_) {
        super(p_41383_, IPedestalFilter.FilterDirection.INSERT);
    }

    @Override
    public boolean canModeUseInventoryAsFilter(int mode) {
        switch (mode)
        {
            case 0: return true;
            case 1: return true;
            case 2: return false;
            case 3: return false;
            case 4: return true;
            default: return false;
        }
    }

    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) {
        boolean filterBool = super.canAcceptItems(filter, incomingStack);

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,0);
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

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,1);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> !getFluidStackFromItemStack(itemStack).isEmpty())
                .filter(itemStack -> getFluidStackFromItemStack(itemStack).equals(incomingFluidStack))
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

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,4);
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
