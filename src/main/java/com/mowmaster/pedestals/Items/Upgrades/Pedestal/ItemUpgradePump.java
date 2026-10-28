package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeWorld;
import com.mowmaster.pedestals.PedestalTags;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

// Pumps fluid source blocks out of the work card's area into the pedestal, a column at a time like the quarry
// ((4 + block capacity) blocks per operation, operate-to-bedrock works the same). Also empties full water/lava
// cauldrons, and (with the config on) waterlogged blocks. If the pedestal holds a block item, it's placed where the
// fluid was (to stop it flowing back in).
public class ItemUpgradePump extends ItemUpgradeBase
{
    public ItemUpgradePump(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.BLOCK_CAPACITY, UpgradeModifiers.RANGE, UpgradeModifiers.AREA, UpgradeModifiers.OPERATE_TO_BEDROCK);
    }

    @Override
    public boolean canModify(ItemStack upgrade, String modifier)
    {
        if (modifier.equals(UpgradeModifiers.AREA)) return Config.REQUIRE_SIZED_AREA.getAsBoolean();
        return super.canModify(upgrade, modifier);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.AREA;
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.PUMP_FUEL;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        int perOperation = Math.max(1, Config.PUMP_BLOCKS.getAsInt() + getModifier(upgrade, UpgradeModifiers.BLOCK_CAPACITY));
        boolean toBedrock = hasModifier(upgrade, UpgradeModifiers.OPERATE_TO_BEDROCK);
        ColumnWork.Step step = ColumnWork.current(level, pedestal, perOperation, toBedrock);
        if (step == null) return;

        for (BlockPos target : step.blocks())
        {
            if (target.equals(pedestal.getBlockPos())) continue;
            BlockState state = level.getBlockState(target);
            Fluid fluid = pumpableFluid(state);
            if (fluid == null || state.is(PedestalTags.CANT_PUMP)) continue;
            FluidResource resource = FluidResource.of(fluid);
            if (!pedestal.machineFilterAllows(PedestalTransportTypes.FLUIDS.get(), resource)) continue;
            int distance = distance(pedestal.getBlockPos(), target);
            if (!useFuel(pedestal, distance, true)) return; // wait for fuel, then carry on with this step
            try (Transaction transaction = Transaction.openRoot())
            {
                // The whole bucket must fit, or the pump waits (1.20 behaviour)
                if (pedestal.getStorage(PedestalTransportTypes.FLUIDS.get()).insert(resource, FluidType.BUCKET_VOLUME, transaction) < FluidType.BUCKET_VOLUME) return;
                transaction.commit();
            }
            useFuel(pedestal, distance, false);
            removeFluid(level, pedestal, target, state);
        }
        ColumnWork.advance(level, pedestal, step, toBedrock);
    }

    // The fluid a bucket's worth can be taken from here, or null
    private static @Nullable Fluid pumpableFluid(BlockState state)
    {
        if (state.is(Blocks.WATER_CAULDRON) && state.getValue(LayeredCauldronBlock.LEVEL) == LayeredCauldronBlock.MAX_FILL_LEVEL) return Fluids.WATER;
        if (state.is(Blocks.LAVA_CAULDRON)) return Fluids.LAVA;
        if (state.hasProperty(BlockStateProperties.WATERLOGGED))
        {
            return state.getValue(BlockStateProperties.WATERLOGGED) && Config.PUMP_WATERLOGGED.getAsBoolean() ? Fluids.WATER : null;
        }
        FluidState fluidState = state.getFluidState();
        return state.getBlock() instanceof LiquidBlock && fluidState.isSource() ? fluidState.getType() : null;
    }

    private static void removeFluid(ServerLevel level, BasePedestalBlockEntity pedestal, BlockPos target, BlockState state)
    {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED))
        {
            level.setBlockAndUpdate(target, state.setValue(BlockStateProperties.WATERLOGGED, false));
            return;
        }
        if (state.is(Blocks.WATER_CAULDRON) || state.is(Blocks.LAVA_CAULDRON))
        {
            level.setBlockAndUpdate(target, Blocks.CAULDRON.defaultBlockState());
            return;
        }
        level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        // Fill the hole with the pedestal's block, if it has one the placer would use
        ItemStack toPlace = pedestal.getItemInPedestal();
        if (UpgradeWorld.canPlace(toPlace) && pedestal.machineFilterAllows(toPlace)
                && UpgradeWorld.placeBlock(level, pedestal.getFakePlayer(level), toPlace, target))
        {
            pedestal.removeItem(ItemResource.of(toPlace), 1, false);
        }
    }
}
