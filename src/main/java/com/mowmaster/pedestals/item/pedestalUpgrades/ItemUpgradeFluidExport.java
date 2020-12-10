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
    public ItemUpgradeFluidExport(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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
            FluidStack fluidInItem = FluidUtil.getFluidContained(itemInPedestal).get();
            return fluidInItem;
        }
        return FluidStack.EMPTY;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            int getMaxFluidValue = getFluidbuffer(coinInPedestal);
            if(!hasMaxFluidSet(coinInPedestal) || readMaxFluidFromNBT(coinInPedestal) != getMaxFluidValue) {setMaxFluid(coinInPedestal, getMaxFluidValue);}

            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos)) {
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
                        //System.out.println("GetTanksFluid: "+ fluidInTank.getDisplayName().getString());
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
        World world = pedestal.getWorld();
        BlockPos posPedestal = pedestal.getPos();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();

        BlockPos posInventory = getPosOfBlockBelow(world,posPedestal,1);
        ItemStack itemFromPedestal = ItemStack.EMPTY;

        LazyOptional<IFluidHandler> cap = findFluidHandlerAtPos(world,posInventory,getPedestalFacing(world, posPedestal),true);

        TileEntity invToPushTo = world.getTileEntity(posInventory);
        if(invToPushTo instanceof PedestalTileEntity) {
            itemFromPedestal = ItemStack.EMPTY;
        }
        else {
            //System.out.println("Has Cap: "+ cap.isPresent());
            if(cap.isPresent())
            {
                IFluidHandler handler = cap.orElse(null);
                if(handler != null)
                {
                    int tanks = handler.getTanks();
                    FluidStack fluidInCoin = getFluidStored(coinInPedestal);
                    //System.out.println("Tanks: "+ tanks);
                    if(tanks > 1)
                    {
                        FluidStack fluidCheckedMatching = FluidStack.EMPTY;
                        fluidCheckedMatching = IntStream.range(0,tanks)//Int Range
                                .mapToObj((handler)::getFluidInTank)//Function being applied to each interval
                                .filter(fluidStack -> fluidStack.isFluidEqual(fluidInCoin))
                                .findFirst().orElse(FluidStack.EMPTY);

                        //There is a matching fluid in a tank to fill
                        //System.out.println("Matching Fluid In 1 of the many tanks: "+ !fluidCheckedMatching.isEmpty());
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
                            //System.out.println("Which tank has the matching fluid: "+ location);

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
                            //System.out.println("Get the first empty tank: "+ location);
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
                        //System.out.println("Fluid In Single Tank: "+ fluidInTank.getDisplayName().getString() + ": "+fluidInTank.getAmount());
                        if(fluidInTank.isEmpty() || fluidInCoin.isFluidEqual(fluidInTank))
                        {
                            int getTankCapacity = handler.getTankCapacity(tanks-1);
                            int tankCurrentlyStored = fluidInTank.getAmount();
                            int spaceInTank = getTankCapacity-tankCurrentlyStored;
                            int amountInCoin = fluidInCoin.getAmount();

                            int rate = getFluidTransferRate(coinInPedestal);
                            int actualCoinRate = (spaceInTank>=rate)?(rate):(spaceInTank);
                            int transferRate = (amountInCoin>=actualCoinRate)?(actualCoinRate):(amountInCoin);
                            //System.out.println("Space vs transfer "+ spaceInTank + " >= " + transferRate);
                            if(spaceInTank >= transferRate)
                            {
                                FluidStack estFluidToFill = new FluidStack(fluidInCoin,transferRate);
                                int fluidToActuallyFill = handler.fill(estFluidToFill,IFluidHandler.FluidAction.SIMULATE);
                                //System.out.println("Simulated to fill amount: "+ fluidToActuallyFill + " ALSO CAN REMOVE FROM COIN:" + removeFluid(pedestal,coinInPedestal,fluidToActuallyFill,true));
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

    public static final Item FLUIDEXPORT = new ItemUpgradeFluidExport(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluidexport"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FLUIDEXPORT);
    }


}
