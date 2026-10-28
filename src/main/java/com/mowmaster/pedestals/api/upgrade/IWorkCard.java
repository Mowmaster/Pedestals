package com.mowmaster.pedestals.api.upgrade;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

// Implemented by work card items (replaces MowLib's IWorkCard). A pedestal holds one work card; upgrades that
// need positions (breakers, placers, the magnet...) read them through the pedestal (getWorkPositions/getWorkArea).
public interface IWorkCard
{
    WorkCardType getWorkCardType();

    // The card's positions that are within range of the pedestal (range is per axis). Empty if none are.
    List<BlockPos> getPositions(ItemStack card, BasePedestalBlockEntity pedestal, int range);

    // For area cards: the selected box as entity search bounds, if both corners are set and in range
    default Optional<AABB> getArea(ItemStack card, BasePedestalBlockEntity pedestal, int range)
    {
        return Optional.empty();
    }
}
