package com.mowmaster.pedestals.client;

import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.render.RenderPedestal;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientDust {

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e) {
        RenderPedestal.init(e);
    }
}
