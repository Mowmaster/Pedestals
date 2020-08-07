package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnergyTank extends ItemUpgradeBaseEnergy
{
    public ItemUpgradeEnergyTank(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }


    //Since Energy Transfer is as fast as possible, speed isnt needed, just capacity
    @Override
    public Boolean canAcceptOpSpeed() {
        return false;
    }

    @Override
    public int getEnergyBuffer(ItemStack stack) {
        //im assuming # = rf value???
        int energyBuffer = 1000;
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

    public int getEnergyTransferRate(ItemStack stack)
    {
        int energyTransferRate = 8000;
        switch (getCapacityModifier(stack))
        {

            case 0:
                energyTransferRate = 8000;//1x
                break;
            case 1:
                energyTransferRate=16000;//2x
                break;
            case 2:
                energyTransferRate = 32000;//4x
                break;
            case 3:
                energyTransferRate = 48000;//6x
                break;
            case 4:
                energyTransferRate = 80000;//10x
                break;
            case 5:
                energyTransferRate=160000;//20x
                break;
            default: energyTransferRate=8000;
        }

        return  energyTransferRate;
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);

            if(!world.isBlockPowered(pedestalPos))
            {
                //Always send energy, as fast as we can within the Pedestal Energy Network
                upgradeActionSendEnergy(world,coinInPedestal,pedestalPos);
                if (tick%speed == 0) {
                    upgradeAction(world,pedestalPos,coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
        if(!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {setMaxEnergy(coinInPedestal, getMaxEnergyValue);}
    }

    public static final Item RFTANK = new ItemUpgradeEnergyTank(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rftank"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RFTANK);
    }


}
