package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeWorld;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Places the pedestal's block items at the work card's positions, one per operation, in turn. It waits at a position
// until it's empty (so it pairs with a breaker), as in 1.20. Plants are the planter's job.
public class ItemUpgradePlacer extends ItemUpgradeBase
{
    public ItemUpgradePlacer(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.RANGE, UpgradeModifiers.AREA);
    }

    @Override
    public boolean canModify(ItemStack upgrade, String modifier)
    {
        if (modifier.equals(UpgradeModifiers.AREA)) return Config.REQUIRE_SIZED_AREA.getAsBoolean();
        return super.canModify(upgrade, modifier);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.AREA_OR_LOCATIONS;
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.PLACER_FUEL;
    }

    // A block the placer handles: a placeable block item, not a plant, passing a NEUTRAL filter
    public static boolean canPlaceWith(BasePedestalBlockEntity pedestal, ItemStack stack)
    {
        return UpgradeWorld.canPlace(stack) && stack.getItem() instanceof BlockItem blockItem
                && !UpgradeWorld.isPlant(blockItem.getBlock()) && pedestal.machineFilterAllows(stack);
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        List<BlockPos> positions = pedestal.getWorkPositions();
        ItemStack toPlace = pedestal.getItemInPedestal();
        if (positions.isEmpty() || !canPlaceWith(pedestal, toPlace)) return;

        int index = pedestal.getUpgradeData().getIntOr("position", 0) % positions.size();
        BlockPos target = positions.get(index);
        if (target.equals(pedestal.getBlockPos()) || !level.getBlockState(target).isAir()) return;
        int distance = distance(pedestal.getBlockPos(), target);
        if (!useFuel(pedestal, distance, true)) return;

        PedestalFakePlayer player = pedestal.getFakePlayer(level);
        if (!UpgradeWorld.placeBlock(level, player, toPlace, target)) return;
        useFuel(pedestal, distance, false);
        pedestal.removeItem(ItemResource.of(toPlace), 1, false);
        pedestal.getUpgradeData().putInt("position", (index + 1) % positions.size());
        pedestal.upgradeDataChanged();
    }
}
