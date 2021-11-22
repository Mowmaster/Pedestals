package com.mowmaster.pedestals.api;

import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeEffectHarvester;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHarvesterOverride {
    /**
     * Register a new harvester override
     *
     * @param override
     *          the override to register
     */
    public static void registerHarvestOverrider(IHarvesterOverride override) {
        ItemUpgradeEffectHarvester.HARVEST_OVERRIDES.add(override);
    }

    /**
     * Check if this harvesting logic applies to a certain block
     *
     * @param state
     *          the state of the block
     * @param world
     *          the world
     * @param pos
     *          the position
     * @return true if this logic handles the given block
     */
    boolean appliesTo(BlockState state, World world, BlockPos pos);

    /**
     * Check if this harvesting logic can actually harvest the block
     * (for example check for maturity)
     *
     * @param world
     *          the world
     * @param state
     *          the state of the block
     * @param pos
     *          the position
     * @return true if this logic handles the given block
     */
    boolean canHarvest(World world, BlockState state, BlockPos pos);

    /**
     * Attempt to harvest and run custom harvest logic for a block at the given position, as if broken by left click
     *
     * @param state
     *          the state of the block
     * @param world
     *          the world
     * @param pos
     *          the position
     * @param player
     *          the fake player used by Pedestals
     */
    void attemptHardHarvest(BlockState state, World world, BlockPos pos, PlayerEntity player);

    /**
     * Attempt to harvest and run custom harvest logic for a block at the given position, as if right-clicked
     *
     * @param state
     *          the state of the block
     * @param world
     *          the world
     * @param pos
     *          the position
     * @param dropConsumer
     *          pass any drops resulting from the harvest operation
     * @return true if the harvesting operation was successfully handled
     */
    void attemptGentleHarvest(BlockState state, World world, BlockPos pos, PlayerEntity player);
}
