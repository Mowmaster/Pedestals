package com.mowmaster.pedestals.QOLHandler;

import com.mowmaster.pedestals.Blocks.BlockRegistry;
import com.mowmaster.pedestals.Items.ItemRegistry;
import com.mowmaster.pedestals.PedestalReferences;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class CreativeTabRegistry
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PedestalReferences.MODID);


    // Tools, filters, the plain pedestal, then one pre-dyed pedestal per dye colour (the colour rides on dyed_color)
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PEDESTALS_TAB = TABS.register("pedestals", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pedestals"))
            .icon(() -> new ItemStack(BlockRegistry.BLOCK_PEDESTAL.get()))
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.TOOL_LINKINGTOOL.get());
                output.accept(ItemRegistry.TOOL_LINKINGTOOLBACKWARDS.get());
                output.accept(ItemRegistry.TOOL_FILTERTOOL.get());
                ItemRegistry.FILTERS.forEach(filter -> output.accept(filter.get()));
                output.accept(BlockRegistry.BLOCK_PEDESTAL.get());
                for (DyeColor dye : DyeColor.values())
                {
                    ItemStack dyed = new ItemStack(BlockRegistry.BLOCK_PEDESTAL.get());
                    dyed.set(DataComponents.DYED_COLOR, new DyedItemColor(dye.getTextureDiffuseColor() & 0xFFFFFF));
                    output.accept(dyed);
                }
            }).build());

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
