package com.mowmaster.pedestals.item.books;

import com.mowmaster.pedestals.api.enchanting.IRangeBook;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemRangeBook extends ItemEnchantableBookBase implements IRangeBook {

    public static final Item RANGE = new ItemRangeBook().setRegistryName(new ResourceLocation(MODID, "bookrange"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RANGE);
    }
}