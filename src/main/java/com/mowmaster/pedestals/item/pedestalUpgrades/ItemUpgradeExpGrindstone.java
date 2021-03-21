package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
import java.util.*;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeExpGrindstone extends ItemUpgradeBaseExp
{

    public ItemUpgradeExpGrindstone(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int getMaxXpValue = getExpCountByLevel(getExpBuffer(coinInPedestal));
            if(!hasMaxXpSet(coinInPedestal) || readMaxXpFromNBT(coinInPedestal) != getMaxXpValue) {setMaxXP(coinInPedestal, getMaxXpValue);}
            upgradeActionSendExp(pedestal);

            int speed = getOperationSpeed(coinInPedestal);
            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    //Just does the unenchanting bit
                    if(itemInPedestal.isEmpty())
                    {
                        grindstoneAction(pedestal);
                    }
                }
            }
        }
    }

    public int getItemsExpDisenchantAmount(ItemStack stack)
    {
        int exp = 0;
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer integer = entry.getValue();

            exp += enchantment.getMinEnchantability(integer.intValue());
        }
        return exp*stack.getCount();
    }

    public void grindstoneAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos posOfPedestal = pedestal.getPos();

        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        ItemStack itemFromInv = ItemStack.EMPTY;
        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(hasAdvancedInventoryTargeting(coinInPedestal))cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(!isInventoryEmpty(cap))
        {
            if(cap.isPresent())
            {
                IItemHandler handler = cap.orElse(null);
                TileEntity invToPullFrom = world.getTileEntity(posInventory);
                if (((hasAdvancedInventoryTargeting(coinInPedestal) && invToPullFrom instanceof PedestalTileEntity)||!(invToPullFrom instanceof PedestalTileEntity))?(false):(true)) {
                    itemFromInv = ItemStack.EMPTY;
                }
                else {
                    if(handler != null)
                    {
                        int range = handler.getSlots();
                        ItemStack nextItemToGrind = ItemStack.EMPTY;
                        nextItemToGrind = IntStream.range(0,range)//Int Range
                                .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                                .filter(itemStack -> itemStack.isEnchanted() || itemStack.getItem() instanceof EnchantedBookItem)
                                .findFirst().orElse(ItemStack.EMPTY);

                        if(!nextItemToGrind.isEmpty())
                        {
                            int slotItemToGrind = getSlotWithMatchingStackExact(cap,nextItemToGrind);
                            itemFromInv = handler.getStackInSlot(slotItemToGrind);
                            int maxXp = readMaxXpFromNBT(coinInPedestal);
                            int currentlyStoredExp = getXPStored(coinInPedestal);
                            int xpDisenchant = getItemsExpDisenchantAmount(itemFromInv);
                            if(maxXp - currentlyStoredExp >= xpDisenchant)
                            {
                                //Code Here
                                Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                ItemStack stackToReturn = (itemFromInv.getItem() instanceof EnchantedBookItem)?(new ItemStack(Items.BOOK,1)):(itemFromInv.copy());
                                EnchantmentHelper.setEnchantments(enchantsNone,stackToReturn);
                                if(!stackToReturn.isEmpty())
                                {
                                    int getExpLeftInPedestal = currentlyStoredExp + xpDisenchant;
                                    setXPStored(coinInPedestal,getExpLeftInPedestal);

                                    ItemStack toReturn = stackToReturn.copy();
                                    handler.extractItem(slotItemToGrind,toReturn.getCount(),false);
                                    world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                    pedestal.addItem(toReturn);
                                }
                            }
                        }
                        else
                        {
                            ItemStack nextItemToRemove = ItemStack.EMPTY;
                            nextItemToRemove = IntStream.range(0,range)//Int Range
                                    .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                                    .filter(itemStack -> !itemStack.isEnchanted() || !(itemStack.getItem() instanceof EnchantedBookItem))
                                    .findFirst().orElse(ItemStack.EMPTY);

                            int slotItemToRemove = getSlotWithMatchingStackExact(cap,nextItemToRemove);
                            ItemStack toReturn = nextItemToRemove.copy();
                            handler.extractItem(slotItemToRemove,toReturn.getCount(),false);
                            pedestal.addItem(toReturn);
                        }
                    }
                }
            }
        }
    }

    public int getExpBuffer(ItemStack stack)
    {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int overEnchanted = (capacityOver*5)+30;

        //20k being the max before we get close to int overflow
        return  (overEnchanted>=maxLVLStored)?(maxLVLStored):(overEnchanted);
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".chat_xp");
        xpstored.appendString(""+ getExpLevelFromCount(getXPStored(stack)) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored,Util.DUMMY_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString(getExpTransferRateString(stack));
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate, Util.DUMMY_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
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
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString(getExpTransferRateString(stack));
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(speed);
    }

    public static final Item XPGRINDSTONE = new ItemUpgradeExpGrindstone(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xpgrindstone"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPGRINDSTONE);
    }


}
