package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.dust.dust;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.dust.references.Reference.MODID;

public class ItemUpgradeFilterEnchantedBlacklist extends ItemUpgradeBaseFilter
{
    public ItemUpgradeFilterEnchantedBlacklist(Properties builder) {super(builder.group(dust.ITEM_GROUP));}

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {

    }

    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        boolean returner = true;

        if(itemStackIn.isEnchanted()  || itemStackIn.getItem().equals(Items.ENCHANTED_BOOK))
        {
            returner = false;
        }

        return returner;
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {

    }

    public static final Item ENCHANTED = new ItemUpgradeFilterEnchantedBlacklist(new Properties().maxStackSize(64).group(dust.ITEM_GROUP)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedb"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ENCHANTED);
    }



}
