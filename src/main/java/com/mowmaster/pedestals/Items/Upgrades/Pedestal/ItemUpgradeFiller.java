package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

// Fills the work card's area with the pedestal's block items, from the bottom up: up to (4 + block capacity)
// blocks per operation, into every empty or replaceable (grass, water...) space.
public class ItemUpgradeFiller extends ItemUpgradeBase
{
    public ItemUpgradeFiller(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.BLOCK_CAPACITY, UpgradeModifiers.RANGE, UpgradeModifiers.AREA);
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
        return WorkCardType.AREA;
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.FILLER_FUEL;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        // Bottom layer first (the card's own order goes top-down when the area is below the pedestal)
        List<BlockPos> positions = pedestal.getWorkPositions().stream().sorted(Comparator.comparingInt(BlockPos::getY)).toList();
        if (positions.isEmpty()) return;

        int blocks = Config.FILLER_BLOCKS.getAsInt() + getModifier(upgrade, UpgradeModifiers.BLOCK_CAPACITY);
        int index = pedestal.getUpgradeData().getIntOr("position", 0);
        if (index >= positions.size()) index = 0;
        PedestalFakePlayer player = pedestal.getFakePlayer(level);

        int checked = 0;
        while (blocks > 0 && checked < positions.size())
        {
            ItemStack toPlace = pedestal.getItemInPedestal();
            if (!ItemUpgradePlacer.canPlaceWith(pedestal, toPlace)) break;
            BlockPos target = positions.get(index);
            if (!target.equals(pedestal.getBlockPos()) && level.getBlockState(target).canBeReplaced())
            {
                int distance = distance(pedestal.getBlockPos(), target);
                if (!useFuel(pedestal, distance, true)) break;
                if (UpgradeWorld.placeBlock(level, player, toPlace, target))
                {
                    useFuel(pedestal, distance, false);
                    pedestal.removeItem(ItemResource.of(toPlace), 1, false);
                    blocks--;
                }
            }
            index = (index + 1) % positions.size();
            checked++;
        }
        pedestal.getUpgradeData().putInt("position", index);
        pedestal.upgradeDataChanged();
    }
}
