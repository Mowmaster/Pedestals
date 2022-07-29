package com.mowmaster.pedestals.Blocks.Pedestal;

public class tempSave
{

    /*
    //Returns items available to be insert, 0 if false
    public int itemCountToAccept(Level worldIn, BlockPos posPedestal, ItemStack itemsIncoming)
    {
        int canAccept = 0;
        int pedestalAccept = 0;

        if(getMatchingItemInPedestalOrEmptySlot(itemsIncoming).isEmpty() || getMatchingItemInPedestalOrEmptySlot(itemsIncoming).equals(ItemStack.EMPTY))
        {
            canAccept = itemsIncoming.getMaxStackSize();
        }
        else
        {
            if(ItemHandlerHelper.canItemStacksStack(getMatchingItemInPedestalOrEmptySlot(itemsIncoming),itemsIncoming))
            {
                //Two buckets match but cant be stacked since max stack size is 1
                //BUT if its a tank, its cooler then this
                if(itemsIncoming.getMaxStackSize() > 1)
                {
                    //If i did this right, slot limit should default to stack max size, or custom allowed
                    int allowed = getSlotSizeLimit();
                    if(getItemInPedestalOrEmptySlot().getCount() < allowed)
                    {
                        canAccept = (allowed - getItemInPedestalOrEmptySlot().getCount());
                    }
                }
            }
        }

        if(hasFilter())
        {
            Item filterInPed = this.getFilterInPedestal().getItem();
            if(filterInPed instanceof IPedestalFilter)
            {
                pedestalAccept = ((IPedestalFilter) filterInPed).canAcceptCount(getPedestal(), itemsIncoming,0);
            }
        }

        if((canAccept > pedestalAccept) && hasFilter())
        {
            canAccept = pedestalAccept;
        }

        return canAccept;
    }

    public int fluidAmountToAccept(Level worldIn, BlockPos posPedestal, FluidStack fluidIncoming)
    {
        int spaceForFluid = spaceForFluid();
        int canAccept = (fluidIncoming.getAmount()>spaceForFluid)?(spaceForFluid):(fluidIncoming.getAmount());

        if(hasFilter())
        {
            Item filterInPed = this.getFilterInPedestal().getItem();
            if(filterInPed instanceof IPedestalFilter)
            {
                int filterCount = ((IPedestalFilter) filterInPed).canAcceptCount(getPedestal(), ItemStack.EMPTY,1);
                if(filterCount>0)canAccept = filterCount;
            }
        }

        return canAccept;
    }

    public int energyAmountToAccept(Level worldIn, BlockPos posPedestal, int energyIncoming)
    {
        int spaceForEnergy = getEnergyCapacity()-getStoredEnergy();
        int canAccept = (energyIncoming>spaceForEnergy)?(spaceForEnergy):(energyIncoming);

        if(hasFilter())
        {
            Item filterInPed = this.getFilterInPedestal().getItem();
            if(filterInPed instanceof IPedestalFilter)
            {
                int filterCount = ((IPedestalFilter) filterInPed).canAcceptCount(getPedestal(), ItemStack.EMPTY,2);
                if(filterCount>0)canAccept = filterCount;
            }
        }

        return canAccept;
    }

    public int experienceAmountToAccept(Level worldIn, BlockPos posPedestal, int experienceIncoming)
    {
        int spaceForExperience = getExperienceCapacity()-getStoredExperience();
        int canAccept = (experienceIncoming>spaceForExperience)?(spaceForExperience):(experienceIncoming);

        if(hasFilter())
        {
            Item filterInPed = this.getFilterInPedestal().getItem();
            if(filterInPed instanceof IPedestalFilter)
            {
                int filterCount = ((IPedestalFilter) filterInPed).canAcceptCount(getPedestal(), ItemStack.EMPTY,3);
                if(filterCount>0)canAccept = filterCount;
            }
        }

        return canAccept;
    }

    public int dustAmountToAccept(Level worldIn, BlockPos posPedestal, int dustIncoming)
    {
        int spaceForDust = getDustCapacity()-getStoredDust().getDustAmount();
        int canAccept = (dustIncoming>spaceForDust)?(spaceForDust):(dustIncoming);

        if(hasFilter())
        {
            Item filterInPed = this.getFilterInPedestal().getItem();
            if(filterInPed instanceof IPedestalFilter)
            {
                int filterCount = ((IPedestalFilter) filterInPed).canAcceptCount(getPedestal(), ItemStack.EMPTY,4);
                if(filterCount>0)canAccept = filterCount;
            }
        }

        return canAccept;
    }

    public boolean hasFilter(BasePedestalBlockEntity pedestalSendingTo)
    {
        boolean returner = false;
        if(pedestalSendingTo.hasFilter())
        {
            Item filterInPedestal = pedestalSendingTo.getFilterInPedestal().getItem();
            if(filterInPedestal instanceof IPedestalFilter)
            {
                returner = true;
            }
        }

        return returner;
    }

    public static LazyOptional<IItemHandler> findItemHandlerPedestal(BasePedestalBlockEntity pedestal)
    {
        Level world = pedestal.getLevel();
        BlockPos pos = pedestal.getPos();
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
            if(cap.isPresent())
                return cap;
        }
        return LazyOptional.empty();
    }
     */
}
