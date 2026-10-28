package com.mowmaster.pedestals.api.filter;

import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resource.Resource;

// Implemented by filter items. A pedestal holds one filter; its storages ask it about every resource going in or
// out (see PedestalStorageHost). Replaces MowLib's IFilterItem, which had one method per transfer mode; here each
// method takes the transport type, so filters work with types added by other mods too.
// "<R extends Resource>" on a method makes it generic per call: R is ItemResource for items, FluidResource for fluids...
public interface IPedestalFilter
{
    FilterDirection getDirection(ItemStack filter);

    // Whether the resource may pass
    <R extends Resource> boolean allows(ItemStack filter, PedestalTransportType<R> type, R resource);

    // The most of this resource the pedestal may hold with this filter in it (return capacity for no limit)
    default <R extends Resource> int limit(ItemStack filter, PedestalTransportType<R> type, R resource, int capacity)
    {
        return capacity;
    }

    // Shown on the pedestal: 1 = whitelist, 2 = blacklist (FILTER_STATUS)
    boolean isBlacklist(ItemStack filter);
}
