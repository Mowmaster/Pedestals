package com.mowmaster.pedestals.item.pedestalUpgrades;

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

public class ItemUpgradeDefault extends ItemUpgradeBase
{
    public ItemUpgradeDefault(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public int canAcceptCount(World world, BlockPos pos,ItemStack inPedestal, ItemStack itemStackIncoming) {
        return 64;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {

    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {

    }

    public static final Item DEFAULT = new ItemUpgradeDefault(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/default"));
    //public static final Item DEFAULT0 = new ItemUpgradeDefault(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/default0"));
    //public static final Item DEFAULT1 = new ItemUpgradeDefault(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/default1"));
    //public static final Item DEFAULT2 = new ItemUpgradeDefault(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/default2"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(DEFAULT);
        //event.getRegistry().register(DEFAULT0);
        //event.getRegistry().register(DEFAULT1);
        //event.getRegistry().register(DEFAULT2);

    }


}
