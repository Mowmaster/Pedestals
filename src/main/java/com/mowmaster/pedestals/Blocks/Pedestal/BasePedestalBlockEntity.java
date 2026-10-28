package com.mowmaster.pedestals.Blocks.Pedestal;

import com.mowmaster.pedestals.Blocks.libBlockEntityRegistry;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.TintHandler.ITintHolder;
import com.mowmaster.pedestals.TintHandler.libTintData;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.filter.FilterDirection;
import com.mowmaster.pedestals.api.filter.IPedestalFilter;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorageHost;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// The pedestal's storage, links, filter and colour.
// Storage: one PedestalStorage per registered transport type (items, fluids, energy, experience, and any type
// another mod registers), each exposed to pipes through its capability (see the types' registerCapability).
// Links: every TICKS_TO_TRANSFER ticks it sends to the first linked pedestal that accepts anything, every type at once.
// Not ported yet: upgrades, augments (speed, capacity, storage, range, round robin), work cards, redstone counts.
public class BasePedestalBlockEntity extends BlockEntity implements ITintHolder, PedestalStorageHost
{
    // LinkedHashMap keeps the registry order, so types are always tried in the same order
    private final Map<PedestalTransportType<?>, PedestalStorage<?>> storages = new LinkedHashMap<>();
    private final libTintData tints = new libTintData(this, 1);
    // Pedestals this one sends to
    private final List<BlockPos> links = new ArrayList<>();
    private ItemStack filter = ItemStack.EMPTY;
    private int transferTicks;

    public BasePedestalBlockEntity(BlockPos pos, BlockState state)
    {
        super(libBlockEntityRegistry.PEDESTAL.get(), pos, state);
        for (PedestalTransportType<?> type : PedestalTransportTypes.REGISTRY)
        {
            storages.put(type, type.createStorage(this));
        }
    }

    // Saves, and sends the new contents to nearby clients
    public void update()
    {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    // ---- Storage ----

    // The storage for a type. The cast is safe: the map only ever pairs a type with the storage it made.
    @SuppressWarnings("unchecked")
    public <R extends Resource> PedestalStorage<R> getStorage(PedestalTransportType<R> type)
    {
        return (PedestalStorage<R>) storages.get(type);
    }

    public PedestalStorage<ItemResource> getItemStorage()
    {
        return getStorage(PedestalTransportTypes.ITEMS.get());
    }

    @Override
    public void onStorageChanged(PedestalTransportType<?> type)
    {
        if (type.syncsToClient()) update();
        else setChanged();
    }

    // ---- Items (used by the block's clicks and the renderer) ----

    public boolean hasItem()
    {
        return !getItemStorage().isEmpty();
    }

    // The stack shown on top: the first slot that has something
    public ItemStack getItemInPedestal()
    {
        PedestalStorage<ItemResource> items = getItemStorage();
        for (int index = 0; index < items.size(); index++)
        {
            ItemStack stack = ItemUtil.getStack(items, index);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    // Puts as much of the stack in as fits (and as the filter allows); returns how many went in
    public int insertItem(ItemStack stack, boolean simulate)
    {
        if (stack.isEmpty()) return 0;
        // try-with-resources: the transaction is closed at the end of the block, and anything not committed is undone
        try (Transaction transaction = Transaction.openRoot())
        {
            int inserted = ResourceHandlerUtil.insertStacking(getItemStorage(), ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate) transaction.commit();
            return inserted;
        }
    }

    // Takes up to maxAmount (and at most one normal stack) from the last slot that has something, like 1.20
    public ItemStack removeItem(int maxAmount)
    {
        PedestalStorage<ItemResource> items = getItemStorage();
        for (int index = items.size() - 1; index >= 0; index--)
        {
            ItemResource resource = items.getResource(index);
            if (resource.isEmpty()) continue;
            int amount = Math.min(maxAmount, resource.getMaxStackSize());
            try (Transaction transaction = Transaction.openRoot())
            {
                int extracted = items.extract(index, resource, amount, transaction);
                transaction.commit();
                return resource.toStack(extracted);
            }
        }
        return ItemStack.EMPTY;
    }

    public @Nullable Component describeItems()
    {
        return getItemStorage().describe();
    }

    // Everything but items, one line per type (fluid, energy, experience...)
    public List<Component> describeOtherContents()
    {
        List<Component> lines = new ArrayList<>();
        storages.forEach((type, storage) -> {
            if (type == PedestalTransportTypes.ITEMS.get()) return; // "return" in a lambda just skips this entry
            Component line = storage.describe();
            if (line != null) lines.add(line);
        });
        return lines;
    }

    // 0-15 by how full the item slots are, same maths as a chest
    public int getRedstoneSignal()
    {
        return ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(getItemStorage());
    }

    // Called by vanilla just before the block is removed (broken, exploded, replaced)
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state)
    {
        super.preRemoveSideEffects(pos, state);
        if (level == null) return;
        storages.values().forEach(storage -> storage.onPedestalRemoved(level, pos));
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), filter);
    }

