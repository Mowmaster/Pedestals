package com.mowmaster.pedestals.blocks;

import com.mowmaster.pedestals.item.*;
import com.mowmaster.pedestals.item.pedestalFilters.ItemFilterBase;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.item.*;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class PedestalBlock extends DirectionalBlock implements IWaterLoggable{

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    //0 = default
    //1= whitelist
    //2= blacklist
    public static final IntegerProperty FILTER_STATUS = IntegerProperty.create("filter_status", 0, 2);

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


    public PedestalBlock(Properties builder)
    {
        super(builder);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP).with(WATERLOGGED, Boolean.valueOf(false)).with(LIT, Boolean.valueOf(false)).with(FILTER_STATUS, 0));
    }

    /*https://github.com/progwml6/ironchest/blob/1.15/src/main/java/com/progwml6/ironchest/common/block/GenericIronChestBlock.java#L120-L133*/
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof PedestalTileEntity) {
                PedestalTileEntity tile = (PedestalTileEntity) tileentity;
                //InventoryHelper.dropInventoryItems(worldIn, pos, tile);
                //Custome Drop Inv below
                tile.dropInventoryItems(worldIn,pos);
                tile.dropInventoryItemsPrivate(worldIn,pos);

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

    public int getFilterStatus(BlockState state) {
        return state.get(FILTER_STATUS);
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
        return blockstate.getBlock() == this &&
                blockstate.get(FACING) == direction ?
                this.getDefaultState().with(FACING, direction.getOpposite()).with(WATERLOGGED, Boolean.valueOf(false)).with(LIT, Boolean.valueOf(false)).with(FILTER_STATUS, 0) :
                this.getDefaultState().with(FACING, direction).with(WATERLOGGED, Boolean.valueOf(false)).with(LIT, Boolean.valueOf(false)).with(FILTER_STATUS,0);
    }

    //TODO: Needs fixed because method is dep. >:(
    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof PedestalTileEntity)
        {
            PedestalTileEntity tilePedestal = (PedestalTileEntity) tileentity;

            if(!worldIn.isRemote)
            {
                tilePedestal.collideWithPedestal(worldIn, tilePedestal, pos, state, entityIn);
            }
        }
    }

    @Override
    public boolean collisionExtendsVertically(BlockState state, IBlockReader world, BlockPos pos, Entity collidingEntity) {
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_220071_1_, IBlockReader p_220071_2_, BlockPos p_220071_3_, ISelectionContext p_220071_4_) {
        return getShape(p_220071_1_,p_220071_2_,p_220071_3_,p_220071_4_);
    }

    public ActionResultType insertToPedestal(World worldIn, BlockPos pos, PlayerEntity player)
    {
        if(!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof PedestalTileEntity) {
                PedestalTileEntity tilePedestal = (PedestalTileEntity) tileEntity;
                ItemStack getItemStackInHand = player.getHeldItemMainhand();

                ItemStack inHandCopy = getItemStackInHand.copy();
                ItemStack checkInsert = tilePedestal.addItemCustom(inHandCopy,true);
                if (checkInsert.isEmpty())
                {
                    tilePedestal.addItemCustom(inHandCopy,false);
                    getItemStackInHand.shrink(inHandCopy.getCount());
                    return ActionResultType.SUCCESS;
                }
                else
                {
                    int shrink = inHandCopy.getCount() - checkInsert.getCount();
                    if(shrink>0)
                    {
                        tilePedestal.addItemCustom(inHandCopy,false);
                        getItemStackInHand.shrink(shrink);
                        return ActionResultType.SUCCESS;
                    }

                    return ActionResultType.SUCCESS;
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_) {
        if(!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof PedestalTileEntity) {
                PedestalTileEntity tilePedestal = (PedestalTileEntity) tileEntity;
                ItemStack getItemStackInHand = player.getHeldItemMainhand();
                ItemStack getItemStackInOffHand = player.getHeldItemOffhand();
                Item getItemInHand = getItemStackInHand.getItem();
                Item getItemInOffHand = getItemStackInOffHand.getItem();
                boolean hasCoin = tilePedestal.hasCoin();
                boolean isCreative = player.isCreative();

                if(getItemStackInHand.isEmpty())
                {
                    if(!getItemStackInOffHand.isEmpty())
                    {
                        if(getItemInOffHand.equals(Items.REDSTONE_TORCH))
                        {
                            if(player.isCrouching())
                            {
                                //remove Item
                                if(tilePedestal.hasTorch())
                                {
                                    ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeTorch());
                                }
                            }
                            else
                            {
                                if(!tilePedestal.hasTorch())
                                {
                                    tilePedestal.addTorch();
                                    if(!isCreative)getItemStackInOffHand.shrink(1);
                                    return ActionResultType.SUCCESS;
                                }
                            }
                        }
                        else if(getItemInHand.equals(Items.GLOWSTONE))
                        {
                            if(player.isCrouching())
                            {
                                //remove Item
                            }
                            else
                            {
                                if(!tilePedestal.hasLight())
                                {
                                    tilePedestal.addLight();
                                    if(!isCreative)getItemStackInOffHand.shrink(1);
                                    return ActionResultType.SUCCESS;
                                }
                            }
                        }
                    }
                    ItemHandlerHelper.giveItemToPlayer(player,(player.isCrouching())?(tilePedestal.removeCoin()):(tilePedestal.removeItem()));
                }
                else
                {
                    if(getItemInHand instanceof ItemLinkingTool || getItemInHand instanceof ItemUpgradeTool || getItemInHand instanceof ItemDevTool || getItemInHand instanceof ItemFilterSwapper)
                    {
                        return ActionResultType.FAIL;
                    }
                    else if(getItemInHand instanceof ItemToolSwapper)
                    {
                        if(tilePedestal.addTool(player.getHeldItemOffhand(),true))
                        {
                            tilePedestal.addTool(player.getHeldItemOffhand(),false);
                            player.getHeldItemOffhand().shrink(1);
                            TranslationTextComponent settool = new TranslationTextComponent(Reference.MODID + ".pedestal_block" + ".add_tool");
                            settool.mergeStyle(TextFormatting.WHITE);
                            player.sendStatusMessage(settool,true);
                            return ActionResultType.SUCCESS;
                        }
                        else if(tilePedestal.hasTool())
                        {
                            ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeTool());
                            return ActionResultType.SUCCESS;
                        }
                        return ActionResultType.FAIL;
                    }
                    else if(player.getHeldItemMainhand().getItem() instanceof ItemUpgradeBase)
                    {
                        if(tilePedestal.addCoin(player,getItemStackInHand,true))
                        {
                            ItemStack coinToBePlaced = getItemStackInHand.copy();
                            if(tilePedestal.addCoin(player,coinToBePlaced,false))
                            {
                                if(!isCreative)getItemStackInHand.shrink(1);
                            }
                        }
                        else return insertToPedestal(worldIn,pos,player);
                    }
                    else if(getItemInHand.equals(ItemColorPallet.COLORPALLET))
                    {
                        if(tilePedestal.addColor(getItemStackInHand))
                        {
                            if(!isCreative)getItemStackInHand.shrink(1);
                            return ActionResultType.SUCCESS;
                        }
                        else
                        {
                            TranslationTextComponent cantsetcolor = new TranslationTextComponent(Reference.MODID + ".pedestal_block" + ".cant_color");
                            cantsetcolor.mergeStyle(TextFormatting.WHITE);
                            player.sendStatusMessage(cantsetcolor,true);
                            return ActionResultType.FAIL;
                        }
                    }
                    else if(getItemInHand instanceof ItemPedestalUpgrades)
                    {
                        if(getItemInHand.equals(ItemPedestalUpgrades.SPEED))
                        {
                            if(tilePedestal.addSpeed(player.getHeldItemMainhand()))
                            {
                                if(!player.isCreative())getItemStackInHand.shrink(1);
                                return ActionResultType.SUCCESS;
                            }
                            else return insertToPedestal(worldIn,pos,player);
                        }
                        else if(getItemInHand.equals(ItemPedestalUpgrades.CAPACITY))
                        {
                            if(tilePedestal.addCapacity(player.getHeldItemMainhand()))
                            {
                                if(!isCreative)getItemStackInHand.shrink(1);
                                return ActionResultType.SUCCESS;
                            }
                            else return insertToPedestal(worldIn,pos,player);
                        }
                        else if(getItemInHand.equals(ItemPedestalUpgrades.RANGE))
                        {
                            if(tilePedestal.addRange(player.getHeldItemMainhand()))
                            {
                                if(!isCreative)getItemStackInHand.shrink(1);
                                return ActionResultType.SUCCESS;
                            }
                            else return insertToPedestal(worldIn,pos,player);
                        }
                        else if(getItemInHand.equals(ItemPedestalUpgrades.ROUNDROBIN))
                        {
                            if(tilePedestal.addRRobin(player.getHeldItemMainhand()))
                            {
                                if(!isCreative)getItemStackInHand.shrink(1);
                                return ActionResultType.SUCCESS;
                            }
                            else return insertToPedestal(worldIn,pos,player);
                        }
                        else if(getItemInHand.equals(ItemPedestalUpgrades.SOUNDMUFFLER))
                        {
                            if(tilePedestal.addMuffler(player.getHeldItemMainhand()))
                            {
                                if(!isCreative)getItemStackInHand.shrink(1);
                                return ActionResultType.SUCCESS;
                            }
                            else return insertToPedestal(worldIn,pos,player);
                        }
                        else if(getItemInHand.equals(ItemPedestalUpgrades.PARTICLEDIFFUSER))
                        {
                            if(tilePedestal.addParticleDiffuser(player.getHeldItemMainhand()))
                            {
                                if(!isCreative)getItemStackInHand.shrink(1);
                                return ActionResultType.SUCCESS;
                            }
                            else return insertToPedestal(worldIn,pos,player);
                        }
                        else return insertToPedestal(worldIn,pos,player);
                    }
                    else return insertToPedestal(worldIn,pos,player);
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        //super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if(!worldIn.isRemote)
        {
            if(placer instanceof PlayerEntity)
            {
                PlayerEntity player = ((PlayerEntity)placer);
                ItemStack offhand = placer.getHeldItemOffhand();
                if(offhand.getItem().equals(ItemLinkingTool.DEFAULT))
                {
                    ItemLinkingTool linkingTool = ((ItemLinkingTool)offhand.getItem());
                    TranslationTextComponent linksucess = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_success");
                    linksucess.mergeStyle(TextFormatting.WHITE);
                    TranslationTextComponent linkunsuccess = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_unsucess");
                    linkunsuccess.mergeStyle(TextFormatting.WHITE);
                    TranslationTextComponent linkremoved = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_removed");
                    linkremoved.mergeStyle(TextFormatting.WHITE);
                    TranslationTextComponent linkitsself = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_itsself");
                    linkitsself.mergeStyle(TextFormatting.WHITE);
                    TranslationTextComponent linknetwork = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_network");
                    linknetwork.mergeStyle(TextFormatting.WHITE);
                    TranslationTextComponent linkdistance = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_distance");
                    linkdistance.mergeStyle(TextFormatting.WHITE);
                    if(offhand.hasTag() && offhand.isEnchanted())
                    {
                        //Checks if clicked blocks is a Pedestal
                        if(worldIn.getBlockState(pos).getBlock() instanceof PedestalBlock)
                        {
                            //Checks Tile at location to make sure its a TilePedestal
                            TileEntity tileEntity = worldIn.getTileEntity(pos);
                            if (tileEntity instanceof PedestalTileEntity) {
                                PedestalTileEntity tilePedestal = (PedestalTileEntity) tileEntity;

                                //checks if connecting pedestal is out of range of the senderPedestal
                                if(linkingTool.isPedestalInRange(tilePedestal,linkingTool.getStoredPosition(offhand)))
                                {
                                    //Checks if pedestals to be linked are on same networks or if one is neutral
                                    if(tilePedestal.canLinkToPedestalNetwork(linkingTool.getStoredPosition(offhand)))
                                    {
                                        //If stored location isnt the same as the connecting pedestal
                                        if(!tilePedestal.isSamePedestal(linkingTool.getStoredPosition(offhand)))
                                        {
                                            //Checks if the conenction hasnt been made once already yet
                                            if(!tilePedestal.isAlreadyLinked(linkingTool.getStoredPosition(offhand)))
                                            {
                                                //Checks if senderPedestal has locationSlots available
                                                //System.out.println("Stored Locations: "+ tilePedestal.getNumberOfStoredLocations());
                                                if(tilePedestal.storeNewLocation(linkingTool.getStoredPosition(offhand)))
                                                {
                                                    player.sendMessage(linksucess,Util.DUMMY_UUID);
                                                }
                                            }
                                        }
                                    }
                                    else player.sendMessage(linknetwork,Util.DUMMY_UUID);
                                }
                                else player.sendMessage(linkdistance, Util.DUMMY_UUID);
                            }
                        }
                    }
                }
                else if(offhand.getItem().equals(ItemLinkingToolBackwards.DEFAULT))
                {
                    ItemLinkingTool linkingTool = ((ItemLinkingTool)offhand.getItem());
                    TranslationTextComponent linksucess = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_success");
                    linksucess.mergeStyle(TextFormatting.WHITE);
                    TranslationTextComponent linkunsuccess = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_unsucess");
                    linkunsuccess.mergeStyle(TextFormatting.WHITE);
                    TranslationTextComponent linkremoved = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_removed");
                    linkremoved.mergeStyle(TextFormatting.WHITE);
                    TranslationTextComponent linkitsself = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_itsself");
                    linkitsself.mergeStyle(TextFormatting.WHITE);
                    TranslationTextComponent linknetwork = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_network");
                    linknetwork.mergeStyle(TextFormatting.WHITE);
                    TranslationTextComponent linkdistance = new TranslationTextComponent(linkingTool.getTranslationKey() + ".tool_link_distance");
                    linkdistance.mergeStyle(TextFormatting.WHITE);
                    if(offhand.hasTag() && offhand.isEnchanted())
                    {
                        //Checks if clicked blocks is a Pedestal
                        if(worldIn.getBlockState(pos).getBlock() instanceof PedestalBlock)
                        {
                            //Checks Tile at location to make sure its a TilePedestal
                            TileEntity tileEntitySender = worldIn.getTileEntity(linkingTool.getStoredPosition(offhand));
                            if (tileEntitySender instanceof PedestalTileEntity) {
                                PedestalTileEntity tilePedestalSender = (PedestalTileEntity) tileEntitySender;

                                //checks if connecting pedestal is out of range of the senderPedestal
                                if(linkingTool.isPedestalInRange(tilePedestalSender,pos))
                                {
                                    //Checks if pedestals to be linked are on same networks or if one is neutral
                                    if(tilePedestalSender.canLinkToPedestalNetwork(pos))
                                    {
                                        //If stored location isnt the same as the connecting pedestal
                                        if(!tilePedestalSender.isSamePedestal(pos))
                                        {
                                            //Checks if the conenction hasnt been made once already yet
                                            if(!tilePedestalSender.isAlreadyLinked(pos))
                                            {
                                                //Checks if senderPedestal has locationSlots available
                                                //System.out.println("Stored Locations: "+ tilePedestal.getNumberOfStoredLocations());
                                                if(tilePedestalSender.storeNewLocation(pos))
                                                {
                                                    player.sendMessage(linksucess,Util.DUMMY_UUID);
                                                }
                                                else player.sendMessage(linkunsuccess,Util.DUMMY_UUID);
                                            }

                                        }
                                        else player.sendMessage(linkitsself,Util.DUMMY_UUID);
                                    }
                                    else player.sendMessage(linknetwork,Util.DUMMY_UUID);
                                }
                                else player.sendMessage(linkdistance, Util.DUMMY_UUID);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return (state.get(LIT))?(15):(0);
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING,WATERLOGGED,LIT,FILTER_STATUS);
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
        if(!world.isRemote())
        {
            if(state.getBlock() instanceof PedestalBlock)
            {
                BlockPos blockBelow = getPosOfBlockBelow((ServerWorld)world,pos,1);
                if(blockBelow.equals(neighbor))
                {
                    TileEntity tile = world.getTileEntity(pos);
                    if(tile instanceof PedestalTileEntity)
                    {
                        PedestalTileEntity pedestal = (PedestalTileEntity)tile;
                        Item coin = pedestal.getCoinOnPedestal().getItem();
                        if(coin instanceof ItemUpgradeBase)
                        {
                            ((ItemUpgradeBase)coin).onPedestalNeighborChanged(pedestal);
                        }
                    }
                }
            }
        }
    }

    //Found this beauty inside of the observer block class :D
    @Override
    public BlockState updatePostPlacement(BlockState p_196271_1_, Direction p_196271_2_, BlockState p_196271_3_, IWorld p_196271_4_, BlockPos p_196271_5_, BlockPos p_196271_6_) {
        if(!p_196271_4_.isRemote())
        {
            if(p_196271_1_.getBlock() instanceof PedestalBlock)
            {
                BlockPos blockBelowPos = p_196271_6_;
                BlockState blockBelow = p_196271_3_;
                if(getPosOfBlockBelow((ServerWorld)p_196271_4_,p_196271_5_,1).equals(blockBelowPos))
                {
                    TileEntity tile = p_196271_4_.getTileEntity(p_196271_5_);
                    if(tile instanceof PedestalTileEntity)
                    {
                        PedestalTileEntity pedestal = (PedestalTileEntity)tile;
                        Item coin = pedestal.getCoinOnPedestal().getItem();
                        if(coin instanceof ItemUpgradeBase)
                        {
                            ((ItemUpgradeBase)coin).onPedestalBelowNeighborChanged(pedestal,p_196271_3_,p_196271_6_);
                        }
                    }
                }
            }
        }

        return super.updatePostPlacement(p_196271_1_, p_196271_2_, p_196271_3_, p_196271_4_, p_196271_5_, p_196271_6_);
    }

    public BlockPos getPosOfBlockBelow(World world, BlockPos posOfPedestal, int numBelow)
    {
        BlockState state = world.getBlockState(posOfPedestal);

        Direction enumfacing = (state.hasProperty(FACING))?(state.get(FACING)):(Direction.UP);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.add(0,-numBelow,0);
            case DOWN:
                return blockBelow.add(0,numBelow,0);
            case NORTH:
                return blockBelow.add(0,0,numBelow);
            case SOUTH:
                return blockBelow.add(0,0,-numBelow);
            case EAST:
                return blockBelow.add(-numBelow,0,0);
            case WEST:
                return blockBelow.add(numBelow,0,0);
            default:
                return blockBelow;
        }
    }

    private int getRedstoneLevel(World worldIn, BlockPos pos)
    {
        int hasItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack itemstack = pedestal.getItemInPedestalOverride();
            ItemStack coin = pedestal.getCoinOnPedestal();
            if(coin.getItem() instanceof ItemUpgradeBase)
            {
                return ((ItemUpgradeBase)coin.getItem()).getComparatorRedstoneLevel(worldIn,pos);
            }
            if(!itemstack.isEmpty())
            {
                float f = (float)itemstack.getCount()/(float)Math.min(pedestal.maxStackSize(), itemstack.getMaxStackSize());
                hasItem = MathHelper.floor(f*14.0F)+1;
            }
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
        return new PedestalTileEntity();
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

    /*@OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        //Direction direction = stateIn.get(FACING);
        double d0 = (double)pos.getX() + 0.55D - (double)(rand.nextFloat() * 0.1F);
        double d1 = (double)pos.getY() + 0.55D - (double)(rand.nextFloat() * 0.1F);
        double d2 = (double)pos.getZ() + 0.55D - (double)(rand.nextFloat() * 0.1F);
        double d3 = (double)(0.4F - (rand.nextFloat() + rand.nextFloat()) * 0.4F);
        if (rand.nextInt(5) == 0) {
            //worldIn.addParticle(ParticleTypes.END_ROD, d0 + (double)direction.getXOffset() * d3, d1 + (double)direction.getYOffset() * d3, d2 + (double)direction.getZOffset() * d3, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D);
        }
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

    public static final Block PEDESTAL_000 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_000);
    public static final Block PEDESTAL_001 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_001);
    public static final Block PEDESTAL_002 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_002);
    public static final Block PEDESTAL_003 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_003);
    public static final Block PEDESTAL_010 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_010);
    public static final Block PEDESTAL_011 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_011);
    public static final Block PEDESTAL_012 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_012);
    public static final Block PEDESTAL_013 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_013);
    public static final Block PEDESTAL_020 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_020);
    public static final Block PEDESTAL_021 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_021);
    public static final Block PEDESTAL_022 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_022);
    public static final Block PEDESTAL_023 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_023);
    public static final Block PEDESTAL_030 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_030);
    public static final Block PEDESTAL_031 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_031);
    public static final Block PEDESTAL_032 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_032);
    public static final Block PEDESTAL_033 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_033);

    public static final Block PEDESTAL_100 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_100);
    public static final Block PEDESTAL_101 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_101);
    public static final Block PEDESTAL_102 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_102);
    public static final Block PEDESTAL_103 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_103);
    public static final Block PEDESTAL_110 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_110);
    public static final Block PEDESTAL_111 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_111);
    public static final Block PEDESTAL_112 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_112);
    public static final Block PEDESTAL_113 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_113);
    public static final Block PEDESTAL_120 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_120);
    public static final Block PEDESTAL_121 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_121);
    public static final Block PEDESTAL_122 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_122);
    public static final Block PEDESTAL_123 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_123);
    public static final Block PEDESTAL_130 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_130);
    public static final Block PEDESTAL_131 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_131);
    public static final Block PEDESTAL_132 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_132);
    public static final Block PEDESTAL_133 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_133);

    public static final Block PEDESTAL_200 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_200);
    public static final Block PEDESTAL_201 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_201);
    public static final Block PEDESTAL_202 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_202);
    public static final Block PEDESTAL_203 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_203);
    public static final Block PEDESTAL_210 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_210);
    public static final Block PEDESTAL_211 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_211);
    public static final Block PEDESTAL_212 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_212);
    public static final Block PEDESTAL_213 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_213);
    public static final Block PEDESTAL_220 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_220);
    public static final Block PEDESTAL_221 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_221);
    public static final Block PEDESTAL_222 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_222);
    public static final Block PEDESTAL_223 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_223);
    public static final Block PEDESTAL_230 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_230);
    public static final Block PEDESTAL_231 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_231);
    public static final Block PEDESTAL_232 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_232);
    public static final Block PEDESTAL_233 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_233);

    public static final Block PEDESTAL_300 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_300);
    public static final Block PEDESTAL_301 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_301);
    public static final Block PEDESTAL_302 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_302);
    public static final Block PEDESTAL_303 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_303);
    public static final Block PEDESTAL_310 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_310);
    public static final Block PEDESTAL_311 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_311);
    public static final Block PEDESTAL_312 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_312);
    public static final Block PEDESTAL_313 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_313);
    public static final Block PEDESTAL_320 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_320);
    public static final Block PEDESTAL_321 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_321);
    public static final Block PEDESTAL_322 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_322);
    public static final Block PEDESTAL_323 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_323);
    public static final Block PEDESTAL_330 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_330);
    public static final Block PEDESTAL_331 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_331);
    public static final Block PEDESTAL_332 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_332);
    public static final Block PEDESTAL_333 = new PedestalBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_333);

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
