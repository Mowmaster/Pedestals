package com.mowmaster.ironpedestals.Transport;

import com.mojang.serialization.Codec;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.mowlib.storage.CBBulkItemHandler;
import com.mowmaster.mowlib.transport.ItemTransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// The pedestal's item storage: MowLib's item storage (stack count from getCapacity, filter, resize, drops...), with
// slots that hold the pedestal tier's stack capacity instead of a normal stack (BasePedestalBlockEntity#getItemSlotLimit).
// MowLib's storage caps a slot at the item's normal stack and saves through ItemStack, which only saves up to 99, so
// this overrides both. (MowLib TODO in CLAUDE.md: a slot-limit hook + bulk saving in MowLib would replace this class.)
public class PedestalItemStorage extends ItemTransportType.Storage
{
    // How slots bigger than an ItemStack can save are written: {resource, amount} per slot (MowLib's bulk handler format)
    private static final String BULK_KEY = "bulk_stacks";
    private static final Codec<List<ResourceStack<ItemResource>>> BULK_CODEC = CBBulkItemHandler.STACK_CODEC.listOf();

    private final BasePedestalBlockEntity pedestal;

    public PedestalItemStorage(BasePedestalBlockEntity pedestal, ItemTransportType type)
    {
        super(pedestal, type);
        this.pedestal = pedestal;
    }

    // A slot holds the tier's stack capacity for this item, then the filter's limit (MowLib's version would cap it
    // at a normal stack first). An empty slot reports 99, like NeoForge's item stack storage.
    @Override
    protected int getCapacity(int index, ItemResource resource)
    {
        if (resource.isEmpty()) return Item.ABSOLUTE_MAX_STACK_SIZE;
        int capacity = pedestal.getItemSlotLimit(resource);
        return Math.min(capacity, pedestal.filterLimit(TransportTypes.ITEMS.get(), resource, capacity));
    }

    // ---- Saving ----
    // Normally saved as item stacks under "stacks" (NeoForge's format, so pedestals saved before the tiers load as
    // they are). When a slot holds more than an ItemStack can be saved with, every slot is saved under "bulk_stacks"
    // instead. Loading reads either.

    @Override
    public void serialize(ValueOutput output)
    {
        if (!hasOversizedStack())
        {
            super.serialize(output);
            return;
        }
        List<ResourceStack<ItemResource>> entries = new ArrayList<>(size());
        for (int index = 0; index < size(); index++)
        {
            ItemResource resource = getResource(index);
            entries.add(resource.isEmpty() ? new ResourceStack<>(ItemResource.EMPTY, 0) : new ResourceStack<>(resource, getAmountAsInt(index)));
        }
        output.store(BULK_KEY, BULK_CODEC, entries);
    }

    @Override
    public void deserialize(ValueInput input)
    {
        // Optional: a value that may be missing (here: no "bulk_stacks" key -> the normal format)
        Optional<List<ResourceStack<ItemResource>>> bulk = input.read(BULK_KEY, BULK_CODEC);
        if (bulk.isEmpty())
        {
            super.deserialize(input);
            return;
        }
        List<ResourceStack<ItemResource>> entries = bulk.get();
        NonNullList<ItemStack> loaded = NonNullList.withSize(Math.max(1, entries.size()), ItemStack.EMPTY);
        for (int index = 0; index < entries.size(); index++)
        {
            ResourceStack<ItemResource> entry = entries.get(index);
            // In memory a slot is still an ItemStack; its count may be bigger than 99, it just can't be saved as one
            if (!entry.resource().isEmpty() && entry.amount() > 0) loaded.set(index, entry.resource().toStack(entry.amount()));
        }
        setStacks(loaded); // NeoForge's (protected) way to replace the slots, size included
    }

    // Does any slot hold more than an ItemStack can be saved with (99, and no more than its max stack size)?
    private boolean hasOversizedStack()
    {
        for (ItemStack stack : stacks)
        {
            if (stack.getCount() > Math.min(Item.ABSOLUTE_MAX_STACK_SIZE, stack.getMaxStackSize())) return true;
        }
        return false;
    }
}
