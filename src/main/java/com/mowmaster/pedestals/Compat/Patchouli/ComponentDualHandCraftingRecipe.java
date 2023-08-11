package com.mowmaster.pedestals.Compat.Patchouli;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Recipes.InWorldDualHandedCrafting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.Ingredient;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

import java.util.function.UnaryOperator;
import java.util.*;

public class ComponentDualHandCraftingRecipe implements ICustomComponent {
    private transient int x, y;
    @SerializedName("recipe_name")
    public String recipeName;
    private transient InWorldDualHandedCrafting recipe;

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        x = componentX;
        y = componentY;
    }

    private void framedRenderIngredient(PoseStack ms, IComponentRenderContext context, int x, int y, int mouseX, int mouseY, Ingredient ingredient) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, context.getCraftingTexture());
        GuiComponent.blit(ms, x - 5, y - 5, 20, 102, 26, 26, 128, 256);
        context.renderIngredient(ms, x, y, mouseX, mouseY, ingredient);
    }

    private void framedRenderItemStack(PoseStack ms, IComponentRenderContext context, int x, int y, int mouseX, int mouseY, ItemStack itemStack) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, context.getCraftingTexture());
        GuiComponent.blit(ms, x - 5, y - 5, 20, 102, 26, 26, 128, 256);
        context.renderItemStack(ms, x, y, mouseX, mouseY, itemStack);
    }

    @Override
    public void render(PoseStack ms, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        if (recipe == null) return;

        List<Ingredient> ingredients = recipe.getIngredients(); // 0 = targetBlock, 1 = mainhand, 2 = offhand.
        if (ingredients.size() != 3) return;

        framedRenderIngredient(ms, context, x + 4, y + 6, mouseX, mouseY, ingredients.get(1));
        framedRenderIngredient(ms, context, x + 4, y + 36, mouseX, mouseY, ingredients.get(2));
        framedRenderIngredient(ms, context, x + 48, y + 20, mouseX, mouseY, ingredients.get(0));
        framedRenderItemStack(ms, context, x + 96, y + 20, mouseX, mouseY, recipe.getResultItem());

        Font font = context.getGui().getMinecraft().font;
        float pctScale = 0.65f;
        ms.pushPose();
        ms.scale(pctScale, pctScale, pctScale);
        font.draw(ms, Component.literal("mainhand").setStyle(context.getFont()), x, y +  (int)(4 / pctScale), context.getTextColor());
        font.draw(ms, Component.literal("offhand").setStyle(context.getFont()), x, y +  (int)(34 / pctScale), context.getTextColor());
        font.draw(ms, Component.literal("in-world").setStyle(context.getFont()), x + (int)(44 / pctScale), y + (int)(18 / pctScale), context.getTextColor());
        font.draw(ms, Component.literal("result").setStyle(context.getFont()), x + (int)(95 / pctScale), y + (int)(18 / pctScale), context.getTextColor());
        ms.popPose();
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        recipeName = lookup.apply(IVariable.wrap(recipeName)).asString();
        Level world = Minecraft.getInstance().level;
        if (world == null) return;
        recipe = (InWorldDualHandedCrafting) world.getRecipeManager().byKey(new ResourceLocation(recipeName)).orElse(null);
    }
}
