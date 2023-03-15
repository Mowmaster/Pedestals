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

public class ItemUpgradePackager extends ItemUpgradeBase implements IHasModeTypes
{
    public ItemUpgradePackager(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {
        int fluidCapacity = pedestal.getFluidCapacity();
        int energyCapacity = pedestal.getEnergyCapacity();
        int xpCapacity = pedestal.getExperienceCapacity();
        int dustCapacity = pedestal.getDustCapacity();

        boolean fluidFull1 = pedestal.getStoredFluid().getAmount() >= fluidCapacity;
        boolean energyFull1 = pedestal.getStoredEnergy() >= energyCapacity;
        boolean xpFull1 = pedestal.getStoredExperience() >= xpCapacity;
        boolean dustFull1 = pedestal.getStoredDust().getDustAmount() >= dustCapacity;
        if(fluidFull1 || energyFull1 || xpFull1 || dustFull1)
        {
            FluidStack fluidInPed = pedestal.getStoredFluid().copy();
            int energyInPed = pedestal.getStoredEnergy();
            int xpInPed = pedestal.getStoredExperience();
            DustMagic dustInPed = pedestal.getStoredDust();

            boolean fluidFull = fluidInPed.getAmount() >= fluidCapacity;
            boolean energyFull = energyInPed >= energyCapacity;
            boolean xpFull = xpInPed >= xpCapacity;
            boolean dustFull = dustInPed.getDustAmount() >= dustCapacity;

            if(fluidFull && canTransferFluids(coin))
            {
                if(fluidInPed.getAmount()>0)
                {
                    if(!pedestal.removeFluid(fluidInPed.getAmount(), IFluidHandler.FluidAction.SIMULATE).isEmpty())
                    {
                        ItemStack toPackage = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_FLUID.get());
                        if(pedestal.addItem(toPackage, true))
                        {
                            //extract Fluid
                            FluidStack toRemove = pedestal.removeFluid(fluidInPed.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                            if(toPackage.getItem() instanceof BaseFluidBulkStorageItem droppedItemFluid)
                            {
                                droppedItemFluid.setFluidStack(toPackage,toRemove);
                            }
                            pedestal.addItem(toPackage, false);
                        }
                    }
                }
            }
            if(energyFull && canTransferEnergy(coin))
            {
                if(energyInPed>0)
                {
                    if(pedestal.removeEnergy(energyInPed, true)>0)
                    {
                        ItemStack toPackage = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_ENERGY.get());
                        if(pedestal.addItem(toPackage, true))
                        {
                            //extract Energy
                            int energyToRemove = pedestal.removeEnergy(energyInPed,false);
                            if(toPackage.getItem() instanceof BaseEnergyBulkStorageItem droppedItemEnergy)
                            {
                                droppedItemEnergy.setEnergy(toPackage,energyToRemove);
                            }
                            pedestal.addItem(toPackage, false);
                        }
                    }
                }
            }
            if(xpFull && canTransferXP(coin))
            {
                if(xpInPed>0)
                {
                    if(pedestal.removeExperience(xpInPed, true)>0)
                    {
                        ItemStack toPackage = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_XP.get());
                        if(pedestal.addItem(toPackage, true))
                        {
                            //extract Energy
                            int xpToRemove = pedestal.removeExperience(xpInPed,false);
                            if(toPackage.getItem() instanceof BaseXpBulkStorageItem droppedItemXp)
                            {
                                droppedItemXp.setXp(toPackage,xpToRemove);
                            }
                            pedestal.addItem(toPackage, false);
                        }
                    }
                }
            }
            if(dustFull && canTransferDust(coin))
            {
                if(dustInPed.getDustAmount()>0)
                {
                    if(!pedestal.removeDust(dustInPed, IDustHandler.DustAction.SIMULATE).isEmpty())
                    {
                        ItemStack toPackage = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_DUST.get());
                        if(pedestal.addItem(toPackage, true))
                        {
                            //extract Energy
                            DustMagic dustToRemove = pedestal.removeDust(dustInPed, IDustHandler.DustAction.EXECUTE);
                            if(toPackage.getItem() instanceof BaseDustBulkStorageItem droppedItemDust)
                            {
                                droppedItemDust.setDustMagicInItem(toPackage,dustToRemove);
                            }
                            pedestal.addItem(toPackage, false);
                        }
                    }
                }
            }
        }
    }
}
