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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Breaks one block per operation at the work card's positions, in turn, as if mined with the pedestal's tool
// (a stone pickaxe if none is in). Drops land where the block was.
public class ItemUpgradeBreaker extends ItemUpgradeBase
{
    public ItemUpgradeBreaker(Properties properties)
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
    public ItemStack getDefaultTool()
    {
        return new ItemStack(Items.STONE_PICKAXE);
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.BREAKER_FUEL;
    }

    @Override
    protected @Nullable Config.ToolRules getToolRules()
    {
        return Config.BREAKER_TOOLS;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        List<BlockPos> positions = pedestal.getWorkPositions();
        if (positions.isEmpty() || !canUseTool(level, pedestal)) return;

        int index = pedestal.getUpgradeData().getIntOr("position", 0) % positions.size();
        BlockPos target = positions.get(index);
        BlockState state = level.getBlockState(target);
        int distance = distance(pedestal.getBlockPos(), target);

        if (UpgradeWorld.canBreak(level, pedestal, target, state))
        {
            // Without fuel it waits on this position (1.20 behaviour)
            if (!useFuel(pedestal, distance, true)) return;
            PedestalFakePlayer player = playerWithTool(level, pedestal);
            try
            {
                if (UpgradeWorld.breakBlock(level, player, target, target)) useFuel(pedestal, distance, false);
            }
            finally
            {
                finishWithTool(pedestal, player);
            }
        }
        pedestal.getUpgradeData().putInt("position", (index + 1) % positions.size());
        pedestal.upgradeDataChanged();
    }
}
