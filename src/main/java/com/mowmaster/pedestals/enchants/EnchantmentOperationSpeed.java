package com.mowmaster.pedestals.enchants;

import com.mowmaster.pedestals.api.enchanting.ISpeedBook;
import com.mowmaster.pedestals.api.upgrade.IUpgradeBase;
import com.mowmaster.pedestals.item.books.ItemEnchantableBookBase;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBase;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class EnchantmentOperationSpeed extends Enchantment
{

    public EnchantmentOperationSpeed() {
        super(Rarity.COMMON, EnchantmentRegistry.COINUPGRADE, new EquipmentSlotType[]{
                EquipmentSlotType.OFFHAND
        });
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return 1 + (enchantmentLevel - 1) * 11;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return this.getMinEnchantability(enchantmentLevel) + 55;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean canApply(ItemStack stack) {
        boolean canApplyToUpgrade = false;
        Item coin = stack.getItem();
        if(coin instanceof IUpgradeBase)
        {
            canApplyToUpgrade = ((IUpgradeBase) coin).canAcceptOpSpeed();
            return stack.getItem() instanceof IUpgradeBase && canApplyToUpgrade;
        }
        if(coin instanceof ISpeedBook)
        {
            canApplyToUpgrade = true;
            return (coin instanceof ISpeedBook) && canApplyToUpgrade;
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        boolean canApplyToUpgrade = false;
        Item coin = stack.getItem();
        if(coin instanceof IUpgradeBase)
        {
            canApplyToUpgrade = ((IUpgradeBase) coin).canAcceptOpSpeed();
        }
        if(coin instanceof ISpeedBook)
        {
            canApplyToUpgrade = true;
        }

        return canApplyToUpgrade;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    public boolean canGenerateInLoot() {
        return true;
    }

    @Override
    public boolean isTreasureEnchantment() {
        return false;
    }
}

