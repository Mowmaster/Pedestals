package com.mowmaster.ironpedestals;

import com.mowmaster.ironpedestals.Blocks.libBlockEntityRegistry;
import com.mowmaster.ironpedestals.ClientSide.BasePedestalBlockEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = IronPedestalReferences.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = IronPedestalReferences.MODID, value = Dist.CLIENT)
public class IronPedestalsClient {
    public IronPedestalsClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs (Mods screen > Pedestals > Config)
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    // Pedestal tints: MowLibClient registers one tint source per region for every IColorableBlock (ironpedestals included)
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(libBlockEntityRegistry.PEDESTAL.get(), BasePedestalBlockEntityRenderer::new);
    }
}
