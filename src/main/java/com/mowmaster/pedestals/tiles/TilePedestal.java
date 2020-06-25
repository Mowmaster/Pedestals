package com.mowmaster.pedestals.tiles;

import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBaseFilter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
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


public class TilePedestal extends TileEntity implements ITickableTileEntity {

    private LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler);

    private static final int[] SLOTS_ALLSIDES = new int[] {0};

    private int storedValueForUpgrades = 0;
    private int intTransferAmount = 0;
    private int intTransferSpeed = 20;
    private int intTransferRange = 8;
    private int intSpeed = 0;
    private int intStorage = 0;
    private int intSpeedAmp = 0;
    private int intStorageAmp = 0;
    private boolean boolLight = false;
    private final List<BlockPos> storedLocations = new ArrayList<BlockPos>();

    public TilePedestal()
    {
        super(PEDESTALTYPE);
    }

    private void update()
    {
        markDirty();
        world.notifyBlockUpdate(pos,getBlockState(),getBlockState(),1);
        world.notifyBlockUpdate(pos,getBlockState(),getBlockState(),2);
    }

    private IItemHandler createHandler() {
        return new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                update();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot == 0) return true;
                if (slot == 1 && stack.getItem() instanceof ItemUpgradeBase) return true;
                return false;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
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

    public String debugLocationList()
    {
        String lists = "";
        for(int i=0;i<getNumberOfStoredLocations();i++)
        {
            lists = lists + storedLocations.get(i).toString() + ", ";
        }
        return lists;
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
    public int getPedestalTransferAmount(){return intTransferAmount;}
    public void setPedestalTransferAmount(int transferAmount){
        intTransferAmount = transferAmount;
        update();
    }
    public int getPedestalTransferSpeed(){return intTransferSpeed;}
    public void setPedestalTransferSpeed(int transferSpeed){
        intTransferSpeed = transferSpeed;
        update();
    }
    public int getPedestalTransferRange(){return intTransferRange;}
    public void setPedestalTransferRange(int transferSpeed){
        intTransferRange = transferSpeed;
        update();
    }
    public int spaceInPedestal()
    {
        int space = 0;

        if(getItemInPedestal().isEmpty() || getItemInPedestal().equals(ItemStack.EMPTY))
        {
            space = 64;
        }
        else
        {
            space = (getMaxStackSize() - getItemInPedestal().getCount());
        }

        return space;
    }

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
        IItemHandler h = handler.orElse(null);
        if(h.getStackInSlot(1).isEmpty())
        {
            return false;
        }
        else  return true;
    }
    public boolean hasLight()
    {
        return boolLight;
    }

    public boolean hasSpeed()
    {
        return (intSpeed>0)?(true):(false);
    }

    public boolean hasStorage()
    {
        return (intStorage>0)?(true):(false);
    }

    public boolean hasSpeedAmp()
    {
        return (intSpeedAmp>0)?(true):(false);
    }

    public boolean hasStorageAmp()
    {
        return (intStorageAmp>0)?(true):(false);
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
    public ItemStack getCoinOnPedestal()
    {
        IItemHandler h = handler.orElse(null);
        if(hasCoin())
        {
            return h.getStackInSlot(1);
        }
        else return ItemStack.EMPTY;
    }

    public int getSpeed()
    {
        return intSpeed;
    }

    public int getStorage()
    {
        return intStorage;
    }

    public int getSpeedAmp()
    {
        return intSpeedAmp;
    }

    public int getStorageAmp()
    {
        return intStorageAmp;
    }


    /**
     * world.notifyBlockUpdate(this.pos,this.getBlockState(),this.getBlockState(),2);
     * Sets a block state into this world.Flags are as follows:
     * 1 will cause a block update.
     * 2 will send the change to clients.
     * 4 will prevent the block from being re-rendered.
     * 8 will force any re-renders to run on the main thread instead
     * 16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
     * 32 will prevent neighbor reactions from spawning drops.
     * 64 will signify the block is being moved.
     * Flags can be OR-ed
     */
    public ItemStack removeItem(int numToRemove) {
        IItemHandler h = handler.orElse(null);
        ItemStack stack = h.extractItem(0,numToRemove,false);
        update();

        return stack;
    }

    public ItemStack removeItem() {
        IItemHandler h = handler.orElse(null);
        ItemStack stack = h.extractItem(0,h.getStackInSlot(0).getCount(),false);
        update();

        return stack;
    }

    public ItemStack removeCoin() {
        IItemHandler h = handler.orElse(null);
        ItemStack stack = h.extractItem(1,h.getStackInSlot(1).getCount(),false);
        setStoredValueForUpgrades(0);
        update();

        return stack;
    }

    public boolean removeSpeed(int count)
    {
        boolean returner = (getSpeed()-count >= 0)?(true):(false);
        if(returner)
        {
            if(getSpeed()-count <=0)setSpeedAmp(0);
            intSpeed-=count;

            update();
        }

        return returner;
    }

    public boolean removeStorage(int count)
    {
        boolean returner = (getStorage()-count >= 0)?(true):(false);
        if(returner)
        {
            if(getStorage()-count <=0)setStorageAmp(0);
            intStorage-=count;
            update();
        }

        return returner;
    }

    public boolean removeSpeedAmp(int count)
    {
        boolean returner = (getSpeedAmp()-count >= 0)?(true):(false);
        if(returner)
        {
            intSpeedAmp-=count;
            update();
        }

        return returner;
    }

    public boolean removeStorageAmp(int count)
    {
        boolean returner = (getStorageAmp()-count >= 0)?(true):(false);
        if(returner)
        {
            intStorageAmp-=count;
            update();
        }

        return returner;
    }

    public int getItemTransferRate()
    {
        int itemRate = 4;
        switch (getStorageAmp())
        {
            case 0:
                itemRate = (getStorage()>0)?(4):(4);
                break;
            case 1:
                itemRate= (getStorage()>0)?(8):(4);
                break;
            case 2:
                itemRate = (getStorage()>0)?(16):(4);
                break;
            case 3:
                itemRate = (getStorage()>0)?(32):(4);
                break;
            case 4:
                itemRate = (getStorage()>0)?(48):(4);
                break;
            case 5:
                itemRate=(getStorage()>0)?(64):(4);
                break;
            default: itemRate=4;
        }

        return  itemRate;
    }

    public int getOperationSpeed()
    {
        int speed = 20;
        switch (getSpeedAmp())
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
        update();

        return true;
    }

    public boolean addCoin(ItemStack coinFromBlock)
    {
        IItemHandler h = handler.orElse(null);
        ItemStack itemFromBlock = coinFromBlock.copy();
        itemFromBlock.setCount(1);
        if(hasCoin()){} else h.insertItem(1,itemFromBlock,false);
        setStoredValueForUpgrades(0);
        update();

        return true;
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
            boolean watered = state.get(BlockPedestalTE.WATERLOGGED);
            Direction dir = state.get(BlockPedestalTE.FACING);
            BlockState newstate = state.with(BlockPedestalTE.FACING,dir).with(BlockPedestalTE.WATERLOGGED,watered).with(BlockPedestalTE.LIT,true);
            world.notifyBlockUpdate(pos,state,newstate,3);
            world.setBlockState(pos,newstate,3);
            //world.markBlockRangeForRenderUpdate(pos,state,newstate);
            update();
            return true;
        }
    }

    public void addSpeed(int count)
    {
        intSpeed+=count;
    }

    public void addStorage(int count)
    {
        intStorage+=count;
    }

    public void setSpeedAmp(int count)
    {
        intSpeedAmp=count;
    }

    public void setStorageAmp(int count)
    {
        intStorageAmp=count;
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

    //Checks when linking pedestals if the two being linked are 1.on the same network 2. if one is neutral thus meaning they can link
    public boolean canLinkToPedestalNetwork(BlockPos pedestalToBeLinked)
    {
        //Check to see if pedestal to be linked is a block pedestal
        if(world.getBlockState(pedestalToBeLinked).getBlock() instanceof BlockPedestalTE)
        {
            return true;
        }

        return false;
    }

    //Returns items available to be insert, 0 if false
    public int canAcceptItems(ItemStack itemsIncoming)
    {
        int canAccept = 0;

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
                    if(getItemInPedestal().getCount() < getMaxStackSize())
                    {
                        canAccept = (getMaxStackSize() - getItemInPedestal().getCount());
                    }
                }
            }
        }

        return canAccept;
    }

    public boolean hasFilter(TilePedestal pedestalSendingTo)
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
                ((ItemUpgradeBase) coinInPed).canSendItem();
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
                    if(world.getBlockState(pedestalToSendTo).getBlock() instanceof BlockPedestalTE)
                    {
                        //Make sure it is still part of the right network
                        if(canLinkToPedestalNetwork(pedestalToSendTo))
                        {
                            //Get the tile before checking other things
                            if(world.getTileEntity(pedestalToSendTo) instanceof TilePedestal)
                            {
                                TilePedestal tilePedestalToSendTo = (TilePedestal)world.getTileEntity(pedestalToSendTo);

                                //Checks if pedestal is empty or if not then checks if items match and how many can be insert
                                if(tilePedestalToSendTo.canAcceptItems(getItemInPedestal()) > 0)
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

    public void sendItemsToPedestal(BlockPos pedestalToSendTo)
    {
        if(world.getTileEntity(pedestalToSendTo) instanceof TilePedestal)
        {
            TilePedestal tileToSendTo = ((TilePedestal)world.getTileEntity(pedestalToSendTo));

            //Max that can be recieved
            int countToSend = tileToSendTo.spaceInPedestal();
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
                removeSpeed(1);
                removeStorage(1);
                tileToSendTo.addItem(copyStackToSend);
                tileToSendTo.update();
                //remove item will mark dirty this pedestan
                //addItem mark dirties the reciever pedestal
            }
        }
    }

    public void collideWithPedestal(World world, TilePedestal tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(!world.isRemote) {
            if(entityIn instanceof ItemEntity)
            {
                /*ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
                if(getItemStack.getItem() instanceof ItemColorDust)
                {
                    //VoidStorage
                    if(getItemStack.getTag().getInt("color") == 11141375)
                    {
                        addStorage(getItemStack.getCount()*20);
                        if(getStorageAmp()==0)setStorageAmp(1);
                        entityIn.remove();

                    }
                    //Speed
                    else if(getItemStack.getTag().getInt("color") == 65535)
                    {
                        addSpeed(getItemStack.getCount()*20);
                        if(getSpeedAmp()==0)setSpeedAmp(1);
                        entityIn.remove();

                    }
                }
                else if(getItemStack.getItem() instanceof ItemSpellScroll)
                {
                    if(getItemStack.getTag().contains(Reference.MODID + "Color"))
                    {
                        CompoundNBT nbt = getItemStack.getTag();
                        //VoidStorage
                        if(nbt.getInt(Reference.MODID + "Color") == 11141375)
                        {
                            addStorage(nbt.getInt(Reference.MODID + "Duration"));
                            setStorageAmp(nbt.getInt(Reference.MODID + "Amplifier")+1);
                            entityIn.remove();

                        }
                        //Speed
                        else if(nbt.getInt(Reference.MODID + "Color") == 65535)
                        {
                            addSpeed(nbt.getInt(Reference.MODID + "Duration"));
                            setSpeedAmp(nbt.getInt(Reference.MODID + "Amplifier")+1);
                            entityIn.remove();

                        }
                    }
                }
                else */if(tilePedestal.hasCoin())
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
                if(hasCoin())
                {
                    Item coinInPed = getCoinOnPedestal().getItem();
                    if(coinInPed instanceof ItemUpgradeBase)
                    {
                        impTicker++;
                        ((ItemUpgradeBase) coinInPed).updateAction(impTicker,this.world,getItemInPedestal(),getCoinOnPedestal(),this.getPos());
                        if(impTicker >=200){impTicker=0;}
                    }
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
                    Random rand = new Random();
                    ((ItemUpgradeBase) coinInPed).onRandomDisplayTick(this, world.getBlockState(getPos()), world, getPos(), rand);
                }
            }
        }
    }

    @Override
    public void read(CompoundNBT tag) {
        CompoundNBT invTag = tag.getCompound("inv");
        handler.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag));

        this.storedValueForUpgrades = tag.getInt("storedUpgradeValue");
        this.intTransferAmount = tag.getInt("intTransferAmount");
        this.intTransferSpeed = tag.getInt("intTransferSpeed");
        this.intTransferRange = tag.getInt("intTransferRange");
        this.boolLight = tag.getBoolean("boollight");
        this.intSpeed = tag.getInt("intSpeed");
        this.intSpeedAmp = tag.getInt("intSpeedAmp");
        this.intStorage = tag.getInt("intStorage");
        this.intStorageAmp = tag.getInt("intStorageAmp");

        int counter = tag.getInt("storedBlockPosCounter");
        for(int i=0;i<counter;i++)
        {
            String getKeyNameX = "storedLocationX" + i;
            String getKeyNameY = "storedLocationY" + i;
            String getKeyNameZ = "storedLocationZ" + i;
            int getX = tag.getInt(getKeyNameX);
            int getY = tag.getInt(getKeyNameY);
            int getZ = tag.getInt(getKeyNameZ);
            BlockPos gotPos = new BlockPos(getX,getY,getZ);
            storeNewLocation(gotPos);
        }
        //CompoundNBT itemTagD = compound.getCompound("display");
        //this.display = new ItemStack(itemTagD);
        super.read(tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        handler.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put("inv", compound);
        });

        tag.putInt("storedUpgradeValue",storedValueForUpgrades);
        tag.putInt("intTransferAmount",intTransferAmount);
        tag.putInt("intTransferSpeed",intTransferSpeed);
        tag.putInt("intTransferRange",intTransferRange);
        tag.putBoolean("boollight", boolLight);
        tag.putInt("intSpeed",intSpeed);
        tag.putInt("intSpeedAmp",intSpeedAmp);
        tag.putInt("intStorage",intStorage);
        tag.putInt("intStorageAmp",intStorageAmp);
        int counter = 0;
        for(int i=0;i<getNumberOfStoredLocations();i++)
        {
            String keyNameX = "storedLocationX" + i;
            String keyNameY = "storedLocationY" + i;
            String keyNameZ = "storedLocationZ" + i;
            tag.putInt(keyNameX,storedLocations.get(i).getX());
            tag.putInt(keyNameY,storedLocations.get(i).getY());
            tag.putInt(keyNameZ,storedLocations.get(i).getZ());
            counter++;
        }
        tag.putInt("storedBlockPosCounter",counter);
        //compound.put("display",display.write(new CompoundNBT()));
        return super.write(tag);
    }

    //https://github.com/TheGreyGhost/MinecraftByExample/blob/1-15-2-working-latestMCP/src/main/java/minecraftbyexample/mbe21_tileentityrenderer/TileEntityMBE21.java
    // When the world loads from disk, the server needs to send the TileEntity information to the client
    //  it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this:
    //  getUpdatePacket() and onDataPacket() are used for one-at-a-time TileEntity updates
    //  getUpdateTag() and handleUpdateTag() are used by vanilla to collate together into a single chunk update packet
    // In this case, we need it for the gem colour.  There's no need to save the gem angular position because
    //  the player will never notice the difference and the client<-->server synchronisation lag will make it
    //  inaccurate anyway
    @Override
    //--+@Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        int tileEntityType = 42;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new SUpdateTileEntityPacket(this.pos, tileEntityType, nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }

    /* Creates a tag containing the TileEntity information, used by vanilla to transmit from server to client
 */
    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        return nbtTagCompound;
    }

    /* Populates this TileEntity with information from the tag, used by vanilla to transmit from server to client
 */
    @Override
    public void handleUpdateTag(CompoundNBT tag)
    {
        this.read(tag);
    }

    private static Block[] pedArray = new Block[]{BlockPedestalTE.PEDESTAL_000, BlockPedestalTE.PEDESTAL_001, BlockPedestalTE.PEDESTAL_002, BlockPedestalTE.PEDESTAL_003
            , BlockPedestalTE.PEDESTAL_010, BlockPedestalTE.PEDESTAL_011, BlockPedestalTE.PEDESTAL_012, BlockPedestalTE.PEDESTAL_013
            , BlockPedestalTE.PEDESTAL_020, BlockPedestalTE.PEDESTAL_021, BlockPedestalTE.PEDESTAL_022, BlockPedestalTE.PEDESTAL_023
            , BlockPedestalTE.PEDESTAL_030, BlockPedestalTE.PEDESTAL_031, BlockPedestalTE.PEDESTAL_032, BlockPedestalTE.PEDESTAL_033
            , BlockPedestalTE.PEDESTAL_100, BlockPedestalTE.PEDESTAL_101, BlockPedestalTE.PEDESTAL_102, BlockPedestalTE.PEDESTAL_103
            , BlockPedestalTE.PEDESTAL_110, BlockPedestalTE.PEDESTAL_111, BlockPedestalTE.PEDESTAL_112, BlockPedestalTE.PEDESTAL_113
            , BlockPedestalTE.PEDESTAL_120, BlockPedestalTE.PEDESTAL_121, BlockPedestalTE.PEDESTAL_122, BlockPedestalTE.PEDESTAL_123
            , BlockPedestalTE.PEDESTAL_130, BlockPedestalTE.PEDESTAL_131, BlockPedestalTE.PEDESTAL_132, BlockPedestalTE.PEDESTAL_133
            , BlockPedestalTE.PEDESTAL_200, BlockPedestalTE.PEDESTAL_201, BlockPedestalTE.PEDESTAL_202, BlockPedestalTE.PEDESTAL_203
            , BlockPedestalTE.PEDESTAL_210, BlockPedestalTE.PEDESTAL_211, BlockPedestalTE.PEDESTAL_212, BlockPedestalTE.PEDESTAL_213
            , BlockPedestalTE.PEDESTAL_220, BlockPedestalTE.PEDESTAL_221, BlockPedestalTE.PEDESTAL_222, BlockPedestalTE.PEDESTAL_223
            , BlockPedestalTE.PEDESTAL_230, BlockPedestalTE.PEDESTAL_231, BlockPedestalTE.PEDESTAL_232, BlockPedestalTE.PEDESTAL_233
            , BlockPedestalTE.PEDESTAL_300, BlockPedestalTE.PEDESTAL_301, BlockPedestalTE.PEDESTAL_302, BlockPedestalTE.PEDESTAL_303
            , BlockPedestalTE.PEDESTAL_310, BlockPedestalTE.PEDESTAL_311, BlockPedestalTE.PEDESTAL_312, BlockPedestalTE.PEDESTAL_313
            , BlockPedestalTE.PEDESTAL_320, BlockPedestalTE.PEDESTAL_321, BlockPedestalTE.PEDESTAL_322, BlockPedestalTE.PEDESTAL_323
            , BlockPedestalTE.PEDESTAL_330, BlockPedestalTE.PEDESTAL_331, BlockPedestalTE.PEDESTAL_332, BlockPedestalTE.PEDESTAL_333};

    private static final ResourceLocation RESLOC_TILE_PEDESTAL = new ResourceLocation(MODID, "tile/pedestal");

    public static TileEntityType<TilePedestal> PEDESTALTYPE = TileEntityType.Builder.create(TilePedestal::new, pedArray).build(null);

    @SubscribeEvent
    public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> r = event.getRegistry();
        r.register(PEDESTALTYPE.setRegistryName(RESLOC_TILE_PEDESTAL));
    }
}