package com.mowmaster.pedestals.item.books;

import com.mowmaster.pedestals.api.enchanting.ISpeedBook;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemSpeedBook extends ItemEnchantableBookBase implements ISpeedBook {

    public static final Item SPEED = new ItemSpeedBook().setRegistryName(new ResourceLocation(MODID, "bookspeed"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(SPEED);
    }
}