    // ---- Filter ----

    public boolean hasFilter()
    {
        return !filter.isEmpty();
    }

    public ItemStack getFilter()
    {
        return filter;
    }

    // Takes one filter from the stack; false if there's already one or it isn't a filter
    public boolean insertFilter(ItemStack stack)
    {
        if (hasFilter() || !(stack.getItem() instanceof IPedestalFilter)) return false;
        filter = stack.split(1);
        filterChanged();
        return true;
    }

    public ItemStack removeFilter()
    {
        ItemStack removed = filter;
        filter = ItemStack.EMPTY;
        filterChanged();
        return removed;
    }

    // Shows whitelist/blacklist on the block and resyncs
    private void filterChanged()
    {
        if (level != null)
        {
            int status = filter.getItem() instanceof IPedestalFilter pedestalFilter ? (pedestalFilter.isBlacklist(filter) ? 2 : 1) : 0;
            BlockState state = getBlockState();
            if (state.getValue(BasePedestalBlock.FILTER_STATUS) != status)
            {
                level.setBlock(worldPosition, state.setValue(BasePedestalBlock.FILTER_STATUS, status), Block.UPDATE_ALL);
            }
        }
        update();
    }

    @Override
    public <R extends Resource> boolean filterAllows(PedestalTransportType<R> type, R resource, FilterDirection direction)
    {
        if (!(filter.getItem() instanceof IPedestalFilter pedestalFilter) || pedestalFilter.getDirection(filter) != direction) return true;
        return pedestalFilter.allows(filter, type, resource);
    }

    @Override
    public <R extends Resource> int filterLimit(PedestalTransportType<R> type, R resource, int capacity)
    {
        if (!(filter.getItem() instanceof IPedestalFilter pedestalFilter) || pedestalFilter.getDirection(filter) != FilterDirection.INSERT) return capacity;
        return pedestalFilter.limit(filter, type, resource, capacity);
    }

    // ---- Links ----

    public List<BlockPos> getLinks()
    {
        return List.copyOf(links);
    }

    public int getLinkingRange()
    {
        return Config.LINKING_RANGE.getAsInt();
    }

    // Within the range on every axis (a cube around the pedestal, as in 1.20)
    public boolean isInRange(BlockPos target)
    {
        int range = getLinkingRange();
        return Math.abs(target.getX() - worldPosition.getX()) <= range
                && Math.abs(target.getY() - worldPosition.getY()) <= range
                && Math.abs(target.getZ() - worldPosition.getZ()) <= range;
    }

    // What toggleLink did, with the message the linking tools show. An enum can hold fields and methods like a class.
    public enum LinkResult
    {
        ADDED(true, null),
        REMOVED(true, ".tool_link_removed"),
        OUT_OF_RANGE(false, ".tool_link_distance"),
        SELF(false, ".tool_link_itsself"),
        FULL(false, ".tool_link_unsucess");

        private final boolean changed;
        private final @Nullable String messageKey;

        LinkResult(boolean changed, @Nullable String messageKey)
        {
            this.changed = changed;
            this.messageKey = messageKey;
        }

        public boolean changed()
        {
            return changed;
        }

        public Component message(boolean backwards)
        {
            String key = messageKey != null ? messageKey
                    : backwards ? ".tool_link_success_backwardslinkingtool" : ".tool_link_success_linkingtool";
            return Component.translatable(PedestalReferences.MODID + key).withStyle(changed ? ChatFormatting.WHITE : ChatFormatting.RED);
        }
    }

