package com.mowmaster.pedestals.PedestalTab;

import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class PedestalsTab extends CreativeModeTab
{
    public PedestalsTab() {
        super("tab_pedestaltab");
    }

    public static final PedestalsTab TAB_ITEMS = new PedestalsTab() {};

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(DeferredRegisterItems.COLOR_APPLICATOR.get());
    }
}
