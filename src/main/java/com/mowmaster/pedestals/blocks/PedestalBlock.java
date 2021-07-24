package com.mowmaster.pedestals.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

import static net.minecraft.core.Direction.*;

public class PedestalBlock extends DirectionalBlock implements SimpleWaterloggedBlock
{
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    //0 = default
    //1= whitelist
    //2= blacklist
    public static final IntegerProperty FILTER_STATUS = IntegerProperty.create("filter_status", 0, 2);
    //https://www.shodor.org/stella2java/rgbint.html
    //public static final IntegerProperty COLOR = IntegerProperty.create("pedestal_color", 0, 16777215);
    // ^ maybe dont do this, but instead look into baked models
    //https://www.youtube.com/watch?v=hYPLL1Q-JCI&list=PLmaTwVFUUXiBKYYSyrv_uPPoPZtEsCBVJ&index=11

    //!mcp -c moj
    protected static final VoxelShape CUP = Shapes.or(Block.box(3.0D, 0.0D, 3.0D, 13.0D, 2.0D, 13.0D),
            Block.box(5.0D, 2.0D, 5.0D, 11.0D, 10.0D, 11.0D),
            Block.box(4.0D, 10.0D, 4.0D, 12.0D, 12.0D, 12.0D));
    protected static final VoxelShape CDOWN = Shapes.or(Block.box(3.0D, 14.0D, 3.0D, 13.0D, 16.0D, 13.0D),
            Block.box(5.0D, 14.0D, 5.0D, 11.0D, 6.0D, 11.0D),
            Block.box(4.0D, 6.0D, 4.0D, 12.0D, 4.0D, 12.0D));
    //height goes in the -z direction
    protected static final VoxelShape CNORTH = Shapes.or(Block.box(3.0D, 3.0D, 14.0D, 13.0D, 13.0D, 16.0D),
            Block.box(5.0D, 5.0D, 6.0D, 11.0D, 11.0D, 14.0D),
            Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 6.0D));
    //height goes in the +x direction
    protected static final VoxelShape CEAST = Shapes.or(Block.box(2.0D, 3.0D, 3.0D, 0.0D, 13.0D, 13.0D),
            Block.box(2.0D, 5.0D, 5.0D, 10.0D, 11.0D, 11.0D),
            Block.box(10.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D));
    protected static final VoxelShape CSOUTH = Shapes.or(Block.box(3.0D, 3.0D, 2.0D, 13.0D, 13.0D, 0.0D),
            Block.box(5.0D, 5.0D, 10.0D, 11.0D, 11.0D, 2.0D),
            Block.box(4.0D, 4.0D, 12.0D, 12.0D, 12.0D, 10.0D));
    protected static final VoxelShape CWEST = Shapes.or(Block.box(14.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D),
            Block.box(14.0D, 5.0D, 5.0D, 6.0D, 11.0D, 11.0D),
            Block.box(4.0D, 4.0D, 4.0D, 6.0D, 12.0D, 12.0D));

    protected static final VoxelShape LCUP = Shapes.or(Block.box(3.0D, 0.0D, 3.0D, 13.0D, 2.0D, 13.0D),
            Block.box(4.0D, 2.0D, 4.0D, 12.0D, 3.0D, 12.0D),
            Block.box(5.0D, 3.0D, 5.0D, 11.0D, 4.0D, 11.0D),
            Block.box(4.5D, 4.0D, 4.5D, 11.5D, 5.0D, 11.5D),
            Block.box(5.0D, 5.0D, 5.0D, 11.0D, 10.0D, 11.0D),
            Block.box(4.0D, 10.0D, 4.0D, 12.0D, 12.0D, 12.0D));
    protected static final VoxelShape LCDOWN = Shapes.or(Block.box(3.0D, 14.0D, 3.0D, 13.0D, 16.0D, 13.0D),
            Block.box(4.0D, 13.0D, 4.0D, 12.0D, 14.0D, 12.0D),
            Block.box(5.0D, 13.0D, 5.0D, 11.0D, 12.0D, 11.0D),
            Block.box(4.5D, 12.0D, 4.5D, 11.5D, 11.0D, 11.5D),
            Block.box(5.0D, 11.0D, 5.0D, 11.0D, 6.0D, 11.0D),
            Block.box(4.0D, 6.0D, 4.0D, 12.0D, 4.0D, 12.0D));
    //height goes in the -z direction
    protected static final VoxelShape LCNORTH = Shapes.or(Block.box(3.0D, 3.0D, 14.0D, 13.0D, 13.0D, 16.0D),
            Block.box(4.0D, 4.0D, 13.0D, 12.0D, 12.0D, 14.0D),
            Block.box(5.0D, 5.0D, 12.0D, 11.0D, 11.0D, 13.0D),
            Block.box(4.5D, 4.5D, 11.0D, 11.5D, 11.5D, 12.0D),
            Block.box(5.0D, 5.0D, 6.0D, 11.0D, 11.0D, 11.0D),
            Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 6.0D));
    //height goes in the +x direction
    protected static final VoxelShape LCEAST = Shapes.or(Block.box(2.0D, 3.0D, 3.0D, 0.0D, 13.0D, 13.0D),
            Block.box(2.0D, 4.0D, 4.0D, 3.0D, 12.0D, 12.0D),
            Block.box(3.0D, 5.0D, 5.0D, 4.0D, 11.0D, 11.0D),
            Block.box(4.0D, 4.5D, 4.5D, 5.0D, 11.5D, 11.5D),
            Block.box(5.0D, 5.0D, 5.0D, 10.0D, 11.0D, 11.0D),
            Block.box(10.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D));
    protected static final VoxelShape LCSOUTH = Shapes.or(Block.box(3.0D, 3.0D, 2.0D, 13.0D, 13.0D, 0.0D),
            Block.box(4.0D, 4.0D, 3.0D, 12.0D, 12.0D, 2.0D),
            Block.box(5.0D, 5.0D, 4.0D, 11.0D, 11.0D, 3.0D),
            Block.box(4.5D, 4.5D, 5.0D, 11.5D, 11.5D, 4.0D),
            Block.box(5.0D, 5.0D, 10.0D, 11.0D, 11.0D, 5.0D),
            Block.box(4.0D, 4.0D, 12.0D, 12.0D, 12.0D, 10.0D));
    protected static final VoxelShape LCWEST = Shapes.or(Block.box(14.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D),
            Block.box(13.0D, 4.0D, 4.0D, 14.0D, 12.0D, 12.0D),
            Block.box(12.0D, 5.0D, 5.0D, 13.0D, 11.0D, 11.0D),
            Block.box(11.0D, 4.5D, 4.5D, 12.0D, 11.5D, 11.5D),
            Block.box(6.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D),
            Block.box(4.0D, 4.0D, 4.0D, 6.0D, 12.0D, 12.0D));


    protected PedestalBlock(Properties p_52591_) {
        super(p_52591_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, UP).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, 0));//.setValue(COLOR,16777215)
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        switch(p_60555_.getValue(FACING)) {
            case UP:
            default:
                return (p_60555_.getValue(LIT)) ? (LCUP) : (CUP);
            case DOWN:
                return (p_60555_.getValue(LIT)) ? (LCDOWN) : (CDOWN);
            case NORTH:
                return (p_60555_.getValue(LIT)) ? (LCNORTH) : (CNORTH);
            case EAST:
                return (p_60555_.getValue(LIT)) ? (LCEAST) : (CEAST);
            case SOUTH:
                return (p_60555_.getValue(LIT)) ? (LCSOUTH) : (CSOUTH);
            case WEST:
                return (p_60555_.getValue(LIT)) ? (LCWEST) : (CWEST);
        }
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation direction) {
        return state.setValue(FACING,direction.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState p_60528_, Mirror p_60529_) {
        return p_60528_.setValue(FACING,p_60529_.mirror(p_60528_.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_49820_) {
        Player player = p_49820_.getPlayer();
        Direction direction = p_49820_.getClickedFace();
        BlockState blockstate = p_49820_.getLevel().getBlockState(p_49820_.getClickedPos().relative(direction.getOpposite()));
        return blockstate.getBlock() == this &&
                blockstate.getValue(FACING) == direction ?
                this.getStateForPlacement(p_49820_).setValue(FACING, direction.getOpposite()).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, 0) :
                this.getStateForPlacement(p_49820_).setValue(FACING, direction).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS,0);

    }

    @Override
    public boolean collisionExtendsVertically(BlockState state, BlockGetter world, BlockPos pos, Entity collidingEntity) {
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_60572_, BlockGetter p_60573_, BlockPos p_60574_, CollisionContext p_60575_) {
        return super.getCollisionShape(p_60572_, p_60573_, p_60574_, p_60575_);
    }

    private static final ResourceLocation R_PEDESTAL_333 = new ResourceLocation("pedestals", "pedestal/stone333");
    public static final Block PEDESTAL_333 = new PedestalBlock(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_RED).strength(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_333);
    public static final Item I_PEDESTAL_333 = new BlockItem(PEDESTAL_333, new Item.Properties()) {}.setRegistryName(R_PEDESTAL_333);

    @SubscribeEvent
    public static void onBlockRegistryReady(RegistryEvent.Register<Block> event){
        event.getRegistry().register(PEDESTAL_333);
    }

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event){
        event.getRegistry().register(I_PEDESTAL_333);
    }

    public static void handleBlockColors(ColorHandlerEvent.Block event) {
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16777215;} else {return -1;}},PEDESTAL_333);
    }

    public static void handleItemColors(ColorHandlerEvent.Item event){
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16777215;} else {return -1;}},I_PEDESTAL_333);
    }
}
