package com.mowmaster.pedestals.Items;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.ArrayList;
import java.util.List;

// An enchantment sacrifice: made by the sacrifice upgrade, burned by the reactor. It carries its enchantments in the
// normal enchantments component (so the tooltip lists them and it glints), but can't go on anything.
public class SacrificeItem extends Item
{
    public SacrificeItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    public static ItemStack create(Item item, ItemEnchantments enchantments)
    {
        ItemStack stack = new ItemStack(item);
        EnchantmentHelper.setEnchantments(stack, enchantments);
        return stack;
    }

    public static ItemEnchantments getEnchantments(ItemStack stack)
    {
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    // Only sacrifices with an enchantment past its normal maximum can be burned
    public static boolean isOverleveled(ItemStack stack)
    {
        if (!(stack.getItem() instanceof SacrificeItem)) return false;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : getEnchantments(stack).entrySet())
        {
            if (entry.getIntValue() > entry.getKey().value().getMaxLevel()) return true;
        }
        return false;
    }

    // Its "kind": which enchantments it has, levels ignored. Protection 6 and Protection 10 are the same kind;
    // Protection 6 + Unbreaking 6 is another.
    public static String kind(ItemStack stack)
    {
        List<String> ids = new ArrayList<>();
        for (Holder<Enchantment> enchantment : getEnchantments(stack).keySet())
        {
            ids.add(enchantment.unwrapKey().map(key -> key.identifier().toString()).orElse("?"));
        }
        ids.sort(null); // natural order: the same set always gives the same text
        return String.join(",", ids);
    }

    // All its enchantment levels added up (the reactor's tier comes from this)
    public static int totalLevels(ItemStack stack)
    {
        int total = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : getEnchantments(stack).entrySet()) total += entry.getIntValue();
        return total;
    }

    // What a grindstone would give for it: each enchantment's minimum enchanting cost (the top of vanilla's range)
    public static int grindstoneValue(ItemStack stack)
    {
        int total = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : getEnchantments(stack).entrySet())
        {
            total += entry.getKey().value().getMinCost(entry.getIntValue());
        }
        return total;
    }
}
