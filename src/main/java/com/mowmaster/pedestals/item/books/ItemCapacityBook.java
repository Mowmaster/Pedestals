package com.mowmaster.pedestals.item.books;

import com.mowmaster.pedestals.api.enchanting.ICapacityBook;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemCapacityBook extends ItemEnchantableBookBase implements ICapacityBook {

    public static final Item CAPACITY = new ItemCapacityBook().setRegistryName(new ResourceLocation(MODID, "bookcapacity"));
    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(CAPACITY);
    }
}