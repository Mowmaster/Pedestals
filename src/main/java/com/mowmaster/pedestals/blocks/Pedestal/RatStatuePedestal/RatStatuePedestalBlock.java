package com.mowmaster.pedestals.Blocks.Pedestal.RatStatuePedestal;

import com.google.common.collect.Maps;
import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlock;
import com.mowmaster.mowlib.Items.ColorApplicator;
import com.mowmaster.mowlib.Items.Filters.IPedestalFilter;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Augments.AugmentTieredCapacity;
import com.mowmaster.pedestals.Items.Augments.AugmentTieredRange;
import com.mowmaster.pedestals.Items.Augments.AugmentTieredSpeed;
import com.mowmaster.pedestals.Items.Augments.AugmentTieredStorage;
import com.mowmaster.pedestals.Items.Tools.IPedestalTool;
import com.mowmaster.pedestals.Items.Tools.Linking.LinkingTool;
import com.mowmaster.pedestals.Items.Tools.Linking.LinkingToolBackwards;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.IPedestalUpgrade;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.WorkCards.IPedestalWorkCard;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardPedestals;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.Registry.DeferredBlockEntityTypes;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


public class RatStatuePedestalBlock extends BasePedestalBlock
{
    protected final VoxelShape UP;
    protected final VoxelShape DOWN;
    protected final VoxelShape NORTH;
    protected final VoxelShape EAST;
    protected final VoxelShape SOUTH;
    protected final VoxelShape WEST;

    public RatStatuePedestalBlock(Properties p_152915_)
    {
        super(p_152915_);
        this.UP = Shapes.or(Block.box(4.5D, 0.0D, 6.0D, 11.5D, 4.0D, 10.0D),
                Block.box(5.0D, 4.0D, 5.0D, 11.0D, 12.0D, 11.0D),
                Block.box(5.0D, 12.0D, 3.0D, 11.0D, 15.0D, 8.5D));

        this.NORTH = Shapes.or(Block.box(4.5D, 0.0D, 6.0D, 11.5D, 4.0D, 10.0D),
                Block.box(5.0D, 4.0D, 5.0D, 11.0D, 12.0D, 11.0D),
                Block.box(5.0D, 12.0D, 3.0D, 11.0D, 15.0D, 8.5D));
        this.DOWN = Shapes.or(Block.box(4.5D, 0.0D, 6.0D, 11.5D, 4.0D, 10.0D),
                Block.box(5.0D, 4.0D, 5.0D, 11.0D, 12.0D, 11.0D),
                Block.box(5.0D, 12.0D, 7.5D, 11.0D, 15.0D, 13.0D));
        this.SOUTH = Shapes.or(Block.box(4.5D, 0.0D, 6.0D, 11.5D, 4.0D, 10.0D),
                Block.box(5.0D, 4.0D, 5.0D, 11.0D, 12.0D, 11.0D),
                Block.box(5.0D, 12.0D, 7.5D, 11.0D, 15.0D, 13.0D));

        this.EAST = Shapes.or(Block.box(6.0D, 0.0D, 4.5D, 10.0D, 4.0D, 11.5D),
                Block.box(5.0D, 4.0D, 5.0D, 11.0D, 12.0D, 11.0D),
                Block.box(7.5D, 12.0D, 5.0D, 13.0D, 15.0D, 11.0D));
        this.WEST = Shapes.or(Block.box(6.0D, 0.0D, 4.5D, 10.0D, 4.0D, 11.5D),
                Block.box(5.0D, 4.0D, 5.0D, 11.0D, 12.0D, 11.0D),
                Block.box(3.0D, 12.0D, 5.0D, 8.5D, 15.0D, 11.0D));
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
        return DeferredBlockEntityTypes.RATSTATUE_PEDESTAL.get().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : (level0, pos, state0, blockEntity) -> ((RatStatuePedestalBlockEntity) blockEntity).tick();
    }
}
