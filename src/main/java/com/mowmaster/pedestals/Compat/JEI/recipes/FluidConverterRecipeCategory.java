package com.mowmaster.pedestals.Compat.JEI.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.pedestals.Compat.JEI.JEIPedestalsRecipeTypes;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Recipes.FluidConverterRecipe;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import com.mowmaster.pedestals.Registry.DeferredRegisterTileBlocks;
import mezz.jei.api.constants.VanillaTypes;
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

public class FluidConverterRecipeCategory implements IRecipeCategory<FluidConverterRecipe>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;
    private final ItemStack renderStack = new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_FLUIDCONVERTER.get());

    public FluidConverterRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(References.MODID, "textures/gui/jei/fluidconverter.png"), 0, 0, 128, 146);
        this.localizedName = Component.translatable(References.MODID + ".jei.fluidconverter");
        //this.overlay =
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public RecipeType<FluidConverterRecipe> getRecipeType() {
        return JEIPedestalsRecipeTypes.FLUIDCONVERTER_RECIPE;
    }

    @Override
    public Component getTitle() {
        return this.localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FluidConverterRecipe recipe, IFocusGroup focuses) {

        //Pedestal
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 69)
                .addItemStack(new ItemStack(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get()).setHoverName(Component.translatable(References.MODID + ".fluidconverter.returnedstack")));

        //Result
        builder.addSlot(RecipeIngredientRole.OUTPUT, 53, 69)
                .addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(FluidConverterRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        Font fontRenderer = Minecraft.getInstance().font;

        MutableComponent fluidneeded = Component.translatable(References.MODID + ".fluidconverter.fluid");
        fluidneeded.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,fluidneeded,10,14,0xffffffff);

        MutableComponent fluid = recipe.getFluidRequired().getDisplayName().copy();
        if(recipe.getFluidRequired().getAmount()<=0){fluid = Component.translatable(References.MODID + ".fluidconverter.fluidna");}
        else{fluid.append(Component.literal(": " + recipe.getFluidRequired().getAmount() + ""));}
        fluid.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,fluid,10,24,0xffffffff);

        MutableComponent energy = Component.translatable(References.MODID + ".fluidconverter.energy");
        energy.append(Component.literal("" + recipe.getEnergyReturned() + ""));
        energy.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,energy,10,94,0xffffffff);

        MutableComponent exp = Component.translatable(References.MODID + ".fluidconverter.xp");
        exp.append(Component.literal("" + recipe.getExperienceReturned() + ""));
        exp.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,exp,10,112,0xffffffff);

        DustMagic dustNeeded = recipe.getDustReturned();
        MutableComponent dust = Component.translatable(References.MODID + ".fluidconverter.dust");
        if(dustNeeded.getDustColor() > 0)
        {
            dust = Component.translatable(MowLibReferences.MODID + "." + MowLibColorReference.getColorName(dustNeeded.getDustColor()));
            dust.append(Component.literal(": " + dustNeeded.getDustAmount() + ""));
        }
        else
        {
            dust.append(Component.literal(dustNeeded.getDustAmount() + ""));
        }
        dust.withStyle(ChatFormatting.BLACK);

        fontRenderer.draw(stack,dust,10,130,0xffffffff);
    }
}
