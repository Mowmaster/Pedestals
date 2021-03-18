package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeEffectGrower extends ItemUpgradeBase
{
    public ItemUpgradeEffectGrower(Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    public int getAreaWidth(ItemStack stack)
    {
        int areaWidth = 0;
        int aW = getAreaModifier(stack);
        areaWidth = ((aW)+1);
        return  areaWidth;
    }

    public int getHeight(ItemStack stack)
    {
        return  getRangeTiny(stack);
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{getHeight(coin),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            int amount = workQueueSize(coin);
            int area = workQueueTwoSize(coin);
            if(amount>0)
            {
                float f = (float)amount/(float)area;
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isClientSide)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getBlockPos();

            if(!world.hasNeighborSignal(pedestalPos))
            {
                int rangeWidth = getAreaWidth(coinInPedestal);
                int rangeHeight = getHeight(coinInPedestal);
                BlockState pedestalState = world.getBlockState(pedestalPos);
                Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
                BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
                BlockPos posNums = getBlockPosRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));

                if(world.isAreaLoaded(negNums,posNums))
                {
                    int speed = getOperationSpeed(coinInPedestal);

                    //Wont Magnet anything

                    int val = readStoredIntTwoFromNBT(coinInPedestal);
                    if(val>0)
                    {
                        writeStoredIntTwoToNBT(coinInPedestal,val-1);
                    }
                    else {
                        //If work queue doesnt exist, try to make one
                        if(workQueueSize(coinInPedestal)<=0)
                        {
                            buildWorkQueue(pedestal,rangeWidth,rangeHeight);
                            buildWorkQueueTwo(pedestal,rangeWidth,rangeHeight);
                            //Update pedestal if no items are present so that the comparator will update for the passive mode
                            if(itemInPedestal.isEmpty()) {pedestal.update();}
                        }

                        if(workQueueSize(coinInPedestal) > 0)
                        {
                            List<BlockPos> workQueue = readWorkQueueFromNBT(coinInPedestal);
                            if (world.getGameTime() % speed == 0) {
                                for(int i = 0;i< workQueue.size(); i++)
                                {
                                    BlockPos targetBlockPos = workQueue.get(i);
                                    BlockPos blockToMinePos = new BlockPos(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());
                                    BlockState targetBlock = world.getBlockState(blockToMinePos);
                                    if(canMineBlock(pedestal,blockToMinePos))
                                    {
                                        workQueue.remove(i);
                                        writeWorkQueueToNBT(coinInPedestal,workQueue);
                                        upgradeAction(world, itemInPedestal, pedestalPos, targetBlockPos, targetBlock);
                                        break;
                                    }
                                    else
                                    {
                                        workQueue.remove(i);
                                    }
                                }
                                writeWorkQueueToNBT(coinInPedestal,workQueue);
                            }
                        }
                        else {
                            //5 second cooldown
                            writeStoredIntTwoToNBT(coinInPedestal,100);
                        }
                    }
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, BlockPos posOfPedestal, BlockPos posTarget, BlockState target)
    {
        ServerWorld sworld = world.getServer().func_241755_D_();
        ItemStack bonemeal = new ItemStack(Items.BONE_MEAL);
        Random rand = new Random();

        if(target.getBlock() instanceof IGrowable || target.getBlock() instanceof IPlantable)
        {
            if (target.getBlock() instanceof IGrowable) {
                if(((IGrowable) target.getBlock()).canGrow(world,posTarget,target,false))
                {
                    if(doItemsMatch(itemInPedestal,bonemeal))
                    {
                        ((IGrowable) target.getBlock()).grow(sworld,rand,posTarget,target);
                        TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                        if(pedestalInv instanceof PedestalTileEntity) {
                            ((PedestalTileEntity) pedestalInv).removeItem(1);
                            PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,posTarget.getX(),posTarget.getY(),posTarget.getZ(),0,255,0));
                        }
                    }
                    else
                    {
                        PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,posTarget.getX(),posTarget.getY(),posTarget.getZ(),255,255,255));
                        target.randomTick((ServerWorld) world, posTarget, rand);
                        world.notifyBlockUpdate(posTarget, target, target, 2);
                    }
                }
            }
            else
            {
                PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,posTarget.getX(),posTarget.getY(),posTarget.getZ(),255,255,255));
                target.randomTick((ServerWorld) world, posTarget, rand);
                world.notifyBlockUpdate(posTarget, target, target, 2);
            }
        }
    }

    //Blocks That Can Be Bonemealed
    @Override
    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos, PlayerEntity player)
    {
        World world = pedestal.getLevel();
        BlockPos targetBlockPos = blockToMinePos;
        BlockPos blockToGrowPos = new BlockPos(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());
        BlockState blockToGrowState = world.getBlockState(blockToGrowPos);
        Block blockToGrow = blockToGrowState.getBlock();
        if(blockToGrow instanceof IGrowable || blockToGrow instanceof IPlantable)
        {
            if (blockToGrow instanceof IGrowable) {
                if (((IGrowable) blockToGrow).canGrow(world, targetBlockPos, blockToGrowState, false)) {
                    return true;
                }
            }
        }

        return false;
    }
    @Override
    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos)
    {
        World world = pedestal.getLevel();
        BlockPos targetBlockPos = blockToMinePos;
        BlockPos blockToGrowPos = new BlockPos(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());
        BlockState blockToGrowState = world.getBlockState(blockToGrowPos);
        Block blockToGrow = blockToGrowState.getBlock();
        if(blockToGrow instanceof IGrowable || blockToGrow instanceof IPlantable)
        {
            if (blockToGrow instanceof IGrowable) {
                if (((IGrowable) blockToGrow).canGrow(world, targetBlockPos, blockToGrowState, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    //All Possible Crops In Area
    @Override
    public boolean canMineBlockTwo(PedestalTileEntity pedestal, BlockPos blockToMinePos)
    {
        World world = pedestal.getLevel();
        BlockPos targetBlockPos = blockToMinePos;
        BlockPos blockToGrowPos = new BlockPos(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());
        BlockState blockToGrowState = world.getBlockState(blockToGrowPos);
        Block blockToGrow = blockToGrowState.getBlock();
        if(blockToGrow instanceof IGrowable || blockToGrow instanceof IPlantable)
        {
            return true;
        }

        return false;
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        name.withStyle(TextFormatting.GOLD);
        player.sendMessage(name, Util.NIL_UUID);
        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getDescriptionId() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getDescriptionId() + ".chat_areax");
        area.append(tr);
        area.append(areax.getString());
        area.append("" + getHeight(stack) + "");
        area.append(areax.getString());
        area.append(tr);
        area.withStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.NIL_UUID);

        TranslationTextComponent btm = new TranslationTextComponent(getDescriptionId() + ".chat_btm");
        btm.append("" + workQueueSize(stack) + "");
        btm.withStyle(TextFormatting.YELLOW);
        player.sendMessage(btm,Util.NIL_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".chat_speed");
        speed.append(getOperationSpeedString(stack));
        speed.withStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.NIL_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getDescriptionId() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getDescriptionId() + ".tooltip_areax");
        area.append(tr);
        area.append(areax.getString());
        area.append("" + getHeight(stack) + "");
        area.append(areax.getString());
        area.append(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".tooltip_speed");
        speed.append(getOperationSpeedString(stack));

        area.withStyle(TextFormatting.WHITE);
        speed.withStyle(TextFormatting.RED);

        tooltip.add(area);
        tooltip.add(speed);
    }

    public static final Item GROWER = new ItemUpgradeEffectGrower(new Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/grower"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(GROWER);
    }


}
