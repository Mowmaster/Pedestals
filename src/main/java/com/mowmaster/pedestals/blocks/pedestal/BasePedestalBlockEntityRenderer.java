package com.mowmaster.pedestals.blocks.pedestal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mowmaster.pedestals.configs.PedestalConfig;
import com.mowmaster.pedestals.items.ISelectablePoints;
import com.mowmaster.pedestals.items.upgrades.pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.items.workcards.WorkCardArea;
import com.mowmaster.pedestals.items.workcards.WorkCardBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlock.FACING;
import static com.mowmaster.pedestals.pedestalutils.References.MODID;

public class BasePedestalBlockEntityRenderer implements BlockEntityRenderer<BasePedestalBlockEntity>
{
    public BasePedestalBlockEntityRenderer() {}

    private static final TextureAtlasSprite WHITE_TEXTURE_SPRITE = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation(MODID, "util/pedestal_render"));
    private static final TextureAtlasSprite UPGRADE_TEXTURE_SPRITE = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation(MODID, "util/upgrade_render"));

    @Override
    public void render(BasePedestalBlockEntity pedestalBlockEntity, float p_112308_, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int p_112311_, int p_112312_) {
        if (pedestalBlockEntity.isRemoved()) {
            return;
        }

        Direction facing = pedestalBlockEntity.getBlockState().getValue(FACING);
        List<ItemStack> listed = pedestalBlockEntity.getItemStacks();
        ItemStack toolStack = pedestalBlockEntity.getToolStack();
        ItemStack coin = pedestalBlockEntity.getCoinOnPedestal();
        ItemStack workCard = pedestalBlockEntity.getWorkCardInPedestal();
        List<BlockPos> linkedLocations = pedestalBlockEntity.getLinkedLocations();
        BlockPos pos = pedestalBlockEntity.getPos();
        Level world = pedestalBlockEntity.getLevel();
        List<String> hudMessages = pedestalBlockEntity.getHudLog();
            // 0 - No Particles
            // 1 - No Render Item
            // 2 - No Render upgrade
            // 3 - No Particles/No Render Item
            // 4 - No Particles/No Render upgrade
            // 5 - No Render Item/No Render upgrade
            // 6 - No Particles/No Render Item/No Render upgrade
            // 7 - No augment exists and thus all rendering is fine.
            int renderAugmentType = pedestalBlockEntity.getRendererType();
        if (pedestalBlockEntity.getRenderRange()) {
            int range = pedestalBlockEntity.getLinkingRange();
            AABB aabb = new AABB(pos.offset(-range, -range, -range), pos.offset(range + 1, range + 1, range + 1));
            poseStack.pushPose();
            renderBoundingBox(pos, aabb, poseStack, multiBufferSource.getBuffer(RenderType.lines()), 1f, 0.2f, 0.2f, 1f);
            renderFaces(WHITE_TEXTURE_SPRITE, pos, aabb, poseStack, multiBufferSource.getBuffer(Sheets.translucentCullBlockSheet()), 1f, 0.2f, 0.2f, 0.5f);
            poseStack.popPose();

            if (!linkedLocations.isEmpty()) {
                poseStack.pushPose();
                for (BlockPos posPoints : linkedLocations) {
                    AABB aabbCoin = new AABB(posPoints);
                    if (!aabbCoin.equals(new AABB(BlockPos.ZERO))) {
                        TextureAtlasSprite upgradeTextureSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation(MODID, "util/pedestal_render_" + (linkedLocations.indexOf(posPoints) + 1)));
                        renderBoundingBox(pos, aabbCoin, poseStack, multiBufferSource.getBuffer(RenderType.lines()), 0.0f, 0.0f, 1f, 1f);
                        renderFaces(upgradeTextureSprite, pos, aabbCoin, poseStack, multiBufferSource.getBuffer(Sheets.translucentCullBlockSheet()), 0.0f, 0.0f, 1f, 0.5f);
                    }
                }
                poseStack.popPose();
            }
        } else {
            linkedLocations.clear();
        }

        if (pedestalBlockEntity.getRenderRangeUpgrade() && !coin.isEmpty()) {
            int range = 0;
            if (coin.getItem() instanceof ItemUpgradeBase upgrade) {
                range = upgrade.getUpgradeWorkRange(coin);
            }

            if (range > 0) {
                AABB aabb = new AABB(pos.offset(-range, -range, -range), pos.offset(range + 1, range + 1, range + 1));
                poseStack.pushPose();
                renderBoundingBox(pos, aabb, poseStack, multiBufferSource.getBuffer(RenderType.lines()), 0.2f, 0.2f, 1f, 1f);
                renderFaces(WHITE_TEXTURE_SPRITE, pos, aabb, poseStack, multiBufferSource.getBuffer(Sheets.translucentCullBlockSheet()), 0.2f, 0.2f, 1f, 0.5f);
                poseStack.popPose();

                if (workCard.getItem() instanceof WorkCardArea workCardArea) {
                    poseStack.pushPose();
                    WorkCardArea.getAABBIfDefined(workCard).ifPresent(workCardAABB -> {
                        AABB expandedWorkCardAABB = workCardAABB.expandTowards(1.0D, 1.0D, 1.0D);
                        boolean inSelectedInRange = workCardArea.selectedAreaWithinRange(pedestalBlockEntity);
                        renderBoundingBox(pos, expandedWorkCardAABB, poseStack, multiBufferSource.getBuffer(RenderType.lines()), inSelectedInRange ? 0.0f : 1f, inSelectedInRange ? 1f : 0.0f, 0.0f, 1f);
                        renderFaces(UPGRADE_TEXTURE_SPRITE, pos, expandedWorkCardAABB, poseStack, multiBufferSource.getBuffer(Sheets.translucentCullBlockSheet()), inSelectedInRange ? 0.0f : 1f, inSelectedInRange ? 1f : 0.0f, 0.0f, 0.5f);
                    });
                    poseStack.popPose();
                }

                if (workCard.getItem() instanceof ISelectablePoints) {
                    if (workCard.getItem() instanceof WorkCardBase cardBase) {
                        if (!cardBase.hasTwoPointsSelected(workCard)) {
                            poseStack.pushPose();
                            List<BlockPos> locations = WorkCardBase.readBlockPosListFromNBT(workCard);
                            if (!locations.isEmpty()) {
                                for (BlockPos posPoints : locations) {
                                    AABB aabbCoin = new AABB(posPoints);
                                    if (!aabbCoin.equals(new AABB(BlockPos.ZERO))) {
                                        boolean inSelectedInRange = cardBase.selectedPointWithinRange(pedestalBlockEntity, posPoints);
                                        renderBoundingBox(pos, aabbCoin, poseStack, multiBufferSource.getBuffer(RenderType.lines()), inSelectedInRange ? 0.0f : 1f, inSelectedInRange ? 1f : 0.0f, 0.0f, 1f);
                                        renderFaces(UPGRADE_TEXTURE_SPRITE, pos, aabbCoin, poseStack, multiBufferSource.getBuffer(Sheets.translucentCullBlockSheet()), inSelectedInRange ? 0.0f : 1f, inSelectedInRange ? 1f : 0.0f, 0.0f, 0.5f);
                                    }
                                }
                            }
                            poseStack.popPose();
                        }
                    }
                }
            }
        }

        if (renderAugmentType != 6) {
            poseStack.pushPose();
            switch (facing) {
                case UP:
                    break;
                case DOWN:
                    poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
                    poseStack.translate(0, -1, 0);
                    poseStack.translate(-1, 0, 0);
                    break;
                case NORTH:
                    poseStack.mulPose(Vector3f.XP.rotationDegrees(270));
                    poseStack.translate(0, -1, 0);
                    break;
                case EAST:
                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(270));
                    poseStack.translate(-1, 0, 0);
                    break;
                case SOUTH:
                    poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
                    poseStack.translate(0, 0, -1);
                    break;
                case WEST:
                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(90));
                    poseStack.translate(0, -1, 0);
                    break;
            }
            renderTileItems(world, poseStack, multiBufferSource, listed, coin, toolStack, p_112311_, p_112312_, renderAugmentType, hudMessages);
            poseStack.popPose();
        }
    }

    public static void renderTileItems(Level worldIn, PoseStack poseStack, MultiBufferSource multiBufferSource, List<ItemStack> item, ItemStack coin, ItemStack toolStack, int p_112311_, int p_112312_, int renderAugmentType, List<String> messages) {
        boolean renderUpgrades = PedestalConfig.CLIENT.pedestalRenderUpgrades.get();
        boolean renderUpgradesInToolSlot = PedestalConfig.CLIENT.pedestalRenderUpgradeInToolSlot.get();
        if(renderUpgradesInToolSlot) {
            if (renderUpgrades) {
                renderTool(worldIn, coin, poseStack, multiBufferSource, p_112311_, p_112312_);
            }
        } else {
            if (renderUpgrades) {
                renderCoin(worldIn, coin, poseStack, multiBufferSource, 0.5f, 0.475f, 0.3125f,0, p_112311_,p_112312_);
                renderCoin(worldIn, coin, poseStack, multiBufferSource, 0.3125f, 0.475f, 0.5f,90, p_112311_,p_112312_);
                renderCoin(worldIn, coin, poseStack, multiBufferSource, 0.5f, 0.475f, 0.6875f,180, p_112311_,p_112312_);
                renderCoin(worldIn, coin, poseStack, multiBufferSource, 0.6875f, 0.475f, 0.5f,270, p_112311_,p_112312_);
            }
            boolean renderUpgradeExtras = PedestalConfig.CLIENT.pedestalRenderToolSlot.get();
            if (renderUpgradeExtras) {
                renderTool(worldIn, toolStack, poseStack, multiBufferSource, p_112311_, p_112312_);
            }
        }

        boolean renderHUD = PedestalConfig.CLIENT.pedestalRenderHUD.get();
        if (renderHUD) {
            if (messages.size() > 0) {
                renderPedestalsHUD(poseStack, multiBufferSource, messages, -0.75F, 0);
                renderPedestalsHUD(poseStack, multiBufferSource, messages, 1.5F, 180);
            }
        }

        boolean renderItems = PedestalConfig.CLIENT.pedestalRenderItems.get();
        if (renderItems) {
            if (!(renderAugmentType == 5 || renderAugmentType == 6)) {
                renderItemsRotating(worldIn, poseStack, multiBufferSource, item, p_112311_, p_112312_);
            }
        }
    }

    public static void renderItemsRotating(Level worldIn, PoseStack posStack, MultiBufferSource buffers, List<ItemStack> listed, int light, int overlay) {
        //https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/client/render/tile/RenderTileRuneAltar.java#L49
        if(listed != null && !listed.isEmpty()) {
            posStack.pushPose();
            posStack.translate(0.5, 1.0, 0.5);
            posStack.scale(0.75F, 0.75F, 0.75F);
            long time = System.currentTimeMillis();
            float angle = time / 25 % 360;
            boolean rotateEnabled = PedestalConfig.CLIENT.pedestalRotateItems.get();
            if(rotateEnabled) {
                posStack.mulPose(Vector3f.YP.rotationDegrees(angle));
            }
            ItemStack stack = listed.get(0);
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel bakedModel = renderer.getModel(stack, worldIn, null, 0);
            renderer.render(stack, ItemTransforms.TransformType.GROUND, true, posStack, buffers, light, overlay, bakedModel);
            posStack.popPose();
        }
    }

    private static final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    private static BakedModel cachedModel = null;

    public static void renderCoin(Level worldIn, ItemStack itemCoin, PoseStack poseStack, MultiBufferSource bufferSource, float x, float y, float z, float angle, int overlay, int light) {
        if (!itemCoin.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.scale(0.1875f, 0.1875f, 0.1875f);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(angle));

            // Check if the coin model is already cached
            if (cachedModel == null || cachedModel != itemRenderer.getModel(itemCoin, worldIn, null, 0)) {
                cachedModel = itemRenderer.getModel(itemCoin, worldIn, null, 0);
            }

            // Render the coin using the current pose stack
            itemRenderer.render(itemCoin, ItemTransforms.TransformType.FIXED, true, poseStack, bufferSource, overlay, light, cachedModel);

            poseStack.popPose();
        }
    }

    /*
    private static boolean isVisibleToPlayer(Level level, BlockPos pos) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Vec3 eyePos = player.getEyePosition(1.0F);
            Vec3 blockPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            Vec3 lookDir = player.getLookAngle().normalize();
            Vec3 playerToBlock = blockPos.subtract(eyePos).normalize();

            double dotProduct = lookDir.dot(playerToBlock);

            // Adjust the threshold to change the visibility angle
            double visibilityAngleThreshold = Math.cos(Math.toRadians(-90)); // 60 degrees

            // Check if the angle between the player's look direction and the vector to the block is within the visibility angle threshold
            return dotProduct >= visibilityAngleThreshold;
        }
        return false;
    }
*/

    /*
    private static boolean isVisibleToPlayer(BlockPos pos) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Vec3 eyePos = player.getEyePosition(1.0F);
            Vec3 lookDir = player.getLookAngle().normalize();
            Vec3 blockPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            // Calculate the vector from the player's eye position to the block position
            Vec3 vecToBlock = blockPos.subtract(eyePos);

            // Check if the angle between the player's look direction and the vector to the block is less than 90 degrees
            return lookDir.dot(vecToBlock) >= 0;
        }
        return false;
    }
*/
    public static void renderTool(Level worldIn, ItemStack itemTool, PoseStack poseStack, MultiBufferSource bufferSource, int overlay, int light) {
        if (!itemTool.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.75D, 0.5D);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));

            if (cachedModel == null || cachedModel != itemRenderer.getModel(itemTool, worldIn, null, 0)) {
                cachedModel = itemRenderer.getModel(itemTool, worldIn, null, 0);
            }

            itemRenderer.render(itemTool, ItemTransforms.TransformType.FIXED, true, poseStack, bufferSource, overlay, light, cachedModel);

            poseStack.popPose();
        }
    }

    //https://github.com/StanCEmpire/RegionProtection/blob/1.18.x/src/main/java/stancempire/stancempiresregionprotection/blockentities/RegionBlockBER.java
    private final Matrix3f normalMatrix = new Matrix3f();

    public void renderBoundingBox(BlockPos pos, AABB aabb, PoseStack matrixStack, VertexConsumer buffer, float red, float green, float blue, float alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();

        float minX = (float) (aabb.minX - pos.getX());
        float minY = (float) (aabb.minY - pos.getY());
        float minZ = (float) (aabb.minZ - pos.getZ());

        float maxX = (float) (aabb.maxX - pos.getX());
        float maxY = (float) (aabb.maxY - pos.getY());
        float maxZ = (float) (aabb.maxZ - pos.getZ());

        // Bottom
        buffer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, 0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, 0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha).normal(normalMatrix, 0, -1, 0).endVertex();
        buffer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha).normal(normalMatrix, 0, -1, 0).endVertex();

        // Top
        buffer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 1, 0).endVertex();
        buffer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 1, 0).endVertex();

        // Sides
        buffer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 0, -1).endVertex();
        buffer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 0, -1).endVertex();

        buffer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 0, 1).endVertex();
        buffer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 0, 1).endVertex();
        buffer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 0, 1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, 0, 0, 1).endVertex();

        buffer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha).normal(normalMatrix, -1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, -1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, -1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha).normal(normalMatrix, -1, 0, 0).endVertex();

        buffer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha).normal(normalMatrix, 1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha).normal(normalMatrix, 1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, 1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha).normal(normalMatrix, 1, 0, 0).endVertex();
    }

    public void renderFaces(TextureAtlasSprite sprite, BlockPos pos, AABB aabb, PoseStack matrixStack, VertexConsumer buffer, float red, float green, float blue, float alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();
        float minX = (float)(aabb.minX - pos.getX());
        float minY = (float)(aabb.minY - pos.getY());
        float minZ = (float)(aabb.minZ - pos.getZ());
        float maxX = (float)(aabb.maxX - pos.getX());
        float maxY = (float)(aabb.maxY - pos.getY());
        float maxZ = (float)(aabb.maxZ - pos.getZ());

        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        int uvBrightness = 240;

        // Calculate shared normals
        Vector3f normalXPos = new Vector3f(1, 0, 0);
        Vector3f normalXNeg = new Vector3f(-1, 0, 0);
        Vector3f normalYPos = new Vector3f(0, 1, 0);
        Vector3f normalYNeg = new Vector3f(0, -1, 0);
        Vector3f normalZPos = new Vector3f(0, 0, 1);
        Vector3f normalZNeg = new Vector3f(0, 0, -1);

        // West and East faces
        buffer.vertex(matrix4f, minX - 0.01f, minY, maxZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalXNeg.x(), normalXNeg.y(), normalXNeg.z()).endVertex();
        buffer.vertex(matrix4f, minX - 0.01f, maxY, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalXNeg.x(), normalXNeg.y(), normalXNeg.z()).endVertex();
        buffer.vertex(matrix4f, minX - 0.01f, maxY, minZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalXNeg.x(), normalXNeg.y(), normalXNeg.z()).endVertex();
        buffer.vertex(matrix4f, minX - 0.01f, minY, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalXNeg.x(), normalXNeg.y(), normalXNeg.z()).endVertex();

        buffer.vertex(matrix4f, minX + 0.01f, minY, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalXPos.x(), normalXPos.y(), normalXPos.z()).endVertex();
        buffer.vertex(matrix4f, minX + 0.01f, maxY, minZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalXPos.x(), normalXPos.y(), normalXPos.z()).endVertex();
        buffer.vertex(matrix4f, minX + 0.01f, maxY, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalXPos.x(), normalXPos.y(), normalXPos.z()).endVertex();
        buffer.vertex(matrix4f, minX + 0.01f, minY, maxZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalXPos.x(), normalXPos.y(), normalXPos.z()).endVertex();

        // North and South faces
        buffer.vertex(matrix4f, minX, maxY, minZ - 0.01f).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalZNeg.x(), normalZNeg.y(), normalZNeg.z()).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ - 0.01f).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalZNeg.x(), normalZNeg.y(), normalZNeg.z()).endVertex();
        buffer.vertex(matrix4f, maxX, minY, minZ - 0.01f).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalZNeg.x(), normalZNeg.y(), normalZNeg.z()).endVertex();
        buffer.vertex(matrix4f, minX, minY, minZ - 0.01f).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalZNeg.x(), normalZNeg.y(), normalZNeg.z()).endVertex();

        buffer.vertex(matrix4f, minX, minY, minZ + 0.01f).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalZPos.x(), normalZPos.y(), normalZPos.z()).endVertex();
        buffer.vertex(matrix4f, maxX, minY, minZ + 0.01f).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalZPos.x(), normalZPos.y(), normalZPos.z()).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ + 0.01f).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalZPos.x(), normalZPos.y(), normalZPos.z()).endVertex();
        buffer.vertex(matrix4f, minX, maxY, minZ + 0.01f).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalZPos.x(), normalZPos.y(), normalZPos.z()).endVertex();

        // Bottom and Top faces
        buffer.vertex(matrix4f, maxX, minY - 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYNeg.x(), normalYNeg.y(), normalYNeg.z()).endVertex();
        buffer.vertex(matrix4f, maxX, minY - 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYNeg.x(), normalYNeg.y(), normalYNeg.z()).endVertex();
        buffer.vertex(matrix4f, minX, minY - 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYNeg.x(), normalYNeg.y(), normalYNeg.z()).endVertex();
        buffer.vertex(matrix4f, minX, minY - 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYNeg.x(), normalYNeg.y(), normalYNeg.z()).endVertex();

        buffer.vertex(matrix4f, minX, minY + 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYPos.x(), normalYPos.y(), normalYPos.z()).endVertex();
        buffer.vertex(matrix4f, minX, minY + 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYPos.x(), normalYPos.y(), normalYPos.z()).endVertex();
        buffer.vertex(matrix4f, maxX, minY + 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYPos.x(), normalYPos.y(), normalYPos.z()).endVertex();
        buffer.vertex(matrix4f, maxX, minY + 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYPos.x(), normalYPos.y(), normalYPos.z()).endVertex();

        buffer.vertex(matrix4f, minX, maxY + 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYPos.x(), normalYPos.y(), normalYPos.z()).endVertex();
        buffer.vertex(matrix4f, minX, maxY + 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYPos.x(), normalYPos.y(), normalYPos.z()).endVertex();
        buffer.vertex(matrix4f, maxX, maxY + 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYPos.x(), normalYPos.y(), normalYPos.z()).endVertex();
        buffer.vertex(matrix4f, maxX, maxY + 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYPos.x(), normalYPos.y(), normalYPos.z()).endVertex();

        buffer.vertex(matrix4f, maxX, maxY - 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYNeg.x(), normalYNeg.y(), normalYNeg.z()).endVertex();
        buffer.vertex(matrix4f, maxX, maxY - 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYNeg.x(), normalYNeg.y(), normalYNeg.z()).endVertex();
        buffer.vertex(matrix4f, minX, maxY - 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYNeg.x(), normalYNeg.y(), normalYNeg.z()).endVertex();
        buffer.vertex(matrix4f, minX, maxY - 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(normalYNeg.x(), normalYNeg.y(), normalYNeg.z()).endVertex();
    }

    private static void renderPedestalsHUD(PoseStack matrixStack, MultiBufferSource buffer, List<String> messages, float x, int angleY) {
        matrixStack.pushPose();
        matrixStack.translate(x, (float) 2.25, (float) 0.5);
        float f3 = 0.025F;
        matrixStack.scale(f3, f3, f3);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(angleY));

        Font fontrenderer = Minecraft.getInstance().font;
        int logsize = messages.size();
        int lines = Math.min(11, logsize);
        int currenty = 7;
        int height = 10;

        StringBuilder concatenatedMessages = new StringBuilder();
        int i = 0;
        for (String s : messages) {
            if (i >= logsize - lines) {
                if (currenty + height <= 124) {
                    String prefix = "";
                    concatenatedMessages.append(prefix).append(s).append('\n');
                    currenty += height;
                }
            }
            i++;
        }

        fontrenderer.drawInBatch(concatenatedMessages.toString(), 7, 7, 0xffffff, false, matrixStack.last().pose(), buffer, false, 0, 0xf000f0);

        matrixStack.popPose();
    }

    //This allows the pedestal render boxes to render when the pedestal isnt within normal render range
    @Override
    public boolean shouldRenderOffScreen(BasePedestalBlockEntity blockEntity) {
        return blockEntity.getRenderRange() || blockEntity.getRenderRangeUpgrade();
    }

    @Override
    public boolean shouldRender(BasePedestalBlockEntity blockEntity, @NotNull Vec3 playerPos) {
        Vec3 blockPos = Vec3.atCenterOf(blockEntity.getBlockPos());
        int renderDistance = PedestalConfig.CLIENT.pedestalRenderDistance.get();
        return blockPos.closerThan(playerPos, renderDistance); //Only render pedestals in x blocks radius
    }

}
