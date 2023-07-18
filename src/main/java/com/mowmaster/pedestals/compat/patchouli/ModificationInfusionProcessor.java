package com.mowmaster.pedestals.compat.patchouli;

import com.mowmaster.pedestals.recipes.UpgradeModificationGlobalRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class ModificationInfusionProcessor implements IComponentProcessor {

    UpgradeModificationGlobalRecipe recipe;

    @Override
    public void setup(IVariableProvider variables) {
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        String recipeID = variables.get("recipe").asString();
        try {
            recipe = (UpgradeModificationGlobalRecipe) manager.byKey(new ResourceLocation(recipeID)).orElse(null);
        } catch (Exception ignored) {
        }
    }

    @Override
    public IVariable process(@NotNull String key) {
        if (recipe == null)
            return null;
        if (key.equals("recipe")) {
            return IVariable.wrap(recipe.getId().toString());
        }
        if (key.equals("input_upgrade") && recipe.getIngredients().size()>0)
            return IVariable.from(recipe.getIngredients().get(0));
        if (key.equals("input_1") && recipe.getIngredients().size()>1)
            return IVariable.from(recipe.getIngredients().get(1));
        if (key.equals("input_2") && recipe.getIngredients().size()>2)
            return IVariable.from(recipe.getIngredients().get(2));
        if (key.equals("input_3") && recipe.getIngredients().size()>3)
            return IVariable.from(recipe.getIngredients().get(3));
        if (key.equals("input_4") && recipe.getIngredients().size()>4)
            return IVariable.from(recipe.getIngredients().get(4));
        if (key.equals("input_5") && recipe.getIngredients().size()>5)
            return IVariable.from(recipe.getIngredients().get(5));
        if (key.equals("input_6") && recipe.getIngredients().size()>6)
            return IVariable.from(recipe.getIngredients().get(6));
        if (key.equals("input_7") && recipe.getIngredients().size()>7)
            return IVariable.from(recipe.getIngredients().get(7));
        if (key.equals("input_8") && recipe.getIngredients().size()>8)
            return IVariable.from(recipe.getIngredients().get(8));
        if (key.equals("input_9") && recipe.getIngredients().size()>9)
            return IVariable.from(recipe.getIngredients().get(9));

        if (key.equals("result_modification"))
            return IVariable.wrap(recipe.getResultModificationName());
        if (key.equals("modification_min"))
            return IVariable.wrap(recipe.getResultModificationAmount());
        if (key.equals("modification_max"))
            return IVariable.wrap(recipe.getResultModificationMaxAmount());

        return null;
    }
}
