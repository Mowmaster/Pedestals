package com.mowmaster.ironpedestals.Blocks;

import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.resource.Resource;

// Lets hoppers, pipes and other mods reach the pedestal's storages. Each MowLib transport type registers its own
// capability (items, fluids, energy, mowlib:experience_handler, and any add-on type), so a new type needs no
// changes here.
@EventBusSubscriber(modid = IronPedestalReferences.MODID)
public class PedestalCapabilities
{
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        for (TransportType<?> type : TransportTypes.REGISTRY) register(event, type);
    }

    // A generic helper so the storage handed over has the type's own R
    private static <R extends Resource> void register(RegisterCapabilitiesEvent event, TransportType<R> type)
    {
        type.registerBlockCapability(event, libBlockEntityRegistry.PEDESTAL.get(), (pedestal, side) -> pedestal.getStorage(type));
    }
}
