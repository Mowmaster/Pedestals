package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.pedestals.api.filter.FilterDirection;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resource.Resource;

// Matches the same item (or fluid, ...) as anything in the list, ignoring extra data (1.20's FilterItem, "fuzzy")
public class ItemFilterItem extends BaseFilterItem
{
    public ItemFilterItem(Properties properties)
    {
        super(properties, FilterDirection.INSERT);
    }

    @Override
    protected <R extends Resource> boolean matches(ItemStack filter, PedestalTransportType<R> type, R resource, FilterData.Entry entry)
    {
        // anyMatch: true if the test passes for at least one element of the stream
        return listResources(type, entry).stream().anyMatch(listed -> type.isSameKind(listed, resource));
    }
}
