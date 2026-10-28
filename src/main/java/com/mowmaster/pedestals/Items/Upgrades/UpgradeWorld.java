package com.mowmaster.pedestals.Items.Upgrades;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.PedestalTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.List;

// Breaking and placing blocks for upgrades, done the way a player would so other mods' protections and events
// apply (1.20 did the same steps by hand in each upgrade).
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

    // Breaks pos as player, with the tool in its main hand. Drops and experience appear at dropAt (the block itself,
    // or the pedestal for the quarry). Fires NeoForge's break event first, so protection mods can stop it.
    // Wears the tool down if it's the pedestal's own (see ItemUpgradeBase#playerWithTool). false if nothing broke.
    public static boolean breakBlock(ServerLevel level, Player player, BlockPos pos, BlockPos dropAt)
    {
        BlockState state = level.getBlockState(pos);
        if (CommonHooks.fireBlockBreak(level, GameType.SURVIVAL, player, pos, state).isCanceled()) return false;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack tool = player.getMainHandItem();
        boolean harvest = !state.requiresCorrectToolForDrops() || player.hasCorrectToolForDrops(state);
        List<ItemStack> drops = harvest ? Block.getDrops(state, level, pos, blockEntity, player, tool) : List.of();
        int experience = harvest ? state.getExpDrop(level, pos, blockEntity, player, tool) : 0;

        // Removes the block (and its block entity) the way a player breaking it does
        if (!state.onDestroyedByPlayer(level, pos, player, tool, harvest, level.getFluidState(pos))) return false;
        state.getBlock().destroy(level, pos, state);
        if (!tool.isEmpty()) tool.mineBlock(level, state, pos, player);

        drops.forEach(drop -> Block.popResource(level, dropAt, drop));
        if (experience > 0) ExperienceOrb.award(level, Vec3.atCenterOf(dropAt), experience);
        return true;
    }

    // Plants and saplings are the planter's job, so the placer and filler leave them alone (1.20 behaviour)
    public static boolean isPlant(Block block)
    {
        return block instanceof VegetationBlock || block instanceof ChorusFlowerBlock || block instanceof GrowingPlantBlock;
    }

    // A block item that placers may place: not air, not tagged pedestals_cannot_place
    public static boolean canPlace(ItemStack stack)
    {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        Block block = blockItem.getBlock();
        BlockState state = block.defaultBlockState();
        return !state.isAir() && !state.is(PedestalTags.CANNOT_PLACE);
    }

    // Places one of stack at pos as player (the item's own placing code runs, with NeoForge's place event).
    // pos must be air or replaceable. Returns true if it was placed; one is then gone from the stack.
    public static boolean placeBlock(ServerLevel level, Player player, ItemStack stack, BlockPos pos)
    {
        ItemStack held = stack.copyWithCount(1);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        try
        {
            // Clicking the target block itself: placing into an air/replaceable block puts the new block right there
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            InteractionResult result = CommonHooks.onPlaceItemIntoWorld(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            return result.consumesAction() && held.isEmpty();
        }
        finally
        {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }
}
