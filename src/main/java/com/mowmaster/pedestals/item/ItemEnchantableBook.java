package com.mowmaster.pedestals.item;

import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemEnchantableBook extends Item {

    public ItemEnchantableBook() {
        super(new Properties().maxStackSize(64).group(PEDESTALS_TAB));
    }

    public static final Item CAPACITY = new ItemEnchantableBook().setRegistryName(new ResourceLocation(MODID, "bookcapacity"));
    public static final Item RANGE = new ItemEnchantableBook().setRegistryName(new ResourceLocation(MODID, "bookrange"));
    public static final Item AREA = new ItemEnchantableBook().setRegistryName(new ResourceLocation(MODID, "bookarea"));
    public static final Item SPEED = new ItemEnchantableBook().setRegistryName(new ResourceLocation(MODID, "bookspeed"));
    public static final Item ADVANCED = new ItemEnchantableBook().setRegistryName(new ResourceLocation(MODID, "bookadvanced"));

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return !EnchantmentRegistry.COINUPGRADE.equals(enchantment.type) && super.canApplyAtEnchantingTable(stack, enchantment);
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

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(SPEED);
        event.getRegistry().register(RANGE);
        event.getRegistry().register(AREA);
        event.getRegistry().register(CAPACITY);
        event.getRegistry().register(ADVANCED);
    }




}
