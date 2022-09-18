package com.mowmaster.pedestals.Compat.JEI;

import com.mowmaster.mowlib.Recipes.*;
import com.mowmaster.pedestals.Recipes.BottlerRecipe;
import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import com.mowmaster.pedestals.Recipes.FluidConverterRecipe;
import com.mowmaster.pedestals.Recipes.UnBottlerRecipe;
import mezz.jei.api.recipe.RecipeType;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class JEIPedestalsRecipeTypes
{
    public static final RecipeType<CobbleGenRecipe> COBBLE_GEN_RECIPE =
            RecipeType.create(MODID, "cobble_gen", CobbleGenRecipe.class);

    public static final RecipeType<BottlerRecipe> BOTTLER_RECIPE =
            RecipeType.create(MODID, "bottler", BottlerRecipe.class);

    public static final RecipeType<UnBottlerRecipe> UNBOTTLER_RECIPE =
            RecipeType.create(MODID, "unbottler", UnBottlerRecipe.class);

    public static final RecipeType<FluidConverterRecipe> FLUIDCONVERTER_RECIPE =
            RecipeType.create(MODID, "fluidconverter", FluidConverterRecipe.class);







}
