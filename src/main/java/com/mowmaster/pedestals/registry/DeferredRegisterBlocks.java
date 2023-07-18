package com.mowmaster.pedestals.registry;

import com.mojang.serialization.Codec;
import com.mowmaster.pedestals.pedestaltab.PedestalsTab;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;

public class DeferredRegisterBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,MODID);


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        DeferredRegisterItems.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    //For Geodes
    private static <P extends PlacementModifier> PlacementModifierType<P> register(String name, Codec<P> codec) {
        return Registry.register(Registry.PLACEMENT_MODIFIERS, name, () -> codec);
    }
}
