package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFluidExport extends ItemUpgradeBaseFluid
{
    public ItemUpgradeFluidExport(Properties builder) {super(builder.tab(PEDESTALS_TAB));}

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
            Optional<IFluidHandlerItem> fluidContainerItemIn = FluidUtil.getFluidHandler(tile.getItemInPedestal()).resolve();
            if(fluidContainerItemIn.isPresent())
            {
                int capacity = fluidContainerItemIn.get().getTankCapacity(0);
                int currentAmount = fluidContainerItemIn.get().getFluidInTank(0).getAmount();
                return capacity == currentAmount;
            }
        }
        return true;
    }

    @Override
    public int canAcceptCount(World world, BlockPos pos,ItemStack inPedestal, ItemStack itemStackIncoming) {

        //If incoming item has a fluid then set max stack to 1, if the pedestal has an item then 0, else allow normal transferring
        return (inPedestal.isEmpty())?((getFluidInItem(itemStackIncoming).isEmpty())?(1):(itemStackIncoming.getMaxStackSize())):(0);
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
            FluidStack fluidInCoin = getFluidStored(coinInPedestal);
            if(!fluidInCoin.isEmpty())
            {
                //https://github.com/mekanism/Mekanism/blob/be11c0df7d6ffece12da666b3100fc5e6d8ce0ab/src/main/java/mekanism/common/inventory/slot/IFluidHandlerSlot.java#L44
                Optional<IFluidHandlerItem> fluidContainerItemIn = FluidUtil.getFluidHandler(itemInPedestal).resolve();
                if(fluidContainerItemIn.isPresent())
                {
                    IFluidHandlerItem fluidHandlerItem = fluidContainerItemIn.get();
                    int tanks = fluidContainerItemIn.get().getTanks();

                    if(tanks > 1)
                    {
                        FluidStack fluidMatching = FluidStack.EMPTY;
                        fluidMatching = IntStream.range(0,tanks)//Int Range
                                .mapToObj((fluidHandlerItem)::getFluidInTank)//Function being applied to each interval
                                .filter(fluidStack -> fluidInCoin.isFluidEqual(fluidStack))
                                .findFirst().orElse(FluidStack.EMPTY);

                        if(!fluidMatching.isEmpty())
                        {
                            FluidStack matchedFluid = fluidMatching;

                            int value = 0;
                            for(int location=0;location<tanks;location++)
                            {
                                if(fluidHandlerItem.getFluidInTank(location).isFluidEqual(matchedFluid))
                                {
                                    value = location;
                                    break;
                                }
                            }
                            //So we need to figure out how much we can put from the coin and into the tank
                            int getTankCapacity = fluidHandlerItem.getTankCapacity(value);
                            int tankCurrentlyStored = fluidMatching.getAmount();
                            int spaceInTank = getTankCapacity-tankCurrentlyStored;
                            int amountInCoin = fluidInCoin.getAmount();

                            int rate = getFluidTransferRate(coinInPedestal);
                            int actualCoinRate = (spaceInTank>=rate)?(rate):(spaceInTank);
                            int transferRate = (amountInCoin>=actualCoinRate)?(actualCoinRate):(amountInCoin);

                            if(spaceInTank >= transferRate)
                            {
                                FluidStack estFluidToFill = new FluidStack(fluidMatching,transferRate);
                                int fluidToActuallyFill = fluidHandlerItem.fill(estFluidToFill,IFluidHandler.FluidAction.SIMULATE);
                                if(!fluidMatching.isEmpty() && removeFluid(pedestal,coinInPedestal,fluidToActuallyFill,true))
                                {
                                    estFluidToFill = new FluidStack(fluidMatching,fluidToActuallyFill);
                                    int fluidDrained = fluidHandlerItem.fill(estFluidToFill,IFluidHandler.FluidAction.SIMULATE);
                                    ItemStack returnerStack = fluidHandlerItem.getContainer();
                                    if(!returnerStack.isEmpty())
                                    {
                                        removeFluid(pedestal,coinInPedestal,fluidDrained,false);
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
                        if(!fluidInCoin.isEmpty() || fluidInCoin.isFluidEqual(fluidInTank))
                        {
                            int getTankCapacity = fluidHandlerItem.getTankCapacity(tanks-1);
                            int tankCurrentlyStored = fluidInTank.getAmount();
                            int spaceInTank = getTankCapacity-tankCurrentlyStored;
                            int amountInCoin = fluidInCoin.getAmount();

                            int rate = getFluidTransferRate(coinInPedestal);
                            int actualCoinRate = (spaceInTank>=rate)?(rate):(spaceInTank);
                            int transferRate = (amountInCoin>=actualCoinRate)?(actualCoinRate):(amountInCoin);

                            if(spaceInTank >= transferRate)
                            {
                                FluidStack estFluidToFill = new FluidStack(fluidInCoin,transferRate);
                                int fluidToActuallyFill = fluidHandlerItem.fill(estFluidToFill,IFluidHandler.FluidAction.SIMULATE);
                                if(fluidToActuallyFill>0 && removeFluid(pedestal,coinInPedestal,fluidToActuallyFill,true))
                                {
                                    estFluidToFill = new FluidStack(fluidInCoin,fluidToActuallyFill);
                                    int fluidDrained = fluidHandlerItem.fill(estFluidToFill,IFluidHandler.FluidAction.EXECUTE);
                                    ItemStack returnerStack = fluidHandlerItem.getContainer();
                                    if(!returnerStack.isEmpty())
                                    {
                                        removeFluid(pedestal,coinInPedestal,fluidDrained,false);
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
                    FluidStack fluidInCoin = getFluidStored(coinInPedestal);
                    if(tanks > 1)
                    {
                        FluidStack fluidCheckedMatching = FluidStack.EMPTY;
                        fluidCheckedMatching = IntStream.range(0,tanks)//Int Range
                                .mapToObj((handler)::getFluidInTank)//Function being applied to each interval
                                .filter(fluidStack -> fluidStack.isFluidEqual(fluidInCoin))
                                .findFirst().orElse(FluidStack.EMPTY);

                        //There is a matching fluid in a tank to fill
                        if(!fluidCheckedMatching.isEmpty())
                        {
                            FluidStack matchedFluid = fluidCheckedMatching;
                            int value = 0;
                            for(int location=0;location<tanks;location++)
                            {
                                if(handler.getFluidInTank(location).isFluidEqual(matchedFluid)){
                                    value = location;
                                    break;
                                }
                            }

                            int getTankCapacity = handler.getTankCapacity(value);
                            int tankCurrentlyStored = matchedFluid.getAmount();
                            int spaceInTank = getTankCapacity-tankCurrentlyStored;
                            int amountInCoin = fluidInCoin.getAmount();

                            int rate = getFluidTransferRate(coinInPedestal);
                            int actualCoinRate = (spaceInTank>=rate)?(rate):(spaceInTank);
                            int transferRate = (amountInCoin>=actualCoinRate)?(actualCoinRate):(amountInCoin);

                            if(spaceInTank >= transferRate)
                            {
                                FluidStack estFluidToFill = new FluidStack(fluidInCoin,transferRate);
                                int fluidToActuallyFill = handler.fill(estFluidToFill,IFluidHandler.FluidAction.SIMULATE);
                                if(fluidToActuallyFill>0 && removeFluid(pedestal,coinInPedestal,fluidToActuallyFill,true))
                                {
                                    estFluidToFill = new FluidStack(fluidInCoin,fluidToActuallyFill);
                                    int fluidDrained = handler.fill(estFluidToFill,IFluidHandler.FluidAction.EXECUTE);
                                    removeFluid(pedestal,coinInPedestal,fluidDrained,false);
                                }
                            }
                        }
                        else
                        {
                            int value = 0;
                            for(int location=0;location<tanks;location++)
                            {
                                if(handler.getFluidInTank(location).isEmpty()){
                                    value = location;
                                    break;
                                }
                            }
                            FluidStack emptyTank = handler.getFluidInTank(value);
                            int getTankCapacity = handler.getTankCapacity(value);
                            int tankCurrentlyStored = emptyTank.getAmount();
                            int spaceInTank = getTankCapacity-tankCurrentlyStored;
                            int amountInCoin = fluidInCoin.getAmount();

                            int rate = getFluidTransferRate(coinInPedestal);
                            int actualCoinRate = (spaceInTank>=rate)?(rate):(spaceInTank);
                            int transferRate = (amountInCoin>=actualCoinRate)?(actualCoinRate):(amountInCoin);

                            if(spaceInTank >= transferRate)
                            {
                                FluidStack estFluidToFill = new FluidStack(fluidInCoin,transferRate);
                                int fluidToActuallyFill = handler.fill(estFluidToFill,IFluidHandler.FluidAction.SIMULATE);
                                if(fluidToActuallyFill>0 && removeFluid(pedestal,coinInPedestal,fluidToActuallyFill,true))
                                {
                                    estFluidToFill = new FluidStack(fluidInCoin,fluidToActuallyFill);
                                    int fluidDrained = handler.fill(estFluidToFill,IFluidHandler.FluidAction.EXECUTE);
                                    removeFluid(pedestal,coinInPedestal,fluidDrained,false);
                                }
                            }
                        }
                    }
                    else
                    {
                        //should i just set this to zero???
                        FluidStack fluidInTank = handler.getFluidInTank(tanks-1);
                        if(fluidInTank.isEmpty() || fluidInCoin.isFluidEqual(fluidInTank))
                        {
                            int getTankCapacity = handler.getTankCapacity(tanks-1);
                            int tankCurrentlyStored = fluidInTank.getAmount();
                            int spaceInTank = getTankCapacity-tankCurrentlyStored;
                            int amountInCoin = fluidInCoin.getAmount();

                            int rate = getFluidTransferRate(coinInPedestal);
                            int actualCoinRate = (spaceInTank>=rate)?(rate):(spaceInTank);
                            int transferRate = (amountInCoin>=actualCoinRate)?(actualCoinRate):(amountInCoin);
                            if(spaceInTank >= transferRate)
                            {
                                FluidStack estFluidToFill = new FluidStack(fluidInCoin,transferRate);
                                int fluidToActuallyFill = handler.fill(estFluidToFill,IFluidHandler.FluidAction.SIMULATE);
                                if(fluidToActuallyFill>0 && removeFluid(pedestal,coinInPedestal,fluidToActuallyFill,true))
                                {
                                    estFluidToFill = new FluidStack(fluidInCoin,fluidToActuallyFill);
                                    int fluidDrained = handler.fill(estFluidToFill,IFluidHandler.FluidAction.EXECUTE);
                                    removeFluid(pedestal,coinInPedestal,fluidDrained,false);
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

    public static final Item FLUIDEXPORT = new ItemUpgradeFluidExport(new Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluidexport"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FLUIDEXPORT);
    }


}
