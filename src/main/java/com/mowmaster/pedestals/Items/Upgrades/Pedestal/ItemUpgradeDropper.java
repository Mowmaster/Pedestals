package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resource.Resource;

import java.util.List;

// Drops the pedestal's contents into the world at the work card's positions, one position per operation in turn:
// items (as item entities), experience (orbs), energy (lightning bolts) and fluids (source blocks), each if enabled.
// Add-on types drop however their PedestalTransportType#dropInWorld says.
public class ItemUpgradeDropper extends ItemUpgradeBase
{
    public ItemUpgradeDropper(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, "upgradeitemcapacity", "upgradexpcapacity", UpgradeModifiers.RANGE, UpgradeModifiers.AREA);
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
        return WorkCardType.AREA_OR_LOCATIONS;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        List<BlockPos> positions = pedestal.getWorkPositions();
        if (positions.isEmpty()) return;
        BlockPos target = positions.get(nextIndex(pedestal, "position", positions.size()));
        if (target.equals(pedestal.getBlockPos())) return;

        ServerPlayer cause = pedestal.getFakePlayer(level);
        for (PedestalTransportType<?> type : getTransportTypes(upgrade))
        {
            if (drop(level, pedestal, upgrade, type, target, cause)) pedestal.spawnParticles(level, target, type.getRgb());
        }
    }

    private <R extends Resource> boolean drop(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade,
                                              PedestalTransportType<R> type, BlockPos target, ServerPlayer cause)
    {
        if (pedestal.getStorage(type).isEmpty()) return false;
        int amount = getRate(upgrade, type, Config.DROPPER_RATES.get(type, pedestal));
        return type.dropInWorld(level, target, pedestal.getStorage(type), amount, cause);
    }
}
