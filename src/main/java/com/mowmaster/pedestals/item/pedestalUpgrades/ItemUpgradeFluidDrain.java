package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeFluidDrain extends ItemUpgradeBaseFluid
{
    public ItemUpgradeFluidDrain(Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    @Override
    public Boolean canAcceptArea() {return true;}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    public int getHeight(ItemStack stack)
    {
        return getRangeLarge(stack);
    }

    public int getWidth(ItemStack stack)
    {
        return  getAreaModifier(stack);
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return getWidth(coin);
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{getHeight(coin),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getWidth(coin);
    }

    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            int width = getWidth(pedestal.getCoinOnPedestal());
            int height = getHeight(pedestal.getCoinOnPedestal());
            int amount = blocksToFillInArea(pedestal,width,height);
            int area = Math.multiplyExact(Math.multiplyExact(amount,amount),height);
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

            int getMaxFluidValue = getFluidbuffer(coinInPedestal);
            if(!hasMaxFluidSet(coinInPedestal) || readMaxFluidFromNBT(coinInPedestal) != getMaxFluidValue) {setMaxFluid(coinInPedestal, getMaxFluidValue);}

            int rangeWidth = getWidth(coinInPedestal);
            int rangeHeight = getHeight(coinInPedestal);
            int speed = getOperationSpeed(coinInPedestal);

            BlockState pedestalState = world.getBlockState(pedestalPos);
            Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
            BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
            BlockPos posNums = getBlockPosRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
            FluidStack fluidInCoin = getFluidStored(coinInPedestal);

            //Check if we can even have a blocks worth of fluid to place
            if(fluidInCoin.getAmount() >= FluidAttributes.BUCKET_VOLUME)
            {
                if(world.isAreaLoaded(negNums,posNums))
                {
                    if(!world.hasNeighborSignal(pedestalPos)) {

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
                            }

                            //
                            if(workQueueSize(coinInPedestal) > 0)
                            {
                                //Check if we can even have a bucket of fluid to place down
                                if(getFluidStored(coinInPedestal).getAmount() >= FluidAttributes.BUCKET_VOLUME)
                                {
                                    List<BlockPos> workQueue = readWorkQueueFromNBT(coinInPedestal);
                                    if (world.getGameTime() % speed == 0) {
                                        for(int i = 0;i< workQueue.size(); i++)
                                        {
                                            BlockPos targetBlockPos = workQueue.get(i);
                                            BlockPos blockToPumpPos = new BlockPos(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());
                                            BlockState targetFluidState = world.getBlockState(blockToPumpPos);
                                            Block targetFluidBlock = targetFluidState.getBlock();
                                            if(canMineBlock(pedestal,blockToPumpPos))
                                            {
                                                workQueue.remove(i);
                                                writeWorkQueueToNBT(coinInPedestal,workQueue);
                                                upgradeAction(pedestal, targetBlockPos, itemInPedestal, coinInPedestal);
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
                            }
                            else {
                                writeStoredIntTwoToNBT(coinInPedestal,(((rangeWidth*2)+1)*20)+20);
                            }
                        }
                        /*if(blocksToFillInArea(pedestal,rangeWidth,rangeHeight) > 0)
                        {
                            if (world.getGameTime() % speed == 0) {
                                int currentPosition = 0;
                                for(currentPosition = getStoredInt(coinInPedestal);!resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));currentPosition++)
                                {
                                    BlockPos targetBlockPos = getBlockPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                    BlockPos blockToFillPos = new BlockPos(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());
                                    FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
                                    fakePlayer.setPos(pedestalPos.getX(),pedestalPos.getY(),pedestalPos.getZ());
                                    if(world.isBlockModifiable(fakePlayer,blockToFillPos) && placeFluid(pedestal,fakePlayer,blockToFillPos,fluidInCoin,true))
                                    {
                                        writeStoredIntToNBT(coinInPedestal,currentPosition);
                                        break;
                                    }
                                }
                                BlockPos targetBlockPos = getBlockPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                BlockState targetBlock = world.getBlockState(targetBlockPos);
                                upgradeAction(pedestal, targetBlockPos, itemInPedestal, coinInPedestal);
                                if(resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums)))
                                {
                                    writeStoredIntToNBT(coinInPedestal,0);
                                }
                            }
                        }*/
                    }
                }
            }
        }
    }

    //Can Pump Block, but just reusing the quarry method here
    @Override
    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos, PlayerEntity player)
    {
        World world = pedestal.getLevel();
        BlockPos blockToPumpPos = new BlockPos(blockToMinePos.getX(), blockToMinePos.getY(), blockToMinePos.getZ());

        return canPlaceFluidBlock(world, blockToPumpPos);
    }
    @Override
    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos)
    {
        World world = pedestal.getLevel();
        BlockPos blockToPumpPos = new BlockPos(blockToMinePos.getX(), blockToMinePos.getY(), blockToMinePos.getZ());

        return canPlaceFluidBlock(world, blockToPumpPos);
    }

    public boolean canPlaceFluidBlock(World world, BlockPos targetBlockPos)
    {
        BlockState targetFluidState = world.getBlockState(targetBlockPos);
        Block targetFluidBlock = targetFluidState.getBlock();

        if(targetFluidBlock.equals(Blocks.AIR))
        {
            return true;
        }
        else if (targetFluidBlock instanceof FlowingFluidBlock)
        {
            if (targetFluidState.get(FlowingFluidBlock.LEVEL) != 0)
            {
                return true;
            }
        }

        return false;
    }

    //https://github.com/BluSunrize/ImmersiveEngineering/blob/1.16/src/main/java/blusunrize/immersiveengineering/common/blocks/metal/FluidPlacerTileEntity.java#L102
    public boolean placeFluid(PedestalTileEntity pedestal, FakePlayer player, BlockPos targetBlock, FluidStack fluidIn, boolean simulate)
    {
        World world = pedestal.getLevel();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();

        if(removeFluid(pedestal, coinInPedestal,FluidAttributes.BUCKET_VOLUME,true))
        {
            FluidStack fluidToBucket = new FluidStack(fluidIn.getFluid(),FluidAttributes.BUCKET_VOLUME,fluidIn.getTag());
            Item bucket = fluidToBucket.getFluid().getFilledBucket();

            if(bucket instanceof BucketItem)
            {
                BucketItem bucketItem = (BucketItem)bucket;
                if(bucketItem.tryPlaceContainedLiquid(player,world,targetBlock,(BlockRayTraceResult)null))
                {
                    if(!simulate)
                    {
                        bucketItem.onLiquidPlaced(world,new ItemStack(bucketItem),targetBlock);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    public ItemStack getBucket(FluidStack fluidIn)
    {
        if(!fluidIn.isEmpty())
        {
            FluidStack fluidForBucket = new FluidStack(fluidIn.getFluid(),FluidAttributes.BUCKET_VOLUME,fluidIn.getTag());
            Item bucketItem = fluidForBucket.getFluid().getFilledBucket();
            if(bucketItem instanceof BucketItem)
            {
                if((BucketItem)bucketItem !=null || (BucketItem)bucketItem != Items.AIR)
                {
                    return new ItemStack((BucketItem)bucketItem);
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public void upgradeAction(PedestalTileEntity pedestal, BlockPos targetBlockPos, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {
        World world = pedestal.getLevel();
        BlockPos pedestalPos = pedestal.getBlockPos();
        FluidStack fluidInCoin = getFluidStored(coinInPedestal);

        if(!fluidInCoin.isEmpty())
        {
            if(canPlaceFluidBlock(world,targetBlockPos)) {
                if(removeFluid(pedestal, coinInPedestal,FluidAttributes.BUCKET_VOLUME,true))
                {
                    FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
                    fakePlayer.setPos(pedestalPos.getX(),pedestalPos.getY(),pedestalPos.getZ());
                    ItemStack getBucketOfFluid = getBucket(fluidInCoin);
                    fakePlayer.setHeldItem(Hand.MAIN_HAND,getBucketOfFluid);

                    if(world.isBlockModifiable(fakePlayer,targetBlockPos) && placeFluid(pedestal,fakePlayer,targetBlockPos,fluidInCoin,true))
                    {
                        removeFluid(pedestal, coinInPedestal,FluidAttributes.BUCKET_VOLUME,false);
                        placeFluid(pedestal,fakePlayer,targetBlockPos,fluidInCoin,false);
                        world.playSound((PlayerEntity) null, targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 0.5F, 1.0F);
                    }
                }
            }
        }
    }

    public int blocksToFillInArea(PedestalTileEntity pedestal, int width, int height)
    {
        World world = pedestal.getLevel();
        BlockPos pedestalPos = pedestal.getBlockPos();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        FluidStack fluidInCoin = getFluidStored(coinInPedestal);
        int validBlocks = 0;
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos negNums = getNegRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        BlockPos posNums = getBlockPosRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));

        for(int i=0;!resetCurrentPosInt(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));i++)
        {
            BlockPos targetBlockPos = getBlockPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
            BlockPos blockToFillPos = new BlockPos(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());
            FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
            fakePlayer.setPos(pedestalPos.getX(),pedestalPos.getY(),pedestalPos.getZ());

            if(world.isBlockModifiable(fakePlayer,blockToFillPos) && placeFluid(pedestal,fakePlayer,blockToFillPos,fluidInCoin,true))
            {
                validBlocks++;
            }
        }

        return validBlocks;
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        name.withStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.NIL_UUID);

        int s3 = getWidth(stack);
        int s4 = getHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + s4 + "";
        TranslationTextComponent area = new TranslationTextComponent(getDescriptionId() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getDescriptionId() + ".chat_areax");
        area.append(tr);
        area.append(areax.getString());
        area.append(trr);
        area.append(areax.getString());
        area.append(tr);
        area.withStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.NIL_UUID);

        FluidStack fluidStored = getFluidStored(stack);
        TranslationTextComponent fluidLabel = new TranslationTextComponent(getDescriptionId() + ".chat_fluidlabel");
        if(!fluidStored.isEmpty())
        {
            TranslationTextComponent fluid = new TranslationTextComponent(getDescriptionId() + ".chat_fluid");
            TranslationTextComponent fluidSplit = new TranslationTextComponent(getDescriptionId() + ".chat_fluidseperator");
            fluid.append("" + fluidStored.getDisplayName().getString() + "");
            fluid.append(fluidSplit.getString());
            fluid.append("" + fluidStored.getAmount() + "");
            fluid.append(fluidLabel.getString());
            fluid.withStyle(TextFormatting.BLUE);
            player.sendMessage(fluid,Util.NIL_UUID);
        }

        TranslationTextComponent btm = new TranslationTextComponent(getDescriptionId() + ".chat_btm");
        btm.append("" + blocksToFillInArea(pedestal,getWidth(pedestal.getCoinOnPedestal()),getHeight(pedestal.getCoinOnPedestal())) + "");
        btm.withStyle(TextFormatting.YELLOW);
        player.sendMessage(btm,Util.NIL_UUID);

        /*TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".chat_rate");
        rate.append("" +  getFluidTransferRate(stack) + "");
        rate.append(fluidLabel.getString());
        rate.withStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.NIL_UUID);*/

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".chat_speed");
        speed.append(getOperationSpeedString(stack));
        speed.withStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.NIL_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        TranslationTextComponent t = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        t.withStyle(TextFormatting.GOLD);
        tooltip.add(t);

        FluidStack fluidStored = getFluidStored(stack);
        TranslationTextComponent fluidLabel = new TranslationTextComponent(getDescriptionId() + ".chat_fluidlabel");
        if(!fluidStored.isEmpty())
        {
            TranslationTextComponent fluid = new TranslationTextComponent(getDescriptionId() + ".chat_fluid");
            TranslationTextComponent fluidSplit = new TranslationTextComponent(getDescriptionId() + ".chat_fluidseperator");
            fluid.append("" + fluidStored.getDisplayName().getString() + "");
            fluid.append(fluidSplit.getString());
            fluid.append("" + fluidStored.getAmount() + "");
            fluid.append(fluidLabel.getString());
            fluid.withStyle(TextFormatting.BLUE);
            tooltip.add(fluid);
        }

        int s3 = getWidth(stack);
        int s4 = getHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + s4 + "";
        TranslationTextComponent area = new TranslationTextComponent(getDescriptionId() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getDescriptionId() + ".tooltip_areax");
        area.append(tr);
        area.append(areax.getString());
        area.append(trr);
        area.append(areax.getString());
        area.append(tr);
        area.withStyle(TextFormatting.WHITE);
        tooltip.add(area);

        TranslationTextComponent fluidcapacity = new TranslationTextComponent(getDescriptionId() + ".tooltip_fluidcapacity");
        fluidcapacity.append(""+ getFluidbuffer(stack) +"");
        fluidcapacity.append(fluidLabel.getString());
        fluidcapacity.withStyle(TextFormatting.AQUA);
        tooltip.add(fluidcapacity);

        /*TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".tooltip_rate");
        rate.append("" + getFluidTransferRate(stack) + "");
        rate.append(fluidLabel.getString());
        rate.withStyle(TextFormatting.GRAY);
        tooltip.add(rate);*/

        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".tooltip_speed");
        speed.append(getOperationSpeedString(stack));
        speed.withStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item FLUIDDRAIN = new ItemUpgradeFluidDrain(new Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluiddrain"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FLUIDDRAIN);
    }


}
