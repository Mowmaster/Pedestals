package com.mowmaster.pedestals.item.books;

import com.mowmaster.pedestals.api.enchanting.IMagnetBook;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemMagnetBook extends ItemEnchantableBookBase implements IMagnetBook {

    public static final Item MAGNET = new ItemEnchantableBookBase().setRegistryName(new ResourceLocation(MODID, "bookmagnet"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(MAGNET);
    }
}
