package com.mowmaster.pedestals.Transport;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.api.filter.FilterDirection;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorageHost;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class FluidTransportType extends PedestalTransportType<FluidResource>
{
    public FluidTransportType()
    {
        super(ChatFormatting.BLUE);
    }

    @Override
    public PedestalStorage<FluidResource> createStorage(PedestalStorageHost host)
    {
        return new Storage(host, this);
    }

    @Override
    public int getTransferRate(PedestalStorageHost host)
    {
        return Config.FLUID_TRANSFER_RATE.getAsInt();
    }

    @Override
    public void registerCapability(RegisterCapabilitiesEvent event, BlockEntityType<BasePedestalBlockEntity> pedestal)
    {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, pedestal, (blockEntity, side) -> blockEntity.getStorage(this));
    }

    // Fluid filters are set from an inventory of fluid containers (buckets, tanks...): each stands for its fluid
    @Override
    public boolean usesFilterList()
    {
        return true;
    }

    @Override
    public @Nullable FluidResource resourceFromFilterEntry(ItemStack entry)
    {
        FluidStack contained = FluidUtil.getFirstStackContained(entry);
        return contained.isEmpty() ? null : FluidResource.of(contained);
    }

    @Override
    public boolean isSameKind(FluidResource first, FluidResource second)
    {
        return first.getFluid().isSame(second.getFluid());
    }

    @Override
    public @Nullable Identifier getRegistryName(FluidResource resource)
    {
        return BuiltInRegistries.FLUID.getKey(resource.getFluid());
    }

    // One tank, with the pedestal's filter applied. Its capacity is read each time so config (and later augments)
    // changes apply straight away.
    public static class Storage extends FluidStacksResourceHandler implements PedestalStorage<FluidResource>
    {
        private final PedestalStorageHost host;
        private final FluidTransportType type;

        public Storage(PedestalStorageHost host, FluidTransportType type)
        {
            super(1, Config.FLUID_STORAGE.getAsInt());
            this.host = host;
            this.type = type;
        }

        public int getCapacity()
        {
            return Config.FLUID_STORAGE.getAsInt();
        }

        @Override
        public boolean isValid(int index, FluidResource resource)
        {
            return host.filterAllows(type, resource, FilterDirection.INSERT);
        }

        @Override
        protected int getCapacity(int index, FluidResource resource)
        {
            return resource.isEmpty() ? getCapacity() : host.filterLimit(type, resource, getCapacity());
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction)
        {
            if (!host.filterAllows(type, resource, FilterDirection.EXTRACT)) return 0;
            return super.extract(index, resource, amount, transaction);
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents)
        {
            host.onStorageChanged(type);
        }

        // Fluid can't be dropped as an item yet; 1.20's mechanical fluid storage item comes back with the upgrades
        @Override
        public void onPedestalRemoved(Level level, BlockPos pos)
        {
        }

        // "Water: 1000/16000"
        @Override
        public @Nullable Component describe()
        {
            FluidStack stored = FluidUtil.getStack(this, 0);
            if (stored.isEmpty()) return null;
            return Component.empty().append(stored.getHoverName())
                    .append(": " + stored.getAmount() + "/" + getCapacity())
                    .withStyle(type.getColor());
        }
    }
}
