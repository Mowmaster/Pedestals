package com.mowmaster.pedestals.Items.WorkCards;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.ISelectableArea;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkCardArea extends WorkCardBase implements ISelectableArea {
    public WorkCardArea(Properties p_41383_) {
        super(p_41383_);
    }

    public int getWorkCardType() {
        return 1;
    }

    // NOTE: The returned AABB is sufficient for cases that are dealing directly with the points covered by the area,
    // but needs to be modified by `.expandTowards(1.0D, 1.0D, 1.0D)` for any code that is using it as a bounding-box.
    public static Optional<AABB> getAABBIfDefined(ItemStack workCardStack) {
        BlockPos posOne = readBlockPosFromNBT(workCardStack, 1);
        BlockPos posTwo = readBlockPosFromNBT(workCardStack, 2);
        if (
            workCardStack.getItem() instanceof WorkCardArea workCard &&
            !posOne.equals(BlockPos.ZERO) && !posTwo.equals(BlockPos.ZERO)
        ) {
            return Optional.of(new AABB(posOne, posTwo));
        } else {
            return Optional.empty();
        }
    }

    // NOTE: The returned AABB is sufficient for cases that are dealing directly with the points covered by the area,
    // but needs to be modified by `.expandTowards(1.0D, 1.0D, 1.0D)` for any code that is using it as a bounding-box.
    public static Optional<AABB> getAABBIfDefinedAndInRange(ItemStack workCardStack, BasePedestalBlockEntity pedestal) {
        BlockPos posOne = readBlockPosFromNBT(workCardStack, 1);
        BlockPos posTwo = readBlockPosFromNBT(workCardStack, 2);
        if (
            workCardStack.getItem() instanceof WorkCardArea workCard &&
            !posOne.equals(BlockPos.ZERO) && !posTwo.equals(BlockPos.ZERO) &&
            workCard.isSelectionInRange(pedestal, posOne) && workCard.isSelectionInRange(pedestal, posTwo)
        ) {
            return Optional.of(new AABB(posOne, posTwo));
        } else {
            return Optional.empty();
        }
    }

    public static List<BlockPos> getPositionsInRangeOfUpgrade(ItemStack workCardStack, BasePedestalBlockEntity pedestal) {
        return getAABBIfDefinedAndInRange(workCardStack, pedestal)
            .map(area -> {
                List<BlockPos> locations = new ArrayList<>();

                int minX = (int) area.minX;
                int minY = (int) area.minY;
                int minZ = (int) area.minZ;

                int maxX = (int) area.maxX;
                int maxY = (int) area.maxY;
                int maxZ = (int) area.maxZ;

                if (minY < pedestal.getPos().getY()) {
                    for (int i = maxX; i >= minX; i--) {
                        for (int j = maxZ; j >= minZ; j--) {
                            for (int k = maxY; k >= minY; k--) {
                                locations.add(new BlockPos(i, k, j));
                            }
                        }
                    }
                } else {
                    for (int i = minX; i <= maxX; i++) {
                        for (int j = minZ; j <= maxZ; j++) {
                            for (int k = minY; k <= maxY; k++) {
                                locations.add(new BlockPos(i, k, j));
                            }
                        }
                    }
                }
                return locations;
            }).orElse(List.of());
    }

    public static <T extends Entity> List<T> getEntitiesInRangeOfUpgrade(Level level, Class<T> entityType, ItemStack workCardStack, BasePedestalBlockEntity pedestal) {
        return getAABBIfDefinedAndInRange(workCardStack, pedestal)
            .map(area -> level.getEntitiesOfClass(entityType, area.expandTowards(1.0D, 1.0D, 1.0D)))
            .orElse(List.of());
    }
}
