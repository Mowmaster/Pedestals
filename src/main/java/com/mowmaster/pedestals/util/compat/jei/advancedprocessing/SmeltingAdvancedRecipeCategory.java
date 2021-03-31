package com.mowmaster.pedestals.util.compat.jei.advancedprocessing;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeCrusher;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeFurnace;
import com.mowmaster.pedestals.recipes.CrusherRecipeAdvanced;
import com.mowmaster.pedestals.recipes.SmeltingRecipeAdvanced;
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


public class SmeltingAdvancedRecipeCategory implements IRecipeCategory<SmeltingRecipeAdvanced>
{
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    public static final ResourceLocation CRUSHER_TEXTURE = new ResourceLocation(Reference.MODID + ":textures/gui/jei/crusherprocessing.png");
    public static final ResourceLocation UID = new ResourceLocation(Reference.MODID, "smelting_advanced");

    public SmeltingAdvancedRecipeCategory(IGuiHelper guiHelper) {
        ItemStack getStack = new ItemStack(ItemUpgradeFurnace.SMELTER.getItem());
        getStack.addEnchantment(EnchantmentRegistry.ADVANCED,1);
        icon = guiHelper.createDrawableIngredient(getStack);
        background = guiHelper.createDrawable(CRUSHER_TEXTURE, 0, 0, 73, 35);
        localizedName = I18n.format(Reference.MODID + ".recipe_category_smelting_advanced");
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Reference.MODID, "smelting_advanced");
    }

    @Override
    public Class<? extends SmeltingRecipeAdvanced> getRecipeClass() {
        return SmeltingRecipeAdvanced.class;
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
    public void setIngredients(SmeltingRecipeAdvanced recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getIngredient()));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResult());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, SmeltingRecipeAdvanced recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 5, 14);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        recipeLayout.getItemStacks().init(1, false, 50, 14);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(SmeltingRecipeAdvanced recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        //icon.draw(matrixStack, 30, 0);
    }
}
