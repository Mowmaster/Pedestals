package com.mowmaster.pedestals.Blocks.Pedestal;

import com.google.common.collect.Maps;
import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlock;
import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import com.mowmaster.mowlib.BlockEntities.MowLibBaseFilterableBlock;
import com.mowmaster.mowlib.BlockEntities.MowLibBaseFilterableBlockEntity;
import com.mowmaster.mowlib.Items.ColorApplicator;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardBE;
import com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.mowlib.api.DefineLocations.IWorkCard;
import com.mowmaster.mowlib.api.Tools.IMowLibTool;
import com.mowmaster.mowlib.api.TransportAndStorage.IFilterItem;
import com.mowmaster.pedestals.Items.Augments.AugmentTieredCapacity;
import com.mowmaster.pedestals.Items.Augments.AugmentTieredRange;
import com.mowmaster.pedestals.Items.Augments.AugmentTieredSpeed;
import com.mowmaster.pedestals.Items.Augments.AugmentTieredStorage;
import com.mowmaster.pedestals.Items.Tools.Linking.LinkingTool;
import com.mowmaster.pedestals.Items.Tools.Linking.LinkingToolBackwards;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.IPedestalUpgrade;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.Registry.DeferredBlockEntityTypes;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Map;

import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.getPosBelowBlockEntity;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


public class BasePedestalBlock extends MowLibBaseBlock implements SimpleWaterloggedBlock, EntityBlock
{
    public static final IntegerProperty FILTER_STATUS = IntegerProperty.create("filter_status", 0, 2);
    public static final BooleanProperty WORKCARD_STATUS = BooleanProperty.create("workcard_status");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final IntegerProperty REDSTONE_STATUS = IntegerProperty.create("redstone_status", 0, 15);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    protected final VoxelShape CUP;
    protected final VoxelShape CDOWN;
    protected final VoxelShape CNORTH;
    protected final VoxelShape CSOUTH;
    protected final VoxelShape CEAST;
    protected final VoxelShape CWEST;
    protected final VoxelShape LCUP;
    protected final VoxelShape LCDOWN;
    protected final VoxelShape LCNORTH;
    protected final VoxelShape LCSOUTH;
    protected final VoxelShape LCEAST;
    protected final VoxelShape LCWEST;

