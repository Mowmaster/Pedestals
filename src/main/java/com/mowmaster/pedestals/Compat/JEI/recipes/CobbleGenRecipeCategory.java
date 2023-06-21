package com.mowmaster.pedestals.Compat.JEI.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.pedestals.Compat.JEI.JEIPedestalsRecipeTypes;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
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
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CobbleGenRecipeCategory implements IRecipeCategory<CobbleGenRecipe>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;
    private final ItemStack renderStack = new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_COBBLEGEN.get());

    public CobbleGenRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(References.MODID, (References.isDustLoaded())?("textures/gui/jei/cobble_gen.png"):("textures/gui/jei/cobble_gen_nodust.png")), 0, 0, 128, 122);
        this.localizedName = Component.translatable(References.MODID + ".jei.cobble_gen");
        //this.overlay =
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public RecipeType<CobbleGenRecipe> getRecipeType() {
        return JEIPedestalsRecipeTypes.COBBLE_GEN_RECIPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, CobbleGenRecipe recipe, IFocusGroup focuses) {

        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 56,17)
                .addItemStack(new ItemStack(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get()).setHoverName(Component.translatable(References.MODID + ".cobble_gen.warning")));
        //Fluid Input
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 17)
                .setFluidRenderer(10,false,16,16).addIngredient(ForgeTypes.FLUID_STACK,recipe.getResultFluidNeeded());

        //Block Below
        builder.addSlot(RecipeIngredientRole.INPUT, 56, 41)
                .addIngredients(recipe.getIngredients().get(0));

        //Result
        builder.addSlot(RecipeIngredientRole.OUTPUT, 102, 17)
                .addItemStack(recipe.getResultItem());

        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 102,41)
                .addItemStack(new ItemStack(Items.STONE_PICKAXE).setHoverName(Component.translatable(References.MODID + ".cobble_gen.tool")));
    }

    @Override
    public void draw(CobbleGenRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        Font fontRenderer = Minecraft.getInstance().font;

        MutableComponent energy = Component.translatable(References.MODID + ".cobble_gen.energy");
        energy.append(Component.literal("" + recipe.getResultEnergyNeeded() + ""));
        energy.withStyle(ChatFormatting.BLACK);
        guiGraphics.drawString(fontRenderer,energy,10,66,0xffffffff);

        MutableComponent exp = Component.translatable(References.MODID + ".cobble_gen.xp");
        exp.append(Component.literal("" + recipe.getResultExperienceNeeded() + ""));
        exp.withStyle(ChatFormatting.BLACK);
        guiGraphics.drawString(fontRenderer,exp,10,84,0xffffffff);

        if(References.isDustLoaded())
        {
            DustMagic dustNeeded = recipe.getResultDustNeeded();
            MutableComponent dust = Component.translatable(References.MODID + ".cobble_gen.dust");
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

            guiGraphics.drawString(fontRenderer,dust,10,102,0xffffffff);
        }
    }
}
