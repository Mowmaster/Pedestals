package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.filter.FilterDirection;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

// Filters that only make sense for items (durability, enchantments, food). Other modes follow the plain
// whitelist/blacklist rule from BaseFilterItem.
public abstract class ItemOnlyFilterItem extends BaseFilterItem
{
    protected ItemOnlyFilterItem(Properties properties)
    {
        super(properties, FilterDirection.INSERT);
    }

    protected abstract boolean matchesItem(ItemStack incoming, FilterData.Entry entry);

    // false for filters with no list (enchanted, food)
    protected boolean usesList()
    {
        return true;
    }

    @Override
    protected boolean handlesType(PedestalTransportType<?> type)
    {
        return type == PedestalTransportTypes.ITEMS.get();
    }

    @Override
    protected boolean usesListFor(PedestalTransportType<?> type)
    {
        return usesList() && handlesType(type);
    }

    @Override
    protected <R extends Resource> boolean matches(ItemStack filter, PedestalTransportType<R> type, R resource, FilterData.Entry entry)
    {
        // "instanceof ItemResource item" checks the type and names the cast value in one go (pattern matching)
        return resource instanceof ItemResource item && matchesItem(item.toStack(), entry);
    }
}
