package com.mowmaster.pedestals.item;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemPedestalUpgrades extends Item {

    public ItemPedestalUpgrades() {
        super(new Properties().stacksTo(64).tab(PEDESTALS_TAB));
    }

    public static final Item SPEED = new ItemPedestalUpgrades().setRegistryName(new ResourceLocation(MODID, "upgradespeed"));
    public static final Item CAPACITY = new ItemPedestalUpgrades().setRegistryName(new ResourceLocation(MODID, "upgradecapacity"));
    public static final Item RANGE = new ItemPedestalUpgrades().setRegistryName(new ResourceLocation(MODID, "upgraderange"));
    public static final Item ROUNDROBIN = new ItemPedestalUpgrades().setRegistryName(new ResourceLocation(MODID, "upgraderoundrobin"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(SPEED);
        event.getRegistry().register(CAPACITY);
        event.getRegistry().register(RANGE);
        event.getRegistry().register(ROUNDROBIN);
    }




}
