package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Storage.PacketContents;
import com.mowmaster.pedestals.Items.Storage.StoragePacketItem;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

// Empties storage packets that are in the pedestal, for every enabled transport type: the contents go into the
// pedestal's storage of that type, and what doesn't fit stays in the packet. Empty packets are removed.
// Item packets empty into the inventory below the pedestal if there is one (the pedestal's own slot is usually
// taken by the packet itself), otherwise into the pedestal.
public class ItemUpgradeUnpackager extends ItemUpgradeBase
{
    public ItemUpgradeUnpackager(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED);
    }

    @Override
    public boolean usesTransportModes()
    {
        return true;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        PedestalStorage<ItemResource> items = pedestal.getItemStorage();
        for (int index = 0; index < items.size(); index++)
        {
            ItemResource resource = items.getResource(index);
            if (resource.isEmpty() || !StoragePacketItem.isPacket(resource.toStack())) continue;
            PacketContents<?> contents = StoragePacketItem.getContents(resource.toStack());
            if (contents != null && !canTransport(upgrade, contents.type())) continue;
            if (unpack(level, pedestal, resource, contents)) return; // one packet per operation
        }
    }

    private static <R extends Resource> boolean unpack(ServerLevel level, BasePedestalBlockEntity pedestal, ItemResource packet, PacketContents<R> contents)
    {
        PedestalStorage<ItemResource> items = pedestal.getItemStorage();
        try (Transaction transaction = Transaction.openRoot())
        {
            // The packet comes out first, so its slot is free for what was in it
            if (items.extract(packet, 1, transaction) != 1) return false;
            if (contents == null || contents.isEmpty())
            {
                transaction.commit(); // an empty packet: just remove it
                return true;
            }

            PacketContents<R> left = StoragePacketItem.insertInto(contents, getTarget(level, pedestal, contents), transaction);
            if (left.totalAmount() == contents.totalAmount()) return false; // nothing fitted
            if (!left.isEmpty())
            {
                ItemStack rest = StoragePacketItem.create(left);
                if (ResourceHandlerUtil.insertStacking(items, ItemResource.of(rest), 1, transaction) != 1) return false;
            }
            transaction.commit();
            return true;
        }
    }

    // Where a packet's contents go. The casts are safe: an item packet's resources are ItemResources.
    @SuppressWarnings("unchecked")
    private static <R extends Resource> ResourceHandler<R> getTarget(ServerLevel level, BasePedestalBlockEntity pedestal, PacketContents<R> contents)
    {
        if (contents.type() == PedestalTransportTypes.ITEMS.get())
        {
            ResourceHandler<ItemResource> below = getInventoryBelow(level, pedestal);
            if (below != null) return (ResourceHandler<R>) (ResourceHandler<?>) below;
        }
        return pedestal.getStorage(contents.type());
    }
}
