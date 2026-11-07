package com.mowmaster.ironpedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.workcard.WorkCardType;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.Config;
import com.mowmaster.ironpedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.ironpedestals.Items.Upgrades.UpgradeModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Speeds up growth at the work card's positions, one per operation (all with super speed):
//   with bone meal in the pedestal: bone meals anything that takes it (uses one each time)
//   otherwise: gives the block an extra random tick (crops grow, saplings grow, amethyst buds grow...)
public class ItemUpgradeFertilizer extends ItemUpgradeBase
{
    public ItemUpgradeFertilizer(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.SUPER_SPEED);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.AREA_OR_LOCATIONS;
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.FERTILIZER_FUEL;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        List<BlockPos> positions = pedestal.getWorkPositions();
        if (positions.isEmpty()) return;
        List<BlockPos> targets = hasModifier(upgrade, UpgradeModifiers.SUPER_SPEED) ? positions
                : List.of(positions.get(nextIndex(pedestal, "position", positions.size())));

        for (BlockPos target : targets)
        {
            BlockState state = level.getBlockState(target);
            if (state.isAir() || target.equals(pedestal.getBlockPos())) continue;
            ItemStack item = pedestal.getItemInPedestal();
            if (!pedestal.machineFilterAllows(item)) continue;
            int distance = distance(pedestal.getBlockPos(), target);
            if (!useFuel(pedestal, distance, true)) return;

            boolean worked = false;
            if (item.getItem() instanceof BoneMealItem && state.getBlock() instanceof BonemealableBlock bonemealable
                    && bonemealable.isValidBonemealTarget(level, target, state, BonemealSource.INTERACTION))
            {
                // growCrop uses up one of the stack it's given, so give it a copy and take one from the pedestal
                if (BoneMealItem.growCrop(item.copyWithCount(1), level, target))
                {
                    pedestal.removeItem(ItemResource.of(item), 1, false);
                    level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, target, 15);
                    worked = true;
                }
            }
            else if (state.isRandomlyTicking())
            {
                state.randomTick(level, target, level.getRandom());
                worked = true;
            }
            if (worked) useFuel(pedestal, distance, false);
        }
    }
}
