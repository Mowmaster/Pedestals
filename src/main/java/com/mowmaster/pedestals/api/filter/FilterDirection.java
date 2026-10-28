package com.mowmaster.pedestals.api.filter;

import com.mowmaster.pedestals.PedestalReferences;
import net.minecraft.network.chat.Component;

// When a filter applies: to what goes INTO the pedestal, what's taken OUT of it, or neither (NEUTRAL, used by
// upgrades that read the filter themselves, e.g. block breakers). Same values as MowLib's FilterDirection.
public enum FilterDirection
{
    INSERT,
    EXTRACT,
    NEUTRAL;

    public Component getName()
    {
        return Component.translatable(PedestalReferences.MODID + ".filter_direction." + name().toLowerCase());
    }
}
