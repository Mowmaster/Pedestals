package com.mowmaster.pedestals.item;

import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBase;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemCraftingPlaceholder extends ItemUpgradeBase
{

    public ItemCraftingPlaceholder(Item.Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    public static final Item PLACEHOLDER = new ItemCraftingPlaceholder(new Item.Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/placeholder"));
    public static final Item PLACEHOLDER_BUCKET = new ItemCraftingPlaceholder(new Item.Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/placeholderbucket"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(PLACEHOLDER);
        event.getRegistry().register(PLACEHOLDER_BUCKET);
    }


}
