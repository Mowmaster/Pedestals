package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class UpgradeUtils
{
    public static ItemStack setCapacityOnItem(ItemStack stackIn, int capacityLevel)
    {
        CompoundTag capacityOnItem = stackIn.getOrCreateTag();
        capacityOnItem.putInt(MODID+"_upgradeCapacity",capacityLevel);
        stackIn.setTag(capacityOnItem);
        return stackIn;
    }

    public static int getCapacityOnItem(ItemStack stackIn)
    {
        CompoundTag capacityOnItem = stackIn.getOrCreateTag();
        if(capacityOnItem.contains(MODID+"_upgradeCapacity"))
        {
            int capacity = capacityOnItem.getInt(MODID+"_upgradeCapacity");
            return capacity;
        }

        return 0;
    }
}
