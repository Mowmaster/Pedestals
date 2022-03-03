package com.mowmaster.pedestals.api.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public interface IAreaRangeProvider
{
    /**
     * To be used with items that store block cords to define work area or range
     */
    /**
     * Get block position data from the given ItemStack.  It is up to the implementor to decide how the block positions
     * should be stored on the itemstack and in what order they should be returned.
     *
     * @param stack the itemstack
     * @return a list of block positions that has been retrieved from the itemstack
     */
    default List<BlockPos> getStoredPositions(@Nonnull ItemStack stack) {
        BlockPos target = null;
        return target == null ? Collections.emptyList() : Collections.singletonList(target);
    }

    /**
     * Color that should be used to highlight the stored block positions if & when they are rendered on-screen.
     *
     * @param index the index in the list returned by getStoredPositions()
     * @return a color in ARGB format, or 0 to skip rendering completely
     */
    default int getRenderColor(int index) { return 0xFFFFFF00; }
}
