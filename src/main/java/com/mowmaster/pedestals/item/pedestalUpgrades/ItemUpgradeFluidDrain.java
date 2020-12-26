package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.crafting.CalculateColor;
import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
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
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFluidDrain extends ItemUpgradeBaseFluid
{
    public ItemUpgradeFluidDrain(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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

            //Check if we can even have a blocks worth of fluid to place
            if(getFluidStored(coinInPedestal).getAmount() >= FluidAttributes.BUCKET_VOLUME)
            {
                int rangeWidth = getWidth(coinInPedestal);
                int rangeHeight = getHeight(coinInPedestal)+1;
                BlockPos negNums = getNegRangePosEntity(world,pedestalPos,rangeWidth,rangeHeight);
                BlockPos posNums = getPosRangePosEntity(world,pedestalPos,rangeWidth,rangeHeight);
                if(world.isAreaLoaded(negNums,posNums))
                {
                    if(!world.isBlockPowered(pedestalPos)) {
                        if (world.getGameTime() % speed == 0) {

                            int currentPosition = pedestal.getStoredValueForUpgrades();

                            BlockPos targetPos = getPosOfNextBlock(currentPosition,negNums,posNums);
                            //Added for testing
                            //System.out.println("CURRENT POS: "+currentPosition);
                            //PacketHandler.sendToNearby(world,pedestalPos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR_CENTERED,targetPos.getX(),targetPos.getY(),targetPos.getZ(),255,164,0));
                            BlockState targetBlock = world.getBlockState(targetPos);

                            upgradeAction(pedestal, targetPos, itemInPedestal, coinInPedestal);

                            pedestal.setStoredValueForUpgrades(currentPosition+1);
                            if(resetCurrentPosInt(currentPosition+1,negNums,posNums))
                            {
                                pedestal.setStoredValueForUpgrades(0);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean canPlaceFluidBlock(World world, BlockPos targetPos)
    {
        BlockState targetFluidState = world.getBlockState(targetPos);
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
        World world = pedestal.getWorld();
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

    public void upgradeAction(PedestalTileEntity pedestal, BlockPos targetPos, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        FluidStack fluidInCoin = getFluidStored(coinInPedestal);

        if(!fluidInCoin.isEmpty())
        {
            if(canPlaceFluidBlock(world,targetPos)) {
                if(removeFluid(pedestal, coinInPedestal,FluidAttributes.BUCKET_VOLUME,true))
                {
                    //fluidInCoin.getFluid().getAttributes().canBePlacedInWorld(world,targetPos,fluidInCoin)
                    FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
                    fakePlayer.setPosition(pedestalPos.getX(),pedestalPos.getY(),pedestalPos.getZ());
                    ItemStack getBucketOfFluid = getBucket(fluidInCoin);
                    fakePlayer.setHeldItem(Hand.MAIN_HAND,getBucketOfFluid);

                    if(world.isBlockModifiable(fakePlayer,targetPos))
                    {
                        if(placeFluid(pedestal,fakePlayer,targetPos,fluidInCoin,true))
                        {
                            removeFluid(pedestal, coinInPedestal,FluidAttributes.BUCKET_VOLUME,false);
                            placeFluid(pedestal,fakePlayer,targetPos,fluidInCoin,false);
                            world.playSound((PlayerEntity) null, targetPos.getX(), targetPos.getY(), targetPos.getZ(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 0.5F, 1.0F);
                        }
                    }

                    /*BlockItemUseContext blockContext = new BlockItemUseContext(fakePlayer, Hand.MAIN_HAND, itemInPedestal.copy(), new BlockRayTraceResult(Vector3d.ZERO, getPedestalFacing(world,pedestalPos), targetPos, false));

                    ActionResultType result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                    if (result == ActionResultType.CONSUME) {
                        removeFluid(coinInPedestal,FluidAttributes.BUCKET_VOLUME,false);
                        placeFluid(world,targetPos,fluidInCoin.copy(),coinInPedestal);
                        world.playSound((PlayerEntity) null, targetPos.getX(), targetPos.getY(), targetPos.getZ(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 0.5F, 1.0F);
                    }*/
                }
            }
        }
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

    public static final Item FLUIDDRAIN = new ItemUpgradeFluidDrain(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluiddrain"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FLUIDDRAIN);
    }


}
