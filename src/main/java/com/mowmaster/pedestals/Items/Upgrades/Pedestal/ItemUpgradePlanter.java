package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeWorld;
import com.mowmaster.pedestals.PedestalTags;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Plants the pedestal's seeds and saplings at the work card's positions (the empty spaces above the farmland),
// one position per operation, or all of them with super speed. Spots where it can't grow are skipped.
public class ItemUpgradePlanter extends ItemUpgradeBase
{
    public ItemUpgradePlanter(Properties properties)
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
        return WorkCardType.AREA_OR_LOCATIONS;
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.PLANTER_FUEL;
    }

    // Seeds, saplings and other plants (NeoForge's SpecialPlantable covers modded seeds that aren't block items)
    private static boolean isPlantable(BasePedestalBlockEntity pedestal, ItemStack stack)
    {
        if (stack.isEmpty() || !pedestal.machineFilterAllows(stack)) return false;
        if (stack.getItem() instanceof SpecialPlantable) return true;
        return stack.getItem() instanceof BlockItem blockItem && UpgradeWorld.isPlant(blockItem.getBlock())
                && !blockItem.getBlock().defaultBlockState().is(PedestalTags.CANNOT_PLACE);
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
            ItemStack seed = pedestal.getItemInPedestal();
            if (!isPlantable(pedestal, seed)) return;
            if (target.equals(pedestal.getBlockPos()) || !level.getBlockState(target).isAir()) continue;
            int distance = distance(pedestal.getBlockPos(), target);
            if (!useFuel(pedestal, distance, true)) return;
            // The seed's own placing code checks the soil (farmland, dirt, soul sand...)
            if (UpgradeWorld.placeBlock(level, pedestal.getFakePlayer(level), seed, target))
            {
                useFuel(pedestal, distance, false);
                pedestal.removeItem(ItemResource.of(seed), 1, false);
                pedestal.spawnParticles(level, target, 0x00C800);
            }
        }
    }
}
