package com.mowmaster.pedestals.item.deprecated;

import com.mowmaster.pedestals.crafting.CalculateColor;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBaseFluid;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.util.PedestalFakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeFluidPumpFilter extends ItemUpgradeBaseFluid
{
    public ItemUpgradeFluidPumpFilter(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    public int getHeight(ItemStack stack)
    {
        return getRangeLarge(stack);
    }

    //Riped Straight from ItemUpgradePlacer
    public void placeBlock(PedestalTileEntity pedestal, BlockPos targetPos)
    {
        World world = pedestal.getWorld();
        BlockPos pedPos = pedestal.getPos();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        ItemStack coinOnPedestal = pedestal.getCoinOnPedestal();
        if(!itemInPedestal.isEmpty())
        {
            Block blockBelow = world.getBlockState(targetPos).getBlock();
            Item singleItemInPedestal = itemInPedestal.getItem();

            if(blockBelow.equals(Blocks.AIR) && !singleItemInPedestal.equals(Items.AIR)) {
                if(singleItemInPedestal instanceof BlockItem)
                {
                    if (((BlockItem) singleItemInPedestal).getBlock() instanceof Block)
                    {
                        if (!itemInPedestal.isEmpty() && itemInPedestal.getItem() instanceof BlockItem && ((BlockItem) itemInPedestal.getItem()).getBlock() instanceof Block) {

                            FakePlayer fakePlayer = new PedestalFakePlayer((ServerWorld) world,getPlayerFromCoin(coinOnPedestal),pedPos,itemInPedestal);
                            if(!fakePlayer.getPosition().equals(new BlockPos(pedPos.getX(), pedPos.getY(), pedPos.getZ()))) {fakePlayer.setPosition(pedPos.getX(), pedPos.getY(), pedPos.getZ());}

                            BlockItemUseContext blockContext = new BlockItemUseContext(fakePlayer, Hand.MAIN_HAND, itemInPedestal.copy(), new BlockRayTraceResult(Vector3d.ZERO, getPedestalFacing(world,pedPos), targetPos, false));

                            ActionResultType result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                            if (result == ActionResultType.CONSUME) {
                                this.removeFromPedestal(world,pedPos,1);
                                world.playSound((PlayerEntity) null, targetPos.getX(), targetPos.getY(), targetPos.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean doesFluidBucketMatch(ItemStack bucketIn, FluidStack fluidIn)
    {
        if(bucketIn.getItem() instanceof BucketItem)
        {
            BucketItem bI = (BucketItem) bucketIn.getItem();
            FluidStack fluidFromBucket = new FluidStack(bI.getFluid(), FluidAttributes.BUCKET_VOLUME,bI.getShareTag(bucketIn));
            if(fluidFromBucket.isFluidEqual(fluidIn))
            {
                return true;
            }
        }
        else if (FluidUtil.getFluidHandler(bucketIn).isPresent())
        {
            LazyOptional<IFluidHandlerItem> handler = FluidUtil.getFluidHandler(bucketIn);
            if(handler.resolve().get().getFluidInTank(0).isFluidEqual(fluidIn))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canRecieveFluid(World world, BlockPos posPedestal, FluidStack fluidIncoming)
    {
        boolean returner = false;
        BlockPos posInventory = getPosOfBlockBelow(world, posPedestal, 1);

        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posPedestal),true);
        if(cap.isPresent())
        {
            IItemHandler handler = cap.orElse(null);
            if(handler != null)
            {
                int range = handler.getSlots();

                ItemStack itemFromInv = ItemStack.EMPTY;
                itemFromInv = IntStream.range(0,range)//Int Range
                        .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                        .filter(itemStack -> doesFluidBucketMatch(itemStack,fluidIncoming))
                        .findFirst().orElse(ItemStack.EMPTY);

                if(!itemFromInv.isEmpty())
                {
                    returner = true;
                }
            }
        }

        return returner;
    }

    public boolean canPumpFluid(World world, BlockPos posPedestal, FluidStack fluidIncoming)
    {
        boolean returner = false;
        if(world.getTileEntity(posPedestal) instanceof PedestalTileEntity)
        {
            PedestalTileEntity pedestal = (PedestalTileEntity)world.getTileEntity(posPedestal);
            ItemStack coin = pedestal.getCoinOnPedestal();
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(coin);
            if(!(stackCurrent.size()>0))
            {
                stackCurrent = buildFilterQueue(pedestal);
                writeFilterQueueToNBT(coin,stackCurrent);
            }

            int range = stackCurrent.size();

            ItemStack itemFromInv = ItemStack.EMPTY;
            itemFromInv = IntStream.range(0,range)//Int Range
                    .mapToObj((stackCurrent)::get)//Function being applied to each interval
                    .filter(itemStack -> doesFluidBucketMatch(itemStack,fluidIncoming))
                    .findFirst().orElse(ItemStack.EMPTY);

            if(!itemFromInv.isEmpty())
            {
                returner = true;
            }
        }

        return returner;
    }

    @Override
    public void onPedestalNeighborChanged(PedestalTileEntity pedestal) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<ItemStack> stackIn = buildFilterQueue(pedestal);
        if(filterQueueSize(coin)>0)
        {
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(coin);
            if(!doesFilterAndQueueMatch(stackIn,stackCurrent))
            {
                writeFilterQueueToNBT(coin,stackIn);
            }
        }
        else
        {
            writeFilterQueueToNBT(coin,stackIn);
        }
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
            ItemStack coin = pedestal.getCoinOnPedestal();
            int width = getWidth(pedestal.getCoinOnPedestal());
            int widdth = (width*2)+1;
            int height = getHeight(pedestal.getCoinOnPedestal());
            int amount = workQueueSize(coin);
            int area = Math.multiplyExact(Math.multiplyExact(widdth,widdth),height);
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
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int getMaxFluidValue = getFluidbuffer(coinInPedestal);
            if(!hasMaxFluidSet(coinInPedestal) || readMaxFluidFromNBT(coinInPedestal) != getMaxFluidValue) {setMaxFluid(coinInPedestal, getMaxFluidValue);}

            int speed = getOperationSpeed(coinInPedestal);

            if(!pedestal.isPedestalBlockPowered(world,pedestalPos)) {
                if(hasFluidInCoin(coinInPedestal) && world.getGameTime() % speed == 0)
                {
                    upgradeActionSendFluid(pedestal);
                }


                int rangeWidth = getWidth(coinInPedestal);
                int rangeHeight = getHeight(coinInPedestal);

                BlockState pedestalState = world.getBlockState(pedestalPos);
                Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
                BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
                BlockPos posNums = getPosRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));

                if(world.isAreaLoaded(negNums,posNums))
                {
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
                            //Check if we can even insert a blocks worth of fluid
                            if(availableFluidSpaceInCoin(coinInPedestal) >= FluidAttributes.BUCKET_VOLUME || getFluidStored(coinInPedestal).isEmpty())
                            {
                                List<BlockPos> workQueue = readWorkQueueFromNBT(coinInPedestal);
                                if (world.getGameTime() % speed == 0) {
                                    for(int i = 0;i< workQueue.size(); i++)
                                    {
                                        BlockPos targetPos = workQueue.get(i);
                                        BlockPos blockToPumpPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                        BlockState targetFluidState = world.getBlockState(blockToPumpPos);
                                        Block targetFluidBlock = targetFluidState.getBlock();
                                        if(canMineBlock(pedestal,blockToPumpPos))
                                        {
                                            workQueue.remove(i);
                                            writeWorkQueueToNBT(coinInPedestal,workQueue);
                                            upgradeAction(pedestal, targetPos, itemInPedestal, coinInPedestal);
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
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal, BlockPos targetPos, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        BlockState targetFluidState = world.getBlockState(targetPos);
        Block targetFluidBlock = targetFluidState.getBlock();
        FluidStack fluidToStore = FluidStack.EMPTY;
        if (targetFluidBlock instanceof FlowingFluidBlock)
        {
            if (targetFluidState.get(FlowingFluidBlock.LEVEL) == 0) {
                Fluid fluid = ((FlowingFluidBlock) targetFluidBlock).getFluid();
                FluidStack fluidToPickup = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
                if(canAddFluidToCoin(pedestal,coinInPedestal,fluidToPickup))
                {
                    fluidToStore = fluidToPickup.copy();
                    if(!fluidToStore.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToStore,true))
                    {
                        if(canPumpFluid(world,pedestalPos,fluidToStore))
                        {
                            world.setBlockState(targetPos, Blocks.AIR.getDefaultState(), 11);
                            addFluid(pedestal,coinInPedestal,fluidToStore,false);
                            if(itemInPedestal.isEmpty())
                            {
                                int[] rgb = CalculateColor.getRGBColorFromInt(fluidToStore.getFluid().getAttributes().getColor());
                                if(!pedestal.hasParticleDiffuser())PacketHandler.sendToNearby(world,pedestalPos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR_CENTERED,targetPos.getX(),targetPos.getY(),targetPos.getZ(),rgb[0],rgb[1],rgb[2]));

                            }
                            else {placeBlock(pedestal,targetPos);}
                        }
                    }
                }
            }
        }
        else if (targetFluidBlock instanceof IFluidBlock) {
            IFluidBlock fluidBlock = (IFluidBlock) targetFluidBlock;

            if (fluidBlock.canDrain(world, targetPos)) {
                fluidToStore =  fluidBlock.drain(world, targetPos, IFluidHandler.FluidAction.SIMULATE);
                if(!fluidToStore.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToStore,true))
                {
                    if(canPumpFluid(world,pedestalPos,fluidToStore))
                    {
                        fluidToStore =  fluidBlock.drain(world, targetPos, IFluidHandler.FluidAction.EXECUTE);
                        addFluid(pedestal,coinInPedestal,fluidToStore,false);
                        if(itemInPedestal.isEmpty())
                        {
                            int[] rgb = CalculateColor.getRGBColorFromInt(fluidToStore.getFluid().getAttributes().getColor());
                            if(!pedestal.hasParticleDiffuser())PacketHandler.sendToNearby(world,pedestalPos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR_CENTERED,targetPos.getX(),targetPos.getY(),targetPos.getZ(),rgb[0],rgb[1],rgb[2]));

                        }
                        else {placeBlock(pedestal,targetPos);}
                    }
                }
            }
        }
    }

    //Can Pump Block, but just reusing the quarry method here
    @Override
    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos, PlayerEntity player)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        BlockPos blockToPumpPos = new BlockPos(blockToMinePos.getX(), blockToMinePos.getY(), blockToMinePos.getZ());
        BlockState targetFluidState = world.getBlockState(blockToPumpPos);
        Block targetFluidBlock = targetFluidState.getBlock();
        FluidStack fluidToStore = (targetFluidBlock instanceof FlowingFluidBlock && targetFluidState.get(FlowingFluidBlock.LEVEL) == 0)?(new FluidStack(((FlowingFluidBlock) targetFluidBlock).getFluid(), FluidAttributes.BUCKET_VOLUME)):((targetFluidBlock instanceof IFluidBlock)?(((IFluidBlock) targetFluidBlock).drain(world, blockToMinePos, IFluidHandler.FluidAction.SIMULATE)):(FluidStack.EMPTY));

        if((targetFluidBlock instanceof FlowingFluidBlock && targetFluidState.get(FlowingFluidBlock.LEVEL) == 0
                || targetFluidBlock instanceof IFluidBlock) && canPumpFluid(world,pedestalPos,fluidToStore))
        {
            return true;
        }

        return false;
    }
    @Override
    public boolean canMineBlock(PedestalTileEntity pedestal, BlockPos blockToMinePos)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        BlockPos blockToPumpPos = new BlockPos(blockToMinePos.getX(), blockToMinePos.getY(), blockToMinePos.getZ());
        BlockState targetFluidState = world.getBlockState(blockToPumpPos);
        Block targetFluidBlock = targetFluidState.getBlock();
        FluidStack fluidToStore = (targetFluidBlock instanceof FlowingFluidBlock && targetFluidState.get(FlowingFluidBlock.LEVEL) == 0)?(new FluidStack(((FlowingFluidBlock) targetFluidBlock).getFluid(), FluidAttributes.BUCKET_VOLUME)):((targetFluidBlock instanceof IFluidBlock)?(((IFluidBlock) targetFluidBlock).drain(world, blockToMinePos, IFluidHandler.FluidAction.SIMULATE)):(FluidStack.EMPTY));

        if((targetFluidBlock instanceof FlowingFluidBlock && targetFluidState.get(FlowingFluidBlock.LEVEL) == 0
                || targetFluidBlock instanceof IFluidBlock) && canPumpFluid(world,pedestalPos,fluidToStore))
        {
            return true;
        }

        return false;
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        int s3 = getWidth(stack);
        int s4 = getHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + s4 + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.DUMMY_UUID);

        FluidStack fluidStored = getFluidStored(stack);
        TranslationTextComponent fluidLabel = new TranslationTextComponent(getTranslationKey() + ".chat_fluidlabel");
        if(!fluidStored.isEmpty())
        {
            TranslationTextComponent fluid = new TranslationTextComponent(getTranslationKey() + ".chat_fluid");
            TranslationTextComponent fluidSplit = new TranslationTextComponent(getTranslationKey() + ".chat_fluidseperator");
            fluid.appendString("" + fluidStored.getDisplayName().getString() + "");
            fluid.appendString(fluidSplit.getString());
            fluid.appendString("" + fluidStored.getAmount() + "");
            fluid.appendString(fluidLabel.getString());
            fluid.mergeStyle(TextFormatting.BLUE);
            player.sendMessage(fluid,Util.DUMMY_UUID);
        }

        TranslationTextComponent btm = new TranslationTextComponent(getTranslationKey() + ".chat_btm");
        btm.appendString("" + ((workQueueSize(stack)>0)?(workQueueSize(stack)):(0)) + "");
        btm.mergeStyle(TextFormatting.YELLOW);
        player.sendMessage(btm,Util.DUMMY_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString("" +  getFluidTransferRate(stack) + "");
        rate.appendString(fluidLabel.getString());
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        FluidStack fluidStored = getFluidStored(stack);
        TranslationTextComponent fluidLabel = new TranslationTextComponent(getTranslationKey() + ".chat_fluidlabel");
        if(!fluidStored.isEmpty())
        {
            TranslationTextComponent fluid = new TranslationTextComponent(getTranslationKey() + ".chat_fluid");
            TranslationTextComponent fluidSplit = new TranslationTextComponent(getTranslationKey() + ".chat_fluidseperator");
            fluid.appendString("" + fluidStored.getDisplayName().getString() + "");
            fluid.appendString(fluidSplit.getString());
            fluid.appendString("" + fluidStored.getAmount() + "");
            fluid.appendString(fluidLabel.getString());
            fluid.mergeStyle(TextFormatting.BLUE);
            tooltip.add(fluid);
        }

        int s3 = getWidth(stack);
        int s4 = getHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + s4 + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        tooltip.add(area);

        TranslationTextComponent fluidcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_fluidcapacity");
        fluidcapacity.appendString(""+ getFluidbuffer(stack) +"");
        fluidcapacity.appendString(fluidLabel.getString());
        fluidcapacity.mergeStyle(TextFormatting.AQUA);
        tooltip.add(fluidcapacity);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + getFluidTransferRate(stack) + "");
        rate.appendString(fluidLabel.getString());
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item FLUIDPUMPFILTER = new ItemUpgradeFluidPumpFilter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluidpumpfilter"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FLUIDPUMPFILTER);
    }


}
