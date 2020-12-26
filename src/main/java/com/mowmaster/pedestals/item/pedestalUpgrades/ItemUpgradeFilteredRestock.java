package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFilteredRestock extends ItemUpgradeBaseFilter
{
    public ItemUpgradeFilteredRestock(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptOpSpeed() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {return true;}

    @Override
    public int getItemEnchantability()
    {
        return 10;
    }

    @Override
    public int canAcceptCount(World world, BlockPos posPedestal, ItemStack inPedestal, ItemStack itemStackIncoming) {
        BlockPos posInventory = getPosOfBlockBelow(world, posPedestal, 1);

        TileEntity tile = world.getTileEntity(posPedestal);
        if(tile instanceof PedestalTileEntity)
        {
            PedestalTileEntity ped = ((PedestalTileEntity)tile);
            ItemStack coin = ped.getCoinOnPedestal();
            int upgradeTransferRate = getItemTransferRate(coin);
            LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posPedestal),true);
            if(cap.isPresent())
            {
                IItemHandler handler = cap.orElse(null);
                if(handler != null)
                {
                    int range = handler.getSlots();

                    ItemStack stack = IntStream.range(0,range)//Int Range
                            .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                            .filter(itemStack -> !itemStack.isEmpty())
                            .filter(itemStack -> itemStack.getCount() != itemStack.getMaxStackSize())
                            .filter(itemStack -> doItemsMatch(itemStack,itemStackIncoming))
                            .filter(itemStack -> (itemStack.getCount()+inPedestal.getCount())<=itemStack.getMaxStackSize())
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!stack.isEmpty())
                    {
                        if((stack.getCount()+inPedestal.getCount())<itemStackIncoming.getMaxStackSize())
                        {
                            return itemStackIncoming.getMaxStackSize()-(stack.getCount()+inPedestal.getCount());
                        }
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        boolean returner = false;
        BlockPos posInventory = getPosOfBlockBelow(world, posPedestal, 1);

        TileEntity tile = world.getTileEntity(posPedestal);
        if(tile instanceof PedestalTileEntity)
        {
            PedestalTileEntity ped = ((PedestalTileEntity)tile);
            ItemStack coin = ped.getCoinOnPedestal();
            int upgradeTransferRate = getItemTransferRate(coin);
            LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posPedestal),true);
            if(cap.isPresent())
            {
                IItemHandler handler = cap.orElse(null);
                if(handler != null)
                {
                    int range = handler.getSlots();

                    ItemStack itemFromInv = ItemStack.EMPTY;
                    itemFromInv = IntStream.range(0,range)//Int Range
                            .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                            .filter(itemStack -> !itemStack.isEmpty())
                            .filter(itemStack -> itemStack.getCount() != itemStack.getMaxStackSize())
                            .filter(itemStack -> doItemsMatch(itemStack,itemStackIn))
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!itemFromInv.isEmpty())
                    {
                        returner = true;
                    }
                }
            }
        }

        return returner;
    }

    public int getSlotNumberNext(int currentSlotNumber, int range, ItemStack inPedestal, ItemStack inInventory)
    {
        int slotToReturn=-1;
        //We just used current slot so add one and start there with finding the next slot
        int slots = currentSlotNumber+1;
        int ranger = range;
        if(slots>=ranger)
        {
            slots=0;
        }

        for(int i=slots;i<ranger;i++)
        {
            if(doItemsMatch(inPedestal,inInventory ))
            {
                slotToReturn=i;
                break;
            }
        }

        //If above loop fails then try it from the beginning
        if(slotToReturn == -1)
        {
            for(int i=0;i<ranger;i++)
            {
                if(doItemsMatch(inPedestal,inInventory ))
                {
                    slotToReturn=i;
                    break;
                }
            }
        }

        //if all else fails return -1 (next time it will start the loop at 0)
        return slots;
    }


    //                          impTicker,this.world,   getItemInPedestal(),      getCoinOnPedestal(),     this.getPos()
    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);

            if(!world.isBlockPowered(pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(world,pedestalPos,coinInPedestal);
                }
            }
        }
    }

    //Upgrade checks each slot and inserts if it can
    //only inserts into slots with items, will not fill blank slots
    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        int i = getIntValueFromPedestal(world,posOfPedestal );
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        int upgradeTransferRate = getItemTransferRate(coinInPedestal);
        ItemStack itemFromPedestal = ItemStack.EMPTY;
        //Checks to make sure a TE exists
        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(hasAdvancedInventoryTargeting(coinInPedestal))cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        //Gets inventory TE then makes sure its not a pedestal
        TileEntity invToPushTo = world.getTileEntity(posInventory);
        if(invToPushTo instanceof PedestalTileEntity) {
            itemFromPedestal = ItemStack.EMPTY;
        }
        else {
            itemFromPedestal = getStackInPedestal(world,posOfPedestal);
            //IF pedestal is empty and has nothing to transfer then dont do anything
            if(!itemFromPedestal.isEmpty() && !itemFromPedestal.equals(ItemStack.EMPTY))
            {
                if(cap.isPresent())
                {
                    IItemHandler handler = cap.orElse(null);

                    //gets next empty or partially filled matching slot
                    if(handler != null)
                    {
                        ItemStack itemFromInventory = ItemStack.EMPTY;
                        itemFromPedestal = getStackInPedestal(world,posOfPedestal).copy();
                        if(i < handler.getSlots() && handler.getStackInSlot(i) != null)
                        {
                            itemFromInventory = handler.getStackInSlot(i);
                        }

                        if(i>=0 && i < handler.getSlots())
                        {
                            if(handler.isItemValid(i, itemFromPedestal))
                            {
                                int spaceInInventoryStack = handler.getSlotLimit(i) - itemFromInventory.getCount();

                                //if inv slot is empty it should be able to handle as much as we can give it
                                int allowedTransferRate = upgradeTransferRate;
                                //checks allowed slot size amount and sets it if its lower then transfer rate
                                if(handler.getSlotLimit(i) <= allowedTransferRate) allowedTransferRate = handler.getSlotLimit(i);
                                //never have to check to see if pedestal and stack match because the slot checker does it for us
                                //if our transfer rate is bigger then what can go in the slot if its partially full we set the transfer size to what can fit
                                //Otherwise if space is bigger then rate we know it can accept as much as we're putting in
                                if(allowedTransferRate> spaceInInventoryStack) allowedTransferRate = spaceInInventoryStack;
                                //IF items in pedestal are less then the allowed transfer amount then set it as the amount
                                if(allowedTransferRate > itemFromPedestal.getCount()) allowedTransferRate = itemFromPedestal.getCount();



                                //After all calculations for transfer rate, set stack size to transfer and transfer the items
                                int slotnext = getSlotNumberNext(i,handler.getSlots(),itemFromPedestal,itemFromInventory);
                                if(doItemsMatch(itemFromPedestal,itemFromInventory ))
                                {
                                    itemFromPedestal.setCount(allowedTransferRate);
                                    //No Storage Drawers Support, because that seems silly
                                    if(handler.insertItem(i,itemFromPedestal,true ).equals(ItemStack.EMPTY)){
                                        removeFromPedestal(world,posOfPedestal ,allowedTransferRate);
                                        handler.insertItem(i,itemFromPedestal,false );
                                        setIntValueToPedestal(world,posOfPedestal ,slotnext);
                                    }
                                }
                                else
                                {
                                    setIntValueToPedestal(world,posOfPedestal ,slotnext);
                                }

                            }
                        }
                        else
                        {
                            int slotnext = getSlotNumberNext(i,handler.getSlots(),itemFromPedestal,itemFromInventory);
                            setIntValueToPedestal(world,posOfPedestal ,slotnext);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString(""+getItemTransferRate(stack)+"");
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + getItemTransferRate(stack) + "");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        rate.mergeStyle(TextFormatting.GRAY);
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(rate);
        tooltip.add(speed);
    }

    public static final Item FRESTOCK = new ItemUpgradeFilteredRestock(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/frestock"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FRESTOCK);
    }


}
