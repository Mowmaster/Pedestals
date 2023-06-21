package com.mowmaster.pedestals.Compat.JEI.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.pedestals.Compat.JEI.JEIPedestalsRecipeTypes;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Recipes.UnBottlerRecipe;
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

public class UnBottlerRecipeCategory implements IRecipeCategory<UnBottlerRecipe>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;
    private final ItemStack renderStack = new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_UNBOTTLER.get());

    public UnBottlerRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(References.MODID, (References.isDustLoaded())?("textures/gui/jei/unbottler.png"):("textures/gui/jei/unbottler_nodust.png")), 0, 0, 128, 120);
        this.localizedName = Component.translatable(References.MODID + ".jei.unbottler");
        //this.overlay =
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public RecipeType<UnBottlerRecipe> getRecipeType() {
        return JEIPedestalsRecipeTypes.UNBOTTLER_RECIPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, UnBottlerRecipe recipe, IFocusGroup focuses) {

        //Input
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 35)
                .addIngredients(recipe.getIngredients().get(0));

        //Pedestal
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 56, 11)
                .addItemStack(new ItemStack(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get()).setHoverName(Component.translatable(References.MODID + ".bottler.placementpedestal")));

        //Chest
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 56, 35)
                .addItemStack(new ItemStack(Items.CHEST).setHoverName(Component.translatable(References.MODID + ".bottler.placementinventory")));

        //Fluid Input
        builder.addSlot(RecipeIngredientRole.OUTPUT, 102, 35)
                .setFluidRenderer(10,false,16,16).addIngredient(ForgeTypes.FLUID_STACK,recipe.getFluidReturned());

        //Result
        builder.addSlot(RecipeIngredientRole.OUTPUT, 102, 11)
                .addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(UnBottlerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        Font fontRenderer = Minecraft.getInstance().font;

        MutableComponent energy = Component.translatable(References.MODID + ".unbottler.energy");
        energy.append(Component.literal("" + recipe.getEnergyReturned() + ""));
        energy.withStyle(ChatFormatting.BLACK);
        guiGraphics.drawString(fontRenderer,energy,10,64,0xffffffff);

        MutableComponent exp = Component.translatable(References.MODID + ".unbottler.xp");
        exp.append(Component.literal("" + recipe.getExperienceReturned() + ""));
        exp.withStyle(ChatFormatting.BLACK);
        guiGraphics.drawString(fontRenderer,exp,10,82,0xffffffff);

        if(References.isDustLoaded())
        {
            DustMagic dustNeeded = recipe.getDustReturned();
            MutableComponent dust = Component.translatable(References.MODID + ".unbottler.dust");
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

            guiGraphics.drawString(fontRenderer,dust,10,100,0xffffffff);
        }
    }
}
