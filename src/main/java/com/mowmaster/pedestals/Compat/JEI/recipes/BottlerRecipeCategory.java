package com.mowmaster.pedestals.Compat.JEI.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.pedestals.Compat.JEI.JEIPedestalsRecipeTypes;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Recipes.BottlerRecipe;
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
import net.minecraft.world.item.Items;

public class BottlerRecipeCategory implements IRecipeCategory<BottlerRecipe>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;
    private final ItemStack renderStack = new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_BOTTLER.get());

    public BottlerRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(References.MODID, "textures/gui/jei/bottler.png"), 0, 0, 128, 146);
        this.localizedName = Component.translatable(References.MODID + ".jei.bottler");
        //this.overlay =
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public RecipeType<BottlerRecipe> getRecipeType() {
        return JEIPedestalsRecipeTypes.BOTTLER_RECIPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, BottlerRecipe recipe, IFocusGroup focuses) {

        //Input
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 41)
                .addIngredients(recipe.getIngredients().get(0));

        //Pedestal
        builder.addSlot(RecipeIngredientRole.INPUT, 56, 17)
                .addItemStack(new ItemStack(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get()).setHoverName(Component.translatable(References.MODID + ".bottler.placementpedestal")));

        //Chest
        builder.addSlot(RecipeIngredientRole.INPUT, 56, 41)
                .addItemStack(new ItemStack(Items.CHEST).setHoverName(Component.translatable(References.MODID + ".bottler.placementinventory")));

        //Result
        builder.addSlot(RecipeIngredientRole.OUTPUT, 102, 17)
                .addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(BottlerRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        Font fontRenderer = Minecraft.getInstance().font;

        MutableComponent fluidneeded = Component.translatable(References.MODID + ".bottler.fluid");
        fluidneeded.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,fluidneeded,10,66,0xffffffff);

        MutableComponent fluid = recipe.getFluidNeeded().getDisplayName().copy();
        if(recipe.getFluidNeeded().getAmount()<=0){fluid = Component.translatable(References.MODID + ".bottler.fluidna");}
        else{fluid.append(Component.literal(": " + recipe.getFluidNeeded().getAmount() + ""));}
        fluid.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,fluid,10,76,0xffffffff);

        MutableComponent energy = Component.translatable(References.MODID + ".bottler.energy");
        energy.append(Component.literal("" + recipe.getEnergyNeeded() + ""));
        energy.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,energy,10,94,0xffffffff);

        MutableComponent exp = Component.translatable(References.MODID + ".bottler.xp");
        exp.append(Component.literal("" + recipe.getExperienceNeeded() + ""));
        exp.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,exp,10,112,0xffffffff);

        DustMagic dustNeeded = recipe.getDustNeeded();
        MutableComponent dust = Component.translatable(References.MODID + ".bottler.dust");
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
