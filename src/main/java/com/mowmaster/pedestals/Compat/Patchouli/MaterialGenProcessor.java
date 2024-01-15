package com.mowmaster.pedestals.Compat.Patchouli;

import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;


public class MaterialGenProcessor implements IComponentProcessor {

    CobbleGenRecipe recipe;

    @Override
    public void setup(Level level, IVariableProvider variables) {
        RecipeManager manager = level.getRecipeManager();
        String recipeID = variables.get("recipe").asString();
        try {
            recipe = (CobbleGenRecipe) manager.byKey(new ResourceLocation(recipeID)).orElse(null);
        } catch (Exception ignored) {
        }
    }

    @Override
    public IVariable process(Level level, String key) {
        if (recipe == null)
            return null;
        if (key.equals("recipe")) {
            return IVariable.wrap(recipe.getId().toString());
        }
        if (key.equals("block_below"))
            return IVariable.from(recipe.getIngredients().get(0));
        if (key.equals("input_fluid_name"))
            return IVariable.wrap(recipe.getResultFluidNeeded().getDisplayName().getString());
        if (key.equals("input_fluid_amount"))
            return IVariable.wrap(recipe.getResultFluidNeeded().getAmount());
        if (key.equals("input_energy"))
            return IVariable.wrap(recipe.getResultEnergyNeeded());
        if (key.equals("input_exp"))
            return IVariable.wrap(recipe.getResultExperienceNeeded());
        if (key.equals("input_dust_color"))
            return IVariable.wrap(recipe.getResultDustNeeded().getDustColor());
        if (key.equals("input_dust_amount"))
            return IVariable.wrap(recipe.getResultDustNeeded().getDustAmount());
        if (key.equals("result_block")) {
            return IVariable.from(recipe.getResultItem());
        }
        if (key.equals("result_description")) {
            return IVariable.wrap(recipe.getResultItem().getDescriptionId());
        }

        return null;
    }
}