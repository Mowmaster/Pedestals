package com.mowmaster.pedestals.items.filters;

import com.mowmaster.mowlib.Items.Filters.IPedestalFilter;
import net.minecraft.world.item.ItemStack;


public class FilterFood extends BaseFilter{
    public FilterFood(Properties p_41383_) {
        super(p_41383_, IPedestalFilter.FilterDirection.INSERT);
    }

    @Override
    public boolean canModeUseInventoryAsFilter(ItemTransferMode mode) {
        return false;
    }

    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) {
        /*List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.ITEMS);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem().getFoodProperties().getNutrition()>0)
                .findFirst().orElse(ItemStack.EMPTY);*/
        boolean filterBool = super.canAcceptItems(filter, incomingStack);

        if(incomingStack.isEmpty())return !filterBool;
        if(incomingStack.getItem().getFoodProperties() != null)
        {
            if(incomingStack.getItem().getFoodProperties().getNutrition()>0)
            {
                return filterBool;
            }
        }

        return !filterBool;
    }
}
