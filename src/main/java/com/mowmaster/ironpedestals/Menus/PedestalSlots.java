package com.mowmaster.ironpedestals.Menus;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import java.util.Optional;
import java.util.function.BooleanSupplier;

// The pedestal menu's two kinds of slot
public final class PedestalSlots
{
    private PedestalSlots() {}

    // One of the pedestal's item stacks. NeoForge's ResourceHandlerSlot shows a transfer-API storage in a menu; it
    // reads and writes the storage directly (IndexModifier: how to overwrite one index, here the storage's own set).
    // A pedestal slot can hold more than a normal stack (tiers), so taking out is capped at one normal stack: the
    // cursor never carries more than an item can stack to. active: whether this slot is on the page being shown.
    public static class Storage extends ResourceHandlerSlot
    {
        private final BooleanSupplier active;

        public Storage(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> modifier, int index, int x, int y, BooleanSupplier active)
        {
            super(handler, modifier, index, x, y);
            this.active = active;
        }

        @Override
        public Optional<ItemStack> tryRemove(int count, int decrement, Player player)
        {
            int max = getItem().getMaxStackSize();
            return super.tryRemove(Math.min(count, max), Math.min(decrement, max), player);
        }

        @Override
        public boolean isActive()
        {
            return active.getAsBoolean();
        }
    }

    // An upgrade / tool / work card / filter / augment slot: one item, only what the part takes (PedestalPart)
    public static class Part extends Slot
    {
        private final PedestalPart part;
        private final BooleanSupplier active;

        public Part(Container container, PedestalPart part, int x, int y, BooleanSupplier active)
        {
            super(container, part.ordinal(), x, y);
            this.part = part;
            this.active = active;
        }

        public PedestalPart getPart()
        {
            return part;
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return part.mayPlace(stack);
        }

        @Override
        public int getMaxStackSize()
        {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack)
        {
            return 1;
        }

        @Override
        public boolean isActive()
        {
            return active.getAsBoolean();
        }
    }
}
