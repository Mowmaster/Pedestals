package com.mowmaster.pedestals.Blocks.Pedestal;


import com.mowmaster.mowlib.Capabilities.Dust.CapabilityDust;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.Capabilities.Experience.CapabilityExperience;
import com.mowmaster.mowlib.Capabilities.Experience.IExperienceStorage;
import com.mowmaster.mowlib.MowLibUtils.*;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Augments.*;
import com.mowmaster.pedestals.Items.Filters.IPedestalFilter;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.IPedestalUpgrade;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.Registry.DeferredBlockEntityTypes;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;

import static com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock.*;

import com.mowmaster.pedestals.Registry.DeferredRegisterTileBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BasePedestalBlockEntity extends BlockEntity
{
    private LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandlerPedestal);
    private LazyOptional<IItemHandler> privateHandler = LazyOptional.of(this::createHandlerPedestalPrivate);
    private LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(this::createHandlerPedestalEnergy);
    private LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(this::createHandlerPedestalFluid);
    private LazyOptional<IExperienceStorage> experienceHandler = LazyOptional.of(this::createHandlerPedestalExperience);
    private LazyOptional<IDustHandler> dustHandler = LazyOptional.of(this::createDustHandler);
    private List<ItemStack> stacksList = new ArrayList<>();
    private MobEffectInstance storedPotionEffect = null;
    private int storedPotionEffectDuration = 0;
    private int storedEnergy = 0;
    private FluidStack storedFluid = FluidStack.EMPTY;
    private int storedExperience = 0;
    private DustMagic storedDust = DustMagic.EMPTY;
    private final List<BlockPos> storedLocations = new ArrayList<BlockPos>();
    private int storedValueForUpgrades = 0;
    private boolean showRenderRange = false;
    public BlockPos getPos() { return this.worldPosition; }
    private BasePedestalBlockEntity getPedestal() { return this; }
    private int maxRate = Integer.MAX_VALUE;


    public boolean getRenderRange(){return this.showRenderRange;}
    public void setRenderRange(boolean setRender){ this.showRenderRange = setRender; update();}

    public BasePedestalBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(DeferredBlockEntityTypes.PEDESTAL.get(), p_155229_, p_155230_);
    }

    public void update()
    {
        BlockState state = level.getBlockState(getPos());
        this.level.sendBlockUpdated(getPos(), state, state, 3);
        this.setChanged();
    }

    //9 slots, but only when it has the tank upgrade will we allow more then the first to be used.
    public IItemHandler createHandlerPedestal() {
        return new ItemStackHandler(27) {
            @Override
            protected void onLoad() {
                super.onLoad();
            }

            @Override
            protected void onContentsChanged(int slot) {
                update();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                //Run filter checks here(slot==0)?(true):(false)
                IPedestalFilter filter = getIPedestalFilter();
                if(filter == null)return true;
                return filter.canAcceptItems(getFilterInPedestal(),stack);
            }

            @Override
            public int getSlots() {
                //maybe return less if there is no tank upgrade???
                int baseSlots = PedestalConfig.COMMON.pedestal_baseItemStacks.get();
                int additionalSlots = getItemSlotIncreaseFromStorage();
                return baseSlots + additionalSlots;
            }

            @Override
            protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
                //Run filter checks here
                IPedestalFilter filter = getIPedestalFilter();
                if(filter == null)return super.getStackLimit(slot, stack);

                return filter.canAcceptCount(getPedestal(),stack,0);
            }

            @Override
            public int getSlotLimit(int slot) {

                //Hopefully never mess with this again
                //Amount of items allowed in the slot --- may use for bibliomania???
                return super.getSlotLimit(slot);
            }

            @Nonnull
            @Override
            public ItemStack getStackInSlot(int slot) {

                return super.getStackInSlot((slot>getSlots())?(0):(slot));
            }

            /*
                Inserts an ItemStack into the given slot and return the remainder. The ItemStack should not be modified in this function!
                Note: This behaviour is subtly different from IFluidHandler.fill(FluidStack, IFluidHandler.FluidAction)
                Params:
                    slot – Slot to insert into.
                    stack – ItemStack to insert. This must not be modified by the item handler.
                    simulate – If true, the insertion is only simulated
                Returns:
                    The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
                    May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
                    The returned ItemStack can be safely modified after.
            */
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return super.insertItem((slot>getSlots())?(0):(slot), stack, simulate);
            }

            /*
                Extracts an ItemStack from the given slot.
                The returned value must be empty if nothing is extracted,
                otherwise its stack size must be less than or equal to amount and ItemStack.getMaxStackSize().
                Params:
                    slot – Slot to extract from.
                    amount – Amount to extract (may be greater than the current stack's max limit)
                    simulate – If true, the extraction is only simulated
                Returns:
                    ItemStack extracted from the slot, must be empty if nothing can be extracted.
                    The returned ItemStack can be safely modified after, so item handlers should return a new or copied stack.
             */
            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {

                return super.extractItem((slot>getSlots())?(0):(slot), amount, simulate);
            }
        };
    }

    private IItemHandler createHandlerPedestalPrivate() {
        //going from 5 to 11 slots to future proof things
        return new ItemStackHandler(12) {

            @Override
            protected void onLoad() {
                if(getSlots()<12)
                {
                    for(int i = 0; i < getSlots(); ++i) {
                        stacksList.add(i,getStackInSlot(i));
                    }
                    setSize(12);
                    for(int j = 0;j<stacksList.size();j++) {
                        setStackInSlot(j, stacksList.get(j));
                    }
                }

                super.onLoad();
            }

            @Override
            protected void onContentsChanged(int slot) {
                if(!(stacksList.size()>0))
                {
                    update();
                }
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot == 0 && stack.getItem() instanceof IPedestalUpgrade && !hasCoin()) return true;
                //if (slot == 1 && stack.getItem().equals(Items.GLOWSTONE_DUST) && getLightBrightness()<15) return true;
                if (slot == 1 && stack.getItem().equals(Items.GLOWSTONE) && !hasLight()) return true;
                if (slot == 2 && stack.getItem() instanceof IPedestalFilter && !(stack.getItem().equals(DeferredRegisterItems.FILTER_BASE.get())) && !hasFilter()) return true;
                if (slot == 3 && stack.getItem().equals(Items.REDSTONE) && getRedstonePowerNeeded()<15) return true;
                if (slot == 4 && stack.getItem().equals(DeferredRegisterItems.AUGMENT_PEDESTAL_ROUNDROBIN.get()) && !hasRRobin()) return true;
                if (slot == 5 && stack.getItem().equals(DeferredRegisterItems.AUGMENT_PEDESTAL_RENDERDIFFUSER.get()) && !hasRenderAugment()) return true;
                if (slot == 6 && stack.getItem().equals(DeferredRegisterItems.AUGMENT_PEDESTAL_NOCOLLIDE.get()) && !hasNoCollide()) return true;
                if (slot == 7 && stack.getItem() instanceof AugmentTieredSpeed && canInsertAugmentSpeed(stack)) return true;
                if (slot == 8 && stack.getItem() instanceof AugmentTieredCapacity && canInsertAugmentCapacity(stack)) return true;
                if (slot == 9 && stack.getItem() instanceof AugmentTieredStorage && canInsertAugmentStorage(stack)) return true;
                if (slot == 10 && stack.getItem() instanceof AugmentTieredRange && canInsertAugmentRange(stack)) return true;
                if (slot == 11 && canInsertTool(stack)) return true;
                return false;
            }
        };
    }

    public IEnergyStorage createHandlerPedestalEnergy() {
        return new IEnergyStorage() {

            /*
            Adds energy to the storage. Returns quantity of energy that was accepted.
            Params:
                maxReceive – Maximum amount of energy to be inserted.
                simulate – If TRUE, the insertion will only be simulated.
            Returns:
                Amount of energy that was (or would have been, if simulated) accepted by the storage.
             */
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {

                if(simulate)
                {
                    IPedestalFilter filter = getIPedestalFilter();
                    int spaceAvailable = getMaxEnergyStored()-getEnergyStored();
                    if(filter == null)return (maxReceive>spaceAvailable)?(spaceAvailable):(maxReceive);
                    return filter.canAcceptCount(getPedestal(),ItemStack.EMPTY,2);
                }
                else
                {
                    int currentEnergy = getEnergyStored();
                    int incomingEnergy = maxReceive;
                    int spaceAvailable = getMaxEnergyStored()-currentEnergy;
                    int newEnergy = currentEnergy + ((maxReceive>spaceAvailable)?(spaceAvailable):(maxReceive));
                    storedEnergy = newEnergy;
                    update();
                    return (incomingEnergy>spaceAvailable)?(spaceAvailable):(incomingEnergy);
                }
            }

            /*
            Removes energy from the storage. Returns quantity of energy that was removed.
            Params:
                maxExtract – Maximum amount of energy to be extracted.
                simulate – If TRUE, the extraction will only be simulated.
            Returns:
                Amount of energy that was (or would have been, if simulated) extracted from the storage.
             */
            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {

                if(simulate)
                {
                    return (maxExtract>getEnergyStored())?(getEnergyStored()):(maxExtract);
                }
                else
                {
                    int currentEnergy = getEnergyStored();
                    int remaining = (maxExtract>getEnergyStored())?(0):(currentEnergy-maxExtract);
                    storedEnergy = remaining;
                    update();
                    return (maxExtract>currentEnergy)?(currentEnergy):(maxExtract);
                }
            }

            @Override
            public int getEnergyStored() {

                return storedEnergy;
            }

            @Override
            public int getMaxEnergyStored() {

                int baseStorage = PedestalConfig.COMMON.pedestal_baseEnergyStorage.get();
                int additionalStorage = getEnergyAmountIncreaseFromStorage();
                return baseStorage + additionalStorage;
            }

            @Override
            public boolean canExtract() {

                if(hasEnergy())return true;
                return false;
            }

            @Override
            public boolean canReceive() {
                if(hasSpaceForEnergy())
                {
                    IPedestalFilter filter = getIPedestalFilter();
                    if(filter == null)return true;
                    return filter.canAcceptItem(getPedestal(),ItemStack.EMPTY,2);
                }

                return false;
            }
        };
    }



    public IFluidHandler createHandlerPedestalFluid() {
        return new IFluidHandler() {

            @Nonnull
            @Override
            public FluidStack getFluidInTank(int i) {
                return storedFluid;
            }

            @Override
            public int getTanks() {
                //Technically we dont use the tanks thing, but we'll pretend and hope it doesnt break...
                return 1;
            }

            @Nonnull
            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {

                FluidStack stored = storedFluid.copy();
                if(stored.isFluidEqual(resource))
                {
                    int storedAmount = stored.getAmount();
                    int receivingAmount = resource.getAmount();
                    if(action.simulate())
                    {
                        if(receivingAmount>storedAmount)
                        {
                            return new FluidStack(resource.getFluid(),storedAmount,resource.getTag());
                        }
                        return resource;
                    }
                    else if(action.execute())
                    {
                        if(receivingAmount>storedAmount)
                        {
                            storedFluid = FluidStack.EMPTY;
                            update();
                            return new FluidStack(resource.getFluid(),storedAmount,resource.getTag());
                        }

                        FluidStack newStack = storedFluid.copy();
                        newStack.setAmount(storedAmount-receivingAmount);
                        storedFluid = newStack;
                        update();
                        return resource;
                    }
                }

                return FluidStack.EMPTY;
            }

            @Nonnull
            @Override
            public FluidStack drain(int i, FluidAction fluidAction) {

                FluidStack stored = storedFluid.copy();
                int storedAmount = stored.getAmount();
                int receivingAmount = i;
                if(fluidAction.simulate())
                {
                    if(receivingAmount>storedAmount)
                    {
                        return stored;
                    }
                    return new FluidStack(stored.getFluid(),receivingAmount,stored.getTag());
                }
                else if(fluidAction.execute())
                {
                    if(receivingAmount>storedAmount)
                    {
                        storedFluid = FluidStack.EMPTY;
                        update();
                        return stored;
                    }

                    FluidStack newStack = stored.copy();
                    newStack.setAmount(storedAmount-receivingAmount);
                    storedFluid = newStack;
                    update();
                    return new FluidStack(stored.getFluid(),receivingAmount,stored.getTag());
                }

                return FluidStack.EMPTY;
            }

            @Override
            public int getTankCapacity(int i)
            {
                int baseFluidStorage = PedestalConfig.COMMON.pedestal_baseFluidStorage.get();
                int additionalFluidStorage = getFluidAmountIncreaseFromStorage();
                return baseFluidStorage + additionalFluidStorage;
            }

            @Override
            public boolean isFluidValid(int i, @Nonnull FluidStack fluidStack) {
                if(hasFilter())
                {
                    IPedestalFilter filter = getIPedestalFilter();
                    Item item = fluidStack.getFluid().getBucket();
                    ItemStack incomingBucket = new ItemStack(item);
                    return filter.canAcceptItem(getPedestal(),incomingBucket,1);
                }
                else
                {
                    if(storedFluid.isEmpty())return true;
                    else if(storedFluid.isFluidEqual(fluidStack))return true;
                }

                return false;
            }

            @Override
            public int fill(FluidStack fluidStack, FluidAction fluidAction) {
                if(isFluidValid(0,fluidStack))
                {
                    FluidStack stored = storedFluid.copy();
                    FluidStack receivingFluid = fluidStack.copy();
                    int storedAmount = stored.getAmount();
                    int receivingAmount = receivingFluid.getAmount();
                    int canReceive;
                    IPedestalFilter filter = getIPedestalFilter();
                    if(filter == null){
                        canReceive = spaceForFluid();
                    }
                    else {
                        canReceive = filter.canAcceptCount(getPedestal(),ItemStack.EMPTY,1);
                    }

                    if(canReceive>0)
                    {
                        if(fluidAction.simulate())
                        {
                            if(receivingAmount>canReceive)return canReceive;
                            else return receivingAmount;
                        }
                        else if(fluidAction.execute())
                        {
                            if(receivingFluid.isEmpty())
                            {
                                storedFluid = FluidStack.EMPTY;
                            }
                            else if(receivingAmount>canReceive)
                            {
                                receivingFluid.setAmount(canReceive);
                                storedFluid = receivingFluid;
                                update();
                                return canReceive;
                            }
                            else
                            {
                                receivingFluid.setAmount(storedAmount+receivingAmount);
                                storedFluid = receivingFluid;
                                update();
                                return receivingAmount;
                            }
                        }
                    }
                }

                return 0;
            }
        };
    }

    public IExperienceStorage createHandlerPedestalExperience() {
        return new IExperienceStorage() {

            @Override
            public int receiveExperience(int maxReceive, boolean simulate) {
                if(simulate)
                {
                    IPedestalFilter filter = getIPedestalFilter();
                    int spaceAvailable = getMaxExperienceStored()-getExperienceStored();
                    if(filter == null)return (maxReceive>spaceAvailable)?(spaceAvailable):(maxReceive);
                    return filter.canAcceptCount(getPedestal(),ItemStack.EMPTY,3);
                }
                else
                {
                    int currentExperience = getExperienceStored();
                    int incomingExperience = maxReceive;
                    int spaceAvailable = getMaxExperienceStored()-currentExperience;
                    int newExperience = currentExperience + ((maxReceive>spaceAvailable)?(spaceAvailable):(maxReceive));
                    storedExperience = newExperience;
                    update();
                    return (incomingExperience>spaceAvailable)?(spaceAvailable):(incomingExperience);
                }
            }

            @Override
            public int extractExperience(int maxExtract, boolean simulate) {
                if(simulate)
                {
                    return (maxExtract>getExperienceStored())?(getExperienceStored()):(maxExtract);
                }
                else
                {
                    int currentExperience = getExperienceStored();
                    int remaining = (maxExtract>getExperienceStored())?(0):(currentExperience-maxExtract);
                    storedExperience = remaining;
                    update();
                    return (maxExtract>currentExperience)?(currentExperience):(maxExtract);
                }
            }

            @Override
            public int getExperienceStored() {
                return storedExperience;
            }

            @Override
            public int getMaxExperienceStored() {
                int convertLevelsToXp = MowLibXpUtils.getExpCountByLevel(PedestalConfig.COMMON.pedestal_baseXpStorage.get());
                //Conditioning out running the xp converter when theres not need to.
                int convertLevelsToXpAdditional = (getXpLevelAmountIncreaseFromStorage()>0)?(MowLibXpUtils.getExpCountByLevel(getXpLevelAmountIncreaseFromStorage())):(0);
                return convertLevelsToXp + convertLevelsToXpAdditional;//30 levels is default
            }

            @Override
            public boolean canExtract() {
                if(hasExperience())return true;
                return false;
            }

            @Override
            public boolean canReceive() {
                if(hasSpaceForExperience())
                {
                    IPedestalFilter filter = getIPedestalFilter();
                    if(filter == null)return true;
                    return filter.canAcceptItem(getPedestal(),ItemStack.EMPTY,3);
                }

                return false;
            }
        };
    }

    public IDustHandler createDustHandler() {
        return new IDustHandler() {
            protected void onContentsChanged()
            {
                update();
            }

            @Override
            public int getTanks() {
                return 1;
            }

            @NotNull
            @Override
            public DustMagic getDustMagicInTank(int tank) {
                return storedDust;
            }

            @Override
            public int getTankCapacity(int tank)
            {
                int baseStorage = PedestalConfig.COMMON.pedestal_baseDustStorage.get();
                int additionalStorage = getDustAmountIncreaseFromStorage();
                return baseStorage + additionalStorage;
            }

            @Override
            public boolean isDustValid(int tank, @NotNull DustMagic dustIn) {
                IPedestalFilter filter = getIPedestalFilter();
                if(filter == null)return storedDust.isDustEqualOrEmpty(dustIn);
                return filter.canAcceptItem(getPedestal(),ItemStack.EMPTY,4);
            }

            @Override
            public int fill(DustMagic dust, DustAction action) {
                if (dust.isEmpty() || !isDustValid(0,dust))
                {
                    return 0;
                }
                if (action.simulate())
                {
                    if (storedDust.isEmpty())
                    {
                        return Math.min(getTankCapacity(0), dust.getDustAmount());
                    }
                    if (!storedDust.isDustEqual(dust))
                    {
                        return 0;
                    }
                    return Math.min(getTankCapacity(0) - storedDust.getDustAmount(), dust.getDustAmount());
                }
                if (storedDust.isEmpty())
                {
                    storedDust = new DustMagic(dust.getDustColor(), Math.min(getTankCapacity(0), dust.getDustAmount()));
                    onContentsChanged();
                    return storedDust.getDustAmount();
                }
                if (!storedDust.isDustEqual(dust))
                {
                    return 0;
                }
                int filled = getTankCapacity(0) - storedDust.getDustAmount();

                if (dust.getDustAmount() < filled)
                {
                    storedDust.grow(dust.getDustAmount());
                    filled = dust.getDustAmount();
                }
                else
                {
                    storedDust.setDustAmount(getTankCapacity(0));
                }
                if (filled > 0)
                    onContentsChanged();
                return filled;
            }

            @NotNull
            @Override
            public DustMagic drain(DustMagic dust, DustAction action) {
                if (dust.isEmpty() || !dust.isDustEqual(storedDust))
                {
                    return new DustMagic(-1, 0);
                }
                return drain(dust.getDustAmount(), action);
            }

            @NotNull
            @Override
            public DustMagic drain(int maxDrain, DustAction action) {
                int drained = maxDrain;
                if (storedDust.getDustAmount() < drained)
                {
                    drained = storedDust.getDustAmount();
                }
                DustMagic magic = new DustMagic(storedDust.getDustColor(), drained);
                if (action.execute() && drained > 0)
                {
                    if(drained>=storedDust.getDustAmount())
                    {
                        storedDust.setDustAmount(0);
                        storedDust.setDustColor(-1);
                    }
                    else
                    {
                        storedDust.shrink(drained);
                    }
                    onContentsChanged();
                }
                return magic;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        if ((cap == CapabilityEnergy.ENERGY)) {
            return energyHandler.cast();
        }
        if ((cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)) {
            return fluidHandler.cast();
        }
        if ((cap == CapabilityExperience.EXPERIENCE)) {
            return experienceHandler.cast();
        }
        if ((cap == CapabilityDust.DUST_HANDLER)) {
            return dustHandler.cast();
        }
        return super.getCapability(cap, side);
    }




    public void dropInventoryItems(Level worldIn, BlockPos pos) {
        IItemHandler h = handler.orElse(null);
        MowLibItemUtils.dropInventoryItems(worldIn,pos,h);
    }

    public void dropInventoryItemsPrivate(Level worldIn, BlockPos pos) {
        IItemHandler ph = privateHandler.orElse(null);
        MowLibItemUtils.dropInventoryItems(worldIn,pos,ph);
    }

    public void dropLiquidsInWorld(Level worldIn, BlockPos pos) {
        IFluidHandler fluids = fluidHandler.orElse(null);
        MowLibFluidUtils.dropLiquidsInWorld(worldIn,pos,fluids);
    }

    public void removeEnergyFromBrokenPedestal(Level worldIn, BlockPos pos) {
        IEnergyStorage energy = energyHandler.orElse(null);
        MowLibEnergyUtils.removeEnergy(worldIn,pos,energy);
    }

    public void dropXPInWorld(Level worldIn, BlockPos pos) {
        IExperienceStorage experience = experienceHandler.orElse(null);
        MowLibXpUtils.dropXPInWorld(worldIn,pos,experience);
    }

    public void dropDustInWorld(Level worldIn, BlockPos pos) {
        IDustHandler dust = dustHandler.orElse(null);
        if(!dust.getDustMagicInTank(0).isEmpty())
        {
            ItemStack toDrop = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_DUST.get());
            DustMagic.setDustMagicInStack(toDrop,storedDust);
            MowLibItemUtils.spawnItemStack(worldIn,pos.getX(),pos.getY(),pos.getZ(),toDrop);
        }
    }

    /*============================================================================
    ==============================================================================
    =========================    STORED VALUE START    ===========================
    ==============================================================================
    ============================================================================*/

    public int getStoredValueForUpgrades()
    {
        return storedValueForUpgrades;
    }

    public void setStoredValueForUpgrades(int value)
    {
        storedValueForUpgrades = value;
        update();
    }

    /*============================================================================
    ==============================================================================
    =========================     STORED VALUE END     ===========================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================     LINKING START    =============================
    ==============================================================================
    ============================================================================*/

    public int getNumberOfStoredLocations() {return storedLocations.size();}

    public boolean storeNewLocation(BlockPos pos)
    {
        boolean returner = false;
        if(getNumberOfStoredLocations() < 8)
        {
            storedLocations.add(pos);
            returner=true;
        }
        update();
        return returner;
    }

    public BlockPos getStoredPositionAt(int index)
    {
        BlockPos sendToPos = getPos();
        if(index<getNumberOfStoredLocations())
        {
            sendToPos = storedLocations.get(index);
        }

        return sendToPos;
    }

    public boolean removeLocation(BlockPos pos)
    {
        boolean returner = false;
        if(getNumberOfStoredLocations() >= 1)
        {
            storedLocations.remove(pos);
            returner=true;
        }
        update();

        return returner;
    }

    public boolean isAlreadyLinked(BlockPos pos) {
        return storedLocations.contains(pos);
    }

    public List<BlockPos> getLocationList()
    {
        return storedLocations;
    }

    public int getLinkingRange()
    {
        int range = 8;
        return  range + getRangeIncrease();
    }

    public boolean isPedestalInRange(BasePedestalBlockEntity pedestalCurrent, BlockPos pedestalToBeLinked)
    {
        int range = pedestalCurrent.getLinkingRange();
        int x = pedestalToBeLinked.getX();
        int y = pedestalToBeLinked.getY();
        int z = pedestalToBeLinked.getZ();
        int x1 = pedestalCurrent.getPos().getX();
        int y1 = pedestalCurrent.getPos().getY();
        int z1 = pedestalCurrent.getPos().getZ();
        int xF = Math.abs(Math.subtractExact(x,x1));
        int yF = Math.abs(Math.subtractExact(y,y1));
        int zF = Math.abs(Math.subtractExact(z,z1));

        if(xF>range || yF>range || zF>range)
        {
            return false;
        }
        else return true;
    }

    /*============================================================================
    ==============================================================================
    ===========================      LINKING END     =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================     ITEM START       =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasItem()
    {
        IItemHandler h = handler.orElse(null);
        int firstPartialOrNonEmptySlot = 0;
        if(h.getSlots()>1)
        {
            for(int i=0;i<h.getSlots();i++)
            {
                ItemStack stackInSlot = h.getStackInSlot(i);
                if(stackInSlot.getCount() < stackInSlot.getMaxStackSize() || stackInSlot.isEmpty())
                {
                    firstPartialOrNonEmptySlot = i;
                    break;
                }
            }
        }

        if(h.getStackInSlot(firstPartialOrNonEmptySlot).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public boolean hasItemFirst()
    {
        IItemHandler h = handler.orElse(null);
        int firstNonEmptySlot = 0;
        if(h.getSlots()>1)
        {
            for(int i=0;i<h.getSlots();i++)
            {
                if(!h.getStackInSlot(i).isEmpty())
                {
                    firstNonEmptySlot = i;
                    break;
                }
            }
        }

        if(h.getStackInSlot(firstNonEmptySlot).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public boolean hasSpaceForItem(ItemStack stackToMatch)
    {
        IItemHandler h = handler.orElse(null);
        if(h.getSlots()>1)
        {
            for(int i=0;i<h.getSlots();i++)
            {
                ItemStack stackInSlot = h.getStackInSlot(i);
                if(((stackInSlot.getCount() < stackInSlot.getMaxStackSize()) && ItemHandlerHelper.canItemStacksStack(stackInSlot,stackToMatch)) || stackInSlot.isEmpty())
                {
                    return true;
                }
            }
        }

        return false;
    }

    public ItemStack getItemInPedestal()
    {
        IItemHandler h = handler.orElse(null);
        if(hasItemFirst())
        {
            int firstNonEmptySlot = 0;
            if(h.getSlots()>1)
            {
                for(int i=0;i<h.getSlots();i++)
                {
                    ItemStack stackInSlot = h.getStackInSlot(i);
                    if(!stackInSlot.isEmpty())
                    {
                        firstNonEmptySlot = i;
                        break;
                    }
                }
            }
            return h.getStackInSlot(firstNonEmptySlot);
        }
        else return ItemStack.EMPTY;
    }

    public ItemStack getItemInPedestalOrEmptySlot()
    {
        IItemHandler h = handler.orElse(null);
        if(hasItemFirst())
        {
            int firstNonEmptySlot = 0;
            if(h.getSlots()>1)
            {
                for(int i=0;i<h.getSlots();i++)
                {
                    ItemStack stackInSlot = h.getStackInSlot(i);
                    if((stackInSlot.getMaxStackSize() > stackInSlot.getCount()) || stackInSlot.isEmpty())
                    {
                        firstNonEmptySlot = i;
                        break;
                    }
                }
            }
            return h.getStackInSlot(firstNonEmptySlot);
        }
        else return ItemStack.EMPTY;
    }

    public ItemStack getMatchingItemInPedestalOrEmptySlot(ItemStack stackToMatch)
    {
        IItemHandler h = handler.orElse(null);
        if(hasItemFirst())
        {
            int firstNonEmptySlot = 0;
            if(h.getSlots()>1)
            {
                for(int i=0;i<h.getSlots();i++)
                {
                    ItemStack stackInSlot = h.getStackInSlot(i);
                    if(((stackInSlot.getMaxStackSize() > stackInSlot.getCount())  && ItemHandlerHelper.canItemStacksStack(stackInSlot,stackToMatch))  || stackInSlot.isEmpty())
                    {
                        firstNonEmptySlot = i;
                        break;
                    }
                }
            }
            return h.getStackInSlot(firstNonEmptySlot);
        }
        else return ItemStack.EMPTY;
    }

    public ItemStack getItemInPedestalFirst()
    {
        IItemHandler h = handler.orElse(null);
        if(hasItemFirst())
        {
            int firstNonEmptySlot = 0;
            if(h.getSlots()>1)
            {
                for(int i=0;i<h.getSlots();i++)
                {
                    if(!h.getStackInSlot(i).isEmpty())
                    {
                        firstNonEmptySlot = i;
                        break;
                    }
                }
            }
            return h.getStackInSlot(firstNonEmptySlot);
        }
        else return ItemStack.EMPTY;
    }

    public List<ItemStack> getItemStacks()
    {
        IItemHandler h = handler.orElse(null);
        List<ItemStack> listed = new ArrayList<>();
        if(h.getSlots()>1)
        {
            for(int i=0;i<h.getSlots();i++)
            {
                if(!h.getStackInSlot(i).isEmpty())
                {
                    listed.add(h.getStackInSlot(i));
                }
            }
        }
        else
        {
            listed.add(h.getStackInSlot(0));
        }

        return listed;
    }

    public ItemStack getItemInPedestal(int slot)
    {
        IItemHandler h = handler.orElse(null);
        if(hasItem())
        {
            int firstNonEmptySlot = 0;
            if(h.getSlots()>1)
            {
                firstNonEmptySlot = slot;
            }
            return h.getStackInSlot(firstNonEmptySlot);
        }
        else return ItemStack.EMPTY;
    }

    public int countAllowedForInsert(ItemStack stackIn) {
        if(hasItem())
        {
            if(stackIn.getItem().equals(getItemInPedestal().getItem()))
            {
                int allowedInsertCount = stackIn.getMaxStackSize() - getItemInPedestal().getCount();
                return allowedInsertCount;
            }
            else return 0;
        }
        else return stackIn.getMaxStackSize();

    }

    public ItemStack removeItem(int numToRemove, boolean simulate) {
        IItemHandler h = handler.orElse(null);
        int firstNonEmptySlot = 0;
        if(h.getSlots()>1)
        {
            int endFirst = h.getSlots()-1;
            for(int i=endFirst;i>=0;i--)
            {
                if(!h.getStackInSlot(i).isEmpty())
                {
                    firstNonEmptySlot = i;
                    break;
                }
            }
        }
        ItemStack stack = h.extractItem(firstNonEmptySlot,numToRemove,simulate);
        //update();

        return stack;
    }

    public ItemStack removeItem(boolean simulate) {
        IItemHandler h = handler.orElse(null);
        int firstNonEmptySlot = 0;
        if(h.getSlots()>1)
        {
            int endFirst = h.getSlots()-1;
            for(int i=endFirst;i>=0;i--)
            {
                if(!h.getStackInSlot(i).isEmpty())
                {
                    firstNonEmptySlot = i;
                    break;
                }
            }
        }
        ItemStack stack = h.extractItem(firstNonEmptySlot,h.getStackInSlot(firstNonEmptySlot).getCount(),simulate);
        //update();

        return stack;
    }

    //If resulting insert stack is empty it means the full stack was inserted
    public boolean addItem(ItemStack itemFromBlock, boolean simulate)
    {
        return addItemStack(itemFromBlock, simulate).isEmpty();
    }

    //Return result not inserted, if all inserted return empty stack
    public ItemStack addItemStack(ItemStack itemFromBlock, boolean simulate)
    {
        IItemHandler h = handler.orElse(null);
        int firstEmptyorMatchingSlot = 0;
        if(h.getSlots()>1)
        {
            for(int i=0;i<h.getSlots();i++)
            {
                ItemStack stackInSlot = h.getStackInSlot(i);
                if(stackInSlot.getCount() < stackInSlot.getMaxStackSize() && ItemHandlerHelper.canItemStacksStack(stackInSlot,itemFromBlock))
                {
                    firstEmptyorMatchingSlot = i;
                    break;
                }
                else if(h.getStackInSlot(i).isEmpty())
                {
                    firstEmptyorMatchingSlot = i;
                    break;
                }
            }
        }


        if(h.isItemValid(firstEmptyorMatchingSlot,itemFromBlock))
        {
            if(hasSpaceForItem(itemFromBlock) && ItemHandlerHelper.canItemStacksStack(h.getStackInSlot(firstEmptyorMatchingSlot),itemFromBlock))
            {
                ItemStack returner = h.insertItem(firstEmptyorMatchingSlot, itemFromBlock.copy(), simulate);
                if(!simulate)update();
                return returner;
            }
            else
            {
                ItemStack returner = h.insertItem(firstEmptyorMatchingSlot, itemFromBlock.copy(), simulate);
                if(!simulate)update();
                return returner;
            }
        }

        return itemFromBlock;
    }

    public int getItemTransferRate()
    {
        int itemRate = PedestalConfig.COMMON.pedestal_baseItemTransferRate.get();
        int getRateIncrease = getItemTransferRateIncreaseFromCapacity();

        return  itemRate + getRateIncrease;
    }

    public int getSlotSizeLimit()
    {
        IItemHandler h = handler.orElse(null);
        int firstNonEmptySlot = 0;
        if(h.getSlots()>1)
        {
            for(int i=0;i<h.getSlots();i++)
            {
                if(!h.getStackInSlot(i).isEmpty())
                {
                    firstNonEmptySlot = i;
                    break;
                }
            }
        }
        return (h != null)?(h.getSlotLimit(firstNonEmptySlot)):(0);
    }

    public int getSlotSizeLimit(int slot)
    {
        IItemHandler h = handler.orElse(null);
        int firstNonEmptySlot = 0;
        if(h.getSlots()>1)
        {
            firstNonEmptySlot = slot;
        }
        return (h != null)?(h.getSlotLimit(firstNonEmptySlot)):(0);
    }

    public void collideWithPedestal(Level world, BasePedestalBlockEntity pedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(!world.isClientSide) {
            if(pedestal.hasCoin())
            {
                Item coinInPed = pedestal.getCoinOnPedestal().getItem();
                if(coinInPed instanceof IPedestalUpgrade upgrade)
                {
                    upgrade.actionOnCollideWithBlock(pedestal, entityIn);
                }
            }
        }
    }

    /*============================================================================
    ==============================================================================
    ===========================      ITEM END        =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================    FLUID  START      =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasFluid()
    {
        IFluidHandler h = fluidHandler.orElse(null);
        if(!h.getFluidInTank(0).isEmpty())return true;
        return false;
    }

    public FluidStack getStoredFluid()
    {
        IFluidHandler h = fluidHandler.orElse(null);
        if(!h.getFluidInTank(0).isEmpty())return h.getFluidInTank(0);
        return FluidStack.EMPTY;
    }

    public int getFluidCapacity()
    {
        IFluidHandler h = fluidHandler.orElse(null);
        return h.getTankCapacity(0);
    }

    public int spaceForFluid()
    {
        return getFluidCapacity()-getStoredFluid().getAmount();
    }

    public boolean canAcceptFluid(FluidStack fluidStackIn)
    {
        IFluidHandler h = fluidHandler.orElse(null);
        return h.isFluidValid(0,fluidStackIn);
    }

    public FluidStack removeFluid(FluidStack fluidToRemove, IFluidHandler.FluidAction action)
    {
        IFluidHandler h = fluidHandler.orElse(null);
        return h.drain(fluidToRemove,action);
    }

    public FluidStack removeFluid(int fluidAmountToRemove, IFluidHandler.FluidAction action)
    {
        IFluidHandler h = fluidHandler.orElse(null);
        return h.drain(new FluidStack(getStoredFluid().getFluid(),fluidAmountToRemove,getStoredFluid().getTag()),action);
    }

    public int addFluid(FluidStack fluidStackIn, IFluidHandler.FluidAction fluidAction)
    {
        IFluidHandler h = fluidHandler.orElse(null);
        return h.fill(fluidStackIn,fluidAction);
    }


    public int getFluidTransferRate()
    {
        int fluidTransferRate = PedestalConfig.COMMON.pedestal_baseFluidTransferRate.get();
        int getFluidRateIncrease = getFluidTransferRateIncreaseFromCapacity();

        return  fluidTransferRate + getFluidRateIncrease;
    }


    /*============================================================================
    ==============================================================================
    ===========================     FLUID  END       =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================    ENERGY START      =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasEnergy()
    {
        IEnergyStorage h = energyHandler.orElse(null);
        if(h.getEnergyStored()>0)return true;

        return false;
    }

    public boolean hasSpaceForEnergy()
    {
        return getEnergyCapacity() - getStoredEnergy() > 0;
    }

    public int getEnergyCapacity()
    {
        IEnergyStorage h = energyHandler.orElse(null);
        return h.getMaxEnergyStored();
    }

    public int getStoredEnergy()
    {
        IEnergyStorage h = energyHandler.orElse(null);
        return h.getEnergyStored();
    }

    public int addEnergy(int amountIn, boolean simulate)
    {
        IEnergyStorage h = energyHandler.orElse(null);
        return h.receiveEnergy(amountIn,simulate);
    }

    public int removeEnergy(int amountOut, boolean simulate)
    {
        IEnergyStorage h = energyHandler.orElse(null);
        return h.extractEnergy(amountOut,simulate);
    }

    public boolean canAcceptEnergy()
    {
        IEnergyStorage h = energyHandler.orElse(null);
        return h.canReceive();
    }

    public boolean canSendEnergy()
    {
        IEnergyStorage h = energyHandler.orElse(null);
        return h.canExtract();
    }

    public int getEnergyTransferRate()
    {
        //im assuming # = rf value???
        int energyTransferRate = PedestalConfig.COMMON.pedestal_baseEnergyTransferRate.get();
        int energyTransferRateIncrease = getEnergyTransferRateIncreaseFromCapacity();

        return  energyTransferRate + energyTransferRateIncrease;
    }

    /*============================================================================
    ==============================================================================
    ===========================     ENERGY END       =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================  EXPERIENCE START    =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasExperience()
    {
        IExperienceStorage h = experienceHandler.orElse(null);
        if(h.getExperienceStored()>0)return true;

        return false;
    }

    public boolean hasSpaceForExperience()
    {
        return getExperienceCapacity() - getStoredExperience() > 0;
    }

    public int getExperienceCapacity()
    {
        IExperienceStorage h = experienceHandler.orElse(null);
        return h.getMaxExperienceStored();
    }

    public int getStoredExperience()
    {
        IExperienceStorage h = experienceHandler.orElse(null);
        return h.getExperienceStored();
    }

    public int addExperience(int amountIn, boolean simulate)
    {
        IExperienceStorage h = experienceHandler.orElse(null);
        return h.receiveExperience(amountIn,simulate);
    }

    public int removeExperience(int amountOut, boolean simulate)
    {
        IExperienceStorage h = experienceHandler.orElse(null);
        return h.extractExperience(amountOut,simulate);
    }

    public boolean canAcceptExperience()
    {
        IExperienceStorage h = experienceHandler.orElse(null);
        return h.canReceive();
    }

    public boolean canSendExperience()
    {
        IExperienceStorage h = experienceHandler.orElse(null);
        return h.canExtract();
    }

    public int getExperienceTransferRate()
    {
        //im assuming # = rf value???
        int baseValue = PedestalConfig.COMMON.pedestal_baseXpTransferRate.get();
        int experienceTransferRateConverted = MowLibXpUtils.getExpCountByLevel(baseValue);

        return  (getXpTransferRateIncreaseFromCapacity()>0)?(MowLibXpUtils.getExpCountByLevel(getXpTransferRateIncreaseFromCapacity()+baseValue)):(experienceTransferRateConverted);
    }

    /*============================================================================
    ==============================================================================
    ===========================   EXPERIENCE END     =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================    DUST   START      =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasDust()
    {
        IDustHandler h = dustHandler.orElse(null);
        if(!h.getDustMagicInTank(0).isEmpty())return true;
        return false;
    }

    public DustMagic getStoredDust()
    {
        IDustHandler h = dustHandler.orElse(null);
        if(!h.getDustMagicInTank(0).isEmpty())return h.getDustMagicInTank(0);
        return DustMagic.EMPTY;
    }

    public int getDustCapacity()
    {
        IDustHandler h = dustHandler.orElse(null);
        return h.getTankCapacity(0);
    }

    public int spaceForDust()
    {
        return getDustCapacity()-getStoredDust().getDustAmount();
    }

    public boolean canAcceptDust(DustMagic dustMagicIn)
    {
        IDustHandler h = dustHandler.orElse(null);
        return h.isDustValid(0,dustMagicIn);
    }

    public DustMagic removeDust(DustMagic dustMagicToRemove, IDustHandler.DustAction action)
    {
        IDustHandler h = dustHandler.orElse(null);
        update();
        return h.drain(dustMagicToRemove,action);
    }

    public DustMagic removeDust(int dustAmountToRemove, IDustHandler.DustAction action)
    {
        IDustHandler h = dustHandler.orElse(null);
        update();
        return h.drain(new DustMagic(getStoredDust().getDustColor(),dustAmountToRemove),action);
    }

    public int addDust(DustMagic dustMagicIn, IDustHandler.DustAction action)
    {
        IDustHandler h = dustHandler.orElse(null);
        update();
        return h.fill(dustMagicIn,action);
    }

    public int getDustTransferRate()
    {
        //im assuming # = rf value???
        int baseValue = PedestalConfig.COMMON.pedestal_baseDustTransferRate.get();
        return  (getDustTransferRateIncreaseFromCapacity()>0)?(MowLibXpUtils.getExpCountByLevel(getDustTransferRateIncreaseFromCapacity()+baseValue)):(baseValue);
    }

    /*============================================================================
    ==============================================================================
    ===========================     DUST   END       =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================      COIN START      =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasCoin()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(0).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public ItemStack getCoinOnPedestal()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasCoin())
        {
            return ph.getStackInSlot(0);
        }
        else return ItemStack.EMPTY;
    }

    public ItemStack removeCoin() {
        IItemHandler ph = privateHandler.orElse(null);
        ItemStack stack = ph.getStackInSlot(0);
        ph.extractItem(0,stack.getCount(),false);
        //update();

        return stack;
    }

    public boolean addCoin(Player player, ItemStack coinFromBlock, boolean simulate)
    {
        if(hasCoin())
        {
            return false;
        }
        else
        {
            IItemHandler ph = privateHandler.orElse(null);
            ItemStack coinItem = coinFromBlock.copy();
            coinItem.setCount(1);
            if(!hasCoin() && ph.isItemValid(0,coinItem))
            {
                if(!simulate)
                {
                    //((IPedestalUpgrade)coinFromBlock.getItem()).setPlayerOnCoin(coinFromBlock,player);
                    ph.insertItem(0,coinItem,false);
                }
                return true;
            }
            else return false;
        }
    }

    public void actionOnNeighborBelowChange(BlockPos belowBlock) {

        if(getCoinOnPedestal().getItem() instanceof IPedestalUpgrade upgrade)
        {
            upgrade.actionOnNeighborBelowChange(getPedestal(),belowBlock);
        }
    }

    public void actionOnRemovedFromPedestal(int type) {
        // 0 = Dropped
        // 1 = Removed

        if(getCoinOnPedestal().getItem() instanceof IPedestalUpgrade upgrade)
        {
            upgrade.actionOnRemovedFromPedestal(getPedestal(), getCoinOnPedestal());
        }
    }

    /*============================================================================
    ==============================================================================
    ===========================       COIN END       =============================
    ==============================================================================
    ============================================================================*/

    /*============================================================================
    ==============================================================================
    ===========================     SPEED START      =============================
    ==============================================================================
    ============================================================================*/

    public boolean addSpeed(ItemStack speedAugment)
    {
        if(speedAugment.getItem() instanceof AugmentTieredSpeed speedStack)
        {
            IItemHandler ph = privateHandler.orElse(null);
            ItemStack itemFromBlock = speedAugment.copy();
            itemFromBlock.setCount(1);
            if(getSpeed() < speedStack.getAllowedInsertAmount(speedAugment.getItem()))
            {
                ph.insertItem(7,itemFromBlock,false);
                //update();
                return true;
            }
            else return false;
        }
        else return false;
    }

    public ItemStack removeSpeed(int count)
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasSpeed())
        {
            return ph.extractItem(7,count,false);
        }
        else return ItemStack.EMPTY;
    }

    public ItemStack removeAllSpeed()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasSpeed())
        {
            //update();
            return ph.extractItem(7,ph.getStackInSlot(7).getCount(),false);
        }
        else return ItemStack.EMPTY;
    }

    public boolean hasSpeed()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(7).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public int getSpeed()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(7).getCount();
    }

    public ItemStack getSpeedStack()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(7);
    }

    public boolean canInsertAugmentSpeed(ItemStack speedAugment)
    {
        if(speedAugment.getItem() instanceof AugmentTieredSpeed speedStack)
        {
            //Check to see if any can be insert
            if(speedStack.getAllowedInsertAmount(speedAugment.getItem())<=0) return false;

            if(hasSpeed())
            {
                //Check to see if stacks to be insert match
                if(!speedAugment.getItem().equals(getSpeedStack().getItem()))return false;
                //Check to see if more can be insert
                if(getSpeed() >= speedStack.getAllowedInsertAmount(speedAugment.getItem())) return false;

                return true;
            }
            else return true;
        }

        return false;
    }

    public int getTicksReduced()
    {
        ItemStack augmentSpeed = getSpeedStack();
        if(augmentSpeed.getItem() instanceof AugmentTieredSpeed speedStack)
        {
            return speedStack.getTicksReducedPerItem(augmentSpeed.getItem()) * augmentSpeed.getCount();
        }

        return 0;
    }

    /*============================================================================
    ==============================================================================
    ===========================      SPEED END       =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================    CAPACITY START    =============================
    ==============================================================================
    ============================================================================*/

    public boolean addCapacity(ItemStack capacityAugment)
    {
        if(capacityAugment.getItem() instanceof AugmentTieredCapacity capacityStack)
        {
            IItemHandler ph = privateHandler.orElse(null);
            ItemStack itemFromBlock = capacityAugment.copy();
            itemFromBlock.setCount(1);
            if(getCapacity() < capacityStack.getAllowedInsertAmount(capacityAugment.getItem()))
            {
                ph.insertItem(8,itemFromBlock,false);
                //update();
                return true;
            }
            else return false;
        }
        else return false;
    }

    public ItemStack removeCapacity(int count)
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasCapacity())
        {
            return ph.extractItem(8,count,false);
        }
        else return ItemStack.EMPTY;
    }

    public ItemStack removeAllCapacity()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasCapacity())
        {
            //update();
            return ph.extractItem(8,ph.getStackInSlot(8).getCount(),false);
        }
        else return ItemStack.EMPTY;
    }

    public boolean hasCapacity()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(8).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public int getCapacity()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(8).getCount();
    }

    public ItemStack getCapacityStack()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(8);
    }

    public boolean canInsertAugmentCapacity(ItemStack capacityAugment)
    {
        if(capacityAugment.getItem() instanceof AugmentTieredCapacity capacityStack)
        {
            //Check to see if any can be insert
            if(capacityStack.getAllowedInsertAmount(capacityAugment.getItem())<=0) return false;

            if(hasCapacity())
            {
                //Check to see if stacks to be insert match
                if(!capacityAugment.getItem().equals(getCapacityStack().getItem()))return false;
                //Check to see if more can be insert
                if(getCapacity() >= capacityStack.getAllowedInsertAmount(capacityAugment.getItem())) return false;

                return true;
            }
            else return true;
        }

        return false;
    }

    public int getItemTransferRateIncreaseFromCapacity()
    {
        ItemStack augmentCapacity = getCapacityStack();
        if(augmentCapacity.getItem() instanceof AugmentTieredCapacity capacityStack)
        {
            return capacityStack.getAdditionalItemTransferRatePerItem(augmentCapacity.getItem()) * augmentCapacity.getCount();
        }

        return 0;
    }

    public int getFluidTransferRateIncreaseFromCapacity()
    {
        ItemStack augmentCapacity = getCapacityStack();
        if(augmentCapacity.getItem() instanceof AugmentTieredCapacity capacityStack)
        {
            return capacityStack.getAdditionalFluidTransferRatePerItem(augmentCapacity.getItem()) * augmentCapacity.getCount();
        }

        return 0;
    }

    public int getEnergyTransferRateIncreaseFromCapacity()
    {
        ItemStack augmentCapacity = getCapacityStack();
        if(augmentCapacity.getItem() instanceof AugmentTieredCapacity capacityStack)
        {
            return capacityStack.getAdditionalEnergyTransferRatePerItem(augmentCapacity.getItem()) * augmentCapacity.getCount();
        }

        return 0;
    }

    public int getXpTransferRateIncreaseFromCapacity()
    {
        ItemStack augmentCapacity = getCapacityStack();
        if(augmentCapacity.getItem() instanceof AugmentTieredCapacity capacityStack)
        {
            return capacityStack.getAdditionalXpTransferRatePerItem(augmentCapacity.getItem()) * augmentCapacity.getCount();
        }

        return 0;
    }

    public int getDustTransferRateIncreaseFromCapacity()
    {
        ItemStack augmentCapacity = getCapacityStack();
        if(augmentCapacity.getItem() instanceof AugmentTieredCapacity capacityStack)
        {
            return capacityStack.getAdditionalDustTransferRatePerItem(augmentCapacity.getItem()) * augmentCapacity.getCount();
        }

        return 0;
    }

    /*============================================================================
    ==============================================================================
    ===========================     CAPACITY END     =============================
    ==============================================================================
    ============================================================================*/


    /*============================================================================
    ==============================================================================
    ===========================    STORAGE  START    =============================
    ==============================================================================
    ============================================================================*/

    public boolean addStorage(ItemStack storageAugment)
    {
        if(storageAugment.getItem() instanceof AugmentTieredStorage storageStack)
        {
            IItemHandler ph = privateHandler.orElse(null);
            ItemStack itemFromBlock = storageAugment.copy();
            itemFromBlock.setCount(1);
            if(getStorage() < storageStack.getAllowedInsertAmount(storageAugment.getItem()))
            {
                ph.insertItem(9,itemFromBlock,false);
                //update();
                return true;
            }
            else return false;
        }
        else return false;
    }

    public ItemStack removeStorage(int count)
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasStorage())
        {
            return ph.extractItem(9,count,false);
        }
        else return ItemStack.EMPTY;
    }

    public ItemStack removeAllStorage()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasStorage())
        {
            //update();
            return ph.extractItem(9,ph.getStackInSlot(9).getCount(),false);
        }
        else return ItemStack.EMPTY;
    }

    public boolean hasStorage()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(9).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public int getStorage()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(9).getCount();
    }

    public ItemStack getStorageStack()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(9);
    }

    public boolean canInsertAugmentStorage(ItemStack storageAugment)
    {
        if(storageAugment.getItem() instanceof AugmentTieredStorage storageStack)
        {
            //Check to see if any can be insert
            if(storageStack.getAllowedInsertAmount(storageAugment.getItem())<=0) return false;

            if(hasStorage())
            {
                //Check to see if stacks to be insert match
                if(!storageAugment.getItem().equals(getStorageStack().getItem()))return false;
                //Check to see if more can be insert
                if(getStorage() >= storageStack.getAllowedInsertAmount(storageAugment.getItem())) return false;

                return true;
            }
            else return true;
        }

        return false;
    }

    public int getItemSlotIncreaseFromStorage()
    {
        ItemStack augmentStorage = getStorageStack();
        if(augmentStorage.getItem() instanceof AugmentTieredStorage storageStack)
        {
            return storageStack.getAdditionalItemStoragePerItem(augmentStorage.getItem()) * augmentStorage.getCount();
        }

        return 0;
    }

    public int getFluidAmountIncreaseFromStorage()
    {
        ItemStack augmentStorage = getStorageStack();
        if(augmentStorage.getItem() instanceof AugmentTieredStorage storageStack)
        {
            return storageStack.getAdditionalFluidStoragePerItem(augmentStorage.getItem()) * augmentStorage.getCount();
        }

        return 0;
    }

    public int getEnergyAmountIncreaseFromStorage()
    {
        ItemStack augmentStorage = getStorageStack();
        if(augmentStorage.getItem() instanceof AugmentTieredStorage storageStack)
        {
            return storageStack.getAdditionalEnergyStoragePerItem(augmentStorage.getItem()) * augmentStorage.getCount();
        }

        return 0;
    }

    public int getXpLevelAmountIncreaseFromStorage()
    {
        ItemStack augmentStorage = getStorageStack();
        if(augmentStorage.getItem() instanceof AugmentTieredStorage storageStack)
        {
            return storageStack.getAdditionalXpStoragePerItem(augmentStorage.getItem()) * augmentStorage.getCount();
        }

        return 0;
    }

    public int getDustAmountIncreaseFromStorage()
    {
        ItemStack augmentStorage = getStorageStack();
        if(augmentStorage.getItem() instanceof AugmentTieredStorage storageStack)
        {
            return storageStack.getAdditionalDustStoragePerItem(augmentStorage.getItem()) * augmentStorage.getCount();
        }

        return 0;
    }

    /*============================================================================
    ==============================================================================
    ===========================     STORAGE  END     =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================     RANGE  START     =============================
    ==============================================================================
    ============================================================================*/

    public boolean addRange(ItemStack rangeAugment)
    {
        if(rangeAugment.getItem() instanceof AugmentTieredRange rangeStack)
        {
            IItemHandler ph = privateHandler.orElse(null);
            if(ph != null)
            {
                ItemStack itemFromBlock = rangeAugment.copy();
                itemFromBlock.setCount(1);
                if(getRange() < rangeStack.getAllowedInsertAmount(rangeAugment.getItem()))
                {
                    ph.insertItem(10,itemFromBlock,false);
                    //update();
                    return true;
                }
            }
        }

        return false;
    }

    public ItemStack removeRange(int count)
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph != null)
        {
            if(hasRange())
            {
                return ph.extractItem(10,count,false);
            }
        }

        return ItemStack.EMPTY;
    }

    public ItemStack removeAllRange()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph != null)
        {
            if(hasRange())
            {
                //update();
                return ph.extractItem(10,ph.getStackInSlot(10).getCount(),false);
            }
        }

        return ItemStack.EMPTY;
    }

    public boolean hasRange()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph != null)
        {
            if(ph.getStackInSlot(10).isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    public int getRange()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph != null)return ph.getStackInSlot(10).getCount();

        return 0;
    }

    public ItemStack getRangeStack()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph != null)return ph.getStackInSlot(10);

        return ItemStack.EMPTY;
    }

    public boolean canInsertAugmentRange(ItemStack rangeAugment)
    {
        if(rangeAugment.getItem() instanceof AugmentTieredRange rangeStack)
        {
            //Check to see if any can be insert
            if(rangeStack.getAllowedInsertAmount(rangeAugment.getItem())<=0) return false;

            if(hasRange())
            {
                //Check to see if stacks to be insert match
                if(!rangeAugment.getItem().equals(getRangeStack().getItem()))return false;
                //Check to see if more can be insert
                if(getRange() >= rangeStack.getAllowedInsertAmount(rangeAugment.getItem())) return false;

                return true;
            }
            else return true;
        }

        return false;
    }

    public int getRangeIncrease()
    {
        ItemStack augmentRange = getRangeStack();
        if(augmentRange.getItem() instanceof AugmentTieredRange rangeStack)
        {
            return rangeStack.getRangeIncreasePerItem(augmentRange.getItem()) * augmentRange.getCount();
        }

        return 0;
    }

    /*============================================================================
    ==============================================================================
    ===========================      RANGE  END      =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================     TOOL   START     =============================
    ==============================================================================
    ============================================================================*/

    public boolean isAllowedTool(ItemStack toolIn)
    {
        if(toolIn.getItem() instanceof DiggerItem)return true;
        if(toolIn.getItem() instanceof SwordItem)return true;
        if(toolIn.getItem() instanceof ProjectileWeaponItem)return true;
        if(toolIn.getItem() instanceof TridentItem)return true;
        if(toolIn.canPerformAction(ToolActions.PICKAXE_DIG))return true;
        if(toolIn.canPerformAction(ToolActions.AXE_DIG))return true;
        if(toolIn.canPerformAction(ToolActions.SHOVEL_DIG))return true;
        if(toolIn.canPerformAction(ToolActions.HOE_DIG))return true;
        if(toolIn.canPerformAction(ToolActions.SWORD_SWEEP))return true;
        if(toolIn.canPerformAction(ToolActions.SWORD_DIG))return true;
        if(toolIn.canPerformAction(ToolActions.SHEARS_HARVEST))return true;
        if(toolIn.canPerformAction(ToolActions.SHEARS_DIG))return true;

        return false;
    }


    public boolean addTool(ItemStack toolItem)
    {
        IItemHandler ph = privateHandler.orElse(null);
        ItemStack itemFromBlock = toolItem.copy();
        itemFromBlock.setCount(1);

        if(canInsertTool(itemFromBlock))
        {
            ph.insertItem(11,itemFromBlock,false);
            return true;
        }
        else return false;
    }

    public ItemStack removeTool(int count)
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasTool())
        {
            return ph.extractItem(11,count,false);
        }
        else return ItemStack.EMPTY;
    }

    public ItemStack removeAllTool()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasTool())
        {
            //update();
            return ph.extractItem(11,ph.getStackInSlot(11).getCount(),false);
        }
        else return ItemStack.EMPTY;
    }

    public boolean hasTool()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(11).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public int getToolDurability()
    {
        if(hasTool())
        {
            ItemStack toolInPed = getToolStack();
            if(toolInPed.isDamageableItem())
            {
                return toolInPed.getDamageValue();
            }
            return -1;
        }
        return 0;
    }

    public int getToolMaxDurability()
    {
        if(hasTool())
        {
            ItemStack toolInPed = getToolStack();
            if(toolInPed.isDamageableItem())
            {
                return toolInPed.getMaxDamage();
            }
            return -1;
        }
        return 0;
    }

    public int getDurabilityRemainingOnInsertedTool()
    {
        return getToolMaxDurability() - getToolDurability();
    }

    public boolean repairInsertedTool(int repairAmount, boolean simulate)
    {
        if(getDurabilityRemainingOnInsertedTool()<getToolMaxDurability())
        {
            if(!simulate)
            {
                int newDamageAmount = getToolDurability() - repairAmount;
                getToolStack().setDamageValue(newDamageAmount);
                return true;
            }
            return true;
        }

        return false;
    }

    public boolean damageInsertedTool(int damageAmount, boolean simulate)
    {
        if(getDurabilityRemainingOnInsertedTool()>damageAmount)
        {
            if(!simulate)
            {
                int newDamageAmount = getToolDurability() + damageAmount;
                getToolStack().setDamageValue(newDamageAmount);
                return true;
            }
            return true;
        }

        return false;
    }

    public ItemStack getToolStack()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(11);
    }

    public boolean canInsertTool(ItemStack tool)
    {
        if(isAllowedTool(tool))
        {
            //Check to see if any can be insert
            if(hasTool())return false;
            else return true;
        }

        return false;
    }

    /*============================================================================
    ==============================================================================
    ===========================      TOOL   END      =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================      LIGHT START     =============================
    ==============================================================================
    ============================================================================*/

    private int slotLight = 1;

    public boolean addLight()
    {
        if(hasLight())
        {
            return false;
        }
        else
        {
            IItemHandler ph = privateHandler.orElse(null);
            BlockState state = level.getBlockState(getPos());
            BlockState newstate = MowLibColorReference.addColorToBlockState(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get().defaultBlockState(),MowLibColorReference.getColorFromStateInt(state)).setValue(WATERLOGGED, state.getValue(WATERLOGGED)).setValue(FACING, state.getValue(FACING)).setValue(LIT, Boolean.valueOf(true)).setValue(FILTER_STATUS, state.getValue(FILTER_STATUS));
            ph.insertItem(slotLight,new ItemStack(Items.GLOWSTONE,1),false);
            update();
            level.setBlock(getPos(),newstate,3);
            return true;
        }
    }

    public ItemStack removeLight()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasLight())
        {
            BlockState state = level.getBlockState(getPos());
            BlockState newstate = MowLibColorReference.addColorToBlockState(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get().defaultBlockState(),MowLibColorReference.getColorFromStateInt(state)).setValue(WATERLOGGED, state.getValue(WATERLOGGED)).setValue(FACING, state.getValue(FACING)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, state.getValue(FILTER_STATUS));
            ph.extractItem(slotLight,1,false);
            level.setBlock(getPos(),newstate,3);
            update();
            return new ItemStack(Items.GLOWSTONE,1);

        }
        else return ItemStack.EMPTY;
    }

    /*public ItemStack removeLight()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasLight())
        {
            BlockState state = level.getBlockState(getPos());
            BlockState newstate = MowLibColorReference.addColorToBlockState(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get().defaultBlockState(),MowLibColorReference.getColorFromStateInt(state)).setValue(WATERLOGGED, state.getValue(WATERLOGGED)).setValue(FACING, state.getValue(FACING)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, state.getValue(FILTER_STATUS));
            if(getLightBrightness()<=1)
            {
                boolLight = true;
                ph.extractItem(slotLight,1,false);
                level.setBlock(getPos(),newstate,3);
                return new ItemStack(Items.GLOWSTONE_DUST,1);
            }
            else
            {
                ph.extractItem(slotLight,1,false);
                state.updateNeighbourShapes(this.level,getPos(),1,3);
                return new ItemStack(Items.GLOWSTONE_DUST,1);
            }

        }
        else return ItemStack.EMPTY;
    }*/

    /*public ItemStack removeAllLight()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasLight())
        {
            BlockState state = level.getBlockState(getPos());
            BlockState newstate = MowLibColorReference.addColorToBlockState(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get().defaultBlockState(),MowLibColorReference.getColorFromStateInt(state)).setValue(WATERLOGGED, state.getValue(WATERLOGGED)).setValue(FACING, state.getValue(FACING)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, state.getValue(FILTER_STATUS));
            int slotCount = ph.getStackInSlot(slotLight).getCount();
            ph.extractItem(slotLight,slotCount,false);
            level.setBlock(getPos(),newstate,3);
            return new ItemStack(Items.GLOWSTONE_DUST,slotCount);
        }
        else return ItemStack.EMPTY;
    }*/

    /*public int getLightBrightness()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(slotLight).getCount();
    }*/


    public boolean hasLight()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(slotLight).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    /*============================================================================
    ==============================================================================
    ===========================       LIGHT END      =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================     FILTER START     =============================
    ==============================================================================
    ============================================================================*/

    private int slotFilter = 2;
    public boolean hasFilter()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(slotFilter).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public ItemStack getFilterInPedestal()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(slotFilter);
    }

    public IPedestalFilter getIPedestalFilter()
    {
        if(hasFilter())
        {
            if(getFilterInPedestal().getItem() instanceof IPedestalFilter)
            {
                return ((IPedestalFilter)getFilterInPedestal().getItem());
            }
        }

        return null;
    }

    public ItemStack removeFilter(boolean updateBlock) {
        IItemHandler ph = privateHandler.orElse(null);
        if(updateBlock)
        {
            BlockState state = level.getBlockState(getPos());
            BlockState newstate = MowLibColorReference.addColorToBlockState(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get().defaultBlockState(),MowLibColorReference.getColorFromStateInt(state)).setValue(WATERLOGGED, state.getValue(WATERLOGGED)).setValue(FACING, state.getValue(FACING)).setValue(LIT, state.getValue(LIT)).setValue(FILTER_STATUS, 0);
            level.setBlock(getPos(),newstate,3);
            update();
        }

        return ph.extractItem(slotFilter,ph.getStackInSlot(slotFilter).getCount(),false);
    }

    public boolean addFilter(ItemStack filter, boolean simulate)
    {
        if(hasFilter())
        {
            return false;
        }
        else
        {
            IItemHandler ph = privateHandler.orElse(null);
            ItemStack itemFromBlock = filter.copy();
            itemFromBlock.setCount(1);
            if(!hasFilter() && ph.isItemValid(slotFilter,itemFromBlock))
            {
                if(!simulate)
                {
                    ph.insertItem(slotFilter,itemFromBlock,false);
                    BlockState state = level.getBlockState(getPos());
                    BlockState newstate = MowLibColorReference.addColorToBlockState(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get().defaultBlockState(),MowLibColorReference.getColorFromStateInt(state)).setValue(WATERLOGGED, state.getValue(WATERLOGGED)).setValue(FACING, state.getValue(FACING)).setValue(LIT, state.getValue(LIT)).setValue(FILTER_STATUS, (((IPedestalFilter) itemFromBlock.getItem()).getFilterType(itemFromBlock))?(2):(1));
                    level.setBlock(getPos(),newstate,3);
                    update();
                }
                return true;
            }
            else return false;
        }
    }

    /*============================================================================
    ==============================================================================
    ===========================      FILTER END      =============================
    ==============================================================================
    ============================================================================*/

    /*============================================================================
    ==============================================================================
    ===========================    REDSTONE START    =============================
    ==============================================================================
    ============================================================================*/

    private int torchSlot = 3;
    public boolean hasRedstone()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(torchSlot).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public boolean addRedstone()
    {
        IItemHandler ph = privateHandler.orElse(null);
        ItemStack itemFromBlock = new ItemStack(Items.REDSTONE);
        itemFromBlock.setCount(1);
        if(!hasRedstone() || getRedstonePowerNeeded()<15)
        {
            ph.insertItem(torchSlot,itemFromBlock,false);
            return true;
        }
        else return false;
    }

    public ItemStack removeRedstone()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasRedstone() && getRedstonePowerNeeded()>=1)
        {
            ph.extractItem(torchSlot,1,false);
            return new ItemStack(Items.REDSTONE,1);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack removeAllRedstone()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasRedstone() && getRedstonePowerNeeded()>=1)
        {
            int slotCount = ph.getStackInSlot(torchSlot).getCount();
            ph.extractItem(torchSlot,slotCount,false);
            return new ItemStack(Items.REDSTONE,slotCount);
        }
        return ItemStack.EMPTY;
    }

    public int getRedstonePowerNeeded()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(torchSlot).getCount();
    }

    public boolean isPedestalBlockPowered(BasePedestalBlockEntity pedestal)
    {
        if(pedestal.hasRedstone())
        {
            //hasRedstone should mean if theres a signal, its off (reverse of normal)
            return (this.getLevel().hasNeighborSignal(pedestal.getBlockPos()))?((pedestal.getRedstonePower()>=pedestal.getRedstonePowerNeeded())?(false):(true)):(true);
        }

        return pedestal.getRedstonePower() > 0;
    }

    public int getRedstonePower() {
        /*System.out.println(this.getLevel().getSignal(this.getBlockPos(), Direction.NORTH));
        System.out.println(this.getLevel().getDirectSignal(this.getBlockPos(), Direction.NORTH));

        //Redstone Dust Linked to it ONLY
        System.out.println(this.getLevel().getDirectSignalTo(this.getBlockPos()));

        //Any Redstone Signal (Lever, torch, dust, whatever
        System.out.println(this.getLevel().getBestNeighborSignal(this.getBlockPos()));*/

        return this.getLevel().getBestNeighborSignal(this.getBlockPos());
    }

    /*============================================================================
    ==============================================================================
    ===========================    REDSTONE END      =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================     Robin START     ==============================
    ==============================================================================
    ============================================================================*/

    private int slotRobin = 4;
    public boolean addRRobin(ItemStack roundRobin)
    {
        IItemHandler ph = privateHandler.orElse(null);
        ItemStack itemFromBlock = roundRobin.copy();
        itemFromBlock.setCount(1);
        if(!hasRRobin())
        {
            ph.insertItem(slotRobin,itemFromBlock,false);
            //update();
            return true;
        }
        else return false;
    }

    public ItemStack removeRRobin()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasRRobin())
        {
            //update();
            return ph.extractItem(slotRobin,ph.getStackInSlot(slotRobin).getCount(),false);
        }
        else return ItemStack.EMPTY;
    }

    public boolean hasRRobin()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(slotRobin).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    /*============================================================================
    ==============================================================================
    ===========================      Robin END      ==============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ============================   RENDER START    ===============================
    ==============================================================================
    ============================================================================*/

    private int slotRenderer = 5;
    public boolean addRenderAugment(ItemStack particle)
    {
        IItemHandler ph = privateHandler.orElse(null);
        ItemStack itemFromBlock = particle.copy();
        itemFromBlock.setCount(1);
        if(!hasRenderAugment())
        {
            //update();
            ph.insertItem(slotRenderer,itemFromBlock,false);
            return true;
        }
        else return false;
    }

    public ItemStack removeRenderAugment()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasRenderAugment())
        {
            //update();
            return ph.extractItem(slotRenderer,ph.getStackInSlot(slotRenderer).getCount(),false);
        }
        else return ItemStack.EMPTY;
    }

    public boolean hasRenderAugment()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(slotRenderer).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public boolean canSpawnParticles()
    {
        switch (getRendererType())
        {
            case 0: return false;
            case 1: return true;
            case 2: return true;
            case 3: return false;
            case 4: return false;
            case 5: return true;
            case 6: return false;
            case 7: return true;
            default: return true;
        }
    }

    public int getRendererType()
    {
        // 0 - No Particles
        // 1 - No Render Item
        // 2 - No Render Upgrade
        // 3 - No Particles/No Render Item
        // 4 - No Particles/No Render Upgrade
        // 5 - No Render Item/No Render Upgrade
        // 6 - No Particles/No Render Item/No Render Upgrade
        // 7 - No Augment exists and thus all rendering is fine.
        IItemHandler ph = privateHandler.orElse(null);
        if(hasRenderAugment())
        {
            if(ph.getStackInSlot(slotRenderer).getItem() instanceof AugmentRenderDiffuser)
            {
                AugmentRenderDiffuser augment = ((AugmentRenderDiffuser)ph.getStackInSlot(slotRenderer).getItem());
                return augment.getAugmentMode(ph.getStackInSlot(slotRenderer));
            }
            else  return 0;
        }
        else  return 7;
    }

    /*============================================================================
    ==============================================================================
    ============================    RENDER END     ===============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================   NO COLLIDE START  ==============================
    ==============================================================================
    ============================================================================*/

    private int slotNoCollide = 6;
    public boolean addNoCollide(ItemStack roundRobin)
    {
        IItemHandler ph = privateHandler.orElse(null);
        ItemStack itemFromBlock = roundRobin.copy();
        itemFromBlock.setCount(1);
        if(!hasRRobin())
        {
            ph.insertItem(slotNoCollide,itemFromBlock,false);
            //update();
            return true;
        }
        else return false;
    }

    public ItemStack removeNoCollide()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(hasRRobin())
        {
            //update();
            return ph.extractItem(slotNoCollide,ph.getStackInSlot(slotNoCollide).getCount(),false);
        }
        else return ItemStack.EMPTY;
    }

    public boolean hasNoCollide()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(slotNoCollide).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    /*============================================================================
    ==============================================================================
    ===========================    NO COLLIDE END   ==============================
    ==============================================================================
    ============================================================================*/



    public boolean canSendItemInPedestal(BasePedestalBlockEntity pedestal)
    {
        if(pedestal.hasItem())return true;

        return false;
    }

    public boolean isSamePedestal(BlockPos pedestalToBeLinked)
    {
        BlockPos thisPedestal = this.getPos();

        if(thisPedestal.equals(pedestalToBeLinked))
        {
            return true;
        }

        return false;
    }

    //Checks when linking pedestals if the two being linked are the same block and within range
    public boolean canLinkToPedestalNetwork(BlockPos pedestalToBeLinked)
    {
        //Check to see if pedestal to be linked is a block pedestal
        if(level.getBlockState(pedestalToBeLinked).getBlock() instanceof BasePedestalBlock)
        {
            //isPedestalInRange(tileCurrent,pedestalToBeLinked);
            return true;
        }

        return false;
    }

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

    //Needed for filtered imports
    public boolean canSendToPedestal(BasePedestalBlockEntity pedestal)
    {
        //Method to check if we can send items FROM this pedestal???
        //Check if Block is Loaded in World
        if(level.isAreaLoaded(pedestal.getPos(),1))
        {
            //If block ISNT powered
            if(!isPedestalBlockPowered(pedestal))
            {
                //Make sure its a pedestal before getting the tile
                if(level.getBlockState(pedestal.getPos()).getBlock() instanceof BasePedestalBlock)
                {
                    //Make sure it is still part of the right network
                    if(canLinkToPedestalNetwork(pedestal.getPos()))
                    {
                        return true;
                    }
                }
                else
                {
                    removeLocation(pedestal.getPos());
                }
            }
        }

        return false;
    }























































































    //The actual transfer methods for items
    public boolean sendItemsToPedestal(BlockPos pedestalToSendTo, ItemStack itemStackIncoming)
    {
        if(!removeItem(true).isEmpty())
        {
            if(level.getBlockEntity(pedestalToSendTo) instanceof BasePedestalBlockEntity tilePedestalToSendTo)
            {
                LazyOptional<IItemHandler> cap = tilePedestalToSendTo.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
                if(cap.isPresent())
                {
                    IItemHandler handler = cap.orElse(null);
                    //Our Item Handler doesnt require a slot value so default == 0
                    if(handler.isItemValid(0,itemStackIncoming))
                    {
                        ItemStack notInsertedStackSimulation = tilePedestalToSendTo.addItemStack(itemStackIncoming,true);
                        //this means some items were inserted OR the full stack was inserted
                        int difference = (notInsertedStackSimulation.isEmpty())?(itemStackIncoming.getCount()):(itemStackIncoming.getCount() - notInsertedStackSimulation.getCount());
                        if(difference > 0)
                        {
                            int countToSend = Math.min(getItemTransferRate(),difference);
                            if(countToSend >=1)
                            {
                                ItemStack copyIncomingStack = itemStackIncoming.copy();
                                copyIncomingStack.setCount(countToSend);
                                //Send items
                                if(tilePedestalToSendTo.addItem(copyIncomingStack,true))
                                {
                                    removeItem(copyIncomingStack.getCount(),false);
                                    tilePedestalToSendTo.addItem(copyIncomingStack,false);
                                    if(canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_BEAM,pedestalToSendTo.getX(),pedestalToSendTo.getY(),pedestalToSendTo.getZ(),getPos().getX(),getPos().getY(),getPos().getZ()));
                                    return true;
                                }
                            }
                        }
                    }
                }

                /*//Checks if pedestal is empty or if not then checks if items match and how many can be insert
                if(tilePedestalToSendTo.itemCountToAccept(level,pedestalToSendTo,itemStackIncoming) > 0)
                {
                    boolean filter = true;
                    //Check if it has filter, if not return true
                    if(tilePedestalToSendTo.hasFilter())
                    {
                        Item filterInPedestal = tilePedestalToSendTo.getFilterInPedestal().getItem();
                        if(filterInPedestal instanceof IPedestalFilter filterable)
                        {
                            filter = filterable.canAcceptItem(tilePedestalToSendTo,itemStackIncoming,0);
                        }
                    }

                    if(filter)
                    {
                        //Max that can be recieved
                        int countToSend = tilePedestalToSendTo.itemCountToAccept(level,pedestalToSendTo,removeItem(true));
                        ItemStack copyStackToSend = removeItem(true).copy();
                        countToSend = Math.min(getItemTransferRate(),(copyStackToSend.getCount()<countToSend)?(copyStackToSend.getCount()):(countToSend));

                        //Max that is available to send
                        *//*if(copyStackToSend.getCount()<countToSend)
                        {
                            countToSend = copyStackToSend.getCount();
                        }*//*
                        //Get max that can be sent
                        *//*if(countToSend > getItemTransferRate())
                        {
                            countToSend = getItemTransferRate();
                        }*//*


                        if(countToSend >=1)
                        {
                            //Send items
                            if(tilePedestalToSendTo.addItem(copyStackToSend,true))
                            {
                                copyStackToSend.setCount(countToSend);
                                removeItem(copyStackToSend.getCount(),false);
                                tilePedestalToSendTo.addItem(copyStackToSend,false);
                                if(canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_BEAM,pedestalToSendTo.getX(),pedestalToSendTo.getY(),pedestalToSendTo.getZ(),getPos().getX(),getPos().getY(),getPos().getZ()));
                                return true;
                            }
                        }
                    }
                }*/
            }
        }

        return false;
    }

    public boolean sendFluidsToPedestal(BlockPos pedestalToSendTo, FluidStack fluidStackIncoming)
    {
        if(hasFluid())
        {
            if(level.getBlockEntity(pedestalToSendTo) instanceof BasePedestalBlockEntity tilePedestalToSendTo)
            {
                if(tilePedestalToSendTo.canAcceptFluid(fluidStackIncoming))
                {
                    if(tilePedestalToSendTo.fluidAmountToAccept(level,pedestalToSendTo,fluidStackIncoming) > 0)
                    {

                        //Max that can be recieved
                        int countToSend = tilePedestalToSendTo.spaceForFluid();
                        FluidStack currentFluid = getStoredFluid().copy();
                        countToSend = Math.min(getFluidTransferRate(),(currentFluid.getAmount()<countToSend)?(currentFluid.getAmount()):(countToSend));
                        //Max that is available to send
                        /*if(currentFluid.getAmount()<countToSend)
                        {
                            countToSend = currentFluid.getAmount();
                        }*/
                        //Get max that can be sent
                        /*if(countToSend > getFluidTransferRate())
                        {
                            countToSend = getFluidTransferRate();
                        }*/


                        if(countToSend > 0)
                        {
                            //Send items
                            FluidStack stackFluidToSend = fluidStackIncoming.copy();
                            stackFluidToSend.setAmount(countToSend);
                            int countFluidToSend = tilePedestalToSendTo.addFluid(stackFluidToSend, IFluidHandler.FluidAction.SIMULATE);

                            if(countFluidToSend>0)
                            {
                                int finalFluidCountToSend = removeFluid(countFluidToSend, IFluidHandler.FluidAction.SIMULATE).getAmount();
                                if(finalFluidCountToSend>0)
                                {
                                    tilePedestalToSendTo.addFluid(new FluidStack(fluidStackIncoming.getFluid(),finalFluidCountToSend,fluidStackIncoming.getTag()), IFluidHandler.FluidAction.EXECUTE);
                                    removeFluid(countFluidToSend, IFluidHandler.FluidAction.EXECUTE);
                                    if(canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_BEAM,pedestalToSendTo.getX(),pedestalToSendTo.getY(),pedestalToSendTo.getZ(),getPos().getX(),getPos().getY(),getPos().getZ()));
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean sendEnergyToPedestal(BlockPos pedestalToSendTo, int energyIncoming)
    {
        if(hasEnergy())
        {
            if(level.getBlockEntity(pedestalToSendTo) instanceof BasePedestalBlockEntity tilePedestalToSendTo)
            {
                //Checks if pedestal is empty or if not then checks if items match and how many can be insert
                if(tilePedestalToSendTo.energyAmountToAccept(level,pedestalToSendTo,energyIncoming) > 0)
                {
                    if(tilePedestalToSendTo.canAcceptEnergy())
                    {
                        //Max that can be recieved
                        int countToSend = tilePedestalToSendTo.getEnergyCapacity()-tilePedestalToSendTo.getStoredEnergy();
                        countToSend = Math.min(getEnergyTransferRate(),(energyIncoming<countToSend)?(energyIncoming):(countToSend));

                        //Max that is available to send
                        /*if(energyIncoming<countToSend)
                        {
                            countToSend = energyIncoming;
                        }*/
                        //Get max that can be sent
                        /*if(countToSend > getEnergyTransferRate())
                        {
                            countToSend = getEnergyTransferRate();
                        }*/


                        if(countToSend > 0)
                        {
                            //Send items
                            int countEnergyToSend = tilePedestalToSendTo.addEnergy(countToSend,true);
                            if(countEnergyToSend>0)
                            {
                                int finalEnergyCountToSend = removeEnergy(countEnergyToSend,true);
                                if(finalEnergyCountToSend>0)
                                {
                                    removeEnergy(finalEnergyCountToSend,false);
                                    tilePedestalToSendTo.addEnergy(finalEnergyCountToSend,false);
                                    if(canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_BEAM,pedestalToSendTo.getX(),pedestalToSendTo.getY(),pedestalToSendTo.getZ(),getPos().getX(),getPos().getY(),getPos().getZ()));
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean sendExperienceToPedestal(BlockPos pedestalToSendTo, int experienceIncoming)
    {
        if(hasExperience())
        {
            if(level.getBlockEntity(pedestalToSendTo) instanceof BasePedestalBlockEntity tilePedestalToSendTo)
            {

                if(tilePedestalToSendTo.canAcceptExperience())
                {
                    if(tilePedestalToSendTo.experienceAmountToAccept(level,pedestalToSendTo,experienceIncoming) > 0)
                    {

                        //Max that can be recieved
                        int countToSend = tilePedestalToSendTo.getExperienceCapacity()-tilePedestalToSendTo.getStoredExperience();
                        countToSend = Math.min(getExperienceTransferRate(),(experienceIncoming<countToSend)?(experienceIncoming):(countToSend));

                        //Max that is available to send
                        /*if(experienceIncoming<countToSend)
                        {
                            countToSend = experienceIncoming;
                        }*/
                        //Get max that can be sent
                        /*if(countToSend > getExperienceTransferRate())
                        {
                            countToSend = getExperienceTransferRate();
                        }*/


                        if(countToSend > 0)
                        {
                            //Send items
                            int countExperienceToSend = tilePedestalToSendTo.addExperience(countToSend,true);
                            if(countExperienceToSend>0)
                            {
                                int finalExperienceCountToSend = removeExperience(countExperienceToSend,true);
                                if(finalExperienceCountToSend>0)
                                {
                                    removeExperience(finalExperienceCountToSend,false);
                                    tilePedestalToSendTo.addExperience(finalExperienceCountToSend,false);
                                    if(canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_BEAM,pedestalToSendTo.getX(),pedestalToSendTo.getY(),pedestalToSendTo.getZ(),getPos().getX(),getPos().getY(),getPos().getZ()));
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean sendDustToPedestal(BlockPos pedestalToSendTo, int dustIncoming)
    {
        if(hasDust())
        {
            if(level.getBlockEntity(pedestalToSendTo) instanceof BasePedestalBlockEntity tilePedestalToSendTo)
            {
                if(tilePedestalToSendTo.canAcceptDust(getStoredDust()))
                {
                    if(tilePedestalToSendTo.dustAmountToAccept(level,pedestalToSendTo,dustIncoming) > 0)
                    {

                        //Max that can be recieved
                        int countToSend = tilePedestalToSendTo.getDustCapacity()-tilePedestalToSendTo.getStoredDust().getDustAmount();
                        countToSend = Math.min(getExperienceTransferRate(),(dustIncoming<countToSend)?(dustIncoming):(countToSend));

                        //Max that is available to send
                        /*if(dustIncoming<countToSend)
                        {
                            countToSend = dustIncoming;
                        }*/
                        //Get max that can be sent
                        /*if(countToSend > getExperienceTransferRate())
                        {
                            countToSend = getExperienceTransferRate();
                        }*/


                        if(countToSend > 0)
                        {
                            //Send items
                            int countDustToSend = tilePedestalToSendTo.addDust(new DustMagic(getStoredDust().getDustColor(),countToSend), IDustHandler.DustAction.SIMULATE);
                            if(countDustToSend>0)
                            {
                                int finalExperienceCountToSend = removeDust(countDustToSend, IDustHandler.DustAction.SIMULATE).getDustAmount();
                                if(finalExperienceCountToSend>0)
                                {
                                    if(tilePedestalToSendTo.addDust(new DustMagic(getStoredDust().getDustColor(),finalExperienceCountToSend), IDustHandler.DustAction.EXECUTE) > 0)
                                    {
                                        removeDust(countDustToSend, IDustHandler.DustAction.EXECUTE);
                                        if(canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_BEAM,pedestalToSendTo.getX(),pedestalToSendTo.getY(),pedestalToSendTo.getZ(),getPos().getX(),getPos().getY(),getPos().getZ()));
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public void transferAction()
    {
        int locations = getNumberOfStoredLocations();
        if(locations > 0)
        {
            if(hasRRobin())
            {
                int robinCount = getStoredValueForUpgrades();
                if(robinCount >= locations)
                {
                    setStoredValueForUpgrades(0);
                    robinCount=0;
                }
                BlockPos posReceiver = getStoredPositionAt(robinCount);
                if(level.getBlockEntity(posReceiver) instanceof BasePedestalBlockEntity pedestal)
                {
                    if(canSendToPedestal(pedestal))
                    {
                        sendItemsToPedestal(posReceiver,removeItem(true));
                        sendFluidsToPedestal(posReceiver,getStoredFluid());
                        sendEnergyToPedestal(posReceiver,getStoredEnergy());
                        sendExperienceToPedestal(posReceiver,getStoredExperience());
                        sendDustToPedestal(posReceiver,getStoredDust().getDustAmount());
                    }
                }

                robinCount++;
                setStoredValueForUpgrades(robinCount);
            }
            else
            {
                for(int i=0;i<locations;i++){
                    BlockPos posReceiver = getStoredPositionAt(i);
                    if(level.getBlockEntity(posReceiver) instanceof BasePedestalBlockEntity pedestal)
                    {
                        if(canSendToPedestal(pedestal))
                        {
                            int b = 0;
                            if(sendItemsToPedestal(posReceiver,removeItem(true)))b++;
                            if(sendFluidsToPedestal(posReceiver,getStoredFluid()))b++;
                            if(sendEnergyToPedestal(posReceiver,getStoredEnergy()))b++;
                            if(sendExperienceToPedestal(posReceiver,getStoredExperience()))b++;
                            if(sendDustToPedestal(posReceiver,getStoredDust().getDustAmount()))b++;

                            if(b>0)break;
                        }
                    }
                }
            }
        }
    }

    public BlockPos offsetBasedOnDirection(Direction enumfacing, BlockPos posOfPedestal, double x, double y, double z)
    {
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return new BlockPos(blockBelow.getX() + x, blockBelow.getY() + y, blockBelow.getZ() + z);
            case DOWN:
                return new BlockPos(blockBelow.getX() + x, blockBelow.getY() + y + 1D, blockBelow.getZ() + z);
            case NORTH:
                return new BlockPos(blockBelow.getX() + x, blockBelow.getY() + z, blockBelow.getZ() + y);
            case SOUTH:
                return new BlockPos(blockBelow.getX() + x, blockBelow.getY() + z, blockBelow.getZ() + y);
            case EAST:
                return new BlockPos(blockBelow.getX() + y, blockBelow.getY() + x, blockBelow.getZ() + z);
            case WEST:
                return new BlockPos(blockBelow.getX() + y, blockBelow.getY() + x, blockBelow.getZ() + z);
            default:
                return blockBelow;
        }
    }






    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BasePedestalBlockEntity e) {
        e.tick();
    }

    public static <E extends BlockEntity> void clientTick(Level level, BlockPos blockPos, BlockState blockState, BasePedestalBlockEntity e) {
        e.tick();
    }

    int partTicker = 0;
    int impTicker = 0;
    int pedTicker = 0;

    public void tick() {

        if(!level.isClientSide() && level.isAreaLoaded(getPos(),1))
        {
            pedTicker++;
            //if (pedTicker%getOperationSpeed() == 0) {
            int configSpeed = PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get();
            int speed = configSpeed;
            if(hasSpeed())speed = PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get() - getTicksReduced();
            //Make sure speed has at least a value of 1
            if(speed<=0)speed = 1;
            if (pedTicker%speed == 0) {

                if(getNumberOfStoredLocations() > 0 && !isPedestalBlockPowered(getPedestal())) { transferAction(); }

                if(hasCoin() && !isPedestalBlockPowered(getPedestal()))
                {
                    Item coinInPed = getCoinOnPedestal().getItem();
                    if(coinInPed instanceof IPedestalUpgrade upgrade) { upgrade.updateAction(level,this); }
                }

                List<Entity> entitiesColliding = level.getEntitiesOfClass(Entity.class,new AABB(getPos()));
                for(Entity getEntity : entitiesColliding)
                {
                    if(!hasNoCollide() && !isPedestalBlockPowered(getPedestal()))
                    {
                        collideWithPedestal(level, getPedestal(), getPos(), getBlockState(), getEntity);
                    }
                }
                //make sure we dont go over max int limit, regardless of config
                if(pedTicker >= maxRate-1){pedTicker=0;}
            }

            if(getRenderRange()){ if(getLevel().getGameTime()%20 == 0)MowLibPacketHandler.sendToNearby(level,getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,getPos().getX(),getPos().getY(),getPos().getZ(),0,0,0)); }

            if(canSpawnParticles())
            {
                BlockPos posDirectionalEnergy = offsetBasedOnDirection(getPedestal().getBlockState().getValue(FACING),getPos(),0D,0D,0D);
                if(getLevel().getGameTime()%20 == 0 && !isPedestalBlockPowered(getPedestal())){if(this.hasEnergy()){MowLibPacketHandler.sendToNearby(level,posDirectionalEnergy,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,posDirectionalEnergy.getX(),posDirectionalEnergy.getY(),posDirectionalEnergy.getZ(),255,0,0));}}
                BlockPos posDirectionalXP = offsetBasedOnDirection(getPedestal().getBlockState().getValue(FACING),getPos(),0.5D,0D,0.5D);
                if(getLevel().getGameTime()%20 == 0 && !isPedestalBlockPowered(getPedestal())){if(this.hasExperience()){MowLibPacketHandler.sendToNearby(level,posDirectionalXP,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,posDirectionalXP.getX(),posDirectionalXP.getY(),posDirectionalXP.getZ(),0,255,0));}}
                //System.out.println(getPos());

                BlockPos posDirectionalFluid = offsetBasedOnDirection(getPedestal().getBlockState().getValue(FACING),getPos(),0.5D,0D,0D);
                //System.out.println(getPos().offset(0.5D,0D,0D));
                if(getLevel().getGameTime()%20 == 0 && !isPedestalBlockPowered(getPedestal())){if(this.hasFluid()){MowLibPacketHandler.sendToNearby(level,posDirectionalFluid,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,posDirectionalFluid.getX(),posDirectionalFluid.getY(),posDirectionalFluid.getZ(),0,0,255));}}

                BlockPos posDirectionalDust = offsetBasedOnDirection(getPedestal().getBlockState().getValue(FACING),getPos(),-0.5D,0D,-0.5D);
                //System.out.println(getPos().offset(0.5D,0D,0D));
                if(getLevel().getGameTime()%20 == 0 && !isPedestalBlockPowered(getPedestal())){if(this.hasDust()){MowLibPacketHandler.sendToNearby(level,posDirectionalDust,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,posDirectionalDust.getX(),posDirectionalDust.getY(),posDirectionalDust.getZ(),178,0,255));}}
            }
        }
    }

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
        CompoundTag invTag = p_155245_.getCompound("inv");
        handler.ifPresent(h -> ((INBTSerializable<CompoundTag>) h).deserializeNBT(invTag));
        CompoundTag invPrivateTag = p_155245_.getCompound("inv_private");
        privateHandler.ifPresent(h -> ((INBTSerializable<CompoundTag>) h).deserializeNBT(invPrivateTag));

        this.storedValueForUpgrades = p_155245_.getInt("storedUpgradeValue");
        this.storedEnergy = p_155245_.getInt("storedEnergy");
        this.storedFluid = FluidStack.loadFluidStackFromNBT(p_155245_.getCompound("storedFluid"));
        this.storedExperience = p_155245_.getInt("storedExperience");
        this.storedDust = DustMagic.getDustMagicInTag(p_155245_);
        //this.setFluid(FluidStack.loadFluidStackFromNBT(p_155245_.getCompound("storedFluid")),0);
        this.storedPotionEffect = (MobEffectInstance.load(p_155245_)!=null)?(MobEffectInstance.load(p_155245_)):(null);
        this.storedPotionEffectDuration = p_155245_.getInt("storedEffectDuration");
        this.showRenderRange = p_155245_.getBoolean("showRenderRange");

        int[] storedIX = p_155245_.getIntArray("intArrayXPos");
        int[] storedIY = p_155245_.getIntArray("intArrayYPos");
        int[] storedIZ = p_155245_.getIntArray("intArrayZPos");

        int[] storedIXS = p_155245_.getIntArray("intArrayXSPos");
        int[] storedIYS = p_155245_.getIntArray("intArrayYSPos");
        int[] storedIZS = p_155245_.getIntArray("intArrayZSPos");

        for(int i=0;i<storedIX.length;i++)
        {
            BlockPos gotPos = new BlockPos(storedIX[i],storedIY[i],storedIZ[i]);
            storedLocations.add(gotPos);
        }

    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
        save(p_187471_);
    }

    public CompoundTag save(CompoundTag p_58888_) {
        //System.out.println("SAVE");
        handler.ifPresent(h -> {
            CompoundTag compound = ((INBTSerializable<CompoundTag>) h).serializeNBT();
            p_58888_.put("inv", compound);
        });
        privateHandler.ifPresent(h -> {
            CompoundTag compound = ((INBTSerializable<CompoundTag>) h).serializeNBT();
            p_58888_.put("inv_private", compound);
        });

        p_58888_.putInt("storedUpgradeValue",storedValueForUpgrades);
        p_58888_.putInt("storedEnergy",storedEnergy);
        p_58888_.put("storedFluid",storedFluid.writeToNBT(new CompoundTag()));
        p_58888_.putInt("storedExperience",storedExperience);

        if(storedPotionEffect!=null)storedPotionEffect.save(p_58888_);
        p_58888_.putInt("storedEffectDuration",storedPotionEffectDuration);
        p_58888_.putBoolean("showRenderRange",showRenderRange);

        List<Integer> storedX = new ArrayList<Integer>();
        List<Integer> storedY = new ArrayList<Integer>();
        List<Integer> storedZ = new ArrayList<Integer>();

        List<Integer> storedXS = new ArrayList<Integer>();
        List<Integer> storedYS = new ArrayList<Integer>();
        List<Integer> storedZS = new ArrayList<Integer>();

        for(int i=0;i<getNumberOfStoredLocations();i++)
        {
            storedX.add(storedLocations.get(i).getX());
            storedY.add(storedLocations.get(i).getY());
            storedZ.add(storedLocations.get(i).getZ());
        }



        p_58888_.putIntArray("intArrayXPos",storedX);
        p_58888_.putIntArray("intArrayYPos",storedY);
        p_58888_.putIntArray("intArrayZPos",storedZ);

        p_58888_.putIntArray("intArrayXSPos",storedXS);
        p_58888_.putIntArray("intArrayYSPos",storedYS);
        p_58888_.putIntArray("intArrayZSPos",storedZS);

        return DustMagic.setDustMagicInTag(p_58888_,this.storedDust);
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB aabb = new AABB(getPos().getX() - getLinkingRange(), getPos().getY() - getLinkingRange(), getPos().getZ() - getLinkingRange(),getPos().getX() + getLinkingRange(), getPos().getY() + getLinkingRange(), getPos().getZ() + getLinkingRange());
        return aabb;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        /*CompoundTag nbtTagCompound = new CompoundTag();
        save(nbtTagCompound);*/
        //super.getUpdatePacket();
        //return new ClientboundBlockEntityDataPacket(getPos(),42,nbtTagCompound);
        //System.out.println("ClientBound");
        return ClientboundBlockEntityDataPacket.create(this.getPedestal());
    }

    @Override
    public CompoundTag getUpdateTag() {
        //System.out.println("getUpdateTag");
        return save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        //System.out.println("onDataPacket");
        super.onDataPacket(net,pkt);
        BlockState state = this.level.getBlockState(getPos());
        this.handleUpdateTag(pkt.getTag());
        this.level.sendBlockUpdated(getPos(), state, state, 3);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        //System.out.println("handleUpdateTag");
        this.load(tag);
    }


    @Override
    public void setRemoved() {
        super.setRemoved();
        if(this.handler != null) {
            this.handler.invalidate();
        }
        if(this.privateHandler != null) {
            this.privateHandler.invalidate();
        }
        if(this.energyHandler != null) {
            this.energyHandler.invalidate();
        }
        if(this.fluidHandler != null) {
            this.fluidHandler.invalidate();
        }
        if(this.experienceHandler != null) {
            this.experienceHandler.invalidate();
        }
        if(this.dustHandler != null) {
            this.dustHandler.invalidate();
        }
    }
}
