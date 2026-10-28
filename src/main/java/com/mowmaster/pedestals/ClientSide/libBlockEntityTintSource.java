package com.mowmaster.pedestals.ClientSide;

import com.mowmaster.pedestals.TintHandler.ITintHolder;
import com.mowmaster.pedestals.TintHandler.libTintBlockEntity;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class libBlockEntityTintSource implements BlockTintSource
{
    private final int tintIndex;

    public libBlockEntityTintSource(int tintIndex)
    {
        this.tintIndex = tintIndex;
    }

    // Used where there is no world position (e.g. falling block entity): show the default colour
    @Override
    public int color(@NotNull BlockState state)
    {
        return 0xFF000000 | libTintBlockEntity.DEFAULT_TINT;
    }

    // Used when the block is rendered in the world: read the colour from the block entity
    @Override
    public int colorInWorld(@NotNull BlockState state, BlockAndTintGetter level, BlockPos pos)
    {
        if (level.getBlockEntity(pos) instanceof ITintHolder tintEntity)
        {
            return 0xFF000000 | tintEntity.getTint(tintIndex); // add full alpha
        }
        return color(state);
    }
}
