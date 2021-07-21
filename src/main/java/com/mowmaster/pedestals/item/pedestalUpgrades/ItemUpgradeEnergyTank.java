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

public class ItemUpgradeEnergyTank extends ItemUpgradeBaseEnergy
{
    public ItemUpgradeEnergyTank(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canAcceptCapacity() {
        return true;
    }


    //Since Energy Transfer is as fast as possible, speed isnt needed, just capacity
    @Override
    public boolean canAcceptOpSpeed() {
        return false;
    }

    @Override
    public int getEnergyBuffer(ItemStack stack) {
        //im assuming # = rf value???
        int energyBuffer = 100000;
        switch (getCapacityModifier(stack))
        {

            case 0:
                energyBuffer = 100000;//100k
                break;
            case 1:
                energyBuffer=500000;//half mil
                break;
            case 2:
                energyBuffer = 1000000;//mil
                break;
            case 3:
                energyBuffer = 500000000;//half bil
                break;
            case 4:
                energyBuffer = 1000000000;//bil
                break;
            case 5:
                energyBuffer=2000000000;//2 bil
                break;
            default: energyBuffer=100000;
        }

        return  energyBuffer;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int speed = getOperationSpeed(coinInPedestal);

            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                //Always send energy, as fast as we can within the Pedestal Energy Network
                upgradeActionSendEnergy(pedestal);
                int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
                if(!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {setMaxEnergy(coinInPedestal, getMaxEnergyValue);}

            }
        }
    }

    public static final Item RFTANK = new ItemUpgradeEnergyTank(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rftank"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RFTANK);
    }


}
