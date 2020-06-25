package com.mowmaster.pedestals.item;

import com.mowmaster.dust.dust;
import com.mowmaster.dust.item.pedestalUpgrades.ItemUpgradeBase;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.dust.references.Reference.MODID;

public class ItemCraftingPlaceholder extends ItemUpgradeBase
{

    public ItemCraftingPlaceholder(Item.Properties builder) {super(builder.group(dust.ITEM_GROUP));}

    public static final Item PLACEHOLDER = new ItemCraftingPlaceholder(new Item.Properties().maxStackSize(64).group(dust.ITEM_GROUP)).setRegistryName(new ResourceLocation(MODID, "coin/placeholder"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(PLACEHOLDER);
    }


}
