package com.mowmaster.pedestals.Blocks.Pedestal;

import com.mowmaster.pedestals.Blocks.libBlockEntityRegistry;
import com.mowmaster.pedestals.TintHandler.ITintHolder;
import com.mowmaster.pedestals.TintHandler.libTintData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.ChatFormatting;
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
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

// The pedestal's storage and colour. This first port only has the item storage; fluids, energy, xp, dust,
// upgrades, augments and linking come back in later steps.
// Uses a libTintData (like CrystalBlocks' barrel and chest) rather than extending libTintBlockEntity, so later the
// pedestal can extend a shared filterable/storage base class without losing its colour.
public class BasePedestalBlockEntity extends BlockEntity implements ITintHolder
{
    // 1.20's default for pedestal_baseItemStacks. Storage augments will raise this once they're ported.
    public static final int BASE_ITEM_SLOTS = 1;

    // NeoForge's transfer API (replaces Forge's ItemStackHandler). This is an anonymous subclass:
    // "new X(...) { ... }" makes a one-off class that extends X, here just to hook onContentsChanged.
    private final ItemStacksResourceHandler items = new ItemStacksResourceHandler(BASE_ITEM_SLOTS)
    {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents)
        {
            update();
        }
    };
    private final libTintData tints = new libTintData(this, 1);

    public BasePedestalBlockEntity(BlockPos pos, BlockState state)
    {
        super(libBlockEntityRegistry.PEDESTAL.get(), pos, state);
    }

    // Saves, and sends the new contents to nearby clients (so the item on top updates)
    public void update()
    {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    // ---- Items ----

    // What hoppers, pipes and other mods see (registered in PedestalCapabilities)
    public ItemStacksResourceHandler getItemHandler()
    {
        return items;
    }

    public boolean hasItem()
    {
        return !ResourceHandlerUtil.isEmpty(items);
    }

    // The stack shown on top: the first slot that has something
    public ItemStack getItemInPedestal()
    {
        for (int index = 0; index < items.size(); index++)
        {
            ItemStack stack = ItemUtil.getStack(items, index);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    // Puts as much of the stack in as fits; returns how many went in. Doesn't change the stack passed in.
    // simulate = only work out how many would fit
    public int insertItem(ItemStack stack, boolean simulate)
    {
        if (stack.isEmpty()) return 0;
        // try-with-resources: the transaction is closed at the end of the block, and anything not committed is undone
        try (Transaction transaction = Transaction.openRoot())
        {
            int inserted = ResourceHandlerUtil.insertStacking(items, ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate) transaction.commit();
            return inserted;
        }
    }

    // Takes up to maxAmount (and at most one normal stack) from the last slot that has something, like 1.20
    public ItemStack removeItem(int maxAmount)
    {
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

    // "Stone 32, Dirt 5" for the right-click message
    public Component describeContents()
    {
        MutableComponent message = Component.empty();
        boolean first = true;
        for (int index = 0; index < items.size(); index++)
        {
            ItemStack stack = ItemUtil.getStack(items, index);
            if (stack.isEmpty()) continue;
            if (!first) message.append(", ");
            message.append(stack.getHoverName()).append(" " + stack.getCount());
            first = false;
        }
        return message.withStyle(ChatFormatting.GOLD);
    }

    // 0-15 by how full the slots are, same maths as a chest
    public int getRedstoneSignal()
    {
        return ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(items);
    }

    // Called by vanilla just before the block is removed (broken, exploded, replaced): spill the items
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state)
    {
        super.preRemoveSideEffects(pos, state);
        if (level == null) return;
        for (int index = 0; index < items.size(); index++)
        {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), ItemUtil.getStack(items, index));
        }
    }

    // ---- Colour (ITintHolder) ----

    @Override public int getColor(int region) { return tints.getColor(region); }
    @Override public void setColor(int region, int color) { tints.setColor(region, color); }
    @Override public int getTint(int tintIndex) { return tints.getTint(tintIndex); }

    // ---- Saving ----

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);
        items.serialize(output); // saved under "stacks"
        tints.save(output);      // saved under "color"
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);
        items.deserialize(input);
        tints.load(input);
    }

    // ---- Syncing to clients: items (for the renderer) and colour ----

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
