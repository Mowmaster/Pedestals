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

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeImport extends ItemUpgradeBase
{
    public ItemUpgradeImport(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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
                    upgradeAction(world,pedestalPos,coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        int transferRate = getItemTransferRate(coinInPedestal);

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
                        int i = getNextSlotWithItemsCap(cap,getStackInPedestal(world,posOfPedestal));
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
                                TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                                if(pedestalInv instanceof TilePedestal) {
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
    public void actionOnCollideWithBlock(World world, TilePedestal tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
            ItemStack itemFromPedestal = getStackInPedestal(world,posPedestal);
            if(itemFromPedestal.isEmpty())
            {
                TileEntity pedestalInv = world.getTileEntity(posPedestal);
                if(pedestalInv instanceof TilePedestal) {
                    entityIn.remove();
                    ((TilePedestal) pedestalInv).addItem(getItemStack);
                }
            }
        }
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

    public static final Item IMPORT = new ItemUpgradeImport(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/import"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(IMPORT);
    }


}