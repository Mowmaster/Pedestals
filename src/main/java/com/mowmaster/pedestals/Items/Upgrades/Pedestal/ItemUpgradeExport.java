package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Capability.Experience.IExperienceStorage;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import static com.mowmaster.pedestals.PedestalUtils.PedestalUtilities.*;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.stream.IntStream;



public class ItemUpgradeExport extends ItemUpgradeBase
{
    public ItemUpgradeExport(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level world = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack itemInOffhand = player.getOffhandItem();


        if(itemInOffhand.isEmpty())
        {

        }
        else
        {
            if(!itemInHand.getItem().equals(DeferredRegisterItems.PEDESTAL_UPGRADE_EXPORT))
            {
                if(itemInOffhand.getItem() instanceof IPedestalUpgrade)
                {
                    HitResult result = player.pick(5,0,false);
                    if(result.getType().equals(HitResult.Type.MISS))
                    {
                        if(player.isCrouching())
                        {
                            int mode = getUpgradeMode(itemInOffhand)+1;
                            int setNewMode = (mode<=14)?(mode):(0);
                            saveModeToNBT(itemInOffhand,setNewMode);
                            player.setItemInHand(InteractionHand.OFF_HAND,itemInOffhand);

                            TranslatableComponent changed = new TranslatableComponent(MODID + ".mode_changed");
                            ChatFormatting colorChange = ChatFormatting.BLACK;
                            String typeString = "";
                            switch(setNewMode)
                            {
                                case 0: typeString = ".mode_items"; colorChange = ChatFormatting.GOLD; break;
                                case 1: typeString = ".mode_fluids"; colorChange = ChatFormatting.DARK_BLUE; break;
                                case 2: typeString = ".mode_energy"; colorChange = ChatFormatting.RED; break;
                                case 3: typeString = ".mode_experience"; colorChange = ChatFormatting.GREEN; break;
                                case 4: typeString = ".mode_if"; colorChange = ChatFormatting.DARK_BLUE; break;
                                case 5: typeString = ".mode_ie"; colorChange = ChatFormatting.LIGHT_PURPLE; break;
                                case 6: typeString = ".mode_ix"; colorChange = ChatFormatting.DARK_GREEN; break;
                                case 7: typeString = ".mode_ife"; colorChange = ChatFormatting.WHITE; break;
                                case 8: typeString = ".mode_ifx"; colorChange = ChatFormatting.AQUA; break;
                                case 9: typeString = ".mode_iex"; colorChange = ChatFormatting.GOLD; break;
                                case 10: typeString = ".mode_ifex"; colorChange = ChatFormatting.BLACK; break;
                                case 11: typeString = ".mode_fe"; colorChange = ChatFormatting.LIGHT_PURPLE; break;
                                case 12: typeString = ".mode_fx"; colorChange = ChatFormatting.AQUA; break;
                                case 13: typeString = ".mode_ex"; colorChange = ChatFormatting.GOLD; break;
                                case 14: typeString = ".mode_fex"; colorChange = ChatFormatting.WHITE; break;
                                default: typeString = ".error"; colorChange = ChatFormatting.DARK_RED; break;
                            }
                            changed.withStyle(colorChange);
                            TranslatableComponent type = new TranslatableComponent(MODID + typeString);
                            changed.append(type);
                            player.displayClientMessage(changed,true);
                        }
                    }
                }
            }
        }

        return InteractionResultHolder.fail(p_41433_.getItemInHand(p_41434_));
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

            ItemStack stackInPedestal = pedestal.getItemInPedestal();
            ItemStack itemFromInv = ItemStack.EMPTY;
            LazyOptional<IItemHandler> cap = PedestalUtilities.findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);

