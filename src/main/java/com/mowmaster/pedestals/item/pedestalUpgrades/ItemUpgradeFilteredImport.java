package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFilteredImport extends ItemUpgradeBase
{
    public ItemUpgradeFilteredImport(Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isClientSide)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getBlockPos();

            int speed = getOperationSpeed(coinInPedestal);

            if(!world.hasNeighborSignal(pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(world,pedestalPos,itemInPedestal,coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {
        BlockPos senderBelowInvPos = getBlockPosOfBlockBelow(world,posOfPedestal,1);
        int transferRate = getItemTransferRate(coinInPedestal);
        ItemStack senderBelowInvStack = ItemStack.EMPTY;

        TileEntity senderBelowInv = world.getTileEntity(senderBelowInvPos);
        //Make sure this Inv isnt a pedestal
        if(senderBelowInv instanceof PedestalTileEntity) {
            senderBelowInvStack = ItemStack.EMPTY;
        }
        else {
            //Get Cap of Inv to pull items out of
            LazyOptional<IItemHandler> senderBelowInvCap = findItemHandlerAtPos(world,senderBelowInvPos,getPedestalFacing(world, posOfPedestal),true);
            if(hasAdvancedInventoryTargeting(coinInPedestal))senderBelowInvCap = findItemHandlerAtPosAdvanced(world,senderBelowInvPos,getPedestalFacing(world, posOfPedestal),true);
            if(!isInventoryEmpty(senderBelowInvCap))
            {
                //If Inv Is Valid
                if(senderBelowInvCap.isPresent())
                {
                    //Get Handler of present Inv
                    IItemHandler senderBelowInvHandler = senderBelowInvCap.orElse(null);
                    //Null Check it since we use the .orElse(null)
                    if(senderBelowInvHandler != null)
                    {
                        ItemStack toImport = ItemStack.EMPTY;
                        //Check for our pedestal
                        TileEntity senderPedestalCheck = world.getTileEntity(posOfPedestal);
                        if(senderPedestalCheck instanceof PedestalTileEntity) {
                            //Create a var for our Pedestal
                            PedestalTileEntity senderPedestal = ((PedestalTileEntity) senderPedestalCheck);
                            List<BlockPos> getRecievers = senderPedestal.getLocationList();
                            if(getRecievers.size()>0)
                            {
                                for(AtomicInteger slot = new AtomicInteger(0); slot.get()<getRecievers.size();slot.set(slot.get()+1))
                                {
                                    BlockPos receiverConnection = getRecievers.get(slot.get());
                                    TileEntity pedestalRec = world.getTileEntity(receiverConnection);
                                    if(pedestalRec instanceof PedestalTileEntity) {
                                        PedestalTileEntity receiverPedestal = ((PedestalTileEntity) pedestalRec);
                                        if(receiverPedestal.hasFilter(senderPedestal))
                                        {
                                            Item coinInPed = receiverPedestal.getCoinOnPedestal().getItem();
                                            if(coinInPed instanceof ItemUpgradeBaseFilter)
                                            {
                                                int range = senderBelowInvHandler.getSlots();
                                                ItemStack itemInInv = ItemStack.EMPTY;
                                                itemInInv = IntStream.range(0,range)
                                                        .mapToObj((senderBelowInvHandler)::getStackInSlot)
                                                        .filter(itemStack -> !itemStack.isEmpty())
                                                        .filter(itemStack -> ((ItemUpgradeBaseFilter) coinInPed).canAcceptItem(world,receiverConnection,itemStack))
                                                        .filter(itemStack -> ((ItemUpgradeBaseFilter) coinInPed).canAcceptCount(world,receiverConnection,((PedestalTileEntity)world.getTileEntity(receiverConnection)).getItemInPedestal(),itemStack)>0)
                                                        .findFirst().orElse(ItemStack.EMPTY);
                                                if(!itemInInv.isEmpty())
                                                {
                                                    toImport = itemInInv.copy();
                                                    break;
                                                }
                                            }
                                        }
                                        else
                                        {
                                            int range = senderBelowInvHandler.getSlots();
                                            ItemStack itemInInv = ItemStack.EMPTY;
                                            itemInInv = IntStream.range(0,range)//Int Range
                                                    .mapToObj((senderBelowInvHandler)::getStackInSlot)//Function being applied to each interval
                                                    .filter(itemStack -> !itemStack.isEmpty())
                                                    .filter(itemStack -> senderPedestal.canSendToPedestal(receiverConnection,itemStack))
                                                    .findFirst().orElse(ItemStack.EMPTY);
                                            if(!itemInInv.isEmpty())
                                            {
                                                toImport = itemInInv.copy();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            //After an Item Allowed to be sent is found. Make sure the sender pedestal can import items into the pedestal
                            if(senderPedestal.getItemInPedestal().isEmpty() || doItemsMatch(senderPedestal.getItemInPedestal(),toImport))
                            {
                                int i = getSlotWithMatchingStackExact(senderBelowInvCap,toImport);
                                if(i>=0)
                                {
                                    int stacksToAllowedInPedestal = 0;
                                    int roomLeftInPedestal = 0;
                                    senderBelowInvStack = senderBelowInvHandler.getStackInSlot(i);
                                    ItemStack itemFromPedestal = getStackInPedestal(world,posOfPedestal);
                                    if(senderBelowInvStack != null && !senderBelowInvStack.isEmpty() && senderBelowInvStack.getItem() != Items.AIR)
                                    {
                                        if(itemFromPedestal.isEmpty() || itemFromPedestal.equals(ItemStack.EMPTY))
                                        {stacksToAllowedInPedestal = 64;}
                                        else
                                        {stacksToAllowedInPedestal = itemFromPedestal.getMaxStackSize();}
                                        roomLeftInPedestal = stacksToAllowedInPedestal-itemFromPedestal.getCount();
                                        int itemCountInInv = senderBelowInvStack.getCount();
                                        //int itemSpaceinInv = senderBelowInvStack.getMaxStackSize() - itemCountInInv;
                                        int allowedTransferRate = transferRate;
                                        if(roomLeftInPedestal < transferRate) allowedTransferRate = roomLeftInPedestal;
                                        if(itemCountInInv < allowedTransferRate) allowedTransferRate = itemCountInInv;
                                        //if(itemSpaceinInv < allowedTransferRate) allowedTransferRate = itemSpaceinInv;
                                        if(senderBelowInvHandler.extractItem(i,allowedTransferRate,true).getCount()>0)
                                        {
                                            ItemStack copyIncoming = senderBelowInvStack.copy();
                                            copyIncoming.setCount(allowedTransferRate);
                                            senderBelowInvHandler.extractItem(i,allowedTransferRate,false);
                                            senderPedestal.addItem(copyIncoming);
                                        }
                                    }
                                }
                            }
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

        TranslationTextComponent name = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        name.withStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.NIL_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".chat_rate");
        rate.append(""+getItemTransferRate(stack)+"");
        rate.withStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.NIL_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".chat_speed");
        speed.append(getOperationSpeedString(stack));
        speed.withStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.NIL_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".tooltip_rate");
        rate.append("" + getItemTransferRate(stack) + "");
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".tooltip_speed");
        speed.append(getOperationSpeedString(stack));

        rate.withStyle(TextFormatting.GRAY);
        speed.withStyle(TextFormatting.RED);

        tooltip.add(rate);
        tooltip.add(speed);
    }

    public static final Item FIMPORT = new ItemUpgradeFilteredImport(new Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fimport"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FIMPORT);
    }


}