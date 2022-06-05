package com.mowmaster.pedestals;

import com.mowmaster.pedestals.Capability.Experience.CapabilityExperience;
import com.mowmaster.pedestals.Client.ClientItemTooltipComponent;
import com.mowmaster.pedestals.Client.ItemTooltipComponent;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Networking.DustPacketHandler;
import com.mowmaster.pedestals.Registry.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
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

import java.util.stream.Collectors;

import static com.mowmaster.pedestals.PedestalUtils.References.MODNAME;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("pedestals")
public class pedestals
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public pedestals() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
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

        eventBus.register(PedestalConfig.class);


        DeferredRegisterItems.ITEMS.register(eventBus);
        DeferredRegisterBlocks.BLOCKS.register(eventBus);
        DeferredRegisterTileBlocks.BLOCKS.register(eventBus);
        DeferredBlockEntityTypes.BLOCK_ENTITIES.register(eventBus);

        addRecipes(eventBus);
    }

    public void addRecipes(IEventBus event)
    {
        DeferredRecipeSerializers.RECIPES.register(event);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Initialize "+MODNAME+" WorldGen");
        DustPacketHandler.registerMessages();
    }

    private void setupClient(final FMLClientSetupEvent event)
    {
        LOGGER.info("Initialize "+MODNAME+" Block Entity Renders");
        ClientRegistry.registerBlockEntityRenderers();

        LOGGER.info("Initialize "+MODNAME+" Tooltip Renders");
        MinecraftForgeClient.registerTooltipComponentFactory(ItemTooltipComponent.class, ClientItemTooltipComponent::new);
    }

    /*private void setupPreClient(final TextureStitchEvent.Pre event)
    {
        LOGGER.info("Initialize "+MODNAME+" Texture Sprites/Atlas");
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
    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void attachCapabilities(final RegisterCapabilitiesEvent event) {
            CapabilityExperience.register(event);
        }
    }
}
