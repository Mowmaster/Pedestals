package com.mowmaster.pedestals.Blocks.Pedestal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mowmaster.pedestals.Items.ISelectablePoints;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock.FACING;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class BasePedestalBlockEntityRenderer implements BlockEntityRenderer<BasePedestalBlockEntity>
{

    public BasePedestalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(BasePedestalBlockEntity p_112307_, float p_112308_, PoseStack p_112309_, MultiBufferSource p_112310_, int p_112311_, int p_112312_) {
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
                    //renderTile(world,p_112309_,p_112310_,coin,stack,p_112311_,p_112312_,renderAugmentType);
                    //renderTile(world,p_112309_,p_112310_,stack,coin,p_112311_,p_112312_,renderAugmentType);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                }
                if(facing== Direction.DOWN) {
                    //p_112309_.rotate(new Quaternion(0, 0, 1,180));
                    p_112309_.mulPose(Vector3f.ZP.rotationDegrees(180));
                    p_112309_.translate(0, -1, 0);
                    p_112309_.translate(-1, 0, 0);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                    //renderTile(world,p_112309_,p_112310_,stack,coin,p_112311_,p_112312_,renderAugmentType);
                    //renderTile(world,p_112309_,p_112310_,coin,stack,p_112311_,p_112312_,renderAugmentType);
                }
                if(facing== Direction.NORTH) {
                    //p_112309_.rotate(new Quaternion(1, 0, 0,270));
                    p_112309_.mulPose(Vector3f.XP.rotationDegrees(270));
                    p_112309_.translate(0, -1, 0);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                    //renderTile(world,p_112309_,p_112310_,stack,coin,p_112311_,p_112312_,renderAugmentType);
                    //renderTile(world,p_112309_,p_112310_,coin,stack,p_112311_,p_112312_,renderAugmentType);
                }
                if(facing== Direction.EAST) {
                    //p_112309_.mulPose(270, 0, 0, 1);
                    p_112309_.mulPose(Vector3f.ZP.rotationDegrees(270));
                    p_112309_.translate(-1, 0, 0);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                    //renderTile(world,p_112309_,p_112310_,stack,coin,p_112311_,p_112312_,renderAugmentType);
                    //renderTile(world,p_112309_,p_112310_,coin,stack,p_112311_,p_112312_,renderAugmentType);
                }
                if(facing== Direction.SOUTH) {
                    //p_112309_.mulPose(90, 1, 0, 0);
                    p_112309_.mulPose(Vector3f.XP.rotationDegrees(90));
                    p_112309_.translate(0, 0, -1);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                    //renderTile(world,p_112309_,p_112310_,stack,coin,p_112311_,p_112312_,renderAugmentType);
                    //renderTile(world,p_112309_,p_112310_,coin,stack,p_112311_,p_112312_,renderAugmentType);
                }
                if(facing== Direction.WEST) {
                    //p_112309_.mulPose(90, 0, 0, 1);
                    p_112309_.mulPose(Vector3f.ZP.rotationDegrees(90));
                    p_112309_.translate(0, -1, 0);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                    //renderTile(world,p_112309_,p_112310_,stack,coin,p_112311_,p_112312_,renderAugmentType);
                    //renderTile(world,p_112309_,p_112310_,coin,stack,p_112311_,p_112312_,renderAugmentType);
                }
            }
        }
    }
    //public static void  renderTile(Level worldIn, PoseStack p_112309_, MultiBufferSource p_112310_, ItemStack coin, ItemStack item, int p_112311_, int p_112312_, int renderAugmentType)
    public static void  renderTile(Level worldIn, PoseStack p_112309_, MultiBufferSource p_112310_, ItemStack item, ItemStack coin, int p_112311_, int p_112312_, int renderAugmentType)
    {
        switch (renderAugmentType)
        {
            case 0:
                renderItemRotating(worldIn,p_112309_,p_112310_,item,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.3125f,0,0,0,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.3125f,0.475f,0.5f,90,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.6875f,180,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.6875f,0.475f,0.5f,270,0,1f,0,p_112311_,p_112312_);
            break;
            case 1:
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.3125f,0,0,0,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.3125f,0.475f,0.5f,90,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.6875f,180,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.6875f,0.475f,0.5f,270,0,1f,0,p_112311_,p_112312_);
                break;
            case 2:
                renderItemRotating(worldIn,p_112309_,p_112310_,item,p_112311_,p_112312_);
                break;
            case 3:
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.3125f,0,0,0,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.3125f,0.475f,0.5f,90,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.6875f,180,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.6875f,0.475f,0.5f,270,0,1f,0,p_112311_,p_112312_);
                break;
            case 4:
                renderItemRotating(worldIn,p_112309_,p_112310_,item,p_112311_,p_112312_);
                break;
            case 5: break;
            case 6: break;
            case 7:
                renderItemRotating(worldIn,p_112309_,p_112310_,item,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.3125f,0,0,0,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.3125f,0.475f,0.5f,90,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.6875f,180,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.6875f,0.475f,0.5f,270,0,1f,0,p_112311_,p_112312_);
                break;
            default:
                renderItemRotating(worldIn,p_112309_,p_112310_,item,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.3125f,0,0,0,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.3125f,0.475f,0.5f,90,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.6875f,180,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.6875f,0.475f,0.5f,270,0,1f,0,p_112311_,p_112312_);
                break;

        }
    }

    public static void  renderTileItems(Level worldIn, PoseStack p_112309_, MultiBufferSource p_112310_, List<ItemStack> item, ItemStack coin,ItemStack toolStack, int p_112311_, int p_112312_, int renderAugmentType, List<String> messages)
    {
        switch (renderAugmentType)
        {
            case 0:
                renderItemsRotating(worldIn,p_112309_,p_112310_,item,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.3125f,0,0,0,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.3125f,0.475f,0.5f,90,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.6875f,180,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.6875f,0.475f,0.5f,270,0,1f,0,p_112311_,p_112312_);
                renderTool(worldIn,toolStack,p_112309_,p_112310_,p_112311_,p_112312_);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,-0.75F, 2.25F, 0.5F, 180,0);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,1.5F, 2.25F, 0.5F, 180,180);
                break;
            case 1:
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.3125f,0,0,0,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.3125f,0.475f,0.5f,90,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.6875f,180,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.6875f,0.475f,0.5f,270,0,1f,0,p_112311_,p_112312_);
                renderTool(worldIn,toolStack,p_112309_,p_112310_,p_112311_,p_112312_);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,-0.75F, 2.25F, 0.5F, 180,0);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,1.5F, 2.25F, 0.5F, 180,180);
                break;
            case 2:
                renderItemsRotating(worldIn,p_112309_,p_112310_,item,p_112311_,p_112312_);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,-0.75F, 2.25F, 0.5F, 180,0);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,1.5F, 2.25F, 0.5F, 180,180);
                break;
            case 3:
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.3125f,0,0,0,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.3125f,0.475f,0.5f,90,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.6875f,180,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.6875f,0.475f,0.5f,270,0,1f,0,p_112311_,p_112312_);
                renderTool(worldIn,toolStack,p_112309_,p_112310_,p_112311_,p_112312_);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,-0.75F, 2.25F, 0.5F, 180,0);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,1.5F, 2.25F, 0.5F, 180,180);
                break;
            case 4:
                renderItemsRotating(worldIn,p_112309_,p_112310_,item,p_112311_,p_112312_);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,-0.75F, 2.25F, 0.5F, 180,0);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,1.5F, 2.25F, 0.5F, 180,180);
                break;
            case 5: break;
            case 6: break;
            case 7:
                renderItemsRotating(worldIn,p_112309_,p_112310_,item,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.3125f,0,0,0,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.3125f,0.475f,0.5f,90,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.6875f,180,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.6875f,0.475f,0.5f,270,0,1f,0,p_112311_,p_112312_);
                renderTool(worldIn,toolStack,p_112309_,p_112310_,p_112311_,p_112312_);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,-0.75F, 2.25F, 0.5F, 180,0);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,1.5F, 2.25F, 0.5F, 180,180);
                break;
            default:
                renderItemsRotating(worldIn,p_112309_,p_112310_,item,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.3125f,0,0,0,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.3125f,0.475f,0.5f,90,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.5f,0.475f,0.6875f,180,0,1f,0,p_112311_,p_112312_);
                renderCoin(worldIn,coin,p_112309_,p_112310_,0.6875f,0.475f,0.5f,270,0,1f,0,p_112311_,p_112312_);
                renderTool(worldIn,toolStack,p_112309_,p_112310_,p_112311_,p_112312_);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,-0.75F, 2.25F, 0.5F, 180,0);
                if(messages.size()>0)renderPedestalsHUD(p_112309_,p_112310_,messages,1.5F, 2.25F, 0.5F, 180,180);
                break;

        }
    }

    public static void renderCoin(Level worldIn,ItemStack itemCoin, PoseStack p_112309_, MultiBufferSource p_112310_, float x, float y, float z, float angle, float xr, float yr, float zr, int p_112311_, int p_112312_) {
        if (!itemCoin.isEmpty()) {
            p_112309_.pushPose();
            p_112309_.translate(x, y, z);
            p_112309_.scale(0.1875f, 0.1875f, 0.1875f);
            p_112309_.mulPose(Vector3f.YP.rotationDegrees(angle));
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel baked = renderer.getModel(itemCoin,worldIn,null,0);
            renderer.render(itemCoin,ItemTransforms.TransformType.FIXED,true,p_112309_,p_112310_,p_112311_,p_112312_,baked);
            //Minecraft.getInstance().getItemRenderer().renderItem(itemCoin, ItemCameraTransforms.TransformType.FIXED, p_112311_, p_112312_, p_112309_, p_112310_);
            p_112309_.popPose();
        }
    }

    public static void renderTool(Level worldIn,ItemStack itemTool, PoseStack p_112309_, MultiBufferSource p_112310_, int p_112311_, int p_112312_) {
        if (!itemTool.isEmpty()) {
            p_112309_.pushPose();
            p_112309_.translate(0.5D, 0.75D, 0.5D);
            p_112309_.scale(0.5F, 0.5F, 0.5F);
            p_112309_.mulPose(Vector3f.YP.rotationDegrees(90));
            p_112309_.mulPose(Vector3f.XP.rotationDegrees(90));
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel baked = renderer.getModel(itemTool,worldIn,null,0);
            renderer.render(itemTool,ItemTransforms.TransformType.FIXED,true,p_112309_,p_112310_,p_112311_,p_112312_,baked);
            //Minecraft.getInstance().getItemRenderer().renderItem(itemCoin, ItemCameraTransforms.TransformType.FIXED, p_112311_, p_112312_, p_112309_, p_112310_);
            p_112309_.popPose();
        }
    }


    //This allows the pedestal render boxes to render when the pedestal isnt within normal render range
    @Override
    public boolean shouldRenderOffScreen(BasePedestalBlockEntity p_112306_) {
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
}
