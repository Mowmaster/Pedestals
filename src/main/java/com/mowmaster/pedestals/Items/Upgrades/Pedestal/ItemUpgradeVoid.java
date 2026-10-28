package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

// Deletes everything in the pedestal of every enabled transport type. A NEUTRAL filter in the pedestal limits what's
// deleted (1.20 behaviour).
public class ItemUpgradeVoid extends ItemUpgradeBase
{
    public ItemUpgradeVoid(Properties properties)
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
        for (PedestalTransportType<?> type : getTransportTypes(upgrade)) voidType(pedestal, type);
    }

    private static <R extends Resource> void voidType(BasePedestalBlockEntity pedestal, PedestalTransportType<R> type)
    {
        PedestalStorage<R> storage = pedestal.getStorage(type);
        if (storage.isEmpty()) return;
        try (Transaction transaction = Transaction.openRoot())
        {
            for (int index = 0; index < storage.size(); index++)
            {
                R resource = storage.getResource(index);
                if (resource.isEmpty() || !pedestal.machineFilterAllows(type, resource)) continue;
                storage.extract(index, resource, storage.getAmountAsInt(index), transaction);
            }
            transaction.commit();
        }
    }
}
