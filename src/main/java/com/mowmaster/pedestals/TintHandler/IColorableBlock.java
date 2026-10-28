package com.mowmaster.pedestals.TintHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

// Implement this on any block (full block, stairs, slab, wall...) that can be dyed.
// Extending EntityBlock means implementing this interface is all a block needs to get a CBTintBlockEntity.
// EntityBlock#getTicker defaults to null, so the block entity never ticks.
public interface IColorableBlock extends EntityBlock
{
    // Returned when there is no region to report
    int NO_REGION = -1;
/*
    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new libTintBlockEntity(pos, state);
    }
*/
    // ---- Tint regions: override these for blocks with more than one colour ----

    // How many independently coloured regions this block has
    default int getTintRegionCount()
    {
        return 1;
    }

    // Which region the player clicked
    default int getTintRegion(BlockState state, BlockHitResult hit)
    {
        return 0;
    }

    // Which region a model face's "tintindex" reads its colour from
    default int getRegionForTintIndex(BlockState state, int tintIndex)
    {
        return tintIndex;
    }

    // Called when this block's state changes but its block entity is kept (e.g. a slab becoming a double slab).
    // Return the region that was just added, or NO_REGION if the change didn't add one.
    default int getAddedRegion(BlockState oldState, BlockState newState)
    {
        return NO_REGION;
    }

    // The colour an undyed region shows (white leaves the texture as it is), e.g. the chest knob's light grey
    default int getDefaultColor(int region)
    {
        return libTintBlockEntity.DEFAULT_TINT;
    }

    // false for blocks with their own block entity (chest, barrel) instead of the plain CBTintBlockEntity
    default boolean usesTintBlockEntity()
    {
        return true;
    }

    // ---- Dyeing ----

    // Call this from the block's useItemOn override.
    default InteractionResult tryDyeBlock(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, BlockHitResult hit)
    {
        Integer dyeColor = dyeColorOf(stack);
        if (dyeColor == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(level.getBlockEntity(pos) instanceof ITintHolder tintEntity)) return InteractionResult.PASS;

        if (!level.isClientSide())
        {
            int region = getTintRegion(state, hit);
            tintEntity.setColor(region, mixDye(tintEntity.getColor(region), dyeColor));
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1f, 1f);
        }
        // SUCCESS on both sides so the client swings the arm
        return InteractionResult.SUCCESS;
    }

    // The colour an item dyes with: vanilla (and tagged modded) dyes, or a Custom Dye. Null if it isn't a dye.
    @Nullable
    static Integer dyeColorOf(ItemStack stack)
    {
        DyeColor dye = DyeColor.getColor(stack);
        if (dye != null) return dye.getTextureDiffuseColor() & 0xFFFFFF;

        return null;
    }

    // Same mixing maths as leather armour: average the channels, then restore the average brightness
    // so mixing doesn't keep getting darker. An undyed region takes the dye colour directly.
    static int mixDye(int currentColor, int dyeColor)
    {
        if (currentColor == libTintBlockEntity.UNDYED) return dyeColor;

        int red = (red(currentColor) + red(dyeColor)) / 2;
        int green = (green(currentColor) + green(dyeColor)) / 2;
        int blue = (blue(currentColor) + blue(dyeColor)) / 2;

        float averageBrightness = (brightest(currentColor) + brightest(dyeColor)) / 2f;
        int mixedBrightness = Math.max(red, Math.max(green, blue));
        if (mixedBrightness == 0) return 0;

        red = (int) (red * averageBrightness / mixedBrightness);
        green = (int) (green * averageBrightness / mixedBrightness);
        blue = (int) (blue * averageBrightness / mixedBrightness);
        return (red << 16) | (green << 8) | blue;
    }

    private static int red(int rgb) { return (rgb >> 16) & 0xFF; }
    private static int green(int rgb) { return (rgb >> 8) & 0xFF; }
    private static int blue(int rgb) { return rgb & 0xFF; }
    private static int brightest(int rgb) { return Math.max(red(rgb), Math.max(green(rgb), blue(rgb))); }
}