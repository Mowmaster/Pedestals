package com.mowmaster.pedestals.Items.Filters;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

// Matches items whose every enchantment (at that level) is on a list item with the same number of enchantments
// (1.20's FilterEnchantedExact)
public class EnchantedExactFilterItem extends ItemOnlyFilterItem
{
    public EnchantedExactFilterItem(Properties properties)
    {
        super(properties);
    }

    @Override
    protected boolean matchesItem(ItemStack incoming, FilterData.Entry entry)
    {
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(incoming);
        if (enchantments.isEmpty()) return false;

        for (Holder<Enchantment> enchantment : enchantments.keySet())
        {
            int level = enchantments.getLevel(enchantment);
            boolean found = false;
            for (ItemStackTemplate template : entry.list())
            {
                ItemEnchantments listed = EnchantmentHelper.getEnchantmentsForCrafting(template.create());
                if (listed.size() == enchantments.size() && listed.getLevel(enchantment) == level)
                {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }
}
