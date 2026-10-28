package com.mowmaster.pedestals.Items.Tools;

import net.minecraft.world.item.Item;

// Hold in the offhand and left-click a pedestal to take its filter out (1.20's MowLib FilterTool).
// The pedestal block does the work (BasePedestalBlock#leftClick); this item only needs to exist.
public class FilterToolItem extends Item
{
    public FilterToolItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }
}