    public BasePedestalBlock(BlockBehaviour.Properties p_152915_)
    {
        super(p_152915_);
        this.registerDefaultState(MowLibColorReference.addColorToBlockState(this.defaultBlockState(),MowLibColorReference.DEFAULTCOLOR).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.UP).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, 0));
        this.CUP = Shapes.or(Block.box(3.0D, 0.0D, 3.0D, 13.0D, 2.0D, 13.0D),
                Block.box(5.0D, 2.0D, 5.0D, 11.0D, 10.0D, 11.0D),
                Block.box(4.0D, 10.0D, 4.0D, 12.0D, 12.0D, 12.0D));
        /*this.CDOWN = Shapes.or(Block.box(3.0D, 14.0D, 3.0D, 13.0D, 16.0D, 13.0D),
                Block.box(5.0D, 14.0D, 5.0D, 11.0D, 6.0D, 11.0D),
                Block.box(4.0D, 6.0D, 4.0D, 12.0D, 4.0D, 12.0D));*/
        this.CDOWN = Shapes.or(Block.box(3.0D, 14.0D, 3.0D, 13.0D, 16.0D, 13.0D),
                Block.box(5.0D, 6.0D, 5.0D, 11.0D, 14.0D, 11.0D),
                Block.box(4.0D, 4.0D, 4.0D, 12.0D, 6.0D, 12.0D));
        //height goes in the -z direction
        this.CNORTH = Shapes.or(Block.box(3.0D, 3.0D, 14.0D, 13.0D, 13.0D, 16.0D),
                Block.box(5.0D, 5.0D, 6.0D, 11.0D, 11.0D, 14.0D),
                Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 6.0D));
        /*this.CSOUTH = Shapes.or(Block.box(3.0D, 3.0D, 2.0D, 13.0D, 13.0D, 0.0D),
                Block.box(5.0D, 5.0D, 10.0D, 11.0D, 11.0D, 2.0D),
                Block.box(4.0D, 4.0D, 12.0D, 12.0D, 12.0D, 10.0D));*/
        this.CSOUTH = Shapes.or(Block.box(3.0D, 3.0D, 0.0D, 13.0D, 13.0D, 2.0D),
                Block.box(5.0D, 5.0D, 2.0D, 11.0D, 11.0D, 10.0D),
                Block.box(4.0D, 4.0D, 10.0D, 12.0D, 12.0D, 12.0D));
        //height goes in the +x direction
        /*this.CEAST = Shapes.or(Block.box(2.0D, 3.0D, 3.0D, 0.0D, 13.0D, 13.0D),
                Block.box(2.0D, 5.0D, 5.0D, 10.0D, 11.0D, 11.0D),
                Block.box(10.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D));*/
        this.CEAST = Shapes.or(Block.box(0.0D, 3.0D, 3.0D, 2.0D, 13.0D, 13.0D),
                Block.box(2.0D, 5.0D, 5.0D, 10.0D, 11.0D, 11.0D),
                Block.box(10.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D));
        /*this.CWEST = Shapes.or(Block.box(14.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D),
                Block.box(14.0D, 5.0D, 5.0D, 6.0D, 11.0D, 11.0D),
                Block.box(4.0D, 4.0D, 4.0D, 6.0D, 12.0D, 12.0D));*/
        this.CWEST = Shapes.or(Block.box(14.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D),
                Block.box(6.0D, 5.0D, 5.0D, 14.0D, 11.0D, 11.0D),
                Block.box(4.0D, 4.0D, 4.0D, 6.0D, 12.0D, 12.0D));

        this.LCUP = Shapes.or(Block.box(3.0D, 0.0D, 3.0D, 13.0D, 2.0D, 13.0D),
                Block.box(4.0D, 2.0D, 4.0D, 12.0D, 3.0D, 12.0D),
                Block.box(5.0D, 3.0D, 5.0D, 11.0D, 4.0D, 11.0D),
                Block.box(4.5D, 4.0D, 4.5D, 11.5D, 5.0D, 11.5D),
                Block.box(5.0D, 5.0D, 5.0D, 11.0D, 10.0D, 11.0D),
                Block.box(4.0D, 10.0D, 4.0D, 12.0D, 12.0D, 12.0D));
        /*this.LCDOWN = Shapes.or(Block.box(3.0D, 14.0D, 3.0D, 13.0D, 16.0D, 13.0D),
                Block.box(4.0D, 13.0D, 4.0D, 12.0D, 14.0D, 12.0D),
                Block.box(5.0D, 13.0D, 5.0D, 11.0D, 12.0D, 11.0D),
                Block.box(4.5D, 12.0D, 4.5D, 11.5D, 11.0D, 11.5D),
                Block.box(5.0D, 11.0D, 5.0D, 11.0D, 6.0D, 11.0D),
                Block.box(4.0D, 6.0D, 4.0D, 12.0D, 4.0D, 12.0D));*/
        this.LCDOWN = Shapes.or(Block.box(3.0D, 14.0D, 3.0D, 13.0D, 16.0D, 13.0D),
                Block.box(4.0D, 13.0D, 4.0D, 12.0D, 14.0D, 12.0D),
                Block.box(5.0D, 12.0D, 5.0D, 11.0D, 13.0D, 11.0D),
                Block.box(4.5D, 11.0D, 4.5D, 11.5D, 12.0D, 11.5D),
                Block.box(5.0D, 6.0D, 5.0D, 11.0D, 11.0D, 11.0D),
                Block.box(4.0D, 4.0D, 4.0D, 12.0D, 6.0D, 12.0D));
        //height goes in the -z direction
        this.LCNORTH = Shapes.or(Block.box(3.0D, 3.0D, 14.0D, 13.0D, 13.0D, 16.0D),
                Block.box(4.0D, 4.0D, 13.0D, 12.0D, 12.0D, 14.0D),
                Block.box(5.0D, 5.0D, 12.0D, 11.0D, 11.0D, 13.0D),
                Block.box(4.5D, 4.5D, 11.0D, 11.5D, 11.5D, 12.0D),
                Block.box(5.0D, 5.0D, 6.0D, 11.0D, 11.0D, 11.0D),
                Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 6.0D));
        /*this.LCSOUTH = Shapes.or(Block.box(3.0D, 3.0D, 2.0D, 13.0D, 13.0D, 0.0D),
                Block.box(4.0D, 4.0D, 3.0D, 12.0D, 12.0D, 2.0D),
                Block.box(5.0D, 5.0D, 4.0D, 11.0D, 11.0D, 3.0D),
                Block.box(4.5D, 4.5D, 5.0D, 11.5D, 11.5D, 4.0D),
                Block.box(5.0D, 5.0D, 10.0D, 11.0D, 11.0D, 5.0D),
                Block.box(4.0D, 4.0D, 12.0D, 12.0D, 12.0D, 10.0D));*/
        this.LCSOUTH = Shapes.or(Block.box(3.0D, 3.0D, 0.0D, 13.0D, 13.0D, 2.0D),
                Block.box(4.0D, 4.0D, 2.0D, 12.0D, 12.0D, 3.0D),
                Block.box(5.0D, 5.0D, 3.0D, 11.0D, 11.0D, 4.0D),
                Block.box(4.5D, 4.5D, 4.0D, 11.5D, 11.5D, 5.0D),
                Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 10.0D),
                Block.box(4.0D, 4.0D, 10.0D, 12.0D, 12.0D, 12.0D));
        //height goes in the +x direction
        /*this.LCEAST = Shapes.or(Block.box(2.0D, 3.0D, 3.0D, 0.0D, 13.0D, 13.0D),
                Block.box(2.0D, 4.0D, 4.0D, 3.0D, 12.0D, 12.0D),
                Block.box(3.0D, 5.0D, 5.0D, 4.0D, 11.0D, 11.0D),
                Block.box(4.0D, 4.5D, 4.5D, 5.0D, 11.5D, 11.5D),
                Block.box(5.0D, 5.0D, 5.0D, 10.0D, 11.0D, 11.0D),
                Block.box(10.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D));*/
        this.LCEAST = Shapes.or(Block.box(0.0D, 3.0D, 3.0D, 2.0D, 13.0D, 13.0D),
                Block.box(2.0D, 4.0D, 4.0D, 3.0D, 12.0D, 12.0D),
                Block.box(3.0D, 5.0D, 5.0D, 4.0D, 11.0D, 11.0D),
                Block.box(4.0D, 4.5D, 4.5D, 5.0D, 11.5D, 11.5D),
                Block.box(5.0D, 5.0D, 5.0D, 10.0D, 11.0D, 11.0D),
                Block.box(10.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D));
        this.LCWEST = Shapes.or(Block.box(14.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D),
                Block.box(13.0D, 4.0D, 4.0D, 14.0D, 12.0D, 12.0D),
                Block.box(12.0D, 5.0D, 5.0D, 13.0D, 11.0D, 11.0D),
                Block.box(11.0D, 4.5D, 4.5D, 12.0D, 11.5D, 11.5D),
                Block.box(6.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D),
                Block.box(4.0D, 4.0D, 4.0D, 6.0D, 12.0D, 12.0D));
    }

    public VoxelShape getShape(BlockState p_152021_, BlockGetter p_152022_, BlockPos p_152023_, CollisionContext p_152024_) {
        Direction direction = p_152021_.getValue(FACING);
        switch(direction) {
            case NORTH:
                return (p_152021_.getValue(LIT)) ? (this.LCNORTH) : (this.CNORTH);
            case SOUTH:
                return (p_152021_.getValue(LIT)) ? (this.LCSOUTH) : (this.CSOUTH);
            case EAST:
                return (p_152021_.getValue(LIT)) ? (this.LCEAST) : (this.CEAST);
            case WEST:
                return (p_152021_.getValue(LIT)) ? (this.LCWEST) : (this.CWEST);
            case DOWN:
                return (p_152021_.getValue(LIT)) ? (this.LCDOWN) : (this.CDOWN);
            case UP:
            default:
                return (p_152021_.getValue(LIT)) ? (this.LCUP) : (this.CUP);
        }
    }

    public BlockState updateShape(BlockState p_152036_, Direction p_152037_, BlockState p_152038_, LevelAccessor p_152039_, BlockPos p_152040_, BlockPos p_152041_) {
        if (p_152036_.getValue(WATERLOGGED)) {
            p_152039_.getFluidTicks();
            //.scheduleTick(p_152040_, Fluids.WATER, Fluids.WATER.getTickDelay(p_152039_))
        }

        return p_152037_ == p_152036_.getValue(FACING).getOpposite() && !p_152036_.canSurvive(p_152039_, p_152040_) ? Blocks.AIR.defaultBlockState() : super.updateShape(p_152036_, p_152037_, p_152038_, p_152039_, p_152040_, p_152041_);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_152019_) {
        LevelAccessor levelaccessor = p_152019_.getLevel();
        BlockPos blockpos = p_152019_.getClickedPos();
        Direction direction = p_152019_.getClickedFace();
        BlockState blockstate = p_152019_.getLevel().getBlockState(p_152019_.getClickedPos().relative(direction.getOpposite()));
        int getColor = MowLibColorReference.getColorFromStateInt(blockstate);
        //Lit and Filter can never be anything other then default when placing the block
        //Also copied the facing direction stuff from EndRodBlock
        return blockstate.is(this) &&
                blockstate.getValue(FACING) == direction
                ?
                MowLibColorReference.addColorToBlockState(this.defaultBlockState(),getColor).setValue(FACING, direction.getOpposite()).setValue(WATERLOGGED, Boolean.valueOf(levelaccessor.getFluidState(blockpos).getType() == Fluids.WATER)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, 0)
                :
                MowLibColorReference.addColorToBlockState(this.defaultBlockState(),getColor).setValue(FACING, direction).setValue(WATERLOGGED, Boolean.valueOf(levelaccessor.getFluidState(blockpos).getType() == Fluids.WATER)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, 0);

    }

    @Override
    public void setPlacedBy(Level p_49847_, BlockPos p_49848_, BlockState p_49849_, @org.jetbrains.annotations.Nullable LivingEntity p_49850_, ItemStack p_49851_) {
        if(!p_49847_.isClientSide())
        {
            if(p_49850_ instanceof Player player)
            {
                ItemStack offhandItemStack = player.getOffhandItem();
                if(offhandItemStack.hasTag() && offhandItemStack.isEnchanted())
                {


                    if(offhandItemStack.is(DeferredRegisterItems.TOOL_LINKINGTOOL.get()))
                    {
                        //Checks if clicked blocks is a Pedestal
                        if(p_49847_.getBlockState(p_49848_).getBlock() instanceof BasePedestalBlock)
                        {
                            // Checks Tile at location to make sure its a TilePedestal
                            if (p_49847_.getBlockEntity(p_49848_) instanceof BasePedestalBlockEntity sendingPedestal) {
                                LinkingTool tool = (LinkingTool) offhandItemStack.getItem();
                                BlockPos receivingPos = tool.getStoredPosition(offhandItemStack);
                                sendingPedestal.attemptUpdateLink(receivingPos, player, MODID + ".tool_link_success_linkingtool");
                            }
                        }
                    }
                    else if(offhandItemStack.is(DeferredRegisterItems.TOOL_LINKINGTOOLBACKWARDS.get()))
                    {
                        //Checks if clicked blocks is a Pedestal
                        if(p_49847_.getBlockState(p_49848_).getBlock() instanceof BasePedestalBlock)
                        {
                            LinkingToolBackwards tool = (LinkingToolBackwards)offhandItemStack.getItem();
                            BlockPos sendingPos = tool.getStoredPosition(offhandItemStack);

                            //Checks Tile at location to make sure its a TilePedestal
                            if (p_49847_.getBlockEntity(sendingPos) instanceof BasePedestalBlockEntity sendingPedestal) {
                                BlockPos receivingPos = p_49848_;
                                sendingPedestal.attemptUpdateLink(receivingPos, player, MODID + ".tool_link_success_backwardslinkingtool");
                            }
                        }
                    }
                }
            }
        }

        super.setPlacedBy(p_49847_, p_49848_, p_49849_, p_49850_, p_49851_);
    }

    public BlockState rotate(BlockState p_152033_, Rotation p_152034_) {
        return p_152033_.setValue(FACING, p_152034_.rotate(p_152033_.getValue(FACING)));
    }

    public BlockState mirror(BlockState p_152030_, Mirror p_152031_) {
        return p_152030_.rotate(p_152031_.getRotation(p_152030_.getValue(FACING)));
    }

    public FluidState getFluidState(BlockState p_152045_) {
        return p_152045_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_152045_);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_152043_) {
        p_152043_.add(WATERLOGGED, FACING, COLOR_RED, COLOR_GREEN, COLOR_BLUE, LIT, FILTER_STATUS);
    }

    public PushReaction getPistonPushReaction(BlockState p_152733_) {
        return PushReaction.IGNORE;
    }

    //Left Click Action
    @Override
    public void attack(BlockState p_60499_, Level p_60500_, BlockPos p_60501_, Player p_60502_) {
        super.attack(p_60499_, p_60500_, p_60501_, p_60502_);

        if(!p_60500_.isClientSide())
        {
            if(p_60502_ instanceof FakePlayer){ super.attack(p_60499_, p_60500_, p_60501_, p_60502_); }

            BlockEntity blockEntity = p_60500_.getBlockEntity(p_60501_);
            if(blockEntity instanceof BasePedestalBlockEntity)
            {
                BasePedestalBlockEntity pedestal = ((BasePedestalBlockEntity)blockEntity);
                ItemStack itemInHand = p_60502_.getMainHandItem();
                ItemStack itemInOffHand = p_60502_.getOffhandItem();

                if(pedestal.hasFilter() && itemInOffHand.is(com.mowmaster.mowlib.Registry.DeferredRegisterItems.TOOL_FILTERTOOL.get()))
                {
                    ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeFilter(null));
                    pedestal.actionOnFilterRemovedFromBlockEntity(1);
                }
                else if(pedestal.hasLight() && itemInOffHand.is(Items.GLOWSTONE))
                {
                    ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeLight(null));
                    pedestal.actionOnLightRemovedFromBlockEntity(1);
                }
                else if(pedestal.hasRedstone() && itemInOffHand.is(Items.REDSTONE))
                {
                    if(p_60502_.isShiftKeyDown())
                    {
                        ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeAllRedstone(null));
                    }
                    else
                    {
                        ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeRedstone(null));
                    }

                    pedestal.actionOnRedstoneRemovedFromBlockEntity(1);
                }
                else if(pedestal.hasWorkCard() && itemInOffHand.is(com.mowmaster.mowlib.Registry.DeferredRegisterItems.TOOL_WORKTOOL.get()))
                {
                    pedestal.actionOnWorkCardRemovedFromBlockEntity(1);
                    ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeWorkCard(null));
                }
                else if(pedestal.hasCoin() && itemInOffHand.is(DeferredRegisterItems.TOOL_UPGRADETOOL.get()))
                {
                    //Method for upgrades to do things before removal
                    pedestal.actionOnRemovedFromPedestal(1);
                    ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeCoin());
                }
                else if(pedestal.hasTool() && itemInOffHand.is(DeferredRegisterItems.TOOL_TOOLSWAPPER.get()))
                {
                    pedestal.actionOnNeighborBelowChange(getPosBelowBlockEntity(p_60499_, p_60501_, 1));
                    pedestal.updatePedestalPlayer(pedestal);
                    ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeAllTool());
                }

                else if(pedestal.hasRRobin() && itemInOffHand.is(DeferredRegisterItems.TOOL_AUGMENTS_ROUNDROBIN.get()))
                {
                    ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeRRobin());
                }
                else if(pedestal.hasRenderAugment() && itemInOffHand.is(DeferredRegisterItems.TOOL_AUGMENTS_DIFFUSER.get()))
                {
                    ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeRenderAugment());
                }
                else if(pedestal.hasNoCollide() && itemInOffHand.is(DeferredRegisterItems.TOOL_AUGMENTS_COLLIDE.get()))
                {
                    ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeNoCollide());
                }
                else if(pedestal.hasSpeed() && itemInOffHand.is(DeferredRegisterItems.TOOL_AUGMENTS_SPEED.get()))
                {
                    if(p_60502_.isShiftKeyDown()){ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeAllSpeed());}
                    else ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeSpeed(1));
                }
                else if(pedestal.hasCapacity() && itemInOffHand.is(DeferredRegisterItems.TOOL_AUGMENTS_CAPACITY.get()))
                {
                    if(p_60502_.isShiftKeyDown()){ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeAllCapacity());}
                    else ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeCapacity(1));
                }
                else if(pedestal.hasStorage() && itemInOffHand.is(DeferredRegisterItems.TOOL_AUGMENTS_STORAGE.get()))
                {
                    if(p_60502_.isShiftKeyDown()){ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeAllStorage());}
                    else ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeStorage(1));
                }
                else if(pedestal.hasRange() && itemInOffHand.is(DeferredRegisterItems.TOOL_AUGMENTS_RANGE.get()))
                {
                    if(p_60502_.isShiftKeyDown()){ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeAllRange());}
                    else ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeRange(1));
                }
                else if(pedestal.hasItemFirst())
                {
                    if(p_60502_.isShiftKeyDown())
                    {
                        ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeItem(false));
                    }
                    else
                    {
                        ItemHandlerHelper.giveItemToPlayer(p_60502_,pedestal.removeItem(1,false));
                    }
                }
            }
        }
    }


    @Override
    public InteractionResult use(BlockState p_60503_, Level p_60504_, BlockPos p_60505_, Player p_60506_, InteractionHand p_60507_, BlockHitResult p_60508_) {

        super.use(p_60503_, p_60504_, p_60505_, p_60506_, p_60507_, p_60508_);

        ItemStack itemInHand = p_60506_.getMainHandItem();
        if(p_60504_.isClientSide())
        {
            if(itemInHand.getItem() instanceof WorkCardBE)
            {
                return InteractionResult.FAIL;
            }
        }
        else
        {
            BlockEntity blockEntity = p_60504_.getBlockEntity(p_60505_);
            if(blockEntity instanceof BasePedestalBlockEntity pedestal)
            {
                //ItemStack itemInHand = p_60506_.getMainHandItem();
                ItemStack itemInOffHand = p_60506_.getOffhandItem();

                int getColor;
                int currentColor;
                Component sameColor;
                BlockState newState;
                List<Item> DYES = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation("forge", "dyes"))).stream().toList();

                if(itemInHand.getItem() instanceof IMowLibTool)
                {
                    if(itemInHand.getItem().equals(DeferredRegisterItems.TOOL_LINKINGTOOL.get()) || itemInHand.getItem().equals(DeferredRegisterItems.TOOL_LINKINGTOOLBACKWARDS.get())){
                        boolean getCurrentRender = pedestal.getRenderRange();
                        pedestal.setRenderRange(!getCurrentRender);

                        MutableComponent render_on = Component.translatable(MODID + ".linkingtool.pedestal_render_on");
                        MutableComponent render_off = Component.translatable(MODID + ".linkingtool.pedestal_render_off");
                        MutableComponent render = (!getCurrentRender)?(render_on):(render_off);
                        ChatFormatting color = (!getCurrentRender)?(ChatFormatting.RED):(ChatFormatting.DARK_RED);
                        render.withStyle(color);

                        p_60506_.displayClientMessage(render, true);
                    }
                    if(itemInHand.getItem().equals(DeferredRegisterItems.TOOL_UPGRADETOOL.get())){
                        if(pedestal.hasCoin())
                        {
                            if(pedestal.getCoinOnPedestal().getItem() instanceof ItemUpgradeBase upgrade)
                            {
                                int value = upgrade.getUpgradeWorkRange(pedestal.getCoinOnPedestal());
                                if(value>0)
                                {
                                    boolean getCurrentRenderUpgrade = pedestal.getRenderRangeUpgrade();
                                    pedestal.setRenderRangeUpgrade(!getCurrentRenderUpgrade);

                                    MutableComponent render_on = Component.translatable(MODID + ".upgradetool.pedestal_render_on");
                                    MutableComponent render_off = Component.translatable(MODID + ".upgradetool.pedestal_render_off");
                                    MutableComponent render = (!getCurrentRenderUpgrade)?(render_on):(render_off);
                                    ChatFormatting color = (!getCurrentRenderUpgrade)?(ChatFormatting.BLUE):(ChatFormatting.DARK_BLUE);
                                    render.withStyle(color);

                                    p_60506_.displayClientMessage(render, true);
                                }
                                else
                                {
                                    MutableComponent render_none = Component.translatable(MODID + ".upgradetool.pedestal_render_none");
                                    render_none.withStyle(ChatFormatting.WHITE);
                                    p_60506_.displayClientMessage(render_none, true);
                                }
                            }
                        }
                    }
                    else if(itemInHand.getItem().equals(DeferredRegisterItems.TOOL_MANIFEST.get())){

                        MutableComponent manifest = Component.translatable(MODID + ".manifest.color");
                        MutableComponent color = Component.translatable(MowLibReferences.MODID + "." +MowLibColorReference.getColorName(MowLibColorReference.getColorFromStateInt(p_60503_)));
                        manifest.withStyle(ChatFormatting.GOLD);
                        color.withStyle(ChatFormatting.WHITE);
                        manifest.append(color);

                        p_60506_.displayClientMessage(manifest, true);
                    }
                    return InteractionResult.FAIL;
                }
                else if (p_60506_.getItemInHand(p_60507_).getItem() instanceof ColorApplicator) {


                    getColor = MowLibColorReference.getColorFromItemStackInt(p_60506_.getItemInHand(p_60507_));
                    currentColor = MowLibColorReference.getColorFromStateInt(p_60503_);
                    if (currentColor != getColor) {
                        newState = MowLibColorReference.addColorToBlockState(p_60503_, getColor);
                        p_60504_.setBlock(p_60505_, newState, 3);
                        return InteractionResult.SUCCESS;
                    }
                    else {
                        MowLibMessageUtils.messagePlayerChat(p_60506_, ChatFormatting.RED,"mowlib.recolor.message_sameColor");
                        return InteractionResult.FAIL;
                    }
                }
                else if(itemInOffHand.getItem() instanceof IPedestalUpgrade)
                {
                    if(pedestal.attemptAddCoin(itemInOffHand, p_60506_)) {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem() instanceof IFilterItem)
                {
                    if(pedestal.attemptAddFilter(itemInOffHand,null)) {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem() instanceof IWorkCard)
                {
                    if(pedestal.attemptAddWorkCard(itemInOffHand,null))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem().equals(Items.GLOWSTONE))
                {
                    if(pedestal.attemptAddLight(itemInOffHand,null))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem().equals(Items.REDSTONE))
                {
                    if(pedestal.attemptAddRedstone(itemInOffHand,null))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem().equals(DeferredRegisterItems.AUGMENT_PEDESTAL_ROUNDROBIN.get()))
                {
                    if(pedestal.attemptAddRRobin(itemInOffHand)) {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem().equals(DeferredRegisterItems.AUGMENT_PEDESTAL_RENDERDIFFUSER.get()))
                {
                    if(pedestal.attemptAddRenderAugment(itemInOffHand))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem().equals(DeferredRegisterItems.AUGMENT_PEDESTAL_NOCOLLIDE.get()))
                {
                    if(pedestal.attemptAddNoCollide(itemInOffHand))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem() instanceof AugmentTieredSpeed)
                {
                    if(pedestal.attemptAddSpeed(itemInOffHand))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem() instanceof AugmentTieredCapacity)
                {
                    if(pedestal.attemptAddCapacity(itemInOffHand))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem() instanceof AugmentTieredStorage)
                {
                    if(pedestal.attemptAddStorage(itemInOffHand))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem() instanceof AugmentTieredRange)
                {
                    if(pedestal.attemptAddRange(itemInOffHand))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(pedestal.isAllowedTool(itemInOffHand))
                {
                    if(pedestal.attemptAddTool(itemInOffHand))
                    {
                        pedestal.actionOnNeighborBelowChange(getPosBelowBlockEntity(p_60503_, p_60505_, 1));
                        pedestal.updatePedestalPlayer(pedestal);
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(DYES.contains(itemInOffHand.getItem()))
                {
                    getColor = MowLibColorReference.getColorFromDyeInt(itemInOffHand);
                    currentColor = MowLibColorReference.getColorFromStateInt(p_60503_);
                    if (currentColor != getColor) {
                        newState = MowLibColorReference.addColorToBlockState(p_60503_, getColor);
                        p_60504_.setBlock(p_60505_, newState, 3);
                        return InteractionResult.SUCCESS;
                    } else {
                        MowLibMessageUtils.messagePlayerChat(p_60506_, ChatFormatting.RED,"mowlib.recolor.message_sameColor");
                        return InteractionResult.FAIL;
                    }

                }
                else if(itemInHand.isEmpty())
                {
                    if(p_60506_.isShiftKeyDown())
                    {
                        boolean displayOther = false;
                        MutableComponent displayOtherComponent = Component.literal("");
                        if(pedestal.hasRedstone())
                        {
                            MutableComponent redstoneCountInPedestal = Component.translatable(MODID + ".pedestal.message_redstone_disable");
                            redstoneCountInPedestal.append(""+pedestal.getRedstonePowerNeeded()+"");
                            redstoneCountInPedestal.withStyle(ChatFormatting.BLACK);
                            if(displayOther)displayOtherComponent.append(Component.translatable(MODID + ".pedestal.message_separator3"));
                            displayOtherComponent.append(redstoneCountInPedestal);
                            displayOther = true;
                        }

                        if(pedestal.hasFluid())
                        {
                            MutableComponent pedestalFluid = pedestal.getStoredFluid().getDisplayName().copy();
                            pedestalFluid.append(Component.translatable(MODID + ".pedestal.message_separator1"));
                            pedestalFluid.append(""+ pedestal.getStoredFluid().getAmount() + "");
                            pedestalFluid.append(Component.translatable(MODID + ".pedestal.message_separator2"));
                            pedestalFluid.append(""+ pedestal.getFluidCapacity() + "");
                            pedestalFluid.withStyle(ChatFormatting.BLUE);
                            if(displayOther)displayOtherComponent.append(Component.translatable(MODID + ".pedestal.message_separator3"));
                            displayOtherComponent.append(pedestalFluid);
                            displayOther = true;
                        }

                        if(pedestal.hasEnergy())
                        {
                            MutableComponent pedestalEnergy = Component.translatable(MODID + ".pedestal.message_energy");
                            pedestalEnergy.append(Component.translatable(MODID + ".pedestal.message_separator1"));
                            pedestalEnergy.append(""+ pedestal.getStoredEnergy() + "");
                            pedestalEnergy.append(Component.translatable(MODID + ".pedestal.message_separator2"));
                            pedestalEnergy.append(""+ pedestal.getEnergyCapacity() + "");
                            pedestalEnergy.withStyle(ChatFormatting.RED);
                            if(displayOther)displayOtherComponent.append(Component.translatable(MODID + ".pedestal.message_separator3"));
                            displayOtherComponent.append(pedestalEnergy);
                            displayOther = true;
                        }

                        if(pedestal.hasExperience())
                        {
                            MutableComponent pedestalExperience = Component.translatable(MODID + ".pedestal.message_experience");
                            pedestalExperience.append(Component.translatable(MODID + ".pedestal.message_separator1"));
                            pedestalExperience.append(""+ pedestal.getStoredExperience() + "");
                            pedestalExperience.append(Component.translatable(MODID + ".pedestal.message_separator2"));
                            pedestalExperience.append(""+ pedestal.getExperienceCapacity() + "");
                            pedestalExperience.withStyle(ChatFormatting.GREEN);
                            if(displayOther)displayOtherComponent.append(Component.translatable(MODID + ".pedestal.message_separator3"));
                            displayOtherComponent.append(pedestalExperience);
                            displayOther = true;
                        }

                        if(pedestal.hasDust())
                        {
                            MutableComponent dustInPedestal = Component.translatable(MODID + ".pedestal.message_dust");
                            dustInPedestal.append(Component.translatable(MODID + ".pedestal.message_separator1"));
                            dustInPedestal.append(Component.translatable(MowLibReferences.MODID + "." + MowLibColorReference.getColorName(pedestal.getStoredDust().getDustColor())));
                            dustInPedestal.append(Component.translatable(MODID + ".pedestal.message_separator4"));
                            dustInPedestal.append(""+ pedestal.getStoredDust().getDustAmount() + "");
                            dustInPedestal.append(Component.translatable(MODID + ".pedestal.message_separator2"));
                            dustInPedestal.append(""+ pedestal.getDustCapacity() + "");
                            dustInPedestal.withStyle(ChatFormatting.LIGHT_PURPLE);
                            if(displayOther)displayOtherComponent.append(Component.translatable(MODID + ".pedestal.message_separator3"));
                            displayOtherComponent.append(dustInPedestal);
                            displayOther = true;
                        }

                        if((displayOtherComponent.getSiblings().size() > 3))
                        {
                            for(int i=0;i<displayOtherComponent.getSiblings().size();i++)
                            {
                                if(displayOtherComponent.getSiblings().get(i).getString().contains(Component.translatable(MODID + ".pedestal.message_separator3").getString()))continue;

                                p_60506_.displayClientMessage(displayOtherComponent.getSiblings().get(i),false);
                            }
                        }
                        else
                        {
                            p_60506_.displayClientMessage(displayOtherComponent,true);
                        }


                    }
                    else
                    {
                        if(pedestal.hasItem())
                        {
                            if(pedestal.getItemStacks().size()>1)
                            {
                                List<ItemStack> stacks = pedestal.getItemStacks();
                                Map<Item,Integer> getMapped =  Maps.<Item,Integer>newLinkedHashMap();
                                for(int i=0;i<stacks.size();i++)
                                {
                                    if(stacks.get(i).isEmpty())continue;

                                    if(getMapped.containsKey(stacks.get(i).getItem()))
                                    {
                                        int currentValue = getMapped.getOrDefault(stacks.get(i).getItem(),0);
                                        getMapped.replace(stacks.get(i).getItem(),currentValue, currentValue + stacks.get(i).getCount());
                                    }
                                    else
                                    {
                                        getMapped.put(stacks.get(i).getItem(),stacks.get(i).getCount());
                                    }
                                }

                                if(getMapped.size()>0)
                                {
                                    MutableComponent itemCountInPedestal = Component.literal("");
                                    for (Item item : getMapped.keySet())
                                    {
                                        itemCountInPedestal.append(Component.translatable(item.getDefaultInstance().getDisplayName().getString() + " " + getMapped.get(item)));
                                        if(getMapped.size()>1)itemCountInPedestal.append(Component.translatable(MODID + ".pedestal.message_separator3"));
                                    }

                                    itemCountInPedestal.withStyle(ChatFormatting.GOLD);

                                    if((itemCountInPedestal.getSiblings().size() > 5))
                                    {
                                        for(int i=0;i<itemCountInPedestal.getSiblings().size();i++)
                                        {
                                            if(itemCountInPedestal.getSiblings().get(i).getString().contains(Component.translatable(MODID + ".pedestal.message_separator3").getString()))continue;

                                            p_60506_.displayClientMessage(itemCountInPedestal.getSiblings().get(i),false);
                                        }
                                    }
                                    else
                                    {
                                        p_60506_.displayClientMessage(itemCountInPedestal,true);
                                    }
                                }
                            }
                            else
                            {
                                MutableComponent itemCountInPedestal = Component.translatable(pedestal.getItemInPedestal().getDisplayName().getString() + " " + pedestal.getItemInPedestal().getCount());
                                itemCountInPedestal.withStyle(ChatFormatting.GOLD);
                                p_60506_.displayClientMessage(itemCountInPedestal,true);
                            }
                        }
                    }
                }
                else
                {
                    if(pedestal.hasFluid() && itemInHand.getItem().equals(Items.BUCKET))
                    {
                        if(p_60506_ instanceof FakePlayer){ return InteractionResult.FAIL; }

                        Item item = pedestal.getStoredFluid().copy().getFluid().getBucket();
                        if(item instanceof BucketItem)
                        {
                            BucketItem bucketItem = (BucketItem) item;
                            String fluid = pedestal.getStoredFluid().getDisplayName().getString();
                            if(!pedestal.removeFluid(1000, IFluidHandler.FluidAction.EXECUTE).isEmpty())
                            {
                                if(!p_60506_.isCreative())itemInHand.shrink(1);
                                if(!p_60506_.isCreative())ItemHandlerHelper.giveItemToPlayer(p_60506_,new ItemStack(bucketItem));

                                String fluidRemoved = fluid +": " +pedestal.getStoredFluid().getAmount() +"/"+pedestal.getFluidCapacity();
                                MutableComponent pedestalFluid = Component.translatable(fluidRemoved);
                                pedestalFluid.withStyle(ChatFormatting.WHITE);
                                p_60506_.displayClientMessage(pedestalFluid,true);

                                return InteractionResult.SUCCESS;
                            }
                        }
                    }

                    if(!itemInHand.isEmpty())
                    {
                        if(itemInHand.getItem() instanceof WorkCardBE)
                        {
                            return InteractionResult.FAIL;
                        }
                        else
                        {
                            ItemStack stackNotInsert = pedestal.addItemStack(itemInHand,true);
                            if(itemInHand.getCount() > stackNotInsert.getCount())
                            {
                                int shrinkAmount = itemInHand.getCount() - pedestal.addItemStack(itemInHand,false).getCount();
                                itemInHand.shrink(shrinkAmount);
                                return InteractionResult.SUCCESS;
                            }
                            else return InteractionResult.FAIL;
                        }
                    }
                    else return InteractionResult.FAIL;

                    /*
                    int allowedInsert = pedestal.countAllowedForInsert(itemInHand);
                    ItemStack stackToInsert = itemInHand.copy();
                    int countToSet = (allowedInsert>itemInHand.getCount())?(itemInHand.getCount()):(allowedInsert);
                    stackToInsert.setCount(countToSet);
                    ItemStack returnStack = (itemInHand.getCount()>countToSet)?(new ItemStack(itemInHand.getItem(),itemInHand.getCount()-countToSet)):(ItemStack.EMPTY);

                    if(!itemInHand.isEmpty() && allowedInsert>0)
                    {
                        if(pedestal.addItemStack(stackToInsert,true))
                        {
                            pedestal.addItem(stackToInsert,false);
                            p_60506_.setItemInHand(InteractionHand.MAIN_HAND,returnStack);
                            return InteractionResult.SUCCESS;
                        }
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.SUCCESS;
                    */
                }
            }
        }


        return InteractionResult.SUCCESS;
    }
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return (state.getValue(LIT))?(15):(0);
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState p_60457_) {
        //super.hasAnalogOutputSignal(p_60457_);
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState p_60487_, Level p_60488_, BlockPos p_60489_) {
        //super.getAnalogOutputSignal(p_60487_, p_60488_, p_60489_);
        return PedestalUtilities.getRedstoneLevelPedestal(p_60488_,p_60489_);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @javax.annotation.Nullable Direction direction) {
        return true;
    }



    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        return super.canHarvestBlock(state, world, pos, player);
    }

    @Override
    public void playerDestroy(Level p_49827_, Player p_49828_, BlockPos p_49829_, BlockState p_49830_, @Nullable BlockEntity p_49831_, ItemStack p_49832_) {
        /*if(!p_49827_.isClientSide())
        {
            if (p_49830_.getBlock() instanceof BasePedestalBlock) {
                if (!p_49827_.isClientSide && !p_49828_.isCreative()) {
                    ItemStack itemstack = new ItemStack(this);
                    int getColor = MowLibColorReference.getColorFromStateInt(p_49830_);
                    ItemStack newStack = MowLibColorReference.addColorToItemStack(itemstack,getColor);
                    newStack.setCount(1);
                    ItemEntity itementity = new ItemEntity(p_49827_, (double)p_49829_.getX() + 0.5D, (double)p_49829_.getY() + 0.5D, (double)p_49829_.getZ() + 0.5D, newStack);
                    itementity.setDefaultPickUpDelay();
                    p_49827_.addFreshEntity(itementity);
                }
            }
        }*/
        super.playerDestroy(p_49827_, p_49828_, p_49829_, p_49830_, p_49831_, p_49832_);
        p_49827_.removeBlock(p_49829_,false);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {

        if(player instanceof FakePlayer) {
            return false;
        }

        if (player.isCreative()) {
            if (player.getOffhandItem().getItem().equals(com.mowmaster.mowlib.Registry.DeferredRegisterItems.TOOL_DEVTOOL.get()))
                return willHarvest || super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
            else
                attack(state, world, pos, player);

            return false;
        }

        return willHarvest || super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public void onRemove(BlockState p_60515_, Level p_60516_, BlockPos p_60517_, BlockState p_60518_, boolean p_60519_) {
        if(p_60515_.getBlock() != p_60518_.getBlock())
        {
            BlockEntity blockEntity = p_60516_.getBlockEntity(p_60517_);
            if(blockEntity instanceof BasePedestalBlockEntity pedestal) {
                pedestal.dropInventoryItems(p_60516_,p_60517_);
                //Method for upgrades to do things before removal
                pedestal.actionOnRemovedFromPedestal(0);
                pedestal.dropInventoryItemsPrivate(p_60516_,p_60517_);

                //Fixed to drop an item and not spill out
                pedestal.dropLiquidsInWorld(p_60516_,p_60517_);
                pedestal.removeEnergyFromBrokenPedestal(p_60516_,p_60517_);
                pedestal.dropXPInWorld(p_60516_,p_60517_);
                pedestal.dropDustInWorld(p_60516_,p_60517_);
                p_60516_.updateNeighbourForOutputSignal(p_60517_,p_60518_.getBlock());
            }
            p_60516_.removeBlock(p_60517_,false);
            super.onRemove(p_60515_, p_60516_, p_60517_, p_60518_, p_60519_);
        }
    }

    public RenderShape getRenderShape(BlockState p_50950_) {
        return RenderShape.MODEL;
    }


    @Override
    public void neighborChanged(BlockState p_60509_, Level p_60510_, BlockPos p_60511_, Block p_60512_, BlockPos p_60513_, boolean p_60514_) {
        if(!p_60510_.isClientSide())
        {
            if(p_60513_.equals(getPosBelowBlockEntity(p_60510_,p_60511_,1)))
            {
                if(p_60510_.getBlockEntity(p_60511_) instanceof MowLibBaseBlockEntity baseBlockEntity)
                {
                    baseBlockEntity.actionOnNeighborBelowChange(getPosBelowBlockEntity(p_60510_,p_60511_,1));
                }
            }
        }
        super.neighborChanged(p_60509_, p_60510_, p_60511_, p_60512_, p_60513_, p_60514_);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return DeferredBlockEntityTypes.PEDESTAL.get().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : (level0, pos, state0, blockEntity) -> ((BasePedestalBlockEntity) blockEntity).tick();
    }
}
