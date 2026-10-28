package com.mowmaster.pedestals.Blocks.Pedestal;

import com.mowmaster.pedestals.TintHandler.IColorableBlock;
import com.mowmaster.pedestals.TintHandler.ITintHolder;
import com.mowmaster.pedestals.TintHandler.libPixelBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// The pedestal: holds items, shows them on top, and (later) takes upgrades and augments.
// Ported from 1.20: the colour now lives in the block entity (ITintHolder) instead of three blockstate properties,
// so it can be any RGB colour and dyes mix like CrystalBlocks' blocks.
// Controls (as in 1.20): right-click inserts the held item, left-click takes one out (sneak: the whole stack).
// A dye or glowstone in the OFFHAND is applied on right-click; glowstone in the offhand + left-click takes the light back.
public class BasePedestalBlock extends Block implements IColorableBlock, SimpleWaterloggedBlock
{
    // 0 = no filter, 1 = whitelist, 2 = blacklist. Filters aren't ported yet, so this stays 0 for now.
    public static final IntegerProperty FILTER_STATUS = IntegerProperty.create("filter_status", 0, 2);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    // The way the top of the pedestal points (up when placed on the ground)
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    // The model's boxes, standing upright (facing up). Same numbers as the 1.20 shapes.
    private static final List<libPixelBox> UNLIT_BOXES = List.of(
            new libPixelBox(3, 0, 3, 13, 2, 13),     // base
            new libPixelBox(5, 2, 5, 11, 10, 11),    // pillar
            new libPixelBox(4, 10, 4, 12, 12, 12));  // top
    private static final List<libPixelBox> LIT_BOXES = List.of(
            new libPixelBox(3, 0, 3, 13, 2, 13),
            new libPixelBox(4, 2, 4, 12, 3, 12),     // glowstone ring
            new libPixelBox(5, 3, 5, 11, 4, 11),
            new libPixelBox(4.5, 4, 4.5, 11.5, 5, 11.5),
            new libPixelBox(5, 5, 5, 11, 10, 11),
            new libPixelBox(4, 10, 4, 12, 12, 12));

    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public BasePedestalBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(LIT, false)
                .setValue(FILTER_STATUS, 0)
                .setValue(WATERLOGGED, false));

        // Work every shape out once, rotated the same way the blockstate file rotates the model
        for (BlockState state : stateDefinition.getPossibleStates())
        {
            List<libPixelBox> boxes = state.getValue(LIT) ? LIT_BOXES : UNLIT_BOXES;
            shapes.put(state, libPixelBox.union(boxes.stream().map(box -> place(box, state.getValue(FACING))).toList()));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, LIT, FILTER_STATUS, WATERLOGGED);
    }

    // ---- Rotations, matching blockstates/block_pedestal.json: up as modelled, down = x 180, sideways = x 90 then y ----

    public static int xQuarterTurns(Direction facing)
    {
        return switch (facing)
        {
            case UP -> 0;
            case DOWN -> 2;
            default -> 1;
        };
    }

    public static int yQuarterTurns(Direction facing)
    {
        return facing.getAxis().isVertical() ? 0 : libPixelBox.quarterTurns(facing);
    }

    private static libPixelBox place(libPixelBox box, Direction facing)
    {
        return box.rotateX(xQuarterTurns(facing)).rotateY(yQuarterTurns(facing));
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return shapes.get(state);
    }

    // ---- Colour: one region, coloured by the pedestal's own block entity ----

    @Override
    public boolean usesTintBlockEntity()
    {
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BasePedestalBlockEntity(pos, state);
    }

    // ---- Placement ----

    // Like the end rod: the top points away from the face you clicked, and placing onto the top of another
    // pedestal that points the same way flips it, so two pedestals can face each other.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Direction direction = context.getClickedFace();
        BlockState clicked = context.getLevel().getBlockState(context.getClickedPos().relative(direction.getOpposite()));
        Direction facing = clicked.is(this) && clicked.getValue(FACING) == direction ? direction.getOpposite() : direction;
        boolean inWater = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return defaultBlockState().setValue(FACING, facing).setValue(WATERLOGGED, inWater);
    }

    // An undyed pedestal item placed on a pedestal takes that pedestal's colour (as in 1.20).
    // A dyed item keeps its own colour: the block entity reads it from the item's dyed_color.
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide() || stack.has(DataComponents.DYED_COLOR)) return;

        BlockPos support = pos.relative(state.getValue(FACING).getOpposite());
        if (level.getBlockEntity(support) instanceof BasePedestalBlockEntity supportPedestal
                && supportPedestal.isDyed(0)
                && level.getBlockEntity(pos) instanceof ITintHolder placed)
        {
            placed.setColor(0, supportPedestal.getColor(0));
        }
    }

    // ---- Right-click ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        // Everything is handled on the main hand's turn; the offhand only holds what gets applied
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack offhand = player.getOffhandItem();

        // Dye in the offhand: colour the pedestal (mixes like leather armour, same as CrystalBlocks)
        if (IColorableBlock.dyeColorOf(offhand) != null)
        {
            return tryDyeBlock(offhand, state, level, pos, player, hitResult);
        }

        // Glowstone in the offhand: light the pedestal
        if (offhand.is(Items.GLOWSTONE) && !state.getValue(LIT))
        {
            if (!level.isClientSide())
            {
                level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
                offhand.consume(1, player); // does nothing in creative
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.SUCCESS;
        }

        // Empty hand: let useWithoutItem show what's inside
        if (stack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;

        // Anything else in the main hand goes into the pedestal
        if (level.getBlockEntity(pos) instanceof BasePedestalBlockEntity pedestal)
        {
            if (!level.isClientSide())
            {
                int inserted = pedestal.insertItem(stack, false);
                if (inserted == 0) return InteractionResult.FAIL;
                stack.shrink(inserted);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // Empty main hand: show the stored items above the hotbar
    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BasePedestalBlockEntity pedestal && pedestal.hasItem())
        {
            player.sendOverlayMessage(pedestal.describeContents());
        }
        return InteractionResult.SUCCESS;
    }

    // ---- Left-click ----

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player)
    {
        if (!level.isClientSide() && !(player instanceof FakePlayer)) leftClick(state, level, pos, player);
        super.attack(state, level, pos, player);
    }

    private static void leftClick(BlockState state, Level level, BlockPos pos, Player player)
    {
        // Glowstone in the offhand: take the light back out
        if (player.getOffhandItem().is(Items.GLOWSTONE) && state.getValue(LIT))
        {
            level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
            giveToPlayer(player, new ItemStack(Items.GLOWSTONE));
            return;
        }

        if (level.getBlockEntity(pos) instanceof BasePedestalBlockEntity pedestal && pedestal.hasItem())
        {
            ItemStack taken = pedestal.removeItem(player.isShiftKeyDown() ? Integer.MAX_VALUE : 1);
            giveToPlayer(player, taken);
        }
    }

    // Into the inventory, or dropped at the player's feet if it's full. The client is sent the inventory change.
    private static void giveToPlayer(Player player, ItemStack stack)
    {
        if (!stack.isEmpty()) player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
    }

    // Creative players don't break pedestals by left-clicking (that takes items out instead, as in 1.20);
    // sneak to break one. Fake players (other mods' machines) can't break them at all.
    // Vanilla skips attack() in creative and goes straight here, so the left-click action runs here instead.
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack,
                                       boolean willHarvest, FluidState fluid)
    {
        if (player instanceof FakePlayer) return false;
        if (player.isCreative() && !player.isShiftKeyDown())
        {
            if (!level.isClientSide()) leftClick(state, level, pos, player);
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
    }

    // ---- Comparators: how full the pedestal is ----

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction)
    {
        return level.getBlockEntity(pos) instanceof BasePedestalBlockEntity pedestal ? pedestal.getRedstoneSignal() : 0;
    }

    // ---- Waterlogging ----

    @Override
    protected @NotNull FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                              Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random)
    {
        // Lets the water around a waterlogged pedestal keep flowing
        if (state.getValue(WATERLOGGED)) ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    // ---- Rotation (structures, other mods' rotation tools) ----

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation)
    {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
