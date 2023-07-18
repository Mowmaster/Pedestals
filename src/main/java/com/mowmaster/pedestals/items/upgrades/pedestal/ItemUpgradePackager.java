package com.mowmaster.pedestals.items.upgrades.pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.configs.PedestalConfig;
import com.mowmaster.pedestals.items.mechanicalonlystorage.*;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

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
    public int getUpgradeWorkRange(ItemStack coinUpgrade) { return 0; }

    public int getMaxItemStacksAllowed()
    {
        return PedestalConfig.COMMON.bulkstorage_maxItemStorage.get();
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

        FluidStack fluidInPed = pedestal.getStoredFluid().copy();
        int energyInPed = pedestal.getStoredEnergy();
        int xpInPed = pedestal.getStoredExperience();
        DustMagic dustInPed = pedestal.getStoredDust();

        boolean fluidFull = fluidInPed.getAmount() >= fluidCapacity;
        boolean energyFull = energyInPed >= energyCapacity;
        boolean xpFull = xpInPed >= xpCapacity;
        boolean dustFull = dustInPed.getDustAmount() >= dustCapacity;

        if(canTransferItems(coin))
        {
            List<ItemStack> stacks = new ArrayList<>();
            BlockPos posInventory = getPosOfBlockBelow(level,pedestalPos,1);
            LazyOptional<IItemHandler> cap = MowLibItemUtils.findItemHandlerAtPos(level,posInventory,getPedestalFacing(level, pedestalPos),true);
            if(!isInventoryEmpty(cap)) {
                if (cap.isPresent()) {
                    IItemHandler handler = cap.orElse(null);
                    if(handler !=null)
                    {
                        int handlerSize = handler.getSlots();
                        for(int i=0;i<handler.getSlots();i++)
                        {
                            ItemStack stackIn = handler.getStackInSlot(i);
                            //Dont Allow Bulk items to be Packaged
                            if(stackIn.getItem() instanceof IBulkItem) { break; }
                            int maxSizeStack = stackIn.getMaxStackSize();
                            if(stackIn.getCount()>=maxSizeStack)
                            {
                                if(i>0)
                                {
                                    if(!doItemsMatch(stacks.get(0),stackIn))
                                    {
                                        break;
                                    }
                                    else {
                                        stacks.add(stackIn);
                                    }
                                }
                                else {
                                    stacks.add(stackIn);
                                }
                            }
                            else
                            {
                                break;
                            }
                        }

                        if(stacks.size()==handlerSize)
                        {
                            //Make package
                            ItemStack toPackage = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_ITEM.get());
                            if(pedestal.addItem(toPackage, true))
                            {
                                List<ItemStack> newList = new ArrayList<>();
                                int maxSlots = (handlerSize>getMaxItemStacksAllowed())?(getMaxItemStacksAllowed()):(handlerSize);
                                for(int i=0;i<maxSlots;i++)
                                {
                                    ItemStack stackIn = handler.getStackInSlot(i);
                                    if(!handler.extractItem(i,stackIn.getCount(),true).isEmpty())
                                    {
                                        newList.add(stackIn.copy());
                                        handler.extractItem(i,stackIn.getCount(),false);
                                    }
                                }

                                if(newList.size()>0)
                                {
                                    if(toPackage.getItem() instanceof BaseItemBulkStorageItem bulkItemPackage)
                                    {
                                        bulkItemPackage.setStacksList(toPackage,newList);
                                    }
                                    pedestal.addItem(toPackage, false);
                                }

                            }
                        }
                    }
                }
            }
        }
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
