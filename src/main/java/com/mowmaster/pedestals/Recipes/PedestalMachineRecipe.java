package com.mowmaster.pedestals.Recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

// The parts of Recipe that pedestal recipes all answer the same way: they're only used by upgrades, so they're
// "special" (no recipe book, no unlock toasts) and can't be placed in a crafting grid.
public abstract class PedestalMachineRecipe<T extends RecipeInput> implements Recipe<T>
{
    // Upgrades read the recipe's own fields instead
    @Override
    public ItemStack assemble(T input)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial()
    {
        return true;
    }

    @Override
    public boolean showNotification()
    {
        return false;
    }

    @Override
    public String group()
    {
        return "";
    }

    @Override
    public PlacementInfo placementInfo()
    {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory()
    {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
