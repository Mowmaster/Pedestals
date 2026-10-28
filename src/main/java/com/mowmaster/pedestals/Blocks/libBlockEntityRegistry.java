package com.mowmaster.pedestals.Blocks;

import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.TintHandler.IColorableBlock;
import com.mowmaster.pedestals.TintHandler.libTintBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class libBlockEntityRegistry
{
    public static final DeferredRegister<BlockEntityType<?>> PEDESTAL_BLOCKENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PedestalReferences.MODID);

    // Every block in our register that implements IColorableBlock
    private static Block[] colorableBlocks()
    {
        List<Block> blocks = new ArrayList<>();
        for (var holder : BlockRegistry.BCBLOCKS.getEntries())
        {
            if (holder.get() instanceof IColorableBlock colorable && colorable.usesTintBlockEntity()) blocks.add(holder.get());
        }
        return blocks.toArray(new Block[0]);
    }

    public static void register(IEventBus eventBus)
    {
        PEDESTAL_BLOCKENTITIES.register(eventBus);
    }
}
