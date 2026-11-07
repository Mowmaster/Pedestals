package com.mowmaster.ironpedestals.Menus;

import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// Registers IronPedestals' network payloads. The registrar's version must match between client and server, so bump
// it whenever a payload changes. @EventBusSubscriber finds the right bus for the event by itself (this one is on the
// mod bus).
@EventBusSubscriber(modid = IronPedestalReferences.MODID)
public class PedestalNetwork
{
    private static final String VERSION = "1";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar(VERSION);
        // Client -> server
        registrar.playToServer(PedestalLinkPayload.TYPE, PedestalLinkPayload.STREAM_CODEC, PedestalLinkPayload::handle);
    }
}
