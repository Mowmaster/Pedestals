package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

// Whitelist = "above": lets through damageable items with at least the target % durability left;
// blacklist = "below": at most the target %. Items that can't be damaged never pass (as in 1.20).
// The target is the durability % of the items in the list, added up (max 100).
public class DurabilityFilterItem extends ItemOnlyFilterItem
{
    public DurabilityFilterItem(Properties properties)
    {
        super(properties);
    }

    public static int percentLeft(ItemStack stack)
    {
        if (!stack.isDamageableItem()) return 100;
        return Math.floorDiv((stack.getMaxDamage() - stack.getDamageValue()) * 100, stack.getMaxDamage());
    }

    public static int getTarget(FilterData.Entry entry)
    {
        int target = 0;
        for (ItemStackTemplate template : entry.list())
        {
            target += percentLeft(template.create());
            if (target >= 100) return 100;
        }
        return target;
    }

    @Override
    protected boolean matchesItem(ItemStack incoming, FilterData.Entry entry)
    {
        return true; // not used: allows() is overridden below
    }

    @Override
    public <R extends Resource> boolean allows(ItemStack filter, PedestalTransportType<R> type, R resource)
    {
        if (!handlesType(type)) return super.allows(filter, type, resource);
        if (!(resource instanceof ItemResource item)) return false;
        ItemStack incoming = item.toStack();
        if (!incoming.isDamageableItem()) return false;

        FilterData.Entry entry = getEntry(filter, type);
        int left = percentLeft(incoming);
        int target = getTarget(entry);
        return entry.blacklist() ? left <= target : left >= target;
    }

    @Override
    protected Component listTypeName(boolean blacklist)
    {
        return Component.translatable(PedestalReferences.MODID + (blacklist ? ".filter_type_below" : ".filter_type_above"));
    }
}
