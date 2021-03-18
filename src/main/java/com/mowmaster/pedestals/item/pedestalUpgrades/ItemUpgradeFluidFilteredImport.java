package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFluidFilteredImport extends ItemUpgradeBaseFluid
{
    public ItemUpgradeFluidFilteredImport(Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    @Override
    public boolean canSendItem(PedestalTileEntity tile)
    {
        if(tile !=null)
        {
            FluidStack fluidInItem = getFluidInItem(tile.getItemInPedestal());
            return fluidInItem.isEmpty() || !canSendAnyFluid(tile,fluidInItem);
        }
        return true;
    }

    @Override
    public int canAcceptCount(World world, BlockPos pos,ItemStack inPedestal, ItemStack itemStackIncoming) {

        //If incoming item has a fluid then set max stack to 1, if the pedestal has an item then 0, else allow normal transferring
        return (!getFluidInItem(itemStackIncoming).isEmpty())?(1):((inPedestal.isEmpty())?(itemStackIncoming.getMaxStackSize()):(0));
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

    public boolean canSendAnyFluid(PedestalTileEntity pedestal, FluidStack incomingFluid)
    {
        World world = pedestal.getLevel();
        int locations = pedestal.getNumberOfStoredLocations();
        ItemStack itemFromInv = ItemStack.EMPTY;
        for(int i=0; i<locations;i++)
        {
            if(canRecieveFluid(world,pedestal.getStoredPositionAt(i),incomingFluid))
            {
                return true;
            }
        }

        return false;
    }

    public FluidStack getFluidInItem(ItemStack itemInPedestal)
    {
        //TODO: Maybe have a fluid recipe thingy for people to add other tiems that 'contain' fluids??? have an input, fluid, amount, and output for the recipe???
        if(FluidUtil.getFluidHandler(itemInPedestal).isPresent())
        {
            FluidStack fluidInItem = FluidUtil.getFluidContained(itemInPedestal).orElse(FluidStack.EMPTY);
            return fluidInItem;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean canRecieveFluid(World world, BlockPos posPedestal, FluidStack fluidIncoming)
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

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isClientSide)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getBlockPos();

            int getMaxFluidValue = getFluidbuffer(coinInPedestal);
            if(!hasMaxFluidSet(coinInPedestal) || readMaxFluidFromNBT(coinInPedestal) != getMaxFluidValue) {setMaxFluid(coinInPedestal, getMaxFluidValue);}

            int speed = getOperationSpeed(coinInPedestal);
            if(!world.hasNeighborSignal(pedestalPos)) {

                if (world.getGameTime() % speed == 0) {
                    if(hasFluidInCoin(coinInPedestal))
                    {
                        upgradeActionSendFluid(pedestal);
                    }

                    upgradeActionItem(pedestal);
                    upgradeActionBlock(pedestal);
                }
            }
        }
    }

//https://github.com/mekanism/Mekanism/blob/be11c0df7d6ffece12da666b3100fc5e6d8ce0ab/src/main/java/mekanism/common/inventory/slot/IFluidHandlerSlot.java#L137
    public void upgradeActionItem(PedestalTileEntity pedestal)
    {
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();

        if(!itemInPedestal.isEmpty())
        {
            FluidStack fluidIn = getFluidInItem(itemInPedestal);
            if(!fluidIn.isEmpty())
            {
                if(canSendAnyFluid(pedestal,fluidIn))
                {
                    FluidStack fluidInCoin = getFluidInItem(coinInPedestal);

                    //https://github.com/mekanism/Mekanism/blob/be11c0df7d6ffece12da666b3100fc5e6d8ce0ab/src/main/java/mekanism/common/inventory/slot/IFluidHandlerSlot.java#L44
                    Optional<IFluidHandlerItem> fluidContainerItemIn = FluidUtil.getFluidHandler(itemInPedestal).resolve();
                    if(fluidContainerItemIn.isPresent())
                    {
                        IFluidHandlerItem fluidHandlerItem = fluidContainerItemIn.get();
                        int tanks = fluidContainerItemIn.get().getTanks();

                        if(tanks > 1)
                        {
                            if(!fluidInCoin.isEmpty())
                            {
                                //Default grab from first tank
                                FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(0);
                                int amountIn = fluidInTank.getAmount();
                                int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                                int rate = getFluidTransferRate(coinInPedestal);
                                int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                                int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                                {
                                    FluidStack estFluidToDrain = new FluidStack(fluidInTank,transferRate);
                                    FluidStack fluidToActuallyDrain = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                    if(!fluidInTank.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                    {
                                        FluidStack fluidDrained = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                        addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                        ItemStack returnerStack = fluidHandlerItem.getContainer();
                                        pedestal.removeItemOverride();
                                        pedestal.addItem(returnerStack);
                                    }
                                }
                            }
                            else
                            {
                                FluidStack fluidMatching = FluidStack.EMPTY;
                                fluidMatching = IntStream.range(0,tanks)//Int Range
                                        .mapToObj((fluidHandlerItem)::getFluidInTank)//Function being applied to each interval
                                        .filter(fluidStack -> fluidInCoin.isFluidEqual(fluidStack))
                                        .findFirst().orElse(FluidStack.EMPTY);

                                if(!fluidMatching.isEmpty())
                                {
                                    int amountIn = fluidMatching.getAmount();
                                    int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                                    int rate = getFluidTransferRate(coinInPedestal);
                                    int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                                    int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                    if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                                    {
                                        FluidStack estFluidToDrain = new FluidStack(fluidMatching,transferRate);
                                        FluidStack fluidToActuallyDrain = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                        if(!fluidMatching.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                        {
                                            FluidStack fluidDrained = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                            addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                            ItemStack returnerStack = fluidHandlerItem.getContainer();
                                            pedestal.removeItemOverride();
                                            pedestal.addItem(returnerStack);
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            //should i just set this to zero???
                            FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tanks-1);
                            if(fluidInCoin.isEmpty() || fluidInCoin.isFluidEqual(fluidInTank))
                            {
                                int amountIn = fluidInTank.getAmount();
                                int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                                int rate = getFluidTransferRate(coinInPedestal);
                                int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                                int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                                {
                                    FluidStack estFluidToDrain = new FluidStack(fluidInTank,transferRate);
                                    FluidStack fluidToActuallyDrain = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                    if(!fluidInTank.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                    {
                                        FluidStack fluidDrained = fluidHandlerItem.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                        addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                        ItemStack returnerStack = fluidHandlerItem.getContainer();
                                        pedestal.removeItemOverride();
                                        pedestal.addItem(returnerStack);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void upgradeActionBlock(PedestalTileEntity pedestal)
    {
        World world = pedestal.getLevel();
        BlockPos posPedestal = pedestal.getBlockPos();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();

        BlockPos posInventory = getBlockPosOfBlockBelow(world,posPedestal,1);
        ItemStack itemFromPedestal = ItemStack.EMPTY;

        LazyOptional<IFluidHandler> cap = findFluidHandlerAtPos(world,posInventory,getPedestalFacing(world, posPedestal),true);

        TileEntity invToPushTo = world.getTileEntity(posInventory);
        if(invToPushTo instanceof PedestalTileEntity) {
            itemFromPedestal = ItemStack.EMPTY;
        }
        else {
            if(cap.isPresent())
            {
                IFluidHandler handler = cap.orElse(null);
                if(handler != null)
                {
                    int tanks = handler.getTanks();
                    FluidStack fluidCheckedMatching = FluidStack.EMPTY;
                    fluidCheckedMatching = IntStream.range(0,tanks)//Int Range
                            .mapToObj((handler)::getFluidInTank)//Function being applied to each interval
                            .filter(fluidStack -> !fluidStack.isEmpty())
                            .findFirst().orElse(FluidStack.EMPTY);

                    if(!fluidCheckedMatching.isEmpty())
                    {
                        FluidStack fluidInCoin = getFluidStored(coinInPedestal);
                        if(tanks > 1)
                        {
                            if(!fluidInCoin.isEmpty())
                            {
                                //Default grab from first tank
                                FluidStack fluidInTank = handler.getFluidInTank(0);
                                if(canSendAnyFluid(pedestal,fluidInTank))
                                {
                                    int amountIn = fluidInTank.getAmount();
                                    int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                                    int rate = getFluidTransferRate(coinInPedestal);
                                    int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                                    int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                    if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                                    {
                                        FluidStack estFluidToDrain = new FluidStack(fluidInTank,transferRate);
                                        FluidStack fluidToActuallyDrain = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                        if(!fluidInTank.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                        {
                                            FluidStack fluidDrained = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                            addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                FluidStack fluidMatching = FluidStack.EMPTY;
                                fluidMatching = IntStream.range(0,tanks)//Int Range
                                        .mapToObj((handler)::getFluidInTank)//Function being applied to each interval
                                        .filter(fluidStack -> fluidInCoin.isFluidEqual(fluidStack))
                                        .findFirst().orElse(FluidStack.EMPTY);

                                if(!fluidMatching.isEmpty())
                                {
                                    if(canSendAnyFluid(pedestal,fluidMatching))
                                    {
                                        int amountIn = fluidMatching.getAmount();
                                        int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                                        int rate = getFluidTransferRate(coinInPedestal);
                                        int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                                        int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                        if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                                        {
                                            FluidStack estFluidToDrain = new FluidStack(fluidMatching,transferRate);
                                            FluidStack fluidToActuallyDrain = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                            if(!fluidMatching.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                            {
                                                FluidStack fluidDrained = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                                addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            //should i just set this to zero???
                            FluidStack fluidInTank = handler.getFluidInTank(tanks-1);
                            if(canSendAnyFluid(pedestal,fluidInTank))
                            {
                                if(fluidInCoin.isEmpty() || fluidInCoin.isFluidEqual(fluidInTank))
                                {
                                    int amountIn = fluidInTank.getAmount();
                                    int spaceInCoin = availableFluidSpaceInCoin(coinInPedestal);
                                    int rate = getFluidTransferRate(coinInPedestal);
                                    int actualCoinRate = (spaceInCoin>=rate)?(rate):(spaceInCoin);
                                    int transferRate = (amountIn>=actualCoinRate)?(actualCoinRate):(amountIn);

                                    if(spaceInCoin >= transferRate || getFluidStored(coinInPedestal).isEmpty())
                                    {
                                        FluidStack estFluidToDrain = new FluidStack(fluidInTank,transferRate);
                                        FluidStack fluidToActuallyDrain = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.SIMULATE);
                                        if(!fluidInTank.isEmpty() && addFluid(pedestal,coinInPedestal,fluidToActuallyDrain,true))
                                        {
                                            FluidStack fluidDrained = handler.drain(estFluidToDrain,IFluidHandler.FluidAction.EXECUTE);
                                            addFluid(pedestal,coinInPedestal,fluidDrained,false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
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

        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".tooltip_speed");
        speed.append(getOperationSpeedString(stack));
        speed.withStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item FLUIDFILTEREDIMPORT = new ItemUpgradeFluidFilteredImport(new Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluidfilteredimport"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FLUIDFILTEREDIMPORT);
    }


}
