package com.mowmaster.ironpedestals;

import com.mojang.logging.LogUtils;
import com.mowmaster.ironpedestals.Blocks.BlockRegistry;
import com.mowmaster.ironpedestals.Blocks.libBlockEntityRegistry;
import com.mowmaster.ironpedestals.Items.ItemRegistry;
import com.mowmaster.ironpedestals.Items.PedestalDataComponents;
import com.mowmaster.ironpedestals.QOLHandler.CreativeTabRegistry;
import com.mowmaster.ironpedestals.Menus.PedestalMenus;
import com.mowmaster.ironpedestals.Recipes.PedestalRecipes;
import com.mowmaster.ironpedestals.Transport.PedestalTransportRules;
import com.mowmaster.ironpedestals.Transport.PedestalTransportTypeAliases;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(IronPedestalReferences.MODID)
public class ironpedestals {
    // Kept so older references still work; IronPedestalReferences.MODID is the one to use
    public static final String MODID = IronPedestalReferences.MODID;
    public static final Logger LOGGER = LogUtils.getLogger();

    // FML passes the mod event bus and container in automatically
    public ironpedestals(IEventBus modEventBus, ModContainer modContainer) {
        // Each DeferredRegister has to be hooked to the mod bus, or nothing in it gets registered
        // The transport types themselves are MowLib's (it registers them); this only adds the old ironpedestals: ids
        PedestalTransportTypeAliases.register(modEventBus);
        PedestalDataComponents.register(modEventBus);
        ItemRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        libBlockEntityRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
        PedestalRecipes.register(modEventBus);
        PedestalMenus.register(modEventBus);

        // Pedestals' numbers for MowLib's types (rates and capacities per tier), once every registry is done
        modEventBus.addListener(FMLCommonSetupEvent.class, event -> PedestalTransportRules.registerBuiltIn());

        modContainer.registerConfig(ModConfig.Type.LOCAL, Config.SPEC);
    }
}
