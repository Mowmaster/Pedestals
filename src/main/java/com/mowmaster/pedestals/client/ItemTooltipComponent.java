package com.mowmaster.pedestals.Client;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ItemTooltipComponent implements TooltipComponent
{
    private final NonNullList<ItemStack> items;

    public ItemTooltipComponent(NonNullList<ItemStack> p_150677_) {
        this.items = p_150677_;
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }



    /*private final ItemStack displayStack;

    public ItemTooltipComponent(ItemStack stack){
        this.displayStack = stack;
    }

    public ItemStack getDisplayStack(){
        return this.displayStack;
    }*/
}
