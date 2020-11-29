package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.crafting.CalculateColor;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeEntityMinecart;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.DispenseFluidContainer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;

public class ItemUpgradeBaseFluid extends ItemUpgradeBase {

    public ItemUpgradeBaseFluid(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    public void setMaxFluid(ItemStack stack, int value)
    {
        writeMaxFluidToNBT(stack, value);
    }

    public int getFluidTransferRate(ItemStack stack)
    {
        //im assuming # = rf value???
        int fluidTransferRate = 1000;
        switch (getCapacityModifier(stack))
        {

            case 0:
                fluidTransferRate = 1000;//1x
                break;
            case 1:
                fluidTransferRate=2000;//2x
                break;
            case 2:
                fluidTransferRate = 4000;//4x
                break;
            case 3:
                fluidTransferRate = 6000;//6x
                break;
            case 4:
                fluidTransferRate = 10000;//10x
                break;
            case 5:
                fluidTransferRate=20000;//20x
                break;
            default: fluidTransferRate=1000;
        }

        return  fluidTransferRate;
    }

    public static boolean isFluidItem(ItemStack itemToCheck)
    {
        LazyOptional<IFluidHandlerItem> cap = itemToCheck.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
        if(cap.isPresent())
        {
            return true;
        }

        return false;
    }
/*
    public static boolean isFluidItemInsert(ItemStack stack)
    {
        if(isFluidItem(stack))
        {
            return isFluidItemInsert(stack, null);
        }

        return false;
    }

    public static boolean isFluidItemInsert(ItemStack itemToCheck, @Nullable Direction facing)
    {
        LazyOptional<IFluidHandler> cap = itemToCheck.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
        if(cap.isPresent())
        {
            return cap.map(IFluidHandler::getFluidInTank)
                    .orElse(false);
        }

        return false;
    }*/

    /*public static boolean isEnergyItemExtract(ItemStack stack)
    {
        if(isFluidItem(stack))
        {
            return isFluidItemExtract(stack, null);
        }

        return false;
    }

    public static boolean isFluidItemExtract(ItemStack itemToCheck, @Nullable Direction facing)
    {
        LazyOptional<IEnergyStorage> cap = itemToCheck.getCapability(CapabilityEnergy.ENERGY);
        if(cap.isPresent())
        {
            return cap.map(IEnergyStorage::canExtract)
                    .orElse(false);
        }

        return false;
    }*/

    /*public static int getMaxIFluidInStack(ItemStack itemToCheck, @Nullable Direction side)
    {
        LazyOptional<IFluidHandler> cap = itemToCheck.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
        if(cap.isPresent())
        {
            int tanks =  cap.map(IFluidHandler::getTanks)
                .orElse(0);

            return (tanks > 0)?():(0);
        }

        return 0;
    }*/

    /*public static int getEnergyInStack(ItemStack itemToCheck)
    {
        return getEnergyInStack(itemToCheck, null);
    }

    public static int getEnergyInStack(ItemStack itemToCheck, @Nullable Direction side)
    {
        LazyOptional<IEnergyStorage> cap = itemToCheck.getCapability(CapabilityEnergy.ENERGY);
        if(cap.isPresent())
        {
            return cap.map(IEnergyStorage::getEnergyStored)
                    .orElse(0);
        }

        return 0;
    }

    public static int insertEnergyIntoStack(ItemStack itemToCheck, int energy, boolean simulate)
    {
        return insertEnergyIntoStack(itemToCheck, null, energy, simulate);
    }

    public static int insertEnergyIntoStack(ItemStack itemToCheck, @Nullable Direction facing, int energy, boolean simulate)
    {

        LazyOptional<IEnergyStorage> cap = itemToCheck.getCapability(CapabilityEnergy.ENERGY);
        if(cap.isPresent())
        {
            return cap.map(storage -> storage.receiveEnergy(energy, simulate))
                    .orElse(0);
        }

        return 0;
    }*/

    /*public static int extractEnergyFromStack(ItemStack itemToCheck, int energy, boolean simulate)
    {
        return extractEnergyFromStack(itemToCheck, null, energy, simulate);
    }

    public static int extractEnergyFromStack(ItemStack itemToCheck, @Nullable Direction facing, int energy, boolean simulate)
    {
        LazyOptional<IEnergyStorage> cap = itemToCheck.getCapability(CapabilityEnergy.ENERGY);
        if(cap.isPresent())
        {
            return cap.map(storage -> storage.extractEnergy(energy, simulate))
                    .orElse(0);
        }

        return 0;
    }*/



    public static LazyOptional<IFluidHandler> findFluidHandlerAtPos(World world, BlockPos pos, Direction side, boolean allowCart)
    {
        TileEntity neighbourTile = world.getTileEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IFluidHandler> cap = neighbourTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            if(AbstractRailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof IForgeEntityMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IFluidHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {
                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof BoatEntity);
                if(!list.isEmpty())
                {
                    LazyOptional<IFluidHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
        }
        return LazyOptional.empty();
    }

    public boolean canRecieveFluid(World world, BlockPos posPedestal, FluidStack fluidIncoming)
    {
        return true;
    }

    public void upgradeActionSendFluid(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        PedestalTileEntity mainPedestalTile = pedestal;
        ItemStack mainPedestalCoin = mainPedestalTile.getCoinOnPedestal();
        BlockPos mainPedestalPos = mainPedestalTile.getPos();

        FluidStack mainPedestalFluid = getFluidStored(mainPedestalCoin);
        int mainPedestalFluidAmount = mainPedestalFluid.getAmount();

        if(mainPedestalFluidAmount>0)
        {
            //Grab the connected pedestals to send to
            if(mainPedestalTile.getNumberOfStoredLocations()>0)
            {
                for(int i=0; i<mainPedestalTile.getNumberOfStoredLocations();i++)
                {
                    BlockPos posStoredPedestal = mainPedestalTile.getStoredPositionAt(i);
                    //Make sure pedestal ISNOT powered and IS loaded in world
                    if(!world.isBlockPowered(posStoredPedestal) && world.isBlockLoaded(posStoredPedestal))
                    {
                        if(posStoredPedestal != mainPedestalPos)
                        {
                            TileEntity storedPedestal = world.getTileEntity(posStoredPedestal);
                            if(storedPedestal instanceof PedestalTileEntity) {
                                PedestalTileEntity storedPedestalTile = ((PedestalTileEntity) storedPedestal);
                                ItemStack storedPedestalCoin = storedPedestalTile.getCoinOnPedestal();
                                //Check if pedestal to send to can even be sent fluid
                                if(storedPedestalCoin.getItem() instanceof ItemUpgradeBaseFluid)
                                {
                                    ItemUpgradeBaseFluid storedCoinItem = ((ItemUpgradeBaseFluid)storedPedestalCoin.getItem());
                                    FluidStack storedCoinFluid = storedCoinItem.getFluidStored(storedPedestalCoin);

                                    //Make Sure Fluids Match or destination is empty
                                    if(storedCoinFluid.isFluidEqual(mainPedestalFluid) || storedCoinFluid.isEmpty() && storedCoinItem.canRecieveFluid(world, posStoredPedestal, mainPedestalFluid))
                                    {
                                        int storedCoinFluidSpace = storedCoinItem.availableFluidSpaceInCoin(storedPedestalCoin);
                                        int storedCoinFluidAmount = storedCoinFluid.getAmount();

                                        if(storedCoinFluidSpace > 0)
                                        {
                                            int getMainTransferRate = getFluidTransferRate(mainPedestalCoin);
                                            int transferRate = (getMainTransferRate <= storedCoinFluidSpace)?(getMainTransferRate):(storedCoinFluidSpace);
                                            //IF main pedestal has more fluid then transfer rate
                                            //if(mainPedestalFluidAmount >= transferRate)
                                            //{
                                                //int mainFluidRemaining = mainPedestalFluidAmount - transferRate;
                                                //int storedFluidRemaining = storedCoinFluidAmount + transferRate;
                                                FluidStack fluidToStore = new FluidStack(mainPedestalFluid.getFluid(),transferRate);
                                                //world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                //System.out.println(fluidToStore.getDisplayName().getString() + " - " + fluidToStore.getAmount());
                                                if(addFluid(pedestal, storedPedestalCoin,fluidToStore,true) && removeFluid(pedestal, mainPedestalCoin,transferRate,true))
                                                {
                                                    removeFluid(pedestal, mainPedestalCoin,transferRate,false);
                                                    //System.out.println("Removed Fluid");
                                                    //setFluidStored(mainPedestalCoin,new FluidStack(mainPedestalFluid,mainFluidRemaining));
                                                    mainPedestalTile.update();
                                                    //world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                    //setFluidStored(storedPedestalCoin,fluidToStore);
                                                    addFluid(pedestal, storedPedestalCoin,fluidToStore,false);
                                                    storedPedestalTile.update();
                                                }
                                                else
                                                {
                                                    mainPedestalFluid = getFluidStored(mainPedestalCoin);
                                                    mainPedestalFluidAmount = mainPedestalFluid.getAmount();
                                                    storedCoinFluid = storedCoinItem.getFluidStored(storedPedestalCoin);
                                                    storedCoinFluidSpace = storedCoinItem.availableFluidSpaceInCoin(storedPedestalCoin);
                                                    storedCoinFluidAmount = storedCoinFluid.getAmount();
                                                    getMainTransferRate = getFluidTransferRate(mainPedestalCoin);
                                                    transferRate = (getMainTransferRate <= storedCoinFluidSpace)?(getMainTransferRate):(storedCoinFluidSpace);
                                                    //IF transfer rate is greater then main pedestal (then empty main pedestal)
                                                    int storedFluidRemaining = storedCoinFluidAmount + transferRate;
                                                    int fluidLeftToSend = (mainPedestalFluidAmount<=transferRate)?(mainPedestalFluidAmount):(transferRate);
                                                    fluidToStore = new FluidStack(mainPedestalFluid.getFluid(),fluidLeftToSend);
                                                    //System.out.println(fluidToStore.getDisplayName().getString() + " - " + fluidToStore.getAmount());
                                                    if(addFluid(pedestal, storedPedestalCoin,fluidToStore,true) && removeFluid(pedestal, mainPedestalCoin,fluidLeftToSend,true))
                                                    {
                                                        removeFluid(pedestal, mainPedestalCoin,fluidLeftToSend,false);
                                                        //setFluidStored(mainPedestalCoin,FluidStack.EMPTY);
                                                        //System.out.println("Removed Fluid");
                                                        mainPedestalTile.update();
                                                        //world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                        //setFluidStored(storedPedestalCoin,fluidToStore);
                                                        addFluid(pedestal, storedPedestalCoin,fluidToStore,false);
                                                        storedPedestalTile.update();
                                                    }
                                                }
                                            //}

                                            continue;
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

    public int availableFluidSpaceInCoin(ItemStack coin)
    {
        FluidStack currentFluid = getFluidStored(coin);
        if(currentFluid.isEmpty())
        {
            return getFluidbuffer(coin);
        }
        else
        {
            int currentlyStored = currentFluid.getAmount();
            int max = readMaxFluidFromNBT(coin);
            return max - currentlyStored;
        }
    }

    public boolean canAddFluidToCoin(PedestalTileEntity pedestal, ItemStack coin, FluidStack fluidIn)
    {
        FluidStack currentFluid = getFluidStored(coin);
        if(currentFluid.isEmpty() || currentFluid.isFluidEqual(fluidIn))
        {

            return addFluid(pedestal,coin,fluidIn,true);
        }

        return false;
    }

    public boolean removeFluid(PedestalTileEntity pedestal, ItemStack stack, int amount, boolean simulate)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        FluidStack old = FluidStack.loadFluidStackFromNBT(compound);
        int currentAmount = old.getAmount();
        int newAmount = currentAmount - amount;

        if(newAmount >=0)
        {
            if(!simulate)
            {
                FluidStack newStack = new FluidStack(old.getFluid(),newAmount);
                if(newAmount == 0)
                {
                    newStack = FluidStack.EMPTY;
                }
                setFluidStored(pedestal,stack,newStack);
            }
            return true;
        }

        return false;
    }

    public boolean removeFluid(PedestalTileEntity pedestal, ItemStack stack, FluidStack fluid, boolean simulate)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        FluidStack old = FluidStack.loadFluidStackFromNBT(compound);
        if(old.isEmpty() || old.isFluidEqual(fluid))
        {
            if(!simulate)
            {
                int currentAmount = old.getAmount();
                int newAmount = fluid.getAmount() + currentAmount;
                FluidStack newStack = new FluidStack(fluid,newAmount);
                setFluidStored(pedestal,stack,newStack);
            }
            return true;
        }
        return false;
    }

    public boolean addFluid(PedestalTileEntity pedestal, ItemStack stack, FluidStack fluid, boolean simulate)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        FluidStack old = FluidStack.loadFluidStackFromNBT(compound);
        if(old.isEmpty() || old.isFluidEqual(fluid))
        {
            if(!simulate)
            {
                int currentAmount = old.getAmount();
                int newAmount = fluid.getAmount() + currentAmount;
                FluidStack newStack = new FluidStack(fluid,newAmount);
                setFluidStored(pedestal,stack,newStack);
            }
            return true;
        }

        return false;
    }

    public void setFluidStored(PedestalTileEntity pedestal, ItemStack stack, FluidStack fluid)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putString("FluidName", fluid.getFluid().getRegistryName().toString());
        compound.putInt("Amount", fluid.getAmount());
        if(fluid.getTag() != null) {
            compound.put("Tag", fluid.getTag());
        }

        stack.setTag(compound);
        pedestal.update();
    }

    public boolean hasFluidInCoin(ItemStack stack)
    {
        return !getFluidStored(stack).isEmpty();
    }

    public FluidStack getFluidStored(ItemStack stack)
    {
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            CompoundNBT tag = new CompoundNBT();

            if(getCompound == null) {
                return FluidStack.EMPTY;
            } else if(!getCompound.contains("FluidName", 8)) {
                return FluidStack.EMPTY;
            } else {
                ResourceLocation fluidName = new ResourceLocation(getCompound.getString("FluidName"));
                Fluid fluid = (Fluid) ForgeRegistries.FLUIDS.getValue(fluidName);
                if(fluid == null) {
                    return FluidStack.EMPTY;
                } else {
                    FluidStack newFluid = new FluidStack(fluid, getCompound.getInt("Amount"));
                    if(getCompound.contains("Tag", 10)) {
                        newFluid.setTag(getCompound.getCompound("Tag"));
                    }

                    return newFluid;
                }
            }
        }

        return FluidStack.EMPTY;
    }

    public boolean hasMaxFluidSet(ItemStack stack)
    {
        boolean returner = false;
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("maxfluid"))
            {
                returner = true;
            }
        }
        return returner;
    }


    public void writeMaxFluidToNBT(ItemStack stack, int value)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("maxfluid",value);
        stack.setTag(compound);
    }

    public int readMaxFluidFromNBT(ItemStack stack)
    {
        int maxenergy = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            maxenergy = getCompound.getInt("maxfluid");
        }
        return maxenergy;
    }

    public int getFluidbuffer(ItemStack stack) {
        int fluidBuffer = 10000;
        switch (getCapacityModifier(stack))
        {
            case 0:
                fluidBuffer = 10000;
                break;
            case 1:
                fluidBuffer = 20000;
                break;
            case 2:
                fluidBuffer = 40000;
                break;
            case 3:
                fluidBuffer = 60000;
                break;
            case 4:
                fluidBuffer = 80000;
                break;
            case 5:
                fluidBuffer = 100000;
                break;
            default: fluidBuffer = 10000;
        }

        return  fluidBuffer;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!world.isBlockPowered(pos))
        {
            int color = getFluidStored(pedestal.getCoinOnPedestal()).getFluid().getAttributes().getColor();
            int[] rgb = CalculateColor.getRGBColorFromInt(color);
            if(hasFluidInCoin(pedestal.getCoinOnPedestal()))
            {
                spawnParticleAroundPedestalBase(world,tick,pos,(float)rgb[0]/255,(float)rgb[1]/255,(float)rgb[2]/255,1.0f);
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

        TranslationTextComponent fluid = new TranslationTextComponent(getTranslationKey() + ".chat_fluid");
        FluidStack fluidStored = getFluidStored(stack);
        fluid.appendString("" + fluidStored.getDisplayName().toString() + "");
        fluid.appendString(" : ");
        fluid.appendString("" + fluidStored.getAmount() + "");
        fluid.appendString("mb");
        fluid.mergeStyle(TextFormatting.BLUE);
        player.sendMessage(fluid,Util.DUMMY_UUID);

        TranslationTextComponent energyRate = new TranslationTextComponent(getTranslationKey() + ".chat_fluidrate");
        energyRate.appendString(""+ getFluidTransferRate(stack) +"");
        energyRate.mergeStyle(TextFormatting.AQUA);
        player.sendMessage(energyRate,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".tooltip_fluidstored");
        //xpstored.appendString()
        xpstored.appendString(""+ getFluidStored(stack).getAmount() +"");
        //xpstored.mergeStyle(TextFormatting.GREEN)
        xpstored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(xpstored);

        TranslationTextComponent xpcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_fluidcapacity");
        xpcapacity.appendString(""+ getFluidbuffer(stack) +"");
        xpcapacity.mergeStyle(TextFormatting.AQUA);
        tooltip.add(xpcapacity);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + getFluidTransferRate(stack) + "");
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

}
