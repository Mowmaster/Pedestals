package com.mowmaster.pedestals.Items.WorkCards;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

// A list of pedestals (1.20's WorkCardBE "machines" card), for the crafter and the modification upgrade.
// Only pedestals can be selected. The positions aren't checked again later, so upgrades must check that each is
// still a pedestal when they use it.
public class WorkCardPedestalsItem extends WorkCardLocationsItem
{
    public WorkCardPedestalsItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public WorkCardType getWorkCardType()
    {
        return WorkCardType.PEDESTALS;
    }

    @Override
    protected @Nullable Component select(Level level, ItemStack card, BlockPos pos)
    {
        if (!(level.getBlockEntity(pos) instanceof BasePedestalBlockEntity)) return Component.translatable(MODID + ".workcard_not_pedestal");
        return super.select(level, card, pos);
    }
}
