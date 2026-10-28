package com.mowmaster.pedestals;

import com.mojang.logging.LogUtils;
import com.mowmaster.pedestals.Blocks.BlockRegistry;
import com.mowmaster.pedestals.Blocks.libBlockEntityRegistry;
import com.mowmaster.pedestals.Items.ItemRegistry;
import com.mowmaster.pedestals.QOLHandler.CreativeTabRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(PedestalReferences.MODID)
public class pedestals {
    // Kept so older references still work; PedestalReferences.MODID is the one to use
    public static final String MODID = PedestalReferences.MODID;
    public static final Logger LOGGER = LogUtils.getLogger();

    // FML passes the mod event bus and container in automatically
    public pedestals(IEventBus modEventBus, ModContainer modContainer) {
        // Each DeferredRegister has to be hooked to the mod bus, or nothing in it gets registered
        ItemRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        libBlockEntityRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.LOCAL, Config.SPEC);
    }
}
