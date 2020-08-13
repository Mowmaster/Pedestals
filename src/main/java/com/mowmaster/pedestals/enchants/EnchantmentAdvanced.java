package com.mowmaster.pedestals.enchants;

import com.mowmaster.pedestals.item.ItemEnchantableBook;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class EnchantmentAdvanced extends Enchantment
{
    public EnchantmentAdvanced() {
        super(Rarity.VERY_RARE, EnchantmentRegistry.COINUPGRADE, new EquipmentSlotType[]{
                EquipmentSlotType.OFFHAND
        });
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return enchantmentLevel * 110;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return this.getMinEnchantability(enchantmentLevel) + 50;
    }

    @Override
    public boolean isTreasureEnchantment() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    public boolean canApply(ItemStack stack) {
        boolean canApplyToUpgrade = false;
        Item coin = stack.getItem();
        if(coin.equals(ItemEnchantableBook.ADVANCED))
        {
            canApplyToUpgrade = true;
            return stack.equals(ItemEnchantableBook.ADVANCED) && canApplyToUpgrade;
        }
        return false;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        boolean canApplyToUpgrade = false;
        Item coin = stack.getItem();
        if(coin.equals(ItemEnchantableBook.ADVANCED))
        {
            canApplyToUpgrade = true;
        }

        return canApplyToUpgrade;
    }

    public boolean isAllowedOnBooks() {
        return false;
    }
}

