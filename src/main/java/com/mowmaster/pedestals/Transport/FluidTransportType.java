package com.mowmaster.pedestals.Transport;

import com.mojang.serialization.Codec;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.ClientSide.ClientTransferParticles;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Storage.StoragePacketItem;
import com.mowmaster.pedestals.api.filter.FilterDirection;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorageHost;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
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
        return Config.FLUID_TRANSFER_RATE.getAsInt() + host.getTransferBonus(this);
    }

    // ---- Augments ----

    @Override
    public int getCapacityAugmentBonus(int tier)
    {
        return tier < 1 || tier > Config.CAPACITY_AUGMENTS.size() ? 0 : Config.CAPACITY_AUGMENTS.get(tier - 1).fluids().getAsInt();
    }

    @Override
    public int getStorageAugmentBonus(int tier)
    {
        return tier < 1 || tier > Config.STORAGE_AUGMENTS.size() ? 0 : Config.STORAGE_AUGMENTS.get(tier - 1).fluids().getAsInt();
    }

    @Override
    public void registerCapability(RegisterCapabilitiesEvent event, BlockEntityType<BasePedestalBlockEntity> pedestal)
    {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, pedestal, (blockEntity, side) -> blockEntity.getStorage(this));
    }

    // ---- Other things' storage ----

    @Override
    public @Nullable ResourceHandler<FluidResource> getBlockHandler(Level level, BlockPos pos, @Nullable Direction side)
    {
        return level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
    }

    @Override
    public @Nullable ResourceHandler<FluidResource> getEntityHandler(Entity entity, @Nullable Direction side)
    {
        return Capabilities.Fluid.ENTITY.getCapability(entity, side);
    }

    // Buckets, bottles, tanks...
    @Override
    public @Nullable ResourceHandler<FluidResource> getItemHandler(ItemAccess access)
    {
        return access.getCapability(Capabilities.Fluid.ITEM);
    }

    // ---- Upgrades ----

    @Override
    public String getCapacityModifier()
    {
        return "upgradefluidcapacity";
    }

    @Override
    public int getDefaultUpgradeRate(PedestalStorageHost host)
    {
        return Config.FLUID_TRANSFER_RATE.getAsInt();
    }

    // Dropper: places a source block (a bucket's worth) where there's room for one. Water boils away in the nether,
    // like emptying a bucket there. (1.20 had a config for this but never dropped fluids.)
    @Override
    public boolean dropInWorld(ServerLevel level, BlockPos pos, PedestalStorage<FluidResource> from, int amount, @Nullable ServerPlayer cause)
    {
        FluidResource stored = from.getResource(0);
        if (stored.isEmpty() || amount < FluidType.BUCKET_VOLUME) return false;
        FluidStack bucket = stored.toStack(FluidType.BUCKET_VOLUME);
        BlockState there = level.getBlockState(pos);
        if (!there.isAir() && !there.canBeReplaced(stored.getFluid())) return false;

        FluidType fluidType = stored.getFluid().getFluidType();
        FluidState toPlace = fluidType.getStateForPlacement(level, pos, bucket);
        if (!fluidType.canBePlacedInLevel(level, pos, bucket)) return false;

        try (Transaction transaction = Transaction.openRoot())
        {
            if (from.extract(stored, FluidType.BUCKET_VOLUME, transaction) < FluidType.BUCKET_VOLUME) return false;
            transaction.commit();
        }
        if (fluidType.isVaporizedOnPlacement(level, pos, bucket))
        {
            fluidType.onVaporize(cause, level, pos, bucket);
        }
        else
        {
            level.setBlock(pos, toPlace.createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
        }
        return true;
    }

    // ---- Packets ----

    @Override
    public Codec<FluidResource> getResourceCodec()
    {
        return FluidResource.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FluidResource> getResourceStreamCodec()
    {
        return FluidResource.STREAM_CODEC;
    }

    // "Water: 16000mB"
    @Override
    public Component describeAmount(FluidResource resource, int amount)
    {
        return Component.empty().append(resource.getHoverName()).append(": " + amount + "mB");
    }

    // ---- Filters ----

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
            return Config.FLUID_STORAGE.getAsInt() + host.getStorageBonus(type);
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

        // The fluid drops as a mechanical fluid storage packet (as in 1.20)
        @Override
        public void onPedestalRemoved(Level level, BlockPos pos)
        {
            StoragePacketItem.dropStorage(level, pos, type, this);
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

    // Bubbles in the fluid's colour. Fluid colours are only known on the client, so the work is in a client class.
    @Override
    public void addTransferParticles(Level level, Vec3 from, Vec3 to, @Nullable FluidResource resource, RandomSource random)
    {
        if (resource == null || resource.isEmpty()) super.addTransferParticles(level, from, to, resource, random);
        else ClientTransferParticles.fluid(level, from, to, resource, random);
    }
}
