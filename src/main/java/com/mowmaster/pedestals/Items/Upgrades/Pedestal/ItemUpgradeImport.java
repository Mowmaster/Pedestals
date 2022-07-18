package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.mowlib.Capabilities.Experience.CapabilityExperience;
import com.mowmaster.mowlib.Capabilities.Experience.IExperienceStorage;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;

import static com.mowmaster.pedestals.PedestalUtils.PedestalUtilities.*;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;


import net.minecraft.world.item.Item.Properties;

public class ItemUpgradeImport extends ItemUpgradeBase implements IHasModeTypes
{
    public ItemUpgradeImport(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public void updateAction(Level world, BasePedestalBlockEntity pedestal) {

        upgradeAction(pedestal, world,pedestal.getPos(),pedestal.getCoinOnPedestal());
        /*if (world.getGameTime()%20 == 0) {

        }*/
    }

    public void upgradeAction(BasePedestalBlockEntity pedestal, Level world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);

        if(canTransferItems(coinInPedestal))
        {
            int transferRate = getItemTransferRate(coinInPedestal);

            ItemStack itemFromInv = ItemStack.EMPTY;
            LazyOptional<IItemHandler> cap = PedestalUtilities.findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
            if(!isInventoryEmpty(cap))
            {
                if(cap.isPresent())
                {
                    IItemHandler handler = cap.orElse(null);
                    BlockEntity invToPullFrom = world.getBlockEntity(posInventory);
                    if(invToPullFrom instanceof BasePedestalBlockEntity) {
                        itemFromInv = ItemStack.EMPTY;

                    }
                    else {
                        if(handler != null)
                        {
                            int i = getNextSlotWithItemsCapFiltered(pedestal,cap);
                            if(i>=0)
                            {
                                int maxStackSizeAllowedInPedestal = 0;
                                int roomLeftInPedestal = 0;
                                itemFromInv = handler.getStackInSlot(i);
                                ItemStack copyIncoming = itemFromInv.copy();
                                ItemStack itemFromPedestal = pedestal.getMatchingItemInPedestalOrEmptySlot(copyIncoming);
                                //if there IS a valid item in the inventory to pull out
                                if(itemFromInv != null && !itemFromInv.isEmpty() && itemFromInv.getItem() != Items.AIR)
                                {
                                    //If pedestal is empty, if not then set max possible stack size for pedestal itemstack(64)
                                    if(itemFromPedestal.isEmpty() || itemFromPedestal.equals(ItemStack.EMPTY))
                                    {maxStackSizeAllowedInPedestal = 64;}
                                    else
                                    {maxStackSizeAllowedInPedestal = itemFromPedestal.getMaxStackSize();}

                                    //Get Room left in pedestal
                                    roomLeftInPedestal = maxStackSizeAllowedInPedestal-itemFromPedestal.getCount();
                                    //Get items stack count(from inventory)
                                    int itemCountInInv = itemFromInv.getCount();
                                    //Allowed transfer rate (from coin)
                                    int allowedTransferRate = transferRate;
                                    //Checks to see if pedestal can accept as many items as transferRate IF NOT it sets the new rate to what it can accept
                                    if(roomLeftInPedestal < transferRate) allowedTransferRate = roomLeftInPedestal;
                                    //Checks to see how many items are left in the slot IF ITS UNDER the allowedTransferRate then sent the max rate to that.
                                    if(itemCountInInv < allowedTransferRate) allowedTransferRate = itemCountInInv;

                                    //if(itemFromInv.maxStackSize() < allowedTransferRate) allowedTransferRate = itemFromInv.maxStackSize();


                                    copyIncoming.setCount(allowedTransferRate);
                                    if(!handler.extractItem(i,allowedTransferRate ,true ).isEmpty() && pedestal.addItem(copyIncoming, true))
                                    {
                                        handler.extractItem(i,allowedTransferRate ,false );
                                        pedestal.addItem(copyIncoming, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //Fluids
        if(canTransferFluids(coinInPedestal))
        {
            LazyOptional<IFluidHandler> cap = findFluidHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
            if(cap.isPresent())
            {
                IFluidHandler handler = cap.orElse(null);
                if(handler != null)
                {
                    int tanks = handler.getTanks();
                    FluidStack fluidCheckedMatching = FluidStack.EMPTY;
                    fluidCheckedMatching = IntStream.range(0,tanks)//Int Range
                            .mapToObj((handler)::getFluidInTank)//Function being applied to each interval
                            .filter(fluidStack -> !fluidStack.isEmpty())
                            .findFirst().orElse(FluidStack.EMPTY);

                    if(!fluidCheckedMatching.isEmpty())
                    {
                        FluidStack fluidInPedestal = pedestal.getStoredFluid();
                        int fluidSpaceInPedestal = pedestal.spaceForFluid();
                        if(tanks > 1)
                        {
                            if(!fluidInPedestal.isEmpty())
                            {
                                //Default grab from first tank
                                FluidStack fluidInTank = handler.getFluidInTank(0);
                                int amountIn = fluidInTank.getAmount();
                                int rate = pedestal.getFluidTransferRate();
                                int actualCoinRate = (fluidSpaceInPedestal>=rate)?(rate):(fluidSpaceInPedestal);
                                int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                if(fluidSpaceInPedestal >= transferRate || pedestal.getStoredFluid().isEmpty())
                                {
                                    FluidStack estFluidToDrain = fluidInTank.copy();
                                    estFluidToDrain.setAmount(transferRate);
                                    FluidStack fluidToActuallyDrain = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                    int amountCanAddToPedestal = pedestal.addFluid(fluidToActuallyDrain,IFluidHandler.FluidAction.SIMULATE);
                                    if(!fluidInTank.isEmpty() && amountCanAddToPedestal>0)
                                    {
                                        FluidStack fluidDrained = handler.drain(amountCanAddToPedestal,IFluidHandler.FluidAction.EXECUTE);
                                        pedestal.addFluid(fluidDrained,IFluidHandler.FluidAction.EXECUTE);
                                    }
                                }
                            }
                            else
                            {
                                FluidStack fluidMatching = FluidStack.EMPTY;
                                fluidMatching = IntStream.range(0,tanks)//Int Range
                                        .mapToObj((handler)::getFluidInTank)//Function being applied to each interval
                                        .filter(fluidStack -> fluidInPedestal.isFluidEqual(fluidStack))
                                        .findFirst().orElse(FluidStack.EMPTY);

                                if(!fluidMatching.isEmpty())
                                {
                                    int amountIn = fluidMatching.getAmount();
                                    int rate = pedestal.getFluidTransferRate();
                                    int actualCoinRate = (fluidSpaceInPedestal>=rate)?(rate):(fluidSpaceInPedestal);
                                    int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                    if(fluidSpaceInPedestal >= transferRate || pedestal.getStoredFluid().isEmpty())
                                    {
                                        FluidStack estFluidToDrain = fluidMatching.copy();
                                        estFluidToDrain.setAmount(transferRate);
                                        FluidStack fluidToActuallyDrain = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                        int amountCanAddToPedestal = pedestal.addFluid(fluidToActuallyDrain,IFluidHandler.FluidAction.SIMULATE);
                                        if(!fluidMatching.isEmpty() && amountCanAddToPedestal>0)
                                        {
                                            FluidStack fluidDrained = handler.drain(amountCanAddToPedestal,IFluidHandler.FluidAction.EXECUTE);
                                            pedestal.addFluid(fluidDrained,IFluidHandler.FluidAction.EXECUTE);
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            //should i just set this to zero???
                            FluidStack fluidInTank = handler.getFluidInTank(tanks-1);
                            if(fluidInPedestal.isEmpty() || fluidInPedestal.isFluidEqual(fluidInTank))
                            {
                                int amountIn = fluidInTank.getAmount();
                                int rate = pedestal.getFluidTransferRate();
                                int actualCoinRate = (fluidSpaceInPedestal>=rate)?(rate):(fluidSpaceInPedestal);
                                int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                if(fluidSpaceInPedestal >= transferRate || pedestal.getStoredFluid().isEmpty())
                                {
                                    FluidStack estFluidToDrain = fluidInTank.copy();
                                    estFluidToDrain.setAmount(transferRate);
                                    FluidStack fluidToActuallyDrain = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                    int amountCanAddToPedestal = pedestal.addFluid(fluidToActuallyDrain,IFluidHandler.FluidAction.SIMULATE);
                                    if(!fluidInTank.isEmpty() && amountCanAddToPedestal>0)
                                    {
                                        FluidStack fluidDrained = handler.drain(amountCanAddToPedestal,IFluidHandler.FluidAction.EXECUTE);
                                        pedestal.addFluid(fluidDrained,IFluidHandler.FluidAction.EXECUTE);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //Energy
        if(canTransferEnergy(coinInPedestal))
        {
            int getMaxEnergyValue = pedestal.getEnergyCapacity();

            LazyOptional<IEnergyStorage> cap = findEnergyHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);

            if(cap.isPresent())
            {
                IEnergyStorage handler = cap.orElse(null);

                if(handler != null)
                {
                    if(handler.canExtract())
                    {
                        int containerCurrentEnergy = handler.getEnergyStored();
                        int getMaxEnergy = getMaxEnergyValue;
                        int getCurrentEnergy = pedestal.getStoredEnergy();
                        int getSpaceForEnergy = getMaxEnergy - getCurrentEnergy;
                        int transferRate = (getSpaceForEnergy >= pedestal.getEnergyTransferRate())?(pedestal.getEnergyTransferRate()):(getSpaceForEnergy);
                        if (containerCurrentEnergy < transferRate) {transferRate = containerCurrentEnergy;}

                        //transferRate at this point is equal to what we can send.
                        if(handler.extractEnergy(transferRate,true) > 0)
                        {
                            pedestal.addEnergy(transferRate,false);
                            handler.extractEnergy(transferRate,false);
                        }
                    }
                }
            }
        }
        if(canTransferXP(coinInPedestal))
        {
            int getMaxExperienceValue = pedestal.getExperienceCapacity();

            LazyOptional<IExperienceStorage> cap = PedestalUtilities.findExperienceHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);

            //Gets inventory TE then makes sure its not a pedestal
            BlockEntity invToPushTo = world.getBlockEntity(posInventory);

            if(cap.isPresent())
            {
                IExperienceStorage handler = cap.orElse(null);

                if(handler != null)
                {
                    if(handler.canExtract())
                    {
                        int containerCurrentExperience = handler.getExperienceStored();
                        int getMaxExperience = getMaxExperienceValue;
                        int getCurrentExperience = pedestal.getStoredExperience();
                        int getSpaceForExperience = getMaxExperience - getCurrentExperience;
                        int transferRate = (getSpaceForExperience >= pedestal.getExperienceTransferRate())?(pedestal.getExperienceTransferRate()):(getSpaceForExperience);
                        if (containerCurrentExperience < transferRate) {transferRate = containerCurrentExperience;}

                        //transferRate at this point is equal to what we can send.
                        if(handler.extractExperience(transferRate,true) > 0)
                        {
                            pedestal.addExperience(transferRate,false);
                            handler.extractExperience(transferRate,false);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void actionOnCollideWithBlock(BasePedestalBlockEntity pedestal, Entity entityIn) {
        if(canTransferItems(pedestal.getCoinOnPedestal()))
        {
            if(entityIn instanceof ItemEntity)
            {
                ItemEntity itemEntity = ((ItemEntity) entityIn);
                ItemStack itemStack = itemEntity.getItem();
                ItemStack stackInPedestal = pedestal.getItemInPedestal();
                boolean stacksMatch = doItemsMatch(stackInPedestal,itemStack);
                if((!pedestal.hasItem() || stacksMatch) && passesItemFilter(pedestal,itemStack))
                {
                    int spaceInPed = stackInPedestal.getMaxStackSize()-stackInPedestal.getCount();
                    int filterAllowedSpace = getCountItemFilter(pedestal,itemStack);
                    int actualSpaceInPed = (filterAllowedSpace>spaceInPed)?(spaceInPed):(filterAllowedSpace);
                    if(actualSpaceInPed>0)
                    {
                        int itemInCount = itemStack.getCount();
                        int countToAdd = ( itemInCount<= actualSpaceInPed)?(itemInCount):(actualSpaceInPed);
                        ItemStack stackToAdd = itemStack.copy();
                        stackToAdd.setCount(countToAdd);
                        if(pedestal.addItem(stackToAdd,true))
                        {
                            itemEntity.getItem().setCount(itemInCount-countToAdd);
                            if(itemInCount<=countToAdd)itemEntity.remove(Entity.RemovalReason.DISCARDED);
                            pedestal.addItem(stackToAdd,false);
                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ(),180,180,180));
                        }
                    }
                }
            }
            else if (entityIn instanceof Player)
            {
                Player player = ((Player) entityIn);
                if(!player.isCrouching())
                {
                    ItemStack itemFromInv = ItemStack.EMPTY;

                    itemFromInv = IntStream.range(0,(player.getInventory().items.size()))//Int Range
                            .mapToObj((player.getInventory().items)::get)//Function being applied to each interval
                            .filter(itemStack -> !itemStack.isEmpty())
                            .filter(itemStack -> passesItemFilter(pedestal,itemStack))
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!itemFromInv.isEmpty())
                    {
                        ItemStack itemStack = itemFromInv;
                        ItemStack stackInPedestal = pedestal.getItemInPedestal();
                        boolean stacksMatch = doItemsMatch(stackInPedestal,itemStack);
                        if((!pedestal.hasItem() || stacksMatch) && passesItemFilter(pedestal,itemStack))
                        {
                            int spaceInPed = stackInPedestal.getMaxStackSize()-stackInPedestal.getCount();
                            int filterAllowedSpace = getCountItemFilter(pedestal,itemStack);
                            int actualSpaceInPed = (filterAllowedSpace>spaceInPed)?(spaceInPed):(filterAllowedSpace);
                            if(actualSpaceInPed>0)
                            {
                                int itemInCount = itemStack.getCount();
                                int countToAdd = ( itemInCount<= actualSpaceInPed)?(itemInCount):(actualSpaceInPed);
                                ItemStack stackToAdd = itemStack.copy();
                                stackToAdd.setCount(countToAdd);
                                if(pedestal.addItem(stackToAdd,true))
                                {
                                    ItemStack newStackInPlayer = (itemInCount>countToAdd)?(itemStack.copy()):(ItemStack.EMPTY);
                                    if(!newStackInPlayer.isEmpty())newStackInPlayer.setCount(itemInCount-countToAdd);
                                    int slot = player.getInventory().findSlotMatchingItem(itemStack);
                                    player.getInventory().setItem(slot,newStackInPlayer);
                                    pedestal.addItem(stackToAdd,false);
                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ(),180,180,0));
                                }
                            }
                        }
                    }
                }
            }
        }

        if(canTransferXP(pedestal.getCoinOnPedestal()) && pedestal.canAcceptExperience())
        {
            if (entityIn instanceof Player) {
                Player player = ((Player) entityIn);
                if(!player.isCrouching())
                {
                    int currentlyStoredExp = pedestal.getStoredExperience();
                    if(currentlyStoredExp < pedestal.getExperienceCapacity())
                    {
                        int transferRate = pedestal.getExperienceTransferRate();
                        int value = removeXp(player, transferRate);
                        if(value > 0)
                        {
                            pedestal.addExperience(value,false);
                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ(),0,255,0));
                        }
                    }
                }
            }
        }

        if(canTransferFluids(pedestal.getCoinOnPedestal()))
        {
            if (entityIn instanceof Player) {
                Player player = ((Player) entityIn);
                if(!player.isCrouching())
                {
                    ItemStack bucketItemStack = IntStream.range(0,(player.getInventory().items.size()))//Int Range
                            .mapToObj((player.getInventory().items)::get)//Function being applied to each interval
                            .filter(itemStack -> !itemStack.isEmpty())
                            .filter(itemStack -> !itemStack.getItem().equals(Items.BUCKET))
                            .filter(itemStack -> itemStack.getItem() instanceof BucketItem)
                            .filter(itemStack -> passesFluidFilter(pedestal,itemStack))
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!bucketItemStack.isEmpty())
                    {
                        BucketItem bucket = ((BucketItem)bucketItemStack.getItem());
                        Fluid bucketFluid = bucket.getFluid();
                        FluidStack fluidInTank = new FluidStack(bucketFluid,1000);
                        int fluidSpaceInPedestal = pedestal.spaceForFluid();

                        FluidStack fluidInPedestal = pedestal.getStoredFluid();
                        if(fluidInPedestal.isEmpty() || fluidInPedestal.isFluidEqual(fluidInTank))
                        {
                            int transferRate = 1000;
                            if(fluidSpaceInPedestal >= transferRate || pedestal.getStoredFluid().isEmpty())
                            {
                                FluidStack fluidDrained = fluidInTank.copy();
                                if(!fluidInTank.isEmpty())
                                {
                                    pedestal.addFluid(fluidDrained,IFluidHandler.FluidAction.EXECUTE);
                                    int slot = player.getInventory().findSlotMatchingItem(bucketItemStack);
                                    if(!player.isCreative())player.getInventory().getItem(slot).shrink(1);
                                    if(!player.isCreative())ItemHandlerHelper.giveItemToPlayer(player,new ItemStack(Items.BUCKET,1));

                                    String fluid = pedestal.getStoredFluid().getDisplayName().getString() +": " +pedestal.getStoredFluid().getAmount() +"/"+pedestal.getFluidCapacity();
                                    MowLibMessageUtils.messagePopupText(player,ChatFormatting.WHITE,fluid);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
