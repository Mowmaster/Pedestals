package com.mowmaster.ironpedestals.Items.WorkCards;

import com.mowmaster.ironpedestals.IronPedestalReferences;
import com.mowmaster.mowlib.items.workcards.WorkCardLocationsItem;
import com.mowmaster.mowlib.workcard.WorkCardType;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

// A list of ironpedestals (1.20's WorkCardBE "machines" card), for the crafter and the modification upgrade: MowLib's
// locations card, but only ironpedestals can be selected. The positions aren't checked again later, so upgrades must
// check that each is still a pedestal when they use it.
public class WorkCardPedestalsItem extends WorkCardLocationsItem
{
    public WorkCardPedestalsItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public WorkCardType getWorkCardType()
    {
        return WorkCardType.MACHINES;
    }

    @Override
    protected @Nullable Component select(Level level, ItemStack card, BlockPos pos)
    {
        if (!(level.getBlockEntity(pos) instanceof BasePedestalBlockEntity)) return Component.translatable(IronPedestalReferences.MODID + ".workcard_not_pedestal");
        return super.select(level, card, pos);
    }
}
