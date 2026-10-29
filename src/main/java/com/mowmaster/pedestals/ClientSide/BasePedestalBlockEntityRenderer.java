package com.mowmaster.pedestals.ClientSide;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.api.upgrade.GhostItem;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

// Draws what's on the pedestal, whichever way the pedestal faces:
//  - the stored item spinning on top
//  - the upgrade as a small "coin" on each of the pillar's four sides
//  - the tool lying flat on the top
//  - the upgrade's ghost items (missing inputs): pulsing items with the amount needed above them, over the input
//    pedestals they belong on, or circling this pedestal
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
    // Block and sky light both at full: ghosts glow the same in the dark
    private static final int FULL_BRIGHT = 0xF000F0;

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

        extractGhosts(pedestal, state, seed);
    }

    private void extractGhosts(BasePedestalBlockEntity pedestal, State state, int seed)
    {
        List<GhostItem> ghosts = pedestal.getGhosts();
        Level level = pedestal.getLevel();
        // The render states are kept between frames and only added to, so nothing is made every frame
        while (state.ghosts.size() < ghosts.size()) state.ghosts.add(new Ghost());
        state.ghostCount = ghosts.size();
        state.orbitCount = 0;
        for (int index = 0; index < ghosts.size(); index++)
        {
            GhostItem ghost = ghosts.get(index);
            Ghost render = state.ghosts.get(index);
            itemModelResolver.updateForTopItem(render.item, ghost.stack(), ItemDisplayContext.GROUND, level, null, seed + index);
            render.label = ghost.label();
            if (ghost.above().isPresent() && level != null)
            {
                // Just above the other pedestal's top, whichever way it faces, relative to this block
                BlockPos at = ghost.above().get();
                Direction facing = level.getBlockState(at).getOptionalValue(BasePedestalBlock.FACING).orElse(Direction.UP);
                render.offset = Vec3.atCenterOf(at).subtract(Vec3.atLowerCornerOf(pedestal.getBlockPos())).add(facing.getUnitVec3().scale(0.95));
                render.orbitIndex = -1;
            }
            else render.orbitIndex = state.orbitCount++;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        if (state.item.isEmpty() && state.upgrade.isEmpty() && state.tool.isEmpty() && state.ghostCount == 0) return;

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

        // Ghosts are drawn in world directions (not turned with the pedestal), so they can face the camera
        for (int index = 0; index < state.ghostCount; index++)
        {
            Ghost ghost = state.ghosts.get(index);
            Vec3 offset = ghost.orbitIndex < 0 ? ghost.offset : orbitOffset(state, ghost.orbitIndex);
            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y, offset.z);
            submitGhost(ghost, state, poseStack, submitNodeCollector, camera);
            poseStack.popPose();
        }
    }

    // Where a ghost circling this pedestal is now: evenly spaced round it, a little higher and wider than its own
    // item, or straight above it if it's the only one. Worked out as if the pedestal stood on the ground, then turned
    // the way it faces (Vector3f.rotate applies the quaternion to the point).
    private static Vec3 orbitOffset(State state, int orbitIndex)
    {
        Vector3f point;
        if (state.orbitCount == 1) point = new Vector3f(0f, 1.1f, 0f);
        else
        {
            double angle = Math.toRadians(state.spin + 360.0 * orbitIndex / state.orbitCount);
            point = new Vector3f((float) Math.cos(angle) * 0.9f, 0.7f, (float) Math.sin(angle) * 0.9f);
        }
        point.rotate(state.facing.getRotation());
        return new Vec3(0.5 + point.x(), 0.5 + point.y(), 0.5 + point.z());
    }

    // A ghost: bobbing and pulsing, lit at full brightness, always turned to face the camera (like a thrown snowball),
    // with its label floating above (a name tag, which faces the camera by itself)
    private static void submitGhost(Ghost ghost, State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        double wave = Math.toRadians(state.spin * 2);
        poseStack.pushPose();
        poseStack.translate(0f, (float) (0.05 * Math.sin(wave)), 0f);
        if (ghost.label != null)
        {
            submitNodeCollector.submitNameTag(poseStack, new Vec3(0, 0.45, 0), 0, ghost.label, true, FULL_BRIGHT, camera);
        }
        poseStack.rotate(camera.orientation);
        float size = 0.45f + 0.05f * (float) Math.sin(wave * 2);
        poseStack.scale(size, size, size);
        ghost.item.submit(poseStack, submitNodeCollector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    // Normally just this block and the space above it; with ghosts, out to every pedestal they float over.
    // (A NeoForge addition: renderers are skipped when this box is off screen.)
    @Override
    public AABB getRenderBoundingBox(BasePedestalBlockEntity pedestal)
    {
        AABB box = new AABB(pedestal.getBlockPos()).inflate(1.5);
        for (GhostItem ghost : pedestal.getGhosts())
        {
            if (ghost.above().isPresent()) box = box.minmax(new AABB(ghost.above().get()).inflate(1.5));
        }
        return box;
    }

    public static class State extends BlockEntityRenderState
    {
        final ItemStackRenderState item = new ItemStackRenderState();
        final ItemStackRenderState upgrade = new ItemStackRenderState();
        final ItemStackRenderState tool = new ItemStackRenderState();
        Direction facing = Direction.UP;
        float spin;
        final List<Ghost> ghosts = new ArrayList<>();
        int ghostCount;
        int orbitCount;
    }

    // One ghost: orbitIndex is its place in the circle round this pedestal, or -1 when it floats above offset
    static class Ghost
    {
        final ItemStackRenderState item = new ItemStackRenderState();
        @Nullable Component label;
        Vec3 offset = Vec3.ZERO;
        int orbitIndex = -1;
    }
}
