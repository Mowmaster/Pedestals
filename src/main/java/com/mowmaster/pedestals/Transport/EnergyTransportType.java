package com.mowmaster.pedestals.Transport;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.api.transport.AmountResource;
import com.mowmaster.pedestals.api.transport.AmountStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorageHost;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
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

        // Energy is lost when the pedestal is broken (1.20's mechanical energy storage item comes back with upgrades)
        @Override
        public void onPedestalRemoved(Level level, BlockPos pos)
        {
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
}
