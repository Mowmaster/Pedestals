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
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class ItemTransportType extends PedestalTransportType<ItemResource>
{
    public ItemTransportType()
    {
        super(ChatFormatting.GOLD);
    }

    @Override
    public PedestalStorage<ItemResource> createStorage(PedestalStorageHost host)
    {
        return new Storage(host, this);
    }

    @Override
    public int getTransferRate(PedestalStorageHost host)
    {
        return Config.ITEM_TRANSFER_RATE.getAsInt();
    }

    // The item on top is drawn by the client
    @Override
    public boolean syncsToClient()
    {
        return true;
    }

    @Override
    public void registerCapability(RegisterCapabilitiesEvent event, BlockEntityType<BasePedestalBlockEntity> pedestal)
    {
        event.registerBlockEntity(Capabilities.Item.BLOCK, pedestal, (blockEntity, side) -> blockEntity.getStorage(this));
    }

    @Override
    public boolean usesFilterList()
    {
        return true;
    }

    @Override
    public @Nullable ItemResource resourceFromFilterEntry(ItemStack entry)
    {
        return entry.isEmpty() ? null : ItemResource.of(entry);
    }

    @Override
    public boolean isSameKind(ItemResource first, ItemResource second)
    {
        return first.getItem() == second.getItem();
    }

    @Override
    public @Nullable Identifier getRegistryName(ItemResource resource)
    {
        return BuiltInRegistries.ITEM.getKey(resource.getItem());
    }

    // NeoForge's item stack storage, with the pedestal's filter applied
    public static class Storage extends ItemStacksResourceHandler implements PedestalStorage<ItemResource>
    {
        private final PedestalStorageHost host;
        private final ItemTransportType type;

        public Storage(PedestalStorageHost host, ItemTransportType type)
        {
            super(Config.ITEM_SLOTS.getAsInt());
            this.host = host;
            this.type = type;
        }

        @Override
        public boolean isValid(int index, ItemResource resource)
        {
            return host.filterAllows(type, resource, FilterDirection.INSERT);
        }

        @Override
        protected int getCapacity(int index, ItemResource resource)
        {
            int capacity = super.getCapacity(index, resource);
            return resource.isEmpty() ? capacity : Math.min(capacity, host.filterLimit(type, resource, capacity));
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction)
        {
            if (!host.filterAllows(type, resource, FilterDirection.EXTRACT)) return 0;
            return super.extract(index, resource, amount, transaction);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents)
        {
            host.onStorageChanged(type);
        }

        @Override
        public void onPedestalRemoved(Level level, BlockPos pos)
        {
            for (int index = 0; index < size(); index++)
            {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), ItemUtil.getStack(this, index));
            }
        }

        // "Stone 32, Dirt 5"
        @Override
        public @Nullable Component describe()
        {
            if (isEmpty()) return null;
            var message = Component.empty();
            boolean first = true;
            for (int index = 0; index < size(); index++)
            {
                ItemStack stack = ItemUtil.getStack(this, index);
                if (stack.isEmpty()) continue;
                if (!first) message.append(", ");
                message.append(stack.getHoverName()).append(" " + stack.getCount());
                first = false;
            }
            return message.withStyle(type.getColor());
        }
    }
}
