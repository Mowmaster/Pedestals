package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// Pulls things lying in the work card's area into the pedestal, for every enabled transport type: items, xp orbs and
// bottles o' enchanting, fluid and energy out of items (buckets, batteries), and whatever add-on types absorb.
// One entity per operation, or all of them with the super speed modifier.
public class ItemUpgradeMagnet extends ItemUpgradeBase
{
    public ItemUpgradeMagnet(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.SUPER_SPEED, "upgradexpcapacity", "upgradeenergycapacity",
                UpgradeModifiers.RANGE, UpgradeModifiers.AREA);
    }

    @Override
    public boolean canModify(ItemStack upgrade, String modifier)
    {
        if (modifier.equals(UpgradeModifiers.AREA)) return Config.REQUIRE_SIZED_AREA.getAsBoolean();
        return super.canModify(upgrade, modifier);
    }

    @Override
    public boolean usesTransportModes()
    {
        return true;
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.AREA;
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.MAGNET_FUEL;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        Optional<AABB> area = pedestal.getWorkArea();
        if (area.isEmpty()) return;
        boolean superSpeed = hasModifier(upgrade, UpgradeModifiers.SUPER_SPEED);

        // Anything that isn't alive: item entities, xp orbs, and add-on types' entities
        for (Entity entity : level.getEntities((Entity) null, area.get(), entity -> entity.isAlive() && !(entity instanceof LivingEntity)))
        {
            int distance = distance(pedestal.getBlockPos(), entity.blockPosition());
            if (!useFuel(pedestal, distance, true)) return;
            boolean taken = false;
            for (PedestalTransportType<?> type : getTransportTypes(upgrade))
            {
                if (!entity.isAlive()) break; // a type took all of it
                taken |= absorb(level, pedestal, upgrade, type, entity);
            }
            if (taken)
            {
                useFuel(pedestal, distance, false);
                pedestal.spawnParticles(level, entity.getX(), entity.getY() + 0.25, entity.getZ(), 0xB4B400);
                if (!superSpeed) return;
            }
        }
    }

    private <R extends Resource> boolean absorb(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade,
                                                PedestalTransportType<R> type, Entity entity)
    {
        // Items and experience: as much as fits (1.20 had no limit); other types: the import upgrade's rate
        boolean unlimited = type == PedestalTransportTypes.ITEMS.get() || type == PedestalTransportTypes.EXPERIENCE.get();
        int amount = unlimited ? Integer.MAX_VALUE : getRate(upgrade, type, Config.IMPORT_RATES.get(type, pedestal));
        return type.absorbEntity(level, entity, pedestal.getStorage(type), amount);
    }
}
