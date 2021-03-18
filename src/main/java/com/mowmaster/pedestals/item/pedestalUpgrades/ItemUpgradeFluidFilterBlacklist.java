package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFluidFilterBlacklist extends ItemUpgradeBaseFluid
{
    public ItemUpgradeFluidFilterBlacklist(Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptOpSpeed() {
        return false;
    }

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return 0;
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{0,0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return 0;
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
        boolean returner = true;
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
                returner = false;
            }
        }

        return returner;
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

            if(!world.hasNeighborSignal(pedestalPos)) {
                if(hasFluidInCoin(coinInPedestal))
                {
                    upgradeActionSendFluid(pedestal);
                }
            }
        }
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

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        name.withStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.NIL_UUID);

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

        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".chat_rate");
        rate.append("" +  getFluidTransferRate(stack) + "");
        rate.append(fluidLabel.getString());
        rate.withStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.NIL_UUID);
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

        TranslationTextComponent fluidcapacity = new TranslationTextComponent(getDescriptionId() + ".tooltip_fluidcapacity");
        fluidcapacity.append(""+ getFluidbuffer(stack) +"");
        fluidcapacity.append(fluidLabel.getString());
        fluidcapacity.withStyle(TextFormatting.AQUA);
        tooltip.add(fluidcapacity);

        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".tooltip_rate");
        rate.append("" + getFluidTransferRate(stack) + "");
        rate.append(fluidLabel.getString());
        rate.withStyle(TextFormatting.GRAY);
        tooltip.add(rate);
    }

    public static final Item FLUIDFILTERBLACKLIST = new ItemUpgradeFluidFilterBlacklist(new Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluidfilterblacklist"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FLUIDFILTERBLACKLIST);
    }


}
