package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Storage.PacketContents;
import com.mowmaster.pedestals.Items.Storage.StoragePacketItem;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

// Makes storage packets, putting them in the pedestal (to be sent on by its links):
//   items: when every slot of the inventory below holds a full stack of the same item, takes them all (up to the
//          config's max stacks) as one item packet
//   every other enabled type (fluids, energy, experience, add-on types with a packet item): when the pedestal's
//          storage is full, empties it into a packet
// Packets are never packaged again. (1.20's packets slowly discharged; these don't.)
public class ItemUpgradePackager extends ItemUpgradeBase
{
    public ItemUpgradePackager(Properties properties)
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
        for (PedestalTransportType<?> type : getTransportTypes(upgrade))
        {
            if (type == PedestalTransportTypes.ITEMS.get()) packageInventory(level, pedestal);
            else packageStorage(pedestal, type);
        }
    }

    private static void packageInventory(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        ResourceHandler<ItemResource> inventory = getInventoryBelow(level, pedestal);
        if (inventory == null || inventory.size() == 0) return;

        // Every slot: a full stack of the same item, and not a packet
        ItemResource item = inventory.getResource(0);
        if (item.isEmpty() || StoragePacketItem.isPacket(item.toStack())) return;
        for (int index = 0; index < inventory.size(); index++)
        {
            if (!inventory.getResource(index).equals(item) || inventory.getAmountAsInt(index) < item.getMaxStackSize()) return;
        }

        int slots = Math.min(inventory.size(), Config.PACKAGE_MAX_STACKS.getAsInt());
        try (Transaction transaction = Transaction.openRoot())
        {
            int total = 0;
            for (int index = 0; index < slots; index++)
            {
                total += inventory.extract(index, item, inventory.getAmountAsInt(index), transaction);
            }
            ItemStack packet = StoragePacketItem.create(new PacketContents<>(PedestalTransportTypes.ITEMS.get(), List.of(new ResourceStack<>(item, total))));
            if (!insertPacket(pedestal, packet, transaction)) return;
            transaction.commit();
        }
    }

    private static <R extends Resource> void packageStorage(BasePedestalBlockEntity pedestal, PedestalTransportType<R> type)
    {
        PedestalStorage<R> storage = pedestal.getStorage(type);
        if (StoragePacketItem.forType(type) == null || storage.isEmpty() || !ResourceHandlerUtil.isFull(storage)) return;
        try (Transaction transaction = Transaction.openRoot())
        {
            ItemStack packet = StoragePacketItem.create(StoragePacketItem.extractAll(type, storage, transaction));
            if (!insertPacket(pedestal, packet, transaction)) return;
            transaction.commit();
        }
    }

    // false (so the caller doesn't commit) if the packet doesn't fit in the pedestal
    private static boolean insertPacket(BasePedestalBlockEntity pedestal, ItemStack packet, Transaction transaction)
    {
        return !packet.isEmpty() && ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(packet), 1, transaction) == 1;
    }
}
