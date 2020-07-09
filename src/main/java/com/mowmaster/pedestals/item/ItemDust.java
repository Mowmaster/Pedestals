package com.mowmaster.pedestals.item;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemDust extends Item {

    public ItemDust() {
        super(new Properties().maxStackSize(64).group(PEDESTALS_TAB));
    }

    public static final Item IRON = new ItemDust().setRegistryName(new ResourceLocation(MODID, "dustiron"));
    public static final Item GOLD = new ItemDust().setRegistryName(new ResourceLocation(MODID, "dustgold"));
    public static final Item FLOUR = new ItemDust().setRegistryName(new ResourceLocation(MODID, "dustflour"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(IRON);
        event.getRegistry().register(GOLD);
        event.getRegistry().register(FLOUR);
    }




}
