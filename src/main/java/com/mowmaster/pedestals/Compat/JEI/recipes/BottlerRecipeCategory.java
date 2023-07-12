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
import net.minecraft.client.gui.GuiGraphics;
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
                new ResourceLocation(References.MODID, (References.isDustLoaded())?("textures/gui/jei/bottler.png"):("textures/gui/jei/bottler_nodust.png")), 0, 0, 128, 120);
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
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 93)
                .addIngredients(recipe.getIngredients().get(0));

        //Fluid Input
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 69)
                .setFluidRenderer(10,false,16,16).addIngredient(ForgeTypes.FLUID_STACK,recipe.getFluidNeeded());

        //Pedestal
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 56, 69)
                .addItemStack(new ItemStack(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get()).setHoverName(Component.translatable(References.MODID + ".bottler.placementpedestal")));

        //Chest
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 56, 93)
                .addItemStack(new ItemStack(Items.CHEST).setHoverName(Component.translatable(References.MODID + ".bottler.placementinventory")));

        //Result
        builder.addSlot(RecipeIngredientRole.OUTPUT, 102, 69)
                .addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(BottlerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        Font fontRenderer = Minecraft.getInstance().font;

        MutableComponent energy = Component.translatable(References.MODID + ".bottler.energy");
        energy.append(Component.literal("" + recipe.getEnergyNeeded() + ""));
        energy.withStyle(ChatFormatting.WHITE);
        guiGraphics.drawString(fontRenderer,energy,10,10,0xffffffff);

        MutableComponent exp = Component.translatable(References.MODID + ".bottler.xp");
        exp.append(Component.literal("" + recipe.getExperienceNeeded() + ""));
        exp.withStyle(ChatFormatting.WHITE);
        guiGraphics.drawString(fontRenderer,exp,10,28,0xffffffff);

        if(References.isDustLoaded())
        {
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
            dust.withStyle(ChatFormatting.WHITE);

            guiGraphics.drawString(fontRenderer,dust,10,46,0xffffffff);
        }
    }
}
