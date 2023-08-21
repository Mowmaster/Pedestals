package com.mowmaster.pedestals.Blocks.Pedestal.GoblinStatuePedestal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.ISelectablePoints;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardArea;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock.FACING;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class GoblinStatuePedestalBlockEntityRenderer implements BlockEntityRenderer<GoblinStatuePedestalBlockEntity>
{
    public GoblinStatuePedestalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(GoblinStatuePedestalBlockEntity p_112307_, float p_112308_, PoseStack p_112309_, MultiBufferSource p_112310_, int p_112311_, int p_112312_) {
        if (!p_112307_.isRemoved()) {
            Direction facing = p_112307_.getBlockState().getValue(FACING);
            List<ItemStack> listed = p_112307_.getItemStacks();
            ItemStack stack = p_112307_.getItemInPedestalFirst();
            ItemStack toolStack = p_112307_.getToolStack();
            ItemStack coin = p_112307_.getCoinOnPedestal();
            ItemStack workCard = p_112307_.getWorkCardInPedestal();
            List<BlockPos> linkedLocations = p_112307_.getLinkedLocations();
            BlockPos pos = p_112307_.getPos();
            Level world = p_112307_.getLevel();
            List<String> hudMessages = p_112307_.getHudLog();
            // 0 - No Particles
            // 1 - No Render Item
            // 2 - No Render Upgrade
            // 3 - No Particles/No Render Item
            // 4 - No Particles/No Render Upgrade
            // 5 - No Render Item/No Render Upgrade
            // 6 - No Particles/No Render Item/No Render Upgrade
            // 7 - No Augment exists and thus all rendering is fine.
            int renderAugmentType = p_112307_.getRendererType();

            if(p_112307_.getRenderRange())
            {
                int range = p_112307_.getLinkingRange();
                //You have to client register this too!!!
                @SuppressWarnings("deprecation")
                TextureAtlasSprite whiteTextureSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation(MODID, "util/pedestal_render"));
                AABB aabb = new AABB(pos.getX() - range, pos.getY() - range, pos.getZ() - range,pos.getX() + range + 1, pos.getY() + range + 1, pos.getZ() + range + 1);
                p_112309_.pushPose();
                renderBoundingBox(pos, aabb, p_112309_, p_112310_.getBuffer(RenderType.lines()), p_112307_, 1f, 0.2f, 0.2f, 1f);
                renderFaces(whiteTextureSprite,pos,aabb,p_112309_, p_112310_.getBuffer(Sheets.translucentCullBlockSheet()), p_112307_, 1f, 0.2f, 0.2f, 0.5f);
                p_112309_.popPose();
                //Orange Color = 1f, 0.42f, 0f,
                //Light Blue Color = 0f, 0.58f, 1f,

                if(linkedLocations.size()>0)
                {
                    List<BlockPos> resetList = new ArrayList<>();
                    //resetList = linkedLocations;
                    p_112309_.pushPose();
                    for (BlockPos posPoints : linkedLocations)
                    //for(int i=0;i<linkedLocations.size();i++)
                    {
                        if(resetList.contains(posPoints))
                        {
                            continue;
                        }
                        else
                        {
                            //AABB aabbCoin = new AABB(linkedLocations.get(i));
                            AABB aabbCoin = new AABB(posPoints);
                            if(aabbCoin != new AABB(BlockPos.ZERO))
                            {
                                //You have to client register this too!!!
                                @SuppressWarnings("deprecation")
                                TextureAtlasSprite upgradeTextureSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation(MODID, "util/pedestal_render_" + (linkedLocations.indexOf(posPoints)+1) + ""));
                                renderBoundingBox(pos, aabbCoin, p_112309_, p_112310_.getBuffer(RenderType.lines()), p_112307_, 0.0f,0.0f, 1f, 1f);
                                renderFaces(upgradeTextureSprite,pos,aabbCoin,p_112309_, p_112310_.getBuffer(Sheets.translucentCullBlockSheet()), p_112307_, 0.0f, 0.0f, 1f, 0.5f);
                                resetList.add(posPoints);
                            }
                        }
                    }
                    p_112309_.popPose();
                }
            }
            else
            {
                linkedLocations.clear();
            }

            if(p_112307_.getRenderRangeUpgrade() && !coin.isEmpty())
            {
                int range = 0;
                if(coin.getItem() instanceof  ItemUpgradeBase upgrade)
                {
                    range = upgrade.getUpgradeWorkRange(coin);
                }

                if(range > 0)
                {
                    //You have to client register this too!!!
                    @SuppressWarnings("deprecation")
                    TextureAtlasSprite whiteTextureSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation(MODID, "util/pedestal_render"));
                    AABB aabb = new AABB(pos.getX() - range, pos.getY() - range, pos.getZ() - range,pos.getX() + range + 1, pos.getY() + range + 1, pos.getZ() + range + 1);
                    p_112309_.pushPose();
                    renderBoundingBox(pos, aabb, p_112309_, p_112310_.getBuffer(RenderType.lines()), p_112307_, 0.2f, 0.2f, 1f, 1f);
                    renderFaces(whiteTextureSprite,pos,aabb,p_112309_, p_112310_.getBuffer(Sheets.translucentCullBlockSheet()), p_112307_, 0.2f, 0.2f, 1f, 0.5f);
                    p_112309_.popPose();


                    if (workCard.getItem() instanceof WorkCardArea workCardArea) {
                        p_112309_.pushPose();
                        WorkCardArea.getAABBIfDefined(workCard).ifPresent(workCardAABB -> {
                            AABB expandedWorkCardAABB = workCardAABB.expandTowards(1.0D, 1.0D, 1.0D); // rendering requires expansion over the actual points.
                            boolean inSelectedInRange = workCardArea.selectedAreaWithinRange(p_112307_);
                            //You have to client register this too!!!
                            @SuppressWarnings("deprecation")
                            TextureAtlasSprite upgradeTextureSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation(MODID, "util/upgrade_render"));
                            renderBoundingBox(pos, expandedWorkCardAABB, p_112309_, p_112310_.getBuffer(RenderType.lines()), p_112307_, (inSelectedInRange)?(0.0f):(1f), (inSelectedInRange)?(1f):(0.0f), 0.0f, 1f);
                            renderFaces(upgradeTextureSprite,pos, expandedWorkCardAABB, p_112309_, p_112310_.getBuffer(Sheets.translucentCullBlockSheet()), p_112307_, (inSelectedInRange)?(0.0f):(1f), (inSelectedInRange)?(1f):(0.0f), 0.0f, 0.5f);
                        });
                        p_112309_.popPose();
                    }

                    if(workCard.getItem() instanceof ISelectablePoints)
                    {
                        if(workCard.getItem() instanceof WorkCardBase cardBase)
                        {
                            if(!cardBase.hasTwoPointsSelected(workCard))
                            {
                                p_112309_.pushPose();
                                List<BlockPos> locations = cardBase.readBlockPosListFromNBT(workCard);
                                if(locations.size()>0)
                                {
                                    for (BlockPos posPoints : locations) {
                                        AABB aabbCoin = new AABB(posPoints);
                                        if(aabbCoin != new AABB(BlockPos.ZERO))
                                        {
                                            Boolean inSelectedInRange = cardBase.selectedPointWithinRange(p_112307_,posPoints);
                                            //You have to client register this too!!!
                                            @SuppressWarnings("deprecation")
                                            TextureAtlasSprite upgradeTextureSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation(MODID, "util/upgrade_render"));
                                            renderBoundingBox(pos, aabbCoin, p_112309_, p_112310_.getBuffer(RenderType.lines()), p_112307_, (inSelectedInRange)?(0.0f):(1f), (inSelectedInRange)?(1f):(0.0f), 0.0f, 1f);
                                            renderFaces(upgradeTextureSprite,pos,aabbCoin,p_112309_, p_112310_.getBuffer(Sheets.translucentCullBlockSheet()), p_112307_, (inSelectedInRange)?(0.0f):(1f), (inSelectedInRange)?(1f):(0.0f), 0.0f, 0.5f);
                                        }
                                    }
                                }
                                p_112309_.popPose();
                            }
                        }
                    }
                }
            }



            if(renderAugmentType !=6)
            {
                if(facing== Direction.UP)//when placed on ground
                {
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                }
                if(facing== Direction.DOWN) {
                    p_112309_.mulPose(Vector3f.YP.rotationDegrees(180));
                    p_112309_.translate(-1, 0, -1);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                }
                if(facing== Direction.NORTH) {
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                }
                if(facing== Direction.EAST) {
                    p_112309_.mulPose(Vector3f.YP.rotationDegrees(270));
                    p_112309_.translate(0, 0, -1);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                }
                if(facing== Direction.SOUTH) {
                    p_112309_.mulPose(Vector3f.YP.rotationDegrees(180));
                    p_112309_.translate(-1, 0, -1);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                }
                if(facing== Direction.WEST) {
                    p_112309_.mulPose(Vector3f.YP.rotationDegrees(90));
                    p_112309_.translate(-1, 0, 0);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                }
            }
        }
    }

    private static void renderCoinAndTool(Level worldIn, PoseStack poseStack, MultiBufferSource multiBufferSource, ItemStack coin, ItemStack toolStack, int p_112311_, int p_112312_) {
        boolean renderUpgrades = PedestalConfig.CLIENT.pedestalRenderUpgrades.get();
        boolean renderUpgradesInToolSlot = PedestalConfig.CLIENT.pedestalRenderUpgradeInToolSlot.get();
        if (renderUpgradesInToolSlot) {
            if (renderUpgrades) {
                renderTool(worldIn, coin, poseStack, multiBufferSource, p_112311_, p_112312_);
            }
        } else {
            if (renderUpgrades) {
                renderCoin(worldIn, coin, poseStack, multiBufferSource, p_112311_, p_112312_);
                //renderCoinTwo(worldIn, coin, poseStack, multiBufferSource, p_112311_, p_112312_);
            }
            boolean renderToolSlot = PedestalConfig.CLIENT.pedestalRenderToolSlot.get();
            if (renderToolSlot) {
                renderTool(worldIn, toolStack, poseStack, multiBufferSource, p_112311_, p_112312_);
            }
        }
    }

    private static void renderHUD(PoseStack poseStack, MultiBufferSource multiBufferSource, List<String> messages) {
        boolean renderHUD = PedestalConfig.CLIENT.pedestalRenderHUD.get();
        if (renderHUD) {
            if (messages.size() > 0) {
                renderPedestalsHUD(poseStack, multiBufferSource, messages, -0.75F, 2.25F, 0.5F, 180, 0);
                renderPedestalsHUD(poseStack, multiBufferSource, messages, 1.5F, 2.25F, 0.5F, 180, 180);
            }
        }
    }

    public static void renderTileItems(Level worldIn, PoseStack poseStack, MultiBufferSource multiBufferSource, List<ItemStack> item, ItemStack coin, ItemStack toolStack, int p_112311_, int p_112312_, int renderAugmentType, List<String> messages) {
        switch (renderAugmentType) {
            case 1, 3:
                renderCoinAndTool(worldIn, poseStack, multiBufferSource, coin, toolStack, p_112311_, p_112312_);
                renderHUD(poseStack, multiBufferSource, messages);
                break;
            case 2, 4:
                renderItemsRotating(worldIn, poseStack, multiBufferSource, item, p_112311_, p_112312_);
                renderHUD(poseStack, multiBufferSource, messages);
                break;
            case 5, 6: break;
            default: // 0, 7
                renderItemsRotating(worldIn, poseStack, multiBufferSource, item, p_112311_, p_112312_);
                renderCoinAndTool(worldIn, poseStack, multiBufferSource, coin, toolStack, p_112311_, p_112312_);
                renderHUD(poseStack, multiBufferSource, messages);
                break;

        }
    }

    private static void renderItemsRotating(Level worldIn, PoseStack posStack, MultiBufferSource buffers, List<ItemStack> listed, int light, int overlay) {
        boolean renderItems = PedestalConfig.CLIENT.pedestalRenderItems.get();
        if (renderItems) {
            //https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/client/render/tile/RenderTileRuneAltar.java#L49
            int stacks = listed.size();
            boolean rotateEnabled = PedestalConfig.CLIENT.pedestalRotateItems.get();
            if (stacks > 1) {
                posStack.pushPose();
                double time = worldIn.getGameTime();

                float[] angles = new float[stacks];

                float anglePer = 360F / stacks;
                for (int i = 0; i < angles.length; i++) {
                    angles[i] = anglePer * i + (rotateEnabled ? (float) time : 0);
                }

                float sized = 1.25F;
                float sizedd = 0.25F;
                if (stacks <= 4) {
                    sized = 0.25F;
                    sizedd = 0.05F;
                }
                if (stacks <= 8 && stacks > 4) {
                    sized = 0.5F;
                    sizedd = 0.10F;
                }
                if (stacks <= 12 && stacks > 8) {
                    sized = 0.75F;
                    sizedd = 0.15F;
                }
                if (stacks <= 16 && stacks > 12) {
                    sized = 1.0F;
                    sizedd = 0.20F;
                }

                for (int i = 0; i < stacks; i++) {
                    posStack.pushPose();
                    posStack.translate(0.5F, 1.0F, 0.5F);
                    posStack.mulPose(Vector3f.YP.rotationDegrees(angles[i]));
                    posStack.translate(sized, 0F, sizedd);
                    posStack.mulPose(Vector3f.YP.rotationDegrees(90F));
                    if (rotateEnabled) {
                        posStack.translate(0D, 0.075 * Math.sin((time + i * 10) / 5D), 0F);
                    }
                    ItemStack stack = listed.get(i);
                    Minecraft mc = Minecraft.getInstance();
                    if (!stack.isEmpty()) {
                        mc.getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GROUND,
                                light, overlay, posStack, buffers, 0);
                    }
                    posStack.popPose();
                }

                posStack.popPose();
            } else if (stacks == 1) {
                posStack.pushPose();
                if(listed.get(0).getItem() instanceof BlockItem)
                {
                    posStack.translate(0.5D, 0.25D, 0.20D);
                    posStack.scale(0.5F, 0.5F, 0.5F);
                    posStack.mulPose(Vector3f.XP.rotationDegrees(-22));
                }
                else
                {
                    posStack.translate(0.5D, 0.30D, 0.30D);
                    posStack.scale(0.30F, 0.30F, 0.30F);
                    posStack.mulPose(Vector3f.ZP.rotationDegrees(22));
                }
                ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
                BakedModel baked = renderer.getModel(listed.get(0), worldIn, null, 0);
                renderer.render(listed.get(0), ItemTransforms.TransformType.FIXED, true, posStack, buffers, light, overlay, baked);

                posStack.popPose();
            }
        }
    }

    private static final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    private static BakedModel cachedModel = null;

    public static void renderCoin(Level worldIn, ItemStack itemCoin, PoseStack poseStack, MultiBufferSource bufferSource, int overlay, int light) {
        if (!itemCoin.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.8625f, 0.5f);
            poseStack.scale(0.25f, 0.25f, 0.25f);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));

            /*
            Earring
            poseStack.translate(0.125f, 0.65f, 0.63f);
            poseStack.scale(0.1f, 0.1f, 0.1f);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(22));

            --OnHead--
            poseStack.translate(0.5f, 0.8625f, 0.5f);
            poseStack.scale(0.25f, 0.25f, 0.25f);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));

            OnBack
            poseStack.translate(0.5f, 0.35f, 0.65f);
            poseStack.scale(0.15f, 0.15f, 0.15f);

            OnGround
            poseStack.translate(0.5f, -0.1f, 0.5f);
            poseStack.scale(0.95f, 0.95f, 0.95f);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
    */

            // Check if the coin model is already cached
            if (cachedModel == null || cachedModel != itemRenderer.getModel(itemCoin, worldIn, null, 0)) {
                cachedModel = itemRenderer.getModel(itemCoin, worldIn, null, 0);
            }

            // Render the coin using the current pose stack
            itemRenderer.render(itemCoin, ItemTransforms.TransformType.FIXED, true, poseStack, bufferSource, overlay, light, cachedModel);

            poseStack.popPose();
        }
    }

    /*public static void renderCoinTwo(Level worldIn, ItemStack itemCoin, PoseStack poseStack, MultiBufferSource bufferSource, int overlay, int light) {
        if (!itemCoin.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.875f, 0.65f, 0.63f);
            poseStack.scale(0.1f, 0.1f, 0.1f);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-22));

            // Check if the coin model is already cached
            if (cachedModel == null || cachedModel != itemRenderer.getModel(itemCoin, worldIn, null, 0)) {
                cachedModel = itemRenderer.getModel(itemCoin, worldIn, null, 0);
            }

            // Render the coin using the current pose stack
            itemRenderer.render(itemCoin, ItemTransforms.TransformType.FIXED, true, poseStack, bufferSource, overlay, light, cachedModel);

            poseStack.popPose();
        }
    }*/

    public static void renderTool(Level worldIn,ItemStack itemTool, PoseStack p_112309_, MultiBufferSource p_112310_, int p_112311_, int p_112312_) {
        p_112309_.pushPose();
        p_112309_.translate(0.6875D, 0.4D, 0.225D);
        p_112309_.scale(0.25F, 0.25F, 0.25F);
        p_112309_.mulPose(Vector3f.YP.rotationDegrees(90));
        p_112309_.mulPose(Vector3f.ZP.rotationDegrees(270));
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        BakedModel baked = renderer.getModel(itemTool,worldIn,null,0);
        renderer.render(itemTool,ItemTransforms.TransformType.FIXED,true,p_112309_,p_112310_,p_112311_,p_112312_,baked);
        //Minecraft.getInstance().getItemRenderer().renderItem(itemCoin, ItemCameraTransforms.TransformType.FIXED, p_112311_, p_112312_, p_112309_, p_112310_);
        p_112309_.popPose();
    }




    //https://github.com/StanCEmpire/RegionProtection/blob/1.18.x/src/main/java/stancempire/stancempiresregionprotection/blockentities/RegionBlockBER.java
    public void renderBoundingBox(BlockPos pos, AABB aabb, PoseStack matrixStack, VertexConsumer buffer, GoblinStatuePedestalBlockEntity blockEntity, float red, float green, float blue, float alpha)
    {
        Matrix4f matrix4f = matrixStack.last().pose();
        Matrix3f matrix3f = matrixStack.last().normal();

        float minX = (float)(aabb.minX - pos.getX());
        float minY = (float)(aabb.minY - pos.getY());
        float minZ = (float)(aabb.minZ - pos.getZ());

        float maxX = (float)(aabb.maxX - pos.getX());
        float maxY = (float)(aabb.maxY - pos.getY());
        float maxZ = (float)(aabb.maxZ - pos.getZ());

        //Bottom
        buffer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();

        buffer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();

        buffer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();
        buffer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();

        buffer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();

        //Top
        buffer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();

        buffer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();

        buffer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();
        buffer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();

        buffer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();

        //Sides
        buffer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, -1).endVertex();
        buffer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, -1).endVertex();

        buffer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, 1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, 1).endVertex();

        buffer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, 1).endVertex();
        buffer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, 1).endVertex();

        buffer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, -1).endVertex();

    }

    public void renderFaces(TextureAtlasSprite sprite, BlockPos pos, AABB aabb, PoseStack matrixStack, VertexConsumer buffer, GoblinStatuePedestalBlockEntity blockEntity, float red, float green, float blue, float alpha)
    {
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

        //West
        buffer.vertex(matrix4f, minX - 0.01f, minY, maxZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX - 0.01f, maxY, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX - 0.01f, maxY, minZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX - 0.01f, minY, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();

        buffer.vertex(matrix4f, minX + 0.01f, minY, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX + 0.01f, maxY, minZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX + 0.01f, maxY, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX + 0.01f, minY, maxZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();


        //East
        buffer.vertex(matrix4f, maxX + 0.01f, minY, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX + 0.01f, maxY, minZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX + 0.01f, maxY, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX + 0.01f, minY, maxZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();

        buffer.vertex(matrix4f, maxX - 0.01f, minY, maxZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX - 0.01f, maxY, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX - 0.01f, maxY, minZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX - 0.01f, minY, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();


        //North
        buffer.vertex(matrix4f, minX, maxY, minZ - 0.01f).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ - 0.01f).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, minY, minZ - 0.01f).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, minX, minY, minZ - 0.01f).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();

        buffer.vertex(matrix4f, minX, minY, minZ + 0.01f).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, maxX, minY, minZ + 0.01f).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ + 0.01f).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, minX, maxY, minZ + 0.01f).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();


        //South
        buffer.vertex(matrix4f, minX, minY, maxZ + 0.01f).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, maxX, minY, maxZ + 0.01f).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ + 0.01f).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, minX, maxY, maxZ + 0.01f).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();

        buffer.vertex(matrix4f, minX, maxY, maxZ - 0.01f).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ - 0.01f).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, minY, maxZ - 0.01f).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, minX, minY, maxZ - 0.01f).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();


        //Bottom
        buffer.vertex(matrix4f, maxX, minY - 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY - 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, minX, minY - 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, minX, minY - 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();

        buffer.vertex(matrix4f, minX, minY + 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, minX, minY + 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY + 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY + 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();


        //Top
        buffer.vertex(matrix4f, minX, maxY + 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, minX, maxY + 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY + 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY + 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();

        buffer.vertex(matrix4f, maxX, maxY - 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY - 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, minX, maxY - 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, minX, maxY - 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
    }

    private static void renderPedestalsHUD(PoseStack matrixStack, MultiBufferSource buffer, List<String> messages, float x, float y, float z, int angleX, int angleY) {


        matrixStack.pushPose();
        matrixStack.translate(x,y,z);
        //float f3 = 0.0075F;
        float f3 = 0.025F;
        matrixStack.scale(f3 , f3 , f3);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(angleX));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(angleY));

        Font fontrenderer = Minecraft.getInstance().font;
        int lines = 11;
        int currenty = 7;
        int height = 10;
        int logsize = messages.size();
        int i = 0;
        for (String s : messages) {
            if (i >= logsize - lines) {
                if (currenty + height <= 124) {
                    String prefix = "";

                    fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(prefix + s, 115), 7, currenty, 0xffffff, false, matrixStack.last().pose(), buffer, false, 0, 0xf000f0);
                    currenty += height;
                }
            }
            i++;
        }


        matrixStack.popPose();
    }


    //This allows the pedestal render boxes to render when the pedestal isnt within normal render range
    @Override
    public boolean shouldRenderOffScreen(GoblinStatuePedestalBlockEntity p_112306_) {
        //BlockEntityRenderer.super.shouldRenderOffScreen(p_112306_)
        if(p_112306_.getRenderRange() || p_112306_.getRenderRangeUpgrade())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean shouldRender(GoblinStatuePedestalBlockEntity blockEntity, @NotNull Vec3 playerPos) {
        Vec3 blockPos = Vec3.atCenterOf(blockEntity.getBlockPos());
        int renderDistance = PedestalConfig.CLIENT.pedestalRenderDistance.get();
        return blockPos.closerThan(playerPos, renderDistance); //Only render pedestals in x blocks radius
    }

}
