package com.mowmaster.pedestals;

import com.mowmaster.pedestals.Blocks.BlockRegistry;
import com.mowmaster.pedestals.Blocks.libBlockEntityRegistry;
import com.mowmaster.pedestals.ClientSide.BasePedestalBlockEntityRenderer;
import com.mowmaster.pedestals.ClientSide.libBlockEntityTintSource;
import com.mowmaster.pedestals.TintHandler.IColorableBlock;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.ArrayList;
import java.util.List;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = PedestalReferences.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = PedestalReferences.MODID, value = Dist.CLIENT)
public class PedestalsClient {
    public PedestalsClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs (Mods screen > Pedestals > Config)
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    // Replaces 1.20's RegisterColorHandlersEvent.Block lambda that read the colour from the blockstate.
    // Every colourable block gets one tint source per tint region (region N = model tintindex N), same as CrystalBlocks.
    @SubscribeEvent
    public static void registerTintBlocks(RegisterColorHandlersEvent.BlockTintSources event)
    {
        for (var holder : BlockRegistry.BCBLOCKS.getEntries())
        {
            Block block = holder.get();
            if (block instanceof IColorableBlock colorable)
            {
                List<BlockTintSource> sources = new ArrayList<>();
                for (int region = 0; region < colorable.getTintRegionCount(); region++)
                {
                    sources.add(new libBlockEntityTintSource(region));
                }
                event.register(sources, block);
            }
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(libBlockEntityRegistry.PEDESTAL.get(), BasePedestalBlockEntityRenderer::new);
    }
}
