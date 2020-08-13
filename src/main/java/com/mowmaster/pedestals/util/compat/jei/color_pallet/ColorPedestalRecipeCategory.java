package com.mowmaster.pedestals.util.compat.jei.color_pallet;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.item.ItemColorPallet;
import com.mowmaster.pedestals.recipes.ColoredPedestalRecipe;
import com.mowmaster.pedestals.references.Reference;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;


public class ColorPedestalRecipeCategory implements IRecipeCategory<ColoredPedestalRecipe>
{
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    public static final ResourceLocation PEDESTAL_TEXTURE = new ResourceLocation(Reference.MODID + ":textures/gui/jei/coloredpedestal.png");
    public static final ResourceLocation UID = new ResourceLocation(Reference.MODID, "coloredpedestals");

    public ColorPedestalRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(new ItemStack(ItemColorPallet.COLORPALLET_DEFAULT.getItem()));
        background = guiHelper.createDrawable(PEDESTAL_TEXTURE, 0, 0, 118, 35);
        localizedName = I18n.format(Reference.MODID + ".recipe_category_coloredpedestals");
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Reference.MODID, "coloredpedestals");
    }

    @Override
    public Class<? extends ColoredPedestalRecipe> getRecipeClass() {
        return ColoredPedestalRecipe.class;
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
    public void setIngredients(ColoredPedestalRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getIngredient()));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResult());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ColoredPedestalRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 50, 14);
        recipeLayout.getItemStacks().set(0,new ItemStack(BlockPedestalTE.I_PEDESTAL_333));

        recipeLayout.getItemStacks().init(1, true, 5, 14);
        int color = recipe.getColor();
        ItemStack pallet = new ItemStack(ItemColorPallet.COLORPALLET);
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("color",color);
        pallet.setTag(nbt);
        recipeLayout.getItemStacks().set(1,pallet);

        recipeLayout.getItemStacks().init(2, false, 95, 14);
        recipeLayout.getItemStacks().set(2, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(ColoredPedestalRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        background.draw(matrixStack);
    }
}
