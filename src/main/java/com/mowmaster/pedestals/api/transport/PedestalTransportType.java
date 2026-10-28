package com.mowmaster.pedestals.api.transport;

import com.mojang.serialization.Codec;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// One kind of thing a pedestal can hold and send down its links: items, fluids, energy, experience, or anything
// another mod registers (e.g. dust). Every pedestal gets one storage per registered type, links move every type,
// filters get a mode per type, transport upgrades (import, export, void, dropper, magnet, packager...) get a mode
// per type, and every type with a resource codec can be put in a storage packet.
//
// To add a type from another mod: extend this class, and register an instance in a DeferredRegister made with
// DeferredRegister.create(PedestalTransportTypes.KEY, "yourmodid"). <R> is the type's NeoForge Resource class
// (ItemResource, FluidResource, ...). Amount-only types (energy-like) can use AmountResource and AmountStorage.
// Then, for upgrades: override getBlockHandler (import/export), getItemHandler (magnet, bottles...),
// getResourceCodec + register a StoragePacketItem for it (packets, packager/unpackager), and dropInWorld (dropper).
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

    // Moves up to amount from one handler to another: pedestal to pedestal, and pedestal to/from the blocks
    // import/export upgrades work on. Filters are already enforced by the pedestal storages themselves.
    public int transfer(ResourceHandler<R> from, ResourceHandler<R> to, int amount, TransactionContext transaction)
    {
        return ResourceHandlerUtil.moveStacking(from, to, resource -> true, amount, transaction);
    }

    // Lets hoppers, pipes and other mods reach this type's storage. Default: not reachable from outside.
    public void registerCapability(RegisterCapabilitiesEvent event, BlockEntityType<BasePedestalBlockEntity> pedestal)
    {
    }

    // ---- Reaching other things' storage (used by upgrades) ----

    // This type's handler in the block at pos, seen from side (import, export...). null = the block has none.
    public @Nullable ResourceHandler<R> getBlockHandler(Level level, BlockPos pos, @Nullable Direction side)
    {
        return null;
    }

    // This type's handler on an entity (chest minecarts...; upgrades with the entity container modifier). null = none.
    public @Nullable ResourceHandler<R> getEntityHandler(Entity entity, @Nullable Direction side)
    {
        return null;
    }

    // This type's handler inside an item (a bucket's fluid, a battery's energy...). null = none.
    // ItemAccess is NeoForge's "the item and where it lives": the handler can swap the item (full bucket -> empty bucket).
    public @Nullable ResourceHandler<R> getItemHandler(ItemAccess access)
    {
        return null;
    }

    // ---- Upgrades ----

    // The modifier that raises upgrades' rates for this type (applied by the modification upgrade's recipes).
    // Default "upgrade<path>capacity", so another mod's "dust" type gets 1.20's "upgradedustcapacity".
    public String getCapacityModifier()
    {
        return "upgrade" + getId().getPath() + "capacity";
    }

    // How much an upgrade moves per operation: its base rate plus the capacity modifier (experience counts levels)
    public int getUpgradeRate(int baseRate, int capacityModifier)
    {
        return baseRate + capacityModifier;
    }

    // The base rate for upgrades that have no config for this type (add-on types): the link transfer rate
    public int getDefaultUpgradeRate(PedestalStorageHost host)
    {
        return getTransferRate(host);
    }

    // Dropper upgrade: put up to amount from the pedestal into the world at pos (items, xp orbs, lightning...).
    // true if anything happened. Default: this type can't be dropped.
    public boolean dropInWorld(ServerLevel level, BlockPos pos, PedestalStorage<R> from, int amount, @Nullable ServerPlayer cause)
    {
        return false;
    }

    // Magnet upgrade: take up to amount of this type from an entity in its area. true if anything was taken.
    // Default: an item entity's item is emptied through getItemHandler (a bucket of water gives its water and stays
    // behind as an empty bucket). Items and experience override this (item pickup, xp orbs).
    public boolean absorbEntity(ServerLevel level, Entity entity, PedestalStorage<R> into, int amount)
    {
        if (!(entity instanceof ItemEntity itemEntity) || itemEntity.getItem().isEmpty()) return false;
        ItemEntityAccess access = new ItemEntityAccess(itemEntity);
        ResourceHandler<R> handler = getItemHandler(access.access());
        if (handler == null) return false;
        try (Transaction transaction = Transaction.openRoot())
        {
            int moved = transfer(handler, into, amount, transaction);
            if (moved <= 0) return false;
            transaction.commit();
        }
        access.writeBack(level);
        return true;
    }

    // ---- Storage packets ----

    // How this type's resource is saved, so storage packets can hold it. null = this type has no packets.
    // Packets also need an item: register a StoragePacketItem for the type (see ItemRegistry's MECHANICAL_STORAGE_*).
    public @Nullable Codec<R> getResourceCodec()
    {
        return null;
    }

    // The same for the network (packet tooltips). Only used when getResourceCodec isn't null.
    public @Nullable StreamCodec<RegistryFriendlyByteBuf, R> getResourceStreamCodec()
    {
        return null;
    }

    // One line for a packet's tooltip, e.g. "Water: 16000mB"
    public Component describeAmount(R resource, int amount)
    {
        return Component.empty().append(getName()).append(": " + amount);
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

    // The colour as RGB, for particles
    public int getRgb()
    {
        TextColor textColor = TextColor.fromLegacyFormat(color);
        return textColor != null ? textColor.getValue() : 0xFFFFFF;
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
