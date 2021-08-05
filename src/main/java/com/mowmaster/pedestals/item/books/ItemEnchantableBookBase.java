package com.mowmaster.pedestals.item.books;

import com.mowmaster.pedestals.api.enchanting.*;
import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.references.Reference;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;

public class ItemEnchantableBookBase extends Item implements IEnchantableBook {

    public ItemEnchantableBookBase() {
        super(new Properties().maxStackSize(64).group(PEDESTALS_TAB));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if(stack.getItem() instanceof IEnchantableBook && enchantment.getRegistryName().getNamespace().equals(Reference.MODID))
        {
            return !EnchantmentRegistry.COINUPGRADE.equals(enchantment.type) && super.canApplyAtEnchantingTable(stack, enchantment);
        }
        return false;
    }

    @Override
    public int getItemEnchantability()
    {
        return 10;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }



    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {

        if(stack.isEnchanted())
        {
            entity.setInvulnerable(true);
            if(entity.isInLava() || entity.isBurning())
            {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
                ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK,stack.getCount());
                EnchantmentHelper.setEnchantments(enchants,newBook);
                entity.setItem(newBook);
            }
        }

        return super.onEntityItemUpdate(stack, entity);
    }

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        ItemAdvancedBook.onItemRegistryReady(event);
        ItemAreaBook.onItemRegistryReady(event);
        ItemCapacityBook.onItemRegistryReady(event);
        ItemMagnetBook.onItemRegistryReady(event);
        ItemRangeBook.onItemRegistryReady(event);
        ItemSpeedBook.onItemRegistryReady(event);
    }




}