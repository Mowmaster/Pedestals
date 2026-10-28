package com.mowmaster.pedestals.Items.WorkCards;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

// A list of single positions (1.20's WorkCardLocations). Clicking a block adds it, clicking it again removes it.
// The pedestals card (WorkCardPedestalsItem) is the same but only takes pedestals.
public class WorkCardLocationsItem extends WorkCardItem
{
    public WorkCardLocationsItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public WorkCardType getWorkCardType()
    {
        return WorkCardType.LOCATIONS;
    }

    @Override
    protected Component select(Level level, ItemStack card, BlockPos pos)
    {
        boolean added = toggle(card, pos);
        return Component.translatable(MODID + (added ? ".workcard_blockpos_added" : ".workcard_blockpos_removed"));
    }

    // In the order they were clicked
    @Override
    public List<BlockPos> getPositions(ItemStack card, BasePedestalBlockEntity pedestal, int range)
    {
        return getStoredPositions(card).stream().filter(pos -> inRange(pedestal, pos, range)).toList();
    }
}
