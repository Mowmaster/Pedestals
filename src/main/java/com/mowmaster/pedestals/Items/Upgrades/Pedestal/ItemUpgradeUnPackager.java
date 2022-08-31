package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseDustBulkStorageItem;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseEnergyBulkStorageItem;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseFluidBulkStorageItem;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseXpBulkStorageItem;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ItemUpgradeUnPackager extends ItemUpgradeBase implements IHasModeTypes
{
    public ItemUpgradeUnPackager(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public void updateAction(Level world, BasePedestalBlockEntity pedestal) {

        if(pedestal.getItemInPedestal().getItem() instanceof BaseFluidBulkStorageItem
        || pedestal.getItemInPedestal().getItem() instanceof BaseEnergyBulkStorageItem
        || pedestal.getItemInPedestal().getItem() instanceof BaseXpBulkStorageItem
        || pedestal.getItemInPedestal().getItem() instanceof BaseDustBulkStorageItem)
        {
            upgradeAction(pedestal, world,pedestal.getPos(),pedestal.getCoinOnPedestal());
        }
    }

    public void upgradeAction(BasePedestalBlockEntity pedestal, Level world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        ItemStack stackInPed = pedestal.getItemInPedestal();
        int fluidSpace = pedestal.spaceForFluid();
        int energySpace = pedestal.spaceForEnergy();
        int xpSpace = pedestal.spaceForExperience();
        int dustSpace = pedestal.spaceForDust();

        if(stackInPed.getItem() instanceof BaseFluidBulkStorageItem fluidItem && canTransferFluids(coinInPedestal))
        {
            if(fluidSpace>0)
            {
                FluidStack fluidInItemCopy = fluidItem.getFluid(stackInPed).copy();
                if(pedestal.addFluid(fluidInItemCopy, IFluidHandler.FluidAction.SIMULATE)>0)
                {
                    if(!pedestal.removeItemStack(stackInPed,true).isEmpty())
                    {
                        //extract Fluid
                        int toRemove = pedestal.addFluid(fluidInItemCopy, IFluidHandler.FluidAction.EXECUTE);
                        if(toRemove < fluidInItemCopy.getAmount())
                        {
                            fluidItem.removeFluid(stackInPed,toRemove);
                        }
                        else
                        {
                            pedestal.removeItemStack(stackInPed,false);
                        }

                        stackInPed = pedestal.getItemInPedestal().copy();
                    }
                }
            }
        }
        if(stackInPed.getItem() instanceof BaseEnergyBulkStorageItem energyItem && canTransferEnergy(coinInPedestal))
        {
            if(energySpace>0)
            {
                int energyInItem = energyItem.getEnergy(stackInPed);
                if(pedestal.addEnergy(energyInItem,true)>0)
                {
                    if(!pedestal.removeItemStack(stackInPed,true).isEmpty())
                    {
                        //extract Fluid
                        int toRemove = pedestal.addEnergy(energyInItem,false);
                        if(toRemove < energyInItem)
                        {
                            energyItem.removeEnergy(stackInPed,toRemove);
                        }
                        else
                        {
                            pedestal.removeItemStack(stackInPed,false);
                        }

                        stackInPed = pedestal.getItemInPedestal().copy();
                    }
                }
            }
        }
        if(stackInPed.getItem() instanceof BaseXpBulkStorageItem xpItem && canTransferXP(coinInPedestal))
        {
            if(xpSpace>0)
            {
                int xpInItem = xpItem.getXp(stackInPed);
                if(pedestal.addExperience(xpInItem,true)>0)
                {
                    if(!pedestal.removeItemStack(stackInPed,true).isEmpty())
                    {
                        //extract Fluid
                        int toRemove = pedestal.addExperience(xpInItem,false);
                        if(toRemove < xpInItem)
                        {
                            xpItem.removeXp(stackInPed,toRemove);
                        }
                        else
                        {
                            pedestal.removeItemStack(stackInPed,false);
                        }

                        stackInPed = pedestal.getItemInPedestal().copy();
                    }
                }
            }
        }
        if(stackInPed.getItem() instanceof BaseDustBulkStorageItem && canTransferDust(coinInPedestal))
        {
            if(dustSpace>0)
            {
                DustMagic dustInItemCopy = DustMagic.getDustMagicInItemStack(stackInPed).copy();
                if(pedestal.addDust(dustInItemCopy, IDustHandler.DustAction.SIMULATE)>0)
                {
                    if(!pedestal.removeItemStack(stackInPed,true).isEmpty())
                    {
                        //extract Fluid
                        int toRemove = pedestal.addDust(dustInItemCopy, IDustHandler.DustAction.EXECUTE);
                        if(toRemove < dustInItemCopy.getDustAmount())
                        {
                            DustMagic.setDustMagicInStack(stackInPed,new DustMagic(dustInItemCopy.getDustColor(),(dustInItemCopy.getDustAmount()-toRemove)));
                        }
                        else
                        {
                            pedestal.removeItemStack(stackInPed,false);
                        }
                    }
                }
            }
        }
    }
}
