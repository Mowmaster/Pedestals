package com.mowmaster.pedestals.compat.jei;

import com.mowmaster.pedestals.recipes.*;
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

    public static final RecipeType<UpgradeModificationRecipe> UPGRADEMODIFICATION_RECIPE =
            RecipeType.create(MODID, "upgrademodification", UpgradeModificationRecipe.class);

    public static final RecipeType<UpgradeModificationGlobalRecipe> UPGRADEMODIFICATIONGLOBAL_RECIPE =
            RecipeType.create(MODID, "upgrademodification_global", UpgradeModificationGlobalRecipe.class);


}
