package com.mowmaster.pedestals.item.books;

import com.mowmaster.pedestals.api.enchanting.IAreaBook;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemAreaBook extends ItemEnchantableBookBase implements IAreaBook {

    public static final Item AREA = new ItemEnchantableBookBase().setRegistryName(new ResourceLocation(MODID, "bookarea"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(AREA);
    }
}
