package com.mowmaster.pedestals.item.deprecated;


import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBaseFilter;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFilterFoodBlacklist extends ItemUpgradeBaseFilter
{
    public ItemUpgradeFilterFoodBlacklist(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptAdvanced() {return true;}

    public void updateAction(World world, PedestalTileEntity pedestal)
    {

    }

    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        boolean returner = true;

        if(itemStackIn.isFood())
        {
            returner = false;
        }

        return returner;
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {

    }

    public static final Item FOOD = new ItemUpgradeFilterFoodBlacklist(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterfoodb"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FOOD);
    }



}
