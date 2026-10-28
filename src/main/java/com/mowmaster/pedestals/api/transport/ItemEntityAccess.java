package com.mowmaster.pedestals.api.transport;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;

// Lets an item lying in the world be used like an item in an inventory, so its fluid/energy capabilities can be
// emptied (magnet upgrade). The stack is copied into a small 2-slot handler: slot 0 is the item itself, slot 1
// catches anything that can't go back into slot 0 (one empty bucket out of a stack of full ones...).
// After a committed transaction, writeBack puts the results back into the world.
public class ItemEntityAccess
{
    private final ItemEntity entity;
    private final ItemStacksResourceHandler handler;

    public ItemEntityAccess(ItemEntity entity)
    {
        this.entity = entity;
        this.handler = new ItemStacksResourceHandler(NonNullList.of(ItemStack.EMPTY, entity.getItem().copy(), ItemStack.EMPTY));
    }

    // Pass this to an item capability (ItemAccess.getCapability or a type's getItemHandler)
    public ItemAccess access()
    {
        return ItemAccess.forHandlerIndex(handler, 0);
    }

    // The entity gets slot 0; anything in slot 1 is dropped next to it
    public void writeBack(ServerLevel level)
    {
        ItemStack main = ItemUtil.getStack(handler, 0);
        if (main.isEmpty()) entity.discard();
        else entity.setItem(main);

        ItemStack extra = ItemUtil.getStack(handler, 1);
        if (!extra.isEmpty())
        {
            ItemEntity dropped = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), extra);
            dropped.setDeltaMovement(0, 0, 0);
            level.addFreshEntity(dropped);
        }
    }
}
