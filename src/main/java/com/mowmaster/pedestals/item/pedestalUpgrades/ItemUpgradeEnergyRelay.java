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

public class ItemUpgradeEnergyRelay extends ItemUpgradeBaseEnergy
{
    public ItemUpgradeEnergyRelay(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canAcceptCapacity() {
        return true;
    }

    //Since Energy Transfer is as fast as possible, speed isnt needed, just capacity
    @Override
    public boolean canAcceptOpSpeed() {
        return false;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
            if(!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {setMaxEnergy(coinInPedestal, getMaxEnergyValue);}

            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                //Always send energy, as fast as we can within the Pedestal Energy Network
                upgradeActionSendEnergy(pedestal);
            }
        }
    }

    public static final Item RFRELAY = new ItemUpgradeEnergyRelay(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rfrelay"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RFRELAY);
    }

}
