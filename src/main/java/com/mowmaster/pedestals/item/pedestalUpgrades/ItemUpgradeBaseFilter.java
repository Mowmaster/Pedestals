package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.dust.dust;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public class ItemUpgradeBaseFilter extends ItemUpgradeBase {

    public ItemUpgradeBaseFilter(Properties builder) {super(builder.group(dust.ITEM_GROUP));}

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
