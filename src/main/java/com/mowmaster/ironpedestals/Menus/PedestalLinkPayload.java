package com.mowmaster.ironpedestals.Menus;

import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Client -> server, from the pedestal menu's link list: remove the link between the open pedestal and other
// (whichever way it goes), or turn it round (flip). Both pedestals update. Sent for outgoing links (this pedestal
// sends to other) and incoming ones (other sends here), so it names the other pedestal rather than a list index.
// A payload is a record holding the data, a Type (its id) and a StreamCodec (how it's written and read).
public record PedestalLinkPayload(BlockPos other, boolean flip) implements CustomPacketPayload
{
    public static final Type<PedestalLinkPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IronPedestalReferences.MODID, "pedestal_link"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PedestalLinkPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PedestalLinkPayload::other,
            ByteBufCodecs.BOOL, PedestalLinkPayload::flip,
            PedestalLinkPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    // Runs on the server's main thread. The client can send anything, so it only works on the pedestal whose menu
    // the player has open (and still may use), and only on links that pedestal actually has.
    public static void handle(PedestalLinkPayload payload, IPayloadContext context)
    {
        Player player = context.player();
        if (!(player.containerMenu instanceof PedestalMenu menu) || !menu.stillValid(player)) return;
        BasePedestalBlockEntity pedestal = menu.getPedestal();
        BlockPos other = payload.other();
        BasePedestalBlockEntity otherPedestal = player.level().getBlockEntity(other) instanceof BasePedestalBlockEntity found ? found : null;
        boolean outgoing = pedestal.getLinks().contains(other);
        boolean incoming = otherPedestal != null && otherPedestal.getLinks().contains(pedestal.getBlockPos());
        if (!outgoing && !incoming) return;

        if (payload.flip())
        {
            BasePedestalBlockEntity.LinkResult result = pedestal.flipLink(other);
            if (!result.changed()) player.sendOverlayMessage(result.message(false));
        }
        else
        {
            pedestal.removeLink(other);
            if (otherPedestal != null) otherPedestal.removeLink(pedestal.getBlockPos());
        }
    }
}
