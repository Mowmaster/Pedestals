package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;

import com.mowmaster.pedestals.PedestalUtils.PedestalModesAndTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.stream.IntStream;

public class FilterItemStack extends BaseFilter{
    public FilterItemStack(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public int canAcceptCount(BasePedestalBlockEntity pedestal, Level world, BlockPos pos, ItemStack itemInPedestal, ItemStack itemStackIncoming, int mode) {

        ItemStack filter = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,mode);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof FilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            if(itemInPedestal.isEmpty())
            {
                List<ItemStack> stackCurrentRestricted = readFilterQueueFromNBT(itemFromInv, PedestalModesAndTypes.getModeFromStack(itemFromInv));
                int rangeRestricted = stackCurrentRestricted.size();
                int count = 0;
                int maxIncomming = itemStackIncoming.getMaxStackSize();
                for(int i=0;i<rangeRestricted;i++)
                {
                    count +=stackCurrent.get(i).getCount();
                    if(count>=maxIncomming)break;
                }

                if(mode==0)
                {
                    return (count>0)?((count>maxIncomming)?(maxIncomming):(count)):(1);
                }
                else return count;
            }

            return 0;
        }

        return super.canAcceptCount(pedestal, world, pos, itemInPedestal, itemStackIncoming,mode);
    }

    @Override
    public boolean canAcceptItem(BasePedestalBlockEntity pedestal, ItemStack itemStackIn, int mode) {
        boolean filterBool=getFilterType(pedestal.getFilterInPedestal(),mode);

        if(mode<=1)
        {
            ItemStack filter = pedestal.getFilterInPedestal();
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,mode);
            int range = stackCurrent.size();

            ItemStack itemFromInv = ItemStack.EMPTY;
            itemFromInv = IntStream.range(0,range)//Int Range
                    .mapToObj((stackCurrent)::get)//Function being applied to each interval
                    .filter(itemStack -> doItemsMatch(itemStack,itemStackIn))
                    .findFirst().orElse(ItemStack.EMPTY);

            if(!itemFromInv.isEmpty())
            {
                return !filterBool;
            }
        }
        else return !filterBool;

        return filterBool;
    }
}
