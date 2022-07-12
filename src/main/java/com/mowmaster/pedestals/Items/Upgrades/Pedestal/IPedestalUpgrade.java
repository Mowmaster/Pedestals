package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IPedestalUpgrade
{
    int getComparatorRedstoneLevel(Level worldIn, BlockPos pos);

    //Main Method Call that allows the upgrade to do things
    void updateAction(Level world, BasePedestalBlockEntity pedestal);
    //Used when Entities Collide with Pedestal
    void actionOnCollideWithBlock(BasePedestalBlockEntity pedestal, Entity entityIn);
    //Used When a block below has changed and the upgrade needs to update its behavior
    void actionOnNeighborBelowChange(BasePedestalBlockEntity pedestal, BlockPos belowBlock);
    //Used when removed from pedestal, or when pedestal breaks and its dropped.
    void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal);


    // Check if these can be transferred
    boolean canTransferItems(ItemStack upgrade);
    boolean canTransferFluids(ItemStack upgrade);
    boolean canTransferEnergy(ItemStack upgrade);
    boolean canTransferXP(ItemStack upgrade);
    boolean canTransferDust(ItemStack upgrade);
}
