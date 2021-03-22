package com.mowmaster.pedestals.item.deprecated;


import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBaseExpFilter;
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

public class ItemUpgradeExpRelayBlocked extends ItemUpgradeBaseExpFilter
{

    public ItemUpgradeExpRelayBlocked(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptOpSpeed() {
        return false;
    }

    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        return false;
    }

    @Override
    public int canAcceptCount(World world, BlockPos posPedestal, ItemStack inPedestal, ItemStack itemStackIncoming)
    {
        return 0;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                int getMaxXpValue = getExpCountByLevel(getExpBuffer(coinInPedestal));
                if(!hasMaxXpSet(coinInPedestal) || readMaxXpFromNBT(coinInPedestal) != getMaxXpValue) {setMaxXP(coinInPedestal, getMaxXpValue);}
                upgradeActionSendExp(pedestal);
            }
        }
    }


    @Override
    public int getExpBuffer(ItemStack stack)
    {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int overEnchanted = (capacityOver*5)+30;

        //20k being the max before we get close to int overflow
        return  (overEnchanted>=maxLVLStored)?(maxLVLStored):(overEnchanted);
    }

    public static final Item XPRELAYBLOCKED = new ItemUpgradeExpRelayBlocked(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xprelayblocked"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPRELAYBLOCKED);
    }
}
