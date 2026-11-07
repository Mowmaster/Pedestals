package com.mowmaster.ironpedestals.QOLHandler;

import com.mowmaster.mowlib.registry.MowLibItems;
import com.mowmaster.ironpedestals.Blocks.BlockRegistry;
import com.mowmaster.ironpedestals.Items.ItemRegistry;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTabRegistry
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IronPedestalReferences.MODID);


    // Tools, filters, work cards, upgrades, (empty) storage packets, the pedestal tiers (plain first), then one
    // pre-dyed plain pedestal per dye colour (the colour rides on dyed_color)
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PEDESTALS_TAB = TABS.register("ironpedestals", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ironpedestals"))
            .icon(() -> new ItemStack(BlockRegistry.BLOCK_PEDESTAL.get()))
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.TOOL_LINKINGTOOL.get());
                output.accept(ItemRegistry.TOOL_LINKINGTOOLBACKWARDS.get());
                output.accept(MowLibItems.TOOL_FILTER.get());
                output.accept(ItemRegistry.TOOL_UPGRADETOOL.get());
                output.accept(ItemRegistry.TOOL_TOOLSWAPPER.get());
                output.accept(MowLibItems.TOOL_WORK.get());
                ItemRegistry.AUGMENT_TOOLS.forEach(tool -> output.accept(tool.get()));
                MowLibItems.FILTERS.forEach(filter -> output.accept(filter.get()));
                ItemRegistry.WORKCARDS.forEach(card -> output.accept(card.get()));
                ItemRegistry.UPGRADES.forEach(upgrade -> output.accept(upgrade.get()));
                ItemRegistry.AUGMENTS.forEach(augment -> output.accept(augment.get()));
                ItemRegistry.PACKETS.forEach(packet -> output.accept(packet.get()));
                output.accept(ItemRegistry.SACRIFICE.get());
                BlockRegistry.PEDESTALS.forEach(pedestal -> output.accept(pedestal.get()));

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
