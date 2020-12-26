package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.crafting.CalculateColor;
import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.ItemEntity;
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
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeFluidPump extends ItemUpgradeBaseFluid
{
    public ItemUpgradeFluidPump(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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

    //Riped Straight from ItemUpgradePlacer
    public void placeBlock(World world, BlockPos pedPos, BlockPos targetPos, ItemStack itemInPedestal, ItemStack coinOnPedestal)
    {
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
                            Block block = ((BlockItem) itemInPedestal.getItem()).getBlock();

                            FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinOnPedestal),"[Pedestals]"));
                            //FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(world.getServer().func_241755_D_());
                            fakePlayer.setPosition(pedPos.getX(),pedPos.getY(),pedPos.getZ());

                            //BlockItemUseContext blockContext = new BlockItemUseContext(fakePlayer, Hand.MAIN_HAND, itemInPedestal, new BlockRayTraceResult(Vector3d.ZERO, getPedestalFacing(world,posOfPedestal), blockPosBelow, false));
                            BlockItemUseContext blockContext = new BlockItemUseContext(fakePlayer, Hand.MAIN_HAND, itemInPedestal.copy(), new BlockRayTraceResult(Vector3d.ZERO, getPedestalFacing(world,pedPos), targetPos, false));

                            /*ActionResultType result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                            if (result == ActionResultType.CONSUME) {
                                this.removeFromPedestal(world,posOfPedestal,1);
                                world.playSound((PlayerEntity) null, blockPosBelow.getX(), blockPosBelow.getY(), blockPosBelow.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            }*/

                            ActionResultType result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                            if (result == ActionResultType.CONSUME) {
                                this.removeFromPedestal(world,pedPos,1);
                                world.playSound((PlayerEntity) null, targetPos.getX(), targetPos.getY(), targetPos.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            }
                            /*world.setBlockState(blockPosBelow,block.getDefaultState());
                            this.removeFromPedestal(world,posOfPedestal,1);
                            world.playSound((PlayerEntity) null, blockPosBelow.getX(), blockPosBelow.getY(), blockPosBelow.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);*/
                        }
                    }
                }
            }
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
            int width = getWidth(pedestal.getCoinOnPedestal());
            int height = getHeight(pedestal.getCoinOnPedestal());
            int amount = blocksToPumpInArea(pedestal,width,height);
            int area = Math.multiplyExact(Math.multiplyExact(amount,amount),height);
            if(amount>0)
            {
                float f = (float)amount/(float)area;
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            int getMaxFluidValue = getFluidbuffer(coinInPedestal);
            if(!hasMaxFluidSet(coinInPedestal) || readMaxFluidFromNBT(coinInPedestal) != getMaxFluidValue) {setMaxFluid(coinInPedestal, getMaxFluidValue);}

            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos)) {
                if (world.getGameTime() % speed == 0) {
                    if(hasFluidInCoin(coinInPedestal))
                    {
                        upgradeActionSendFluid(pedestal);
                    }
                }
            }


            //Check if we can even insert a blocks worth of fluid
            if(availableFluidSpaceInCoin(coinInPedestal) >= FluidAttributes.BUCKET_VOLUME || getFluidStored(coinInPedestal).isEmpty())
            {
                int rangeWidth = getWidth(coinInPedestal);
                int rangeHeight = getHeight(coinInPedestal);

                BlockState pedestalState = world.getBlockState(pedestalPos);
                Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
                BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
                BlockPos posNums = getPosRangePosEntity(world,pedestalPos,rangeWidth,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(rangeHeight-1):(rangeHeight));
                FluidStack fluidInCoin = getFluidStored(coinInPedestal);

                /*int rangeWidth = getWidth(coinInPedestal);
                int rangeHeight = getHeight(coinInPedestal)+1;
                BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,rangeHeight);
                BlockPos posNums = getPosRangePosEntity(world,pedestalPos,rangeWidth,rangeHeight);*/
                if(world.isAreaLoaded(negNums,posNums))
                {
                    if(!world.isBlockPowered(pedestalPos)) {
                        if(blocksToPumpInArea(pedestal,rangeWidth,rangeHeight) > 0)
                        {
                            if (world.getGameTime() % speed == 0) {
                                int currentPosition = 0;
                                for(currentPosition = getStoredInt(coinInPedestal);!resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));currentPosition++)
                                {
                                    BlockPos targetPos = getPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                    BlockPos blockToPumpPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                    BlockState targetFluidState = world.getBlockState(blockToPumpPos);
                                    Block targetFluidBlock = targetFluidState.getBlock();
                                    if(targetFluidBlock instanceof FlowingFluidBlock && targetFluidState.get(FlowingFluidBlock.LEVEL) == 0
                                            || targetFluidBlock instanceof IFluidBlock)
                                    {
                                        writeStoredIntToNBT(coinInPedestal,currentPosition);
                                        break;
                                    }
                                }
                                BlockPos targetPos = getPosOfNextBlock(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
                                BlockState targetBlock = world.getBlockState(targetPos);
                                upgradeAction(pedestal, targetPos, itemInPedestal, coinInPedestal);
                                if(resetCurrentPosInt(currentPosition,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums)))
                                {
                                    writeStoredIntToNBT(coinInPedestal,0);
                                }
                            }
                        }
                        /*if (world.getGameTime() % speed == 0) {

                            int currentPosition = pedestal.getStoredValueForUpgrades();

                            BlockPos targetPos = getPosOfNextBlock(currentPosition,negNums,posNums);
                            //Added for testing
                            //System.out.println("CURRENT POS: "+currentPosition);
                            //PacketHandler.sendToNearby(world,pedestalPos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR_CENTERED,targetPos.getX(),targetPos.getY(),targetPos.getZ(),255,164,0));
                            BlockState targetBlock = world.getBlockState(targetPos);

                            upgradeAction(pedestal,targetPos, itemInPedestal, coinInPedestal);

                            pedestal.setStoredValueForUpgrades(currentPosition+1);
                            if(resetCurrentPosInt(currentPosition+1,negNums,posNums))
                            {
                                pedestal.setStoredValueForUpgrades(0);
                            }
                        }*/
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
        if (targetFluidBlock instanceof FlowingFluidBlock && targetFluidState.get(FlowingFluidBlock.LEVEL) == 0) {
            Fluid fluid = ((FlowingFluidBlock) targetFluidBlock).getFluid();
            FluidStack fluidToPickup = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
            if(canAddFluidToCoin(pedestal,coinInPedestal,fluidToPickup))
            {
                fluidToStore = fluidToPickup.copy();
                if(!fluidToStore.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToStore,true))
                {
                    world.setBlockState(targetPos, Blocks.AIR.getDefaultState(), 11);
                    addFluid(pedestal,coinInPedestal,fluidToStore,false);
                    if(itemInPedestal.isEmpty())
                    {
                        int[] rgb = CalculateColor.getRGBColorFromInt(fluidToStore.getFluid().getAttributes().getColor());
                        PacketHandler.sendToNearby(world,pedestalPos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR_CENTERED,targetPos.getX(),targetPos.getY(),targetPos.getZ(),rgb[0],rgb[1],rgb[2]));
                    }
                    else {placeBlock(world,pedestalPos,targetPos,itemInPedestal,coinInPedestal);}
                }
            }
        }
        else if (targetFluidBlock instanceof IFluidBlock) {
            IFluidBlock fluidBlock = (IFluidBlock) targetFluidBlock;

            if (fluidBlock.canDrain(world, targetPos)) {
                fluidToStore =  fluidBlock.drain(world, targetPos, IFluidHandler.FluidAction.SIMULATE);
                if(!fluidToStore.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToStore,true))
                {
                    fluidToStore =  fluidBlock.drain(world, targetPos, IFluidHandler.FluidAction.EXECUTE);
                    addFluid(pedestal,coinInPedestal,fluidToStore,false);
                    if(itemInPedestal.isEmpty())
                    {
                        int[] rgb = CalculateColor.getRGBColorFromInt(fluidToStore.getFluid().getAttributes().getColor());
                        PacketHandler.sendToNearby(world,pedestalPos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR_CENTERED,targetPos.getX(),targetPos.getY(),targetPos.getZ(),rgb[0],rgb[1],rgb[2]));

                    }
                    else {placeBlock(world,pedestalPos,targetPos,itemInPedestal,coinInPedestal);}
                }
            }
        }
    }

    public int blocksToPumpInArea(PedestalTileEntity pedestal, int width, int height)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        int validBlocks = 0;
        BlockState pedestalState = world.getBlockState(pedestalPos);
        Direction enumfacing = (pedestalState.hasProperty(FACING))?(pedestalState.get(FACING)):(Direction.UP);
        BlockPos negNums = getNegRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));
        BlockPos posNums = getPosRangePosEntity(world,pedestalPos,width,(enumfacing == Direction.NORTH || enumfacing == Direction.EAST || enumfacing == Direction.SOUTH || enumfacing == Direction.WEST)?(height-1):(height));

        for(int i=0;!resetCurrentPosInt(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));i++)
        {
            BlockPos targetPos = getPosOfNextBlock(i,(enumfacing == Direction.DOWN)?(negNums.add(0,1,0)):(negNums),(enumfacing != Direction.UP)?(posNums.add(0,1,0)):(posNums));
            BlockPos blockToPumpPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            BlockState targetFluidState = world.getBlockState(blockToPumpPos);
            Block targetFluidBlock = targetFluidState.getBlock();

            if(targetFluidBlock instanceof FlowingFluidBlock && targetFluidState.get(FlowingFluidBlock.LEVEL) == 0
                    || targetFluidBlock instanceof IFluidBlock)
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

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

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
        btm.appendString("" + blocksToPumpInArea(pedestal,getWidth(pedestal.getCoinOnPedestal()),getHeight(pedestal.getCoinOnPedestal())) + "");
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

    public static final Item FLUIDPUMP = new ItemUpgradeFluidPump(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluidpump"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FLUIDPUMP);
    }


}
