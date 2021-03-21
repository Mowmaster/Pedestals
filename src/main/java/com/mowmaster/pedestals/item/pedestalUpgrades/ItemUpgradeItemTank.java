package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeItemTank extends ItemUpgradeBase
{
    private int maxStored = 2000000000;

    public ItemUpgradeItemTank(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptOpSpeed() {
        return false;
    }

    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            if(!getItemStored(coin).isEmpty())
            {
                float f = (float)getItemStored(coin).getCount()/(float)readMaxStorageFromNBT(coin);
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    @Override
    public boolean customIsValid(PedestalTileEntity pedestal, int slot, @Nonnull ItemStack stack)
    {
        ItemStack stored = getItemStored(pedestal);
        ItemStack inPed = pedestal.getItemInPedestalOverride();
        if(stored.isEmpty() && inPed.isEmpty())
        {
            return (slot==0)?(true):(false);
        }
        else if(!stored.isEmpty() && inPed.isEmpty())
        {
            return (slot==0)?(doItemsMatch(stored,stack)):(false);
        }
        else if(stored.isEmpty() && !inPed.isEmpty())
        {
            return (slot==0)?(doItemsMatch(inPed,stack)):(false);
        }
        else
        {
            //Recommended by intelij: (slot==0)&&(doItemsMatch(inPed,stack) && doItemsMatch(stored,stack))
            return (slot==0)?(doItemsMatch(inPed,stack) && doItemsMatch(stored,stack)):(false);

        }
    }

    @Override
    public ItemStack customInsertItem(PedestalTileEntity pedestal, ItemStack stackIn, boolean simulate)
    {
        //Return stack to be inserted if nothing can be accepted, otherwise return Empty if All can be inserted
        ItemStack stored = getItemStored(pedestal);
        ItemStack inPed = pedestal.getItemInPedestalOverride();

        if(customIsValid(pedestal,0,stackIn))
        {
            if(stored.isEmpty())
            {
                if(!simulate)setItemStored(pedestal,stackIn);
                return ItemStack.EMPTY;
            }
            else
            {

                int itemsToAdd = addCountToStorage(pedestal,stackIn.getCount(),true);
                if(availableStorageSpace(pedestal)>0)
                {
                    if(itemsToAdd==0)
                    {
                        if(!simulate)
                        {
                            addCountToStorage(pedestal,stackIn.getCount(),false);
                        }
                        return ItemStack.EMPTY;
                    }
                    else
                    {
                        ItemStack copyStackIn = stackIn.copy();
                        copyStackIn.setCount(itemsToAdd);
                        if(!simulate)
                        {
                            addCountToStorage(pedestal,itemsToAdd,false);
                        }
                        int currentIn = stackIn.getCount();
                        int diff = currentIn - itemsToAdd;
                        copyStackIn.setCount(diff);
                        return copyStackIn;
                    }
                }
            }
        }

        return stackIn;
    }

    @Override
    public ItemStack customExtractItem(PedestalTileEntity pedestal, int amountOut, boolean simulate)
    {
        //Return stack that was extracted, (it cant be more then the amountOut or max size)
        ItemStack stored = getItemStored(pedestal);
        ItemStack itemInPed = pedestal.getItemInPedestalOverride();
        int itemsToRemove = removeFromStorage(pedestal,amountOut,true);
        ItemStack itemStackToExtract = stored.copy();
        if(stored.isEmpty())
        {
            //Should default to normal pedestal pull out methods
            return new ItemStack(Items.COMMAND_BLOCK);
        }
        else if(itemsToRemove==0)
        {
            itemStackToExtract.setCount(amountOut);
            if(!simulate)
            {
                removeFromStorage(pedestal,amountOut,false);
            }

            ItemStack toReturn = new ItemStack((stored.isEmpty())?(itemInPed.getItem()):(itemStackToExtract.getItem()),(amountOut>itemStackToExtract.getMaxStackSize())?(itemStackToExtract.getMaxStackSize()):(amountOut));
            toReturn.setTag((stored.isEmpty())?(itemInPed.getTag()):(itemStackToExtract.getTag()));

            return toReturn;
        }
        else
        {
            itemStackToExtract.setCount(itemsToRemove);
            if(!simulate)
            {
                removeFromStorage(pedestal,itemsToRemove,false);
            }
            return itemStackToExtract;
        }
    }

    @Override
    public ItemStack customStackInSlot(PedestalTileEntity pedestal, ItemStack stackFromHandler)
    {
        //Somehow when sending items fast, theyll go into the buffer before an item in the stack spawns
        if(!stackFromHandler.isEmpty())
        {
            ItemStack stored = getItemStored(pedestal);
            Item getItem = stored.getItem();
            ItemStack getItemStackInPedestal = pedestal.getItemInPedestalOverride();
            Item getItemInPedestal = getItemStackInPedestal.getItem();
            int storedCount = stored.getCount();
            //Basically if the coin and the stack in pedestal dont match, return whats in the pedestal
            if(getItemInPedestal.equals(getItem) || getItemStackInPedestal.isEmpty())
            {
                if(storedCount>0)
                {
                    int amount = storedCount+pedestal.getItemInPedestalOverride().getCount();
                    ItemStack getStack = new ItemStack(getItem,amount);
                    return getStack;
                }
            }
            else
            {
                return getItemStackInPedestal;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int customSlotLimit(PedestalTileEntity pedestal)
    {
        return maxStorage(pedestal);
    }

    public int addCountToStorage(PedestalTileEntity pedestal, int amountIn ,boolean simulate)
    {
        //Returns 0 if it can add all cobble, otherwise returns the amount you could add
        ItemStack stored = getItemStored(pedestal);
        int space = availableStorageSpace(pedestal);
        if(space>=amountIn)
        {
            ItemStack storedCopy = stored.copy();
            if(!simulate)
            {
                int current = stored.getCount();
                storedCopy.setCount(current+amountIn);
                setItemStored(pedestal,storedCopy);
            }
            return 0;
        }
        return space;
    }

    public int removeFromStorageBuffer(PedestalTileEntity pedestal, int amountOut ,boolean simulate)
    {
        //Returns 0 if it can remove all cobble, otherwise returns the amount you could remove
        ItemStack stored = getItemStored(pedestal);
        int current = stored.getCount();
        if((current - amountOut)>=0)
        {
            if(!simulate)
            {
                ItemStack storedCopy = stored.copy();
                storedCopy.setCount((current - amountOut));
                setItemStored(pedestal,storedCopy);
            }
            return 0;
        }
        return current;
    }

    public int removeFromStorage(PedestalTileEntity pedestal, int amountOut ,boolean simulate)
    {
        //Returns 0 if it can remove all cobble, otherwise returns the amount you could remove
        ItemStack stored = getItemStored(pedestal);
        int current = stored.getCount();
        int currentActual = current+pedestal.getItemInPedestalOverride().getCount();
        if((currentActual - amountOut)>=0)
        {
            ItemStack storedCopy = stored.copy();
            if(!simulate)
            {
                if((current - amountOut)>=0)
                {
                    storedCopy.setCount((current - amountOut));
                    setItemStored(pedestal,storedCopy);
                }
                else
                {
                    int removeFromPedestal = amountOut - current;
                    setItemStored(pedestal,ItemStack.EMPTY);
                    pedestal.removeItemOverride(removeFromPedestal);
                }

            }
            return 0;
        }
        return current;
    }

    public int getCountStored(PedestalTileEntity pedestal)
    {
        return getItemStored(pedestal).getCount();
    }

    public int maxStorage(PedestalTileEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return readMaxStorageFromNBT(coin);
    }

    public int availableStorageSpace(PedestalTileEntity pedestal)
    {
        return maxStorage(pedestal)-getCountStored(pedestal);
    }

    public void setItemStored(PedestalTileEntity pedestal, ItemStack stack)
    {
        //The itemstack.write() uses a byte value for the count so we have to store the actual count seperately
        int countToStore = stack.getCount();
        //after getting the count for our purposes, to prevent the stack from nulling itsself we need to make sure its below max stack size
        if(stack.getCount()>=stack.getMaxStackSize())stack.setCount(stack.getMaxStackSize());
        ItemStack coin = pedestal.getCoinOnPedestal();

        CompoundNBT coinNBT = (coin.hasTag())?(coin.getTag()):(new CompoundNBT());
        stack.write(coinNBT);
        coinNBT.putInt("tankcount",countToStore);

        if(stack.isEmpty()||stack.getCount()<=0)
        {
            if(coinNBT.contains("id"))coinNBT.remove("id");
            if(coinNBT.contains("Count"))coinNBT.remove("Count");
            if(coinNBT.contains("tag"))coinNBT.remove("tag");
            if(coinNBT.contains("ForgeCaps"))coinNBT.remove("ForgeCaps");
            coin.setTag(coinNBT);
        }

        coin.setTag(coinNBT);
        pedestal.update();
    }

    public ItemStack getItemStored(PedestalTileEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
        }

        ItemStack getItemStored = coin.read(compound);
        if(!getItemStored.isEmpty())
        {
            int getItemStackCount = compound.getInt("tankcount");
            getItemStored.setCount(getItemStackCount);
        }

        //Should return the stack with any nbt on it???
        //.println("Get Stored: "+ getItemStored.getDisplayName().getString());
        return getItemStored;
    }

    public ItemStack getItemStored(ItemStack coin)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
        }

        ItemStack getItemStored = coin.read(compound);
        if(!getItemStored.isEmpty())
        {
            int getItemStackCount = compound.getInt("tankcount");
            getItemStored.setCount(getItemStackCount);
        }


        //Should return the stack with any nbt on it???
        return getItemStored;
    }

    public boolean hasMaxStorageSet(ItemStack stack)
    {
        boolean returner = false;
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("maxstorage"))
            {
                returner = true;
            }
        }
        return returner;
    }

    public void writeMaxStorageToNBT(ItemStack stack, int value)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("maxstorage",value);
        stack.setTag(compound);
    }

    public int readMaxStorageFromNBT(ItemStack stack)
    {
        int maxStorage = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            maxStorage = getCompound.getInt("maxstorage");
        }
        return maxStorage;
    }

    //nerfed technically, but also give the ability to set storage to a nice number
    public int getStorageBuffer(ItemStack stack) {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        //1728 = 27*64 (size of double chest)
        int storageBuffer = (int)(Math.pow(4,(capacityOver>=33)?(33):(capacityOver)+1)*1728);

        return  (storageBuffer>=Integer.MAX_VALUE)?(Integer.MAX_VALUE):(storageBuffer);
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int getMaxStorageValue = getStorageBuffer(coinInPedestal);
            if(!hasMaxStorageSet(coinInPedestal) || readMaxStorageFromNBT(coinInPedestal) != getMaxStorageValue) {writeMaxStorageToNBT(coinInPedestal, getMaxStorageValue);}

            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                if (world.getGameTime()%20 == 0) {
                    //Keep Pedestal Full at all times
                    ItemStack stackInPed = pedestal.getItemInPedestalOverride();
                    if(stackInPed.getCount() < stackInPed.getMaxStackSize())
                    {
                        fillPedestalAction(pedestal);
                    }
                }
            }
        }
    }

    public void fillPedestalAction(PedestalTileEntity pedestal)
    {
        ItemStack itemInPedestal = pedestal.getItemInPedestalOverride();
        int intSpace = intSpaceLeftInStack(itemInPedestal);
        int cobbleStored = this.getCountStored(pedestal);
        if(intSpace>0 && cobbleStored>0)
        {

            int returned = removeFromStorageBuffer(pedestal,intSpace,true);
            int itemsToAdd = (returned==0)?(intSpace):(returned);
            ItemStack stackSpawnedItem = getItemStored(pedestal).copy();//new ItemStack(.getItem(),itemsToAdd);
            stackSpawnedItem.setCount(itemsToAdd);
            ItemStack getInserted = pedestal.addItemStackOverride(stackSpawnedItem);
            if(getInserted.isEmpty())
            {
                removeFromStorageBuffer(pedestal,itemsToAdd,false);
            }
            else
            {
                int countUsed = itemsToAdd - getInserted.getCount();
                removeFromStorageBuffer(pedestal,countUsed,false);
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {

    }

    @Override
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack stored = getItemStored(tilePedestal);
            ItemStack stackCollidedItem = ((ItemEntity) entityIn).getItem();
            //if completely empty OR matching
            if(tilePedestal.addItemCustom(stackCollidedItem,true).isEmpty())
            {
                tilePedestal.addItemCustom(stackCollidedItem,false);
                ((ItemEntity) entityIn).remove();
            }
            else
            {
                ItemStack unused = tilePedestal.addItemCustom(stackCollidedItem,false).copy();
                ((ItemEntity) entityIn).setItem(unused);
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

        ItemStack storedStack = getItemStored(pedestal);
        TranslationTextComponent stored = new TranslationTextComponent(getTranslationKey() + ".chat_stored");
        if(!storedStack.isEmpty())
        {
            stored.appendString("" +  storedStack.getDisplayName().getString() + " - ");
            stored.appendString("" +  (storedStack.getCount()+pedestal.getItemInPedestalOverride().getCount()) + "");
            stored.mergeStyle(TextFormatting.GREEN);
            player.sendMessage(stored, Util.DUMMY_UUID);
        }
        else
        {
            if(!pedestal.getItemInPedestalOverride().isEmpty())
            {
                stored.appendString("" +  pedestal.getItemInPedestalOverride().getDisplayName().getString() + " - ");
                stored.appendString("" +  (pedestal.getItemInPedestalOverride().getCount()) + "");
                stored.mergeStyle(TextFormatting.GREEN);
                player.sendMessage(stored, Util.DUMMY_UUID);
            }
        }


        TranslationTextComponent buffer = new TranslationTextComponent(getTranslationKey() + ".chat_buffer");
        buffer.appendString("" + readMaxStorageFromNBT(stack) + "");
        buffer.mergeStyle(TextFormatting.AQUA);
        player.sendMessage(buffer, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        ItemStack storedStack = getItemStored(stack);
        if(!storedStack.isEmpty())
        {
            TranslationTextComponent stored = new TranslationTextComponent(getTranslationKey() + ".chat_stored");
            stored.appendString("" +  storedStack.getDisplayName().getString() + " - ");
            stored.appendString("" +  (storedStack.getCount()) + "");
            stored.mergeStyle(TextFormatting.GREEN);
            tooltip.add(stored);
        }

        TranslationTextComponent buffer = new TranslationTextComponent(getTranslationKey() + ".chat_buffer");
        buffer.appendString("" + getStorageBuffer(stack) + "");
        buffer.mergeStyle(TextFormatting.AQUA);
        tooltip.add(buffer);
    }

    public static final Item ITEMTANK = new ItemUpgradeItemTank(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/itemtank"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITEMTANK);
    }


}