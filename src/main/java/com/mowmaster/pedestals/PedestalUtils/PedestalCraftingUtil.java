package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class PedestalCraftingUtil
{
    public static AbstractContainerMenu getAbstractContainerMenu(int id) {
        AbstractContainerMenu abstractContainerMenu = new AbstractContainerMenu((MenuType)null, id) {
            public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
                return null;
            }

            public boolean stillValid(Player p_38874_) {
                return true;
            }
        };
        return abstractContainerMenu;
    }
}
