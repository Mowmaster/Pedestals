package com.mowmaster.pedestals.util.compat.jei.cobblegensilk;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeCobbleGen;
import com.mowmaster.pedestals.recipes.CobbleGenRecipe;
import com.mowmaster.pedestals.recipes.CobbleGenSilkRecipe;
import com.mowmaster.pedestals.references.Reference;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;


public class CobbleGenSilkRecipeCategory implements IRecipeCategory<CobbleGenSilkRecipe>
{
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    public static final ResourceLocation COBBLEGENSILK_TEXTURE = new ResourceLocation(Reference.MODID + ":textures/gui/jei/cobblegen.png");
    public static final ResourceLocation UID = new ResourceLocation(Reference.MODID, "cobblegensilk");

    public CobbleGenSilkRecipeCategory(IGuiHelper guiHelper) {
        ItemStack getStack = new ItemStack(ItemUpgradeCobbleGen.COBBLE.getItem());
        getStack.addEnchantment(Enchantments.SILK_TOUCH,1);
        icon = guiHelper.createDrawableIngredient(getStack);
        background = guiHelper.createDrawable(COBBLEGENSILK_TEXTURE, 0, 0, 71, 84);
        localizedName = I18n.format(Reference.MODID + ".recipe_category_cobblegensilk");
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Reference.MODID, "cobblegensilk");
    }

    @Override
    public Class<? extends CobbleGenSilkRecipe> getRecipeClass() {
        return CobbleGenSilkRecipe.class;
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
    public void setIngredients(CobbleGenSilkRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getIngredient()));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResult());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CobbleGenSilkRecipe recipe, IIngredients ingredients) {
        ItemStack getStack = new ItemStack(ItemUpgradeCobbleGen.COBBLE.getItem());
        getStack.addEnchantment(Enchantments.SILK_TOUCH,1);
        recipeLayout.getItemStacks().init(0, true, 6, 35);
        recipeLayout.getItemStacks().set(0,getStack);
        recipeLayout.getItemStacks().init(1, true, 47, 35);
        recipeLayout.getItemStacks().set(1,new ItemStack(PedestalBlock.I_PEDESTAL_333));
        recipeLayout.getItemStacks().init(2, true, 47, 59);
        recipeLayout.getItemStacks().set(2, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        recipeLayout.getItemStacks().init(3, false, 47, 6);
        recipeLayout.getItemStacks().set(3, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(CobbleGenSilkRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        //icon.draw(matrixStack, 30, 0);
    }
}
