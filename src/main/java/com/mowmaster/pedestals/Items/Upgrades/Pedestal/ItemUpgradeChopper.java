package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeWorld;
import com.mowmaster.pedestals.PedestalTags;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Fells trees in the work card's area: each operation clears one column (bottom to top) of logs, leaves, mushrooms
// and the rest of the pedestals_can_chop_cant_quarry tag; with super speed, every column at once. Uses an iron axe
// if no tool is in. Drops land where the blocks were.
public class ItemUpgradeChopper extends ItemUpgradeBase
{
    public ItemUpgradeChopper(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.SUPER_SPEED, UpgradeModifiers.RANGE, UpgradeModifiers.AREA);
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
    public ItemStack getDefaultTool()
    {
        return new ItemStack(Items.IRON_AXE);
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.CHOPPER_FUEL;
    }

    @Override
    protected @Nullable Config.ToolRules getToolRules()
    {
        return Config.CHOPPER_TOOLS;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        List<BlockPos> area = pedestal.getWorkPositions();
        if (area.isEmpty() || !canUseTool(level, pedestal)) return;
        int minY = area.stream().mapToInt(BlockPos::getY).min().orElse(0);
        int maxY = area.stream().mapToInt(BlockPos::getY).max().orElse(0);
        List<BlockPos> columns = columns(area, minY);

        if (hasModifier(upgrade, UpgradeModifiers.SUPER_SPEED))
        {
            for (BlockPos column : columns)
            {
                if (!chopColumn(level, pedestal, column, maxY)) return;
            }
            return;
        }
        int index = nextIndex(pedestal, "position", columns.size());
        chopColumn(level, pedestal, columns.get(index), maxY);
    }

    // The bottom of each column, highest x and z first (1.20's order)
    private static List<BlockPos> columns(List<BlockPos> area, int minY)
    {
        Set<BlockPos> bottoms = new LinkedHashSet<>();
        for (BlockPos pos : area) bottoms.add(new BlockPos(pos.getX(), minY, pos.getZ()));
        List<BlockPos> sorted = new ArrayList<>(bottoms);
        sorted.sort(Comparator.<BlockPos>comparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ).reversed());
        return sorted;
    }

    // false if it ran out of fuel
    private boolean chopColumn(ServerLevel level, BasePedestalBlockEntity pedestal, BlockPos bottom, int maxY)
    {
        PedestalFakePlayer player = playerWithTool(level, pedestal);
        try
        {
            for (int y = bottom.getY(); y <= maxY; y++)
            {
                BlockPos target = new BlockPos(bottom.getX(), y, bottom.getZ());
                BlockState state = level.getBlockState(target);
                if (!state.is(PedestalTags.CAN_CHOP_CANT_QUARRY) || !UpgradeWorld.canBreak(level, pedestal, target, state)) continue;
                int distance = distance(pedestal.getBlockPos(), target);
                if (!useFuel(pedestal, distance, true)) return false;
                if (wearsTool(pedestal) && pedestal.getTool().isDamageableItem()
                        && pedestal.getTool().getMaxDamage() - pedestal.getTool().getDamageValue() <= 1) return false;
                if (UpgradeWorld.breakBlock(level, player, target, target)) useFuel(pedestal, distance, false);
            }
            return true;
        }
        finally
        {
            finishWithTool(pedestal, player);
        }
    }
}
