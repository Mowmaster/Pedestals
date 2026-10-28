package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

// Pushes from the pedestal into the block below, for every enabled transport type. Same modifiers as the import upgrade.
public class ItemUpgradeExport extends ItemUpgradeBase
{
    public ItemUpgradeExport(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.ANY_CAPACITY, UpgradeModifiers.ENTITY_CONTAINERS, UpgradeModifiers.REMOTE_STORAGE);
    }

    @Override
    public boolean usesTransportModes()
    {
        return true;
    }

    @Override
    public @Nullable WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return hasModifier(upgrade, UpgradeModifiers.REMOTE_STORAGE) ? WorkCardType.LOCATIONS : null;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        boolean entities = hasModifier(upgrade, UpgradeModifiers.ENTITY_CONTAINERS);
        for (BlockPos target : ItemUpgradeImport.getTargets(pedestal, upgrade, this))
        {
            // Pedestals are skipped (1.20 behaviour), the other locations still get their turn
            if (level.getBlockEntity(target) instanceof BasePedestalBlockEntity) continue;
            boolean moved = false;
            for (PedestalTransportType<?> type : getTransportTypes(upgrade))
            {
                moved |= exportType(level, pedestal, upgrade, type, target, pedestal.getFacing(), entities);
            }
            if (moved) break;
        }
    }

    private <R extends Resource> boolean exportType(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade,
                                                    PedestalTransportType<R> type, BlockPos target, Direction side, boolean entities)
    {
        ResourceHandler<R> to = findHandler(level, target, side, type, entities);
        if (to == null || pedestal.getStorage(type).isEmpty()) return false;
        int rate = getRate(upgrade, type, Config.EXPORT_RATES.get(type, pedestal));
        try (Transaction transaction = Transaction.openRoot())
        {
            if (type.transfer(pedestal.getStorage(type), to, rate, transaction) <= 0) return false;
            transaction.commit();
            return true;
        }
    }
}
