package com.mowmaster.pedestals.blocks;

import com.mowmaster.pedestals.item.ItemColorPallet;
import com.mowmaster.pedestals.item.ItemLinkingTool;
import com.mowmaster.pedestals.item.ItemPedestalUpgrades;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.*;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class BlockPedestalTE extends DirectionalBlock implements IWaterLoggable {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected static final VoxelShape CUP = VoxelShapes.or(Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 2.0D, 13.0D),
            Block.makeCuboidShape(5.0D, 2.0D, 5.0D, 11.0D, 10.0D, 11.0D),
            Block.makeCuboidShape(4.0D, 10.0D, 4.0D, 12.0D, 12.0D, 12.0D));
    protected static final VoxelShape CDOWN = VoxelShapes.or(Block.makeCuboidShape(3.0D, 14.0D, 3.0D, 13.0D, 16.0D, 13.0D),
            Block.makeCuboidShape(5.0D, 14.0D, 5.0D, 11.0D, 6.0D, 11.0D),
            Block.makeCuboidShape(4.0D, 6.0D, 4.0D, 12.0D, 4.0D, 12.0D));
    //height goes in the -z direction
    protected static final VoxelShape CNORTH = VoxelShapes.or(Block.makeCuboidShape(3.0D, 3.0D, 14.0D, 13.0D, 13.0D, 16.0D),
            Block.makeCuboidShape(5.0D, 5.0D, 6.0D, 11.0D, 11.0D, 14.0D),
            Block.makeCuboidShape(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 6.0D));
    //height goes in the +x direction
    protected static final VoxelShape CEAST = VoxelShapes.or(Block.makeCuboidShape(2.0D, 3.0D, 3.0D, 0.0D, 13.0D, 13.0D),
            Block.makeCuboidShape(2.0D, 5.0D, 5.0D, 10.0D, 11.0D, 11.0D),
            Block.makeCuboidShape(10.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D));
    protected static final VoxelShape CSOUTH = VoxelShapes.or(Block.makeCuboidShape(3.0D, 3.0D, 2.0D, 13.0D, 13.0D, 0.0D),
            Block.makeCuboidShape(5.0D, 5.0D, 10.0D, 11.0D, 11.0D, 2.0D),
            Block.makeCuboidShape(4.0D, 4.0D, 12.0D, 12.0D, 12.0D, 10.0D));
    protected static final VoxelShape CWEST = VoxelShapes.or(Block.makeCuboidShape(14.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D),
            Block.makeCuboidShape(14.0D, 5.0D, 5.0D, 6.0D, 11.0D, 11.0D),
            Block.makeCuboidShape(4.0D, 4.0D, 4.0D, 6.0D, 12.0D, 12.0D));

    protected static final VoxelShape LCUP = VoxelShapes.or(Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 2.0D, 13.0D),
            Block.makeCuboidShape(4.0D, 2.0D, 4.0D, 12.0D, 3.0D, 12.0D),
            Block.makeCuboidShape(5.0D, 3.0D, 5.0D, 11.0D, 4.0D, 11.0D),
            Block.makeCuboidShape(4.5D, 4.0D, 4.5D, 11.5D, 5.0D, 11.5D),
            Block.makeCuboidShape(5.0D, 5.0D, 5.0D, 11.0D, 10.0D, 11.0D),
            Block.makeCuboidShape(4.0D, 10.0D, 4.0D, 12.0D, 12.0D, 12.0D));
    protected static final VoxelShape LCDOWN = VoxelShapes.or(Block.makeCuboidShape(3.0D, 14.0D, 3.0D, 13.0D, 16.0D, 13.0D),
            Block.makeCuboidShape(4.0D, 13.0D, 4.0D, 12.0D, 14.0D, 12.0D),
            Block.makeCuboidShape(5.0D, 13.0D, 5.0D, 11.0D, 12.0D, 11.0D),
            Block.makeCuboidShape(4.5D, 12.0D, 4.5D, 11.5D, 11.0D, 11.5D),
            Block.makeCuboidShape(5.0D, 11.0D, 5.0D, 11.0D, 6.0D, 11.0D),
            Block.makeCuboidShape(4.0D, 6.0D, 4.0D, 12.0D, 4.0D, 12.0D));
    //height goes in the -z direction
    protected static final VoxelShape LCNORTH = VoxelShapes.or(Block.makeCuboidShape(3.0D, 3.0D, 14.0D, 13.0D, 13.0D, 16.0D),
            Block.makeCuboidShape(4.0D, 4.0D, 13.0D, 12.0D, 12.0D, 14.0D),
            Block.makeCuboidShape(5.0D, 5.0D, 12.0D, 11.0D, 11.0D, 13.0D),
            Block.makeCuboidShape(4.5D, 4.5D, 11.0D, 11.5D, 11.5D, 12.0D),
            Block.makeCuboidShape(5.0D, 5.0D, 6.0D, 11.0D, 11.0D, 11.0D),
            Block.makeCuboidShape(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 6.0D));
    //height goes in the +x direction
    protected static final VoxelShape LCEAST = VoxelShapes.or(Block.makeCuboidShape(2.0D, 3.0D, 3.0D, 0.0D, 13.0D, 13.0D),
            Block.makeCuboidShape(2.0D, 4.0D, 4.0D, 3.0D, 12.0D, 12.0D),
            Block.makeCuboidShape(3.0D, 5.0D, 5.0D, 4.0D, 11.0D, 11.0D),
            Block.makeCuboidShape(4.0D, 4.5D, 4.5D, 5.0D, 11.5D, 11.5D),
            Block.makeCuboidShape(5.0D, 5.0D, 5.0D, 10.0D, 11.0D, 11.0D),
            Block.makeCuboidShape(10.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D));
    protected static final VoxelShape LCSOUTH = VoxelShapes.or(Block.makeCuboidShape(3.0D, 3.0D, 2.0D, 13.0D, 13.0D, 0.0D),
            Block.makeCuboidShape(4.0D, 4.0D, 3.0D, 12.0D, 12.0D, 2.0D),
            Block.makeCuboidShape(5.0D, 5.0D, 4.0D, 11.0D, 11.0D, 3.0D),
            Block.makeCuboidShape(4.5D, 4.5D, 5.0D, 11.5D, 11.5D, 4.0D),
            Block.makeCuboidShape(5.0D, 5.0D, 10.0D, 11.0D, 11.0D, 5.0D),
            Block.makeCuboidShape(4.0D, 4.0D, 12.0D, 12.0D, 12.0D, 10.0D));
    protected static final VoxelShape LCWEST = VoxelShapes.or(Block.makeCuboidShape(14.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D),
            Block.makeCuboidShape(13.0D, 4.0D, 4.0D, 14.0D, 12.0D, 12.0D),
            Block.makeCuboidShape(12.0D, 5.0D, 5.0D, 13.0D, 11.0D, 11.0D),
            Block.makeCuboidShape(11.0D, 4.5D, 4.5D, 12.0D, 11.5D, 11.5D),
            Block.makeCuboidShape(6.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D),
            Block.makeCuboidShape(4.0D, 4.0D, 4.0D, 6.0D, 12.0D, 12.0D));


    public BlockPedestalTE(Properties builder)
    {
        super(builder);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP).with(WATERLOGGED, Boolean.valueOf(false)).with(LIT, Boolean.valueOf(false)));
    }

    /*https://github.com/progwml6/ironchest/blob/1.15/src/main/java/com/progwml6/ironchest/common/block/GenericIronChestBlock.java#L120-L133*/
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TilePedestal) {
                InventoryHelper.dropInventoryItems(worldIn, pos, (TilePedestal) tileentity);
                worldIn.updateComparatorOutputLevel(pos, this);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
        if (!state.get(BlockStateProperties.WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER) {
            if (!worldIn.isRemote()) {
                worldIn.setBlockState(pos, state.with(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)), 3);
                worldIn.getPendingFluidTicks().scheduleTick(pos, fluidStateIn.getFluid(), fluidStateIn.getFluid().getTickRate(worldIn));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public Fluid pickupFluid(IWorld worldIn, BlockPos pos, BlockState state) {
        if (state.get(BlockStateProperties.WATERLOGGED)) {
            worldIn.setBlockState(pos, state.with(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)), 3);
            return Fluids.WATER;
        } else {
            return Fluids.EMPTY;
        }
    }

    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return !state.get(WATERLOGGED);
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    public int getLightValue(BlockState state) {
        return state.get(LIT) ? state.getLightValue() : 0;
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        switch(state.get(FACING)) {
            case UP:
            default:
                return (state.get(LIT)) ? (LCUP) : (CUP);
            case DOWN:
                return (state.get(LIT)) ? (LCDOWN) : (CDOWN);
            case NORTH:
                return (state.get(LIT)) ? (LCNORTH) : (CNORTH);
            case EAST:
                return (state.get(LIT)) ? (LCEAST) : (CEAST);
            case SOUTH:
                return (state.get(LIT)) ? (LCSOUTH) : (CSOUTH);
            case WEST:
                return (state.get(LIT)) ? (LCWEST) : (CWEST);
        }
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via IBlockState#withRotation(Rotation) whenever possible. Implementing/overriding is
     * fine.
     */
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via IBlockState#withMirror(Mirror) whenever possible. Implementing/overriding is fine.
     */
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.with(FACING, mirrorIn.mirror(state.get(FACING)));
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Direction direction = context.getFace();
        BlockState blockstate = context.getWorld().getBlockState(context.getPos().offset(direction.getOpposite()));
        return blockstate.getBlock() == this && blockstate.get(FACING) == direction ? this.getDefaultState().with(FACING, direction.getOpposite()).with(WATERLOGGED, Boolean.valueOf(false)).with(LIT, Boolean.valueOf(false)) : this.getDefaultState().with(FACING, direction).with(WATERLOGGED, Boolean.valueOf(false)).with(LIT, Boolean.valueOf(false));
    }

    /*Directly From CactusBlock Code*/
    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TilePedestal)
        {
            TilePedestal tilePedestal = (TilePedestal) tileentity;

            if(!worldIn.isRemote)
            {
                tilePedestal.collideWithPedestal(worldIn, tilePedestal, pos, state, entityIn);
            }
        }
    }



    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_) {
        if(!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof TilePedestal) {
                TilePedestal tilePedestal = (TilePedestal) tileEntity;

                if(player.isCrouching())
                {
                    if (player.getHeldItemMainhand().isEmpty())
                    {
                        if (tilePedestal.hasCoin()) {
                            player.inventory.addItemStackToInventory(tilePedestal.removeCoin());
                        }
                    }
                }
                else if(!tilePedestal.hasCoin())
                {
                    if (player.getHeldItemMainhand().isEmpty()) {
                        if (tilePedestal.hasItem()) {
                            player.inventory.addItemStackToInventory(tilePedestal.removeItem());
                        }
                    }
                    else{
                        if(player.getHeldItemMainhand().getItem() instanceof ItemUpgradeBase)
                        {
                            if(tilePedestal.addCoin(player.getHeldItemMainhand()))
                            {
                                player.getHeldItemMainhand().shrink(1);
                            }
                        }
                        else if(player.getHeldItemMainhand().getItem().equals(Items.GLOWSTONE))
                        {
                            if(!tilePedestal.hasLight())
                            {
                                tilePedestal.addLight();
                                player.getHeldItemMainhand().shrink(1);
                                return ActionResultType.SUCCESS;
                            }
                            else
                            {
                                int availableSpace = tilePedestal.canAcceptItems(player.getHeldItemMainhand());
                                if(availableSpace>0)
                                {
                                    if (tilePedestal.addItem(player.getHeldItemMainhand()))
                                    {
                                        player.getHeldItemMainhand().shrink(availableSpace);
                                        return ActionResultType.SUCCESS;
                                    }
                                }
                                else return ActionResultType.SUCCESS;
                            }
                        }
                        else if(player.getHeldItemMainhand().getItem().equals(ItemColorPallet.COLORPALLET))
                        {
                            if(tilePedestal.addColor(player.getHeldItemMainhand()))
                            {
                                player.getHeldItemMainhand().shrink(1);
                                return ActionResultType.SUCCESS;
                            }
                            else
                            {
                                player.sendStatusMessage(new StringTextComponent(TextFormatting.WHITE +"Color Can't be set on Pedestals with Items, Upgrades, or Linked Pedestals"),true);
                                //player.sendMessage(new StringTextComponent(TextFormatting.GOLD +"ColorPallet"),player.getUniqueID());
                                return ActionResultType.FAIL;
                            }

                        }
                        else if(player.getHeldItemMainhand().getItem() instanceof ItemPedestalUpgrades)
                        {
                            if(player.getHeldItemMainhand().getItem().equals(ItemPedestalUpgrades.SPEED))
                            {
                                if(tilePedestal.addSpeed(player.getHeldItemMainhand()))
                                {
                                    player.getHeldItemMainhand().shrink(1);
                                    return ActionResultType.SUCCESS;
                                }
                                else return ActionResultType.FAIL;
                            }
                            else
                            {
                                if(tilePedestal.addCapacity(player.getHeldItemMainhand()))
                                {
                                    player.getHeldItemMainhand().shrink(1);
                                    return ActionResultType.SUCCESS;
                                }
                                else return ActionResultType.FAIL;
                            }
                        }
                        else
                        {
                            int availableSpace = tilePedestal.canAcceptItems(player.getHeldItemMainhand());
                            if(availableSpace>0)
                            {
                                if (tilePedestal.addItem(player.getHeldItemMainhand()))
                                {
                                    player.getHeldItemMainhand().shrink(availableSpace);
                                    return ActionResultType.SUCCESS;
                                }
                            }
                            else return ActionResultType.SUCCESS;
                        }
                    }
                }
                else if(player.getHeldItemMainhand().getItem() instanceof ItemLinkingTool)
                {
                    return ActionResultType.FAIL;
                }
                else if(player.getHeldItemMainhand().getItem().equals(ItemColorPallet.COLORPALLET))
                {
                    if(tilePedestal.addColor(player.getHeldItemMainhand()))
                    {
                        player.getHeldItemMainhand().shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                    else
                    {
                        player.sendStatusMessage(new StringTextComponent(TextFormatting.WHITE +"Color Can't be set on Pedestals with Items, Upgrades, or Linked Pedestals"),true);
                        //player.sendMessage(new StringTextComponent(TextFormatting.GOLD +"ColorPallet"),player.getUniqueID());
                        return ActionResultType.FAIL;
                    }

                }
                else if(player.getHeldItemMainhand().getItem().equals(Items.GLOWSTONE))
                {
                    //player.sendMessage(new StringTextComponent(TextFormatting.GOLD +"GLOWSTONE"),player.getUniqueID());
                    if(!tilePedestal.hasLight())
                    {
                        tilePedestal.addLight();
                        player.getHeldItemMainhand().shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                    else
                    {
                        int availableSpace = tilePedestal.canAcceptItems(player.getHeldItemMainhand());
                        if(availableSpace>0)
                        {
                            if (tilePedestal.addItem(player.getHeldItemMainhand()))
                            {
                                player.getHeldItemMainhand().shrink(availableSpace);
                                return ActionResultType.SUCCESS;
                            }
                        }
                        else return ActionResultType.SUCCESS;
                    }
                }
                else if(player.getHeldItemMainhand().getItem() instanceof ItemPedestalUpgrades)
                {
                    if(player.getHeldItemMainhand().getItem().equals(ItemPedestalUpgrades.SPEED))
                    {
                        if(tilePedestal.addSpeed(player.getHeldItemMainhand()))
                        {
                            player.getHeldItemMainhand().shrink(1);
                            return ActionResultType.SUCCESS;
                        }
                        else return ActionResultType.FAIL;
                    }
                    else
                    {
                        if(tilePedestal.addCapacity(player.getHeldItemMainhand()))
                        {
                            player.getHeldItemMainhand().shrink(1);
                            return ActionResultType.SUCCESS;
                        }
                        else return ActionResultType.FAIL;
                    }
                }
                else if (player.getHeldItemMainhand().isEmpty()) {
                    if (tilePedestal.hasItem()) {
                        player.inventory.addItemStackToInventory(tilePedestal.removeItem());
                    }
                }
                else
                {
                    int availableSpace = tilePedestal.canAcceptItems(player.getHeldItemMainhand());
                    if(availableSpace>0)
                    {
                        if (tilePedestal.addItem(player.getHeldItemMainhand()))
                        {
                            player.getHeldItemMainhand().shrink(availableSpace);
                            return ActionResultType.SUCCESS;
                        }
                    }
                    else return ActionResultType.SUCCESS;
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return (state.get(LIT))?(15):(0);
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING,WATERLOGGED,LIT);
    }

    private int getRedstoneLevel(World worldIn, BlockPos pos)
    {
        int hasItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TilePedestal) {
            TilePedestal pedestal = (TilePedestal) tileEntity;
            int counter = pedestal.getItemInPedestal().getCount();
            if(counter<=0) {hasItem=0;}
            else if(counter>0 && counter<=1) {hasItem=1;}
            else if(counter>1 && counter<=4) {hasItem=2;}//1-4
            else if(counter>4 && counter<=9) {hasItem=3;}//4-9
            else if(counter>9 && counter<=14) {hasItem=4;}//9-14
            else if(counter>14 && counter<=19) {hasItem=5;}//14-19
            else if(counter>19 && counter<=24) {hasItem=6;}//19-24
            else if(counter>24 && counter<=29) {hasItem=7;}//24-29
            else if(counter>29 && counter<=34) {hasItem=8;}//29-34
            else if(counter>34 && counter<=39) {hasItem=9;}//34-39
            else if(counter>39 && counter<=44) {hasItem=10;}//39-44
            else if(counter>44 && counter<=49) {hasItem=11;}//44-49
            else if(counter>49 && counter<=54) {hasItem=12;}//49-54
            else if(counter>54 && counter<=59) {hasItem=13;}//54-59
            else if(counter>59 && counter<=63) {hasItem=14;}//59-63
            else if(counter>63) {hasItem=15;}
        }
        return hasItem;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.getWeakPower(blockAccess,pos,side);
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        return getRedstoneLevel(worldIn,pos);
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TilePedestal();
    }

    /**
     * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
     * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
     * @deprecated call via IBlockState...getRenderType() whenever possible. Implementing/overriding is fine.
     */
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        //Direction direction = stateIn.get(FACING);
        double d0 = (double)pos.getX() + 0.55D - (double)(rand.nextFloat() * 0.1F);
        double d1 = (double)pos.getY() + 0.55D - (double)(rand.nextFloat() * 0.1F);
        double d2 = (double)pos.getZ() + 0.55D - (double)(rand.nextFloat() * 0.1F);
        double d3 = (double)(0.4F - (rand.nextFloat() + rand.nextFloat()) * 0.4F);
        if (rand.nextInt(5) == 0) {
            //worldIn.addParticle(ParticleTypes.END_ROD, d0 + (double)direction.getXOffset() * d3, d1 + (double)direction.getYOffset() * d3, d2 + (double)direction.getZOffset() * d3, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D);
        }

    }

    /*private static final ResourceLocation RESLOC_PEDESTAL_STONE = new ResourceLocation(MODID, "pedestal/pedestal_stone");

    public static final Block BLOCK_PEDESTAL_STONE = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(RESLOC_PEDESTAL_STONE);

    public static final Item ITEM_PEDESTAL_STONE = new BlockItem(BLOCK_PEDESTAL_STONE, new Item.Properties().group(pedestals.BLOCK_GROUP)) {}.setRegistryName(RESLOC_PEDESTAL_STONE);

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITEM_PEDESTAL_STONE);
    }

    @SubscribeEvent
    public static void onBlockRegistryReady(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(BLOCK_PEDESTAL_STONE);
    }*/

    private static final ResourceLocation R_PEDESTAL_000 = new ResourceLocation(MODID, "pedestal/stone000");
    private static final ResourceLocation R_PEDESTAL_001 = new ResourceLocation(MODID, "pedestal/stone001");
    private static final ResourceLocation R_PEDESTAL_002 = new ResourceLocation(MODID, "pedestal/stone002");
    private static final ResourceLocation R_PEDESTAL_003 = new ResourceLocation(MODID, "pedestal/stone003");
    private static final ResourceLocation R_PEDESTAL_010 = new ResourceLocation(MODID, "pedestal/stone010");
    private static final ResourceLocation R_PEDESTAL_011 = new ResourceLocation(MODID, "pedestal/stone011");
    private static final ResourceLocation R_PEDESTAL_012 = new ResourceLocation(MODID, "pedestal/stone012");
    private static final ResourceLocation R_PEDESTAL_013 = new ResourceLocation(MODID, "pedestal/stone013");
    private static final ResourceLocation R_PEDESTAL_020 = new ResourceLocation(MODID, "pedestal/stone020");
    private static final ResourceLocation R_PEDESTAL_021 = new ResourceLocation(MODID, "pedestal/stone021");
    private static final ResourceLocation R_PEDESTAL_022 = new ResourceLocation(MODID, "pedestal/stone022");
    private static final ResourceLocation R_PEDESTAL_023 = new ResourceLocation(MODID, "pedestal/stone023");
    private static final ResourceLocation R_PEDESTAL_030 = new ResourceLocation(MODID, "pedestal/stone030");
    private static final ResourceLocation R_PEDESTAL_031 = new ResourceLocation(MODID, "pedestal/stone031");
    private static final ResourceLocation R_PEDESTAL_032 = new ResourceLocation(MODID, "pedestal/stone032");
    private static final ResourceLocation R_PEDESTAL_033 = new ResourceLocation(MODID, "pedestal/stone033");

    private static final ResourceLocation R_PEDESTAL_100 = new ResourceLocation(MODID, "pedestal/stone100");
    private static final ResourceLocation R_PEDESTAL_101 = new ResourceLocation(MODID, "pedestal/stone101");
    private static final ResourceLocation R_PEDESTAL_102 = new ResourceLocation(MODID, "pedestal/stone102");
    private static final ResourceLocation R_PEDESTAL_103 = new ResourceLocation(MODID, "pedestal/stone103");
    private static final ResourceLocation R_PEDESTAL_110 = new ResourceLocation(MODID, "pedestal/stone110");
    private static final ResourceLocation R_PEDESTAL_111 = new ResourceLocation(MODID, "pedestal/stone111");
    private static final ResourceLocation R_PEDESTAL_112 = new ResourceLocation(MODID, "pedestal/stone112");
    private static final ResourceLocation R_PEDESTAL_113 = new ResourceLocation(MODID, "pedestal/stone113");
    private static final ResourceLocation R_PEDESTAL_120 = new ResourceLocation(MODID, "pedestal/stone120");
    private static final ResourceLocation R_PEDESTAL_121 = new ResourceLocation(MODID, "pedestal/stone121");
    private static final ResourceLocation R_PEDESTAL_122 = new ResourceLocation(MODID, "pedestal/stone122");
    private static final ResourceLocation R_PEDESTAL_123 = new ResourceLocation(MODID, "pedestal/stone123");
    private static final ResourceLocation R_PEDESTAL_130 = new ResourceLocation(MODID, "pedestal/stone130");
    private static final ResourceLocation R_PEDESTAL_131 = new ResourceLocation(MODID, "pedestal/stone131");
    private static final ResourceLocation R_PEDESTAL_132 = new ResourceLocation(MODID, "pedestal/stone132");
    private static final ResourceLocation R_PEDESTAL_133 = new ResourceLocation(MODID, "pedestal/stone133");

    private static final ResourceLocation R_PEDESTAL_200 = new ResourceLocation(MODID, "pedestal/stone200");
    private static final ResourceLocation R_PEDESTAL_201 = new ResourceLocation(MODID, "pedestal/stone201");
    private static final ResourceLocation R_PEDESTAL_202 = new ResourceLocation(MODID, "pedestal/stone202");
    private static final ResourceLocation R_PEDESTAL_203 = new ResourceLocation(MODID, "pedestal/stone203");
    private static final ResourceLocation R_PEDESTAL_210 = new ResourceLocation(MODID, "pedestal/stone210");
    private static final ResourceLocation R_PEDESTAL_211 = new ResourceLocation(MODID, "pedestal/stone211");
    private static final ResourceLocation R_PEDESTAL_212 = new ResourceLocation(MODID, "pedestal/stone212");
    private static final ResourceLocation R_PEDESTAL_213 = new ResourceLocation(MODID, "pedestal/stone213");
    private static final ResourceLocation R_PEDESTAL_220 = new ResourceLocation(MODID, "pedestal/stone220");
    private static final ResourceLocation R_PEDESTAL_221 = new ResourceLocation(MODID, "pedestal/stone221");
    private static final ResourceLocation R_PEDESTAL_222 = new ResourceLocation(MODID, "pedestal/stone222");
    private static final ResourceLocation R_PEDESTAL_223 = new ResourceLocation(MODID, "pedestal/stone223");
    private static final ResourceLocation R_PEDESTAL_230 = new ResourceLocation(MODID, "pedestal/stone230");
    private static final ResourceLocation R_PEDESTAL_231 = new ResourceLocation(MODID, "pedestal/stone231");
    private static final ResourceLocation R_PEDESTAL_232 = new ResourceLocation(MODID, "pedestal/stone232");
    private static final ResourceLocation R_PEDESTAL_233 = new ResourceLocation(MODID, "pedestal/stone233");

    private static final ResourceLocation R_PEDESTAL_300 = new ResourceLocation(MODID, "pedestal/stone300");
    private static final ResourceLocation R_PEDESTAL_301 = new ResourceLocation(MODID, "pedestal/stone301");
    private static final ResourceLocation R_PEDESTAL_302 = new ResourceLocation(MODID, "pedestal/stone302");
    private static final ResourceLocation R_PEDESTAL_303 = new ResourceLocation(MODID, "pedestal/stone303");
    private static final ResourceLocation R_PEDESTAL_310 = new ResourceLocation(MODID, "pedestal/stone310");
    private static final ResourceLocation R_PEDESTAL_311 = new ResourceLocation(MODID, "pedestal/stone311");
    private static final ResourceLocation R_PEDESTAL_312 = new ResourceLocation(MODID, "pedestal/stone312");
    private static final ResourceLocation R_PEDESTAL_313 = new ResourceLocation(MODID, "pedestal/stone313");
    private static final ResourceLocation R_PEDESTAL_320 = new ResourceLocation(MODID, "pedestal/stone320");
    private static final ResourceLocation R_PEDESTAL_321 = new ResourceLocation(MODID, "pedestal/stone321");
    private static final ResourceLocation R_PEDESTAL_322 = new ResourceLocation(MODID, "pedestal/stone322");
    private static final ResourceLocation R_PEDESTAL_323 = new ResourceLocation(MODID, "pedestal/stone323");
    private static final ResourceLocation R_PEDESTAL_330 = new ResourceLocation(MODID, "pedestal/stone330");
    private static final ResourceLocation R_PEDESTAL_331 = new ResourceLocation(MODID, "pedestal/stone331");
    private static final ResourceLocation R_PEDESTAL_332 = new ResourceLocation(MODID, "pedestal/stone332");
    private static final ResourceLocation R_PEDESTAL_333 = new ResourceLocation(MODID, "pedestal/stone333");

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(I_PEDESTAL_000);
        event.getRegistry().register(I_PEDESTAL_001);
        event.getRegistry().register(I_PEDESTAL_002);
        event.getRegistry().register(I_PEDESTAL_003);
        event.getRegistry().register(I_PEDESTAL_010);
        event.getRegistry().register(I_PEDESTAL_011);
        event.getRegistry().register(I_PEDESTAL_012);
        event.getRegistry().register(I_PEDESTAL_013);
        event.getRegistry().register(I_PEDESTAL_020);
        event.getRegistry().register(I_PEDESTAL_021);
        event.getRegistry().register(I_PEDESTAL_022);
        event.getRegistry().register(I_PEDESTAL_023);
        event.getRegistry().register(I_PEDESTAL_030);
        event.getRegistry().register(I_PEDESTAL_031);
        event.getRegistry().register(I_PEDESTAL_032);
        event.getRegistry().register(I_PEDESTAL_033);

        event.getRegistry().register(I_PEDESTAL_100);
        event.getRegistry().register(I_PEDESTAL_101);
        event.getRegistry().register(I_PEDESTAL_102);
        event.getRegistry().register(I_PEDESTAL_103);
        event.getRegistry().register(I_PEDESTAL_110);
        event.getRegistry().register(I_PEDESTAL_111);
        event.getRegistry().register(I_PEDESTAL_112);
        event.getRegistry().register(I_PEDESTAL_113);
        event.getRegistry().register(I_PEDESTAL_120);
        event.getRegistry().register(I_PEDESTAL_121);
        event.getRegistry().register(I_PEDESTAL_122);
        event.getRegistry().register(I_PEDESTAL_123);
        event.getRegistry().register(I_PEDESTAL_130);
        event.getRegistry().register(I_PEDESTAL_131);
        event.getRegistry().register(I_PEDESTAL_132);
        event.getRegistry().register(I_PEDESTAL_133);

        event.getRegistry().register(I_PEDESTAL_200);
        event.getRegistry().register(I_PEDESTAL_201);
        event.getRegistry().register(I_PEDESTAL_202);
        event.getRegistry().register(I_PEDESTAL_203);
        event.getRegistry().register(I_PEDESTAL_210);
        event.getRegistry().register(I_PEDESTAL_211);
        event.getRegistry().register(I_PEDESTAL_212);
        event.getRegistry().register(I_PEDESTAL_213);
        event.getRegistry().register(I_PEDESTAL_220);
        event.getRegistry().register(I_PEDESTAL_221);
        event.getRegistry().register(I_PEDESTAL_222);
        event.getRegistry().register(I_PEDESTAL_223);
        event.getRegistry().register(I_PEDESTAL_230);
        event.getRegistry().register(I_PEDESTAL_231);
        event.getRegistry().register(I_PEDESTAL_232);
        event.getRegistry().register(I_PEDESTAL_233);

        event.getRegistry().register(I_PEDESTAL_300);
        event.getRegistry().register(I_PEDESTAL_301);
        event.getRegistry().register(I_PEDESTAL_302);
        event.getRegistry().register(I_PEDESTAL_303);
        event.getRegistry().register(I_PEDESTAL_310);
        event.getRegistry().register(I_PEDESTAL_311);
        event.getRegistry().register(I_PEDESTAL_312);
        event.getRegistry().register(I_PEDESTAL_313);
        event.getRegistry().register(I_PEDESTAL_320);
        event.getRegistry().register(I_PEDESTAL_321);
        event.getRegistry().register(I_PEDESTAL_322);
        event.getRegistry().register(I_PEDESTAL_323);
        event.getRegistry().register(I_PEDESTAL_330);
        event.getRegistry().register(I_PEDESTAL_331);
        event.getRegistry().register(I_PEDESTAL_332);
        event.getRegistry().register(I_PEDESTAL_333);
    }

    @SubscribeEvent
    public static void onBlockRegistryReady(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(PEDESTAL_000);
        event.getRegistry().register(PEDESTAL_001);
        event.getRegistry().register(PEDESTAL_002);
        event.getRegistry().register(PEDESTAL_003);
        event.getRegistry().register(PEDESTAL_010);
        event.getRegistry().register(PEDESTAL_011);
        event.getRegistry().register(PEDESTAL_012);
        event.getRegistry().register(PEDESTAL_013);
        event.getRegistry().register(PEDESTAL_020);
        event.getRegistry().register(PEDESTAL_021);
        event.getRegistry().register(PEDESTAL_022);
        event.getRegistry().register(PEDESTAL_023);
        event.getRegistry().register(PEDESTAL_030);
        event.getRegistry().register(PEDESTAL_031);
        event.getRegistry().register(PEDESTAL_032);
        event.getRegistry().register(PEDESTAL_033);

        event.getRegistry().register(PEDESTAL_100);
        event.getRegistry().register(PEDESTAL_101);
        event.getRegistry().register(PEDESTAL_102);
        event.getRegistry().register(PEDESTAL_103);
        event.getRegistry().register(PEDESTAL_110);
        event.getRegistry().register(PEDESTAL_111);
        event.getRegistry().register(PEDESTAL_112);
        event.getRegistry().register(PEDESTAL_113);
        event.getRegistry().register(PEDESTAL_120);
        event.getRegistry().register(PEDESTAL_121);
        event.getRegistry().register(PEDESTAL_122);
        event.getRegistry().register(PEDESTAL_123);
        event.getRegistry().register(PEDESTAL_130);
        event.getRegistry().register(PEDESTAL_131);
        event.getRegistry().register(PEDESTAL_132);
        event.getRegistry().register(PEDESTAL_133);

        event.getRegistry().register(PEDESTAL_200);
        event.getRegistry().register(PEDESTAL_201);
        event.getRegistry().register(PEDESTAL_202);
        event.getRegistry().register(PEDESTAL_203);
        event.getRegistry().register(PEDESTAL_210);
        event.getRegistry().register(PEDESTAL_211);
        event.getRegistry().register(PEDESTAL_212);
        event.getRegistry().register(PEDESTAL_213);
        event.getRegistry().register(PEDESTAL_220);
        event.getRegistry().register(PEDESTAL_221);
        event.getRegistry().register(PEDESTAL_222);
        event.getRegistry().register(PEDESTAL_223);
        event.getRegistry().register(PEDESTAL_230);
        event.getRegistry().register(PEDESTAL_231);
        event.getRegistry().register(PEDESTAL_232);
        event.getRegistry().register(PEDESTAL_233);

        event.getRegistry().register(PEDESTAL_300);
        event.getRegistry().register(PEDESTAL_301);
        event.getRegistry().register(PEDESTAL_302);
        event.getRegistry().register(PEDESTAL_303);
        event.getRegistry().register(PEDESTAL_310);
        event.getRegistry().register(PEDESTAL_311);
        event.getRegistry().register(PEDESTAL_312);
        event.getRegistry().register(PEDESTAL_313);
        event.getRegistry().register(PEDESTAL_320);
        event.getRegistry().register(PEDESTAL_321);
        event.getRegistry().register(PEDESTAL_322);
        event.getRegistry().register(PEDESTAL_323);
        event.getRegistry().register(PEDESTAL_330);
        event.getRegistry().register(PEDESTAL_331);
        event.getRegistry().register(PEDESTAL_332);
        event.getRegistry().register(PEDESTAL_333);
    }



    public static void handleBlockColors(ColorHandlerEvent.Block event) {

        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 0;} else {return -1;}},PEDESTAL_000);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 85;} else {return -1;}},PEDESTAL_001);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 170;} else {return -1;}},PEDESTAL_002);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 255;} else {return -1;}},PEDESTAL_003);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 21760;} else {return -1;}},PEDESTAL_010);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 21845;} else {return -1;}},PEDESTAL_011);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 21930;} else {return -1;}},PEDESTAL_012);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 22015;} else {return -1;}},PEDESTAL_013);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 43520;} else {return -1;}},PEDESTAL_020);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 43605;} else {return -1;}},PEDESTAL_021);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 43690;} else {return -1;}},PEDESTAL_022);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 43775;} else {return -1;}},PEDESTAL_023);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 65280;} else {return -1;}},PEDESTAL_030);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 65365;} else {return -1;}},PEDESTAL_031);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 65450;} else {return -1;}},PEDESTAL_032);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 65535;} else {return -1;}},PEDESTAL_033);

        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5570560;} else {return -1;}},PEDESTAL_100);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5570645;} else {return -1;}},PEDESTAL_101);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5570730;} else {return -1;}},PEDESTAL_102);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5570815;} else {return -1;}},PEDESTAL_103);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5592320;} else {return -1;}},PEDESTAL_110);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5592405;} else {return -1;}},PEDESTAL_111);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5592490;} else {return -1;}},PEDESTAL_112);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5592575;} else {return -1;}},PEDESTAL_113);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5614080;} else {return -1;}},PEDESTAL_120);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5614165;} else {return -1;}},PEDESTAL_121);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5614250;} else {return -1;}},PEDESTAL_122);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5614335;} else {return -1;}},PEDESTAL_123);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5635840;} else {return -1;}},PEDESTAL_130);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5635925;} else {return -1;}},PEDESTAL_131);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5636010;} else {return -1;}},PEDESTAL_132);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 5636095;} else {return -1;}},PEDESTAL_133);

        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11141120;} else {return -1;}},PEDESTAL_200);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11141205;} else {return -1;}},PEDESTAL_201);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11141290;} else {return -1;}},PEDESTAL_202);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11141375;} else {return -1;}},PEDESTAL_203);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11162880;} else {return -1;}},PEDESTAL_210);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11162965;} else {return -1;}},PEDESTAL_211);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11163050;} else {return -1;}},PEDESTAL_212);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11163135;} else {return -1;}},PEDESTAL_213);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11184640;} else {return -1;}},PEDESTAL_220);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11184725;} else {return -1;}},PEDESTAL_221);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11184810;} else {return -1;}},PEDESTAL_222);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11184895;} else {return -1;}},PEDESTAL_223);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11206400;} else {return -1;}},PEDESTAL_230);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11206485;} else {return -1;}},PEDESTAL_231);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11206570;} else {return -1;}},PEDESTAL_232);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 11206655;} else {return -1;}},PEDESTAL_233);

        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16711680;} else {return -1;}},PEDESTAL_300);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16711765;} else {return -1;}},PEDESTAL_301);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16711850;} else {return -1;}},PEDESTAL_302);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16711935;} else {return -1;}},PEDESTAL_303);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16733440;} else {return -1;}},PEDESTAL_310);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16733525;} else {return -1;}},PEDESTAL_311);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16733610;} else {return -1;}},PEDESTAL_312);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16733695;} else {return -1;}},PEDESTAL_313);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16755200;} else {return -1;}},PEDESTAL_320);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16755285;} else {return -1;}},PEDESTAL_321);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16755370;} else {return -1;}},PEDESTAL_322);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16755455;} else {return -1;}},PEDESTAL_323);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16776960;} else {return -1;}},PEDESTAL_330);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16777045;} else {return -1;}},PEDESTAL_331);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16777130;} else {return -1;}},PEDESTAL_332);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16777215;} else {return -1;}},PEDESTAL_333);
    }

    public static void handleItemColors(ColorHandlerEvent.Item event) {

        //event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 0;} else {return -1;}},I_PEDESTAL_000);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 0;} else {return -1;}},I_PEDESTAL_000);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 85;} else {return -1;}},I_PEDESTAL_001);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 170;} else {return -1;}},I_PEDESTAL_002);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 255;} else {return -1;}},I_PEDESTAL_003);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 21760;} else {return -1;}},I_PEDESTAL_010);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 21845;} else {return -1;}},I_PEDESTAL_011);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 21930;} else {return -1;}},I_PEDESTAL_012);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 22015;} else {return -1;}},I_PEDESTAL_013);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 43520;} else {return -1;}},I_PEDESTAL_020);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 43605;} else {return -1;}},I_PEDESTAL_021);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 43690;} else {return -1;}},I_PEDESTAL_022);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 43775;} else {return -1;}},I_PEDESTAL_023);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 65280;} else {return -1;}},I_PEDESTAL_030);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 65365;} else {return -1;}},I_PEDESTAL_031);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 65450;} else {return -1;}},I_PEDESTAL_032);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 65535;} else {return -1;}},I_PEDESTAL_033);

        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5570560;} else {return -1;}},I_PEDESTAL_100);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5570645;} else {return -1;}},I_PEDESTAL_101);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5570730;} else {return -1;}},I_PEDESTAL_102);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5570815;} else {return -1;}},I_PEDESTAL_103);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5592320;} else {return -1;}},I_PEDESTAL_110);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5592405;} else {return -1;}},I_PEDESTAL_111);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5592490;} else {return -1;}},I_PEDESTAL_112);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5592575;} else {return -1;}},I_PEDESTAL_113);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5614080;} else {return -1;}},I_PEDESTAL_120);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5614165;} else {return -1;}},I_PEDESTAL_121);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5614250;} else {return -1;}},I_PEDESTAL_122);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5614335;} else {return -1;}},I_PEDESTAL_123);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5635840;} else {return -1;}},I_PEDESTAL_130);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5635925;} else {return -1;}},I_PEDESTAL_131);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5636010;} else {return -1;}},I_PEDESTAL_132);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 5636095;} else {return -1;}},I_PEDESTAL_133);

        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11141120;} else {return -1;}},I_PEDESTAL_200);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11141205;} else {return -1;}},I_PEDESTAL_201);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11141290;} else {return -1;}},I_PEDESTAL_202);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11141375;} else {return -1;}},I_PEDESTAL_203);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11162880;} else {return -1;}},I_PEDESTAL_210);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11162965;} else {return -1;}},I_PEDESTAL_211);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11163050;} else {return -1;}},I_PEDESTAL_212);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11163135;} else {return -1;}},I_PEDESTAL_213);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11184640;} else {return -1;}},I_PEDESTAL_220);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11184725;} else {return -1;}},I_PEDESTAL_221);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11184810;} else {return -1;}},I_PEDESTAL_222);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11184895;} else {return -1;}},I_PEDESTAL_223);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11206400;} else {return -1;}},I_PEDESTAL_230);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11206485;} else {return -1;}},I_PEDESTAL_231);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11206570;} else {return -1;}},I_PEDESTAL_232);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 11206655;} else {return -1;}},I_PEDESTAL_233);

        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16711680;} else {return -1;}},I_PEDESTAL_300);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16711765;} else {return -1;}},I_PEDESTAL_301);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16711850;} else {return -1;}},I_PEDESTAL_302);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16711935;} else {return -1;}},I_PEDESTAL_303);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16733440;} else {return -1;}},I_PEDESTAL_310);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16733525;} else {return -1;}},I_PEDESTAL_311);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16733610;} else {return -1;}},I_PEDESTAL_312);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16733695;} else {return -1;}},I_PEDESTAL_313);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16755200;} else {return -1;}},I_PEDESTAL_320);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16755285;} else {return -1;}},I_PEDESTAL_321);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16755370;} else {return -1;}},I_PEDESTAL_322);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16755455;} else {return -1;}},I_PEDESTAL_323);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16776960;} else {return -1;}},I_PEDESTAL_330);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16777045;} else {return -1;}},I_PEDESTAL_331);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16777130;} else {return -1;}},I_PEDESTAL_332);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16777215;} else {return -1;}},I_PEDESTAL_333);
    }

    public static final Block PEDESTAL_000 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_000);
    public static final Block PEDESTAL_001 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_001);
    public static final Block PEDESTAL_002 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_002);
    public static final Block PEDESTAL_003 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_003);
    public static final Block PEDESTAL_010 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_010);
    public static final Block PEDESTAL_011 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_011);
    public static final Block PEDESTAL_012 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_012);
    public static final Block PEDESTAL_013 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_013);
    public static final Block PEDESTAL_020 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_020);
    public static final Block PEDESTAL_021 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_021);
    public static final Block PEDESTAL_022 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_022);
    public static final Block PEDESTAL_023 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_023);
    public static final Block PEDESTAL_030 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_030);
    public static final Block PEDESTAL_031 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_031);
    public static final Block PEDESTAL_032 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_032);
    public static final Block PEDESTAL_033 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_033);

    public static final Block PEDESTAL_100 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_100);
    public static final Block PEDESTAL_101 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_101);
    public static final Block PEDESTAL_102 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_102);
    public static final Block PEDESTAL_103 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_103);
    public static final Block PEDESTAL_110 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_110);
    public static final Block PEDESTAL_111 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_111);
    public static final Block PEDESTAL_112 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_112);
    public static final Block PEDESTAL_113 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_113);
    public static final Block PEDESTAL_120 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_120);
    public static final Block PEDESTAL_121 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_121);
    public static final Block PEDESTAL_122 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_122);
    public static final Block PEDESTAL_123 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_123);
    public static final Block PEDESTAL_130 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_130);
    public static final Block PEDESTAL_131 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_131);
    public static final Block PEDESTAL_132 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_132);
    public static final Block PEDESTAL_133 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_133);

    public static final Block PEDESTAL_200 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_200);
    public static final Block PEDESTAL_201 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_201);
    public static final Block PEDESTAL_202 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_202);
    public static final Block PEDESTAL_203 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_203);
    public static final Block PEDESTAL_210 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_210);
    public static final Block PEDESTAL_211 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_211);
    public static final Block PEDESTAL_212 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_212);
    public static final Block PEDESTAL_213 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_213);
    public static final Block PEDESTAL_220 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_220);
    public static final Block PEDESTAL_221 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_221);
    public static final Block PEDESTAL_222 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_222);
    public static final Block PEDESTAL_223 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_223);
    public static final Block PEDESTAL_230 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_230);
    public static final Block PEDESTAL_231 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_231);
    public static final Block PEDESTAL_232 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_232);
    public static final Block PEDESTAL_233 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_233);

    public static final Block PEDESTAL_300 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_300);
    public static final Block PEDESTAL_301 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_301);
    public static final Block PEDESTAL_302 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_302);
    public static final Block PEDESTAL_303 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_303);
    public static final Block PEDESTAL_310 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_310);
    public static final Block PEDESTAL_311 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_311);
    public static final Block PEDESTAL_312 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_312);
    public static final Block PEDESTAL_313 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_313);
    public static final Block PEDESTAL_320 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_320);
    public static final Block PEDESTAL_321 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_321);
    public static final Block PEDESTAL_322 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_322);
    public static final Block PEDESTAL_323 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_323);
    public static final Block PEDESTAL_330 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_330);
    public static final Block PEDESTAL_331 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_331);
    public static final Block PEDESTAL_332 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_332);
    public static final Block PEDESTAL_333 = new BlockPedestalTE(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_333);

    public static final Item I_PEDESTAL_000 = new BlockItem(PEDESTAL_000, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_000);
    public static final Item I_PEDESTAL_001 = new BlockItem(PEDESTAL_001, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_001);
    public static final Item I_PEDESTAL_002 = new BlockItem(PEDESTAL_002, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_002);
    public static final Item I_PEDESTAL_003 = new BlockItem(PEDESTAL_003, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_003);
    public static final Item I_PEDESTAL_010 = new BlockItem(PEDESTAL_010, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_010);
    public static final Item I_PEDESTAL_011 = new BlockItem(PEDESTAL_011, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_011);
    public static final Item I_PEDESTAL_012 = new BlockItem(PEDESTAL_012, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_012);
    public static final Item I_PEDESTAL_013 = new BlockItem(PEDESTAL_013, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_013);
    public static final Item I_PEDESTAL_020 = new BlockItem(PEDESTAL_020, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_020);
    public static final Item I_PEDESTAL_021 = new BlockItem(PEDESTAL_021, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_021);
    public static final Item I_PEDESTAL_022 = new BlockItem(PEDESTAL_022, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_022);
    public static final Item I_PEDESTAL_023 = new BlockItem(PEDESTAL_023, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_023);
    public static final Item I_PEDESTAL_030 = new BlockItem(PEDESTAL_030, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_030);
    public static final Item I_PEDESTAL_031 = new BlockItem(PEDESTAL_031, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_031);
    public static final Item I_PEDESTAL_032 = new BlockItem(PEDESTAL_032, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_032);
    public static final Item I_PEDESTAL_033 = new BlockItem(PEDESTAL_033, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_033);

    public static final Item I_PEDESTAL_100 = new BlockItem(PEDESTAL_100, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_100);
    public static final Item I_PEDESTAL_101 = new BlockItem(PEDESTAL_101, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_101);
    public static final Item I_PEDESTAL_102 = new BlockItem(PEDESTAL_102, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_102);
    public static final Item I_PEDESTAL_103 = new BlockItem(PEDESTAL_103, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_103);
    public static final Item I_PEDESTAL_110 = new BlockItem(PEDESTAL_110, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_110);
    public static final Item I_PEDESTAL_111 = new BlockItem(PEDESTAL_111, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_111);
    public static final Item I_PEDESTAL_112 = new BlockItem(PEDESTAL_112, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_112);
    public static final Item I_PEDESTAL_113 = new BlockItem(PEDESTAL_113, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_113);
    public static final Item I_PEDESTAL_120 = new BlockItem(PEDESTAL_120, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_120);
    public static final Item I_PEDESTAL_121 = new BlockItem(PEDESTAL_121, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_121);
    public static final Item I_PEDESTAL_122 = new BlockItem(PEDESTAL_122, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_122);
    public static final Item I_PEDESTAL_123 = new BlockItem(PEDESTAL_123, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_123);
    public static final Item I_PEDESTAL_130 = new BlockItem(PEDESTAL_130, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_130);
    public static final Item I_PEDESTAL_131 = new BlockItem(PEDESTAL_131, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_131);
    public static final Item I_PEDESTAL_132 = new BlockItem(PEDESTAL_132, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_132);
    public static final Item I_PEDESTAL_133 = new BlockItem(PEDESTAL_133, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_133);

    public static final Item I_PEDESTAL_200 = new BlockItem(PEDESTAL_200, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_200);
    public static final Item I_PEDESTAL_201 = new BlockItem(PEDESTAL_201, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_201);
    public static final Item I_PEDESTAL_202 = new BlockItem(PEDESTAL_202, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_202);
    public static final Item I_PEDESTAL_203 = new BlockItem(PEDESTAL_203, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_203);
    public static final Item I_PEDESTAL_210 = new BlockItem(PEDESTAL_210, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_210);
    public static final Item I_PEDESTAL_211 = new BlockItem(PEDESTAL_211, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_211);
    public static final Item I_PEDESTAL_212 = new BlockItem(PEDESTAL_212, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_212);
    public static final Item I_PEDESTAL_213 = new BlockItem(PEDESTAL_213, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_213);
    public static final Item I_PEDESTAL_220 = new BlockItem(PEDESTAL_220, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_220);
    public static final Item I_PEDESTAL_221 = new BlockItem(PEDESTAL_221, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_221);
    public static final Item I_PEDESTAL_222 = new BlockItem(PEDESTAL_222, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_222);
    public static final Item I_PEDESTAL_223 = new BlockItem(PEDESTAL_223, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_223);
    public static final Item I_PEDESTAL_230 = new BlockItem(PEDESTAL_230, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_230);
    public static final Item I_PEDESTAL_231 = new BlockItem(PEDESTAL_231, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_231);
    public static final Item I_PEDESTAL_232 = new BlockItem(PEDESTAL_232, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_232);
    public static final Item I_PEDESTAL_233 = new BlockItem(PEDESTAL_233, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_233);

    public static final Item I_PEDESTAL_300 = new BlockItem(PEDESTAL_300, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_300);
    public static final Item I_PEDESTAL_301 = new BlockItem(PEDESTAL_301, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_301);
    public static final Item I_PEDESTAL_302 = new BlockItem(PEDESTAL_302, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_302);
    public static final Item I_PEDESTAL_303 = new BlockItem(PEDESTAL_303, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_303);
    public static final Item I_PEDESTAL_310 = new BlockItem(PEDESTAL_310, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_310);
    public static final Item I_PEDESTAL_311 = new BlockItem(PEDESTAL_311, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_311);
    public static final Item I_PEDESTAL_312 = new BlockItem(PEDESTAL_312, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_312);
    public static final Item I_PEDESTAL_313 = new BlockItem(PEDESTAL_313, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_313);
    public static final Item I_PEDESTAL_320 = new BlockItem(PEDESTAL_320, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_320);
    public static final Item I_PEDESTAL_321 = new BlockItem(PEDESTAL_321, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_321);
    public static final Item I_PEDESTAL_322 = new BlockItem(PEDESTAL_322, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_322);
    public static final Item I_PEDESTAL_323 = new BlockItem(PEDESTAL_323, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_323);
    public static final Item I_PEDESTAL_330 = new BlockItem(PEDESTAL_330, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_330);
    public static final Item I_PEDESTAL_331 = new BlockItem(PEDESTAL_331, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_331);
    public static final Item I_PEDESTAL_332 = new BlockItem(PEDESTAL_332, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_332);
    public static final Item I_PEDESTAL_333 = new BlockItem(PEDESTAL_333, new Item.Properties().group(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_333);
}
