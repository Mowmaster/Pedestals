package com.mowmaster.pedestals.Compat.JEI;

import com.mowmaster.mowlib.Recipes.*;
import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import mezz.jei.api.recipe.RecipeType;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class JEIPedestalsRecipeTypes
{
    public static final RecipeType<CobbleGenRecipe> COBBLE_GEN_RECIPE =
            RecipeType.create(MODID, "cobble_gen", CobbleGenRecipe.class);







}
