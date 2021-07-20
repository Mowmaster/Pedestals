package com.mowmaster.pedestals.item.books;

import com.mowmaster.pedestals.api.enchanting.IAdvancedBook;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemAdvancedBook extends ItemEnchantableBookBase implements IAdvancedBook {

    public static final Item ADVANCED = new ItemEnchantableBookBase().setRegistryName(new ResourceLocation(MODID, "bookadvanced"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ADVANCED);
    }
}
