package com.mowmaster.pedestals.creativetab;

import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.references.ReferenceMain;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class PedestalsTab extends CreativeModeTab {
    public PedestalsTab() {
        super(ReferenceMain.MODID+"_tab");
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(PedestalBlock.I_PEDESTAL_333);
    }
}
