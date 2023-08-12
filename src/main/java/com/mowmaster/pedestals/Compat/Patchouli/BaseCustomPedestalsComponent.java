package com.mowmaster.pedestals.Compat.Patchouli;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;

public abstract class BaseCustomPedestalsComponent implements ICustomComponent {
    protected transient int x, y;

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        x = componentX;
        y = componentY;
    }

    protected void framedRenderIngredient(PoseStack ms, IComponentRenderContext context, int x, int y, int mouseX, int mouseY, Ingredient ingredient) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, context.getCraftingTexture());
        GuiComponent.blit(ms, x - 5, y - 5, 20, 102, 26, 26, 128, 256);
        context.renderIngredient(ms, x, y, mouseX, mouseY, ingredient);
    }

    protected void framedRenderItemStack(PoseStack ms, IComponentRenderContext context, int x, int y, int mouseX, int mouseY, ItemStack itemStack) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, context.getCraftingTexture());
        GuiComponent.blit(ms, x - 5, y - 5, 20, 102, 26, 26, 128, 256);
        context.renderItemStack(ms, x, y, mouseX, mouseY, itemStack);
    }

    protected void renderArrow(PoseStack ms, IComponentRenderContext context, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, context.getCraftingTexture());
        GuiComponent.blit(ms, x, y + 1, 35, 77, 13, 13, 128, 256);
    }
}
