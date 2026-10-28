package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Collects from full bee nests and hives at the work card's positions, one per operation: with glass bottles in the
// pedestal it fills them with honey (using one per hive), otherwise it shears honeycomb with the pedestal's tool
// (shears if none is in). What it collects drops by the hive.
public class ItemUpgradeHiveHarvester extends ItemUpgradeBase
{
    public ItemUpgradeHiveHarvester(Properties properties)
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
        return new ItemStack(Items.SHEARS);
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.HIVE_HARVESTER_FUEL;
    }

    @Override
    protected @Nullable Config.ToolRules getToolRules()
    {
        return Config.HIVE_HARVESTER_TOOLS;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        List<BlockPos> positions = pedestal.getWorkPositions();
        if (positions.isEmpty()) return;
        BlockPos target = positions.get(nextIndex(pedestal, "position", positions.size()));
        BlockState state = level.getBlockState(target);
        if (!(state.getBlock() instanceof BeehiveBlock) || state.getValue(BeehiveBlock.HONEY_LEVEL) < BeehiveBlock.MAX_HONEY_LEVELS) return;
        if (!pedestal.machineFilterAllows(new ItemStack(state.getBlock().asItem()))) return;
        int distance = distance(pedestal.getBlockPos(), target);
        if (!useFuel(pedestal, distance, true)) return;

        ItemStack bottles = pedestal.getItemInPedestal();
        boolean useBottle = bottles.is(Items.GLASS_BOTTLE);
        if (!useBottle && !canUseTool(level, pedestal)) return;

        PedestalFakePlayer player = useBottle ? pedestal.getFakePlayer(level) : playerWithTool(level, pedestal);
        try
        {
            // Protection mods treat taking honey like breaking the hive
            if (CommonHooks.fireBlockBreak(level, GameType.SURVIVAL, player, target, state).isCanceled()) return;
            ItemStack held = useBottle ? bottles.copyWithCount(1) : player.getMainHandItem();
            if (useBottle) player.setItemInHand(InteractionHand.MAIN_HAND, held);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false);
            // The hive's own right-click: shears drop honeycomb, a bottle becomes a honey bottle in the player's inventory
            if (!state.useItemOn(held, level, player, InteractionHand.MAIN_HAND, hit).consumesAction()) return;

            useFuel(pedestal, distance, false);
            if (useBottle) pedestal.removeItem(ItemResource.of(bottles), 1, false);
            // Anything that ended up in the fake player's inventory drops by the hive
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
            {
                ItemStack stack = player.getInventory().getItem(slot);
                if (stack.isEmpty() || stack == held) continue; // "==": the very stack we're holding (the tool)
                Block.popResource(level, target, player.getInventory().removeItemNoUpdate(slot));
            }
        }
        finally
        {
            if (useBottle) player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            else finishWithTool(pedestal, player);
        }
    }
}
