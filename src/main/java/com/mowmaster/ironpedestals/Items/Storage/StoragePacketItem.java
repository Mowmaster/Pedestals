package com.mowmaster.ironpedestals.Items.Storage;

import com.mowmaster.mowlib.registry.MowLibDataComponents;
import com.mowmaster.mowlib.transport.TransportContents;
import com.mowmaster.mowlib.transport.TransportStorage;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.ironpedestals.IronPedestalReferences;
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
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
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
// and give the type a resource codec (TransportType#getResourceCodec). The packager, unpackager and
// pedestal drops find the packet item for a type by looking through the item registry, so nothing else is needed.
public class StoragePacketItem extends Item
{
    // type -> packet item, filled the first time it's needed (all items are registered by then)
    private static @Nullable Map<TransportType<?>, StoragePacketItem> byType;

    private final Supplier<? extends TransportType<?>> type;

    public StoragePacketItem(Properties properties, Supplier<? extends TransportType<?>> type)
    {
        super(properties.stacksTo(1));
        this.type = type;
    }

    public TransportType<?> getType()
    {
        return type.get();
    }

    // The packet item for a type, or null if no mod made one
    public static @Nullable StoragePacketItem forType(TransportType<?> type)
    {
        if (byType == null)
        {
            Map<TransportType<?>, StoragePacketItem> found = new HashMap<>();
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
    public static <R extends Resource> @Nullable TransportContents<R> getContents(ItemStack packet, TransportType<R> type)
    {
        TransportContents<?> contents = packet.get(MowLibDataComponents.TRANSPORT_CONTENTS.get());
        return contents != null && contents.type() == type ? (TransportContents<R>) contents : null;
    }

    public static @Nullable TransportContents<?> getContents(ItemStack packet)
    {
        return packet.get(MowLibDataComponents.TRANSPORT_CONTENTS.get());
    }

    // A packet holding contents, or EMPTY if the type has no packet item (or nothing to hold)
    public static <R extends Resource> ItemStack create(TransportContents<R> contents)
    {
        StoragePacketItem item = forType(contents.type());
        if (item == null || contents.isEmpty() || contents.type().getResourceCodec() == null) return ItemStack.EMPTY;
        ItemStack packet = new ItemStack(item);
        packet.set(MowLibDataComponents.TRANSPORT_CONTENTS.get(), contents);
        return packet;
    }

    // A broken pedestal drops this storage's contents as a packet (used by the fluid, energy and experience storages)
    public static <R extends Resource> void dropStorage(Level level, BlockPos pos, TransportType<R> type, TransportStorage<R> storage)
    {
        if (storage.isEmpty() || forType(type) == null) return;
        ItemStack packet;
        try (Transaction transaction = Transaction.openRoot())
        {
            packet = create(TransportContents.extractAll(type, storage, transaction));
            transaction.commit();
        }
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), packet);
    }

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        TransportContents<?> contents = getContents(stack);
        if (contents == null || contents.isEmpty())
        {
            tooltip.accept(Component.translatable(IronPedestalReferences.MODID + ".bulkstorage_empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.accept(Component.translatable(IronPedestalReferences.MODID + ".bulkstorage_item").withStyle(ChatFormatting.GOLD));
        describe(contents, tooltip);
    }

    // A generic helper again, so each stack's resource has the type's R (see BasePedestalBlockEntity#send)
    private static <R extends Resource> void describe(TransportContents<R> contents, Consumer<Component> tooltip)
    {
        for (ResourceStack<R> entry : contents.contents())
        {
            tooltip.accept(Component.literal(" ").append(contents.type().describeAmount(entry.resource(), entry.amount())).withStyle(ChatFormatting.WHITE));
        }
    }
}
