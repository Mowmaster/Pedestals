package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.pedestals.api.filter.FilterDirection;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.transfer.resource.Resource;

// Lets everything in, but only up to an amount: the total count of the items in the list for that mode
// (e.g. set from a chest holding 16 cobblestone, the pedestal holds at most 16 of anything; for fluids 16 mB).
// It can also go in another filter's list to add the limit to that filter (see BaseFilterItem#limit).
public class RestrictedFilterItem extends BaseFilterItem
{
    public RestrictedFilterItem(Properties properties)
    {
        super(properties, FilterDirection.INSERT);
    }

    @Override
    protected <R extends Resource> boolean matches(ItemStack filter, PedestalTransportType<R> type, R resource, FilterData.Entry entry)
    {
        return true;
    }

    @Override
    public <R extends Resource> boolean allows(ItemStack filter, PedestalTransportType<R> type, R resource)
    {
        return true;
    }

    // Every type can have a count, even energy and experience (the list is only counted)
    @Override
    protected boolean usesListFor(PedestalTransportType<?> type)
    {
        return true;
    }

    @Override
    protected boolean canToggleBlacklist()
    {
        return false;
    }

    @Override
    public <R extends Resource> int limit(ItemStack filter, PedestalTransportType<R> type, R resource, int capacity)
    {
        int count = getEntry(filter, type).list().stream().mapToInt(ItemStackTemplate::count).sum();
        return count > 0 ? Math.min(count, capacity) : capacity;
    }
}
