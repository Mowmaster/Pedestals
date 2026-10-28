package com.mowmaster.pedestals.Items.Filters;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

// Matches anything enchanted, and enchanted books (1.20's FilterEnchanted). No list.
public class EnchantedFilterItem extends ItemOnlyFilterItem
{
    public EnchantedFilterItem(Properties properties)
    {
        super(properties);
    }

    public static boolean hasEnchantments(ItemStack stack)
    {
        // getEnchantmentsForCrafting reads a book's stored enchantments, and an item's own otherwise
        return !EnchantmentHelper.getEnchantmentsForCrafting(stack).isEmpty();
    }

    @Override
    protected boolean usesList()
    {
        return false;
    }

    @Override
    protected boolean matchesItem(ItemStack incoming, FilterData.Entry entry)
    {
        return hasEnchantments(incoming);
    }
}
