package com.mowmaster.pedestals.Items.WorkCards;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.ISelectablePoints;
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

    public static int getNumPositions(ItemStack workCardStack) {
        return WorkCardBase.readBlockPosListFromNBT(workCardStack).size();
    }
}
