package com.mowmaster.pedestals.items.workcards;

import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.items.ISelectablePoints;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WorkCardLocations extends WorkCardBase implements ISelectablePoints {

    public WorkCardLocations(Properties p_41383_) {
        super(p_41383_);
    }

    public int getWorkCardType()
    {
        return 2;
    }

    public static List<BlockPos> getPositionsInRangeOfUpgrade(ItemStack workCardStack, BasePedestalBlockEntity pedestal) {
        if (workCardStack.getItem() instanceof WorkCardLocations workCard) {
            return WorkCardBase.readBlockPosListFromNBT(workCardStack).stream()
                .filter(blockPos -> workCard.selectedPointWithinRange(pedestal, blockPos))
                .toList();
        } else {
            return List.of();
        }
    }
}
