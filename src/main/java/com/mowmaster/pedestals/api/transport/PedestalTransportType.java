package com.mowmaster.pedestals.api.transport;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// One kind of thing a pedestal can hold and send down its links: items, fluids, energy, experience, or anything
// another mod registers (e.g. dust). Every pedestal gets one storage per registered type, links move every type,
// and filters get a mode per type.
//
// To add a type from another mod: extend this class, and register an instance in a DeferredRegister made with
// DeferredRegister.create(PedestalTransportTypes.KEY, "yourmodid"). <R> is the type's NeoForge Resource class
// (ItemResource, FluidResource, ...). Amount-only types (energy-like) can use AmountResource and AmountStorage.
public abstract class PedestalTransportType<R extends Resource>
{
    private final ChatFormatting color;

    protected PedestalTransportType(ChatFormatting color)
    {
        this.color = color;
    }

    // ---- Storage ----

    // Called once for every pedestal when its block entity is made
    public abstract PedestalStorage<R> createStorage(PedestalStorageHost host);

    // How much one link transfer may move per transfer tick
    public abstract int getTransferRate(PedestalStorageHost host);

    // true if clients need the contents (e.g. the renderer shows items). Other types only save, which is cheaper.
    public boolean syncsToClient()
    {
        return false;
    }

    // Moves up to amount from one pedestal to another. Filters are already enforced by the storages themselves.
    public int transfer(PedestalStorage<R> from, PedestalStorage<R> to, int amount, TransactionContext transaction)
    {
        return ResourceHandlerUtil.moveStacking(from, to, resource -> true, amount, transaction);
    }

    // Lets hoppers, pipes and other mods reach this type's storage. Default: not reachable from outside.
    public void registerCapability(RegisterCapabilitiesEvent event, BlockEntityType<BasePedestalBlockEntity> pedestal)
    {
    }

    // ---- Filters ----

    // Whether filters can be set up for this type by sneak-right-clicking an inventory with the filter
    public boolean usesFilterList()
    {
        return false;
    }

    // What an item in a filter's list stands for in this type (the item itself, the fluid in a bucket...). null = nothing.
    public @Nullable R resourceFromFilterEntry(ItemStack entry)
    {
        return null;
    }

    // Same thing ignoring extra data (fuzzy filters): same item, same fluid
    public boolean isSameKind(R first, R second)
    {
        return first.equals(second);
    }

    // The registry name, for the mod filter (null if it has none)
    public @Nullable Identifier getRegistryName(R resource)
    {
        return null;
    }

    // ---- Display ----

    public ChatFormatting getColor()
    {
        return color;
    }

    public Identifier getId()
    {
        return Objects.requireNonNull(PedestalTransportTypes.REGISTRY.getKey(this), "Transport type is not registered");
    }

    // "Items", "Fluids"... from the lang key transport_type.<namespace>.<path>
    public Component getName()
    {
        return Component.translatable(getId().toLanguageKey("transport_type")).withStyle(color);
    }
}
