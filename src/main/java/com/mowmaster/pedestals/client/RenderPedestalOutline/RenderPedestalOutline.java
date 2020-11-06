package com.mowmaster.pedestals.client.RenderPedestalOutline;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mowmaster.pedestals.item.ItemLinkingTool;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.List;

public class RenderPedestalOutline
{
    public static void render(RenderWorldLastEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        ItemStack stack = (player.getHeldItemOffhand().getItem() instanceof ItemLinkingTool)?(player.getHeldItemOffhand()):(player.getHeldItemMainhand());
        if (stack.isEnchanted() && stack.getItem() instanceof ItemLinkingTool) {
            ItemLinkingTool LT = (ItemLinkingTool) stack.getItem();
            if(stack.hasTag())
            {
                LT.getPosFromNBT(stack);
                BlockPos pos = LT.getStoredPosition(stack);
                List<BlockPos> storedRecievers = LT.getStoredPositionList(stack);

                locateTileEntities(player, event.getMatrixStack(),pos,storedRecievers);
            }

        }
    }

    private static void blueLine(IVertexBuilder builder, Matrix4f positionMatrix, BlockPos pos, float dx1, float dy1, float dz1, float dx2, float dy2, float dz2) {
        builder.pos(positionMatrix, pos.getX()+dx1, pos.getY()+dy1, pos.getZ()+dz1)
                .color(0.95f, 0.95f, 0.95f, 1.0f)
                .endVertex();
        builder.pos(positionMatrix, pos.getX()+dx2, pos.getY()+dy2, pos.getZ()+dz2)
                .color(0.95f, 0.95f, 0.95f, 1.0f)
                .endVertex();
    }

    private static void areaLine(IVertexBuilder builder, Matrix4f positionMatrix, BlockPos pos, float dx1, float dy1, float dz1, float dx2, float dy2, float dz2) {
        builder.pos(positionMatrix, pos.getX()+dx1, pos.getY()+dy1, pos.getZ()+dz1)
                .color(1.0f, 0.0f, 0.0f, 1.0f)
                .endVertex();
        builder.pos(positionMatrix, pos.getX()+dx2, pos.getY()+dy2, pos.getZ()+dz2)
                .color(1.0f, 0.0f, 0.0f, 1.0f)
                .endVertex();
    }

    private static void locateTileEntities(ClientPlayerEntity player, MatrixStack matrixStack,BlockPos storedPos, List<BlockPos> storedRecievers) {
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer(RenderPedestalType.OVERLAY_LINES);

        World world = player.getEntityWorld();
        matrixStack.push();

        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        Matrix4f matrix = matrixStack.getLast().getMatrix();

        int locationsNum = storedRecievers.size();

        for(int i=0;i<locationsNum;i++)
        {
            BlockPos pos = storedRecievers.get(i);
            if (world.getTileEntity(pos) != null) {
                blueLine(builder, matrix, pos, 0, 0, 0, 1, 0, 0);
                blueLine(builder, matrix, pos, 0, 1, 0, 1, 1, 0);
                blueLine(builder, matrix, pos, 0, 0, 1, 1, 0, 1);
                blueLine(builder, matrix, pos, 0, 1, 1, 1, 1, 1);

                blueLine(builder, matrix, pos, 0, 0, 0, 0, 0, 1);
                blueLine(builder, matrix, pos, 1, 0, 0, 1, 0, 1);
                blueLine(builder, matrix, pos, 0, 1, 0, 0, 1, 1);
                blueLine(builder, matrix, pos, 1, 1, 0, 1, 1, 1);

                blueLine(builder, matrix, pos, 0, 0, 0, 0, 1, 0);
                blueLine(builder, matrix, pos, 1, 0, 0, 1, 1, 0);
                blueLine(builder, matrix, pos, 0, 0, 1, 0, 1, 1);
                blueLine(builder, matrix, pos, 1, 0, 1, 1, 1, 1);
            }
        }

        if(world.getTileEntity(storedPos) != null){
            if(world.getTileEntity(storedPos) instanceof PedestalTileEntity)
            {
                PedestalTileEntity pedestal = ((PedestalTileEntity)world.getTileEntity(storedPos));
                int range = pedestal.getLinkingRange();
                int zmax = range;
                int xmax = range;
                int ymax = range;

                //Just so we know the differnce from the original
                int lineLength = zmax + xmax+1;
                int lineLength2 = zmax + xmax+1;

                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1),  0, 0, 0,-lineLength, 0, 0);
                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1),  0, -lineLength, 0,-lineLength, -lineLength, 0);
                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1),  0, 0, -lineLength,-lineLength, 0, -lineLength);
                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1),  0, -lineLength, -lineLength,-lineLength, -lineLength, -lineLength);

                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1), 0, 0, 0, 0, 0, -lineLength);
                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1), -lineLength, 0, 0, -lineLength, 0, -lineLength);
                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1), 0, -lineLength, 0, 0, -lineLength, -lineLength);
                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1), -lineLength, -lineLength, 0, -lineLength, -lineLength, -lineLength);

                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1), 0, 0, 0, 0, -lineLength, 0);
                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1), -lineLength, 0, 0, -lineLength, -lineLength, 0);
                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1), 0, 0, -lineLength, 0, -lineLength, -lineLength);
                areaLine(builder, matrix, storedPos.add(xmax+1,ymax+1,zmax+1), -lineLength, 0, -lineLength, -lineLength, -lineLength, -lineLength);
            }

        }



        /*for (int dx = -10; dx <= 10; dx++) {
            for (int dy = -10; dy <= 10; dy++) {
                for (int dz = -10; dz <= 10; dz++) {
                    pos.setPos(px + dx, py + dy, pz + dz);
                    if (world.getTileEntity(pos) != null) {
                        blueLine(builder, matrix, pos, 0, 0, 0, 1, 0, 0);
                        blueLine(builder, matrix, pos, 0, 1, 0, 1, 1, 0);
                        blueLine(builder, matrix, pos, 0, 0, 1, 1, 0, 1);
                        blueLine(builder, matrix, pos, 0, 1, 1, 1, 1, 1);

                        blueLine(builder, matrix, pos, 0, 0, 0, 0, 0, 1);
                        blueLine(builder, matrix, pos, 1, 0, 0, 1, 0, 1);
                        blueLine(builder, matrix, pos, 0, 1, 0, 0, 1, 1);
                        blueLine(builder, matrix, pos, 1, 1, 0, 1, 1, 1);

                        blueLine(builder, matrix, pos, 0, 0, 0, 0, 1, 0);
                        blueLine(builder, matrix, pos, 1, 0, 0, 1, 1, 0);
                        blueLine(builder, matrix, pos, 0, 0, 1, 0, 1, 1);
                        blueLine(builder, matrix, pos, 1, 0, 1, 1, 1, 1);
                    }
                }
            }
        }*/

        matrixStack.pop();

        RenderSystem.disableDepthTest();
        buffer.finish(RenderPedestalType.OVERLAY_LINES);
    }
}
