package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.system.CallbackI;

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
            if(!getItemStored(pedestal).isEmpty())
            {
                float f = (float)getItemStored(pedestal).getCount()/(float)readMaxStorageFromNBT(coin);
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    @Override
    public ItemStack customExtractItem(PedestalTileEntity pedestal, int amountOut, boolean simulate)
    {
        //Return stack that was extracted, (it cant be more then the amountOut or max size)
        ItemStack stored = getItemStored(pedestal);
        ItemStack itemInPed = pedestal.getItemInPedestalOverride();
        int itemsToRemove = removeFromStorage(pedestal,amountOut,true);
        ItemStack itemStackToExtract = stored.copy();
        if(itemsToRemove==0)
        {
            itemStackToExtract.setCount(amountOut);
            if(!simulate)
            {
                removeFromStorage(pedestal,amountOut,false);
            }
            return new ItemStack((stored.isEmpty())?(itemInPed.getItem()):(itemStackToExtract.getItem()),(amountOut>itemStackToExtract.getMaxStackSize())?(itemStackToExtract.getMaxStackSize()):(amountOut));
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
    public ItemStack customInsertItem(PedestalTileEntity pedestal, ItemStack stackIn, boolean simulate)
    {
        //Return stack to be inserted if nothing can be accepted, otherwise return Empty if All can be inserted
        ItemStack stored = getItemStored(pedestal);
        if(stored.isItemEqual(stackIn) || pedestal.getItemInPedestalOverride().isEmpty() || stored.isEmpty())
        {
            if(pedestal.getItemInPedestalOverride().isEmpty() && stored.isEmpty())
            {

                if(!simulate)
                {
                    //System.out.println("Triggered1");
                    setItemStored(pedestal,stackIn);
                }
                return ItemStack.EMPTY;
            }
            else if(stored.isEmpty() && pedestal.getItemInPedestalOverride().isItemEqual(stackIn))
            {
                if(!simulate)
                {
                    //System.out.println("Triggered2");
                    setItemStored(pedestal,stackIn);
                }
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
                            //System.out.println("Triggered3");
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
                            //System.out.println("Triggered4");
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
    public ItemStack customStackInSlot(PedestalTileEntity pedestal, ItemStack stackFromHandler)
    {
        if(!stackFromHandler.isEmpty())
        {
            ItemStack stored = getItemStored(pedestal);
            if(!stored.isEmpty())
            {
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
                System.out.println(getItemStored(pedestal));
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

    public CompoundNBT writeStack(CompoundNBT nbt,ItemStack stack) {
        ResourceLocation resourcelocation = Registry.ITEM.getKey(stack.getItem());
        nbt.putString(Reference.MODID+"_"+"id", resourcelocation == null?"minecraft:air":resourcelocation.toString());
        nbt.putInt(Reference.MODID+"_"+"Count", (int)stack.getCount());
        if(stack.getTag() != null) {
            nbt.put(Reference.MODID+"_"+"tag", stack.getTag().copy());
        }

        //Might just have to make my own item count storage and use this for the item
        /*CompoundNBT cnbt = stack.serializeCaps();
        if(cnbt != null && !cnbt.isEmpty()) {
            nbt.put("ForgeCaps", cnbt);
        }*/

        return nbt;
    }

    public void setItemStored(PedestalTileEntity pedestal, ItemStack stack)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
        }

        compound = writeStack(compound,stack);
        coin.setTag(compound);
        pedestal.update();
    }

    public ItemStack readStackFromNBT(CompoundNBT compound) {
        CompoundNBT compoundNBT = new CompoundNBT();
        Item getItem = ItemStack.EMPTY.getItem();
        int getCount = 0;
        getItem = (Item)Registry.ITEM.getOrDefault(new ResourceLocation(compound.getString("id")));
        getCount = compound.getInt(Reference.MODID+"_"+"Count");
        if(compound.contains(Reference.MODID+"_"+"tag", 10)) {
            compoundNBT = compound.getCompound(Reference.MODID+"_"+"tag");
            getItem.updateItemStackNBT(compound);
        }

        return new ItemStack(getItem,getCount);
    }

    public ItemStack getItemStored(PedestalTileEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
        }

        ItemStack returner = readStackFromNBT(compound);

        return returner;
    }

    /*
    public void setItemStored(PedestalTileEntity pedestal, ItemStack stack)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
        }

        compound = stack.write(compound);
        coin.setTag(compound);
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

        ItemStack returner = coin.read(compound);
        pedestal.update();

        return returner;
    }

    public ItemStack getItemStored(ItemStack coin)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
        }

        ItemStack returner = coin.read(compound);

        return returner;
    }
     */

    public ItemStack getItemStored(ItemStack coin)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
        }

        ItemStack returner = coin.read(compound);

        return returner;
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

    public int getStorageBuffer(ItemStack stack) {
        int storageBuffer = 6912;
        switch (getCapacityModifier(stack))
        {
            //1Double chest = 1728
            case 0:
                storageBuffer = 6912;//4x
                break;
            case 1:
                storageBuffer = 27648;//16x
                break;
            case 2:
                storageBuffer = 110592;//64x
                break;
            case 3:
                storageBuffer = 442368;//256x
                break;
            case 4:
                storageBuffer = 1769472;//1024x
                break;
            case 5:
                storageBuffer = maxStored;//A Lot
                break;
            default: storageBuffer = 6912;
        }

        return  storageBuffer;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();

        if(!world.isRemote)
        {
            int getMaxStorageValue = getStorageBuffer(coinInPedestal);
            if(!hasMaxStorageSet(coinInPedestal) || readMaxStorageFromNBT(coinInPedestal) != getMaxStorageValue) {writeMaxStorageToNBT(coinInPedestal, getMaxStorageValue);}

            if(!world.isBlockPowered(pedestalPos))
            {
                //Keep Pedestal Full at all times
                ItemStack stackInPed = pedestal.getItemInPedestalOverride();
                if(stackInPed.getCount() < stackInPed.getMaxStackSize())
                {
                    fillPedestalAction(pedestal);
                }
            }
        }
    }

    public void fillPedestalAction(PedestalTileEntity pedestal)
    {
        ItemStack stored = getItemStored(pedestal);
        ItemStack itemInPedestal = pedestal.getItemInPedestalOverride();
        int intSpace = intSpaceLeftInStack(itemInPedestal);
        int cobbleStored = stored.getCount();
        ItemStack stackSpawnedItem = new ItemStack(stored.getItem(),intSpace);
        if(intSpace>0 && cobbleStored>0)
        {

            int itemsToRemove = this.removeFromStorageBuffer(pedestal,intSpace,true);
            if(itemsToRemove==0)
            {
                this.removeFromStorageBuffer(pedestal,intSpace,false);
                pedestal.addItemOverride(stackSpawnedItem);
            }
            else
            {
                stackSpawnedItem.setCount(itemsToRemove);
                this.removeFromStorageBuffer(pedestal,itemsToRemove,false);
                pedestal.addItemOverride(stackSpawnedItem);
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