package com.mowmaster.pedestals;

import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Registry.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.mowmaster.pedestals.PedestalUtils.References.MODNAME;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("pedestals")
public class pedestals
{
    // Directly reference a log4j logger.
    public static final Logger PLOGGER = LogManager.getLogger();

    public pedestals() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupTextureStitchClient);

        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClientTooltips);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);



        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        //MinecraftForge.EVENT_BUS.register(new DustGeneration());

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> eventBus.register(new ClientRegistry()));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PedestalConfig.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, PedestalConfig.clientSpec);

        eventBus.register(PedestalConfig.class);


        DeferredRegisterItems.ITEMS.register(eventBus);
        DeferredRegisterBlocks.BLOCKS.register(eventBus);
        DeferredRegisterTileBlocks.BLOCKS.register(eventBus);
        DeferredBlockEntityTypes.BLOCK_ENTITIES.register(eventBus);

        DeferredRecipeSerializers.register(eventBus);

        DeferredCreativeTabRegistry.DEF_REG.register(eventBus);


    }

    private void setup(final FMLCommonSetupEvent event)
    {
        PLOGGER.info("Initialize "+MODNAME+" WorldGen");
    }

    private void setupClient(final FMLClientSetupEvent event)
    {
        PLOGGER.info("Initialize "+MODNAME+" Block Entity Renders");
        ClientRegistry.registerBlockEntityRenderers();

        //PLOGGER.info("Initialize "+MODNAME+" Tooltip Renders");
        //MinecraftForgeClient.registerTooltipComponentFactory(ItemTooltipComponent.class, ClientItemTooltipComponent::new);
    }

    /*private void setupClientTooltips(final RegisterClientTooltipComponentFactoriesEvent event)
    {
        PLOGGER.info("Initialize "+MODNAME+" Tooltip Renders");
        event.register(ItemTooltipComponent.class, ClientItemTooltipComponent::new);
    }*/

    /*private void setupTextureStitchClient(final TextureStitchEvent event)
    {
        PLOGGER.info("Initialize "+MODNAME+" Texture Sprites/Atlas");
        ClientRegistry.textureStitchPreEvent(event);
    }*/

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        //InterModComms.sendTo("dust", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        //LOGGER.info("Got IMC {}", event.getIMCStream().map(m->m.messageSupplier().get()).collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
        //LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    /*
    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void attachCapabilities(final RegisterCapabilitiesEvent event) {
            //CapabilityExperience.register(event);
        }
    }*/
}
