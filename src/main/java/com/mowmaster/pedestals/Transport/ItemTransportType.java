package com.mowmaster.pedestals.Transport;

import com.mojang.serialization.Codec;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.api.filter.FilterDirection;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorageHost;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemTransportType extends PedestalTransportType<ItemResource>
{
    public ItemTransportType()
    {
        super(ChatFormatting.GOLD);
    }

    @Override
    public PedestalStorage<ItemResource> createStorage(PedestalStorageHost host)
    {
        return new Storage(host, this);
    }

    @Override
    public int getTransferRate(PedestalStorageHost host)
    {
        return Config.ITEM_TRANSFER_RATE.getAsInt() + host.getTransferBonus(this);
    }

    // ---- Augments ----

    @Override
    public int getCapacityAugmentBonus(int tier)
    {
        return tier < 1 || tier > Config.CAPACITY_AUGMENTS.size() ? 0 : Config.CAPACITY_AUGMENTS.get(tier - 1).items().getAsInt();
    }

    @Override
    public int getStorageAugmentBonus(int tier)
    {
        return tier < 1 || tier > Config.STORAGE_AUGMENTS.size() ? 0 : Config.STORAGE_AUGMENTS.get(tier - 1).items().getAsInt();
    }

    // The item on top is drawn by the client
    @Override
    public boolean syncsToClient()
    {
        return true;
    }

    @Override
    public void registerCapability(RegisterCapabilitiesEvent event, BlockEntityType<BasePedestalBlockEntity> pedestal)
    {
        event.registerBlockEntity(Capabilities.Item.BLOCK, pedestal, (blockEntity, side) -> blockEntity.getStorage(this));
    }

    // ---- Other things' storage ----

    @Override
    public @Nullable ResourceHandler<ItemResource> getBlockHandler(Level level, BlockPos pos, @Nullable Direction side)
    {
        return level.getCapability(Capabilities.Item.BLOCK, pos, side);
    }

    // ENTITY_AUTOMATION is the one meant for machines (chest minecarts, ...)
    @Override
    public @Nullable ResourceHandler<ItemResource> getEntityHandler(Entity entity, @Nullable Direction side)
    {
        return Capabilities.Item.ENTITY_AUTOMATION.getCapability(entity, side);
    }

    // Items inside items: shulker boxes, bundles...
    @Override
    public @Nullable ResourceHandler<ItemResource> getItemHandler(ItemAccess access)
    {
        return access.getCapability(Capabilities.Item.ITEM);
    }

    // ---- Upgrades ----

    @Override
    public String getCapacityModifier()
    {
        return "upgradeitemcapacity";
    }

    @Override
    public int getDefaultUpgradeRate(PedestalStorageHost host)
    {
        return Config.ITEM_TRANSFER_RATE.getAsInt();
    }

    // Drops up to amount (at most one stack) of the first item, at the centre of pos, without throwing it.
    // Only into open space (air, fluids, grass...), as in 1.20.
    @Override
    public boolean dropInWorld(ServerLevel level, BlockPos pos, PedestalStorage<ItemResource> from, int amount, @Nullable ServerPlayer cause)
    {
        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) return false;
        ResourceStack<ItemResource> taken;
        try (Transaction transaction = Transaction.openRoot())
        {
            ItemResource first = ResourceHandlerUtil.findExtractableResource(from, resource -> true, transaction);
            if (first == null) return false;
            int extracted = from.extract(first, Math.min(amount, first.getMaxStackSize()), transaction);
            if (extracted <= 0) return false;
            taken = new ResourceStack<>(first, extracted);
            transaction.commit();
        }
        Vec3 centre = Vec3.atCenterOf(pos);
        ItemEntity dropped = new ItemEntity(level, centre.x, centre.y, centre.z, taken.resource().toStack(taken.amount()), 0, 0, 0);
        dropped.setDefaultPickUpDelay();
        level.addFreshEntity(dropped);
        return true;
    }

    // Picks up item entities: as many as fit (up to amount)
    @Override
    public boolean absorbEntity(ServerLevel level, Entity entity, PedestalStorage<ItemResource> into, int amount)
    {
        if (!(entity instanceof ItemEntity itemEntity) || itemEntity.getItem().isEmpty()) return false;
        ItemStack stack = itemEntity.getItem();
        int inserted;
        try (Transaction transaction = Transaction.openRoot())
        {
            inserted = ResourceHandlerUtil.insertStacking(into, ItemResource.of(stack), Math.min(amount, stack.getCount()), transaction);
            if (inserted <= 0) return false;
            transaction.commit();
        }
        ItemStack left = stack.copyWithCount(stack.getCount() - inserted);
        if (left.isEmpty()) itemEntity.discard();
        else itemEntity.setItem(left);
        return true;
    }

    // ---- Packets ----

    @Override
    public Codec<ItemResource> getResourceCodec()
    {
        return ItemResource.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ItemResource> getResourceStreamCodec()
    {
        return ItemResource.STREAM_CODEC;
    }

    // "Stone x3456"
    @Override
    public Component describeAmount(ItemResource resource, int amount)
    {
        return Component.empty().append(resource.getHoverName()).append(" x" + amount);
    }

    // ---- Filters ----

    @Override
    public boolean usesFilterList()
    {
        return true;
    }

    @Override
    public @Nullable ItemResource resourceFromFilterEntry(ItemStack entry)
    {
        return entry.isEmpty() ? null : ItemResource.of(entry);
    }

    @Override
    public boolean isSameKind(ItemResource first, ItemResource second)
    {
        return first.getItem() == second.getItem();
    }

    @Override
    public @Nullable Identifier getRegistryName(ItemResource resource)
    {
        return BuiltInRegistries.ITEM.getKey(resource.getItem());
    }

    // NeoForge's item stack storage, with the pedestal's filter applied
    public static class Storage extends ItemStacksResourceHandler implements PedestalStorage<ItemResource>
    {
        private final PedestalStorageHost host;
        private final ItemTransportType type;

        public Storage(PedestalStorageHost host, ItemTransportType type)
        {
            super(Config.ITEM_SLOTS.getAsInt());
            this.host = host;
            this.type = type;
        }

        // How many stacks it should have: the config's base plus the storage augments' extra stacks
        public int wantedSize()
        {
            return Math.max(1, Config.ITEM_SLOTS.getAsInt() + host.getStorageBonus(type));
        }

        // Changes the number of stacks to wantedSize (setStacks is NeoForge's way to resize). Stacks that no
        // longer fit are returned, for the pedestal to drop; with shrink = false it only ever grows (when loading,
        // where nothing can be dropped yet).
        public List<ItemStack> resize(boolean shrink)
        {
            int size = wantedSize();
            if (size == stacks.size() || (!shrink && size < stacks.size())) return List.of();
            List<ItemStack> overflow = new ArrayList<>();
            NonNullList<ItemStack> resized = NonNullList.withSize(size, ItemStack.EMPTY);
            for (int index = 0; index < stacks.size(); index++)
            {
                if (index < size) resized.set(index, stacks.get(index));
                else if (!stacks.get(index).isEmpty()) overflow.add(stacks.get(index));
            }
            setStacks(resized);
            if (!overflow.isEmpty() || size != stacks.size()) host.onStorageChanged(type);
            return overflow;
        }

        @Override
        public boolean isValid(int index, ItemResource resource)
        {
            return host.filterAllows(type, resource, FilterDirection.INSERT);
        }

        @Override
        protected int getCapacity(int index, ItemResource resource)
        {
            int capacity = super.getCapacity(index, resource);
            return resource.isEmpty() ? capacity : Math.min(capacity, host.filterLimit(type, resource, capacity));
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction)
        {
            if (!host.filterAllows(type, resource, FilterDirection.EXTRACT)) return 0;
            return super.extract(index, resource, amount, transaction);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents)
        {
            host.onStorageChanged(type);
        }

        @Override
        public void onPedestalRemoved(Level level, BlockPos pos)
        {
            for (int index = 0; index < size(); index++)
            {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), ItemUtil.getStack(this, index));
            }
        }

        // "Stone 32, Dirt 5"
        @Override
        public @Nullable Component describe()
        {
            if (isEmpty()) return null;
            var message = Component.empty();
            boolean first = true;
            for (int index = 0; index < size(); index++)
            {
                ItemStack stack = ItemUtil.getStack(this, index);
                if (stack.isEmpty()) continue;
                if (!first) message.append(", ");
                message.append(stack.getHoverName()).append(" " + stack.getCount());
                first = false;
            }
            return message.withStyle(type.getColor());
        }
    }

    // Bits of the item: a block's breaking particles, or an item's (like an item breaking or being eaten)
    @Override
    public void addTransferParticles(Level level, Vec3 from, Vec3 to, @Nullable ItemResource resource, RandomSource random)
    {
        if (resource == null || resource.isEmpty())
        {
            super.addTransferParticles(level, from, to, resource, random);
            return;
        }
        ParticleOptions particle = resource.getItem() instanceof BlockItem blockItem
                ? new BlockParticleOption(ParticleTypes.BLOCK, blockItem.getBlock().defaultBlockState())
                : new ItemParticleOption(ParticleTypes.ITEM, resource.getItem());
        addParticlesAlong(level, particle, from, to, 10, random);
    }
}
