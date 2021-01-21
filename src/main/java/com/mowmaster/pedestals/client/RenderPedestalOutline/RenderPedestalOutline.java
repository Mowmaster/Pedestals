package com.mowmaster.pedestals.client.RenderPedestalOutline;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mowmaster.pedestals.blocks.PedestalBlock;
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
                if(player.getEntityWorld().getBlockState(pos).getBlock() instanceof PedestalBlock)
                {
                    int[] getWorkArea = UT.getWorkPosFromNBT(stack);
                    showWorkArea(player, event.getMatrixStack(),pos,getWorkArea);
                }
            }
        }*/
    }

    public static BlockPos getNegRangePosEntity(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(-intWidth,0,-intWidth);
            case DOWN:
                return blockBelow.add(-intWidth,-intHeight,-intWidth);
            case NORTH:
                return blockBelow.add(-intWidth,-intWidth,-intHeight);
            case SOUTH:
                return blockBelow.add(-intWidth,-intWidth,0);
            case EAST:
                return blockBelow.add(0,-intWidth,-intWidth);
            case WEST:
                return blockBelow.add(-intHeight,-intWidth,-intWidth);
            default:
                return blockBelow;
        }
    }

    public static BlockPos getPosRangePosEntity(World world, BlockPos posOfPedestal, int intWidth, int intHeight)
    {
        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(intWidth+1,intHeight,intWidth+1);
            case DOWN:
                return blockBelow.add(intWidth+1,0,intWidth+1);
            case NORTH:
                return blockBelow.add(intWidth+1,intWidth,0+1);
            case SOUTH:
                return blockBelow.add(intWidth+1,intWidth,intHeight+1);
            case EAST:
                return blockBelow.add(intHeight+1,intWidth,intWidth+1);
            case WEST:
                return blockBelow.add(0+1,intWidth,intWidth+1);
            default:
                return blockBelow;
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
                BlockPos negBlock = getNegRangePosEntity(world,storedPos,z,y);
                BlockPos posBlock = getPosRangePosEntity(world,storedPos,z,y);


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

                System.out.print(xdiff);
                System.out.print(" x ");
                System.out.print(ydiff);
                System.out.print(" x ");
                System.out.print(zdiff);

                int lineLength = zdiff + xdiff+1;
                int lineLength2 = zdiff + xdiff+1;

                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff,zdiff+1),  0, 0, 0,-lineLength, 0, 0);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff,zdiff+1),  0, lineLength, 0,-lineLength, lineLength, 0);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff,zdiff+1),  0, 0, -lineLength,-lineLength, 0, -lineLength);
                workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff,zdiff+1),  0, lineLength, -lineLength,-lineLength, lineLength, -lineLength);

                //workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), 0, 0, 0, 0, 0, -lineLength);
                //workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), -lineLength, 0, 0, -lineLength, 0, -lineLength);
                //workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), 0, -lineLength, 0, 0, -lineLength, -lineLength);
                //workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), -lineLength, -lineLength, 0, -lineLength, -lineLength, -lineLength);

                //workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), 0, 0, 0, 0, -lineLength, 0);
                //workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), -lineLength, 0, 0, -lineLength, -lineLength, 0);
                //workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), 0, 0, -lineLength, 0, -lineLength, -lineLength);
                //workAreaLine(builder, matrix, negBlock.add(xdiff+1,ydiff+1,zdiff+1), -lineLength, 0, -lineLength, -lineLength, -lineLength, -lineLength);
            }

        }

        matrixStack.pop();

        RenderSystem.disableDepthTest();
        buffer.finish(RenderPedestalType.OVERLAY_LINES);
    }

}
