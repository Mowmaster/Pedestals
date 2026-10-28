package com.mowmaster.pedestals.Items.Filters;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

// Matches items with exactly as many enchantments as the list's items have in total (1.20's FilterEnchantCount)
public class EnchantedCountFilterItem extends ItemOnlyFilterItem
{
    public EnchantedCountFilterItem(Properties properties)
    {
        super(properties);
    }

    @Override
    protected boolean matchesItem(ItemStack incoming, FilterData.Entry entry)
    {
        int target = entry.list().stream()
                .mapToInt(template -> EnchantmentHelper.getEnchantmentsForCrafting(template.create()).size())
                .sum();
        return target > 0 && EnchantmentHelper.getEnchantmentsForCrafting(incoming).size() == target;
    }
}
