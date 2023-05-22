package com.mowmaster.pedestals.Compat.Patchouli;

import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import java.util.Arrays;

public class MaterialGenProcessor implements IComponentProcessor {

    CobbleGenRecipe recipe;

    @Override
    public void setup(IVariableProvider variables) {
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        String recipeID = variables.get("recipe").asString();
        try {
            recipe = (CobbleGenRecipe) manager.byKey(new ResourceLocation(recipeID)).orElse(null);
        } catch (Exception ignored) {
        }
    }

    @Override
    public IVariable process(String key) {
        if (recipe == null)
            return null;
        if (key.equals("recipe")) {
            return IVariable.wrap(recipe.getId().toString());
        }
        if (key.equals("block_below"))
            return IVariable.from(recipe.getIngredients().get(0));
        if (key.equals("input_fluid"))
            return IVariable.from(recipe.getResultFluidNeeded());
        if (key.equals("input_energy"))
            return IVariable.wrap(recipe.getResultEnergyNeeded());
        if (key.equals("input_exp"))
            return IVariable.wrap(recipe.getResultExperienceNeeded());
        /*if (key.equals("input_dust"))
            return IVariable.from(recipe.getResultDustNeeded());*/
        if (key.equals("result_block")) {
            return IVariable.from(recipe.getResultItem());
        }
        if (key.equals("result_description")) {
            return IVariable.wrap(recipe.getResultItem().getDescriptionId());
        }

        return null;
    }
}
