package com.mowmaster.pedestals.Blocks.Pedestal.GoblinStatuePedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Registry.DeferredBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;


public class GoblinStatuePedestalBlock extends BasePedestalBlock
{
    protected final VoxelShape UP;
    protected final VoxelShape DOWN;
    protected final VoxelShape NORTH;
    protected final VoxelShape EAST;
    protected final VoxelShape SOUTH;
    protected final VoxelShape WEST;

    public GoblinStatuePedestalBlock(Properties p_152915_)
    {
        super(p_152915_);
        this.UP = Shapes.or(Block.box(4.0D, 0.0D, 5.0D, 12.0D, 14.0D, 11.0D));
        this.NORTH = Shapes.or(Block.box(4.0D, 0.0D, 5.0D, 12.0D, 14.0D, 11.0D));
        this.DOWN = Shapes.or(Block.box(4.0D, 0.0D, 5.0D, 12.0D, 14.0D, 11.0D));
        this.SOUTH = Shapes.or(Block.box(4.0D, 0.0D, 5.0D, 12.0D, 14.0D, 11.0D));
        this.EAST = Shapes.or(Block.box(5.0D, 0.0D, 4.0D, 11.0D, 14.0D, 12.0D));
        this.WEST = Shapes.or(Block.box(5.0D, 0.0D, 4.0D, 11.0D, 14.0D, 12.0D));
    }

    @Override
    public VoxelShape getShape(BlockState p_152021_, BlockGetter p_152022_, BlockPos p_152023_, CollisionContext p_152024_) {
        Direction direction = p_152021_.getValue(FACING);
        return switch (direction) {
            case NORTH -> this.NORTH;
            case SOUTH -> this.SOUTH;
            case EAST -> this.EAST;
            case WEST -> this.WEST;
            case DOWN -> this.DOWN;
            default -> this.UP;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_152019_) {
        LevelAccessor levelaccessor = p_152019_.getLevel();
        BlockPos blockpos = p_152019_.getClickedPos();
        Direction direction = p_152019_.getNearestLookingDirection().getOpposite();
        BlockState blockstate = p_152019_.getLevel().getBlockState(p_152019_.getClickedPos().relative(direction));
        int getColor = MowLibColorReference.getColorFromStateInt(blockstate);
        //Lit and Filter can never be anything other then default when placing the block
        //Also copied the facing direction stuff from EndRodBlock
        return blockstate.is(this) &&
                blockstate.getValue(FACING) == direction
                ?
                MowLibColorReference.addColorToBlockState(this.defaultBlockState(),getColor).setValue(FACING, direction).setValue(WATERLOGGED, Boolean.valueOf(levelaccessor.getFluidState(blockpos).getType() == Fluids.WATER)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, 0)
                :
                MowLibColorReference.addColorToBlockState(this.defaultBlockState(),getColor).setValue(FACING, direction).setValue(WATERLOGGED, Boolean.valueOf(levelaccessor.getFluidState(blockpos).getType() == Fluids.WATER)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, 0);

    }

    @Override
    public BlockPos getPosOfBlockBelow(BlockState state, BlockPos posOfPedestal, int numBelow)
    {
        //Statues just use NESW for turn direction
        Direction enumfacing = (state.hasProperty(FACING))?(state.getValue(FACING)):(Direction.NORTH);
        return switch (enumfacing) {
            case UP -> posOfPedestal.offset(0, -numBelow, 0);
            case DOWN -> posOfPedestal.offset(0, -numBelow, 0);
            case NORTH -> posOfPedestal.offset(0, -numBelow, 0);
            case SOUTH -> posOfPedestal.offset(0, -numBelow, 0);
            case EAST -> posOfPedestal.offset(0, -numBelow, 0);
            case WEST -> posOfPedestal.offset(0, -numBelow, 0);
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return DeferredBlockEntityTypes.GOBLINSTATUE_PEDESTAL.get().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : (level0, pos, state0, blockEntity) -> ((GoblinStatuePedestalBlockEntity) blockEntity).tick();
    }
}
