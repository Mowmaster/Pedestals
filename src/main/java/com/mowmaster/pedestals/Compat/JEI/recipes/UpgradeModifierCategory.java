package com.mowmaster.pedestals.Compat.JEI.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.pedestals.Compat.JEI.JEIPedestalsRecipeTypes;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import com.mowmaster.pedestals.Recipes.UpgradeModificationGlobalRecipe;
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

public class UpgradeModifierCategory implements IRecipeCategory<UpgradeModificationGlobalRecipe>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;
    private final ItemStack renderStack = new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_MODIFICATIONS.get());

    public UpgradeModifierCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(References.MODID, "textures/gui/jei/upgrade_modification.png"), 0, 0, 128, 128);
        this.localizedName = Component.translatable(References.MODID + ".jei.upgrade_modification");
        //this.overlay =
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public RecipeType<UpgradeModificationGlobalRecipe> getRecipeType() {
        return JEIPedestalsRecipeTypes.UPGRADEMODIFICATIONGLOBAL_RECIPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, UpgradeModificationGlobalRecipe recipe, IFocusGroup focuses) {

        //Pedestal
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 56,77)
                .addItemStack(new ItemStack(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get()).setHoverName(Component.translatable(References.MODID + ".upgrade_modification.pedestalsetup")));

        //Chest
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 56, 101)
                .addItemStack(new ItemStack(Items.CHEST).setHoverName(Component.translatable(References.MODID + ".upgrade_modification.placementinventory")));

        //InputSlot (upgrade)
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 101)
                .addIngredients(recipe.getIngredients().get(0));

        int invSize = recipe.getIngredients().size();

        //Input Ingredients (1)
        if(invSize>=2)builder.addSlot(RecipeIngredientRole.INPUT, 36, 15).addIngredients(recipe.getIngredients().get(1));
        //Input Ingredients (2)
        if(invSize>=3)builder.addSlot(RecipeIngredientRole.INPUT, 56, 15).addIngredients(recipe.getIngredients().get(2));
        //Input Ingredients (3)
        if(invSize>=4)builder.addSlot(RecipeIngredientRole.INPUT, 76, 15).addIngredients(recipe.getIngredients().get(3));
        //Input Ingredients (4)
        if(invSize>=5)builder.addSlot(RecipeIngredientRole.INPUT, 36, 35).addIngredients(recipe.getIngredients().get(4));
        //Input Ingredients (5)
        if(invSize>=6)builder.addSlot(RecipeIngredientRole.INPUT, 56, 35).addIngredients(recipe.getIngredients().get(5));
        //Input Ingredients (6)
        if(invSize>=7)builder.addSlot(RecipeIngredientRole.INPUT, 76, 35).addIngredients(recipe.getIngredients().get(6));
        //Input Ingredients (7)
        if(invSize>=8)builder.addSlot(RecipeIngredientRole.INPUT, 36, 55).addIngredients(recipe.getIngredients().get(7));
        //Input Ingredients (8)
        if(invSize>=9)builder.addSlot(RecipeIngredientRole.INPUT, 56, 55).addIngredients(recipe.getIngredients().get(8));
        //Input Ingredients (9)
        if(invSize>=10)builder.addSlot(RecipeIngredientRole.INPUT, 76, 55).addIngredients(recipe.getIngredients().get(9));
    }

    @Override
    public void draw(UpgradeModificationGlobalRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        Font fontRenderer = Minecraft.getInstance().font;

        //Modification: Speed
        //MutableComponent modTypeLabel = Component.translatable(References.MODID + ".upgrade_modification.type");
        MutableComponent modType = Component.translatable(References.MODID + ".upgrade_modification." + recipe.getResultModificationName());
        //modTypeLabel.withStyle(ChatFormatting.BLACK);
        modType.withStyle(ChatFormatting.WHITE);
        //modTypeLabel.append(modType);
        int width1 = fontRenderer.width(modType.getString());
        guiGraphics.drawString(fontRenderer,modType,64-Math.floorDiv(width1,2),4,0xffffffff);
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

        MutableComponent increaseAmount = Component.literal(""+ recipe.getResultModificationAmount() +"");
        MutableComponent minAmount = Component.literal(""+ recipe.getResultModificationMinAmount() +"");
        MutableComponent maxAmount = Component.literal(""+ recipe.getResultModificationMaxAmount() +"");


        separator1.append(increaseAmount);
        separator1.append(spaceText);
        separator1.append(minText);
        separator1.append(minAmount);
        separator1.append(spaceText);
        separator1.append(maxText);
        separator1.append(maxAmount);
        separator1.withStyle(ChatFormatting.WHITE);

        int width = fontRenderer.width(separator1.getString());
        guiGraphics.drawString(fontRenderer,separator1,64-Math.floorDiv(width,2),73,0xffffffff);
    }
}
