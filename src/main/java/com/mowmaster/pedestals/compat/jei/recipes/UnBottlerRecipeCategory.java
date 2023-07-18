package com.mowmaster.pedestals.compat.jei.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.pedestals.compat.jei.JEIPedestalsRecipeTypes;
import com.mowmaster.pedestals.pedestalutils.References;
import com.mowmaster.pedestals.recipes.UnBottlerRecipe;
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
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class UnBottlerRecipeCategory implements IRecipeCategory<UnBottlerRecipe>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;

    public UnBottlerRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(References.MODID, "textures/gui/jei/unbottler.png"), 0, 0, 128, 120);
        this.localizedName = Component.translatable(References.MODID + ".jei.unbottler");
        //this.overlay =
        ItemStack renderStack = new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_UNBOTTLER.get());
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public @NotNull RecipeType<UnBottlerRecipe> getRecipeType() {
        return JEIPedestalsRecipeTypes.UNBOTTLER_RECIPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, UnBottlerRecipe recipe, @NotNull IFocusGroup focuses) {

        //Input
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 35)
                .addIngredients(recipe.getIngredients().get(0));

        //pedestal
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
    public void draw(UnBottlerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull PoseStack stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        Font fontRenderer = Minecraft.getInstance().font;

        MutableComponent energy = Component.translatable(References.MODID + ".unbottler.energy");
        energy.append(Component.literal(String.valueOf(recipe.getEnergyReturned())));
        energy.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,energy,10,64,0xffffffff);

        MutableComponent exp = Component.translatable(References.MODID + ".unbottler.xp");
        exp.append(Component.literal(String.valueOf(recipe.getExperienceReturned())));
        exp.withStyle(ChatFormatting.BLACK);
        fontRenderer.draw(stack,exp,10,82,0xffffffff);

        DustMagic dustNeeded = recipe.getDustReturned();
        MutableComponent dust = Component.translatable(References.MODID + ".unbottler.dust");
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

        fontRenderer.draw(stack,dust,10,100,0xffffffff);
    }
}
