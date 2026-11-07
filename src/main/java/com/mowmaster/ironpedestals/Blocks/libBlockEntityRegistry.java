package com.mowmaster.ironpedestals.Blocks;

import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class libBlockEntityRegistry
{
    public static final DeferredRegister<BlockEntityType<?>> PEDESTAL_BLOCKENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IronPedestalReferences.MODID);

    // Same id as 1.20 ("block_entity_pedestal"), shared by every pedestal tier. Supplier<T> is a lambda that makes the
    // value when asked: the type is only built once the blocks it belongs to have been registered.
    // toArray(Block[]::new): the tier blocks as the Block... list the constructor takes.
    public static final Supplier<BlockEntityType<BasePedestalBlockEntity>> PEDESTAL = PEDESTAL_BLOCKENTITIES.register("block_entity_pedestal",
            () -> new BlockEntityType<>(BasePedestalBlockEntity::new, BlockRegistry.PEDESTALS.stream().map(DeferredBlock::get).toArray(Block[]::new)));

    public static void register(IEventBus eventBus)
    {
        PEDESTAL_BLOCKENTITIES.register(eventBus);
    }
}
