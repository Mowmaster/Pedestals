package com.mowmaster.pedestals.Items.WorkCards;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Two corners of a box (1.20's WorkCardArea). The first click sets one corner, the second click the other.
public class WorkCardAreaItem extends WorkCardItem
{
    public WorkCardAreaItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public WorkCardType getWorkCardType()
    {
        return WorkCardType.AREA;
    }

    @Override
    protected Component select(Level level, ItemStack card, BlockPos pos)
    {
        List<BlockPos> corners = new ArrayList<>(getStoredPositions(card));
        if (corners.size() >= 2) return Component.translatable(MODID + ".workcard_blockpos_full");
        corners.add(pos);
        setStoredPositions(card, corners);
        return Component.translatable(MODID + (corners.size() == 1 ? ".workcard_blockpos_first" : ".workcard_blockpos_second"));
    }

    // The box between the corners, as min/max block positions, if both are set and in range (and, with
    // REQUIRE_SIZED_AREA on, no bigger than the upgrade's area size)
    private static Optional<BlockPos[]> getCorners(ItemStack card, BasePedestalBlockEntity pedestal, int range)
    {
        List<BlockPos> corners = getStoredPositions(card);
        if (corners.size() < 2) return Optional.empty();
        BlockPos first = corners.get(0);
        BlockPos second = corners.get(1);
        if (!inRange(pedestal, first, range) || !inRange(pedestal, second, range)) return Optional.empty();
        BlockPos min = BlockPos.min(first, second);
        BlockPos max = BlockPos.max(first, second);
        if (Config.REQUIRE_SIZED_AREA.getAsBoolean() && pedestal.getUpgrade().getItem() instanceof ItemUpgradeBase upgrade)
        {
            int size = upgrade.getAreaSize(pedestal.getUpgrade());
            if (max.getX() - min.getX() > size || max.getY() - min.getY() > size || max.getZ() - min.getZ() > size) return Optional.empty();
        }
        return Optional.of(new BlockPos[]{min, max});
    }

    // Every block in the box. When the box goes below the pedestal, each column is listed top-down, so breakers and
    // quarries dig downwards (same order as 1.20).
    @Override
    public List<BlockPos> getPositions(ItemStack card, BasePedestalBlockEntity pedestal, int range)
    {
        return getCorners(card, pedestal, range).map(corners -> {
            BlockPos min = corners[0];
            BlockPos max = corners[1];
            List<BlockPos> positions = new ArrayList<>();
            if (min.getY() < pedestal.getBlockPos().getY())
            {
                for (int x = max.getX(); x >= min.getX(); x--)
                    for (int z = max.getZ(); z >= min.getZ(); z--)
                        for (int y = max.getY(); y >= min.getY(); y--)
                            positions.add(new BlockPos(x, y, z));
            }
            else
            {
                for (int x = min.getX(); x <= max.getX(); x++)
                    for (int z = min.getZ(); z <= max.getZ(); z++)
                        for (int y = min.getY(); y <= max.getY(); y++)
                            positions.add(new BlockPos(x, y, z));
            }
            return positions;
        }).orElse(List.of());
    }

    // The whole box, including the far blocks' full size, for finding entities
    @Override
    public Optional<AABB> getArea(ItemStack card, BasePedestalBlockEntity pedestal, int range)
    {
        return getCorners(card, pedestal, range).map(corners -> AABB.encapsulatingFullBlocks(corners[0], corners[1]));
    }
}
