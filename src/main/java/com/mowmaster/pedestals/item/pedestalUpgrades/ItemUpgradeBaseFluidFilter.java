package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.crafting.CalculateColor;
import com.mowmaster.pedestals.item.ItemUpgradeTool;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeEntityMinecart;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;

public class ItemUpgradeBaseFluidFilter extends ItemUpgradeBaseFilter {

    public ItemUpgradeBaseFluidFilter(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    //Since Energy Transfer is as fast as possible, speed isnt needed, just capacity
    @Override
    public Boolean canAcceptOpSpeed() {
        return false;
    }

    @Override
    public int getItemEnchantability()
    {
        return 10;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return true;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }



    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        return false;
    }

    @Override
    public int canAcceptCount(World world, BlockPos posPedestal, ItemStack inPedestal, ItemStack itemStackIncoming)
    {
        TileEntity tile = world.getTileEntity(posPedestal);
        if(tile instanceof PedestalTileEntity)
        {
            PedestalTileEntity pedestal = (PedestalTileEntity)tile;
            return pedestal.getSlotSizeLimit();
        }
        //int stackabe = itemStackIncoming.getMaxStackSize();
        return 0;
    }

    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            if(hasFluidInCoin(coin))
            {
                float f = (float)getFluidStored(coin).getAmount()/(float)readMaxFluidFromNBT(coin);
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
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
                                            FluidStack fluidToStore = new FluidStack(mainPedestalFluid.getFluid(),transferRate,mainPedestalFluid.getTag());
                                                if(addFluid(pedestal, storedPedestalCoin,fluidToStore,true) && removeFluid(pedestal, mainPedestalCoin,transferRate,true))
                                                {
                                                    removeFluid(pedestal, mainPedestalCoin,transferRate,false);
                                                    mainPedestalTile.update();
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
                                                    fluidToStore = new FluidStack(mainPedestalFluid.getFluid(),fluidLeftToSend,mainPedestalFluid.getTag());
                                                    if(addFluid(pedestal, storedPedestalCoin,fluidToStore,true) && removeFluid(pedestal, mainPedestalCoin,fluidLeftToSend,true))
                                                    {
                                                        removeFluid(pedestal, mainPedestalCoin,fluidLeftToSend,false);
                                                        mainPedestalTile.update();
                                                        addFluid(pedestal, storedPedestalCoin,fluidToStore,false);
                                                        storedPedestalTile.update();
                                                    }
                                                }
                                            //}

                                            continue;
                                        }
                                    }
                                }
                                else if(storedPedestalCoin.getItem() instanceof ItemUpgradeBaseFluidFilter)
                                {
                                    ItemUpgradeBaseFluidFilter storedCoinItem = ((ItemUpgradeBaseFluidFilter)storedPedestalCoin.getItem());
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
                                            FluidStack fluidToStore = new FluidStack(mainPedestalFluid.getFluid(),transferRate,mainPedestalFluid.getTag());
                                            if(addFluid(pedestal, storedPedestalCoin,fluidToStore,true) && removeFluid(pedestal, mainPedestalCoin,transferRate,true))
                                            {
                                                removeFluid(pedestal, mainPedestalCoin,transferRate,false);
                                                mainPedestalTile.update();
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
                                                fluidToStore = new FluidStack(mainPedestalFluid.getFluid(),fluidLeftToSend,mainPedestalFluid.getTag());
                                                if(addFluid(pedestal, storedPedestalCoin,fluidToStore,true) && removeFluid(pedestal, mainPedestalCoin,fluidLeftToSend,true))
                                                {
                                                    removeFluid(pedestal, mainPedestalCoin,fluidLeftToSend,false);
                                                    mainPedestalTile.update();
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

    @Override
    public ActionResult<ItemStack> onItemRightClick(World p_77659_1_, PlayerEntity p_77659_2_, Hand p_77659_3_) {
        if(!p_77659_1_.isRemote)
        {
            ItemStack wand = p_77659_2_.getHeldItemOffhand();
            ItemStack coin = p_77659_2_.getHeldItemMainhand();
            if(wand.getItem() instanceof ItemUpgradeTool)
            {
                if(hasFluidInCoin(coin))
                {
                    FluidStack fluidIn = getFluidStored(coin);
                    if(removeFluidFromItem(coin,fluidIn.getAmount(),true))
                    {
                        removeFluidFromItem(coin,fluidIn.getAmount(),false);
                        p_77659_1_.playSound(p_77659_2_,p_77659_2_.getPosition().getX(), p_77659_2_.getPosition().getY(), p_77659_2_.getPosition().getZ(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 0.25F, 1.0F);

                        //TODO: localize this text
                        TranslationTextComponent output = new TranslationTextComponent("Fluid Cleared");
                        output.mergeStyle(TextFormatting.WHITE);
                        p_77659_2_.sendMessage(output,p_77659_2_.getUniqueID());
                    }
                }
            }
        }

        return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
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

    public boolean removeFluidFromItem(ItemStack stack, int amount, boolean simulate)
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
                FluidStack newStack = new FluidStack(old.getFluid(),newAmount,old.getTag());
                if(newAmount == 0)
                {
                    newStack = FluidStack.EMPTY;
                }
                setFluidStoredItem(stack,newStack);
            }
            return true;
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
                FluidStack newStack = new FluidStack(old.getFluid(),newAmount,old.getTag());
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

    public boolean addFluid(PedestalTileEntity pedestal, ItemStack coin, FluidStack fluid, boolean simulate)
    {
        CompoundNBT compound = new CompoundNBT();
        if(coin.hasTag())
        {
            compound = coin.getTag();
        }

        FluidStack old = FluidStack.loadFluidStackFromNBT(compound);
        if(old.isEmpty() || old.isFluidEqual(fluid))
        {
            if(!simulate)
            {
                int currentAmount = old.getAmount();
                int newAmount = fluid.getAmount() + currentAmount;
                FluidStack newStack = new FluidStack(fluid,newAmount);
                setFluidStored(pedestal,coin,newStack);
            }
            return true;
        }

        return false;
    }

    public void setFluidStoredItem(ItemStack stack, FluidStack fluid)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound = fluid.writeToNBT(compound);
        stack.setTag(compound);
    }

    public void setFluidStored(PedestalTileEntity pedestal, ItemStack stack, FluidStack fluid)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound = fluid.writeToNBT(compound);
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
            return FluidStack.loadFluidStackFromNBT(getCompound);
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
            if(hasFluidInCoin(pedestal.getCoinOnPedestal()))
            {
                int color = getFluidStored(pedestal.getCoinOnPedestal()).getFluid().getAttributes().getColor();
                int[] rgb = CalculateColor.getRGBColorFromInt(color);
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
