package com.mowmaster.pedestals.tiles.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nonnull;

import static net.minecraft.block.DirectionalBlock.FACING;

public class RenderPedestal extends TileEntityRenderer<TilePedestal> {

    public RenderPedestal(TileEntityRendererDispatcher rendererDispatcher)
    {
        super(rendererDispatcher);
    }

    @Override
    public void render(@Nonnull TilePedestal tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        if (!tileEntityIn.isRemoved()) {
            Direction facing = tileEntityIn.getBlockState().get(FACING);
            ItemStack stack = tileEntityIn.getItemInPedestal();
            ItemStack coin = tileEntityIn.getCoinOnPedestal();
            World world = tileEntityIn.getWorld();

            if(facing== Direction.UP)//when placed on ground
            {
                renderTile(world,matrixStackIn,bufferIn,coin,stack,combinedLightIn,combinedOverlayIn);
            }
            if(facing== Direction.DOWN) {
                //matrixStackIn.rotate(new Quaternion(0, 0, 1,180));
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180));
                matrixStackIn.translate(0, -1, 0);
                matrixStackIn.translate(-1, 0, 0);
                renderTile(world,matrixStackIn,bufferIn,coin,stack,combinedLightIn,combinedOverlayIn);            }
            if(facing== Direction.NORTH) {
                //matrixStackIn.rotate(new Quaternion(1, 0, 0,270));
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(270));
                matrixStackIn.translate(0, -1, 0);
                renderTile(world,matrixStackIn,bufferIn,coin,stack,combinedLightIn,combinedOverlayIn);            }
            if(facing== Direction.EAST) {
                //matrixStackIn.rotate(270, 0, 0, 1);
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(270));
                matrixStackIn.translate(-1, 0, 0);
                renderTile(world,matrixStackIn,bufferIn,coin,stack,combinedLightIn,combinedOverlayIn);            }
            if(facing== Direction.SOUTH) {
                //matrixStackIn.rotate(90, 1, 0, 0);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90));
                matrixStackIn.translate(0, 0, -1);
                renderTile(world,matrixStackIn,bufferIn,coin,stack,combinedLightIn,combinedOverlayIn);            }
            if(facing== Direction.WEST) {
                //matrixStackIn.rotate(90, 0, 0, 1);
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(90));
                matrixStackIn.translate(0, -1, 0);
                renderTile(world,matrixStackIn,bufferIn,coin,stack,combinedLightIn,combinedOverlayIn);            }
        }
    }

    public static void  renderTile(World worldIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, ItemStack coin, ItemStack item, int combinedLightIn, int combinedOverlayIn)
    {
        renderItemRotating(worldIn,matrixStackIn,bufferIn,item,combinedLightIn,combinedOverlayIn);
        renderCoin(coin,matrixStackIn,bufferIn,0.5f,0.475f,0.3125f,0,0,0,0,combinedLightIn,combinedOverlayIn);
        renderCoin(coin,matrixStackIn,bufferIn,0.3125f,0.475f,0.5f,90,0,1f,0,combinedLightIn,combinedOverlayIn);
        renderCoin(coin,matrixStackIn,bufferIn,0.5f,0.475f,0.6875f,180,0,1f,0,combinedLightIn,combinedOverlayIn);
        renderCoin(coin,matrixStackIn,bufferIn,0.6875f,0.475f,0.5f,270,0,1f,0,combinedLightIn,combinedOverlayIn);

    }
    public static void renderItemRotating(World worldIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, ItemStack itemStack, int combinedLightIn, int combinedOverlayIn)
    {
        if (!itemStack.isEmpty()) {
            matrixStackIn.push();
            matrixStackIn.translate(0.5, 1.0, 0.5);
            //matrixStackIn.translate(0, MathHelper.sin((worldIn.getGameTime()) / 10.0F) * 0.1 + 0.1, 0); BOBBING ITEM
            matrixStackIn.scale(0.75F, 0.75F, 0.75F);
            float angle = (worldIn.getGameTime()) / 20.0F * (180F / (float) Math.PI);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(angle));
            Minecraft.getInstance().getItemRenderer().renderItem(itemStack, ItemCameraTransforms.TransformType.GROUND, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
            matrixStackIn.pop();
        }
    }
    public static void renderCoin(ItemStack itemCoin, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float x, float y, float z, float angle, float xr, float yr, float zr, int combinedLightIn, int combinedOverlayIn) {
        if (!itemCoin.isEmpty()) {
            matrixStackIn.push();
            matrixStackIn.translate(x, y, z);
            matrixStackIn.scale(0.1875f, 0.1875f, 0.1875f);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(angle));
            Minecraft.getInstance().getItemRenderer().renderItem(itemCoin, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
            matrixStackIn.pop();
        }
    }

    /*private static void renderPart(MatrixStack matrixStack, IVertexBuilder vertexBuilder,
                                   float red, float green, float blue, float alpha, int ymin, int ymax,
                                   float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4,
                                   float u1, float u2, float v1, float v2) {
        MatrixStack.Entry currentTransformMatrix = matrixStack.getLast();  // getLast
        Matrix4f positionMatrix = currentTransformMatrix.getMatrix();  // getPositionMatrix
        Matrix3f normalMatrix = currentTransformMatrix.getNormal();  // getNormalMatrix
        addQuad(positionMatrix, normalMatrix, vertexBuilder, red, green, blue, alpha, ymin, ymax, x1, z1, x2, z2, u1, u2, v1, v2);
        addQuad(positionMatrix, normalMatrix, vertexBuilder, red, green, blue, alpha, ymin, ymax, x4, z4, x3, z3, u1, u2, v1, v2);
        addQuad(positionMatrix, normalMatrix, vertexBuilder, red, green, blue, alpha, ymin, ymax, x2, z2, x4, z4, u1, u2, v1, v2);
        addQuad(positionMatrix, normalMatrix, vertexBuilder, red, green, blue, alpha, ymin, ymax, x3, z3, x1, z1, u1, u2, v1, v2);
    }
    private static void addQuad(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder vertexBuilder,
                                float red, float green, float blue, float alpha, int yMin, int yMax,
                                float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2) {
        addVertex(matrixPos, matrixNormal, vertexBuilder, red, green, blue, alpha, yMax, x1, z1, u2, v1);
        addVertex(matrixPos, matrixNormal, vertexBuilder, red, green, blue, alpha, yMin, x1, z1, u2, v2);
        addVertex(matrixPos, matrixNormal, vertexBuilder, red, green, blue, alpha, yMin, x2, z2, u1, v2);
        addVertex(matrixPos, matrixNormal, vertexBuilder, red, green, blue, alpha, yMax, x2, z2, u1, v1);
    }
    private static void addVertex(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder vertexBuilder,
                                  float red, float green, float blue, float alpha,
                                  int y, float x, float z, float texU, float texV) {
        vertexBuilder.pos(matrixPos, x, (float)y, z) // pos
                .color(red, green, blue, alpha)        // color
                .tex(texU, texV)                     // tex
                .overlay(OverlayTexture.NO_OVERLAY) // overlay; field_229196_a_ = no modifier
                .lightmap(0xf000f0)                       // lightmap with full brightness
                .normal(matrixNormal, 0.0F, 1.0F, 0.0F)  // normal
                .endVertex();
    }*/

    public static void init(final FMLClientSetupEvent event)
    {
        ClientRegistry.bindTileEntityRenderer(TilePedestal.PEDESTALTYPE,RenderPedestal::new);
    }
}