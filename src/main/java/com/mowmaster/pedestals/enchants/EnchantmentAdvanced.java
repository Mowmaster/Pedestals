package com.mowmaster.pedestals.enchants;

import com.mowmaster.pedestals.api.enchanting.IAdvancedBook;
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
        return enchantmentLevel * 100;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return this.getMinEnchantability(enchantmentLevel) + 10;
    }

    @Override
    public boolean isTreasureEnchantment() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean canApply(ItemStack stack) {
        boolean canApplyToUpgrade = false;
        Item book = stack.getItem();
        if(book instanceof IAdvancedBook)
        {
            canApplyToUpgrade = true;
            return (book instanceof IAdvancedBook) && canApplyToUpgrade;
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        boolean canApplyToUpgrade = false;
        Item book = stack.getItem();
        if(book instanceof IAdvancedBook)
        {
            canApplyToUpgrade = true;
        }

        return canApplyToUpgrade;
    }

    /*@Override
    public boolean canApplyTogether(Enchantment enchant) {

        return super.canApplyTogether(enchant) && (enchant.equals(EnchantmentRegistry.ADVANCED)||enchant.equals(EnchantmentRegistry.AREA)||enchant.equals(EnchantmentRegistry.CAPACITY)||enchant.equals(EnchantmentRegistry.OPERATIONSPEED)||enchant.equals(EnchantmentRegistry.RANGE));
    }*/

    //Added because wyld found a villager with it...
    @Override
    public boolean canVillagerTrade() {
        return false;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return false;
    }

    @Override
    public boolean canGenerateInLoot() {
        return true;
    }
}

