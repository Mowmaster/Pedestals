package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
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
import net.minecraft.world.server.ServerWorld;
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

public class ItemUpgradeEnderFilteredRestock extends ItemUpgradeBaseFilter
{
    public ItemUpgradeEnderFilteredRestock(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return true;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int canAcceptCount(World world, BlockPos posPedestal, ItemStack inPedestal, ItemStack itemStackIncoming) {
        TileEntity tile = world.getTileEntity(posPedestal);
        if(tile instanceof PedestalTileEntity)
        {
            PedestalTileEntity ped = ((PedestalTileEntity)tile);
            ItemStack coin = ped.getCoinOnPedestal();
            int upgradeTransferRate = getItemTransferRate(coin);

            PlayerEntity player = ((ServerWorld) world).getPlayerByUuid(getPlayerFromCoin(coin));
            if(player != null)
            {
                if(hasAdvancedInventoryTargeting(coin))
                {
                    int range = player.inventory.getSizeInventory();

                    ItemStack stack = IntStream.range(0,range)//Int Range
                            .mapToObj((player.inventory)::getStackInSlot)//Function being applied to each interval
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
                else
                {
                    int range = player.getInventoryEnderChest().getSizeInventory();

                    ItemStack stack = IntStream.range(0,range)//Int Range
                            .mapToObj((player.getInventoryEnderChest())::getStackInSlot)//Function being applied to each interval
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
        TileEntity tile = world.getTileEntity(posPedestal);
        if(tile instanceof PedestalTileEntity)
        {
            PedestalTileEntity ped = ((PedestalTileEntity)tile);
            ItemStack coin = ped.getCoinOnPedestal();

            PlayerEntity player = ((ServerWorld) world).getPlayerByUuid(getPlayerFromCoin(coin));
            if(player != null)
            {
                if(hasAdvancedInventoryTargeting(coin))
                {
                    int range = player.inventory.getSizeInventory();

                    ItemStack itemFromInv = ItemStack.EMPTY;
                    itemFromInv = IntStream.range(0,range)//Int Range
                            .mapToObj((player.inventory)::getStackInSlot)//Function being applied to each interval
                            .filter(itemStack -> !itemStack.isEmpty())
                            .filter(itemStack -> itemStack.getCount() != itemStack.getMaxStackSize())
                            .filter(itemStack -> doItemsMatch(itemStack,itemStackIn))
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!itemFromInv.isEmpty())
                    {
                        returner = true;
                    }
                }
                else
                {
                    int range = player.getInventoryEnderChest().getSizeInventory();

                    ItemStack itemFromInv = ItemStack.EMPTY;
                    itemFromInv = IntStream.range(0,range)//Int Range
                            .mapToObj((player.getInventoryEnderChest())::getStackInSlot)//Function being applied to each interval
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
                    upgradeAction(world,pedestalPos,itemInPedestal,coinInPedestal);
                }
            }
        }
    }

    //Upgrade checks each slot and inserts if it can
    //only inserts into slots with items, will not fill blank slots
    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {
        if(!itemInPedestal.isEmpty())
        {
            ItemStack itemInPedestalCopy = itemInPedestal.copy();
            //IF pedestal is empty and has nothing to transfer then dont do anything
            PlayerEntity player = ((ServerWorld) world).getPlayerByUuid(getPlayerFromCoin(coinInPedestal));
            if(player != null)
            {
                if(hasAdvancedInventoryTargeting(coinInPedestal))
                {
                    int range = player.inventory.getSizeInventory();
                    ItemStack itemFromInv = ItemStack.EMPTY;
                    itemFromInv = IntStream.range(0,range)//Int Range
                            .mapToObj((player.inventory)::getStackInSlot)//Function being applied to each interval
                            .filter(itemStack -> !itemStack.isEmpty())
                            .filter(itemStack -> doItemsMatch(itemStack,itemInPedestalCopy))
                            .filter(itemStack -> itemStack.getCount() < itemStack.getMaxStackSize())
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!itemFromInv.isEmpty())
                    {
                        int ps = getPlayerSlotWithMatchingStackExactNotFull(player.inventory,itemInPedestalCopy);

                        if(ps>=0 && ps < player.inventory.getSizeInventory())
                        {
                            //Need to check if item matches item in slot, and then decide how much can be input
                            if(player.inventory.isItemValidForSlot(ps,itemInPedestalCopy))
                            {
                                if(doItemsMatch(itemInPedestalCopy,itemFromInv))
                                {
                                    int spaceInInventoryStack = player.inventory.getStackInSlot(ps).getMaxStackSize() - itemFromInv.getCount();
                                    int allowedTransfer = (spaceInInventoryStack>=itemInPedestalCopy.getCount())?(itemInPedestalCopy.getCount()):(spaceInInventoryStack);
                                    ItemStack stackToSet = itemFromInv.copy();
                                    stackToSet.setCount(itemFromInv.getCount()+allowedTransfer);
                                    player.inventory.setInventorySlotContents(ps,stackToSet);
                                    removeFromPedestal(world,posOfPedestal ,allowedTransfer);
                                }
                            }
                        }
                    }
                }
                /*else
                {
                    if(!itemInPedestalCopy.isEmpty())
                    {
                        ItemStack itemFromInventory = ItemStack.EMPTY;
                        if(i < player.getInventoryEnderChest().getSizeInventory() && player.getInventoryEnderChest().getStackInSlot(i) != null)
                        {
                            itemFromInventory = player.getInventoryEnderChest().getStackInSlot(i);
                        }

                        if(i>=0 && i < player.getInventoryEnderChest().getSizeInventory())
                        {
                            if(player.getInventoryEnderChest().isItemValidForSlot(i, itemInPedestalCopy))
                            {
                                int spaceInInventoryStack = player.getInventoryEnderChest().getStackInSlot(i).getMaxStackSize() - itemFromInventory.getCount();

                                //if inv slot is empty it should be able to handle as much as we can give it
                                int allowedTransferRate = 64;
                                //checks allowed slot size amount and sets it if its lower then transfer rate
                                if(player.getInventoryEnderChest().getStackInSlot(i).getMaxStackSize() <= allowedTransferRate) allowedTransferRate = player.getInventoryEnderChest().getStackInSlot(i).getMaxStackSize();
                                //never have to check to see if pedestal and stack match because the slot checker does it for us
                                //if our transfer rate is bigger then what can go in the slot if its partially full we set the transfer size to what can fit
                                //Otherwise if space is bigger then rate we know it can accept as much as we're putting in
                                if(allowedTransferRate> spaceInInventoryStack) allowedTransferRate = spaceInInventoryStack;
                                //IF items in pedestal are less then the allowed transfer amount then set it as the amount
                                if(allowedTransferRate > itemInPedestalCopy.getCount()) allowedTransferRate = itemInPedestalCopy.getCount();



                                //After all calculations for transfer rate, set stack size to transfer and transfer the items
                                int slotnext = getSlotNumberNext(i,player.getInventoryEnderChest().getSizeInventory(),itemInPedestalCopy,itemFromInventory);
                                if(doItemsMatch(itemInPedestalCopy,itemFromInventory ))
                                {
                                    itemInPedestalCopy.setCount(allowedTransferRate);
                                    //No Storage Drawers Support, because that seems silly
                                    if(player.getInventoryEnderChest().addItem(itemInPedestalCopy).equals(ItemStack.EMPTY)){
                                        removeFromPedestal(world,posOfPedestal ,allowedTransferRate);
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
                            int slotnext = getSlotNumberNext(i,player.getInventoryEnderChest().getSizeInventory(),itemInPedestalCopy,itemFromInventory);
                            setIntValueToPedestal(world,posOfPedestal ,slotnext);
                        }
                    }
                }*/
            }
        }
    }

    /*public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {

        PlayerEntity player = ((ServerWorld) world).getPlayerByUuid(getPlayerFromCoin(coinInPedestal));
        if(player != null)
        {
            if(hasAdvancedInventoryTargeting(coinInPedestal))
            {
                if(!itemInPedestal.isEmpty())
                {
                    if(player.inventory.addItemStackToInventory(itemInPedestal.copy()))
                    {
                        removeFromPedestal(world,posOfPedestal,itemInPedestal.getCount());
                    }
                }
            }
            else
            {
                if(!itemInPedestal.isEmpty())
                {
                    ItemStack leftovers = player.getInventoryEnderChest().addItem(itemInPedestal.copy());
                    if(leftovers.isEmpty())
                    {
                        removeFromPedestal(world,posOfPedestal,itemInPedestal.getCount());
                    }
                    else
                    {
                        int remover = itemInPedestal.getCount()-leftovers.getCount();
                        removeFromPedestal(world,posOfPedestal,remover);
                    }
                }
            }
        }
    }*/

    @Override
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        /*if(entityIn instanceof ItemEntity)
        {
            ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
            ItemStack itemFromPedestal = getStackInPedestal(world,posPedestal);
            if(itemFromPedestal.isEmpty())
            {
                TileEntity pedestalInv = world.getTileEntity(posPedestal);
                if(pedestalInv instanceof PedestalTileEntity) {
                    entityIn.remove();
                    ((PedestalTileEntity) pedestalInv).addItem(getItemStack);
                }
            }
        }*/
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name, Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(coin));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(speed);
    }

    public static final Item ENDERFRESTOCK = new ItemUpgradeEnderFilteredRestock(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/enderfilteredrestock"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ENDERFRESTOCK);
    }


}