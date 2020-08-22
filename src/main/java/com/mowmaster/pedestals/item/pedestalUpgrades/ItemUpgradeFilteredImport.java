package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
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
    public ItemUpgradeFilteredImport(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);

            if(!world.isBlockPowered(pedestalPos))
            {
                if (tick%speed == 0) {
                    upgradeAction(world,pedestalPos,itemInPedestal,coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        int transferRate = getItemTransferRate(coinInPedestal);
        //int i = -1;

        ItemStack itemFromInv = ItemStack.EMPTY;
        //if(world.getTileEntity(posInventory) !=null)
        //{
        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(hasAdvancedInventoryTargeting(coinInPedestal))cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, posOfPedestal),true);

            if(cap.isPresent())
            {
                IItemHandler handler = cap.orElse(null);
                TileEntity invToPullFrom = world.getTileEntity(posInventory);
                if(invToPullFrom instanceof TilePedestal) {
                    itemFromInv = ItemStack.EMPTY;

                }
                else {
                    if(handler != null)
                    {
                        ItemStack toImport = ItemStack.EMPTY;
                        TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                        if(pedestalInv instanceof TilePedestal) {
                            List<BlockPos> getRecievers = ((TilePedestal) pedestalInv).getLocationList();
                            if(getRecievers.size()>0)
                            {
                                for(AtomicInteger slot = new AtomicInteger(0); slot.get()<getRecievers.size();slot.set(slot.get()+1))
                                {
                                    //System.out.println(getRecievers.get(slot.get()));
                                    TileEntity pedestalRec = world.getTileEntity(getRecievers.get(slot.get()));
                                    if(pedestalRec instanceof TilePedestal) {
                                        //Check if it has filter, if not return true
                                        if(((TilePedestal) pedestalRec).hasFilter(((TilePedestal) pedestalRec)))
                                        {
                                            //System.out.println("HAS FILTER");
                                            Item coinInPed = ((TilePedestal) pedestalRec).getCoinOnPedestal().getItem();
                                            if(coinInPed instanceof ItemUpgradeBaseFilter)
                                            {
                                                //Already checked if its a filter, so now check if it can accept items.
                                                if(cap.isPresent())
                                                {
                                                    if(handler != null)
                                                    {
                                                        int range = handler.getSlots();

                                                        ItemStack itemInInv = ItemStack.EMPTY;
                                                        itemInInv = IntStream.range(0,range)//Int Range
                                                                .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                                                                .filter(itemStack -> !itemStack.isEmpty())
                                                                .filter(itemStack -> ((ItemUpgradeBaseFilter) coinInPed).canAcceptCount(world,getRecievers.get(slot.get()),((TilePedestal)world.getTileEntity(getRecievers.get(slot.get()))).getItemInPedestal(),itemStack)>0)
                                                                .filter(itemStack -> ((ItemUpgradeBaseFilter) coinInPed).canAcceptItem(world,getRecievers.get(slot.get()),itemStack))
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
                                        else
                                        {
                                            //System.out.println("NO FILTER");
                                            Item coinInPed = ((TilePedestal) pedestalRec).getCoinOnPedestal().getItem();
                                            //Already checked if its a filter, so now check if it can accept items.
                                            if(cap.isPresent())
                                            {
                                                if(handler != null)
                                                {
                                                    int range = handler.getSlots();

                                                    ItemStack itemInInv = ItemStack.EMPTY;
                                                    itemInInv = IntStream.range(0,range)//Int Range
                                                            .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                                                            .filter(itemStack -> !itemStack.isEmpty())
                                                            .filter(itemStack -> ((TilePedestal) pedestalInv).canSendToPedestal(getRecievers.get(slot.get()),itemStack))
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
                                }
                            }

                            //System.out.println(toImport.getDisplayName());
                            int i = getSlotWithMatchingStackExact(cap,toImport);
                            //System.out.println(i);
                            if(i>=0)
                            {
                                int maxStackSizeAllowedInPedestal = 0;
                                int roomLeftInPedestal = 0;
                                itemFromInv = handler.getStackInSlot(i);
                                ItemStack itemFromPedestal = getStackInPedestal(world,posOfPedestal);
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

                                    ItemStack copyIncoming = itemFromInv.copy();
                                    copyIncoming.setCount(allowedTransferRate);
                                    handler.extractItem(i,allowedTransferRate ,false );
                                    ((TilePedestal) pedestalInv).addItem(copyIncoming);
                                }
                            }
                        }
                    }
                }
            }
        //}

    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
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

    public static final Item FIMPORT = new ItemUpgradeFilteredImport(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fimport"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FIMPORT);
    }


}