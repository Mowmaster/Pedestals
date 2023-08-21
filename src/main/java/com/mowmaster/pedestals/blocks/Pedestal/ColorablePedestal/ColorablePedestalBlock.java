package com.mowmaster.pedestals.Blocks.Pedestal.ColorablePedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.RatStatuePedestal.RatStatuePedestalBlockEntity;
import com.mowmaster.pedestals.Registry.DeferredBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ColorablePedestalBlock extends BasePedestalBlock {
    public ColorablePedestalBlock(Properties p_152915_) {
        super(p_152915_);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return DeferredBlockEntityTypes.PEDESTAL.get().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : (level0, pos, state0, blockEntity) -> ((ColorablePedestalBlockEntity) blockEntity).tick();
    }
}
