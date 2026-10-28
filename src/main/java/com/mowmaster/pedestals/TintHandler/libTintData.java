package com.mowmaster.pedestals.TintHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Arrays;

// The colour storage of CBTintBlockEntity, for block entities that can't extend it (they already extend a vanilla
// container class). The owner calls save/load from its own saveAdditional/loadAdditional and sends updateTag()
// to clients. Uses the same save keys as CBTintBlockEntity ("color", "color1", ...).
public class libTintData
{
    private static final String TAG_COLOR = "color";

    private final BlockEntity owner;
    private final int[] colors;

    public libTintData(BlockEntity owner, int regions)
    {
        this.owner = owner;
        this.colors = new int[regions];
        Arrays.fill(colors, libTintBlockEntity.UNDYED);
    }

    public int getColor(int region)
    {
        return region >= 0 && region < colors.length ? colors[region] : libTintBlockEntity.UNDYED;
    }

    public void setColor(int region, int color)
    {
        if (region < 0 || region >= colors.length) return;
        colors[region] = color;
        owner.setChanged();
        Level level = owner.getLevel();
        if (level != null)
        {
            // Sends the owner's getUpdatePacket() to nearby clients
            level.sendBlockUpdated(owner.getBlockPos(), owner.getBlockState(), owner.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // Undyed regions use the block's default colour for that region (white unless the block says otherwise)
    public int getTint(int tintIndex)
    {
        BlockState state = owner.getBlockState();
        int region = state.getBlock() instanceof IColorableBlock colorable ? colorable.getRegionForTintIndex(state, tintIndex) : tintIndex;
        int color = getColor(region);
        if (color != libTintBlockEntity.UNDYED) return color;
        return state.getBlock() instanceof IColorableBlock colorable ? colorable.getDefaultColor(region) : libTintBlockEntity.DEFAULT_TINT;
    }

    private static String key(int region)
    {
        return region == 0 ? TAG_COLOR : TAG_COLOR + region;
    }

    public void save(ValueOutput output)
    {
        for (int region = 0; region < colors.length; region++)
        {
            if (colors[region] != libTintBlockEntity.UNDYED) output.putInt(key(region), colors[region]);
        }
    }

    public void load(ValueInput input)
    {
        for (int region = 0; region < colors.length; region++)
        {
            colors[region] = input.getIntOr(key(region), libTintBlockEntity.UNDYED);
        }
        // On the client, re-mesh the chunk so the block model picks up the new colours
        Level level = owner.getLevel();
        if (level != null && level.isClientSide())
        {
            level.sendBlockUpdated(owner.getBlockPos(), owner.getBlockState(), owner.getBlockState(), Block.UPDATE_ALL);
        }
    }

    // What the client is sent: only the colours (never the stored items)
    public CompoundTag updateTag()
    {
        CompoundTag tag = new CompoundTag();
        for (int region = 0; region < colors.length; region++)
        {
            if (colors[region] != libTintBlockEntity.UNDYED) tag.putInt(key(region), colors[region]);
        }
        return tag;
    }
}
