package com.mowmaster.pedestals.ClientSide;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// Draws what's on the pedestal, whichever way the pedestal faces:
//  - the stored item spinning on top
//  - the upgrade as a small "coin" on each of the pillar's four sides
//  - the tool lying flat on the top
// 26.x renderers work in two steps (see CrystalBlocks' chest renderer): extractRenderState copies what's needed
// out of the block entity, then submit draws from that copy only.
// Not ported yet from 1.20: several items orbiting, range boxes, render augments, the client render options.
public class BasePedestalBlockEntityRenderer implements BlockEntityRenderer<BasePedestalBlockEntity, BasePedestalBlockEntityRenderer.State>
{
    // Where the coins sit, from 1.20: just outside each face of the pillar (pixels 5 to 11), halfway up it,
    // each turned to face outwards. {x, z, degrees about Y}
    private static final float[][] COIN_SPOTS = {
            {0.5f, 0.3125f, 0f},
            {0.3125f, 0.5f, 90f},
            {0.5f, 0.6875f, 180f},
            {0.6875f, 0.5f, 270f}};
    private static final float COIN_Y = 0.475f;

    private final ItemModelResolver itemModelResolver;

    public BasePedestalBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState()
    {
        return new State();
    }

    @Override
    public void extractRenderState(BasePedestalBlockEntity pedestal, State state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay)
    {
        BlockEntityRenderer.super.extractRenderState(pedestal, state, partialTick, cameraPos, crumblingOverlay);
        state.facing = pedestal.getBlockState().getValue(BasePedestalBlock.FACING);
        // 2 degrees a tick, the same speed as 1.20; partialTick keeps it smooth between ticks
        state.spin = pedestal.getLevel() == null ? 0 : ((pedestal.getLevel().getGameTime() % 180) + partialTick) * 2f;

        // The last number is a seed for items whose model picks randomly; the position keeps it steady per pedestal
        int seed = (int) pedestal.getBlockPos().asLong();
        ItemStack stack = pedestal.getItemInPedestal();
        itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.GROUND, pedestal.getLevel(), null, seed);
        // FIXED is the item frame look: flat, full size, no bobbing. 1.20 used it for both of these too.
        itemModelResolver.updateForTopItem(state.upgrade, pedestal.getUpgrade(), ItemDisplayContext.FIXED, pedestal.getLevel(), null, seed);
        // Only the inserted tool, not the upgrade's default one (1.20 showed the default too; drop the
        // hasTool check, i.e. use getToolStack(), if you want that back)
        itemModelResolver.updateForTopItem(state.tool, pedestal.getTool(), ItemDisplayContext.FIXED, pedestal.getLevel(), null, seed);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        if (state.item.isEmpty() && state.upgrade.isEmpty() && state.tool.isEmpty()) return;

        poseStack.pushPose();
        // Turn the whole block so "up" points the way the pedestal faces (getRotation turns UP onto that direction).
        // Everything below is then placed as if the pedestal stood on the ground.
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.rotate(state.facing.getRotation());
        poseStack.translate(-0.5f, -0.5f, -0.5f);

        if (!state.item.isEmpty())
        {
            poseStack.pushPose();
            // Just above the top (which is at y 12), three quarters size, spinning
            poseStack.translate(0.5f, 1.0f, 0.5f);
            poseStack.scale(0.75f, 0.75f, 0.75f);
            poseStack.rotateDegrees(Axis.YP, state.spin);
            state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (!state.upgrade.isEmpty())
        {
            for (float[] spot : COIN_SPOTS)
            {
                poseStack.pushPose();
                poseStack.translate(spot[0], COIN_Y, spot[1]);
                poseStack.scale(0.1875f, 0.1875f, 0.1875f);
                poseStack.rotateDegrees(Axis.YP, spot[2]);
                state.upgrade.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }

        if (!state.tool.isEmpty())
        {
            poseStack.pushPose();
            // Lying flat on the top face (y 12), half size, pointing along X
            poseStack.translate(0.5f, 0.75f, 0.5f);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.rotateDegrees(Axis.YP, 90f);
            poseStack.rotateDegrees(Axis.XP, 90f);
            state.tool.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState
    {
        final ItemStackRenderState item = new ItemStackRenderState();
        final ItemStackRenderState upgrade = new ItemStackRenderState();
        final ItemStackRenderState tool = new ItemStackRenderState();
        Direction facing = Direction.UP;
        float spin;
    }
}
