package com.mowmaster.pedestals.compat.jei.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.pedestals.compat.jei.JEIPedestalsRecipeTypes;
import com.mowmaster.pedestals.pedestalutils.References;
import com.mowmaster.pedestals.recipes.UpgradeModificationGlobalRecipe;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import com.mowmaster.pedestals.registry.DeferredRegisterTileBlocks;
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
import org.jetbrains.annotations.NotNull;

public class UpgradeModifierCategory implements IRecipeCategory<UpgradeModificationGlobalRecipe>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;

    public UpgradeModifierCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(References.MODID, "textures/gui/jei/upgrade_modification.png"), 0, 0, 128, 128);
        this.localizedName = Component.translatable(References.MODID + ".jei.upgrade_modification");
        //this.overlay =
        ItemStack renderStack = new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_MODIFICATIONS.get());
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public @NotNull RecipeType<UpgradeModificationGlobalRecipe> getRecipeType() {
        return JEIPedestalsRecipeTypes.UPGRADEMODIFICATIONGLOBAL_RECIPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, UpgradeModificationGlobalRecipe recipe, @NotNull IFocusGroup focuses) {

        //pedestal
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 56,77)
                .addItemStack(new ItemStack(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get()).setHoverName(Component.translatable(References.MODID + ".upgrade_modification.pedestalsetup")));

        //Chest
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 56, 101)
                .addItemStack(new ItemStack(Items.CHEST).setHoverName(Component.translatable(References.MODID + ".upgrade_modification.placementinventory")));

        //InputSlot (upgrade)
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 101)
                .addIngredients(recipe.getIngredients().get(0));

        int invSize = recipe.getIngredients().size();

        //Input ingredients (1)
        if(invSize>=2)builder.addSlot(RecipeIngredientRole.INPUT, 36, 15).addIngredients(recipe.getIngredients().get(1));
        //Input ingredients (2)
        if(invSize>=3)builder.addSlot(RecipeIngredientRole.INPUT, 56, 15).addIngredients(recipe.getIngredients().get(2));
        //Input ingredients (3)
        if(invSize>=4)builder.addSlot(RecipeIngredientRole.INPUT, 76, 15).addIngredients(recipe.getIngredients().get(3));
        //Input ingredients (4)
        if(invSize>=5)builder.addSlot(RecipeIngredientRole.INPUT, 36, 35).addIngredients(recipe.getIngredients().get(4));
        //Input ingredients (5)
        if(invSize>=6)builder.addSlot(RecipeIngredientRole.INPUT, 56, 35).addIngredients(recipe.getIngredients().get(5));
        //Input ingredients (6)
        if(invSize>=7)builder.addSlot(RecipeIngredientRole.INPUT, 76, 35).addIngredients(recipe.getIngredients().get(6));
        //Input ingredients (7)
        if(invSize>=8)builder.addSlot(RecipeIngredientRole.INPUT, 36, 55).addIngredients(recipe.getIngredients().get(7));
        //Input ingredients (8)
        if(invSize>=9)builder.addSlot(RecipeIngredientRole.INPUT, 56, 55).addIngredients(recipe.getIngredients().get(8));
        //Input ingredients (9)
        if(invSize>=10)builder.addSlot(RecipeIngredientRole.INPUT, 76, 55).addIngredients(recipe.getIngredients().get(9));
    }

    @Override
    public void draw(UpgradeModificationGlobalRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull PoseStack stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        Font fontRenderer = Minecraft.getInstance().font;

        //Modification: Speed
        //MutableComponent modTypeLabel = Component.translatable(References.MODID + ".upgrade_modification.type");
        MutableComponent modType = Component.translatable(References.MODID + ".upgrade_modification." + recipe.getResultModificationName());
        //modTypeLabel.withStyle(ChatFormatting.BLACK);
        modType.withStyle(ChatFormatting.BLACK);
        //modTypeLabel.append(modType);
        int width1 = fontRenderer.width(modType.getString());
        fontRenderer.draw(stack,modType,64-Math.floorDiv(width1,2),4,0xffffffff);
        //fontRenderer.draw(stack,modType,34,4,0xffffffff);


        //+2 - Max 10

        //+
        MutableComponent separator1 = Component.translatable(References.MODID + ".text.separator.plus");
        //" "
        MutableComponent spaceText = Component.translatable(References.MODID + ".text.separator.space");
        //"Min: "
        MutableComponent minText = Component.translatable(References.MODID + ".text.separator.min");
        //"Max: "
        MutableComponent maxText = Component.translatable(References.MODID + ".text.separator.max");

        MutableComponent increaseAmount = Component.literal(String.valueOf(recipe.getResultModificationAmount()));
        MutableComponent minAmount = Component.literal(String.valueOf(recipe.getResultModificationMinAmount()));
        MutableComponent maxAmount = Component.literal(String.valueOf(recipe.getResultModificationMaxAmount()));


        separator1.append(increaseAmount);
        separator1.append(spaceText);
        separator1.append(minText);
        separator1.append(minAmount);
        separator1.append(spaceText);
        separator1.append(maxText);
        separator1.append(maxAmount);
        separator1.withStyle(ChatFormatting.BLACK);

        int width = fontRenderer.width(separator1.getString());
        fontRenderer.draw(stack,separator1,64-Math.floorDiv(width,2),73,0xffffffff);
    }
}
