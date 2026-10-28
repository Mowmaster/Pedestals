package com.mowmaster.pedestals.QOLHandler;

import com.mowmaster.pedestals.PedestalReferences;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class CreativeTabRegistry
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PedestalReferences.MODID);


    /*public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BIBLIOMANIA_TAB = TABS.register("bibliomania", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.bibliomania"))
            .icon(() -> new ItemStack(BlockRegistry.LEATHER_BOOK.get()))
            .displayItems((parameters, output) -> {
            }).build());*/

    public static void register(IEventBus eventBus)
    {
        TABS.register(eventBus);
    }
}
