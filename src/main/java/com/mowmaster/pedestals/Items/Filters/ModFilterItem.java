package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.pedestals.api.filter.FilterDirection;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resource.Resource;

// Matches anything from the same mod as something in the list (1.20's FilterMod)
public class ModFilterItem extends BaseFilterItem
{
    public ModFilterItem(Properties properties)
    {
        super(properties, FilterDirection.INSERT);
    }

    @Override
    protected <R extends Resource> boolean matches(ItemStack filter, PedestalTransportType<R> type, R resource, FilterData.Entry entry)
    {
        Identifier incoming = type.getRegistryName(resource);
        if (incoming == null) return false;
        return listResources(type, entry).stream()
                .map(type::getRegistryName)
                .anyMatch(listed -> listed != null && listed.getNamespace().equals(incoming.getNamespace()));
    }
}
