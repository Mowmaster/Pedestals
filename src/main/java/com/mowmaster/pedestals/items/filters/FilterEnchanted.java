package com.mowmaster.pedestals.items.filters;

import com.mowmaster.mowlib.Items.Filters.IPedestalFilter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FilterEnchanted extends BaseFilter{
    public FilterEnchanted(Properties p_41383_) {
        super(p_41383_, IPedestalFilter.FilterDirection.INSERT);
    }

    @Override
    public boolean canModeUseInventoryAsFilter(ItemTransferMode mode) {
        return false;
    }

    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) {
        boolean filterBool = super.canAcceptItems(filter, incomingStack);

        if(incomingStack.isEmpty())return !filterBool;
        if(incomingStack.isEnchanted() || incomingStack.getItem().equals(Items.ENCHANTED_BOOK))
        {
            return filterBool;
        }
        else return !filterBool;
    }

}
