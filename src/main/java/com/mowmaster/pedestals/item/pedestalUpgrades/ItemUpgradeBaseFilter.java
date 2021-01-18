package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

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

    public Boolean canAcceptAdvanced()
    {
        return false;
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".chat_name");
        TranslationTextComponent name2 = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.appendString(name2.getString());
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name, Util.DUMMY_UUID);
    }
}
