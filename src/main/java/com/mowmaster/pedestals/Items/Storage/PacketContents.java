package com.mowmaster.pedestals.Items.Storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;

import java.util.ArrayList;
import java.util.List;

// What a storage packet holds: resources of one transport type, with amounts (NeoForge's ResourceStack record).
// Generic like the transport types: PacketContents<FluidResource> holds fluids, and so on. The component type is
// PacketContents<?> ("some type"), so one component works for every packet.
//
// Saved as {"type": "pedestals:fluids", "contents": [{"resource": {...}, "amount": 16000}]}: "dispatch" reads the
// type first, then uses that type's resource codec for the rest.
public record PacketContents<R extends Resource>(PedestalTransportType<R> type, List<ResourceStack<R>> contents)
{
    public static final Codec<PacketContents<?>> CODEC = PedestalTransportTypes.REGISTRY.byNameCodec()
            .dispatch("type", PacketContents::type, type -> codecFor(type));

    // Through the codec: simple, and packets are rarely sent
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketContents<?>> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public PacketContents
    {
        contents = List.copyOf(contents);
    }

    // The codec for one type's contents. Types without a resource codec can't be in packets.
    private static <R extends Resource> MapCodec<PacketContents<R>> codecFor(PedestalTransportType<R> type)
    {
        Codec<R> resourceCodec = type.getResourceCodec();
        if (resourceCodec == null) throw new IllegalStateException("Transport type " + type.getId() + " has no resource codec, so it can't be in a packet");
        Codec<ResourceStack<R>> stackCodec = RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.fieldOf("resource").forGetter(ResourceStack::resource),
                Codec.INT.fieldOf("amount").forGetter(ResourceStack::amount))
                .apply(instance, ResourceStack::new));
        return stackCodec.listOf().fieldOf("contents").xmap(list -> new PacketContents<>(type, list), PacketContents::contents);
    }

    public boolean isEmpty()
    {
        return contents.isEmpty();
    }

    public int totalAmount()
    {
        return contents.stream().mapToInt(ResourceStack::amount).sum();
    }

    // Adds amount of resource, merged into the matching entry if there is one
    public PacketContents<R> plus(R resource, int amount)
    {
        List<ResourceStack<R>> changed = new ArrayList<>(contents);
        for (int index = 0; index < changed.size(); index++)
        {
            if (changed.get(index).resource().equals(resource))
            {
                changed.set(index, new ResourceStack<>(resource, changed.get(index).amount() + amount));
                return new PacketContents<>(type, changed);
            }
        }
        changed.add(new ResourceStack<>(resource, amount));
        return new PacketContents<>(type, changed);
    }
}
