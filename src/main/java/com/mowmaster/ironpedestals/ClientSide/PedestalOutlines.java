package com.mowmaster.ironpedestals.ClientSide;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Outlines round linked pedestals, switched on from the pedestal screen's link list (client only; nothing is saved
// or sent to the server). Each outline has its own colour, the same one its row in the list shows. An outline goes
// away by itself once the outlined block isn't a pedestal any more (broken), the pedestal that links to it is gone,
// or that link was removed (checked each frame, while those blocks are loaded).
//
// 26.x draws the level in two steps, and NeoForge has an event for each:
//   ExtractLevelRenderStateEvent - copy what will be drawn into the frame's LevelRenderState (here: which blocks
//                                  and their shapes, while the level can still be read)
//   SubmitCustomGeometryEvent    - hand the geometry to the SubmitNodeCollector, which draws it later in the frame
// submitShapeOutline is what vanilla uses for the block you're looking at: the shape's edges as lines.
@EventBusSubscriber(modid = IronPedestalReferences.MODID, value = Dist.CLIENT)
public final class PedestalOutlines
{
    private PedestalOutlines() {}

    // Outline colours (0xAARRGGBB, fully opaque so they draw with the solid geometry), picked by link index
    private static final int[] COLORS = {0xFFFF5555, 0xFF55FF55, 0xFF5599FF, 0xFFFFDD33, 0xFFFF55FF, 0xFF55FFFF, 0xFFFF9933, 0xFFFFFFFF};
    private static final float LINE_WIDTH = 3.0f;
    // A ContextKey names one piece of custom data on the render state (like a typed map key)
    private static final ContextKey<List<Outline>> OUTLINES = new ContextKey<>(Identifier.fromNamespaceAndPath(IronPedestalReferences.MODID, "link_outlines"));

    // Shown outlines: outlined position -> which pedestal links to it and the colour, per dimension (a pedestal's
    // link is always in its own dimension)
    private static final Map<ResourceKey<Level>, Map<BlockPos, Shown>> SHOWN = new LinkedHashMap<>();

    private record Shown(BlockPos source, int color) {}

    // What one outline needs once extracted: where, which shape, which colour
    private record Outline(BlockPos pos, VoxelShape shape, int color) {}

    public static int colorFor(int linkIndex)
    {
        return COLORS[Math.floorMod(linkIndex, COLORS.length)];
    }

    public static boolean isShown(ResourceKey<Level> dimension, BlockPos pos)
    {
        Map<BlockPos, Shown> shown = SHOWN.get(dimension);
        return shown != null && shown.containsKey(pos);
    }

    // Turns one outline on or off; returns whether it's on now. source: the pedestal whose link it is.
    public static boolean toggle(ResourceKey<Level> dimension, BlockPos source, BlockPos pos, int color)
    {
        Map<BlockPos, Shown> shown = SHOWN.computeIfAbsent(dimension, key -> new LinkedHashMap<>());
        if (shown.remove(pos) != null) return false;
        shown.put(pos.immutable(), new Shown(source.immutable(), color));
        return true;
    }

    public static void hide(ResourceKey<Level> dimension, BlockPos pos)
    {
        Map<BlockPos, Shown> shown = SHOWN.get(dimension);
        if (shown != null) shown.remove(pos);
    }

    // Still worth showing? Only blocks that are loaded can be checked; unloaded ones keep their outline for now.
    private static boolean stillLinked(ClientLevel level, BlockPos pos, Shown shown)
    {
        if (level.isLoaded(pos) && !(level.getBlockEntity(pos) instanceof BasePedestalBlockEntity)) return false;
        if (!level.isLoaded(shown.source())) return true;
        return level.getBlockEntity(shown.source()) instanceof BasePedestalBlockEntity source && source.getLinks().contains(pos);
    }

    // ---- Step 1: what to draw (the level can be read here) ----

    @SubscribeEvent
    public static void extract(ExtractLevelRenderStateEvent event)
    {
        ClientLevel level = event.getLevel();
        Map<BlockPos, Shown> shown = SHOWN.get(level.dimension());
        if (shown == null || shown.isEmpty()) return;
        // Broken pedestals and removed links lose their outline (removeIf: removes every entry the test is true for)
        shown.entrySet().removeIf(entry -> !stillLinked(level, entry.getKey(), entry.getValue()));
        List<Outline> outlines = new ArrayList<>();
        shown.forEach((pos, entry) -> {
            if (!level.isLoaded(pos)) return;
            VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
            outlines.add(new Outline(pos, shape.isEmpty() ? Shapes.block() : shape, entry.color()));
        });
        event.getRenderState().setRenderData(OUTLINES, outlines);
    }

    // ---- Step 2: hand the lines to the renderer ----

    @SubscribeEvent
    public static void submit(SubmitCustomGeometryEvent event)
    {
        List<Outline> outlines = event.getLevelRenderState().getRenderData(OUTLINES);
        if (outlines == null || outlines.isEmpty()) return;
        // The pose stack is relative to the camera, so each block is moved by (its position - the camera's)
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        PoseStack poseStack = event.getPoseStack();
        for (Outline outline : outlines)
        {
            poseStack.pushPose();
            poseStack.translate(outline.pos().getX() - camera.x, outline.pos().getY() - camera.y, outline.pos().getZ() - camera.z);
            event.getSubmitNodeCollector().submitShapeOutline(poseStack, outline.shape(), RenderTypes.lines(), outline.color(), LINE_WIDTH, false);
            poseStack.popPose();
        }
    }
}
