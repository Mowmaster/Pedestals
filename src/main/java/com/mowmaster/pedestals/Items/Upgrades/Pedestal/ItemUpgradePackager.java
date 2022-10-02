package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibXpUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseDustBulkStorageItem;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseEnergyBulkStorageItem;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseFluidBulkStorageItem;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseXpBulkStorageItem;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.IHasModeTypes;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ISelectableArea;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class ItemUpgradePackager extends ItemUpgradeBase implements IHasModeTypes
{
    public ItemUpgradePackager(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {
        boolean fluidFull1 = pedestal.getStoredFluid().getAmount() >= pedestal.getFluidCapacity();
        boolean energyFull1 = pedestal.getStoredEnergy() >= pedestal.getEnergyCapacity();
        boolean xpFull1 = pedestal.getStoredExperience() >= pedestal.getExperienceCapacity();
        boolean dustFull1 = pedestal.getStoredDust().getDustAmount() >= pedestal.getDustCapacity();
        if(fluidFull1 || energyFull1 || xpFull1 || dustFull1)
        {
            FluidStack fluidInPed = pedestal.getStoredFluid().copy();
            int energyInPed = pedestal.getStoredEnergy();
            int xpInPed = pedestal.getStoredExperience();
            DustMagic dustInPed = pedestal.getStoredDust();

            boolean fluidFull = fluidInPed.getAmount() >= pedestal.getFluidCapacity();
            boolean energyFull = energyInPed >= pedestal.getEnergyCapacity();
            boolean xpFull = xpInPed >= pedestal.getExperienceCapacity();
            boolean dustFull = dustInPed.getDustAmount() >= pedestal.getDustCapacity();

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
