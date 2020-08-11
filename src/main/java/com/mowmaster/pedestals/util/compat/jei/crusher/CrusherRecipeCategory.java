package com.mowmaster.pedestals.util.compat.jei.crusher;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeCrusher;
import com.mowmaster.pedestals.recipes.CrusherRecipe;
import com.mowmaster.pedestals.references.Reference;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import java.util.Collections;


public class CrusherRecipeCategory implements IRecipeCategory<CrusherRecipe>
{
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    public static final ResourceLocation CRUSHER_TEXTURE = new ResourceLocation(Reference.MODID + ":textures/gui/jei/crusherprocessing.png");
    public static final ResourceLocation UID = new ResourceLocation(Reference.MODID, "crushing");

    public CrusherRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(new ItemStack(ItemUpgradeCrusher.CRUSHER.getItem()));
        background = guiHelper.createDrawable(CRUSHER_TEXTURE, 0, 0, 73, 35);
        localizedName = I18n.format(Reference.MODID + ".recipe_category_crusher");
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Reference.MODID, "crushing");
    }

    @Override
    public Class<? extends CrusherRecipe> getRecipeClass() {
        return CrusherRecipe.class;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(CrusherRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getIngredient()));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResult());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CrusherRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 5, 14);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        recipeLayout.getItemStacks().init(1, false, 50, 14);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(CrusherRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        icon.draw(matrixStack, 30, 0);
    }
}
