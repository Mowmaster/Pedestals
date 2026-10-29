package com.mowmaster.pedestals.Transport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Storage.StoragePacketItem;
import com.mowmaster.pedestals.api.transport.AmountResource;
import com.mowmaster.pedestals.api.transport.AmountStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorageHost;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class EnergyTransportType extends PedestalTransportType<AmountResource>
{
    public static final AmountResource ENERGY = new AmountResource(Identifier.fromNamespaceAndPath("pedestals", "energy"));

    public EnergyTransportType()
    {
        super(ChatFormatting.RED);
    }

    @Override
    public PedestalStorage<AmountResource> createStorage(PedestalStorageHost host)
    {
        return new Storage(host, this);
    }

    @Override
    public int getTransferRate(PedestalStorageHost host)
    {
        return Config.ENERGY_TRANSFER_RATE.getAsInt();
    }

    // Other mods' cables see a normal NeoForge EnergyHandler
    @Override
    public void registerCapability(RegisterCapabilitiesEvent event, BlockEntityType<BasePedestalBlockEntity> pedestal)
    {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, pedestal,
                (blockEntity, side) -> new EnergyView((Storage) blockEntity.getStorage(this)));
    }

    // ---- Other things' storage: NeoForge energy handlers, seen as resource handlers ----

    @Override
    public @Nullable ResourceHandler<AmountResource> getBlockHandler(Level level, BlockPos pos, @Nullable Direction side)
    {
        return HandlerView.of(level.getCapability(Capabilities.Energy.BLOCK, pos, side));
    }

    @Override
    public @Nullable ResourceHandler<AmountResource> getEntityHandler(Entity entity, @Nullable Direction side)
    {
        return HandlerView.of(Capabilities.Energy.ENTITY.getCapability(entity, side));
    }

    // Batteries and other charged items
    @Override
    public @Nullable ResourceHandler<AmountResource> getItemHandler(ItemAccess access)
    {
        return HandlerView.of(access.getCapability(Capabilities.Energy.ITEM));
    }

    // ---- Upgrades (the capacity modifier is the default name, "upgradeenergycapacity") ----

    @Override
    public int getDefaultUpgradeRate(PedestalStorageHost host)
    {
        return Config.ENERGY_TRANSFER_RATE.getAsInt();
    }

    // Dropper: a lightning bolt, costing amount energy (as in 1.20)
    @Override
    public boolean dropInWorld(ServerLevel level, BlockPos pos, PedestalStorage<AmountResource> from, int amount, @Nullable ServerPlayer cause)
    {
        /*if (!Config.DROPPER_LIGHTNING.getAsBoolean() || amount <= 0) return false;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt == null) return false;
        try (Transaction transaction = Transaction.openRoot())
        {
            if (from.extract(ENERGY, amount, transaction) < amount) return false;
            transaction.commit();
        }
        bolt.snapTo(Vec3.atBottomCenterOf(pos));
        bolt.setCause(cause);
        level.addFreshEntity(bolt);
        level.playSound(null, pos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.WEATHER, 5.0F, 1.0F);
         */
        return true;
    }

    // ---- Packets: the resource is always ENERGY, so nothing needs saving but the amount ----

    @Override
    public Codec<AmountResource> getResourceCodec()
    {
        return MapCodec.unitCodec(ENERGY);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AmountResource> getResourceStreamCodec()
    {
        return StreamCodec.unit(ENERGY);
    }

    public static class Storage extends AmountStorage
    {
        public Storage(PedestalStorageHost host, EnergyTransportType type)
        {
            super(host, type, ENERGY);
        }

        @Override
        public int getCapacity()
        {
            return Config.ENERGY_STORAGE.getAsInt();
        }

        // The energy drops as a mechanical energy storage packet (as in 1.20)
        @Override
        public void onPedestalRemoved(Level level, BlockPos pos)
        {
            StoragePacketItem.dropStorage(level, pos, type, this);
        }

        @Override
        public @Nullable Component describe()
        {
            if (amount <= 0) return null;
            return Component.translatable("pedestals.pedestal.message_energy")
                    .append(": " + amount + "/" + getAllowedCapacity())
                    .withStyle(type.getColor());
        }
    }

    // The storage seen as a NeoForge EnergyHandler. A record (see AmountResource) holding the storage it wraps.
    public record EnergyView(Storage storage) implements EnergyHandler
    {
        @Override
        public long getAmountAsLong()
        {
            return storage.getAmount();
        }

        @Override
        public long getCapacityAsLong()
        {
            return storage.getCapacityAsLong(0, ENERGY);
        }

        @Override
        public int insert(int amount, TransactionContext transaction)
        {
            return amount <= 0 ? 0 : storage.insert(0, ENERGY, amount, transaction);
        }

        @Override
        public int extract(int amount, TransactionContext transaction)
        {
            return amount <= 0 ? 0 : storage.extract(0, ENERGY, amount, transaction);
        }
    }

    // The other way round: another mod's EnergyHandler seen as a ResourceHandler of ENERGY, so upgrades can move
    // energy with the same code as items and fluids
    public record HandlerView(EnergyHandler handler) implements ResourceHandler<AmountResource>
    {
        public static @Nullable HandlerView of(@Nullable EnergyHandler handler)
        {
            return handler == null ? null : new HandlerView(handler);
        }

        @Override
        public int size()
        {
            return 1;
        }

        @Override
        public AmountResource getResource(int index)
        {
            return handler.getAmountAsLong() > 0 ? ENERGY : AmountResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index)
        {
            return handler.getAmountAsLong();
        }

        @Override
        public long getCapacityAsLong(int index, AmountResource resource)
        {
            return handler.getCapacityAsLong();
        }

        @Override
        public boolean isValid(int index, AmountResource resource)
        {
            return ENERGY.equals(resource);
        }

        @Override
        public int insert(int index, AmountResource resource, int amount, TransactionContext transaction)
        {
            return ENERGY.equals(resource) && amount > 0 ? handler.insert(amount, transaction) : 0;
        }

        @Override
        public int extract(int index, AmountResource resource, int amount, TransactionContext transaction)
        {
            return ENERGY.equals(resource) && amount > 0 ? handler.extract(amount, transaction) : 0;
        }
    }

    // A lightning bolt: electric sparks along a zig-zag line
    @Override
    public void addTransferParticles(Level level, Vec3 from, Vec3 to, @Nullable AmountResource resource, RandomSource random)
    {
        int segments = 8;
        Vec3 previous = from;
        for (int segment = 1; segment <= segments; segment++)
        {
            Vec3 next = from.lerp(to, (double) segment / segments);
            if (segment < segments) next = next.add(random.nextGaussian() * 0.15, random.nextGaussian() * 0.15, random.nextGaussian() * 0.15);
            for (int spark = 0; spark < 3; spark++)
            {
                Vec3 at = previous.lerp(next, random.nextDouble());
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 0, 0, 0);
            }
            previous = next;
        }
    }
}
