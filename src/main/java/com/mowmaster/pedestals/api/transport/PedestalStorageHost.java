package com.mowmaster.pedestals.api.transport;

import com.mowmaster.pedestals.api.filter.FilterDirection;
import net.neoforged.neoforge.transfer.resource.Resource;

// What a storage can ask of the pedestal it belongs to (implemented by BasePedestalBlockEntity)
public interface PedestalStorageHost
{
    // Call after the contents changed (on the root transaction commit), so the pedestal saves and, for types
    // that sync to clients, sends the update
    void onStorageChanged(PedestalTransportType<?> type);

    // false if the pedestal's filter blocks this resource in this direction
    <R extends Resource> boolean filterAllows(PedestalTransportType<R> type, R resource, FilterDirection direction);

    // The most of this resource the filter allows the pedestal to hold (capacity if the filter doesn't limit it)
    <R extends Resource> int filterLimit(PedestalTransportType<R> type, R resource, int capacity);

    // What the pedestal's capacity augments add to this type's link transfer rate, and its storage augments to its
    // storage (in the type's own units: experience in levels, items in extra stacks). 0 without augments.
    default int getTransferBonus(PedestalTransportType<?> type)
    {
        return 0;
    }

    default int getStorageBonus(PedestalTransportType<?> type)
    {
        return 0;
    }
}
