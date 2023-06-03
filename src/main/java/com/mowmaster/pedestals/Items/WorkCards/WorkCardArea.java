package com.mowmaster.pedestals.Items.WorkCards;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.ISelectableArea;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class WorkCardArea extends WorkCardBase implements ISelectableArea {

    public WorkCardArea(Properties p_41383_) {
        super(p_41383_);
    }

    public int getWorkCardType()
    {
        return 1;
    }

    public static List<BlockPos> getPositionsInRangeOfUpgrade(ItemStack workCardStack, BasePedestalBlockEntity pedestal) {
        if (workCardStack.getItem() instanceof WorkCardArea workCard) {
            List<BlockPos> locations = new ArrayList<>();
            if (workCard.selectedAreaWithinRange(pedestal)) {
                AABB area = new AABB(WorkCardBase.readBlockPosFromNBT(workCardStack, 1), WorkCardBase.readBlockPosFromNBT(workCardStack, 2));

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
            }
            return locations;
        } else {
            return List.of();
        }
    }
}
