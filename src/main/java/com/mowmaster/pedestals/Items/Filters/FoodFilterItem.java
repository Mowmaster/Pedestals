package com.mowmaster.pedestals.Items.Filters;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

// Matches food that gives hunger (1.20's FilterFood). No list.
public class FoodFilterItem extends ItemOnlyFilterItem
{
    public FoodFilterItem(Properties properties)
    {
        super(properties);
    }

    @Override
    protected boolean usesList()
    {
        return false;
    }

    @Override
    protected boolean matchesItem(ItemStack incoming, FilterData.Entry entry)
    {
        FoodProperties food = incoming.get(DataComponents.FOOD);
        return food != null && food.nutrition() > 0;
    }
}
