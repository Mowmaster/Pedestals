package com.mowmaster.pedestals.client.RenderPedestalOutline;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mowmaster.pedestals.item.ItemLinkingTool;
import com.mowmaster.pedestals.item.ItemUpgradeTool;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.List;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

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

        /*ItemStack stackUpgrade = (player.getHeldItemOffhand().getItem() instanceof ItemUpgradeTool)?(player.getHeldItemOffhand()):(player.getHeldItemMainhand());
        if (stackUpgrade.isEnchanted() && stackUpgrade.getItem() instanceof ItemUpgradeTool) {
            ItemUpgradeTool UT = (ItemUpgradeTool) stackUpgrade.getItem();
            if(stackUpgrade.hasTag())
            {
                UT.getPosFromNBT(stack);
                BlockPos pos = UT.getStoredPosition(stack);
                int[] getWorkArea = UT.getWorkPosFromNBT(stack);
                showWorkArea(player, event.getMatrixStack(),pos,getWorkArea);
            }

        }*/
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

    private static void workAreaLine(IVertexBuilder builder, Matrix4f positionMatrix, BlockPos pos, float dx1, float dy1, float dz1, float dx2, float dy2, float dz2) {
        builder.pos(positionMatrix, pos.getX()+dx1, pos.getY()+dy1, pos.getZ()+dz1)
                .color(0.0f, 1.0f, 0.0f, 1.0f)
                .endVertex();
        builder.pos(positionMatrix, pos.getX()+dx2, pos.getY()+dy2, pos.getZ()+dz2)
                .color(0.0f, 1.0f, 0.0f, 1.0f)
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

    //TODO: FIX ME!!!
    private static void showWorkArea(ClientPlayerEntity player, MatrixStack matrixStack,BlockPos storedPos, int[] workArea) {
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer(RenderPedestalType.OVERLAY_LINES);

        World world = player.getEntityWorld();
        matrixStack.push();

        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        Matrix4f matrix = matrixStack.getLast().getMatrix();

        if(world.getTileEntity(storedPos) != null){
            if(world.getTileEntity(storedPos) instanceof PedestalTileEntity)
            {
                int z = workArea[2];
                int x = workArea[0];
                int y = workArea[1];

                BlockState state = world.getBlockState(storedPos);
                Direction enumfacing = state.get(FACING);
                BlockPos negBlock = storedPos;
                BlockPos posBlock = storedPos;

                if(enumfacing.equals(Direction.UP))
                {
                    negBlock.add(-x,0,-z);
                    posBlock.add(x,y,z);
                }
                else if(enumfacing.equals(Direction.DOWN))
                {
                    negBlock.add(-x,-y,-z);
                    posBlock.add(x,0,z);
                }
                else if(enumfacing.equals(Direction.NORTH))
                {
                    negBlock.add(-x,-z,-y);
                    posBlock.add(x,z,0);
                }
                else if(enumfacing.equals(Direction.SOUTH))
                {
                    negBlock.add(-x,-z,0);
                    posBlock.add(x,z,y);
                }
                else if(enumfacing.equals(Direction.EAST))
                {
                    negBlock.add(0,-x,-z);
                    posBlock.add(y,x,z);
                }
                else if(enumfacing.equals(Direction.WEST))
                {
                    negBlock.add(-y,-x,-z);
                    posBlock.add(0,x,z);
                }

                /*switch (enumfacing)
                {
                    case UP:

                    case DOWN:

                    case NORTH:

                    case SOUTH:

                    case EAST:

                    case WEST:

                    default:
                        negBlock.add(0,0,0);
                        posBlock.add(0,0,0);
                }*/


                int xdiff = posBlock.getX() - negBlock.getX();
                int ydiff = posBlock.getY() - negBlock.getY();
                int zdiff = posBlock.getZ() - negBlock.getZ();

                int lineLength = zdiff + xdiff+1;
                int lineLength2 = zdiff + xdiff+1;

                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1),  0, 0, 0,-lineLength, 0, 0);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1),  0, -lineLength, 0,-lineLength, -lineLength, 0);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1),  0, 0, -lineLength,-lineLength, 0, -lineLength);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1),  0, -lineLength, -lineLength,-lineLength, -lineLength, -lineLength);

                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), 0, 0, 0, 0, 0, -lineLength);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), -lineLength, 0, 0, -lineLength, 0, -lineLength);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), 0, -lineLength, 0, 0, -lineLength, -lineLength);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), -lineLength, -lineLength, 0, -lineLength, -lineLength, -lineLength);

                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), 0, 0, 0, 0, -lineLength, 0);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), -lineLength, 0, 0, -lineLength, -lineLength, 0);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), 0, 0, -lineLength, 0, -lineLength, -lineLength);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), -lineLength, 0, -lineLength, -lineLength, -lineLength, -lineLength);
            }

        }

        matrixStack.pop();

        RenderSystem.disableDepthTest();
        buffer.finish(RenderPedestalType.OVERLAY_LINES);
    }
}
