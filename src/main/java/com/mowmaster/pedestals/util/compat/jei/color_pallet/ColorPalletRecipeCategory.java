package com.mowmaster.pedestals.util.compat.jei.color_pallet;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.crafting.CalculateColor;
import com.mowmaster.pedestals.item.ItemColorPallet;
import com.mowmaster.pedestals.item.ItemLinkingTool;
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
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ColorPalletRecipeCategory implements IRecipeCategory<ColoredPedestalRecipe>
{
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    public static final ResourceLocation PALLET_TEXTURE = new ResourceLocation(Reference.MODID + ":textures/gui/jei/coloredpallet.png");
    public static final ResourceLocation UID = new ResourceLocation(Reference.MODID, "coloredpallets");

    public ColorPalletRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(new ItemStack(ItemLinkingTool.DEFAULT.getItem()));
        background = guiHelper.createDrawable(PALLET_TEXTURE, 0, 0, 119, 78);
        localizedName = I18n.format(Reference.MODID + ".recipe_category_coloredpallets");
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Reference.MODID, "coloredpallets");
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
        /*int color = recipe.getColor();
        ItemStack pallet = new ItemStack(ItemColorPallet.COLORPALLET);
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("color",color);
        pallet.setTag(nbt);
        ingredients.setOutput(VanillaTypes.ITEM, pallet);*/
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResult());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ColoredPedestalRecipe recipe, IIngredients ingredients) {


        int r = (CalculateColor.getRGBColorFromInt(recipe.getColor())[0]+1)/64;
        int g = (CalculateColor.getRGBColorFromInt(recipe.getColor())[1]+1)/64;
        int b = (CalculateColor.getRGBColorFromInt(recipe.getColor())[2]+1)/64;
        ItemStack rDye = (r>0)?((r==4)?(new ItemStack(Items.RED_DYE,r-1)):(new ItemStack(Items.RED_DYE,r))):(ItemStack.EMPTY);
        ItemStack gDye = (g>0)?((g==4)?(new ItemStack(Items.GREEN_DYE,g-1)):(new ItemStack(Items.GREEN_DYE,g))):(ItemStack.EMPTY);
        ItemStack bDye = (b>0)?((b==4)?(new ItemStack(Items.BLUE_DYE,b-1)):(new ItemStack(Items.BLUE_DYE,b))):(ItemStack.EMPTY);

        recipeLayout.getItemStacks().init(0, true, 9, 9);
        if(rDye.getCount()>0)
        {
            recipeLayout.getItemStacks().set(0,rDye);
        }


        recipeLayout.getItemStacks().init(1, true, 9, 30);
        if(gDye.getCount()>0)
        {
            recipeLayout.getItemStacks().set(1,gDye);
        }


        recipeLayout.getItemStacks().init(2, true, 9, 51);
        if(bDye.getCount()>0)
        {
            recipeLayout.getItemStacks().set(2,bDye);
        }

        recipeLayout.getItemStacks().init(3, true, 46, 30);
        ItemStack palletDefault = new ItemStack(ItemColorPallet.COLORPALLET_DEFAULT);
        recipeLayout.getItemStacks().set(3,palletDefault);


        recipeLayout.getItemStacks().init(4, true, 92, 30);
        int color = recipe.getColor();
        ItemStack pallet = new ItemStack(ItemColorPallet.COLORPALLET);
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("color",color);
        pallet.setTag(nbt);
        recipeLayout.getItemStacks().set(4,pallet);

        /*recipeLayout.getItemStacks().init(5, false, 93, 49);
        recipeLayout.getItemStacks().set(5, ingredients.getOutputs(VanillaTypes.ITEM).get(0));*/
    }

    @Override
    public void draw(ColoredPedestalRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        icon.draw(matrixStack, 69, 8);
    }
}
