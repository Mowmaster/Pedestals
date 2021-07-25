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
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

import java.util.Random;

import static com.mowmaster.pedestals.Pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.ReferenceMain.MODID;
import static net.minecraft.core.Direction.*;

public class PedestalBlock extends DirectionalBlock implements SimpleWaterloggedBlock{

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

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        super.tick(state, worldIn, pos, rand);
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

    /*@Nullable
    @Override
    public BlockState getToolModifiedState(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack stack, ToolType toolType) {
        return null;
    }*/

    //Should be the left click action
    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        if(!worldIn.isRemote) {
            boolean isCreative = player.isCreative();

            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof PedestalTileEntity) {
                PedestalTileEntity tilePedestal = (PedestalTileEntity) tileEntity;
                ItemStack getItemStackInHand = player.getHeldItemMainhand();
                ItemStack getItemStackInOffHand = player.getHeldItemOffhand();
                Item getItemInHand = getItemStackInHand.getItem();
                Item getItemInOffHand = getItemStackInOffHand.getItem();
                boolean hasCoin = tilePedestal.hasCoin();

                /*
                if(no item in off hand)
                left click = remove item (1)
                crouch + left click = remove item stack
                if(item in off hand)
                    if(matches an augment)
                        left click = remove item (1)
                        crouch + left click = remove item stack(if augment has more then 1)
                 */

                /*  Currently no way to remove glowstone
                if(getItemInOffHand.equals(Blocks.GLOWSTONE.asItem()))
                {
                    if(!tilePedestal.hasLight())
                    {
                        tilePedestal.addLight();
                        if(!isCreative)getItemStackInOffHand.shrink(1);
                    }
                }
                else */

                if(getItemInOffHand.equals(Items.REDSTONE_TORCH))
                {
                    if(tilePedestal.hasTorch())
                    {
                        ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeTorch());
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.ROUNDROBIN))
                {
                    if(tilePedestal.hasRRobin())
                    {
                        ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeRRobin());
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.SPEED))
                {
                    if(tilePedestal.hasSpeed())
                    {
                        if(player.isCrouching())
                        {
                            ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeSpeed());
                        }
                        else
                        {
                            ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeSpeed(1));
                        }
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.RANGE))
                {
                    if(tilePedestal.hasRange())
                    {
                        if(player.isCrouching())
                        {
                            ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeRange());
                        }
                        else
                        {
                            ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeRange(1));
                        }
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.CAPACITY))
                {
                    if(tilePedestal.hasCapacity())
                    {
                        if(player.isCrouching())
                        {
                            ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeCapacity());
                        }
                        else
                        {
                            ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeCapacity(1));
                        }
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.SOUNDMUFFLER))
                {
                    if(tilePedestal.hasMuffler())
                    {
                        ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeMuffler());
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.PARTICLEDIFFUSER))
                {
                    if(tilePedestal.hasParticleDiffuser())
                    {
                        ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeParticleDiffuser());
                    }
                }
                else if(getItemInOffHand.equals(ItemUpgradeTool.UPGRADE) || getItemInOffHand instanceof IUpgradeBase)
                {
                    if(tilePedestal.hasCoin())
                    {
                        ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeCoin());
                    }
                }
                else if(getItemInOffHand.equals(ItemFilterSwapper.FILTERTOOL) || getItemInOffHand instanceof IFilterBase)
                {
                    if(tilePedestal.hasFilter())
                    {
                        ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeFilter(true));
                    }
                }
                else if(getItemInOffHand.equals(ItemToolSwapper.QUARRYTOOL))
                {
                    if(tilePedestal.hasTool())
                    {
                        ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeTool());
                    }
                }
                else if(getItemStackInOffHand.isEmpty() && (getItemStackInHand.isEmpty() || getItemInHand.equals(tilePedestal.getItemInPedestal().getItem())))
                {
                    if(tilePedestal.hasItem())
                    {
                        if(player.isCrouching())
                        {
                            ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeItem());
                        }
                        else
                        {
                            ItemHandlerHelper.giveItemToPlayer(player,tilePedestal.removeItem(1));
                        }
                    }
                }
            }
        }

        super.onBlockClicked(state, worldIn, pos, player);
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

                /*
                Inserting items = right click
                Inserting upgrades or augments = item in offhand + right click on pedestal
                 */


                if(getItemInHand instanceof ItemLinkingTool || getItemInHand instanceof ItemUpgradeTool || getItemInHand instanceof ItemDevTool || getItemInHand instanceof ItemFilterSwapper || getItemInHand instanceof ItemToolSwapper)
                {
                    return ActionResultType.FAIL;
                }
                else if(getItemInOffHand instanceof IUpgradeBase)
                {
                    if(!tilePedestal.hasCoin())
                    {
                        ItemStack coinToBePlaced = getItemStackInOffHand.copy();
                        if(tilePedestal.addCoin(player,getItemStackInOffHand,true))
                        {
                            tilePedestal.addCoin(player,coinToBePlaced,false);
                            if(!isCreative)getItemStackInOffHand.shrink(1);
                        }
                    }
                }
                else if(getItemInOffHand.equals(ItemColorPallet.COLORPALLET))
                {
                    if(tilePedestal.addColor(getItemStackInOffHand))
                    {
                        if(!isCreative)getItemStackInOffHand.shrink(1);
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
                else if(getItemInOffHand.equals(Blocks.GLOWSTONE.asItem()))
                {
                    if(!tilePedestal.hasLight())
                    {
                        tilePedestal.addLight();
                        if(!isCreative)getItemStackInOffHand.shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                }
                else if(getItemInOffHand.equals(Items.REDSTONE_TORCH))
                {
                    if(!tilePedestal.hasTorch())
                    {
                        tilePedestal.addTorch();
                        if(!isCreative)getItemStackInOffHand.shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.ROUNDROBIN))
                {
                    if(!tilePedestal.hasRRobin())
                    {
                        tilePedestal.addRRobin(getItemStackInOffHand);
                        if(!isCreative)getItemStackInOffHand.shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.SPEED))
                {
                    if(!(tilePedestal.getSpeed()>=5))
                    {
                        tilePedestal.addSpeed(getItemStackInOffHand);
                        if(!isCreative)getItemStackInOffHand.shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.RANGE))
                {
                    if(!(tilePedestal.getRange()>=5))
                    {
                        tilePedestal.addRange(getItemStackInOffHand);
                        if(!isCreative)getItemStackInOffHand.shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.CAPACITY))
                {
                    if(!(tilePedestal.getCapacity()>=5))
                    {
                        tilePedestal.addCapacity(getItemStackInOffHand);
                        if(!isCreative)getItemStackInOffHand.shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.SOUNDMUFFLER))
                {
                    if(!tilePedestal.hasMuffler())
                    {
                        tilePedestal.addMuffler(getItemStackInOffHand);
                        if(!isCreative)getItemStackInOffHand.shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                }
                else if(getItemInOffHand.equals(ItemPedestalUpgrades.PARTICLEDIFFUSER))
                {
                    if(!tilePedestal.hasParticleDiffuser())
                    {
                        tilePedestal.addParticleDiffuser(getItemStackInOffHand);
                        if(!isCreative)getItemStackInOffHand.shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                }
                else if(getItemInOffHand instanceof IFilterBase)
                {
                    if(!tilePedestal.hasFilter())
                    {
                        if(tilePedestal.addFilter(getItemStackInOffHand,true))
                        {
                            tilePedestal.addFilter(getItemStackInOffHand,false);
                            if(!isCreative)getItemStackInOffHand.shrink(1);
                            return ActionResultType.SUCCESS;
                        }
                    }
                }
                else if(!getItemStackInOffHand.isEmpty() && tilePedestal.addTool(getItemStackInOffHand,true))
                {
                    if(!tilePedestal.hasTool())
                    {
                        tilePedestal.addTool(getItemStackInOffHand,false);
                        if(!isCreative)getItemStackInOffHand.shrink(1);
                        TranslationTextComponent settool = new TranslationTextComponent(Reference.MODID + ".pedestal_block" + ".add_tool");
                        settool.mergeStyle(TextFormatting.WHITE);
                        player.sendStatusMessage(settool,true);
                        return ActionResultType.SUCCESS;
                    }
                }
                else if(getItemStackInHand.isEmpty())
                {
                    if(!tilePedestal.getItemInPedestal().isEmpty())
                    {
                        TranslationTextComponent getItemInPed = new TranslationTextComponent(tilePedestal.getItemInPedestal().getDisplayName().getString() +": "+ tilePedestal.getItemInPedestal().getCount());
                        getItemInPed.mergeStyle(TextFormatting.WHITE);
                        player.sendStatusMessage(getItemInPed,true);
                        return ActionResultType.SUCCESS;
                    }
                }
                else return insertToPedestal(worldIn,pos,player);
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean removedByPlayer (BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {

        if (player.isCreative()) {
            if (player.inventory.hasItemStack(new ItemStack(ItemDevTool.DEV)))
                return willHarvest || super.removedByPlayer(state, world, pos, player, false, fluid);
            else
                onBlockClicked(state, world, pos, player);

            return false;
        }

        return willHarvest || super.removedByPlayer(state, world, pos, player, false, fluid);
    }

    @Override
    public void harvestBlock (World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.removeBlock(pos, false);
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
                        if(coin instanceof IUpgradeBase)
                        {
                            ((IUpgradeBase)coin).onPedestalNeighborChanged(pedestal);
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
                        if(coin instanceof IUpgradeBase)
                        {
                            ((IUpgradeBase)coin).onPedestalBelowNeighborChanged(pedestal,p_196271_3_,p_196271_6_);
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
            if(coin.getItem() instanceof IUpgradeBase)
            {
                return ((IUpgradeBase)coin.getItem()).getComparatorRedstoneLevel(worldIn,pos);
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

    public RenderShape getRenderType(BlockState state) {
        return RenderShape.MODEL;
    }



    private static final ResourceLocation R_PEDESTAL_333 = new ResourceLocation(MODID, "pedestal/stone333");

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(I_PEDESTAL_333);
    }

    @SubscribeEvent
    public static void onBlockRegistryReady(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(PEDESTAL_333);
    }

    public static void handleBlockColors(ColorHandlerEvent.Block event) {
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return 16777215;} else {return -1;}},PEDESTAL_333);
    }

    public static void handleItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return 16777215;} else {return -1;}},I_PEDESTAL_333);
    }

    public static final Block PEDESTAL_333 = new PedestalBlock(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_RED).strength(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(R_PEDESTAL_333);

    public static final Item I_PEDESTAL_333 = new BlockItem(PEDESTAL_333, new Item.Properties().tab(PEDESTALS_TAB)) {}.setRegistryName(R_PEDESTAL_333);
}
