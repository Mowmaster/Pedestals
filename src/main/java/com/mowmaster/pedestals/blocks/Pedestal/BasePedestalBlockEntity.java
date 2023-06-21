package com.mowmaster.pedestals.blocks.Pedestal;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseOmniStorageBlockEntity;
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
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseEnergyBulkStorageItem;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseFluidBulkStorageItem;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseXpBulkStorageItem;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.IPedestalUpgrade;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Registry.DeferredBlockEntityTypes;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class BasePedestalBlockEntity extends MowLibBaseOmniStorageBlockEntity
{
    private ItemStackHandler privateItems = createPrivateItemHandler();
    private List<ItemStack> stacksList = new ArrayList<>();
    private WeakReference<FakePlayer> pedestalPlayer;
    private final List<BlockPos> linkedPedestals = new ArrayList<BlockPos>();
    private int storedValueForUpgrades = 0;
    private boolean showRenderRange = false;
    private boolean showRenderRangeUpgrade = false;
    private BasePedestalBlockEntity getPedestal() { return this; }
    private int maxRate = Integer.MAX_VALUE;

    public boolean getRenderRange(){return this.showRenderRange;}
    public void setRenderRange(boolean setRender){ this.showRenderRange = setRender; update();}

    public boolean getRenderRangeUpgrade(){return this.showRenderRangeUpgrade;}
    public void setRenderRangeUpgrade(boolean setRenderUpgrade){ this.showRenderRangeUpgrade = setRenderUpgrade; update();}


    public BasePedestalBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(DeferredBlockEntityTypes.PEDESTAL.get(), p_155229_, p_155230_);
    }

    @Override
    public void update() {
        BlockState state = level.getBlockState(getPos());
        this.level.sendBlockUpdated(getPos(), state, state, 3);
        this.setChanged();
    }

    @Override
    public ItemStackHandler createItemHandlerPedestal() {
        super.createItemHandlerPedestal();
        return new ItemStackHandler()
        {
            @Override
            protected void onContentsChanged(int slot) {
                update();
            }

            @Override
            public int getSlots() {
                int baseSlots = PedestalConfig.COMMON.pedestal_baseItemStacks.get();
                int additionalSlots = getItemSlotIncreaseFromStorage();
                return baseSlots + additionalSlots;
            }
        };
    }

    private static class PrivateInventorySlot {
        static final int COIN = 0;
        static final int AUGMENT_ROUNDROBIN = 1;
        static final int AUGMENT_RENDERDIFFUSER = 2;
        static final int AUGMENT_NOCOLLIDE = 3;
        static final int AUGMENT_TIERED_SPEED = 4;
        static final int AUGMENT_TIERED_CAPACITY = 5;
        static final int AUGMENT_TIERED_STORAGE = 6;
        static final int AUGMENT_TIERED_RANGE = 7;
        static final int TOOL = 8;
    }

    private ItemStackHandler createPrivateItemHandler() {
        //going from 11 to 20 slots to future proof things
        return new ItemStackHandler(20) {

            @Override
            protected void onLoad() {
                if(getSlots()<20)
                {
                    for(int i = 0; i < getSlots(); ++i) {
                        stacksList.add(i,getStackInSlot(i));
                    }
                    setSize(20);
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
                return switch (slot) {
                    case PrivateInventorySlot.COIN -> stack.getItem() instanceof IPedestalUpgrade && !hasCoin();
                    case PrivateInventorySlot.AUGMENT_ROUNDROBIN -> stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_ROUNDROBIN.get()) && !hasRRobin();
                    case PrivateInventorySlot.AUGMENT_RENDERDIFFUSER -> stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_RENDERDIFFUSER.get()) && !hasRenderAugment();
                    case PrivateInventorySlot.AUGMENT_NOCOLLIDE -> stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_NOCOLLIDE.get()) && !hasNoCollide();
                    case PrivateInventorySlot.AUGMENT_TIERED_SPEED -> canInsertSpeedAugment(stack);
                    case PrivateInventorySlot.AUGMENT_TIERED_CAPACITY -> canInsertAugmentCapacity(stack);
                    case PrivateInventorySlot.AUGMENT_TIERED_STORAGE -> canInsertAugmentStorage(stack);
                    case PrivateInventorySlot.AUGMENT_TIERED_RANGE -> canInsertAugmentRange(stack);
                    case PrivateInventorySlot.TOOL -> canInsertTool(stack);
                    default -> false;
                };
            }
        };
    }

    @Override
    public IFluidHandler createFluidHandler() {
        IFluidHandler handler = super.createFluidHandler();
        return new IFluidHandler() {
            @Override
            public int getTanks() {
                return handler.getTanks();
            }

            @Override
            public @NotNull FluidStack getFluidInTank(int tank) {
                return handler.getFluidInTank(tank);
            }

            @Override
            public int getTankCapacity(int tank) {
                int baseFluidStorage = PedestalConfig.COMMON.pedestal_baseFluidStorage.get();
                int additionalFluidStorage = getFluidAmountIncreaseFromStorage();
                return baseFluidStorage + additionalFluidStorage;
            }

            @Override
            public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                return handler.isFluidValid(tank,stack);
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return handler.fill(resource,action);
            }

            @Override
            public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
                return handler.drain(resource,action);
            }

            @Override
            public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
                return handler.drain(maxDrain,action);
            }
        };
    }

    @Override
    public IEnergyStorage createEnergyHandler() {
        IEnergyStorage handler = super.createEnergyHandler();
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                return handler.receiveEnergy(maxReceive,simulate);
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return handler.extractEnergy(maxExtract,simulate);
            }

            @Override
            public int getEnergyStored() {
                return handler.getEnergyStored();
            }

            @Override
            public int getMaxEnergyStored() {
                int baseStorage = PedestalConfig.COMMON.pedestal_baseEnergyStorage.get();
                int additionalStorage = getEnergyAmountIncreaseFromStorage();
                return baseStorage + additionalStorage;
            }

            @Override
            public boolean canExtract() {
                return handler.canExtract();
            }

            @Override
            public boolean canReceive() {
                return handler.canReceive();
            }
        };
    }


    @Override
    public IExperienceStorage createExperienceHandler() {
        IExperienceStorage handler = super.createExperienceHandler();
        return new IExperienceStorage() {
            @Override
            public int receiveExperience(int i, boolean b) {
                return handler.receiveExperience(i,b);
            }

            @Override
            public int extractExperience(int i, boolean b) {
                return handler.extractExperience(i,b);
            }

            @Override
            public int getExperienceStored() {
                return handler.getExperienceStored();
            }

            @Override
            public int getMaxExperienceStored() {
                int convertLevelsToXp = MowLibXpUtils.getExpCountByLevel(PedestalConfig.COMMON.pedestal_baseXpStorage.get());
                //Conditioning out running the xp converter when theres not need to.
                int convertLevelsToXpAdditional = (getXpLevelAmountIncreaseFromStorage()>0)?(MowLibXpUtils.getExpCountByLevel(getXpLevelAmountIncreaseFromStorage())):(0);
                return convertLevelsToXp + convertLevelsToXpAdditional;
            }

            @Override
            public boolean canExtract() {
                return handler.canExtract();
            }

            @Override
            public boolean canReceive() {
                return handler.canReceive();
            }
        };
    }

    @Override
    public IDustHandler createDustHandler() {
        IDustHandler handler = super.createDustHandler();
        return new IDustHandler() {
            @Override
            public int getTanks() {
                return handler.getTanks();
            }

            @NotNull
            @Override
            public DustMagic getDustMagicInTank(int i) {
                return handler.getDustMagicInTank(i);
            }

            @Override
            public int getTankCapacity(int i) {
                int baseStorage = PedestalConfig.COMMON.pedestal_baseDustStorage.get();
                int additionalStorage = getDustAmountIncreaseFromStorage();
                return baseStorage + additionalStorage;
            }

            @Override
            public boolean isDustValid(int i, @NotNull DustMagic dustMagic) {
                return handler.isDustValid(i,dustMagic);
            }

            @Override
            public int fill(DustMagic dustMagic, DustAction dustAction) {
                return handler.fill(dustMagic,dustAction);
            }

            @NotNull
            @Override
            public DustMagic drain(DustMagic dustMagic, DustAction dustAction) {
                return handler.drain(dustMagic,dustAction);
            }

            @NotNull
            @Override
            public DustMagic drain(int i, DustAction dustAction) {
                return handler.drain(i,dustAction);
            }
        };
    }

    /*==========================================================
    ============================================================
    =====                Fake Player Start                ======
    ============================================================
    ==========================================================*/

    public WeakReference<FakePlayer> getFakePlayer()
    {
        return pedestalPlayer;
    }

    public void setFakePlayer()
    {
        pedestalPlayer = fakePedestalPlayer(getPedestal());
        if(pedestalPlayer.get() != null)
        {
            pedestalPlayer.get().setPos(getPos().getX(),getPos().getY(),getPos().getZ());
            pedestalPlayer.get().setRespawnPosition(pedestalPlayer.get().getRespawnDimension(), getPos(),0F,false,false);
        }
    }

    public WeakReference<FakePlayer> getPedestalPlayer(BasePedestalBlockEntity pedestal) {
        if(pedestal.getFakePlayer() == null || pedestal.getFakePlayer().get() == null)
        {
            pedestal.setFakePlayer();
        }

        return pedestal.getFakePlayer();
    }

    public void updatePedestalPlayer(BasePedestalBlockEntity pedestal)
    {
        if(pedestal.getFakePlayer() != null)
        {
            pedestal.setFakePlayer();
        }
    }

    public WeakReference<FakePlayer> fakePedestalPlayer(BasePedestalBlockEntity pedestal)
    {
        Level world = pedestal.getLevel();
        ItemStack upgrade = pedestal.getCoinOnPedestal();
        ItemStack tool = pedestal.getToolStack();
        if(world instanceof ServerLevel slevel)
        {
            return new WeakReference<FakePlayer>(new MowLibFakePlayer(slevel , MowLibOwnerUtils.getPlayerFromStack(upgrade), MowLibOwnerUtils.getPlayerNameFromStack(upgrade),pedestal.getPos(),tool,"[Pedestal_"+ pedestal.getPos().getX() + pedestal.getPos().getY() + pedestal.getPos().getZ() +"]"));
        }
        else return null;
    }

    /*==========================================================
    ============================================================
    =====                 Fake Player End                 ======
    ============================================================
    ==========================================================*/


    public void dropInventoryItemsPrivate(Level worldIn, BlockPos pos) {
        MowLibItemUtils.dropInventoryItems(worldIn,pos,privateItems);
    }

    public List<ItemStack> dropInventoryItemsPrivateList() {
        List<ItemStack> returner = new ArrayList<>();
        for(int i = 0; i < privateItems.getSlots(); ++i) {
            returner.add(privateItems.getStackInSlot(i));
        }

        return returner;
    }

    @Override
    public void dropLiquidsInWorld(Level worldIn, BlockPos pos) {
        FluidStack inTank = getStoredFluid();
        if (inTank.getAmount()>0) {
            ItemStack toDrop = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_FLUID.get());
            if(toDrop.getItem() instanceof BaseFluidBulkStorageItem droppedItemFluid) {
                droppedItemFluid.setFluidStack(toDrop,inTank);
            }
            MowLibItemUtils.spawnItemStack(worldIn,pos.getX(),pos.getY(),pos.getZ(),toDrop);
        }
    }

    @Override
    public void removeEnergyFromBrokenPedestal(Level worldIn, BlockPos pos) {
        if(getStoredEnergy()>0)
        {
            ItemStack toDrop = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_ENERGY.get());
            if(toDrop.getItem() instanceof BaseEnergyBulkStorageItem droppedItemEnergy)
            {
                droppedItemEnergy.setEnergy(toDrop,getStoredEnergy());
            }
            MowLibItemUtils.spawnItemStack(worldIn,pos.getX(),pos.getY(),pos.getZ(),toDrop);
        }
    }

    @Override
    public void dropXPInWorld(Level worldIn, BlockPos pos) {
        if(getStoredExperience()>0)
        {
            ItemStack toDrop = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_XP.get());
            if(toDrop.getItem() instanceof BaseXpBulkStorageItem droppedItemEnergy)
            {
                droppedItemEnergy.setXp(toDrop,getStoredExperience());
                //System.out.println("stored xp: "+ droppedItemEnergy.getXp(toDrop));
            }
            MowLibItemUtils.spawnItemStack(worldIn,pos.getX(),pos.getY(),pos.getZ(),toDrop);
        }
    }

    @Override
    public void dropDustInWorld(Level worldIn, BlockPos pos) {
        if(!getStoredDust().isEmpty())
        {
            ItemStack toDrop = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_DUST.get());
            DustMagic.setDustMagicInStack(toDrop,getStoredDust());
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

    public int getNumLinkedPedestals() {
        return linkedPedestals.size();
    }

    private void addPedestalLink(BlockPos pos) {
        linkedPedestals.add(pos);
        update();
    }

    private void removePedestalLink(BlockPos pos) {
        linkedPedestals.remove(pos);
        update();
    }

    public List<BlockPos> getLinkedLocations() {
        return linkedPedestals;
    }

    public int getLinkingRange() {
        int range = PedestalConfig.COMMON.pedestal_baseLinkingRange.get();
        return  range + getRangeIncrease();
    }

    public boolean isPedestalInRange(BlockPos targetPos) {
        return MowLibBlockPosUtils.arePositionsInRange(getPos(), targetPos, getLinkingRange());
    }

    public boolean isSamePedestal(BlockPos targetPos) {
        return !getPos().equals(targetPos);
    }

    public boolean isAlreadyLinked(BlockPos pos) {
        return linkedPedestals.contains(pos);
    }

    public boolean hasSpaceForPedestalLink() {
        return linkedPedestals.size() < 8;
    }

    public boolean attemptUpdateLink(BlockPos targetPos, Player player, String successLocalizedMessage) {
        boolean attemptSuccessful = false;
        if (!isPedestalInRange(targetPos)) {
            MowLibMessageUtils.messagePopup(player, ChatFormatting.WHITE, MODID + ".tool_link_distance");
        } else if (!isSamePedestal(targetPos)) {
            MowLibMessageUtils.messagePopup(player, ChatFormatting.WHITE, MODID + ".tool_link_itsself");
        } else if (isAlreadyLinked(targetPos)) {
            // this path can only occur via the linking tools, which helps because we don't need a parameter to control it
            attemptSuccessful = true;
            removePedestalLink(targetPos);
            MowLibMessageUtils.messagePopup(player, ChatFormatting.WHITE, MODID + ".tool_link_removed");
        } else if (!hasSpaceForPedestalLink()) {
            MowLibMessageUtils.messagePopup(player, ChatFormatting.WHITE, MODID + ".tool_link_unsucess");
        } else {
            attemptSuccessful = true;
            addPedestalLink(targetPos);
            MowLibMessageUtils.messagePopup(player, ChatFormatting.WHITE, successLocalizedMessage);
        }
        return attemptSuccessful;
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

    public int getItemTransferRate() {
        return  PedestalConfig.COMMON.pedestal_baseItemTransferRate.get() + getItemTransferRateIncreaseFromCapacity();
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

    public int getFluidTransferRate() {
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

    public int getExperienceTransferRate() {
        //im assuming # = xp value???
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

    public int getDustTransferRate() {
        //im assuming # = rf value???
        int baseValue = PedestalConfig.COMMON.pedestal_baseDustTransferRate.get();
        // TODO: I don't imagine this is meant to be using MowLibXpUtils for Dust actions?
        return (getDustTransferRateIncreaseFromCapacity()>0) ? MowLibXpUtils.getExpCountByLevel(getDustTransferRateIncreaseFromCapacity() + baseValue) : baseValue;
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
        return !privateItems.getStackInSlot(PrivateInventorySlot.COIN).isEmpty();
    }

    public ItemStack getCoinOnPedestal()
    {
        return privateItems.getStackInSlot(PrivateInventorySlot.COIN);
    }

    public ItemStack removeCoin() {
        return privateItems.extractItem(PrivateInventorySlot.COIN, 1, false);
    }

    public boolean attemptAddCoin(ItemStack stack, Player player)
    {
        if (privateItems.isItemValid(PrivateInventorySlot.COIN, stack)) {
            // stack.split might reduce `stack` to an empty stack, so if we need to use any property of the item being
            // insert we need to make a reference to it it prior to insertion.
            ItemStack toInsert = stack.split(1);
            privateItems.insertItem(PrivateInventorySlot.COIN, toInsert, false);
            IPedestalUpgrade upgrade = (IPedestalUpgrade)toInsert.getItem();
            upgrade.actionOnAddedToPedestal(player, this, toInsert);
            // update();
            return true;
        } else {
            return false;
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

        //this way other things can trigger this, like the work cards...
        if(hasCoin())
        {
            if(getCoinOnPedestal().getItem() instanceof IPedestalUpgrade upgrade)
            {
                upgrade.actionOnRemovedFromPedestal(getPedestal(), getCoinOnPedestal());
            }
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

    public boolean attemptAddSpeed(ItemStack stack)
    {
        if (privateItems.isItemValid(PrivateInventorySlot.AUGMENT_TIERED_SPEED, stack)) {
            privateItems.insertItem(PrivateInventorySlot.AUGMENT_TIERED_SPEED, stack.split(1), false);
            // update();
            return true;
        } else {
            return false;
        }
    }

    public ItemStack removeSpeed(int count)
    {
        return privateItems.extractItem(PrivateInventorySlot.AUGMENT_TIERED_SPEED, count, false);
    }

    public ItemStack removeAllSpeed()
    {
        return removeSpeed(numAugmentsSpeed());
    }

    public boolean hasSpeed()
    {
        return numAugmentsSpeed() > 0;
    }

    public int numAugmentsSpeed()
    {
        return privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_TIERED_SPEED).getCount();
    }

    public ItemStack currentSpeedAugments()
    {
        return privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_TIERED_SPEED);
    }

    public boolean sameItem(ItemStack stackCompareTo, ItemStack stackIn) {
        return !stackIn.isEmpty() && stackCompareTo.is(stackIn.getItem());
    }

    public boolean canInsertSpeedAugment(ItemStack itemStack) {
        int allowedInsertAmount = AugmentTieredSpeed.getAllowedInsertAmount(itemStack);

        return allowedInsertAmount > 0 && // is an insertable augment, and
            (
                // there is no existing augment, or
                !hasSpeed() ||
                    // this matches the existing augment and there is space left
                    (sameItem(itemStack,currentSpeedAugments()) && numAugmentsSpeed() < allowedInsertAmount)
            );
    }

    public int getTicksReduced()
    {
        return AugmentTieredSpeed.getTicksReduced(currentSpeedAugments()) * numAugmentsSpeed();
    }

    public int getCurrentSpeed()
    {
        return PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get() - getTicksReduced();
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

    public boolean attemptAddCapacity(ItemStack stack)
    {
        if (privateItems.isItemValid(PrivateInventorySlot.AUGMENT_TIERED_CAPACITY, stack)) {
            privateItems.insertItem(PrivateInventorySlot.AUGMENT_TIERED_CAPACITY, stack.split(1), false);
            // update();
            return true;
        } else {
            return false;
        }
    }

    public ItemStack removeCapacity(int count)
    {
        return privateItems.extractItem(PrivateInventorySlot.AUGMENT_TIERED_CAPACITY, count, false);
    }

    public ItemStack removeAllCapacity()
    {
        return removeCapacity(numAugmentsCapacity());
    }

    public boolean hasCapacity()
    {
        return numAugmentsCapacity() > 0;
    }

    public int numAugmentsCapacity()
    {
        return privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_TIERED_CAPACITY).getCount();
    }

    public ItemStack currentCapacityAugments()
    {
        return privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_TIERED_CAPACITY);
    }

    public boolean canInsertAugmentCapacity(ItemStack itemStack)
    {
        int allowedInsertAmount = AugmentTieredCapacity.getAllowedInsertAmount(itemStack);

        return allowedInsertAmount > 0 && // is an insertable augment, and
            (
                // there is no existing augment, or
                !hasCapacity() ||
                    // this matches the existing augment and there is space left
                    (sameItem(itemStack,currentCapacityAugments()) && numAugmentsCapacity() < allowedInsertAmount)
            );
    }

    public int getItemTransferRateIncreaseFromCapacity()
    {
        return AugmentTieredCapacity.getAdditionalItemTransferRatePerItem(currentCapacityAugments()) * numAugmentsCapacity();
    }

    public int getFluidTransferRateIncreaseFromCapacity()
    {
        return AugmentTieredCapacity.getAdditionalFluidTransferRatePerItem(currentCapacityAugments()) * numAugmentsCapacity();
    }

    public int getEnergyTransferRateIncreaseFromCapacity()
    {
        return AugmentTieredCapacity.getAdditionalEnergyTransferRatePerItem(currentCapacityAugments()) * numAugmentsCapacity();
    }

    public int getXpTransferRateIncreaseFromCapacity()
    {
        return AugmentTieredCapacity.getAdditionalXpTransferRatePerItem(currentCapacityAugments()) * numAugmentsCapacity();
    }

    public int getDustTransferRateIncreaseFromCapacity()
    {
        return AugmentTieredCapacity.getAdditionalDustTransferRatePerItem(currentCapacityAugments()) * numAugmentsCapacity();
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

    public boolean attemptAddStorage(ItemStack stack)
    {
        if (privateItems.isItemValid(PrivateInventorySlot.AUGMENT_TIERED_STORAGE, stack)) {
            privateItems.insertItem(PrivateInventorySlot.AUGMENT_TIERED_STORAGE, stack.split(1), false);
            // update();
            return true;
        } else {
            return false;
        }
    }

    public ItemStack removeStorage(int count)
    {
        return privateItems.extractItem(PrivateInventorySlot.AUGMENT_TIERED_STORAGE, count, false);
    }

    public ItemStack removeAllStorage()
    {
        return removeStorage(numAugmentsStorage());
    }

    public boolean hasStorage()
    {
        return numAugmentsStorage() > 0;
    }

    public int numAugmentsStorage()
    {
        return privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_TIERED_STORAGE).getCount();
    }

    public ItemStack currentStorageAugments()
    {
        return privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_TIERED_STORAGE);
    }

    public boolean canInsertAugmentStorage(ItemStack itemStack)
    {
        int allowedInsertAmount = AugmentTieredStorage.getAllowedInsertAmount(itemStack);

        return allowedInsertAmount > 0 && // is an insertable augment, and
            (
                // there is no existing augment, or
                !hasStorage() ||
                    // this matches the existing augment and there is space left
                    (sameItem(itemStack,currentStorageAugments()) && numAugmentsStorage() < allowedInsertAmount)
            );
    }

    public int getItemSlotIncreaseFromStorage()
    {
        return AugmentTieredStorage.getAdditionalItemStoragePerItem(currentStorageAugments()) * numAugmentsStorage();
    }

    public int getFluidAmountIncreaseFromStorage()
    {
        return AugmentTieredStorage.getAdditionalFluidStoragePerItem(currentStorageAugments()) * numAugmentsStorage();
    }

    public int getEnergyAmountIncreaseFromStorage()
    {
        return AugmentTieredStorage.getAdditionalEnergyStoragePerItem(currentStorageAugments()) * numAugmentsStorage();
    }

    public int getXpLevelAmountIncreaseFromStorage()
    {
        return AugmentTieredStorage.getAdditionalXpStoragePerItem(currentStorageAugments()) * numAugmentsStorage();
    }

    public int getDustAmountIncreaseFromStorage()
    {
        return AugmentTieredStorage.getAdditionalDustStoragePerItem(currentStorageAugments()) * numAugmentsStorage();
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

    public boolean attemptAddRange(ItemStack stack)
    {
        if (privateItems.isItemValid(PrivateInventorySlot.AUGMENT_TIERED_RANGE, stack)) {
            privateItems.insertItem(PrivateInventorySlot.AUGMENT_TIERED_RANGE, stack.split(1), false);
            // update();
            return true;
        } else {
            return false;
        }
    }

    public ItemStack removeRange(int count)
    {
        return privateItems.extractItem(PrivateInventorySlot.AUGMENT_TIERED_RANGE, count, false);
    }

    public ItemStack removeAllRange()
    {
        return removeRange(numAugmentsRange());
    }

    public boolean hasRange()
    {
        return numAugmentsRange() > 0;
    }

    public int numAugmentsRange()
    {
        return privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_TIERED_RANGE).getCount();
    }

    public ItemStack currentRangeAugments()
    {
        return privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_TIERED_RANGE);
    }

    public boolean canInsertAugmentRange(ItemStack itemStack)
    {
        int allowedInsertAmount = AugmentTieredRange.getAllowedInsertAmount(itemStack);

        return allowedInsertAmount > 0 && // is an insertable augment, and
            (
                // there is no existing augment, or
                !hasRange() ||
                    // this matches the existing augment and there is space left
                    (sameItem(itemStack,currentRangeAugments()) && numAugmentsRange() < allowedInsertAmount)
            );
    }

    public int getRangeIncrease()
    {
        return AugmentTieredRange.getRangeIncreasePerItem(currentRangeAugments()) * numAugmentsRange();
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

    public boolean attemptAddTool(ItemStack stack)
    {
        if (privateItems.isItemValid(PrivateInventorySlot.TOOL, stack)) {
            privateItems.insertItem(PrivateInventorySlot.TOOL, stack.split(1), false);
            return true;
        } else {
            return false;
        }
    }

    public ItemStack removeTool(int count)
    {
        return privateItems.extractItem(PrivateInventorySlot.TOOL, count, false);
    }

    public ItemStack removeAllTool()
    {
        return removeTool(privateItems.getStackInSlot(PrivateInventorySlot.TOOL).getCount());
    }

    public boolean hasTool()
    {
        return !privateItems.getStackInSlot(PrivateInventorySlot.TOOL).isEmpty();
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

    public boolean damageTool(ItemStack stackTool, int damageAmount, boolean simulate)
    {
        if(stackTool.isDamageableItem())
        {
            if((stackTool.getMaxDamage() - stackTool.getDamageValue())>damageAmount)
            {
                if(!simulate)
                {
                    if(stackTool.isDamageableItem())
                    {
                        int newDamageAmount = stackTool.getDamageValue() + damageAmount;
                        stackTool.setDamageValue(newDamageAmount);
                        return true;
                    }
                }
                return true;
            }
        }

        return false;
    }

    public ItemStack getActualToolStack()
    {
        return privateItems.getStackInSlot(PrivateInventorySlot.TOOL);
    }

    public ItemStack getToolStack()
    {
        ItemStack toolInPed = privateItems.getStackInSlot(PrivateInventorySlot.TOOL);
        ItemStack coinTool = ItemStack.EMPTY;
        if(hasCoin())
        {
            if(getCoinOnPedestal().getItem() instanceof ItemUpgradeBase upgrade)
            {
                coinTool = upgrade.getUpgradeDefaultTool();
            }
        }
        return (!toolInPed.isEmpty())?(toolInPed):(coinTool);
    }

    public boolean canInsertTool(ItemStack tool)
    {
        return isAllowedTool(tool) && // is an allowed tool, and
            !hasTool(); // there is no existing tool
    }

    /*============================================================================
    ==============================================================================
    ===========================      TOOL   END      =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================     Robin START     ==============================
    ==============================================================================
    ============================================================================*/

    public boolean attemptAddRRobin(ItemStack stack)
    {
        if (privateItems.isItemValid(PrivateInventorySlot.AUGMENT_ROUNDROBIN, stack)) {
            privateItems.insertItem(PrivateInventorySlot.AUGMENT_ROUNDROBIN, stack.split(1), false);
            // update();
            return true;
        } else {
            return false;
        }
    }

    public ItemStack removeRRobin()
    {
        return privateItems.extractItem(PrivateInventorySlot.AUGMENT_ROUNDROBIN, 1, false);
    }

    public boolean hasRRobin()
    {
        return !privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_ROUNDROBIN).isEmpty();
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

    public boolean attemptAddRenderAugment(ItemStack stack)
    {
        if (privateItems.isItemValid(PrivateInventorySlot.AUGMENT_RENDERDIFFUSER, stack)) {
            privateItems.insertItem(PrivateInventorySlot.AUGMENT_RENDERDIFFUSER, stack.split(1), false);
            // update();
            return true;
        } else {
            return false;
        }
    }

    public ItemStack removeRenderAugment()
    {
        return privateItems.extractItem(PrivateInventorySlot.AUGMENT_RENDERDIFFUSER, 1, false);
    }

    public boolean hasRenderAugment()
    {
        return !privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_RENDERDIFFUSER).isEmpty();
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

    public int  getRendererType()
    {
        // 0 - No Particles
        // 1 - No Render Item
        // 2 - No Render Upgrade
        // 3 - No Particles/No Render Item
        // 4 - No Particles/No Render Upgrade
        // 5 - No Render Item/No Render Upgrade
        // 6 - No Particles/No Render Item/No Render Upgrade
        // 7 - No Augment exists and thus all rendering is fine.
        if(hasRenderAugment())
        {
            return AugmentRenderDiffuser.getAugmentMode(privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_RENDERDIFFUSER));
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

    public boolean attemptAddNoCollide(ItemStack stack)
    {
        if (privateItems.isItemValid(PrivateInventorySlot.AUGMENT_NOCOLLIDE, stack)) {
            privateItems.insertItem(PrivateInventorySlot.AUGMENT_NOCOLLIDE, stack.split(1), false);
            // update();
            return true;
        } else {
            return false;
        }
    }

    public ItemStack removeNoCollide()
    {
        return privateItems.extractItem(PrivateInventorySlot.AUGMENT_NOCOLLIDE, 1, false);
    }

    public boolean hasNoCollide()
    {
        return !privateItems.getStackInSlot(PrivateInventorySlot.AUGMENT_NOCOLLIDE).isEmpty();
    }

    /*============================================================================
    ==============================================================================
    ===========================    NO COLLIDE END   ==============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    =========================== PEDESTALS HUD START ==============================
    ==============================================================================
    ============================================================================*/

    public List<String> getHudLog()
    {
        List<String> messages = new ArrayList<>();

        if(hasCoin())
        {
            if(getCoinOnPedestal().getItem() instanceof ItemUpgradeBase upgrade)
            {
                return upgrade.getUpgradeHUD(this);
            }
        }

        return messages;
    }

    /*============================================================================
    ==============================================================================
    ===========================  PEDESTALS HUD END  ==============================
    ==============================================================================
    ============================================================================*/

    public boolean canSendToPedestal(BasePedestalBlockEntity pedestal)
    {
        return level != null &&
            level.isLoaded(pedestal.getPos()) &&
            !isPedestalBlockPowered(pedestal);
    }











    //The actual transfer methods for items
    public boolean sendItemsToPedestal(BlockPos posReceiver, List<ItemStack> itemStacksIncoming) {
        if (itemStacksIncoming.size() > 0 && level != null && level.getBlockEntity(posReceiver) instanceof BasePedestalBlockEntity receiverPedestal) {
            return receiverPedestal.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).map(receiverHandler -> {
                for (ItemStack stack: itemStacksIncoming) {
                    ItemStack insertedStackSimulation = receiverPedestal.addItemStack(stack, true);
                    int countToSend = Math.min(getItemTransferRate(), stack.getCount() - insertedStackSimulation.getCount());
                    if (countToSend > 0 && receiverHandler.isItemValid(0, stack)) {
                        ItemStack stackToRemove = stack.copy();
                        stackToRemove.setCount(countToSend);
                        receiverPedestal.addItemStack(stackToRemove, false);
                        removeItemStack(stackToRemove, false);
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }

    public boolean sendFluidsToPedestal(BlockPos posReceiver, FluidStack fluidStackIncoming) {
        if(!fluidStackIncoming.isEmpty() && level != null && level.getBlockEntity(posReceiver) instanceof BasePedestalBlockEntity receiverPedestal) {
            return receiverPedestal.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).map(receiverHandler -> {
                if (receiverHandler.isFluidValid(0, fluidStackIncoming)) {
                    int insertedStackSimulation = receiverHandler.fill(fluidStackIncoming, IFluidHandler.FluidAction.SIMULATE);
                    int countToSend = Math.min(getFluidTransferRate(), insertedStackSimulation);
                    if (countToSend > 0) {
                        FluidStack a = new FluidStack(fluidStackIncoming, countToSend);
                        receiverHandler.fill(a, IFluidHandler.FluidAction.EXECUTE);
                        removeFluid(countToSend, IFluidHandler.FluidAction.EXECUTE);
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }

    public boolean sendEnergyToPedestal(BlockPos posReceiver, int energyIncoming)
    {
        if (energyIncoming > 0 && level != null && level.getBlockEntity(posReceiver) instanceof BasePedestalBlockEntity receiverPedestal) {
            return receiverPedestal.getCapability(ForgeCapabilities.ENERGY, Direction.UP).map(receiverHandler -> {
                if(receiverHandler.canReceive()) {
                    int insertedStackSimulation = receiverHandler.receiveEnergy(energyIncoming, true);
                    if (insertedStackSimulation > 0) {
                        int countToSend = Math.min(getEnergyTransferRate(), insertedStackSimulation);
                        if(countToSend > 0) {
                            removeEnergy(countToSend, false);
                            receiverHandler.receiveEnergy(countToSend, false);
                            return true;
                        }
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }

    public boolean sendExperienceToPedestal(BlockPos posReceiver, int experienceIncoming) {
        if (experienceIncoming > 0 && level != null && level.getBlockEntity(posReceiver) instanceof BasePedestalBlockEntity receiverPedestal) {
            return receiverPedestal.getCapability(CapabilityExperience.EXPERIENCE, Direction.UP).map(receiverHandler -> {
                if (receiverHandler.canReceive()) {
                    int insertedStackSimulation = receiverHandler.receiveExperience(experienceIncoming, true);
                    if (insertedStackSimulation > 0) {
                        int countToSend = Math.min(getExperienceTransferRate(), insertedStackSimulation);
                        if (countToSend > 0) {
                            removeExperience(countToSend, false);
                            receiverHandler.receiveExperience(countToSend, false);
                            return true;
                        }
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }

    public boolean sendDustToPedestal(BlockPos posReceiver, DustMagic dustIncoming) {
        if (!dustIncoming.isEmpty() && level != null && level.getBlockEntity(posReceiver) instanceof BasePedestalBlockEntity receiverPedestal) {
            return receiverPedestal.getCapability(CapabilityDust.DUST_HANDLER, Direction.UP).map(receiverHandler -> {
                if (receiverHandler.isDustValid(0, dustIncoming)) {
                    int insertedStackSimulation = receiverHandler.fill(dustIncoming, IDustHandler.DustAction.SIMULATE);
                    if (insertedStackSimulation > 0) {
                        int countToSend = Math.min(getFluidTransferRate(), insertedStackSimulation);
                        if (countToSend > 0) {
                            removeDust(countToSend, IDustHandler.DustAction.EXECUTE);
                            receiverHandler.fill(new DustMagic(dustIncoming.getDustColor(), countToSend), IDustHandler.DustAction.EXECUTE);
                            update();
                            return true;
                        }
                    }
                }
                return false;
            }).orElse(false);
        } else {
            return false;
        }
    }

    private boolean transferActionImpl(List<BlockPos> view, int startIndex) {
        boolean hasSent = false;
        int numScanned = 0;
        for (Iterator<BlockPos> it = view.iterator(); it.hasNext(); ++numScanned) {
            BlockPos posReceiver = it.next();
            if(level.getBlockEntity(posReceiver) instanceof BasePedestalBlockEntity pedestal) {
                if(isPedestalInRange(posReceiver))
                {
                    if(canSendToPedestal(pedestal)) {
                        if(sendItemsToPedestal(posReceiver,getItemStacks())) { hasSent = true; }
                        if(sendFluidsToPedestal(posReceiver,getStoredFluid())) { hasSent = true; }
                        if(sendEnergyToPedestal(posReceiver,getStoredEnergy())) { hasSent = true; }
                        if(sendExperienceToPedestal(posReceiver,getStoredExperience())) { hasSent = true; }
                        if(sendDustToPedestal(posReceiver,getStoredDust())) { hasSent = true; }

                        if(hasSent) {
                            if (canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_BEAM,posReceiver.getX(),posReceiver.getY(),posReceiver.getZ(),getPos().getX(),getPos().getY(),getPos().getZ()));
                            if (hasRRobin()) setStoredValueForUpgrades(startIndex + numScanned + 1);
                            return true;
                        }
                    }
                }
            } else {
                it.remove();
                update();
            }
        }
        return false;
    }

    public void transferAction() {
        if(!linkedPedestals.isEmpty()) {
            int scanStart = (hasRRobin()) ? getStoredValueForUpgrades(): 0;
            if (scanStart >= linkedPedestals.size()) scanStart = 0; // handle rRobin looping behavior (as well as stale data)

            // `List.subList` provides a view of the underlying collection (i.e. mutations to it impact the actual collection,
            // and it's as performant as using the actual collection). We leverage this and an *Impl function as we might have
            // to scan the whole collection but don't always start at the first element (in some cases due to the rRobin augment
            // existing).
            if (!transferActionImpl(linkedPedestals.subList(scanStart, linkedPedestals.size()), scanStart) && scanStart > 0) {
                transferActionImpl(linkedPedestals.subList(0, scanStart), 0);
            }
        }
    }

    int pedTicker = 0;

    @Override
    public void tick() {
        super.tick();
        if(!level.isClientSide() &&level.isAreaLoaded(getPos(),1))
        {
            pedTicker++;
            //if (pedTicker%getOperationSpeed() == 0) {
            int configSpeed = PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get();
            int speed = configSpeed;
            if(hasSpeed())speed = PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get() - getTicksReduced();
            //Make sure speed has at least a value of 1
            if(speed<=0)speed = 1;
            if (pedTicker%speed == 0) {

                if(!isPedestalBlockPowered(getPedestal()))
                {
                    if(getNumLinkedPedestals() > 0) { transferAction(); }
                }
                //make sure we dont go over max int limit, regardless of config
                if(pedTicker >= maxRate-1){pedTicker=0;}
            }

            if(hasCoin())
            {
                Item coinInPed = getCoinOnPedestal().getItem();
                if(coinInPed instanceof IPedestalUpgrade upgrade) {
                    upgrade.updateAction(level,this);

                    if(hasNoCollide())
                    {
                        upgrade.actionOnCollideWithBlock(this);
                    }
                }

            }

            if(canSpawnParticles())
            {
                BlockPos posOrientated = MowLibBlockPosUtils.getPosBelowBlockEntity(level,getPos(),0);
                if(getRenderRange() && pedTicker%10 == 0){
                    MowLibPacketHandler.sendToNearby(level,getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,getPos().getX(),getPos().getY()+1.0D,getPos().getZ(),0,0,0));
                }

                if(getRenderRangeUpgrade() && pedTicker%10 == 0){
                    MowLibPacketHandler.sendToNearby(level,getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,getPos().getX(),getPos().getY()+1.0D,getPos().getZ(),50,50,50));
                }

                if(pedTicker%40 == 0){if(this.hasEnergy()){MowLibPacketHandler.sendToNearby(level,posOrientated,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,posOrientated.getX()+0.25D,posOrientated.getY(),posOrientated.getZ()+0.25D,255,0,0));}}
                if(pedTicker%40 == 0){if(this.hasExperience()){MowLibPacketHandler.sendToNearby(level,posOrientated,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,posOrientated.getX()+0.75D,posOrientated.getY(),posOrientated.getZ()+0.75D,0,255,0));}}
                if(pedTicker%40 == 0){if(this.hasFluid()){MowLibPacketHandler.sendToNearby(level,posOrientated,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,posOrientated.getX()+0.75D,posOrientated.getY(),posOrientated.getZ()+0.25D,0,0,255));}}
                if(References.isDustLoaded())
                {
                    if(pedTicker%40 == 0 && !isPedestalBlockPowered(getPedestal())){if(this.hasDust()){MowLibPacketHandler.sendToNearby(level,posOrientated,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,posOrientated.getX()+0.25D,posOrientated.getY(),posOrientated.getZ()+0.75D,178,0,255));}}
                }
            }

        }
    }

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
        CompoundTag invPrivateTag = p_155245_.getCompound("inv_private");
        privateItems.deserializeNBT(invPrivateTag);

        this.storedValueForUpgrades = p_155245_.getInt("storedUpgradeValue");
        this.showRenderRange = p_155245_.getBoolean("showRenderRange");

        //new value added in 0.2.30
        if(p_155245_.contains("showRenderRangeUpgrade"))
        {
            this.showRenderRangeUpgrade = p_155245_.getBoolean("showRenderRangeUpgrade");
        }
        else this.showRenderRangeUpgrade = false;

        int[] storedIX = p_155245_.getIntArray("intArrayXPos");
        int[] storedIY = p_155245_.getIntArray("intArrayYPos");
        int[] storedIZ = p_155245_.getIntArray("intArrayZPos");

        for(int i=0;i<storedIX.length;i++)
        {
            BlockPos gotPos = new BlockPos(storedIX[i],storedIY[i],storedIZ[i]);
            linkedPedestals.add(gotPos);
        }
    }


    @Override
    public CompoundTag save(CompoundTag p_58888_) {
        p_58888_.put("inv_private", privateItems.serializeNBT());

        p_58888_.putInt("storedUpgradeValue",storedValueForUpgrades);
        p_58888_.putBoolean("showRenderRange",showRenderRange);
        p_58888_.putBoolean("showRenderRangeUpgrade",showRenderRangeUpgrade);

        List<Integer> storedX = new ArrayList<Integer>();
        List<Integer> storedY = new ArrayList<Integer>();
        List<Integer> storedZ = new ArrayList<Integer>();

        for(int i=0;i<getNumLinkedPedestals();i++)
        {
            storedX.add(linkedPedestals.get(i).getX());
            storedY.add(linkedPedestals.get(i).getY());
            storedZ.add(linkedPedestals.get(i).getZ());
        }

        p_58888_.putIntArray("intArrayXPos",storedX);
        p_58888_.putIntArray("intArrayYPos",storedY);
        p_58888_.putIntArray("intArrayZPos",storedZ);

        return super.save(p_58888_);
    }

    //This is needed so that the render boxes show up when the player is just out of normal render range of the pedestal
    @Override
    public AABB getRenderBoundingBox() {
        int range = getLinkingRange();
        if(hasCoin())
        {
            if(getCoinOnPedestal().getItem() instanceof ItemUpgradeBase upgrade)
            {
                int upgradeRange = upgrade.getUpgradeWorkRange(getCoinOnPedestal());
                if(upgradeRange>range)range = upgradeRange;
            }
        }


        AABB aabb = new AABB(getPos().getX() - range, getPos().getY() - range, getPos().getZ() - range,getPos().getX() + range, getPos().getY() + range, getPos().getZ() + range);
        return aabb;
    }
}
