package com.mowmaster.ironpedestals.Transport;

import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.mowlib.transport.FluidTransportType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// The pedestal's fluid storage: MowLib's fluid storage (capacity per tank from getCapacity, filter, saving...), with
// as many tanks as the pedestal tier has (BasePedestalBlockEntity#getFluidTankCount) instead of one. Each tank holds
// its own fluid. MowLib's storage always makes one tank; NeoForge's setStacks (protected, so a subclass can call it)
// changes the number. (MowLib TODO in CLAUDE.md: a tank-count hook in MowLib would replace this class.)
//
// Reading it: BasePedestalBlockEntity#findFluid / #getFluidAmount, never getResource(0) alone. insert / extract
// without an index already work across every tank.
public class PedestalFluidStorage extends FluidTransportType.Storage
{
    private final BasePedestalBlockEntity pedestal;
    private final FluidTransportType type;

    public PedestalFluidStorage(BasePedestalBlockEntity pedestal, FluidTransportType type)
    {
        super(pedestal, type);
        this.pedestal = pedestal;
        this.type = type;
        setStacks(NonNullList.withSize(wantedSize(), FluidStack.EMPTY));
    }

    // How many tanks it should have now (at least 1)
    public int wantedSize()
    {
        return Math.max(1, pedestal.getFluidTankCount());
    }

    // Changes the number of tanks to wantedSize (like MowLib's item storage resize). The fluid in tanks that no
    // longer exist is returned, for the pedestal to drop as a packet; with shrink = false it only ever grows (when
    // loading, where nothing can be dropped yet).
    public List<FluidStack> resize(boolean shrink)
    {
        int size = wantedSize();
        if (size == stacks.size() || (!shrink && size < stacks.size())) return List.of();
        List<FluidStack> overflow = new ArrayList<>();
        NonNullList<FluidStack> resized = NonNullList.withSize(size, FluidStack.EMPTY);
        for (int index = 0; index < stacks.size(); index++)
        {
            if (index < size) resized.set(index, stacks.get(index));
            else if (!stacks.get(index).isEmpty()) overflow.add(stacks.get(index));
        }
        setStacks(resized);
        if (!overflow.isEmpty()) pedestal.onStorageChanged(type);
        return overflow;
    }

    // "Water: 1000 mB / 32000 mB, Lava: ..." one per tank that holds something (MowLib's only shows the first tank)
    @Override
    public @Nullable Component describe()
    {
        MutableComponent message = null;
        for (int index = 0; index < size(); index++)
        {
            FluidResource stored = getResource(index);
            if (stored.isEmpty()) continue;
            Component tank = type.describeStored(stored, getAmountAsLong(index), getCapacity());
            if (message == null) message = Component.empty().append(tank);
            else message.append(", ").append(tank);
        }
        return message;
    }

    // ---- One tank at a time, for MowLib code that only looks at index 0 (FluidTransportType#dropInWorld) ----

    // A single tank as a handler of its own. RangedResourceHandler is NeoForge's view of some of another handler's
    // indexes: its index 0 is our tank `index`.
    public ResourceHandler<FluidResource> tank(int index)
    {
        return RangedResourceHandler.ofSingleIndex(this, index);
    }

    // The first tank holding at least amount of this fluid, or null if no single tank does
    public @Nullable ResourceHandler<FluidResource> tankHolding(FluidResource fluid, int amount)
    {
        for (int index = 0; index < size(); index++)
        {
            if (getResource(index).equals(fluid) && getAmountAsLong(index) >= amount) return tank(index);
        }
        return null;
    }

    // MowLib's dropInWorld, tried on each tank in turn until one places (the dropper)
    public boolean dropInWorld(ServerLevel level, BlockPos pos, int amount, @Nullable ServerPlayer cause)
    {
        for (int index = 0; index < size(); index++)
        {
            if (getResource(index).isEmpty()) continue;
            if (type.dropInWorld(level, pos, tank(index), amount, cause)) return true;
        }
        return false;
    }
}