            if(!stackInPedestal.isEmpty() && !stackInPedestal.equals(ItemStack.EMPTY))
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
                            //gets next empty or partially filled matching slot
                            int i = getNextSlotEmptyOrMatching(cap, stackInPedestal);
                            if(handler != null)
                            {
                                if(i>=0)
                                {
                                    if(handler.isItemValid(i, stackInPedestal))
                                    {
                                        stackInPedestal = pedestal.getItemInPedestal().copy();
                                        ItemStack itemFromInventory = handler.getStackInSlot(i);
                                        int spaceInInventoryStack = handler.getSlotLimit(i) - itemFromInventory.getCount();

                                        //if inv slot is empty it should be able to handle as much as we can give it
                                        int allowedTransferRate = transferRate;
                                        //checks allowed slot size amount and sets it if its lower then transfer rate
                                        if(handler.getSlotLimit(i) <= allowedTransferRate) allowedTransferRate = handler.getSlotLimit(i);
                                        //never have to check to see if pedestal and stack match because the slot checker does it for us
                                        //if our transfer rate is bigger then what can go in the slot if its partially full we set the transfer size to what can fit
                                        //Otherwise if space is bigger then rate we know it can accept as much as we're putting in
                                        if(allowedTransferRate> spaceInInventoryStack) allowedTransferRate = spaceInInventoryStack;
                                        //IF items in pedestal are less then the allowed transfer amount then set it as the amount
                                        if(allowedTransferRate > stackInPedestal.getCount()) allowedTransferRate = stackInPedestal.getCount();

                                        //After all calculations for transfer rate, set stack size to transfer and transfer the items
                                        stackInPedestal.setCount(allowedTransferRate);

                                        if(ItemHandlerHelper.insertItem(handler,stackInPedestal,true).equals(ItemStack.EMPTY)){
                                            pedestal.removeItem(allowedTransferRate);
                                            ItemHandlerHelper.insertItem(handler,stackInPedestal,false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(canTransferFluids(coinInPedestal))
        {
            LazyOptional<IFluidHandler> cap = findFluidHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
            if(cap.isPresent())
            {
                IFluidHandler handler = cap.orElse(null);
                if(handler != null)
                {
                    int tanks = handler.getTanks();
                    FluidStack fluidInPedestal = pedestal.getStoredFluid();
                    if(tanks > 1)
                    {
                        FluidStack fluidCheckedMatching = FluidStack.EMPTY;
                        fluidCheckedMatching = IntStream.range(0,tanks)//Int Range
                                .mapToObj((handler)::getFluidInTank)//Function being applied to each interval
                                .filter(fluidStack -> fluidStack.isFluidEqual(fluidInPedestal))
                                .findFirst().orElse(FluidStack.EMPTY);

                        //There is a matching fluid in a tank to fill
                        if(!fluidCheckedMatching.isEmpty())
                        {
                            FluidStack matchedFluid = fluidCheckedMatching;
                            int value = 0;
                            for(int location=0;location<tanks;location++)
                            {
                                if(handler.getFluidInTank(location).isFluidEqual(matchedFluid)){
                                    value = location;
                                    break;
                                }
                            }

                            int getTankCapacity = handler.getTankCapacity(value);
                            int tankCurrentlyStored = matchedFluid.getAmount();
                            int spaceInTank = getTankCapacity-tankCurrentlyStored;
                            int amountInCoin = fluidInPedestal.getAmount();

                            int rate = pedestal.getFluidTransferRate();
                            int actualCoinRate = (spaceInTank>=rate)?(rate):(spaceInTank);
                            int transferRate = (amountInCoin>=actualCoinRate)?(actualCoinRate):(amountInCoin);

                            if(spaceInTank >= transferRate)
                            {
                                FluidStack estFluidToFill = new FluidStack(fluidInPedestal.getFluid(),transferRate,fluidInPedestal.getTag());
                                int fluidToActuallyFill = handler.fill(estFluidToFill,IFluidHandler.FluidAction.SIMULATE);
                                FluidStack fluidToRemove = pedestal.removeFluid(fluidToActuallyFill, IFluidHandler.FluidAction.SIMULATE);
                                if(fluidToActuallyFill>0 && fluidToRemove.getAmount()>0)
                                {
                                    estFluidToFill = new FluidStack(fluidInPedestal.getFluid(),fluidToRemove.getAmount(),fluidInPedestal.getTag());
                                    int fluidDrained = handler.fill(estFluidToFill,IFluidHandler.FluidAction.EXECUTE);
                                    pedestal.removeFluid(fluidDrained,IFluidHandler.FluidAction.EXECUTE);
                                }
                            }
                        }
                        else
                        {
                            int value = 0;
                            for(int location=0;location<tanks;location++)
                            {
                                if(handler.getFluidInTank(location).isEmpty()){
                                    value = location;
                                    break;
                                }
                            }
                            FluidStack emptyTank = handler.getFluidInTank(value);
                            int getTankCapacity = handler.getTankCapacity(value);
                            int tankCurrentlyStored = emptyTank.getAmount();
                            int spaceInTank = getTankCapacity-tankCurrentlyStored;
                            int amountInCoin = fluidInPedestal.getAmount();

                            int rate = pedestal.getFluidTransferRate();
                            int actualCoinRate = (spaceInTank>=rate)?(rate):(spaceInTank);
                            int transferRate = (amountInCoin>=actualCoinRate)?(actualCoinRate):(amountInCoin);

                            if(spaceInTank >= transferRate)
                            {
                                FluidStack estFluidToFill = new FluidStack(fluidInPedestal.getFluid(),transferRate,fluidInPedestal.getTag());
                                int fluidToActuallyFill = handler.fill(estFluidToFill,IFluidHandler.FluidAction.SIMULATE);
                                FluidStack fluidToRemove = pedestal.removeFluid(fluidToActuallyFill, IFluidHandler.FluidAction.SIMULATE);
                                if(fluidToActuallyFill>0 && fluidToRemove.getAmount()>0)
                                {
                                    estFluidToFill = new FluidStack(fluidInPedestal.getFluid(),fluidToRemove.getAmount(),fluidInPedestal.getTag());
                                    int fluidDrained = handler.fill(estFluidToFill,IFluidHandler.FluidAction.EXECUTE);
                                    pedestal.removeFluid(fluidDrained,IFluidHandler.FluidAction.EXECUTE);
                                }

                            }
                        }
                    }
                    else
                    {
                        //should i just set this to zero???
                        FluidStack fluidInTank = handler.getFluidInTank(tanks-1);
                        if(fluidInTank.isEmpty() || fluidInPedestal.isFluidEqual(fluidInTank))
                        {
                            int getTankCapacity = handler.getTankCapacity(tanks-1);
                            int tankCurrentlyStored = fluidInTank.getAmount();
                            int spaceInTank = getTankCapacity-tankCurrentlyStored;
                            int amountInCoin = fluidInPedestal.getAmount();

                            int rate = pedestal.getFluidTransferRate();
                            int actualCoinRate = (spaceInTank>=rate)?(rate):(spaceInTank);
                            int transferRate = (amountInCoin>=actualCoinRate)?(actualCoinRate):(amountInCoin);
                            if(spaceInTank >= transferRate)
                            {
                                FluidStack estFluidToFill = new FluidStack(fluidInPedestal.getFluid(),transferRate,fluidInPedestal.getTag());
                                int fluidToActuallyFill = handler.fill(estFluidToFill,IFluidHandler.FluidAction.SIMULATE);
                                FluidStack fluidToRemove = pedestal.removeFluid(fluidToActuallyFill, IFluidHandler.FluidAction.SIMULATE);
                                if(fluidToActuallyFill>0 && fluidToRemove.getAmount()>0)
                                {
                                    estFluidToFill = new FluidStack(fluidInPedestal.getFluid(),fluidToRemove.getAmount(),fluidInPedestal.getTag());
                                    int fluidDrained = handler.fill(estFluidToFill,IFluidHandler.FluidAction.EXECUTE);
                                    pedestal.removeFluid(fluidDrained,IFluidHandler.FluidAction.EXECUTE);
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
            LazyOptional<IEnergyStorage> cap = findEnergyHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);

            if(cap.isPresent())
            {
                IEnergyStorage handler = cap.orElse(null);

                if(handler != null)
                {
                    if(handler.canReceive())
                    {
                        int containerMaxEnergy = handler.getMaxEnergyStored();
                        int containerCurrentEnergy = handler.getEnergyStored();
                        int containerEnergySpace = containerMaxEnergy - containerCurrentEnergy;
                        int getCurrentEnergy = pedestal.getStoredEnergy();
                        int transferRate = (containerEnergySpace >= pedestal.getEnergyTransferRate())?(pedestal.getEnergyTransferRate()):(containerEnergySpace);
                        if (getCurrentEnergy < transferRate) {transferRate = getCurrentEnergy;}

                        //transferRate at this point is equal to what we can send.
                        if(handler.receiveEnergy(transferRate,true) > 0)
                        {
                            pedestal.removeEnergy(transferRate,false);
                            handler.receiveEnergy(transferRate,false);
                        }
                    }
                }
            }
        }
        //XP
        if(canTransferXP(coinInPedestal))
        {
            LazyOptional<IExperienceStorage> cap = findExperienceHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);

            if(cap.isPresent())
            {
                IExperienceStorage handler = cap.orElse(null);

                if(handler != null)
                {
                    if(handler.canReceive())
                    {
                        int containerMaxExperience = handler.getMaxExperienceStored();
                        int containerCurrentExperience = handler.getExperienceStored();
                        int containerExperienceSpace = containerMaxExperience - containerCurrentExperience;
                        int getCurrentExperience = pedestal.getStoredExperience();
                        int transferRate = (containerExperienceSpace >= pedestal.getExperienceTransferRate())?(pedestal.getExperienceTransferRate()):(containerExperienceSpace);
                        if (getCurrentExperience < transferRate) {transferRate = getCurrentExperience;}

                        //transferRate at this point is equal to what we can send.
                        if(handler.receiveExperience(transferRate,true) > 0)
                        {
                            pedestal.removeExperience(transferRate,false);
                            handler.receiveExperience(transferRate,false);
                        }
                    }
                }
            }
        }
    }
}
