package com.mowmaster.pedestals.Network;

import com.mowmaster.pedestals.PedestalReferences;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

// Registers the mod's network payloads. The registrar's version ("1") must match between client and server, so
// bump it whenever a payload's format changes.
@EventBusSubscriber(modid = PedestalReferences.MODID)
public class PedestalNetwork
{
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event)
    {
        event.registrar("1").playToClient(TransferParticlesPayload.TYPE, TransferParticlesPayload.STREAM_CODEC, TransferParticlesPayload::handle);
    }
}
