package com.mowmaster.pedestals.Compat.Patchouli;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
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

    protected void framedRenderIngredient(GuiGraphics graphics, IComponentRenderContext context, int x, int y, int mouseX, int mouseY, Ingredient ingredient) {
        RenderSystem.enableBlend();
        graphics.blit(context.getCraftingTexture(), x - 5, y - 5, 20, 102, 26, 26, 128, 256);
        context.renderIngredient(graphics, x, y, mouseX, mouseY, ingredient);
    }

    protected void framedRenderItemStack(GuiGraphics graphics, IComponentRenderContext context, int x, int y, int mouseX, int mouseY, ItemStack itemStack) {
        RenderSystem.enableBlend();
        graphics.blit(context.getCraftingTexture(), x - 5, y - 5, 20, 102, 26, 26, 128, 256);
        context.renderItemStack(graphics, x, y, mouseX, mouseY, itemStack);
    }

    protected void renderArrow(GuiGraphics graphics, IComponentRenderContext context, int x, int y) {
        RenderSystem.enableBlend();
        graphics.blit(context.getCraftingTexture(), x, y + 1, 35, 77, 13, 13, 128, 256);
    }
}
