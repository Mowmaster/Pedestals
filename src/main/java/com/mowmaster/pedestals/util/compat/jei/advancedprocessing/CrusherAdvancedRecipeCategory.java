package com.mowmaster.pedestals.util.compat.jei.advancedprocessing;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeCobbleGen;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeCrusher;
import com.mowmaster.pedestals.recipes.CrusherRecipe;
import com.mowmaster.pedestals.recipes.CrusherRecipeAdvanced;
import com.mowmaster.pedestals.references.Reference;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;


public class CrusherAdvancedRecipeCategory implements IRecipeCategory<CrusherRecipeAdvanced>
{
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    public static final ResourceLocation CRUSHER_TEXTURE = new ResourceLocation(Reference.MODID + ":textures/gui/jei/crusherprocessing.png");
    public static final ResourceLocation UID = new ResourceLocation(Reference.MODID, "crushing_advanced");

    public CrusherAdvancedRecipeCategory(IGuiHelper guiHelper) {
        ItemStack getStack = new ItemStack(ItemUpgradeCrusher.CRUSHER.getItem());
        getStack.addEnchantment(EnchantmentRegistry.ADVANCED,1);
        icon = guiHelper.createDrawableIngredient(getStack);
        background = guiHelper.createDrawable(CRUSHER_TEXTURE, 0, 0, 73, 35);
        localizedName = I18n.format(Reference.MODID + ".recipe_category_crusher_advanced");
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Reference.MODID, "crushing_advanced");
    }

    @Override
    public Class<? extends CrusherRecipeAdvanced> getRecipeClass() {
        return CrusherRecipeAdvanced.class;
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
    public void setIngredients(CrusherRecipeAdvanced recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getIngredient()));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResult());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CrusherRecipeAdvanced recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 5, 14);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        recipeLayout.getItemStacks().init(1, false, 50, 14);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(CrusherRecipeAdvanced recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        //icon.draw(matrixStack, 30, 0);
    }
}
