package com.mowmaster.pedestals.Blocks.Pedestal.GoblinStatuePedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Registry.DeferredBlockEntityTypes;
import com.mowmaster.pedestals.Registry.DeferredRegisterTileBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GoblinStatuePedestalBlockEntity extends BasePedestalBlockEntity {

    public GoblinStatuePedestalBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(DeferredBlockEntityTypes.GOBLINSTATUE_PEDESTAL.get(),p_155229_, p_155230_);
    }

    @Override
    public Block getPedestalBlockForTile() {
        return DeferredRegisterTileBlocks.BLOCK_GOBLINSTATUE_PEDESTAL.get();
    }

    @Override
    public BlockPos getPosOfBlockBelowPedestal(Level world, int numBelow) {
        BlockPos blockBelow = getPos();
        return blockBelow.offset(0, -numBelow, 0);
    }
}
