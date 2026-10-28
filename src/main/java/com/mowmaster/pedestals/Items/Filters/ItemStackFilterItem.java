package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.pedestals.api.filter.FilterDirection;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resource.Resource;

// Matches only exactly the same thing as something in the list, data included (1.20's FilterItemStack, "exact")
public class ItemStackFilterItem extends BaseFilterItem
{
    public ItemStackFilterItem(Properties properties)
    {
        super(properties, FilterDirection.INSERT);
    }

    @Override
    protected <R extends Resource> boolean matches(ItemStack filter, PedestalTransportType<R> type, R resource, FilterData.Entry entry)
    {
        return listResources(type, entry).contains(resource);
    }
}
