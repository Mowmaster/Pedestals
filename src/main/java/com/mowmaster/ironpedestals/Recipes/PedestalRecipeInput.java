package com.mowmaster.ironpedestals.Recipes;

import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

// What a pedestal machine recipe is matched against: one item, plus the pedestal itself so the recipe can check
// the fluid, energy and experience it holds. (1.20 copied those into MowLib's "multi container".)
public record PedestalRecipeInput(ItemStack item, BasePedestalBlockEntity pedestal) implements RecipeInput
{
    @Override
    public ItemStack getItem(int index)
    {
        return index == 0 ? item : ItemStack.EMPTY;
    }

    @Override
    public int size()
    {
        return 1;
    }
}
