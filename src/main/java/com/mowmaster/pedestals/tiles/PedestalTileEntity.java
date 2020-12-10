package com.mowmaster.pedestals.tiles;

import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.crafting.CraftingPedestals;
import com.mowmaster.pedestals.item.ItemPedestalUpgrades;
import com.mowmaster.pedestals.item.pedestalUpgrades.*;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.*;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.references.Reference.MODID;

//
public class PedestalTileEntity extends TileEntity implements IInventory, ITickableTileEntity, IEnergyStorage {

    private LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler);
    private LazyOptional<IItemHandler> privateHandler = LazyOptional.of(this::createHandlerPedestalPrivate);
    private LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(this::createHandlerEnergy);

    private static final int[] SLOTS_ALLSIDES = new int[] {0};

    private int storedValueForUpgrades = 0;
    private boolean boolLight = false;
    private final List<BlockPos> storedLocations = new ArrayList<BlockPos>();
    private LockCode lockCode;

    public PedestalTileEntity()
    {
        super(PEDESTALTYPE);
        this.lockCode = LockCode.EMPTY_CODE;
    }

    @Override
    public ItemStack removeStackFromSlot(int i) {
        return removeItem(i);
    }

    @Override
    public boolean isEmpty() {
        return hasItem();
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        IItemHandler h = handler.orElse(null);
        return h.getStackInSlot(i);
    }

    @Override
    public int getSizeInventory() {
        IItemHandler h = handler.orElse(null);
        return h.getSlots();
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity playerEntity) {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int i, int i1) {
        return removeItem(i);
    }

    @Override
    public void clear() {
        remove();
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemStack) {
        IItemHandler h = handler.orElse(null);
        h.insertItem(i,itemStack,false);
    }

    //Above this is mostly IInventory stuff needed for block drops


    /**********************************
     **********************************
     **  ENERGY IMPLIMENTION START  ***
     **********************************
     *********************************/

    public IEnergyStorage createHandlerEnergy() {
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {

                if((hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeEnergyImport))
                {
                    ItemUpgradeBaseEnergy itemE =  (ItemUpgradeBaseEnergy)getCoinOnPedestal().getItem();
                    if(itemE.addEnergy(getCoinOnPedestal(),maxReceive,simulate))
                    {
                        //Return input power if it can be insert???
                        return maxReceive;
                    }
                    //Else return availablespace???
                    else return itemE.availableEnergySpaceInCoin(getCoinOnPedestal());
                }

                return 0;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                if((hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeEnergyExport))
                {
                    ItemUpgradeBaseEnergy itemE =  (ItemUpgradeBaseEnergy)getCoinOnPedestal().getItem();
                    if(itemE.removeEnergy(getCoinOnPedestal(),maxExtract,simulate))
                    {
                        //Return output power if it can be taken???
                        return maxExtract;
                    }
                    //Else return currently stored energy???
                    else return itemE.getEnergyStored(getCoinOnPedestal());
                }
                return 0;
            }

            @Override
            public int getEnergyStored() {
                if(hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeBaseEnergy)
                {
                    ItemUpgradeBaseEnergy itemE =  (ItemUpgradeBaseEnergy)getCoinOnPedestal().getItem();
                    return itemE.getEnergyStored(getCoinOnPedestal());
                }
                else return 0;
            }

            @Override
            public int getMaxEnergyStored() {
                if(hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeBaseEnergy)
                {
                    ItemUpgradeBaseEnergy itemE =  (ItemUpgradeBaseEnergy)getCoinOnPedestal().getItem();
                    return itemE.getEnergyBuffer(getCoinOnPedestal());
                }
                else return 0;
            }

            @Override
            public boolean canExtract() {
                return (hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeEnergyExport);
            }

            @Override
            public boolean canReceive() {
                return (hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeEnergyImport);
            }
        };
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {

        if((hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeEnergyImport))
        {
            ItemUpgradeBaseEnergy itemE =  (ItemUpgradeBaseEnergy)getCoinOnPedestal().getItem();
            if(itemE.addEnergy(getCoinOnPedestal(),maxReceive,simulate))
            {
                //Return input power if it can be insert???
                return maxReceive;
            }
        }

        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if((hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeEnergyExport))
        {
            ItemUpgradeBaseEnergy itemE =  (ItemUpgradeBaseEnergy)getCoinOnPedestal().getItem();
            if(itemE.removeEnergy(getCoinOnPedestal(),maxExtract,simulate))
            {
                //Return output power if it can be taken???
                return maxExtract;
            }
        }
        return 0;
    }

    @Override
    public int getEnergyStored() {
        if(hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeBaseEnergy)
        {
            ItemUpgradeBaseEnergy itemE =  (ItemUpgradeBaseEnergy)getCoinOnPedestal().getItem();
            return itemE.getEnergyStored(getCoinOnPedestal());
        }
        else return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        if(hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeBaseEnergy)
        {
            ItemUpgradeBaseEnergy itemE =  (ItemUpgradeBaseEnergy)getCoinOnPedestal().getItem();
            return itemE.getEnergyBuffer(getCoinOnPedestal());
        }
        else return 0;
    }

    @Override
    public boolean canExtract() {
        return (hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeEnergyExport);
    }

    @Override
    public boolean canReceive() {
        return (hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeEnergyImport);
    }

    public int getMaxEnergyReceive()
    {
        int est = this.getMaxEnergyStored() - this.getEnergyStored();
        return (est > 0)?(est):(0);
    }

    public int getMaxEnergyExtract()
    {
        return this.getEnergyStored();
    }

    /**********************************
     **********************************
     ***  ENERGY IMPLIMENTION END   ***
     **********************************
     *********************************/

    public void update()
    {
        this.markDirty();
        this.world.notifyBlockUpdate(pos,getBlockState(),getBlockState(),2);
        /*public static final int NOTIFY_NEIGHBORS = 1;
        public static final int BLOCK_UPDATE = 2;
        public static final int NO_RERENDER = 4;
        public static final int RERENDER_MAIN_THREAD = 8;
        public static final int UPDATE_NEIGHBORS = 16;
        public static final int NO_NEIGHBOR_DROPS = 32;
        public static final int IS_MOVING = 64;
        public static final int DEFAULT = 3;
        public static final int DEFAULT_AND_RERENDER = 11;*/
    }

    public IItemHandler createHandler() {
        return new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                update();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                //System.out.println("Is Valid");
                if (slot == 0) return true;
                return false;
            }

            @Override
            public int getSlots() {
                return 1;
            }

            @Override
            protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
                ItemStack coinOnPedestal = getCoinOnPedestal();
                if(hasCoin() && coinOnPedestal.getItem() instanceof ItemUpgradeBase)
                {
                    ItemUpgradeBase IUB = (ItemUpgradeBase)getCoinOnPedestal().getItem();
                    return IUB.canAcceptCount(world,pos,getItemInPedestal(),stack);
                }
                else return super.getStackLimit(slot, stack);
            }

            @Override
            public int getSlotLimit(int slot) {

                ItemStack coinOnPedestal = getCoinOnPedestal();
                if(hasCoin() && coinOnPedestal.getItem() instanceof ItemUpgradeBase)
                {
                    ItemUpgradeBase IUB = (ItemUpgradeBase)getCoinOnPedestal().getItem();
                    //System.out.println(IUB.customSlotLimit(getTile(),stack));
                    if(IUB.customSlotLimit(getTile())!=-1)
                    {
                        return IUB.customSlotLimit(getTile());
                    }
                }

                return super.getSlotLimit(slot);

            }

            @Nonnull
            @Override
            public ItemStack getStackInSlot(int slot) {
                if(slot == -1)
                {
                    return super.getStackInSlot(0);
                }
                else if(hasCoin())
                {
                    if(getCoinOnPedestal().getItem() instanceof ItemUpgradeBase)
                    {
                        ItemUpgradeBase IUB = (ItemUpgradeBase)getCoinOnPedestal().getItem();
                        if(!IUB.customStackInSlot(getTile(),super.getStackInSlot(slot)).getItem().equals(Items.COMMAND_BLOCK))
                        {
                            return IUB.customStackInSlot(getTile(),super.getStackInSlot(slot));
                        }
                    }
                }
                //System.out.println(super.getStackInSlot(slot));
                return super.getStackInSlot(slot);
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                //System.out.println("AttemptInsert");
                if(slot==-1)
                {
                    return super.insertItem(0, stack, simulate);
                }
                else if(hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeBase)
                {
                    ItemUpgradeBase IUB = (ItemUpgradeBase)getCoinOnPedestal().getItem();
                    //System.out.println("Coin: "+IUB.customInsertItem(getTile(),stack, true).getItem().getName().toString());
                    if(!IUB.customInsertItem(getTile(),stack, true).getItem().equals(Items.COMMAND_BLOCK))
                    {
                        return IUB.customInsertItem(getTile(),stack, simulate);
                    }
                }
                //System.out.println("No Coin: "+super.insertItem(slot, stack, simulate));
                return super.insertItem(slot, stack, simulate);
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {

                if(slot==-1)
                {
                    return super.extractItem(0, amount, simulate);
                }
                else if(hasCoin() && getCoinOnPedestal().getItem() instanceof ItemUpgradeBase)
                {
                    ItemUpgradeBase IUB = (ItemUpgradeBase)getCoinOnPedestal().getItem();
                    if(!IUB.customExtractItem(getTile(),amount, true).getItem().equals(Items.COMMAND_BLOCK))
                    {
                        return IUB.customExtractItem(getTile(),amount, simulate);
                    }
                    else
                    {
                        return IUB.canSendItem(getTile())?(super.extractItem(slot, amount, simulate)):(ItemStack.EMPTY);
                    }
                }
                else return super.extractItem(slot, amount, simulate);
            }
        };
    }

    public PedestalTileEntity getTile()
    {
        return this;
    }

    private IItemHandler createHandlerPedestalPrivate() {
        return new ItemStackHandler(5) {
            @Override
            protected void onContentsChanged(int slot) {
                update();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot == 0 && stack.getItem() instanceof ItemUpgradeBase && !hasCoin()) return true;
                if (slot == 1 && stack.getItem().equals(Items.GLOWSTONE) && !hasLight()) return true;
                if (slot == 2 && stack.getItem().equals(ItemPedestalUpgrades.SPEED) && getSpeed()<5) return true;
                if (slot == 3 && stack.getItem().equals(ItemPedestalUpgrades.CAPACITY) && getCapacity()<5) return true;
                if (slot == 4 && stack.getItem().equals(ItemPedestalUpgrades.RANGE) && getRange()<5) return true;
                return false;
            }

            @Override
            public int getSlots() {
                return 5;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        if (cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

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

    public int getStoredValueForUpgrades()
    {
        return storedValueForUpgrades;
    }
    public void setStoredValueForUpgrades(int value)
    {
        storedValueForUpgrades = value;
        update();
    }
    public int getPedestalTransferRange(){return getRange();}

    public boolean hasItem()
    {
        IItemHandler h = handler.orElse(null);
        if(h.getStackInSlot(0).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public boolean hasCoin()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(0).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public boolean hasLight()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(1).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public boolean hasSpeed()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(2).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public boolean hasCapacity()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(3).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public boolean hasRange()
    {
        IItemHandler ph = privateHandler.orElse(null);
        if(ph.getStackInSlot(4).isEmpty())
        {
            return false;
        }
        else  return true;
    }

    public ItemStack getItemInPedestal()
    {
        IItemHandler h = handler.orElse(null);
        if(hasItem())
        {
            return h.getStackInSlot(0);
        }
        else return ItemStack.EMPTY;
    }

    public ItemStack getItemInPedestalOverride()
    {
        IItemHandler h = handler.orElse(null);
        return h.getStackInSlot(-1);
    }

    public ItemStack getCoinOnPedestal()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(0);
        /*if(hasCoin())
        {
            return ph.getStackInSlot(0);
        }
        else return ItemStack.EMPTY;*/
    }

    public int getSpeed()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(2).getCount();
    }

    public int getCapacity()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(3).getCount();
    }

    public int getRange()
    {
        IItemHandler ph = privateHandler.orElse(null);
        return ph.getStackInSlot(4).getCount();
    }

    public ItemStack setItemInPedestal(ItemStack itemToSet)
    {
        IItemHandler h = handler.orElse(null);
        ItemStack stack = h.insertItem(0,itemToSet,false);
        return stack;
    }

    public int getSlotSizeLimit() {
        IItemHandler h = handler.orElse(null);
        return h.getSlotLimit(0);
    }

    public ItemStack removeItem(int numToRemove) {
        IItemHandler h = handler.orElse(null);
        ItemStack stack = h.extractItem(0,numToRemove,false);
        //update();

        return stack;
    }

    public ItemStack removeItemOverride(int numToRemove) {
        IItemHandler h = handler.orElse(null);
        ItemStack stack = h.extractItem(-1,numToRemove,false);
        //update();

        return stack;
    }

    public ItemStack removeItem() {
        IItemHandler h = handler.orElse(null);
        ItemStack stack = h.extractItem(0,h.getStackInSlot(0).getCount(),false);
        //update();

        return stack;
    }

    public ItemStack removeItemOverride() {
        IItemHandler h = handler.orElse(null);
        ItemStack stack = h.extractItem(-1,h.getStackInSlot(0).getCount(),false);
        //update();

        return stack;
    }

    public ItemStack removeCoin() {
        IItemHandler ph = privateHandler.orElse(null);
        ItemStack stack = ph.extractItem(0,ph.getStackInSlot(0).getCount(),false);
        ((ItemUpgradeBase)stack.getItem()).removePlayerFromCoin(stack);
        setStoredValueForUpgrades(0);
        //update();

        return stack;
    }

    public void spawnItemStack(World worldIn, double x, double y, double z, ItemStack stack) {
        Random RANDOM = new Random();
        double d0 = (double) EntityType.ITEM.getWidth();
        double d1 = 1.0D - d0;
        double d2 = d0 / 2.0D;
        double d3 = Math.floor(x) + RANDOM.nextDouble() * d1 + d2;
        double d4 = Math.floor(y) + RANDOM.nextDouble() * d1;
        double d5 = Math.floor(z) + RANDOM.nextDouble() * d1 + d2;

        while(!stack.isEmpty()) {
            ItemEntity itementity = new ItemEntity(worldIn, d3, d4, d5, stack.split(RANDOM.nextInt(21) + 10));
            float f = 0.05F;
            itementity.setMotion(RANDOM.nextGaussian() * 0.05000000074505806D, RANDOM.nextGaussian() * 0.05000000074505806D + 0.20000000298023224D, RANDOM.nextGaussian() * 0.05000000074505806D);
            worldIn.addEntity(itementity);
        }
    }

    public void dropInventoryItems(World worldIn, BlockPos pos) {
        IItemHandler h = handler.orElse(null);
        for(int i = 0; i < h.getSlots(); ++i) {
            spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), h.getStackInSlot(-1));
        }
    }

    public void dropInventoryItemsPrivate(World worldIn, BlockPos pos) {
        IItemHandler ph = privateHandler.orElse(null);
        for(int i = 0; i < ph.getSlots(); ++i) {
            if(i==0 && hasCoin())((ItemUpgradeBase)ph.getStackInSlot(i).getItem()).removePlayerFromCoin(ph.getStackInSlot(i));
            spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), ph.getStackInSlot(i));
        }
    }

    public int getItemTransferRate()
    {
        int itemRate = 4;
        switch (getCapacity())
        {
            case 0:
                itemRate = (getCapacity()>0)?(4):(4);
                break;
            case 1:
                itemRate= (getCapacity()>0)?(8):(4);
                break;
            case 2:
                itemRate = (getCapacity()>0)?(16):(4);
                break;
            case 3:
                itemRate = (getCapacity()>0)?(32):(4);
                break;
            case 4:
                itemRate = (getCapacity()>0)?(48):(4);
                break;
            case 5:
                itemRate=(getCapacity()>0)?(64):(4);
                break;
            default: itemRate=4;
        }

        return  itemRate;
    }

    public int getOperationSpeed()
    {
        int speed = 20;
        switch (getSpeed())
        {
            case 0:
                speed = (getSpeed()>0)?(20):(20);//normal speed
                break;
            case 1:
                speed=(getSpeed()>0)?(10):(20);//2x faster
                break;
            case 2:
                speed = (getSpeed()>0)?(5):(20);;//4x faster
                break;
            case 3:
                speed = (getSpeed()>0)?(3):(20);;//6x faster
                break;
            case 4:
                speed = (getSpeed()>0)?(2):(20);;//10x faster
                break;
            case 5:
                speed=(getSpeed()>0)?(1):(20);;//20x faster
                break;
            default: speed=20;
        }

        return  speed;
    }

    public int getLinkingRange()
    {
        int range = 8;
        switch (getRange())
        {
            case 0:
                range = (getRange()>0)?(8):(8);//normal speed
                break;
            case 1:
                range=(getRange()>0)?(12):(8);//2x faster
                break;
            case 2:
                range = (getRange()>0)?(16):(8);;//4x faster
                break;
            case 3:
                range = (getRange()>0)?(32):(8);;//6x faster
                break;
            case 4:
                range = (getRange()>0)?(48):(8);;//10x faster
                break;
            case 5:
                range=(getRange()>0)?(64):(8);;//20x faster
                break;
            default: range=8;
        }
        return  range;
    }

    public boolean isPedestalInRange(PedestalTileEntity pedestalCurrent, BlockPos pedestalToBeLinked)
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

    public int getMaxStackSize(){return 64;}

    public boolean addItem(ItemStack itemFromBlock)
    {
        IItemHandler h = handler.orElse(null);
        if(hasItem())
        {
            if(doItemsMatch(itemFromBlock))
            {
                h.insertItem(0, itemFromBlock.copy(), false);
            }
        }
        else {h.insertItem(0, itemFromBlock.copy(), false);}
        //update();

        return true;
    }

    public boolean addItemOverride(ItemStack itemFromBlock)
    {
        IItemHandler h = handler.orElse(null);
        if(hasItem())
        {
            if(doItemsMatch(itemFromBlock))
            {
                h.insertItem(-1, itemFromBlock.copy(), false);
            }
        }
        else {h.insertItem(-1, itemFromBlock.copy(), false);}
        //update();

        return true;
    }

    public boolean addItem(ItemStack itemFromBlock,boolean simulate)
    {
        IItemHandler h = handler.orElse(null);
        if(hasItem())
        {
            if(doItemsMatch(itemFromBlock))
            {
                if(!simulate)
                {
                    h.insertItem(0, itemFromBlock.copy(), false);
                    //update();
                }
                return true;
            }
        }
        else
        {
            if(!simulate)
            {
                h.insertItem(0, itemFromBlock.copy(), false);
                //update();
            }
            return true;
        }

        return false;
    }

    public ItemStack addItemCustom(ItemStack itemFromBlock,boolean simulate)
    {
        IItemHandler h = handler.orElse(null);
        return h.insertItem(0, itemFromBlock.copy(), false);
    }

    public boolean addCoin(PlayerEntity player, ItemStack coinFromBlock,boolean simulate)
    {
        if(!hasCoin())
        {
            if(!simulate)
            {
                IItemHandler ph = privateHandler.orElse(null);
                ItemStack itemFromBlock = coinFromBlock.copy();
                itemFromBlock.setCount(1);
                //We know this is what the item is because of the pedestal block check
                ((ItemUpgradeBase)itemFromBlock.getItem()).setPlayerOnCoin(itemFromBlock,player);
                if(!hasCoin())ph.insertItem(0,itemFromBlock,false);
                setStoredValueForUpgrades(0);
                //update();
            }
            return true;
        }
        return false;
    }

    public boolean addSpeed(ItemStack speedUpgrade)
    {
        IItemHandler ph = privateHandler.orElse(null);
        ItemStack itemFromBlock = speedUpgrade.copy();
        itemFromBlock.setCount(1);
        if(getSpeed() < 5)
        {
            ph.insertItem(2,itemFromBlock,false);
            //update();
            return true;
        }
        else return false;
    }

    public boolean addCapacity(ItemStack capacityUpgrade)
    {
        IItemHandler ph = privateHandler.orElse(null);
        ItemStack itemFromBlock = capacityUpgrade.copy();
        itemFromBlock.setCount(1);
        if(getCapacity() < 5)
        {
            ph.insertItem(3,itemFromBlock,false);
            //update();
            return true;
        }
        else return false;
    }

    public boolean addRange(ItemStack rangeUpgrade)
    {
        IItemHandler ph = privateHandler.orElse(null);
        ItemStack itemFromBlock = rangeUpgrade.copy();
        itemFromBlock.setCount(1);
        if(getRange() < 5)
        {
            ph.insertItem(4,itemFromBlock,false);
            //update();
            return true;
        }
        else return false;
    }

    public boolean addLight()
    {
        if(hasLight())
        {
            return false;
        }
        else
        {
            boolLight = true;
            BlockState state = world.getBlockState(pos);
            boolean watered = state.get(PedestalBlock.WATERLOGGED);
            Direction dir = state.get(PedestalBlock.FACING);
            BlockState newstate = state.with(PedestalBlock.FACING,dir).with(PedestalBlock.WATERLOGGED,watered).with(PedestalBlock.LIT,true);
            IItemHandler ph = privateHandler.orElse(null);
            ph.insertItem(1,new ItemStack(Items.GLOWSTONE,1),false);
            world.notifyBlockUpdate(pos,state,newstate,3);
            world.setBlockState(pos,newstate,3);
            world.markBlockRangeForRenderUpdate(pos,state,newstate);
            //update();
            return true;
        }
    }

    public boolean addColor(ItemStack stack)
    {
        if(!hasCoin() && !hasItem() && !hasLight() && getNumberOfStoredLocations() <= 0)
        {
            int intColor = stack.getTag().getInt("color");
            BlockState replacestate = CraftingPedestals.instance().getResult(intColor);
            BlockState state = world.getBlockState(pos);
            boolean watered = state.get(PedestalBlock.WATERLOGGED);
            Direction dir = state.get(PedestalBlock.FACING);
            boolean lit = state.get(PedestalBlock.LIT);
            BlockState newstate = replacestate.with(PedestalBlock.FACING,dir).with(PedestalBlock.WATERLOGGED,watered).with(PedestalBlock.LIT,lit);
            world.notifyBlockUpdate(pos,state,newstate,3);
            world.setBlockState(pos,newstate,3);
            //update();
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean doItemsMatch(ItemStack itemStackIn)
    {
        IItemHandler h = handler.orElse(null);
        if(hasItem())
        {
            if(itemStackIn.hasTag())
            {
                CompoundNBT itemIn = itemStackIn.getTag();
                CompoundNBT itemStored = h.getStackInSlot(0).getTag();
                if(itemIn.equals(itemStored) && itemStackIn.getItem().equals(h.getStackInSlot(0).getItem()))
                {
                    return true;
                }
                else return false;
            }
            else
            {
                if(itemStackIn.getItem().equals(h.getStackInSlot(0).getItem()))
                {
                    return true;
                }
            }
        }
        else{return true;}

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
        if(world.getBlockState(pedestalToBeLinked).getBlock() instanceof PedestalBlock)
        {
            //isPedestalInRange(tileCurrent,pedestalToBeLinked);
            return true;
        }

        return false;
    }

    //Returns items available to be insert, 0 if false
    public int canAcceptItems(World worldIn, BlockPos posPedestal, ItemStack itemsIncoming)
    {
        int canAccept = 0;
        int pedestalAccept = 0;

        if(hasCoin())
        {
            Item coinInPed = this.getCoinOnPedestal().getItem();
            if(coinInPed instanceof ItemUpgradeBase)
            {
                pedestalAccept = ((ItemUpgradeBase) coinInPed).canAcceptCount(worldIn, posPedestal, getItemInPedestal(), itemsIncoming);
            }
        }

        if(getItemInPedestal().isEmpty() || getItemInPedestal().equals(ItemStack.EMPTY))
        {
            canAccept = 64;
        }
        else
        {
            if(doItemsMatch(itemsIncoming))
            {
                //Two buckets match but cant be stacked since max stack size is 1
                if(itemsIncoming.getMaxStackSize() > 1)
                {
                    //If i did this right, slot limit should default to stack max size, or custom allowed
                    if(getItemInPedestal().getCount() < getSlotSizeLimit())
                    {
                        canAccept = (getSlotSizeLimit() - getItemInPedestal().getCount());
                    }
                }
            }
        }

        if(canAccept > pedestalAccept && hasCoin())
        {
            canAccept = pedestalAccept;
        }

        return canAccept;
    }

    public boolean hasFilter(PedestalTileEntity pedestalSendingTo)
    {
        boolean returner = false;
        if(pedestalSendingTo.hasCoin())
        {
            Item coinInPed = pedestalSendingTo.getCoinOnPedestal().getItem();
            if(coinInPed instanceof ItemUpgradeBaseFilter)
            {
                returner = true;
            }
        }

        return returner;
    }

    public Boolean canSendItemInPedestal()
    {
        boolean returner = true;

        if(hasCoin())
        {
            Item coinInPed = getCoinOnPedestal().getItem();
            if(coinInPed instanceof ItemUpgradeBase)
            {
                return ((ItemUpgradeBase) coinInPed).canSendItem(this);
                //return false;
            }
        }

        return returner;
    }

    private boolean canSendToPedestal(BlockPos pedestalToSendTo)
    {
        boolean returner = false;

        //Method to check if we can send items FROM this pedestal???
        if(canSendItemInPedestal())
        {
            //Check if Block is Loaded in World
            if(world.isAreaLoaded(pedestalToSendTo,1))
            {
                //If block ISNT powered
                if(!world.isBlockPowered(pedestalToSendTo))
                {
                    //Make sure its a pedestal before getting the tile
                    if(world.getBlockState(pedestalToSendTo).getBlock() instanceof PedestalBlock)
                    {
                        //Make sure it is still part of the right network
                        if(canLinkToPedestalNetwork(pedestalToSendTo))
                        {
                            //Get the tile before checking other things
                            if(world.getTileEntity(pedestalToSendTo) instanceof PedestalTileEntity)
                            {
                                PedestalTileEntity tilePedestalToSendTo = (PedestalTileEntity)world.getTileEntity(pedestalToSendTo);

                                //Checks if pedestal is empty or if not then checks if items match and how many can be insert
                                if(tilePedestalToSendTo.canAcceptItems(world,pedestalToSendTo,getItemInPedestal()) > 0)
                                {
                                    //Check if it has filter, if not return true
                                    if(hasFilter(tilePedestalToSendTo))
                                    {
                                        Item coinInPed = tilePedestalToSendTo.getCoinOnPedestal().getItem();
                                        if(coinInPed instanceof ItemUpgradeBaseFilter)
                                        {
                                            //Already checked if its a filter, so now check if it can accept items.
                                            if(((ItemUpgradeBaseFilter) coinInPed).canAcceptItem(world,pedestalToSendTo,getItemInPedestal()))
                                            {
                                                returner = true;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        returner = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return returner;
    }

    public boolean canSendToPedestal(BlockPos pedestalToSendTo, ItemStack itemStackIncoming)
    {
        boolean returner = false;

        //Method to check if we can send items FROM this pedestal???
        if(canSendItemInPedestal())
        {
            //Check if Block is Loaded in World
            if(world.isAreaLoaded(pedestalToSendTo,1))
            {
                //If block ISNT powered
                if(!world.isBlockPowered(pedestalToSendTo))
                {
                    //Make sure its a pedestal before getting the tile
                    if(world.getBlockState(pedestalToSendTo).getBlock() instanceof PedestalBlock)
                    {
                        //Make sure it is still part of the right network
                        if(canLinkToPedestalNetwork(pedestalToSendTo))
                        {
                            //Get the tile before checking other things
                            if(world.getTileEntity(pedestalToSendTo) instanceof PedestalTileEntity)
                            {
                                PedestalTileEntity tilePedestalToSendTo = (PedestalTileEntity)world.getTileEntity(pedestalToSendTo);

                                //Checks if pedestal is empty or if not then checks if items match and how many can be insert
                                if(tilePedestalToSendTo.canAcceptItems(world,pedestalToSendTo,itemStackIncoming) > 0)
                                {
                                    //Check if it has filter, if not return true
                                    if(hasFilter(tilePedestalToSendTo))
                                    {
                                        Item coinInPed = tilePedestalToSendTo.getCoinOnPedestal().getItem();
                                        if(coinInPed instanceof ItemUpgradeBaseFilter)
                                        {
                                            //Already checked if its a filter, so now check if it can accept items.
                                            if(((ItemUpgradeBaseFilter) coinInPed).canAcceptItem(world,pedestalToSendTo,itemStackIncoming))
                                            {
                                                returner = true;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        returner = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return returner;
    }

    public void sendItemsToPedestal(BlockPos pedestalToSendTo)
    {
        if(world.getTileEntity(pedestalToSendTo) instanceof PedestalTileEntity)
        {
            PedestalTileEntity tileToSendTo = ((PedestalTileEntity)world.getTileEntity(pedestalToSendTo));

            //Max that can be recieved
            int countToSend = tileToSendTo.canAcceptItems(world,pedestalToSendTo,getItemInPedestal());
            ItemStack copyStackToSend = getItemInPedestal().copy();
            //Max that is available to send
            if(copyStackToSend.getCount()<countToSend)
            {
                countToSend = copyStackToSend.getCount();
            }
            //Get max that can be sent
            if(countToSend > getItemTransferRate())
            {
                countToSend = getItemTransferRate();
            }


            if(countToSend >=1)
            {
                //Send items
                copyStackToSend.setCount(countToSend);
                removeItem(copyStackToSend.getCount());
                tileToSendTo.addItem(copyStackToSend);
                PacketHandler.sendToNearby(world,pos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR_BEAM,pedestalToSendTo.getX(),pedestalToSendTo.getY(),pedestalToSendTo.getZ(),pos.getX(),pos.getY(),pos.getZ()));
            }
        }
    }

    public void collideWithPedestal(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(!world.isRemote) {
            if(entityIn instanceof ItemEntity)
            {
               if(tilePedestal.hasCoin())
                {
                    Item coinInPed = tilePedestal.getCoinOnPedestal().getItem();
                    if(coinInPed instanceof ItemUpgradeBase)
                    {
                        ((ItemUpgradeBase) coinInPed).actionOnCollideWithBlock(world, tilePedestal, pos, state, entityIn);
                    }
                }
            }
        }
    }

    //Needed for Rendering Tile Stuff
    public boolean isBlockUnder(int x,int y,int z)
    {
        TileEntity tileEntity = world.getTileEntity(pos.add(x,y,z));
        if(tileEntity instanceof ICapabilityProvider)
        {
            return true;
        }
        return false;
    }

    int partTicker = 0;
    int impTicker = 0;
    int pedTicker = 0;
    @Override
    public void tick() {

        if(!world.isRemote)
        {
            if(world.isAreaLoaded(pos,1))
            {
                int speed = getOperationSpeed();
                if(speed<1){speed = 20;}
                //dont bother unless pedestal has items in it.
                if(!getItemInPedestal().isEmpty())
                {
                    if(!world.isBlockPowered(pos))
                    {
                        if(getNumberOfStoredLocations()>0)
                        {
                            pedTicker++;
                            if (pedTicker%speed == 0) {
                                for(int i=0; i<getNumberOfStoredLocations();i++)
                                {
                                    if(getStoredPositionAt(i) != getPos())
                                    {
                                        //check for any slots that can accept items if not then keep trying
                                        if(canSendToPedestal(getStoredPositionAt(i)))
                                        {
                                            //Once a slot is found and items transfered, stop loop(so it restarts next check)
                                            sendItemsToPedestal(getStoredPositionAt(i));
                                            break;
                                        }
                                    }
                                }
                                if(pedTicker >=20){pedTicker=0;}
                            }
                        }
                    }
                }
            }
        }
        if(world.isAreaLoaded(pos,1))
        {
            if(hasCoin())
            {
                Item coinInPed = getCoinOnPedestal().getItem();
                if(coinInPed instanceof ItemUpgradeBase)
                {
                    impTicker++;
                    ((ItemUpgradeBase) coinInPed).updateAction(this);
                    //Has to be bigger than our biggest ticker value for an upgrade, or itll reset the upgrade instance before the upgrade action can fire
                    if(impTicker >=Integer.MAX_VALUE-100){impTicker=0;}
                }
            }
        }
        if(world.isRemote)
        {
            if(hasCoin())
            {
                Item coinInPed = getCoinOnPedestal().getItem();
                if(coinInPed instanceof ItemUpgradeBase)
                {
                    partTicker++;
                    Random rand = new Random();
                    ((ItemUpgradeBase) coinInPed).onRandomDisplayTick(this,partTicker, world.getBlockState(getPos()), world, getPos(), rand);
                    if(partTicker >=Integer.MAX_VALUE-100){partTicker=0;}
                }
            }
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        CompoundNBT invTag = nbt.getCompound("inv");
        CompoundNBT invTagP = nbt.getCompound("invp");
        handler.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag));
        privateHandler.ifPresent(ph -> ((INBTSerializable<CompoundNBT>) ph).deserializeNBT(invTagP));

        this.storedValueForUpgrades = nbt.getInt("storedUpgradeValue");
        this.boolLight = nbt.getBoolean("boollight");

        int[] storedIX = nbt.getIntArray("intArrayXPos");
        int[] storedIY = nbt.getIntArray("intArrayYPos");
        int[] storedIZ = nbt.getIntArray("intArrayZPos");

        for(int i=0;i<storedIX.length;i++)
        {
            BlockPos gotPos = new BlockPos(storedIX[i],storedIY[i],storedIZ[i]);
            storedLocations.add(gotPos);
        }

        this.lockCode = LockCode.read(nbt);
    }


    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        handler.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put("inv", compound);
        });

        privateHandler.ifPresent(ph -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) ph).serializeNBT();
            tag.put("invp", compound);
        });

        tag.putInt("storedUpgradeValue",storedValueForUpgrades);
        tag.putBoolean("boollight", boolLight);

        List<Integer> storedX = new ArrayList<Integer>();
        List<Integer> storedY = new ArrayList<Integer>();
        List<Integer> storedZ = new ArrayList<Integer>();

        for(int i=0;i<getNumberOfStoredLocations();i++)
        {
            storedX.add(storedLocations.get(i).getX());
            storedY.add(storedLocations.get(i).getY());
            storedZ.add(storedLocations.get(i).getZ());
        }

        tag.putIntArray("intArrayXPos",storedX);
        tag.putIntArray("intArrayYPos",storedY);
        tag.putIntArray("intArrayZPos",storedZ);


        this.lockCode.write(tag);
        return tag;
    }

    // TODO: When syncing data to the client, only update the two itemsstacks that get updated, not everything.

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        int tileEntityType = 42;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new SUpdateTileEntityPacket(this.pos, tileEntityType, nbtTagCompound);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
        BlockState state = this.world.getBlockState(this.pos);
        this.handleUpdateTag(state,pkt.getNbtCompound());
        this.world.notifyBlockUpdate(this.pos, state, state, 3);
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag)
    {
        this.read(state, tag);
    }

    private static Block[] pedArray = new Block[]{PedestalBlock.PEDESTAL_000, PedestalBlock.PEDESTAL_001, PedestalBlock.PEDESTAL_002, PedestalBlock.PEDESTAL_003
            , PedestalBlock.PEDESTAL_010, PedestalBlock.PEDESTAL_011, PedestalBlock.PEDESTAL_012, PedestalBlock.PEDESTAL_013
            , PedestalBlock.PEDESTAL_020, PedestalBlock.PEDESTAL_021, PedestalBlock.PEDESTAL_022, PedestalBlock.PEDESTAL_023
            , PedestalBlock.PEDESTAL_030, PedestalBlock.PEDESTAL_031, PedestalBlock.PEDESTAL_032, PedestalBlock.PEDESTAL_033
            , PedestalBlock.PEDESTAL_100, PedestalBlock.PEDESTAL_101, PedestalBlock.PEDESTAL_102, PedestalBlock.PEDESTAL_103
            , PedestalBlock.PEDESTAL_110, PedestalBlock.PEDESTAL_111, PedestalBlock.PEDESTAL_112, PedestalBlock.PEDESTAL_113
            , PedestalBlock.PEDESTAL_120, PedestalBlock.PEDESTAL_121, PedestalBlock.PEDESTAL_122, PedestalBlock.PEDESTAL_123
            , PedestalBlock.PEDESTAL_130, PedestalBlock.PEDESTAL_131, PedestalBlock.PEDESTAL_132, PedestalBlock.PEDESTAL_133
            , PedestalBlock.PEDESTAL_200, PedestalBlock.PEDESTAL_201, PedestalBlock.PEDESTAL_202, PedestalBlock.PEDESTAL_203
            , PedestalBlock.PEDESTAL_210, PedestalBlock.PEDESTAL_211, PedestalBlock.PEDESTAL_212, PedestalBlock.PEDESTAL_213
            , PedestalBlock.PEDESTAL_220, PedestalBlock.PEDESTAL_221, PedestalBlock.PEDESTAL_222, PedestalBlock.PEDESTAL_223
            , PedestalBlock.PEDESTAL_230, PedestalBlock.PEDESTAL_231, PedestalBlock.PEDESTAL_232, PedestalBlock.PEDESTAL_233
            , PedestalBlock.PEDESTAL_300, PedestalBlock.PEDESTAL_301, PedestalBlock.PEDESTAL_302, PedestalBlock.PEDESTAL_303
            , PedestalBlock.PEDESTAL_310, PedestalBlock.PEDESTAL_311, PedestalBlock.PEDESTAL_312, PedestalBlock.PEDESTAL_313
            , PedestalBlock.PEDESTAL_320, PedestalBlock.PEDESTAL_321, PedestalBlock.PEDESTAL_322, PedestalBlock.PEDESTAL_323
            , PedestalBlock.PEDESTAL_330, PedestalBlock.PEDESTAL_331, PedestalBlock.PEDESTAL_332, PedestalBlock.PEDESTAL_333};

    private static final ResourceLocation RESLOC_TILE_PEDESTAL = new ResourceLocation(MODID, "tile/pedestal");

    public static TileEntityType<PedestalTileEntity> PEDESTALTYPE = TileEntityType.Builder.create(PedestalTileEntity::new, pedArray).build(null);

    @SubscribeEvent
    public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> r = event.getRegistry();
        r.register(PEDESTALTYPE.setRegistryName(RESLOC_TILE_PEDESTAL));
    }

    @Override
    public void remove() {
        super.remove();
        if(this.handler != null) {
            this.handler.invalidate();
        }
        if(this.privateHandler != null) {
            this.privateHandler.invalidate();
        }
    }
}