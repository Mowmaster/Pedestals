package com.mowmaster.pedestals.enchants;

import com.mowmaster.pedestals.item.ItemEnchantableBook;
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

    public int getMaxEnchantability(int enchantmentLevel) {
        return this.getMinEnchantability(enchantmentLevel) + 20;
    }

    public int getMaxLevel() {
        return 5;
    }

    public boolean canApply(ItemStack stack) {
        boolean canApplyToUpgrade = false;
        Item coin = stack.getItem();
        if(coin instanceof ItemUpgradeBase)
        {
            canApplyToUpgrade = ((ItemUpgradeBase) coin).canAcceptOpSpeed();
            return stack.getItem() instanceof ItemUpgradeBase && canApplyToUpgrade;
        }
        if(coin.equals(ItemEnchantableBook.SPEED))
        {
            canApplyToUpgrade = true;
            return stack.equals(ItemEnchantableBook.SPEED) && canApplyToUpgrade;
        }
        return false;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        boolean canApplyToUpgrade = false;
        Item coin = stack.getItem();
        if(coin instanceof ItemUpgradeBase)
        {
            canApplyToUpgrade = ((ItemUpgradeBase) coin).canAcceptOpSpeed();
        }
        if(coin.equals(ItemEnchantableBook.SPEED))
        {
            canApplyToUpgrade = true;
        }

        return canApplyToUpgrade;
    }

    public boolean isAllowedOnBooks() {
        return true;
    }
}

