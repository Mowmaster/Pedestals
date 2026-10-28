package com.mowmaster.pedestals.StorageHandler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Predicate;

// Item storage where every slot holds one kind of item (same item, same data) in any amount up to `capacity`,
// up to Integer.MAX_VALUE, ignoring the item's normal stack size. Built on NeoForge's transfer API, so hoppers
// and pipes can use it directly as the block's item capability.
// A slot takes whatever is first put in it; once it's emptied again it accepts anything the filter allows.
// Reusable: pass a different size, capacity and filter for other bulk storage blocks.
public class libBulkItemHandler extends ResourceStacksResourceHandler<ItemResource>
{
    // How one slot is saved: the item (with its data) and the amount. Empty slots save as {}.
    public static final Codec<ResourceStack<ItemResource>> STACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ItemResource.OPTIONAL_CODEC.optionalFieldOf("resource", ItemResource.EMPTY).forGetter(ResourceStack::resource),
                    Codec.INT.optionalFieldOf("amount", 0).forGetter(ResourceStack::amount))
            .apply(instance, ResourceStack::new));

    private final int capacity;
    private final Predicate<ItemResource> filter;
    private final Runnable onChanged;

    // size = number of slots; capacity = items per slot; filter = what may go in; onChanged = called after every change
    public libBulkItemHandler(int size, int capacity, Predicate<ItemResource> filter, Runnable onChanged)
    {
        super(size, ItemResource.EMPTY, STACK_CODEC);
        this.capacity = capacity;
        this.filter = filter;
        this.onChanged = onChanged;
    }

    @Override
    public boolean isValid(int index, ItemResource resource)
    {
        return filter.test(resource);
    }

    @Override
    protected int getCapacity(int index, ItemResource resource)
    {
        return capacity;
    }

    public int getCapacity()
    {
        return capacity;
    }

    @Override
    protected void onContentsChanged(int index, ResourceStack<ItemResource> previousContents)
    {
        onChanged.run();
    }

    // ---- Helpers for players (each runs as its own transaction) ----

    // Puts as much of the stack in as fits (does not change the stack). Returns how many went in.
    public int insertStack(ItemStack stack)
    {
        if (stack.isEmpty()) return 0;
        ItemResource resource = ItemResource.of(stack);
        // try-with-resources: the transaction is closed (and rolled back if not committed) at the end of the block
        try (Transaction transaction = Transaction.openRoot())
        {
            int inserted = 0;
            for (int index = 0; index < size() && inserted < stack.getCount(); index++)
            {
                inserted += insert(index, resource, stack.getCount() - inserted, transaction);
            }
            transaction.commit();
            return inserted;
        }
    }

    // Takes up to maxAmount (and at most one normal stack) out of the first slot that has something
    public ItemStack extractStack(int maxAmount)
    {
        for (int index = 0; index < size(); index++)
        {
            ItemResource resource = getResource(index);
            if (resource.isEmpty()) continue;
            int amount = Math.min(maxAmount, resource.getMaxStackSize());
            try (Transaction transaction = Transaction.openRoot())
            {
                int extracted = extract(index, resource, amount, transaction);
                transaction.commit();
                return resource.toStack(extracted);
            }
        }
        return ItemStack.EMPTY;
    }

    // Everything stored, across all slots
    public long totalAmount()
    {
        long total = 0;
        for (int index = 0; index < size(); index++) total += getAmountAsLong(index);
        return total;
    }

    public boolean isEmptyStorage()
    {
        return totalAmount() == 0;
    }

    // How full it is, 0 to 1
    public float fullness()
    {
        return (float) totalAmount() / ((long) capacity * size());
    }
}
