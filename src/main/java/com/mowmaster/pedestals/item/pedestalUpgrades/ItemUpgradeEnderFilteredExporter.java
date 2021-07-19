package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
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
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnderFilteredExporter extends ItemUpgradeBase
{
    public ItemUpgradeEnderFilteredExporter(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptAdvanced() {
        return super.canAcceptAdvanced();
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();
            int speed = getOperationSpeed(coinInPedestal);

            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal,world,pedestalPos,itemInPedestal,coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal, World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {
        ServerWorld sworld = world.getServer().getWorld(world.getDimensionKey());
        PlayerEntity player = sworld.getPlayerByUuid(getPlayerFromCoin(coinInPedestal));
        if(player != null)
        {
            if(itemInPedestal.isEmpty())
            {
                if(hasAdvancedInventoryTargeting(coinInPedestal))
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
                            for(AtomicInteger slot = new AtomicInteger(0); slot.get()<getRecievers.size(); slot.set(slot.get()+1))
                            {
                                BlockPos receiverConnection = getRecievers.get(slot.get());
                                TileEntity pedestalRec = world.getTileEntity(receiverConnection);
                                if(pedestalRec instanceof PedestalTileEntity) {
                                    PedestalTileEntity receiverPedestal = ((PedestalTileEntity) pedestalRec);
                                    if(receiverPedestal.hasFilter(senderPedestal))
                                    {
                                        Item coinInPed = receiverPedestal.getCoinOnPedestal().getItem();
                                        int range = player.inventory.getSizeInventory();
                                        ItemStack itemInInv = ItemStack.EMPTY;
                                        itemInInv = IntStream.range(0,range)
                                                .mapToObj((player.inventory)::getStackInSlot)
                                                .filter(itemStack -> !itemStack.isEmpty())
                                                .filter(itemStack -> canAcceptItem(world,receiverConnection,itemStack))
                                                .filter(itemStack -> canAcceptCount(world,receiverConnection,((PedestalTileEntity)world.getTileEntity(receiverConnection)).getItemInPedestal(),itemStack)>0)
                                                .filter(itemStack -> passesItemFilter(pedestal,itemStack))
                                                .findFirst().orElse(ItemStack.EMPTY);
                                        if(!itemInInv.isEmpty())
                                        {
                                            toImport = itemInInv.copy();
                                            break;
                                        }
                                    }
                                    else
                                    {
                                        int range = player.inventory.getSizeInventory();
                                        ItemStack itemInInv = ItemStack.EMPTY;
                                        itemInInv = IntStream.range(0,range)//Int Range
                                                .mapToObj((player.inventory)::getStackInSlot)//Function being applied to each interval
                                                .filter(itemStack -> !itemStack.isEmpty())
                                                .filter(itemStack -> senderPedestal.canSendToPedestal(receiverConnection,itemStack))
                                                .filter(itemStack -> passesItemFilter(pedestal,itemStack))
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


                        //After an Item Allowed to be sent is found.
                        int i = getPlayerSlotWithMatchingStackExact(player.inventory,toImport);
                        if(i>=0)
                        {
                            int maxStackSizeAllowedInPedestal = 0;
                            int roomLeftInPedestal = 0;
                            ItemStack senderBelowInvStack = player.inventory.getStackInSlot(i);
                            ItemStack itemFromPedestal = getStackInPedestal(world,posOfPedestal);
                            if(senderBelowInvStack != null && !senderBelowInvStack.isEmpty() && senderBelowInvStack.getItem() != Items.AIR)
                            {
                                if(itemFromPedestal.isEmpty() || itemFromPedestal.equals(ItemStack.EMPTY))
                                {maxStackSizeAllowedInPedestal = 64;}
                                else
                                {maxStackSizeAllowedInPedestal = itemFromPedestal.getMaxStackSize();}
                                roomLeftInPedestal = maxStackSizeAllowedInPedestal-itemFromPedestal.getCount();
                                int itemCountInInv = senderBelowInvStack.getCount();
                                int transferRate = 64;
                                int allowedTransferRate = transferRate;
                                if(roomLeftInPedestal < transferRate) allowedTransferRate = roomLeftInPedestal;
                                if(itemCountInInv < allowedTransferRate) allowedTransferRate = itemCountInInv;

                                ItemStack copyIncoming = senderBelowInvStack.copy();
                                copyIncoming.setCount(allowedTransferRate);

                                addToPedestal(world,posOfPedestal,copyIncoming);
                                ItemStack stackToSetInEnder = senderBelowInvStack.copy();
                                stackToSetInEnder.setCount(senderBelowInvStack.getCount()-allowedTransferRate);
                                player.inventory.setInventorySlotContents(i,stackToSetInEnder);
                            }
                        }
                    }
                }
                else
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
                            for(AtomicInteger slot = new AtomicInteger(0); slot.get()<getRecievers.size(); slot.set(slot.get()+1))
                            {
                                BlockPos receiverConnection = getRecievers.get(slot.get());
                                TileEntity pedestalRec = world.getTileEntity(receiverConnection);
                                if(pedestalRec instanceof PedestalTileEntity) {
                                    PedestalTileEntity receiverPedestal = ((PedestalTileEntity) pedestalRec);
                                    if(receiverPedestal.hasFilter(senderPedestal))
                                    {
                                        Item coinInPed = receiverPedestal.getCoinOnPedestal().getItem();
                                        int range = player.getInventoryEnderChest().getSizeInventory();
                                        ItemStack itemInInv = ItemStack.EMPTY;
                                        itemInInv = IntStream.range(0,range)
                                                .mapToObj((player.getInventoryEnderChest())::getStackInSlot)
                                                .filter(itemStack -> !itemStack.isEmpty())
                                                .filter(itemStack -> canAcceptItem(world,receiverConnection,itemStack))
                                                .filter(itemStack -> canAcceptCount(world,receiverConnection,((PedestalTileEntity)world.getTileEntity(receiverConnection)).getItemInPedestal(),itemStack)>0)
                                                .filter(itemStack -> passesItemFilter(pedestal,itemStack))
                                                .findFirst().orElse(ItemStack.EMPTY);
                                        if(!itemInInv.isEmpty())
                                        {
                                            toImport = itemInInv.copy();
                                            break;
                                        }
                                    }
                                    else
                                    {
                                        int range = player.getInventoryEnderChest().getSizeInventory();
                                        ItemStack itemInInv = ItemStack.EMPTY;
                                        itemInInv = IntStream.range(0,range)//Int Range
                                                .mapToObj((player.getInventoryEnderChest())::getStackInSlot)//Function being applied to each interval
                                                .filter(itemStack -> !itemStack.isEmpty())
                                                .filter(itemStack -> senderPedestal.canSendToPedestal(receiverConnection,itemStack))
                                                .filter(itemStack -> passesItemFilter(pedestal,itemStack))
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


                        //After an Item Allowed to be sent is found.
                        int i = getEnderChestSlotWithMatchingStackExact(player.getInventoryEnderChest(),toImport);
                        if(i>=0)
                        {
                            int maxStackSizeAllowedInPedestal = 0;
                            int roomLeftInPedestal = 0;
                            ItemStack senderBelowInvStack = player.getInventoryEnderChest().getStackInSlot(i);
                            ItemStack itemFromPedestal = getStackInPedestal(world,posOfPedestal);
                            if(senderBelowInvStack != null && !senderBelowInvStack.isEmpty() && senderBelowInvStack.getItem() != Items.AIR)
                            {
                                if(itemFromPedestal.isEmpty() || itemFromPedestal.equals(ItemStack.EMPTY))
                                {maxStackSizeAllowedInPedestal = 64;}
                                else
                                {maxStackSizeAllowedInPedestal = itemFromPedestal.getMaxStackSize();}
                                roomLeftInPedestal = maxStackSizeAllowedInPedestal-itemFromPedestal.getCount();
                                int itemCountInInv = senderBelowInvStack.getCount();
                                int transferRate = 64;
                                int allowedTransferRate = transferRate;
                                if(roomLeftInPedestal < transferRate) allowedTransferRate = roomLeftInPedestal;
                                if(itemCountInInv < allowedTransferRate) allowedTransferRate = itemCountInInv;

                                ItemStack copyIncoming = senderBelowInvStack.copy();
                                copyIncoming.setCount(allowedTransferRate);

                                addToPedestal(world,posOfPedestal,copyIncoming);
                                ItemStack stackToSetInEnder = senderBelowInvStack.copy();
                                stackToSetInEnder.setCount(senderBelowInvStack.getCount()-allowedTransferRate);
                                player.getInventoryEnderChest().setInventorySlotContents(i,stackToSetInEnder);
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
        ItemStack coin = pedestal.getCoinOnPedestal();
        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name, Util.DUMMY_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(coin);
        if(map.size() > 0 && getNumNonPedestalEnchants(map)>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getTranslationKey() + ".chat_enchants");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,Util.DUMMY_UUID);

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea))
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.mergeStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants,Util.DUMMY_UUID);
                }
            }
        }

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

    public static final Item ENDERFEXPORT = new ItemUpgradeEnderFilteredExporter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/enderfilteredexport"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ENDERFEXPORT);
    }


}