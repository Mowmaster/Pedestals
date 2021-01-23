package com.mowmaster.pedestals.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemFilterSwapper extends Item {

    public ItemFilterSwapper() {
        super(new Properties().maxStackSize(1).containerItem(FILTERTOOL).group(PEDESTALS_TAB));
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(this.getItem());
    }


    public static final Item FILTERTOOL = new ItemFilterSwapper().setRegistryName(new ResourceLocation(MODID, "filterswapper"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FILTERTOOL);
    }




}
