package com.mowmaster.pedestals.Registry;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.CatStatuePedestal.CatStatuePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.ColorablePedestal.ColorablePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.RatStatuePedestal.RatStatuePedestalBlock;
import com.mowmaster.pedestals.PedestalTab.PedestalsTab;
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

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class DeferredRegisterTileBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,MODID);

    public static final RegistryObject<Block> BLOCK_PEDESTAL = registerBlock("block_pedestal",
            () -> new ColorablePedestalBlock(BlockBehaviour.Properties.of(Material.STONE).strength(2.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> BLOCK_RATSTATUE_PEDESTAL = registerBlock("block_ratstatue_pedestal",
            () -> new RatStatuePedestalBlock(BlockBehaviour.Properties.of(Material.STONE).strength(2.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> BLOCK_CATSTATUE_PEDESTAL = registerBlock("block_catstatue_pedestal",
            () -> new CatStatuePedestalBlock(BlockBehaviour.Properties.of(Material.STONE).strength(2.0F).sound(SoundType.STONE)));

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
}
