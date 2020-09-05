package com.mowmaster.pedestals.creativeTab;

import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.references.Reference;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CreativeTabBlock extends ItemGroup
{
    public CreativeTabBlock() {
        super(Reference.MODID+"_blocks");
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(PedestalBlock.I_PEDESTAL_203);
    }
}
