package com.mowmaster.ironpedestals.ClientSide;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import com.mowmaster.ironpedestals.Items.Tools.LinkingToolItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Link beams, shown while the player holds a linking tool (either hand): one between every linked pair of loaded
// pedestals nearby, in the sending pedestal's colour, and one between the tool's saved pedestal and the tool itself.
// The texture (textures/misc/link_beam.png) is a chevron that points towards the receiving end and slides along it.
// The tool's beam runs saved pedestal -> hand in Send To mode, and hand -> saved pedestal in Receive From mode.
//
// Same two steps as PedestalOutlines: ExtractLevelRenderStateEvent copies the beams' ends into the frame's render
// state, SubmitCustomGeometryEvent draws them. They're drawn like the beacon's beam (vanilla's beaconBeam render type,
// see BeaconRenderer): flat strips turned to face the camera, full brightness, see-through.
@EventBusSubscriber(modid = IronPedestalReferences.MODID, value = Dist.CLIENT)
public final class PedestalBeams
{
    private PedestalBeams() {}

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IronPedestalReferences.MODID, "textures/misc/link_beam.png");
    private static final ContextKey<Frame> BEAMS = new ContextKey<>(Identifier.fromNamespaceAndPath(IronPedestalReferences.MODID, "link_beams"));
    // Pedestals further away than this (from the player) don't show their beams
    private static final double RANGE = 64;
    private static final float HALF_WIDTH = 0.07f;
    // Chevrons per block of beam, and how fast they move (texture lengths per tick)
    private static final float REPEATS_PER_BLOCK = 2f;
    private static final float SPEED = 0.05f;
    private static final int ALPHA = 0xC0;
    private static final int TOOL_BEAM_COLOR = 0xFFFFFF;

    // One beam: from the sender's end to the receiver's, and its colour (0xAARRGGBB)
    private record Beam(Vec3 from, Vec3 to, int color) {}

    // Everything one frame draws, and the time that slides the chevrons
    private record Frame(List<Beam> beams, float time) {}

    // ---- Step 1: what to draw ----

    @SubscribeEvent
    public static void extract(ExtractLevelRenderStateEvent event)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = event.getLevel();
        if (player == null) return;
        InteractionHand toolHand = toolHand(player);
        if (toolHand == null) return;

        float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        List<Beam> beams = new ArrayList<>();
        for (BasePedestalBlockEntity pedestal : BasePedestalBlockEntity.CLIENT_LOADED)
        {
            if (pedestal.getLevel() != level || pedestal.isRemoved()) continue;
            if (pedestal.getBlockPos().distToCenterSqr(player.position()) > RANGE * RANGE) continue;
            int color = (ALPHA << 24) | (pedestal.getTint(0) & 0xFFFFFF);
            Vec3 from = top(level, pedestal.getBlockPos());
            for (BlockPos link : pedestal.getLinks()) beams.add(new Beam(from, top(level, link), color));
        }

        // The tool's saved pedestal to the tool, at about where it is held
        ItemStack tool = player.getItemInHand(toolHand);
        BlockPos saved = LinkingToolItem.getSelected(tool);
        if (saved != null)
        {
            Vec3 hand = handPosition(player, toolHand, partialTick);
            Vec3 pedestalEnd = top(level, saved);
            int color = (ALPHA << 24) | TOOL_BEAM_COLOR;
            beams.add(LinkingToolItem.isReceiving(tool) ? new Beam(hand, pedestalEnd, color) : new Beam(pedestalEnd, hand, color));
        }
        if (beams.isEmpty()) return;
        event.getRenderState().setRenderData(BEAMS, new Frame(beams, level.getGameTime() + partialTick));
    }

    // The hand holding a linking tool (main hand first), or null
    private static @Nullable InteractionHand toolHand(LocalPlayer player)
    {
        if (player.getMainHandItem().getItem() instanceof LinkingToolItem) return InteractionHand.MAIN_HAND;
        if (player.getOffhandItem().getItem() instanceof LinkingToolItem) return InteractionHand.OFF_HAND;
        return null;
    }

    // A point just above a pedestal's top (whichever way it faces); the block's centre if it isn't a pedestal
    private static Vec3 top(ClientLevel level, BlockPos pos)
    {
        Vec3 centre = Vec3.atCenterOf(pos);
        if (!(level.getBlockEntity(pos) instanceof BasePedestalBlockEntity pedestal)) return centre;
        return centre.add(pedestal.getFacing().getUnitVec3().scale(0.32));
    }

    // Roughly where the tool is: in front of the eyes, a little down and out to the holding hand's side
    private static Vec3 handPosition(LocalPlayer player, InteractionHand hand, float partialTick)
    {
        Vec3 look = player.getViewVector(partialTick);
        Vec3 right = look.cross(new Vec3(0, 1, 0));
        right = right.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : right.normalize();
        boolean rightHand = (hand == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.RIGHT);
        return player.getEyePosition(partialTick).add(look.scale(0.6)).add(right.scale(rightHand ? 0.32 : -0.32)).add(0, -0.3, 0);
    }

    // ---- Step 2: hand the strips to the renderer ----

    @SubscribeEvent
    public static void submit(SubmitCustomGeometryEvent event)
    {
        Frame frame = event.getLevelRenderState().getRenderData(BEAMS);
        if (frame == null) return;
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        PoseStack poseStack = event.getPoseStack();
        // The lambda is called later in the frame with the pose and a buffer to write vertices into
        event.getSubmitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.beaconBeam(TEXTURE, true), (pose, buffer) -> {
            for (Beam beam : frame.beams()) strip(pose, buffer, beam.from().subtract(camera), beam.to().subtract(camera), beam.color(), frame.time());
        });
    }

    // A flat strip from a to b (camera-relative), turned towards the camera: its sideways direction is at right
    // angles to both the beam and the line from the camera to the beam's middle. Drawn from both sides.
    private static void strip(PoseStack.Pose pose, VertexConsumer buffer, Vec3 a, Vec3 b, int color, float time)
    {
        Vec3 along = b.subtract(a);
        double length = along.length();
        if (length < 1.0E-3) return;
        Vec3 side = along.cross(a.add(b).scale(0.5));
        if (side.lengthSqr() < 1.0E-8) return;
        side = side.normalize().scale(HALF_WIDTH);

        // v runs along the beam; subtracting the time slides the chevrons from a towards b
        float v0 = -time * SPEED;
        float v1 = v0 + (float) length * REPEATS_PER_BLOCK;
        Vec3 a0 = a.subtract(side), a1 = a.add(side), b1 = b.add(side), b0 = b.subtract(side);
        vertex(pose, buffer, a0, 0, v0, color);
        vertex(pose, buffer, a1, 1, v0, color);
        vertex(pose, buffer, b1, 1, v1, color);
        vertex(pose, buffer, b0, 0, v1, color);
        // The back, wound the other way, so it shows whichever side faces the camera
        vertex(pose, buffer, b0, 0, v1, color);
        vertex(pose, buffer, b1, 1, v1, color);
        vertex(pose, buffer, a1, 1, v0, color);
        vertex(pose, buffer, a0, 0, v0, color);
    }

    // The beacon beam's vertex format: position, colour, texture, overlay, light, normal
    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 at, float u, float v, int color)
    {
        buffer.addVertex(pose, (float) at.x, (float) at.y, (float) at.z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0, 1, 0);
    }
}
