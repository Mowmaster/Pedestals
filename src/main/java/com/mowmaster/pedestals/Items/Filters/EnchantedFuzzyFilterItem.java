package com.mowmaster.pedestals.Items.Filters;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

// Matches items with any enchantment that's on a list item, at any level (1.20's FilterEnchantedFuzzy)
public class EnchantedFuzzyFilterItem extends ItemOnlyFilterItem
{
    public EnchantedFuzzyFilterItem(Properties properties)
    {
        super(properties);
    }

    @Override
    protected boolean matchesItem(ItemStack incoming, FilterData.Entry entry)
    {
        for (Holder<Enchantment> enchantment : EnchantmentHelper.getEnchantmentsForCrafting(incoming).keySet())
        {
            for (ItemStackTemplate template : entry.list())
            {
                if (EnchantmentHelper.getEnchantmentsForCrafting(template.create()).getLevel(enchantment) > 0) return true;
            }
        }
        return false;
    }
}
