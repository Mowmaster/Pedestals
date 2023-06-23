package com.mowmaster.pedestals.Blocks.Pedestal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardArea;
import com.mowmaster.mowlib.api.DefineLocations.ISelectablePoints;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
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
                            boolean inSelectedInRange = selectedAreaWithinRange(p_112307_, ((ItemUpgradeBase)coin.getItem()).getUpgradeWorkRange(coin));
                            //You have to client register this too!!!
                            TextureAtlasSprite upgradeTextureSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation(MODID, "util/upgrade_render"));
                            renderBoundingBox(pos, expandedWorkCardAABB, p_112309_, p_112310_.getBuffer(RenderType.lines()), p_112307_, (inSelectedInRange)?(0.0f):(1f), (inSelectedInRange)?(1f):(0.0f), 0.0f, 1f);
                            renderFaces(upgradeTextureSprite,pos, expandedWorkCardAABB, p_112309_, p_112310_.getBuffer(Sheets.translucentCullBlockSheet()), p_112307_, (inSelectedInRange)?(0.0f):(1f), (inSelectedInRange)?(1f):(0.0f), 0.0f, 0.5f);
                        });
                        p_112309_.popPose();
                    }

                    if(workCard.getItem() instanceof ISelectablePoints)
                    {
                        if(!hasTwoPointsSelected(workCard))
                        {
                            p_112309_.pushPose();
                            List<BlockPos> locations = readBlockPosListFromNBT(workCard);
                            if(locations.size()>0)
                            {
                                for (BlockPos posPoints : locations) {
                                    AABB aabbCoin = new AABB(posPoints);
                                    if(aabbCoin != new AABB(BlockPos.ZERO))
                                    {
                                        Boolean inSelectedInRange = selectedPointWithinRange(p_112307_,posPoints,((ItemUpgradeBase)coin.getItem()).getUpgradeWorkRange(coin));
                                        //You have to client register this too!!!
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


                    p_112309_.mulPose(Axis.ZP.rotationDegrees(180));
                    p_112309_.translate(0, -1, 0);
                    p_112309_.translate(-1, 0, 0);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                    //renderTile(world,p_112309_,p_112310_,stack,coin,p_112311_,p_112312_,renderAugmentType);
                    //renderTile(world,p_112309_,p_112310_,coin,stack,p_112311_,p_112312_,renderAugmentType);
                }
                if(facing== Direction.NORTH) {
                    //p_112309_.rotate(new Quaternion(1, 0, 0,270));

                    p_112309_.mulPose(Axis.XP.rotationDegrees(270));
                    p_112309_.translate(0, -1, 0);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                    //renderTile(world,p_112309_,p_112310_,stack,coin,p_112311_,p_112312_,renderAugmentType);
                    //renderTile(world,p_112309_,p_112310_,coin,stack,p_112311_,p_112312_,renderAugmentType);
                }
                if(facing== Direction.EAST) {
                    //p_112309_.mulPose(270, 0, 0, 1);
                    p_112309_.mulPose(Axis.ZP.rotationDegrees(270));
                    p_112309_.translate(-1, 0, 0);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                    //renderTile(world,p_112309_,p_112310_,stack,coin,p_112311_,p_112312_,renderAugmentType);
                    //renderTile(world,p_112309_,p_112310_,coin,stack,p_112311_,p_112312_,renderAugmentType);
                }
                if(facing== Direction.SOUTH) {
                    //p_112309_.mulPose(90, 1, 0, 0);
                    p_112309_.mulPose(Axis.XP.rotationDegrees(90));
                    p_112309_.translate(0, 0, -1);
                    renderTileItems(world,p_112309_,p_112310_,listed,coin,toolStack,p_112311_,p_112312_,renderAugmentType,hudMessages);
                    //renderTile(world,p_112309_,p_112310_,stack,coin,p_112311_,p_112312_,renderAugmentType);
                    //renderTile(world,p_112309_,p_112310_,coin,stack,p_112311_,p_112312_,renderAugmentType);
                }
                if(facing== Direction.WEST) {
                    //p_112309_.mulPose(90, 0, 0, 1);
                    p_112309_.mulPose(Axis.ZP.rotationDegrees(90));
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

    public static void renderItemRotating(Level worldIn, PoseStack p_112309_, MultiBufferSource p_112310_, ItemStack itemStack, int p_112311_, int p_112312_)
    {
        if (!itemStack.isEmpty()) {
            p_112309_.pushPose();
            p_112309_.translate(0.5, 1.0, 0.5);
            //p_112309_.translate(0, MathHelper.sin((worldIn.getGameTime()) / 10.0F) * 0.1 + 0.1, 0); BOBBING ITEM
            p_112309_.scale(0.75F, 0.75F, 0.75F);
            long time = System.currentTimeMillis();
            float angle = (time/25) % 360;
            //float angle = (worldIn.getGameTime()) / 20.0F * (180F / (float) Math.PI);
            p_112309_.mulPose(Axis.YP.rotationDegrees(angle));
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel baked = renderer.getModel(itemStack,worldIn,null,0);
            renderer.render(itemStack, ItemDisplayContext.GROUND,true,p_112309_,p_112310_,p_112311_,p_112312_,baked);

            //Minecraft.getInstance().getItemRenderer().renderItem(itemStack, ItemCameraTransforms.TransformType.GROUND, p_112311_, p_112312_, p_112309_, p_112310_);
            p_112309_.popPose();
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

    public static void renderItemsRotating(Level worldIn, PoseStack posStack, MultiBufferSource buffers, List<ItemStack> listed, int light, int overlay)
    {
        //https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/client/render/tile/RenderTileRuneAltar.java#L49
        int stacks = listed.size();
        if (stacks>1) {
            posStack.pushPose();

            int items = stacks;
            float[] angles = new float[stacks];

            float anglePer = 360F / items;
            float totalAngle = 0F;
            for (int i = 0; i < angles.length; i++) {
                angles[i] = totalAngle += anglePer;
            }

            double time = worldIn.getGameTime();


            float sized = 1.25F;
            float sizedd = 0.25F;
            if(stacks <= 4){sized = 0.25F; sizedd = 0.05F;}
            if(stacks <= 8 && stacks > 4){sized = 0.5F; sizedd = 0.10F;}
            if(stacks <= 12 && stacks > 8){sized = 0.75F; sizedd = 0.15F;}
            if(stacks <= 16 && stacks > 12){sized = 1.0F; sizedd = 0.20F;}

            for (int i = 0; i < stacks; i++) {
                posStack.pushPose();
                posStack.translate(0.5F, 0.75F, 0.5F);
                posStack.mulPose(Axis.YP.rotationDegrees(angles[i] + (float) time));
                posStack.translate(sized, 0F, sizedd);

                posStack.mulPose(Axis.YP.rotationDegrees(90));
                posStack.translate(0D, 0.075 * Math.sin((time + i * 10) / 5D), 0F);
                ItemStack stack = listed.get(i);
                Minecraft mc = Minecraft.getInstance();
                if (!stack.isEmpty()) {
                    mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND,
                            light, overlay, posStack, buffers,worldIn,0);
                }
                posStack.popPose();
            }

            posStack.popPose();
        }
        else if(stacks==1)
        {
            posStack.pushPose();
            posStack.translate(0.5, 1.0, 0.5);
            //posStack.translate(0, MathHelper.sin((worldIn.getGameTime()) / 10.0F) * 0.1 + 0.1, 0); BOBBING ITEM
            posStack.scale(0.75F, 0.75F, 0.75F);
            long time = System.currentTimeMillis();
            float angle = (time/25) % 360;
            //float angle = (worldIn.getGameTime()) / 20.0F * (180F / (float) Math.PI);
            posStack.mulPose(Axis.YP.rotationDegrees(angle));
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel baked = renderer.getModel(listed.get(0),worldIn,null,0);
            renderer.render(listed.get(0), ItemDisplayContext.GROUND,true,posStack,buffers,light,overlay,baked);

            //Minecraft.getInstance().getItemRenderer().renderItem(itemStack, ItemCameraTransforms.TransformType.GROUND, p_112311_, p_112312_, posStack, p_112310_);
            posStack.popPose();
        }
    }

    public static void renderCoin(Level worldIn,ItemStack itemCoin, PoseStack p_112309_, MultiBufferSource p_112310_, float x, float y, float z, float angle, float xr, float yr, float zr, int p_112311_, int p_112312_) {
        if (!itemCoin.isEmpty()) {
            p_112309_.pushPose();
            p_112309_.translate(x, y, z);
            p_112309_.scale(0.1875f, 0.1875f, 0.1875f);
            p_112309_.mulPose(Axis.YP.rotationDegrees(angle));
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel baked = renderer.getModel(itemCoin,worldIn,null,0);
            renderer.render(itemCoin,ItemDisplayContext.FIXED,true,p_112309_,p_112310_,p_112311_,p_112312_,baked);
            //Minecraft.getInstance().getItemRenderer().renderItem(itemCoin, ItemCameraTransforms.TransformType.FIXED, p_112311_, p_112312_, p_112309_, p_112310_);
            p_112309_.popPose();
        }
    }

    public static void renderTool(Level worldIn,ItemStack itemTool, PoseStack p_112309_, MultiBufferSource p_112310_, int p_112311_, int p_112312_) {
        if (!itemTool.isEmpty()) {
            p_112309_.pushPose();
            p_112309_.translate(0.5D, 0.75D, 0.5D);
            p_112309_.scale(0.5F, 0.5F, 0.5F);
            p_112309_.mulPose(Axis.YP.rotationDegrees(90));
            p_112309_.mulPose(Axis.XP.rotationDegrees(90));
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel baked = renderer.getModel(itemTool,worldIn,null,0);
            renderer.render(itemTool,ItemDisplayContext.FIXED,true,p_112309_,p_112310_,p_112311_,p_112312_,baked);
            //Minecraft.getInstance().getItemRenderer().renderItem(itemCoin, ItemCameraTransforms.TransformType.FIXED, p_112311_, p_112312_, p_112309_, p_112310_);
            p_112309_.popPose();
        }
    }




    //https://github.com/StanCEmpire/RegionProtection/blob/1.18.x/src/main/java/stancempire/stancempiresregionprotection/blockentities/RegionBlockBER.java
    public void renderBoundingBox(BlockPos pos, AABB aabb, PoseStack matrixStack, VertexConsumer buffer, BasePedestalBlockEntity blockEntity, float red, float green, float blue, float alpha)
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

    public void renderFaces(TextureAtlasSprite sprite, BlockPos pos, AABB aabb, PoseStack matrixStack, VertexConsumer buffer, BasePedestalBlockEntity blockEntity, float red, float green, float blue, float alpha)
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
        matrixStack.mulPose(new Quaternionf(Axis.XP.rotationDegrees(angleX)));
        matrixStack.mulPose(new Quaternionf(Axis.YP.rotationDegrees(angleY)));

        Font fontrenderer = Minecraft.getInstance().font;
        int lines = 11;
        int currenty = 7;
        int height = 10;
        int logsize = messages.size();
        int i = 0;
        float xF = 0.0F;
        float yF = 0.0F;
        for (String s : messages) {
            if (i >= logsize - lines) {
                if (currenty + height <= 124) {
                    String prefix = "";
                    fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(prefix + s, 115),xF,yF,7,false,matrixStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH,0, 0xf000f0);
                    currenty += height;
                    yF+= 10.0F;
                }
            }
            i++;
        }


        matrixStack.popPose();
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
