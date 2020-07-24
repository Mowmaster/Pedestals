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

    public static final Item COPPER = new ItemDust().setRegistryName(new ResourceLocation(MODID, "dustcopper"));
    public static final Item TIN = new ItemDust().setRegistryName(new ResourceLocation(MODID, "dusttin"));
    public static final Item OSMIUM = new ItemDust().setRegistryName(new ResourceLocation(MODID, "dustosmium"));
    public static final Item URANIUM = new ItemDust().setRegistryName(new ResourceLocation(MODID, "dusturanium"));
    public static final Item LEAD = new ItemDust().setRegistryName(new ResourceLocation(MODID, "dustlead"));
    public static final Item SILVER = new ItemDust().setRegistryName(new ResourceLocation(MODID, "dustsilver"));
    public static final Item ALUMINUM = new ItemDust().setRegistryName(new ResourceLocation(MODID, "dustaluminum"));
    public static final Item NICKEL = new ItemDust().setRegistryName(new ResourceLocation(MODID, "dustnickel"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(IRON);
        event.getRegistry().register(GOLD);
        event.getRegistry().register(FLOUR);

        event.getRegistry().register(COPPER);
        event.getRegistry().register(TIN);
        event.getRegistry().register(OSMIUM);
        event.getRegistry().register(URANIUM);
        event.getRegistry().register(LEAD);
        event.getRegistry().register(SILVER);
        event.getRegistry().register(ALUMINUM);
        event.getRegistry().register(NICKEL);
    }




}
