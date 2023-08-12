package com.mowmaster.pedestals.Compat.Patchouli;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Recipes.ToolSwapCrafting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.IVariable;

import java.util.List;
import java.util.function.UnaryOperator;

public class ComponentToolSwapCraftingRecipe extends BaseCustomPedestalsComponent {
    @SerializedName("recipe_name")
    public String recipeName;
    private transient ToolSwapCrafting recipe;

    @Override
    public void render(PoseStack ms, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        if (recipe == null) return;

        List<Ingredient> ingredients = recipe.getIngredients(); // 0 = mainhand.
        if (ingredients.size() != 1) return;

        framedRenderIngredient(ms, context, x + 23, y + 6, mouseX, mouseY, ingredients.get(0));
        renderArrow(ms, context, x + 50, y + 6);
        framedRenderItemStack(ms, context, x + 76, y + 6, mouseX, mouseY, recipe.getResultItem());

        Font font = context.getGui().getMinecraft().font;
        float pctScale = 0.65f;
        ms.pushPose();
        ms.scale(pctScale, pctScale, pctScale);
        font.draw(ms, Component.literal("mainhand").setStyle(context.getFont()), x + (int)(18 / pctScale), y +  (int)(4 / pctScale), context.getTextColor());
        font.draw(ms, Component.literal("result").setStyle(context.getFont()), x + (int)(75 / pctScale), y + (int)(4 / pctScale), context.getTextColor());
        ms.popPose();
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        recipeName = lookup.apply(IVariable.wrap(recipeName)).asString();
        Level world = Minecraft.getInstance().level;
        if (world == null) return;
        recipe = (ToolSwapCrafting) world.getRecipeManager().byKey(new ResourceLocation(recipeName)).orElse(null);
    }
}
