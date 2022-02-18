package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ContainerUtils {

    public static Container getContainer(int size)
    {
        return new Container() {
        List<ItemStack> stack = new ArrayList<>();

        @Override
        public int getContainerSize() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }

        @Override
        public ItemStack getItem(int p_18941_) {
            return stack.get(p_18941_);
        }

        @Override
        public ItemStack removeItem(int p_18942_, int p_18943_) {
            ItemStack oldStack = stack.get(p_18942_).copy();
            stack.remove(p_18942_);
            return oldStack;
        }

        @Override
        public ItemStack removeItemNoUpdate(int p_18951_) {
            ItemStack oldStack = stack.get(p_18951_).copy();
            stack.remove(p_18951_);
            return oldStack;
        }

        @Override
        public void setItem(int p_18944_, ItemStack p_18945_) {
            if(p_18944_ == -1)stack.add(p_18945_);
            else stack.set(p_18944_,p_18945_);
        }

        @Override
        public void setChanged() {

        }

        @Override
        public boolean stillValid(Player p_18946_) {
            return stack.isEmpty();
        }

        @Override
        public void clearContent() {
            stack = new ArrayList<>();
        }
        };
    }

    public static AbstractContainerMenu getAbstractContainerMenu(int id)
    {
        AbstractContainerMenu abstractContainerMenu = new AbstractContainerMenu(null,id) {
            @Override
            public boolean stillValid(Player p_38874_) {
                return true;
            }
        };

        return abstractContainerMenu;
    }



    public static CraftingContainer getContainerCrafting(int sizeX, int sizeY)
    {
        return new CraftingContainer(getAbstractContainerMenu(40),sizeX,sizeY) {
            List<ItemStack> stack = new ArrayList<>();

            @Override
            public int getContainerSize() {
                return sizeX*sizeY;
            }

            @Override
            public boolean isEmpty() {
                return stack.isEmpty();
            }

            @Override
            public ItemStack getItem(int p_18941_) {
                return stack.get(p_18941_);
            }

            @Override
            public ItemStack removeItem(int p_18942_, int p_18943_) {
                ItemStack oldStack = stack.get(p_18942_).copy();
                stack.remove(p_18942_);
                return oldStack;
            }

            @Override
            public ItemStack removeItemNoUpdate(int p_18951_) {
                ItemStack oldStack = stack.get(p_18951_).copy();
                stack.remove(p_18951_);
                return oldStack;
            }

            @Override
            public void setItem(int p_18944_, ItemStack p_18945_) {
                if(p_18944_ == -1)stack.add(p_18945_);
                else stack.set(p_18944_,p_18945_);
            }

            @Override
            public void setChanged() {

            }

            @Override
            public boolean stillValid(Player p_18946_) {
                return stack.isEmpty();
            }

            @Override
            public void clearContent() {
                stack = new ArrayList<>();
            }
        };
    }
}
