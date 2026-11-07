package com.mowmaster.ironpedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.transport.FluidTransportType;
import com.mowmaster.mowlib.transport.TransportStorage;
import com.mowmaster.mowlib.transport.TransportTypes;
import com.mowmaster.mowlib.workcard.WorkCardType;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.Config;
import com.mowmaster.ironpedestals.Transport.PedestalFluidStorage;
import com.mowmaster.ironpedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.ironpedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.ironpedestals.IronPedestalTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Places the pedestal's fluid as source blocks at the work card's positions: into air, flowing fluid, or (water only)
// waterloggable blocks. Up to (4 + block capacity) per operation, a bucket's worth each.
public class ItemUpgradeDrain extends ItemUpgradeBase
{
    public ItemUpgradeDrain(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.BLOCK_CAPACITY);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.AREA_OR_LOCATIONS;
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.DRAIN_FUEL;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        List<BlockPos> positions = pedestal.getWorkPositions();
        FluidTransportType fluids = TransportTypes.FLUIDS.get();
        TransportStorage<FluidResource> storage = pedestal.getStorage(fluids);
        if (positions.isEmpty()) return;
        // The first tank's fluid that may be placed, that the filter allows, and that there's a bucket of
        FluidResource stored = pedestal.findFluid(fluid -> !fluid.getFluid().is(IronPedestalTags.CANNOT_PLACE_FLUID)
                && pedestal.machineFilterAllows(fluids, fluid) && pedestal.getFluidAmount(fluid) >= FluidType.BUCKET_VOLUME);
        if (stored.isEmpty()) return;

        int blocks = Config.DRAIN_BLOCKS.getAsInt() + getModifier(upgrade, UpgradeModifiers.BLOCK_CAPACITY);
        int index = pedestal.getUpgradeData().getIntOr("position", 0);
        if (index >= positions.size()) index = 0;
        for (int checked = 0; checked < positions.size() && blocks > 0 && pedestal.getFluidAmount(stored) >= FluidType.BUCKET_VOLUME; checked++)
        {
            BlockPos target = positions.get(index);
            index = (index + 1) % positions.size();
            if (target.equals(pedestal.getBlockPos())) continue;
            BlockState state = level.getBlockState(target);
            if (state.getFluidState().isSource()) continue; // already full
            int distance = distance(pedestal.getBlockPos(), target);
            if (!useFuel(pedestal, distance, true)) break;
            if (place(level, pedestal, storage, stored, target, state))
            {
                useFuel(pedestal, distance, false);
                blocks--;
            }
        }
        pedestal.getUpgradeData().putInt("position", index);
        pedestal.upgradeDataChanged();
    }

    private static boolean place(ServerLevel level, BasePedestalBlockEntity pedestal, TransportStorage<FluidResource> storage,
                                 FluidResource stored, BlockPos target, BlockState state)
    {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED))
        {
            if (!stored.getFluid().isSame(Fluids.WATER) || state.getValue(BlockStateProperties.WATERLOGGED)) return false;
            try (Transaction transaction = Transaction.openRoot())
            {
                if (storage.extract(stored, FluidType.BUCKET_VOLUME, transaction) < FluidType.BUCKET_VOLUME) return false;
                transaction.commit();
            }
            level.setBlockAndUpdate(target, state.setValue(BlockStateProperties.WATERLOGGED, true));
            return true;
        }
        // Otherwise the same as the dropper: a source block where there's room (water boils away in the nether).
        // MowLib's dropInWorld only looks at a storage's first tank, so it gets just the tank holding this fluid.
        ResourceHandler<FluidResource> tank = storage instanceof PedestalFluidStorage tanks ? tanks.tankHolding(stored, FluidType.BUCKET_VOLUME) : storage;
        if (tank == null) return false;
        return TransportTypes.FLUIDS.get().dropInWorld(level, target, tank, FluidType.BUCKET_VOLUME, pedestal.getFakePlayer(level));
    }
}
