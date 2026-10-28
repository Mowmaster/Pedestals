package com.mowmaster.pedestals.Items.Storage;

import com.mowmaster.pedestals.Items.PedestalDataComponents;
import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

// A "mechanical storage" packet (1.20's Base*BulkStorageItem): holds the contents of one transport type, so fluid,
// energy and experience can be carried as an item. Made by the packager upgrade and by breaking a pedestal,
// emptied by the unpackager upgrade. Packets can't be used by hand, and don't discharge any more (1.20 slowly
// emptied them in inventories).
//
// One packet item per transport type. For an add-on type, register
//   new StoragePacketItem(properties, YOUR_TYPE)   (YOUR_TYPE being the type's DeferredHolder, which is a Supplier)
// and give the type a resource codec (PedestalTransportType#getResourceCodec). The packager, unpackager and
// pedestal drops find the packet item for a type by looking through the item registry, so nothing else is needed.
public class StoragePacketItem extends Item
{
    // type -> packet item, filled the first time it's needed (all items are registered by then)
    private static @Nullable Map<PedestalTransportType<?>, StoragePacketItem> byType;

    private final Supplier<? extends PedestalTransportType<?>> type;

    public StoragePacketItem(Properties properties, Supplier<? extends PedestalTransportType<?>> type)
    {
        super(properties.stacksTo(1));
        this.type = type;
    }

    public PedestalTransportType<?> getType()
    {
        return type.get();
    }

    // The packet item for a type, or null if no mod made one
    public static @Nullable StoragePacketItem forType(PedestalTransportType<?> type)
    {
        if (byType == null)
        {
            Map<PedestalTransportType<?>, StoragePacketItem> found = new HashMap<>();
            for (Item item : BuiltInRegistries.ITEM)
            {
                if (item instanceof StoragePacketItem packet) found.putIfAbsent(packet.getType(), packet);
            }
            byType = found;
        }
        return byType.get(type);
    }

    public static boolean isPacket(ItemStack stack)
    {
        return stack.getItem() instanceof StoragePacketItem;
    }

    // ---- Contents ----

    // The packet's contents if it holds this type (the unchecked cast is safe: the contents record carries its type)
    @SuppressWarnings("unchecked")
    public static <R extends Resource> @Nullable PacketContents<R> getContents(ItemStack packet, PedestalTransportType<R> type)
    {
        PacketContents<?> contents = packet.get(PedestalDataComponents.PACKET_CONTENTS.get());
        return contents != null && contents.type() == type ? (PacketContents<R>) contents : null;
    }

    public static @Nullable PacketContents<?> getContents(ItemStack packet)
    {
        return packet.get(PedestalDataComponents.PACKET_CONTENTS.get());
    }

    // A packet holding contents, or EMPTY if the type has no packet item (or nothing to hold)
    public static <R extends Resource> ItemStack create(PacketContents<R> contents)
    {
        StoragePacketItem item = forType(contents.type());
        if (item == null || contents.isEmpty() || contents.type().getResourceCodec() == null) return ItemStack.EMPTY;
        ItemStack packet = new ItemStack(item);
        packet.set(PedestalDataComponents.PACKET_CONTENTS.get(), contents);
        return packet;
    }

    // Takes everything out of a handler (as part of the transaction) and returns it as packet contents
    public static <R extends Resource> PacketContents<R> extractAll(PedestalTransportType<R> type, ResourceHandler<R> handler, TransactionContext transaction)
    {
        PacketContents<R> contents = new PacketContents<>(type, List.of());
        for (int index = 0; index < handler.size(); index++)
        {
            R resource = handler.getResource(index);
            int amount = handler.getAmountAsInt(index);
            if (resource.isEmpty() || amount <= 0) continue;
            int extracted = handler.extract(index, resource, amount, transaction);
            if (extracted > 0) contents = contents.plus(resource, extracted);
        }
        return contents;
    }

    // Puts as much of the contents into a handler as fits; returns what's left (empty contents if it all went in)
    public static <R extends Resource> PacketContents<R> insertInto(PacketContents<R> contents, ResourceHandler<R> handler, TransactionContext transaction)
    {
        List<ResourceStack<R>> left = new ArrayList<>();
        for (ResourceStack<R> stack : contents.contents())
        {
            int inserted = handler.insert(stack.resource(), stack.amount(), transaction);
            if (inserted < stack.amount()) left.add(new ResourceStack<>(stack.resource(), stack.amount() - inserted));
        }
        return new PacketContents<>(contents.type(), left);
    }

    // A broken pedestal drops this storage's contents as a packet (used by the fluid, energy and experience storages)
    public static <R extends Resource> void dropStorage(Level level, BlockPos pos, PedestalTransportType<R> type, PedestalStorage<R> storage)
    {
        if (storage.isEmpty() || forType(type) == null) return;
        ItemStack packet;
        try (Transaction transaction = Transaction.openRoot())
        {
            packet = create(extractAll(type, storage, transaction));
            transaction.commit();
        }
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), packet);
    }

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        PacketContents<?> contents = getContents(stack);
        if (contents == null || contents.isEmpty())
        {
            tooltip.accept(Component.translatable(PedestalReferences.MODID + ".bulkstorage_empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.accept(Component.translatable(PedestalReferences.MODID + ".bulkstorage_item").withStyle(ChatFormatting.GOLD));
        describe(contents, tooltip);
    }

    // A generic helper again, so each stack's resource has the type's R (see BasePedestalBlockEntity#send)
    private static <R extends Resource> void describe(PacketContents<R> contents, Consumer<Component> tooltip)
    {
        for (ResourceStack<R> entry : contents.contents())
        {
            tooltip.accept(Component.literal(" ").append(contents.type().describeAmount(entry.resource(), entry.amount())).withStyle(ChatFormatting.WHITE));
        }
    }
}
