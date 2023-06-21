package com.mowmaster.pedestals.PedestalUtils;

import com.mowmaster.mowlib.api.TransportAndStorage.IFilterItem;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.IPedestalUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class PedestalUtilities
{
    /*
    public static int getRedstoneLevelPedestal(Level worldIn, BlockPos pos) {
        int hasItem = 0;
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (blockEntity instanceof BasePedestalBlockEntity pedestal) {
            List<ItemStack> itemstacks = pedestal.getItemStacks();
            if (pedestal.getCoinOnPedestal().getItem() instanceof IPedestalUpgrade upgrade) {
                int value = upgrade.getComparatorRedstoneLevel(worldIn, pos);
                if(value !=-1)
                {
                    return value;
                }
            }
            if (itemstacks.size() > 0) {
                int maxStackSizeDefault = 64;
                if (pedestal.hasFilter()) {
                    IPedestalFilter filter = pedestal.getIPedestalFilter();
                    if (filter != null && filter.getFilterDirection().insert()) {
                        maxStackSizeDefault = Math.max(1, filter.canAcceptCountItems(pedestal, pedestal.getFilterInPedestal(), new ItemStack(Items.STONE, 64).getMaxStackSize(), pedestal.getSlotSizeLimit(), new ItemStack(Items.STONE, 64)));
                    }
                }
                int counter = 0;
                int maxStorageCount = Math.max(1, (pedestal.getPedestalSlots() - 1)) * maxStackSizeDefault;
                for (ItemStack stack : itemstacks) {
                    //adjust max storage possible based on itemstacks present
                    if (stack.getMaxStackSize() < maxStackSizeDefault) {
                        maxStorageCount -= maxStackSizeDefault;
                        maxStorageCount += stack.getMaxStackSize();
                    }

                    counter += stack.getCount();
                }
                float f = (float) counter / (float) maxStorageCount;
                hasItem = (int) Math.floor(f * 15.0F);
            }
            else
            {
                float f = (float) pedestal.getItemInPedestal().getCount() / (float) pedestal.getItemInPedestal().getMaxStackSize();
                return (int) Math.floor(f * 15.0F);
            }
        }

        return hasItem;
    }
    */

    public static int getRedstoneLevelPedestal(Level worldIn, BlockPos pos)
    {
        int hasItem=0;
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if(blockEntity instanceof BasePedestalBlockEntity pedestal) {
            List<ItemStack> itemstacks = pedestal.getItemStacks();
            if(pedestal.getCoinOnPedestal().getItem() instanceof IPedestalUpgrade upgrade)
            {
                int value = upgrade.getComparatorRedstoneLevel(worldIn, pos);
                if(value !=-1)
                {
                    return value;
                }
            }
            if(itemstacks.size()>0)
            {
                int maxStackSizeDefault = 64;
                if(pedestal.hasFilter())
                {
                    IFilterItem filter = pedestal.getIFilterItem();
                    if(filter != null && filter.getFilterDirection().insert())
                    {
                        maxStackSizeDefault = Math.max(1,filter.canAcceptCountItems(pedestal,pedestal.getFilterInBlockEntity(), new ItemStack(Items.STONE,64).getMaxStackSize(), pedestal.getSlotSizeLimit(), new ItemStack(Items.STONE,64)));
                    }
                }
                int counter = 0;
                int maxStorageCount = Math.max(1,(pedestal.getPedestalSlots()-1)) * maxStackSizeDefault;
                for (ItemStack stack : itemstacks)
                {
                    //adjust max storage possible based on itemstacks present
                    if(stack.getMaxStackSize()<maxStackSizeDefault)
                    {
                        maxStorageCount-=maxStackSizeDefault;
                        maxStorageCount+=stack.getMaxStackSize();
                    }

                    counter+=stack.getCount();
                }
                float f = (float)counter/(float)maxStorageCount;
                hasItem = (int)Math.floor(f*15.0F);
            }
            else
            {
                float f = (float) pedestal.getItemInPedestal().getCount() / (float) pedestal.getItemInPedestal().getMaxStackSize();
                return (int) Math.floor(f * 15.0F);
            }
        }

        return hasItem;
    }
}
