package com.mowmaster.pedestals.item.pedestalUpgrades;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;

public class ItemUpgradeBaseFilter extends ItemUpgradeBase {

    public ItemUpgradeBaseFilter(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public int getItemEnchantability()
    {
        return 0;
    }

    @Override
    public Boolean canAcceptOpSpeed() {
        return false;
    }
}
