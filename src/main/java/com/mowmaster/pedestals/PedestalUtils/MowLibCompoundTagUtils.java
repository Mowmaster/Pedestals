package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MowLibCompoundTagUtils
{
    public static CompoundTag writeItemStackListToNBT(String ModID, @Nullable CompoundTag inputNBT, List<ItemStack> listIn)
    {
        CompoundTag compound = (inputNBT != null)?(inputNBT):(new CompoundTag());
        if(listIn.size()<=0)return compound;

        CompoundTag compoundStorage = new CompoundTag();
                ItemStackHandler handler = new ItemStackHandler();
        handler.setSize(listIn.size());

        for(int i=0;i<handler.getSlots();i++) {handler.setStackInSlot(i,listIn.get(i));}

        compoundStorage = handler.serializeNBT();
        compound.put(ModID + "_stacksStored",compoundStorage);
        return compound;
    }

    public static List<ItemStack> readItemStackListFromNBT(String ModID, CompoundTag inputNBT)
    {
        List<ItemStack> stackList = new ArrayList<>();
        if(inputNBT.contains(ModID + "_stacksStored"))
        {
            CompoundTag invTag = inputNBT.getCompound(ModID + "_stacksStored");
            ItemStackHandler handler = new ItemStackHandler();
            ((INBTSerializable<CompoundTag>) handler).deserializeNBT(invTag);

            for(int i=0;i<handler.getSlots();i++) {stackList.add(handler.getStackInSlot(i));}
        }

        return stackList;
    }

    public static CompoundTag writeFluidStackToNBT(String ModID, @Nullable CompoundTag inputNBT, FluidStack fluidStack)
    {
        CompoundTag compound = (inputNBT != null)?(inputNBT):(new CompoundTag());
        compound.putInt(ModID + intName,input);
        return compound;
    }

    public static FluidStack readFluidStackFromNBT(String ModID, CompoundTag inputNBT)
    {
        if(inputNBT.contains(ModID + intName))
        {
            return inputNBT.getInt(ModID + intName);
        }

        //new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName)),fluid)
        return null;
    }

    public static CompoundTag writeIntegerToNBT(String ModID, @Nullable CompoundTag inputNBT, int input, String intName)
    {
        CompoundTag compound = (inputNBT != null)?(inputNBT):(new CompoundTag());
        compound.putInt(ModID + intName,input);
        return compound;
    }

    public static Integer readIntegerFromNBT(String ModID, CompoundTag inputNBT, String intName)
    {
        if(inputNBT.contains(ModID + intName))
        {
            return inputNBT.getInt(ModID + intName);
        }

        return null;
    }
}
