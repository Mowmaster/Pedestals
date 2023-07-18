package com.mowmaster.pedestals.pedestaltab;

import com.mowmaster.pedestals.registry.DeferredRegisterTileBlocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PedestalsTab extends CreativeModeTab
{
    public PedestalsTab() {
        super("tab_pedestaltab");
    }

    public static final PedestalsTab TAB_ITEMS = new PedestalsTab() {};

    @Override
    public @NotNull ItemStack makeIcon() {
        return new ItemStack(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get());
    }
}
