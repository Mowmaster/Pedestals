package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Where the quarry and pump are up to: they work down the work card's area one column at a time, perOperation blocks
// per step (1.20's _numposition / _numheight / _miny / _boolstop, now in the pedestal's upgrade data).
//   normal: each column from the area's top to its bottom, then round again
//   to bedrock: the area must be flat; every column is worked perOperation blocks down, then the next layer, until
//   the bottom of the world, then it stops
public class ColumnWork
{
    private static final String POSITION = "position";
    private static final String HEIGHT = "height";
    private static final String STOPPED = "stopped";

    // One step: blocks top..bottom (inclusive) in one column
    public record Step(int x, int z, int top, int bottom)
    {
        public List<BlockPos> blocks()
        {
            List<BlockPos> blocks = new ArrayList<>();
            for (int y = top; y >= bottom; y--) blocks.add(new BlockPos(x, y, z));
            return blocks;
        }
    }

    // The step to do now, or null if there's nothing to do (no area, finished, or not flat for to-bedrock)
    public static @Nullable Step current(ServerLevel level, BasePedestalBlockEntity pedestal, int perOperation, boolean toBedrock)
    {
        CompoundTag data = pedestal.getUpgradeData();
        List<BlockPos> area = pedestal.getWorkPositions();
        if (area.isEmpty() || data.getBooleanOr(STOPPED, false)) return null;
        int minY = area.stream().mapToInt(BlockPos::getY).min().orElse(0);
        int maxY = area.stream().mapToInt(BlockPos::getY).max().orElse(0);
        List<BlockPos> columns = columns(area);
        int index = data.getIntOr(POSITION, 0);

        if (toBedrock)
        {
            if (minY != maxY) return null;
            if (index >= columns.size()) index = 0;
            int top = data.getIntOr(HEIGHT, maxY);
            BlockPos column = columns.get(index);
            return new Step(column.getX(), column.getZ(), top, Math.max(level.getMinY(), top - perOperation + 1));
        }
        int steps = (maxY - minY) / perOperation + 1;
        if (index >= columns.size() * steps) index = 0;
        BlockPos column = columns.get(index / steps);
        int top = maxY - (index % steps) * perOperation;
        return new Step(column.getX(), column.getZ(), top, Math.max(minY, top - perOperation + 1));
    }

    // Moves on after a step was done
    public static void advance(ServerLevel level, BasePedestalBlockEntity pedestal, Step done, boolean toBedrock)
    {
        CompoundTag data = pedestal.getUpgradeData();
        int index = data.getIntOr(POSITION, 0) + 1;
        if (toBedrock && index >= columns(pedestal.getWorkPositions()).size())
        {
            index = 0;
            data.putInt(HEIGHT, done.bottom() - 1);
            if (done.bottom() <= level.getMinY()) data.putBoolean(STOPPED, true);
        }
        data.putInt(POSITION, index);
        pedestal.upgradeDataChanged();
    }

    // One position per column (x, z), highest x and z first (1.20's order)
    private static List<BlockPos> columns(List<BlockPos> area)
    {
        Set<BlockPos> columns = new LinkedHashSet<>();
        for (BlockPos pos : area) columns.add(new BlockPos(pos.getX(), 0, pos.getZ()));
        List<BlockPos> sorted = new ArrayList<>(columns);
        sorted.sort(Comparator.comparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ).reversed());
        return sorted;
    }
}
