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

// Shows the stored item spinning on top of the pedestal, whichever way the pedestal faces.
// 26.x renderers work in two steps (see CrystalBlocks' chest renderer): extractRenderState copies what's needed
// out of the block entity, then submit draws from that copy only.
// Not ported yet from 1.20: several items orbiting, the upgrade coin, the tool, range boxes, render augments.
public class BasePedestalBlockEntityRenderer implements BlockEntityRenderer<BasePedestalBlockEntity, BasePedestalBlockEntityRenderer.State>
{
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

        ItemStack stack = pedestal.getItemInPedestal();
        // The last number is a seed for items whose model picks randomly; the position keeps it steady per pedestal
        itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.GROUND, pedestal.getLevel(), null,
                (int) pedestal.getBlockPos().asLong());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        if (state.item.isEmpty()) return;

        poseStack.pushPose();
        // Turn the whole block so "up" points the way the pedestal faces (getRotation turns UP onto that direction)
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.rotate(state.facing.getRotation());
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        // Just above the top (which is at y 12), three quarters size, spinning
        poseStack.translate(0.5f, 1.0f, 0.5f);
        poseStack.scale(0.75f, 0.75f, 0.75f);
        poseStack.rotateDegrees(Axis.YP, state.spin);

        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState
    {
        final ItemStackRenderState item = new ItemStackRenderState();
        Direction facing = Direction.UP;
        float spin;
    }
}
