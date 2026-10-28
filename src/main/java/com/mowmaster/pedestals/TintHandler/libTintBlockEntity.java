package com.mowmaster.pedestals.TintHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Arrays;

public class libTintBlockEntity extends BlockEntity implements ITintHolder
{
    // Colours are stored as 0xRRGGBB with no alpha, so -1 can never be a real colour
    public static final int UNDYED = -1;
    // Colour shown for an undyed region. White leaves the greyscale texture as-is.
    public static final int DEFAULT_TINT = 0xFFFFFF;
    // Region 0 keeps the original key so existing saves still load; region N saves as "colorN"
    private static final String TAG_COLOR = "color";

    // One colour per tint region. Single-colour blocks only have region 0.
    private final int[] colors;
    // The region most recently added by a state change (e.g. second slab placed). Not saved: it only lives
    // between the state change and setPlacedBy during a single placement.
    private int addedRegion = IColorableBlock.NO_REGION;

    public libTintBlockEntity(BlockPos pos, BlockState state)
    {
        //super(libBlockEntityRegistry.TINT_BLOCK_ENTITY.get(), pos, state);
        super(null, pos, state);
        int regions = state.getBlock() instanceof IColorableBlock colorable ? colorable.getTintRegionCount() : 1;
        this.colors = new int[regions];
        Arrays.fill(colors, UNDYED);
    }

    public int getColor(int region)
    {
        return isValidRegion(region) ? colors[region] : UNDYED;
    }

    public boolean isDyed(int region)
    {
        return getColor(region) != UNDYED;
    }

    // What the renderer asks for: the block decides which region each model tintindex reads from
    public int getTint(int tintIndex)
    {
        BlockState state = getBlockState();
        int region = state.getBlock() instanceof IColorableBlock colorable
                ? colorable.getRegionForTintIndex(state, tintIndex)
                : tintIndex;
        int color = getColor(region);
        if (color != UNDYED) return color;
        return state.getBlock() instanceof IColorableBlock colorable ? colorable.getDefaultColor(region) : DEFAULT_TINT;
    }

    public void setColor(int region, int color)
    {
        if (!isValidRegion(region)) return;
        colors[region] = color;
        setChanged(); // marks the chunk as needing to be saved
        if (level != null)
        {
            // Sends getUpdatePacket() to nearby clients
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private boolean isValidRegion(int region)
    {
        return region >= 0 && region < colors.length;
    }

    // Vanilla calls this when the block state changes but the block stays the same, so this block entity is kept
    @Override
    public void setBlockState(BlockState newState)
    {
        BlockState oldState = getBlockState();
        super.setBlockState(newState);
        if (newState.getBlock() instanceof IColorableBlock colorable)
        {
            int added = colorable.getAddedRegion(oldState, newState);
            if (added != IColorableBlock.NO_REGION) addedRegion = added;
        }
    }

    // Returns the region added by the last placement, and clears it so it's only used once
    public int takeAddedRegion()
    {
        int added = addedRegion;
        addedRegion = IColorableBlock.NO_REGION;
        return added;
    }

    private static String colorKey(int region)
    {
        return region == 0 ? TAG_COLOR : TAG_COLOR + region;
    }

    // ---- Saving to disk ----

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);
        for (int region = 0; region < colors.length; region++)
        {
            if (colors[region] != UNDYED) output.putInt(colorKey(region), colors[region]);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);
        for (int region = 0; region < colors.length; region++)
        {
            colors[region] = input.getIntOr(colorKey(region), UNDYED);
        }

        // On the client, the block state didn't change, so force the chunk to re-mesh with the new colours
        if (level != null && level.isClientSide())
        {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // ---- Syncing to the client ----

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return saveCustomOnly(registries); // runs saveAdditional into a tag
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this); // uses getUpdateTag
    }

    // ---- Item <-> block (data components) ----

    // Item placed: if it carries a dyed_color, apply it to the placed region.
    // Never clears or overwrites an existing colour, so placing a second slab can't wipe the first half.
    // Item placed: if it carries a dyed_color, apply it to the region that was placed.
    // A second slab colours the half that was just added; a fresh block colours its own region.
    @Override
    protected void applyImplicitComponents(DataComponentGetter components)
    {
        super.applyImplicitComponents(components);
        DyedItemColor dyed = components.get(DataComponents.DYED_COLOR);
        if (dyed == null) return;

        BlockState state = getBlockState();
        int region;
        if (addedRegion != IColorableBlock.NO_REGION)
        {
            region = addedRegion;
        }
        else
        {
            region = state.getBlock() instanceof IColorableBlock colorable
                    ? colorable.getRegionForTintIndex(state, 0)
                    : 0;
        }
        if (isValidRegion(region)) colors[region] = dyed.rgb();
    }

    // Block turned into an item (creative pick-block): region 0's colour goes on the item
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components)
    {
        super.collectImplicitComponents(components);
        if (isDyed(0)) components.set(DataComponents.DYED_COLOR, new DyedItemColor(colors[0]));
    }
}