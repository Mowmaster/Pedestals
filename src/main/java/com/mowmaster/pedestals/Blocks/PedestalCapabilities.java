package com.mowmaster.pedestals.Blocks;

import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

// Lets hoppers, pipes and other mods reach the pedestal's storages (1.20's getCapability/LazyOptional is gone:
// capabilities are registered once, per block entity type). Each transport type registers its own (items, fluids,
// energy, experience, and any add-on type), so a new type needs no changes here.
@EventBusSubscriber(modid = PedestalReferences.MODID)
public class PedestalCapabilities
{
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        for (PedestalTransportType<?> type : PedestalTransportTypes.REGISTRY)
        {
            type.registerCapability(event, libBlockEntityRegistry.PEDESTAL.get());
        }
    }
}
