package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.PedestalTags;
import com.mowmaster.pedestals.Transport.FluidTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
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
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.BLOCK_CAPACITY, UpgradeModifiers.RANGE, UpgradeModifiers.AREA);
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
        FluidTransportType fluids = PedestalTransportTypes.FLUIDS.get();
        PedestalStorage<FluidResource> storage = pedestal.getStorage(fluids);
        if (positions.isEmpty() || storage.getAmountAsInt(0) < FluidType.BUCKET_VOLUME) return;
        FluidResource stored = storage.getResource(0);
        if (stored.getFluid().is(PedestalTags.CANNOT_PLACE_FLUID) || !pedestal.machineFilterAllows(fluids, stored)) return;

        int blocks = Config.DRAIN_BLOCKS.getAsInt() + getModifier(upgrade, UpgradeModifiers.BLOCK_CAPACITY);
        int index = pedestal.getUpgradeData().getIntOr("position", 0);
        if (index >= positions.size()) index = 0;
        for (int checked = 0; checked < positions.size() && blocks > 0 && storage.getAmountAsInt(0) >= FluidType.BUCKET_VOLUME; checked++)
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

    private static boolean place(ServerLevel level, BasePedestalBlockEntity pedestal, PedestalStorage<FluidResource> storage,
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
        // Otherwise the same as the dropper: a source block where there's room (water boils away in the nether)
        return PedestalTransportTypes.FLUIDS.get().dropInWorld(level, target, storage, FluidType.BUCKET_VOLUME, pedestal.getFakePlayer(level));
    }
}
