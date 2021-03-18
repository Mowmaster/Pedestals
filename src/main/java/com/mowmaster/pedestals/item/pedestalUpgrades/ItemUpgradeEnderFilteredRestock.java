package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.enchants.*;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
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
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnderFilteredRestock extends ItemUpgradeBaseFilter
{
    public ItemUpgradeEnderFilteredRestock(Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptOpSpeed() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {return true;}

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if(stack.getItem() instanceof ItemUpgradeBase && enchantment.getRegistryName().getNamespace().equals(Reference.MODID))
        {
            return !EnchantmentRegistry.COINUPGRADE.equals(enchantment.type) && super.canApplyAtEnchantingTable(stack, enchantment);
        }
        return false;
    }

    @Override
    public int getItemEnchantability()
    {
        return 10;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return (stack.getCount()==1)?(super.isBookEnchantable(stack, book)):(false);
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
            ItemStack coinInPedestal = ped.getCoinOnPedestal();

            PlayerEntity player = ((ServerWorld) world).getPlayerByUuid(getPlayerFromCoin(coinInPedestal));
            if(player != null)
            {
                if(hasAdvancedInventoryTargeting(coinInPedestal))
                {
                    int range = player.inventory.getSizeInventory();
                    ItemStack stack = IntStream.range(0,range)//Int Range
                            .mapToObj((player.inventory)::getStackInSlot)//Function being applied to each interval
                            .filter(itemStack -> !itemStack.isEmpty())
                            .filter(itemStack -> itemStack.getCount() != itemStack.getMaxStackSize())
                            .filter(itemStack -> doItemsMatch(itemStack,itemStackIncoming))
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
            ItemStack coinInPedestal = ped.getCoinOnPedestal();
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


    //                          impTicker,this.world,   getItemInPedestal(),      getCoinOnPedestal(),     this.getBlockPos()
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

    //Upgrade checks each slot and inserts if it can
    //only inserts into slots with items, will not fill blank slots
    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {

        int i = getStoredInt(coinInPedestal);
        int upgradeTransferRate = 64;
        ItemStack itemFromPedestal = getStackInPedestal(world,posOfPedestal);
        //IF pedestal is empty and has nothing to transfer then dont do anything
        if(!itemFromPedestal.isEmpty() && !itemFromPedestal.equals(ItemStack.EMPTY))
        {
            PlayerEntity player = ((ServerWorld) world).getPlayerByUuid(getPlayerFromCoin(coinInPedestal));
            PlayerMainInvWrapper inventory = new PlayerMainInvWrapper(player.inventory);
            if(player != null)
            {
                if(hasAdvancedInventoryTargeting(coinInPedestal))
                {
                    ItemStack itemFromInventory = ItemStack.EMPTY;
                    itemFromPedestal = itemInPedestal.copy();
                    if(i < inventory.getSlots() && inventory.getStackInSlot(i) != null)
                    {
                        itemFromInventory = inventory.getStackInSlot(i);
                    }

                    if(i>=0 && i < inventory.getSlots())
                    {
                        if(inventory.isItemValid(i, itemFromPedestal))
                        {
                            int slotnext = getSlotNumberNext(i,inventory.getSlots(),itemFromPedestal,itemFromInventory);
                            if(doItemsMatch(itemFromPedestal,itemFromInventory ))
                            {
                                int spaceInInventoryStack = (inventory.getStackInSlot(i).isEmpty())?(0):(inventory.getStackInSlot(i).getMaxStackSize() - itemFromInventory.getCount());
                                //if inv slot is empty it should be able to handle as much as we can give it
                                int allowedTransferRate = upgradeTransferRate;
                                //checks allowed slot size amount and sets it if its lower then transfer rate
                                if(inventory.getStackInSlot(i).getMaxStackSize() <= allowedTransferRate) allowedTransferRate = inventory.getStackInSlot(i).getMaxStackSize();
                                //never have to check to see if pedestal and stack match because the slot checker does it for us
                                //if our transfer rate is bigger then what can go in the slot if its partially full we set the transfer size to what can fit
                                //Otherwise if space is bigger then rate we know it can accept as much as we're putting in
                                if(allowedTransferRate> spaceInInventoryStack) allowedTransferRate = spaceInInventoryStack;
                                //IF items in pedestal are less then the allowed transfer amount then set it as the amount
                                if(allowedTransferRate > itemFromPedestal.getCount()) allowedTransferRate = itemFromPedestal.getCount();

                                //After all calculations for transfer rate, set stack size to transfer and transfer the items
                                itemFromPedestal.setCount(allowedTransferRate);
                                if(inventory.insertItem(i,itemFromPedestal,true ).equals(ItemStack.EMPTY)){
                                    removeFromPedestal(world,posOfPedestal ,allowedTransferRate);
                                    inventory.insertItem(i,itemFromPedestal,false );
                                    writeStoredIntToNBT(coinInPedestal,slotnext);
                                }
                            }
                            else
                            {
                                writeStoredIntToNBT(coinInPedestal,slotnext);
                            }

                        }
                    }
                    else
                    {
                        int slotnext = getSlotNumberNext(i,inventory.getSlots(),itemFromPedestal,itemFromInventory);
                        writeStoredIntToNBT(coinInPedestal,slotnext);
                    }
                }
                else
                {
                    ItemStack itemFromInventory = ItemStack.EMPTY;
                    itemFromPedestal = itemInPedestal.copy();
                    if(i < player.getInventoryEnderChest().getSizeInventory() && player.getInventoryEnderChest().getStackInSlot(i) != null)
                    {
                        itemFromInventory = player.getInventoryEnderChest().getStackInSlot(i);
                    }

                    if(i>=0 && i < player.getInventoryEnderChest().getSizeInventory())
                    {
                        if(player.getInventoryEnderChest().isItemValidForSlot(i, itemFromPedestal))
                        {
                            int slotnext = getSlotNumberNext(i,player.getInventoryEnderChest().getSizeInventory(),itemFromPedestal,itemFromInventory);
                            if(doItemsMatch(itemFromPedestal,itemFromInventory ))
                            {
                                int spaceInInventoryStack = (player.getInventoryEnderChest().getStackInSlot(i).isEmpty())?(0):(player.getInventoryEnderChest().getStackInSlot(i).getMaxStackSize() - itemFromInventory.getCount());
                                //if inv slot is empty it should be able to handle as much as we can give it
                                int allowedTransferRate = upgradeTransferRate;
                                //checks allowed slot size amount and sets it if its lower then transfer rate
                                if(player.getInventoryEnderChest().getStackInSlot(i).getMaxStackSize() <= allowedTransferRate) allowedTransferRate = player.getInventoryEnderChest().getStackInSlot(i).getMaxStackSize();
                                //never have to check to see if pedestal and stack match because the slot checker does it for us
                                //if our transfer rate is bigger then what can go in the slot if its partially full we set the transfer size to what can fit
                                //Otherwise if space is bigger then rate we know it can accept as much as we're putting in
                                if(allowedTransferRate> spaceInInventoryStack) allowedTransferRate = spaceInInventoryStack;
                                //IF items in pedestal are less then the allowed transfer amount then set it as the amount
                                if(allowedTransferRate > itemFromPedestal.getCount()) allowedTransferRate = itemFromPedestal.getCount();

                                //After all calculations for transfer rate, set stack size to transfer and transfer the items
                                itemFromPedestal.setCount(allowedTransferRate);
                                ItemStack leftovers = player.getInventoryEnderChest().addItem(itemFromPedestal);
                                if(leftovers.isEmpty())
                                {
                                    removeFromPedestal(world,posOfPedestal,itemFromPedestal.getCount());
                                }
                                else
                                {
                                    int remover = itemFromPedestal.getCount()-leftovers.getCount();
                                    removeFromPedestal(world,posOfPedestal,remover);
                                }
                            }
                            else
                            {
                                writeStoredIntToNBT(coinInPedestal,slotnext);
                            }

                        }
                    }
                    else
                    {
                        int slotnext = getSlotNumberNext(i,player.getInventoryEnderChest().getSizeInventory(),itemFromPedestal,itemFromInventory);
                        writeStoredIntToNBT(coinInPedestal,slotnext);
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

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        if(map.size() > 0 && getNumNonPedestalEnchants(map)>0)
        {

            TranslationTextComponent enchant = new TranslationTextComponent(getDescriptionId() + ".chat_enchants");
            enchant.withStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,Util.NIL_UUID);

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea))
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.withStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants,Util.NIL_UUID);
                }
            }
        }

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

    public static final Item ENDERFRESTOCK = new ItemUpgradeEnderFilteredRestock(new Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/enderfilteredrestock"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ENDERFRESTOCK);
    }


}
