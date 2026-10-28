package com.mowmaster.pedestals.Blocks;

import com.mowmaster.pedestals.PedestalReferences;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

// Lets hoppers, pipes and other mods reach the pedestal's items (1.20's getCapability/LazyOptional is gone:
// capabilities are registered once here, per block entity type). Same setup as CrystalBlocks' CBCapabilities.
@EventBusSubscriber(modid = PedestalReferences.MODID)
public class PedestalCapabilities
{
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        // (pedestal, side) -> ... is a lambda: called with the block entity and the side being asked about
        event.registerBlockEntity(Capabilities.Item.BLOCK, libBlockEntityRegistry.PEDESTAL.get(),
                (pedestal, side) -> pedestal.getItemHandler());
    }
}
