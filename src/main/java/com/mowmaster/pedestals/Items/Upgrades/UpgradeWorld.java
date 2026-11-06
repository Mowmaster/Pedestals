package com.mowmaster.pedestals.Items.Upgrades;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.PedestalTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// Pedestals' rules for which blocks upgrades may break and place. The breaking and placing itself (as a player, so
// other mods' protections and events apply) is MowLib's: MowLibBlockActions.breakBlock / placeBlock / isPlant.
public class UpgradeWorld
{
    // The rules every breaking upgrade shares: not air or unbreakable (bedrock), not tagged pedestals_cannot_break,
    // not the pedestal itself, no block entities unless the config allows them, and passing a NEUTRAL filter
    // (which sees the block as its item)
    public static boolean canBreak(ServerLevel level, BasePedestalBlockEntity pedestal, BlockPos pos, BlockState state)
    {
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0 || state.is(PedestalTags.CANNOT_BREAK)) return false;
        if (pos.equals(pedestal.getBlockPos())) return false;
        if (state.hasBlockEntity() && !Config.BREAK_BLOCK_ENTITIES.getAsBoolean()) return false;
        return pedestal.machineFilterAllows(new ItemStack(state.getBlock().asItem()));
    }

    // A block item that placers may place: not air, not tagged pedestals_cannot_place
    public static boolean canPlace(ItemStack stack)
    {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        Block block = blockItem.getBlock();
        BlockState state = block.defaultBlockState();
        return !state.isAir() && !state.is(PedestalTags.CANNOT_PLACE);
    }
}
