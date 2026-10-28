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

// Digs out the work card's area a column at a time, (4 + block capacity) blocks down per operation, and drops what
// it mines on the pedestal (an iron pickaxe if no tool is in). Leaves logs, leaves and the like (the
// pedestals_can_chop_cant_quarry tag) for the chopper.
// Operate to bedrock modifier: select a flat (one block high) area and it digs from there to the bottom of the world,
// then stops.
public class ItemUpgradeQuarry extends ItemUpgradeBase
{
    public ItemUpgradeQuarry(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.BLOCK_CAPACITY, UpgradeModifiers.RANGE, UpgradeModifiers.AREA, UpgradeModifiers.OPERATE_TO_BEDROCK);
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
        return new ItemStack(Items.IRON_PICKAXE);
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.QUARRY_FUEL;
    }

    @Override
    protected @Nullable Config.ToolRules getToolRules()
    {
        return Config.QUARRY_TOOLS;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        if (!canUseTool(level, pedestal)) return;
        int perOperation = Math.max(1, Config.QUARRY_BLOCKS.getAsInt() + getModifier(upgrade, UpgradeModifiers.BLOCK_CAPACITY));
        boolean toBedrock = hasModifier(upgrade, UpgradeModifiers.OPERATE_TO_BEDROCK);
        ColumnWork.Step step = ColumnWork.current(level, pedestal, perOperation, toBedrock);
        if (step == null) return;
        if (digColumn(level, pedestal, step)) ColumnWork.advance(level, pedestal, step, toBedrock);
    }

    // Mines the step's blocks; false if it ran out of fuel or tool before the first block (it'll try again)
    private boolean digColumn(ServerLevel level, BasePedestalBlockEntity pedestal, ColumnWork.Step step)
    {
        PedestalFakePlayer player = playerWithTool(level, pedestal);
        try
        {
            boolean first = true;
            for (BlockPos target : step.blocks())
            {
                BlockState state = level.getBlockState(target);
                if (state.is(PedestalTags.CAN_CHOP_CANT_QUARRY) || !UpgradeWorld.canBreak(level, pedestal, target, state)) continue;
                int distance = distance(pedestal.getBlockPos(), target);
                if (!useFuel(pedestal, distance, true)) return !first;
                if (wearsTool(pedestal) && pedestal.getTool().isDamageableItem()
                        && pedestal.getTool().getMaxDamage() - pedestal.getTool().getDamageValue() <= 1) return !first;
                if (UpgradeWorld.breakBlock(level, player, target, pedestal.getBlockPos())) useFuel(pedestal, distance, false);
                first = false;
            }
            return true;
        }
        finally
        {
            finishWithTool(pedestal, player);
        }
    }
}
