package com.mowmaster.pedestals.Network;

import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;

// Tells clients to draw a transport type moving from one place to another (PedestalTransportType#addTransferParticles).
// Vanilla's particle packet can't do this: fluid colours are only known on the client, and add-on types can draw
// whatever they like. A payload is a mod's own network message: a record with a Type (its id) and a StreamCodec.
//
// type is null when the client doesn't know the type id (a mismatched add-on); the handler then does nothing.
// resource is null when the type has no resource stream codec.
public record TransferParticlesPayload(@Nullable PedestalTransportType<?> type, Vec3 from, Vec3 to, @Nullable Resource resource) implements CustomPacketPayload
{
    public static final Type<TransferParticlesPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PedestalReferences.MODID, "transfer_particles"));

    // Written by hand because the resource's codec depends on the type: the type id first, then its resource
    public static final StreamCodec<RegistryFriendlyByteBuf, TransferParticlesPayload> STREAM_CODEC = StreamCodec.of(
            TransferParticlesPayload::write, TransferParticlesPayload::read);

    @Override
    public Type<TransferParticlesPayload> type()
    {
        return TYPE;
    }

    public static void send(ServerLevel level, PedestalTransportType<?> type, Vec3 from, Vec3 to, @Nullable Resource resource)
    {
        PacketDistributor.sendToPlayersTrackingChunk(level, ChunkPos.containing(BlockPos.containing(to)), new TransferParticlesPayload(type, from, to, resource));
    }

    private static void write(RegistryFriendlyByteBuf buf, TransferParticlesPayload payload)
    {
        Identifier.STREAM_CODEC.encode(buf, payload.type().getId());
        Vec3.STREAM_CODEC.encode(buf, payload.from());
        Vec3.STREAM_CODEC.encode(buf, payload.to());
        writeResource(buf, payload.type(), payload.resource());
    }

    // <R> names the type's resource class so its codec can be used; the cast is safe because send() is only
    // reached through PedestalTransportType#sendTransferParticles, which takes an R
    @SuppressWarnings("unchecked")
    private static <R extends Resource> void writeResource(RegistryFriendlyByteBuf buf, PedestalTransportType<R> type, @Nullable Resource resource)
    {
        StreamCodec<RegistryFriendlyByteBuf, R> codec = type.getResourceStreamCodec();
        boolean hasResource = codec != null && resource != null;
        buf.writeBoolean(hasResource);
        if (hasResource) codec.encode(buf, (R) resource);
    }

    private static TransferParticlesPayload read(RegistryFriendlyByteBuf buf)
    {
        PedestalTransportType<?> type = PedestalTransportTypes.REGISTRY.getValue(Identifier.STREAM_CODEC.decode(buf));
        Vec3 from = Vec3.STREAM_CODEC.decode(buf);
        Vec3 to = Vec3.STREAM_CODEC.decode(buf);
        Resource resource = null;
        if (buf.readBoolean())
        {
            // Without the type the rest can't be read; this is the last field, so it can just be skipped
            if (type == null || type.getResourceStreamCodec() == null) buf.skipBytes(buf.readableBytes());
            else resource = type.getResourceStreamCodec().decode(buf);
        }
        return new TransferParticlesPayload(type, from, to, resource);
    }

    // On the client's main thread (NeoForge's default for payload handlers)
    public static void handle(TransferParticlesPayload payload, IPayloadContext context)
    {
        if (payload.type() == null) return;
        Level level = context.player().level();
        show(payload.type(), level, payload);
    }

    @SuppressWarnings("unchecked")
    private static <R extends Resource> void show(PedestalTransportType<R> type, Level level, TransferParticlesPayload payload)
    {
        type.addTransferParticles(level, payload.from(), payload.to(), (R) payload.resource(), level.getRandom());
    }
}
