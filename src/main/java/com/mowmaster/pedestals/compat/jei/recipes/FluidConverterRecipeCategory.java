package com.mowmaster.pedestals.compat.jei.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.pedestals.compat.jei.JEIPedestalsRecipeTypes;
import com.mowmaster.pedestals.pedestalutils.References;
import com.mowmaster.pedestals.recipes.FluidConverterRecipe;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import com.mowmaster.pedestals.registry.DeferredRegisterTileBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FluidConverterRecipeCategory implements IRecipeCategory<FluidConverterRecipe>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;

    public FluidConverterRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(References.MODID, "textures/gui/jei/fluidconverter.png"), 0, 0, 128, 96);
        this.localizedName = Component.translatable(References.MODID + ".jei.fluidconverter");
        //this.overlay =
        ItemStack renderStack = new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_FLUIDCONVERTER.get());
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public @NotNull RecipeType<FluidConverterRecipe> getRecipeType() {
        return JEIPedestalsRecipeTypes.FLUIDCONVERTER_RECIPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return this.localizedName;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FluidConverterRecipe recipe, @NotNull IFocusGroup focuses) {

        //Fluid
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 19)
                .setFluidRenderer(10,false,16,16).addIngredient(ForgeTypes.FLUID_STACK,recipe.getFluidRequired());

        //pedestal
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 53, 19)
                .addItemStack(new ItemStack(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get()).setHoverName(Component.translatable(References.MODID + ".fluidconverter.returnedstack")));

        //Result
        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 19)
                .addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(FluidConverterRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull PoseStack stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        Font fontRenderer = Minecraft.getInstance().font;

        MutableComponent energy = Component.translatable(References.MODID + ".fluidconverter.energy");
        energy.append(Component.literal(String.valueOf(recipe.getEnergyReturned())));
        energy.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,energy,10,44,0xffffffff);

        MutableComponent exp = Component.translatable(References.MODID + ".fluidconverter.xp");
        exp.append(Component.literal(String.valueOf(recipe.getExperienceReturned())));
        exp.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,exp,10,62,0xffffffff);

        DustMagic dustNeeded = recipe.getDustReturned();
        MutableComponent dust = Component.translatable(References.MODID + ".fluidconverter.dust");
        if(dustNeeded.getDustColor() > 0)
        {
            dust = Component.translatable(MowLibReferences.MODID + "." + MowLibColorReference.getColorName(dustNeeded.getDustColor()));
            dust.append(Component.literal(": " + dustNeeded.getDustAmount()));
        }
        else
        {
            dust.append(Component.literal(String.valueOf(dustNeeded.getDustAmount())));
        }
        dust.withStyle(ChatFormatting.BLACK);

        fontRenderer.draw(stack,dust,10,80,0xffffffff);
    }
}