    // Adds a link to target, or removes it if it's already linked
    public LinkResult toggleLink(BlockPos target)
    {
        if (target.equals(worldPosition)) return LinkResult.SELF;
        if (links.remove(target))
        {
            update();
            return LinkResult.REMOVED;
        }
        if (!isInRange(target)) return LinkResult.OUT_OF_RANGE;
        if (links.size() >= Config.MAX_LINKS.getAsInt()) return LinkResult.FULL;
        links.add(target.immutable());
        update();
        return LinkResult.ADDED;
    }

    // ---- Transfer (server only, called every tick by the block's ticker) ----

    public void serverTick()
    {
        if (level == null || links.isEmpty()) return;
        if (++transferTicks < Config.TICKS_TO_TRANSFER.getAsInt()) return;
        transferTicks = 0;
        // A redstone signal turns the pedestal off
        if (level.hasNeighborSignal(worldPosition)) return;

        Iterator<BlockPos> iterator = links.iterator();
        while (iterator.hasNext())
        {
            BlockPos target = iterator.next();
            if (!level.isLoaded(target)) continue;
            if (!(level.getBlockEntity(target) instanceof BasePedestalBlockEntity receiver))
            {
                // The pedestal there is gone
                iterator.remove();
                setChanged();
                continue;
            }
            if (!isInRange(target) || level.hasNeighborSignal(target)) continue;
            if (sendTo(receiver)) return; // one receiver per transfer, like 1.20 (round robin comes with augments)
        }
    }

    private boolean sendTo(BasePedestalBlockEntity receiver)
    {
        boolean sent = false;
        for (PedestalTransportType<?> type : storages.keySet())
        {
            // "sent |= x" is "sent = sent | x": every type gets its turn even after one has sent something
            sent |= send(type, receiver);
        }
        return sent;
    }

    // A generic helper so the storages' types line up: Java can't move between two PedestalStorage<?> directly,
    // but inside here both are PedestalStorage<R> for the same R
    private <R extends Resource> boolean send(PedestalTransportType<R> type, BasePedestalBlockEntity receiver)
    {
        PedestalStorage<R> from = getStorage(type);
        PedestalStorage<R> to = receiver.getStorage(type);
        if (from == null || to == null || from.isEmpty()) return false;
        try (Transaction transaction = Transaction.openRoot())
        {
            int moved = type.transfer(from, to, type.getTransferRate(this), transaction);
            transaction.commit();
            return moved > 0;
        }
    }

    // ---- Colour (ITintHolder) ----

    @Override public int getColor(int region) { return tints.getColor(region); }
    @Override public void setColor(int region, int color) { tints.setColor(region, color); }
    @Override public int getTint(int tintIndex) { return tints.getTint(tintIndex); }

    // ---- Saving ----
    // Each type saves under storage/<type id>, e.g. storage/pedestals:fluids, so add-on types can't clash

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);
        ValueOutput storageOutput = output.child("storage");
        storages.forEach((type, storage) -> storage.serialize(storageOutput.child(type.getId().toString())));
        if (!links.isEmpty()) output.store("links", BlockPos.CODEC.listOf(), links);
        if (!filter.isEmpty()) output.store("filter", ItemStack.CODEC, filter);
        tints.save(output);
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);
        ValueInput storageInput = input.childOrEmpty("storage");
        storages.forEach((type, storage) -> storage.deserialize(storageInput.childOrEmpty(type.getId().toString())));
        links.clear();
        input.read("links", BlockPos.CODEC.listOf()).ifPresent(links::addAll);
        filter = input.read("filter", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        tints.load(input);
    }

    // ---- Syncing to clients ----

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return saveCustomOnly(registries); // runs saveAdditional into a tag
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this); // uses getUpdateTag
    }

    // ---- Item <-> block (data components) ----

    // Placed from an item with a dyed_color: take that colour
    @Override
    protected void applyImplicitComponents(DataComponentGetter components)
    {
        super.applyImplicitComponents(components);
        DyedItemColor dyed = components.get(DataComponents.DYED_COLOR);
        if (dyed != null) tints.setColor(0, dyed.rgb());
    }

    // Block turned into an item (the loot table's copy_components, creative pick-block): keep the colour
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components)
    {
        super.collectImplicitComponents(components);
        if (isDyed(0)) components.set(DataComponents.DYED_COLOR, new DyedItemColor(getColor(0)));
    }
}
