package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeEntityMinecart;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

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
        int energyTransferRate = 1000;
        switch (getCapacityModifier(stack))
        {

            case 0:
                energyTransferRate = 1000;//1x
                break;
            case 1:
                energyTransferRate=2000;//2x
                break;
            case 2:
                energyTransferRate = 4000;//4x
                break;
            case 3:
                energyTransferRate = 6000;//6x
                break;
            case 4:
                energyTransferRate = 10000;//10x
                break;
            case 5:
                energyTransferRate=20000;//20x
                break;
            default: energyTransferRate=1000;
        }

        return  energyTransferRate;
    }

    //LazyOptional<IFluidHandler> cap = neighbourTile.getCapability();



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
                    LazyOptional<IFluidHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
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
                    LazyOptional<IFluidHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
                    if(cap.isPresent())
                        return cap;
                }
            }
        }
        return LazyOptional.empty();
    }

    public void upgradeActionSendFluid(World world, ItemStack coinMainPedestal, BlockPos posMainPedestal)
    {
        TileEntity pedestalInv = world.getTileEntity(posMainPedestal);
        if(pedestalInv instanceof TilePedestal) {
            TilePedestal tileMainPedestal = ((TilePedestal) pedestalInv);
            //If this Pedestal has any Exp
            int xpMainPedestal = getFluidStored(coinMainPedestal);
            if(xpMainPedestal>0)
            {
                //Grab the connected pedestals to send to
                if(tileMainPedestal.getNumberOfStoredLocations()>0)
                {
                    for(int i=0; i<tileMainPedestal.getNumberOfStoredLocations();i++)
                    {
                        BlockPos posStoredPedestal = tileMainPedestal.getStoredPositionAt(i);
                        //Make sure pedestal ISNOT powered and IS loaded in world
                        if(!world.isBlockPowered(posStoredPedestal) && world.isBlockLoaded(posStoredPedestal))
                        {
                            if(posStoredPedestal != posMainPedestal)
                            {
                                TileEntity storedPedestal = world.getTileEntity(posStoredPedestal);
                                if(storedPedestal instanceof TilePedestal) {
                                    TilePedestal tileStoredPedestal = ((TilePedestal) storedPedestal);
                                    ItemStack coinStoredPedestal = tileStoredPedestal.getCoinOnPedestal();
                                    //Check if pedestal to send to can even be sent exp
                                    if(coinStoredPedestal.getItem() instanceof ItemUpgradeBaseFluid)
                                    {
                                        int energyMaxStoredPedestal = ((ItemUpgradeBaseFluid)coinStoredPedestal.getItem()).readMaxFluidFromNBT(coinStoredPedestal);
                                        int energyStoredPedestal = getFluidStored(coinStoredPedestal);
                                        int energySpaceInTargetPedestal = energyMaxStoredPedestal - energyStoredPedestal;
                                        //if Stored Pedestal has room for exp (will be lazy sending exp here)
                                        if(energyStoredPedestal < energyMaxStoredPedestal)
                                        {
                                            int transferRate = (getFluidTransferRate(coinMainPedestal) <= energySpaceInTargetPedestal)?(getFluidTransferRate(coinMainPedestal)):(energySpaceInTargetPedestal);
                                            //If we have more then X levels in the pedestal we're sending from
                                            if(xpMainPedestal >= transferRate)
                                            {
                                                int xpRemainingMainPedestal = xpMainPedestal - transferRate;
                                                int xpRemainingStoredPedestal = energyStoredPedestal + transferRate;
                                                //world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setFluidStored(coinMainPedestal,xpRemainingMainPedestal);
                                                tileMainPedestal.update();
                                                //world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setFluidStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                                tileStoredPedestal.update();

                                            }
                                            else
                                            {
                                                //If we have less then X levels, just send them all.
                                                int xpRemainingMainPedestal = 0;
                                                int xpRemainingStoredPedestal = energyStoredPedestal + xpMainPedestal;
                                                //world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setFluidStored(coinMainPedestal,xpRemainingMainPedestal);
                                                tileMainPedestal.update();
                                                //world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setFluidStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                                tileStoredPedestal.update();
                                            }

                                            break;
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



    public void setFluidStored(ItemStack stack, int fluid)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("fluid",fluid);
        stack.setTag(compound);
    }

    public boolean hasFluid(ItemStack stack)
    {
        return getFluidStored(stack)>0;
    }

    public int getFluidStored(ItemStack stack)
    {
        int storedFluid = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            storedFluid = getCompound.getInt("fluid");
        }
        return storedFluid;
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

    public int getFluidBuffer(ItemStack stack) {
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
    public void onRandomDisplayTick(TilePedestal pedestal,int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!world.isBlockPowered(pos))
        {
            if(hasFluid(pedestal.getCoinOnPedestal()))
            {
                spawnParticleAroundPedestalBase(world,tick,pos,0.0f,0.0f,1.0f,1.0f);
            }
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".chat_fluidstored");
        xpstored.appendString(""+ getFluidStored(stack) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored,Util.DUMMY_UUID);

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

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".tooltip_rfstored");
        //xpstored.appendString()
        xpstored.appendString(""+ getFluidStored(stack) +"");
        //xpstored.mergeStyle(TextFormatting.GREEN)
        xpstored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(xpstored);

        TranslationTextComponent xpcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_rfcapacity");
        xpcapacity.appendString(""+ getFluidBuffer(stack) +"");
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
