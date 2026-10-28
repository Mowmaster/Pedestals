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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Harvests grown crops at the work card's positions, one position per operation (all of them with super speed),
// with the pedestal's tool (a stone hoe if none is in):
//   crops with an "age" (wheat, carrots, berries, nether wart...): when fully grown
//   tall plants (sugar cane, cactus, bamboo, kelp...): everything above the bottom block
//   melons and pumpkins: always (their stems are left alone)
// Gentle harvest modifier: crops are replanted (set back to age 0) and one seed is kept back from the drops.
public class ItemUpgradeHarvester extends ItemUpgradeBase
{
    public ItemUpgradeHarvester(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.SUPER_SPEED, UpgradeModifiers.GENTLE_HARVEST, UpgradeModifiers.RANGE, UpgradeModifiers.AREA);
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
        return new ItemStack(Items.STONE_HOE);
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.HARVESTER_FUEL;
    }

    @Override
    protected @Nullable Config.ToolRules getToolRules()
    {
        return Config.HARVESTER_TOOLS;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        List<BlockPos> positions = pedestal.getWorkPositions();
        if (positions.isEmpty() || !canUseTool(level, pedestal)) return;
        boolean gentle = hasModifier(upgrade, UpgradeModifiers.GENTLE_HARVEST);
        List<BlockPos> targets = hasModifier(upgrade, UpgradeModifiers.SUPER_SPEED) ? positions
                : List.of(positions.get(nextIndex(pedestal, "position", positions.size())));

        PedestalFakePlayer player = playerWithTool(level, pedestal);
        try
        {
            for (BlockPos target : targets)
            {
                BlockState state = level.getBlockState(target);
                if (!canHarvest(level, target, state) || !UpgradeWorld.canBreak(level, pedestal, target, state)) continue;
                int distance = distance(pedestal.getBlockPos(), target);
                if (!useFuel(pedestal, distance, true)) return;
                IntegerProperty age = ageProperty(state);
                boolean harvested = gentle && age != null && !isTallPlant(state.getBlock())
                        ? replant(level, player, target, state, age)
                        : UpgradeWorld.breakBlock(level, player, target, target);
                if (harvested) useFuel(pedestal, distance, false);
            }
        }
        finally
        {
            finishWithTool(pedestal, player);
        }
    }

    private static boolean isTallPlant(Block block)
    {
        return block instanceof GrowingPlantBlock || block instanceof BambooStalkBlock || block instanceof SugarCaneBlock || block instanceof CactusBlock;
    }

    // The block's "age" property, if it has one (1.20's getBlockPropertyAge)
    public static @Nullable IntegerProperty ageProperty(BlockState state)
    {
        for (Property<?> property : state.getProperties())
        {
            if (property instanceof IntegerProperty integer && property.getName().equalsIgnoreCase("age")) return integer;
        }
        return null;
    }

    private static boolean canHarvest(ServerLevel level, BlockPos pos, BlockState state)
    {
        Block block = state.getBlock();
        if (isTallPlant(block)) return level.getBlockState(pos.below()).is(block); // keep the bottom one
        if (state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN)) return true;
        if (block instanceof StemBlock) return false;
        IntegerProperty age = ageProperty(state);
        return age != null && state.getValue(age).equals(Collections.max(age.getPossibleValues()));
    }

    // Gentle harvest: the drops, minus one of the crop's own item (its seed), and the crop goes back to age 0.
    // Done by hand rather than breaking the block, but still asks protection mods first (the break event).
    private static boolean replant(ServerLevel level, PedestalFakePlayer player, BlockPos pos, BlockState state, IntegerProperty age)
    {
        if (CommonHooks.fireBlockBreak(level, GameType.SURVIVAL, player, pos, state).isCanceled()) return false;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack tool = player.getMainHandItem();
        List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, level, pos, blockEntity, player, tool));
        // A crop block's item is its seed (wheat -> wheat seeds, carrots -> carrot)
        for (ItemStack drop : drops)
        {
            if (drop.is(state.getBlock().asItem()))
            {
                drop.shrink(1);
                break;
            }
        }
        level.setBlockAndUpdate(pos, state.setValue(age, Collections.min(age.getPossibleValues())));
        if (!tool.isEmpty()) tool.mineBlock(level, state, pos, player);
        drops.forEach(drop -> Block.popResource(level, pos, drop));
        return true;
    }
}
