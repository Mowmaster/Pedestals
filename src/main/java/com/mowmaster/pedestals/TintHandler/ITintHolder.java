package com.mowmaster.pedestals.TintHandler;

// Anything that stores per-region colours for a block: the plain CBTintBlockEntity, and storage block entities
// (chest, barrel) that already extend a vanilla container class and so keep their colours in a CBTintData.
// Dyeing, the colour picker and the client tint sources all go through this.
public interface ITintHolder
{
    // The stored colour (0xRRGGBB), or CBTintBlockEntity.UNDYED
    int getColor(int region);

    default boolean isDyed(int region)
    {
        return getColor(region) != libTintBlockEntity.UNDYED;
    }

    void setColor(int region, int color);

    // The colour to draw a model tintindex with: the stored colour, or the region's default if undyed
    int getTint(int tintIndex);
}
