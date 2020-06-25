package com.mowmaster.pedestals.crafting;

import net.minecraft.item.ItemStack;

public class CrusherMap
{

    public ItemStack stackIn;
    public ItemStack outputStack;

    public CrusherMap(ItemStack getInput, ItemStack outputDustStack)
    {
        this.stackIn=getInput;
        this.outputStack=outputDustStack;
    }

    public ItemStack getInputColor()
    {
        return stackIn;
    }

    public ItemStack getOutput()
    {
        return outputStack;
    }

    @Override
    public String toString() {
        return "DustCrusher [input=" + stackIn + ", output=" + outputStack + "]";
    }
}
