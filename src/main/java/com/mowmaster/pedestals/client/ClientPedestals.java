package com.mowmaster.pedestals.client;

import com.mowmaster.pedestals.blocks.PedestalBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "pedestals", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientPedestals {

    public static void init(final FMLClientSetupEvent event) {

        //MinecraftForge.EVENT_BUS.addListener(RenderPedestalOutline::render);
    }

    @SubscribeEvent
    public static void setup(ModelRegistryEvent e) {
        //RenderPedestal.init(e);
        //PedestalRender.init(e);
    }

    @SubscribeEvent
    public static void onBlockColorsReady(ColorHandlerEvent.Block event)
    {
        //PedestalBlock.handleBlockColors(event);
        //PedestalBlock.handleBlockColors(event);
    }

    @SubscribeEvent
    public static void onItemColorsReady(ColorHandlerEvent.Item event)
    {
        //PedestalBlock.handleItemColors(event);
    }
}
