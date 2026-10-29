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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import java.util.List;

// Pulls from the block below into the pedestal: the first thing it can take of every enabled transport type
// (items, fluids, energy, experience, add-on types). Modifiers: speed, capacity (per type), entity containers
// (also pull from chest minecarts etc.), remote storage (pull from up to 8 locations on a locations work card instead).
public class ItemUpgradeImport extends ItemUpgradeBase
{
    public ItemUpgradeImport(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.ANY_CAPACITY, UpgradeModifiers.ENTITY_CONTAINERS, UpgradeModifiers.REMOTE_STORAGE);
    }

    @Override
    public boolean usesTransportModes()
    {
        return true;
    }

    // Only with the remote storage modifier
    @Override
    public @Nullable WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return hasModifier(upgrade, UpgradeModifiers.REMOTE_STORAGE) ? WorkCardType.LOCATIONS : null;
    }

    // The block below, or the work card's locations (at most 8)
    protected static List<BlockPos> getTargets(BasePedestalBlockEntity pedestal, ItemStack upgrade, ItemUpgradeBase base)
    {
        if (!base.hasModifier(upgrade, UpgradeModifiers.REMOTE_STORAGE)) return List.of(pedestal.getPosBelow());
        List<BlockPos> positions = pedestal.getWorkPositions();
        return positions.size() > 8 ? List.of() : positions;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        boolean entities = hasModifier(upgrade, UpgradeModifiers.ENTITY_CONTAINERS);
        for (BlockPos target : getTargets(pedestal, upgrade, this))
        {
            // Never pull out of other pedestals (that's what links are for)
            if (level.getBlockEntity(target) instanceof BasePedestalBlockEntity) return;
            boolean moved = false;
            for (PedestalTransportType<?> type : getTransportTypes(upgrade))
            {
                moved |= importType(level, pedestal, upgrade, type, target, pedestal.getFacing(), entities);
            }
            if (moved) break; // one location per operation, as in 1.20
        }
    }

    private <R extends Resource> boolean importType(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade,
                                                    PedestalTransportType<R> type, BlockPos target, Direction side, boolean entities)
    {
        ResourceHandler<R> from = findHandler(level, target, side, type, entities);
        if (from == null) return false;
        int rate = getRate(upgrade, type, Config.IMPORT_RATES.get(type, pedestal));
        try (Transaction transaction = Transaction.openRoot())
        {
            // The first thing that can move, up to the rate (1.20 took from the first slot that had something)
            if (ResourceHandlerUtil.moveFirstStacking(from, pedestal.getStorage(type), resource -> true, rate, transaction) == null) return false;
            transaction.commit();
            return true;
        }
    }

    // With the collision augment: also pulls from what touches the pedestal (see ItemUpgradeBase#pullFromTouching)
    @Override
    public void onCollide(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade, List<Entity> touching)
    {
        pullFromTouching(level, pedestal, upgrade, touching);
    }
}
