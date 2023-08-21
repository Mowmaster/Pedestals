package com.mowmaster.pedestals.Blocks.Pedestal.ColorablePedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Registry.DeferredBlockEntityTypes;
import com.mowmaster.pedestals.Registry.DeferredRegisterTileBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ColorablePedestalBlockEntity extends BasePedestalBlockEntity {
    public ColorablePedestalBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(DeferredBlockEntityTypes.PEDESTAL.get(), p_155229_, p_155230_);
    }

    @Override
    public Block getPedestalBlockForTile() {
        return DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get();
    }
}
