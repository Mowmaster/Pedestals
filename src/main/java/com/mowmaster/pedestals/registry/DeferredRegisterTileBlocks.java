package com.mowmaster.pedestals.registry;

import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.pedestaltab.PedestalsTab;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;

public class DeferredRegisterTileBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,MODID);

    public static final RegistryObject<Block> BLOCK_PEDESTAL = registerBlock(
            () -> new BasePedestalBlock(BlockBehaviour.Properties.of(Material.STONE).strength(2.0F).sound(SoundType.STONE)));

    private static <T extends Block> RegistryObject<T> registerBlock(Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register("block_pedestal", block);
        registerBlockItem(toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(RegistryObject<T> block) {
        DeferredRegisterItems.ITEMS.register("block_pedestal", () -> new BlockItem(block.get(),
                new Item.Properties().tab(PedestalsTab.TAB_ITEMS)));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
