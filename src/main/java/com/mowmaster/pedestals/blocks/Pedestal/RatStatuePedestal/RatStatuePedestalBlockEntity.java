package com.mowmaster.pedestals.Blocks.Pedestal.RatStatuePedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Registry.DeferredBlockEntityTypes;
import com.mowmaster.pedestals.Registry.DeferredRegisterTileBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import static com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock.*;
import static com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock.FILTER_STATUS;

public class RatStatuePedestalBlockEntity extends BasePedestalBlockEntity {

    public RatStatuePedestalBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(DeferredBlockEntityTypes.RATSTATUE_PEDESTAL.get(),p_155229_, p_155230_);
    }

    @Override
    public Block getPedestalBlockForTile() {
        return DeferredRegisterTileBlocks.BLOCK_RATSTATUE_PEDESTAL.get();
    }

    @Override
    public BlockPos getPosOfBlockBelowPedestal(Level world, int numBelow) {
        BlockPos blockBelow = getPos();
        return blockBelow.offset(0, -numBelow, 0);
    }
}
