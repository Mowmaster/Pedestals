package com.mowmaster.pedestals.TintHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

// A box in pixel coordinates (0-16), matching a model element's "from" and "to"
public record libPixelBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
{
    public libPixelBox offset(double dx, double dy, double dz)
    {
        return new libPixelBox(minX + dx, minY + dy, minZ + dz, maxX + dx, maxY + dy, maxZ + dz);
    }

    public double min(Direction.Axis axis)
    {
        return axis.choose(minX, minY, minZ);
    }

    public double max(Direction.Axis axis)
    {
        return axis.choose(maxX, maxY, maxZ);
    }

    // Matches a blockstate "x" rotation of quarterTurns x 90 degrees: one turn moves a north-facing back to face down
    public libPixelBox rotateX(int quarterTurns)
    {
        libPixelBox box = this;
        for (int i = 0; i < quarterTurns % 4; i++)
        {
            box = new libPixelBox(box.minX, box.minZ, 16 - box.maxY, box.maxX, box.maxZ, 16 - box.minY);
        }
        return box;
    }

    // Matches a blockstate "y" rotation of quarterTurns x 90 degrees: clockwise seen from above, north -> east
    public libPixelBox rotateY(int quarterTurns)
    {
        libPixelBox box = this;
        for (int i = 0; i < quarterTurns % 4; i++)
        {
            box = new libPixelBox(16 - box.maxZ, box.minY, box.minX, 16 - box.minZ, box.maxY, box.maxX);
        }
        return box;
    }

    public boolean contains(Vec3 pixel)
    {
        return pixel.x >= minX && pixel.x <= maxX
                && pixel.y >= minY && pixel.y <= maxY
                && pixel.z >= minZ && pixel.z <= maxZ;
    }

    public VoxelShape toShape()
    {
        return Block.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static VoxelShape union(List<libPixelBox> boxes)
    {
        return boxes.stream().map(libPixelBox::toShape).reduce(Shapes.empty(), Shapes::or);
    }

    // Quarter turns of "y" from a north-facing model to the given horizontal facing (north 0, east 1, south 2, west 3)
    public static int quarterTurns(Direction facing)
    {
        return switch (facing)
        {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    // The clicked point in pixels, nudged slightly into the face that was hit so it lands inside the clicked element
    public static Vec3 pixelInside(BlockHitResult hit)
    {
        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();
        Vec3 location = hit.getLocation();
        double nudge = 0.01;
        return new Vec3(
                (location.x - pos.getX()) * 16 - face.getStepX() * nudge,
                (location.y - pos.getY()) * 16 - face.getStepY() * nudge,
                (location.z - pos.getZ()) * 16 - face.getStepZ() * nudge);
    }

    // Mirrored east-west (x -> 16 - x)
    public libPixelBox mirrorX()
    {
        return new libPixelBox(16 - maxX, minY, minZ, 16 - minX, maxY, maxZ);
    }
}