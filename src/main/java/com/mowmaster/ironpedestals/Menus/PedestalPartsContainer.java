package com.mowmaster.ironpedestals.Menus;

import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// The pedestal's upgrade, tool, work card, filter and augments seen as a 7-slot Container (one per PedestalPart),
// which is what vanilla Slots work on. Nothing is stored here: every read and write goes straight to the block
// entity, so the menu and the block (and its offhand clicks) always agree. Only the server changes anything: on the
// client (the menu's prediction of a click) taking hands back a copy and putting does nothing, and the screen catches
// up from the block entity's own update a tick later.
public class PedestalPartsContainer implements Container
{
    private static final PedestalPart[] PARTS = PedestalPart.values();

    private final BasePedestalBlockEntity pedestal;
    // Who put an upgrade in (its owner, for the fake player); null on the client
    private final @Nullable Player player;

    public PedestalPartsContainer(BasePedestalBlockEntity pedestal, @Nullable Player player)
    {
        this.pedestal = pedestal;
        this.player = player;
    }

    @Override
    public int getContainerSize()
    {
        return PARTS.length;
    }

    @Override
    public boolean isEmpty()
    {
        for (PedestalPart part : PARTS)
        {
            if (!part.get(pedestal).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot)
    {
        return PARTS[slot].get(pedestal);
    }

    // Each part holds one item, so any amount takes it out
    @Override
    public ItemStack removeItem(int slot, int amount)
    {
        return amount <= 0 ? ItemStack.EMPTY : removeItemNoUpdate(slot);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {
        if (isClientSide()) return PARTS[slot].get(pedestal).copy();
        return PARTS[slot].take(pedestal);
    }

    // Replaces the part: the old one is taken out (the menu has already moved it to the cursor or inventory), the
    // new one goes in through the block entity's insert method
    @Override
    public void setItem(int slot, ItemStack stack)
    {
        if (isClientSide()) return;
        PedestalPart part = PARTS[slot];
        if (!part.get(pedestal).isEmpty()) part.take(pedestal);
        part.put(pedestal, stack, player);
    }

    private boolean isClientSide()
    {
        return pedestal.getLevel() == null || pedestal.getLevel().isClientSide();
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }

    @Override
    public void setChanged()
    {
        pedestal.setChanged();
    }

    @Override
    public boolean stillValid(Player player)
    {
        return !pedestal.isRemoved();
    }

    @Override
    public void clearContent()
    {
    }
}
