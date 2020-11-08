package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
import net.minecraftforge.common.extensions.IForgeEntityMinecart;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;

public class ItemUpgradeBaseEnergy extends ItemUpgradeBase {

    public ItemUpgradeBaseEnergy(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    public void setMaxEnergy(ItemStack stack, int value)
    {
        writeMaxEnergyToNBT(stack, value);
    }

    public int getEnergyTransferRate(ItemStack stack)
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

    public static boolean isEnergyItem(ItemStack itemToCheck)
    {
        LazyOptional<IEnergyStorage> cap = itemToCheck.getCapability(CapabilityEnergy.ENERGY);
        if(cap.isPresent())
        {
            return true;
        }

        return false;
    }

    public static boolean isEnergyItemInsert(ItemStack stack)
    {
        if(isEnergyItem(stack))
        {
            return isEnergyItemInsert(stack, null);
        }

        return false;
    }

    public static boolean isEnergyItemInsert(ItemStack itemToCheck, @Nullable Direction facing)
    {
        LazyOptional<IEnergyStorage> cap = itemToCheck.getCapability(CapabilityEnergy.ENERGY);
        if(cap.isPresent())
        {
            return cap.map(IEnergyStorage::canReceive)
                    .orElse(false);
        }

        return false;
    }

    public static boolean isEnergyItemExtract(ItemStack stack)
    {
        if(isEnergyItem(stack))
        {
            return isEnergyItemExtract(stack, null);
        }

        return false;
    }

    public static boolean isEnergyItemExtract(ItemStack itemToCheck, @Nullable Direction facing)
    {
        LazyOptional<IEnergyStorage> cap = itemToCheck.getCapability(CapabilityEnergy.ENERGY);
        if(cap.isPresent())
        {
            return cap.map(IEnergyStorage::canExtract)
                    .orElse(false);
        }

        return false;
    }

    public static int getMaxEnergyInStack(ItemStack itemToCheck, @Nullable Direction side)
    {
        LazyOptional<IEnergyStorage> cap = itemToCheck.getCapability(CapabilityEnergy.ENERGY);
        if(cap.isPresent())
        {
            return cap.map(IEnergyStorage::getMaxEnergyStored)
                .orElse(0);
        }

        return 0;
    }

    public static int getEnergyInStack(ItemStack itemToCheck)
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
    }

    public static int extractEnergyFromStack(ItemStack itemToCheck, int energy, boolean simulate)
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
    }

    public static LazyOptional<IEnergyStorage> findEnergyHandlerAtPos(World world, BlockPos pos, Direction side, boolean allowCart)
    {
        TileEntity neighbourTile = world.getTileEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IEnergyStorage> cap = neighbourTile.getCapability(CapabilityEnergy.ENERGY, side);
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
                    LazyOptional<IEnergyStorage> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityEnergy.ENERGY);
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
                    LazyOptional<IEnergyStorage> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityEnergy.ENERGY);
                    if(cap.isPresent())
                        return cap;
                }
            }
        }
        return LazyOptional.empty();
    }

    public void upgradeActionSendEnergy(World world, ItemStack coinMainPedestal, BlockPos posMainPedestal)
    {
        TileEntity pedestalInv = world.getTileEntity(posMainPedestal);
        if(pedestalInv instanceof PedestalTileEntity) {
            PedestalTileEntity tileMainPedestal = ((PedestalTileEntity) pedestalInv);
            //If this Pedestal has any Energy
            int energyMainPedestal = getEnergyStored(coinMainPedestal);
            if(energyMainPedestal>0)
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
                                if(storedPedestal instanceof PedestalTileEntity) {
                                    PedestalTileEntity tileStoredPedestal = ((PedestalTileEntity) storedPedestal);
                                    ItemStack coinStoredPedestal = tileStoredPedestal.getCoinOnPedestal();
                                    //Check if pedestal to send to can even be sent exp
                                    if(coinStoredPedestal.getItem() instanceof ItemUpgradeBaseEnergy)
                                    {
                                        int energyMaxStoredPedestal = ((ItemUpgradeBaseEnergy)coinStoredPedestal.getItem()).readMaxEnergyFromNBT(coinStoredPedestal);
                                        int energyStoredPedestal = getEnergyStored(coinStoredPedestal);
                                        int energySpaceInTargetPedestal = energyMaxStoredPedestal - energyStoredPedestal;
                                        //if Stored Pedestal has room for exp (will be lazy sending exp here)
                                        if(energyStoredPedestal < energyMaxStoredPedestal)
                                        {
                                            int transferRate = (getEnergyTransferRate(coinMainPedestal) <= energySpaceInTargetPedestal)?(getEnergyTransferRate(coinMainPedestal)):(energySpaceInTargetPedestal);
                                            //If we have more then X levels in the pedestal we're sending from
                                            if(energyMainPedestal >= transferRate)
                                            {
                                                int xpRemainingMainPedestal = energyMainPedestal - transferRate;
                                                int xpRemainingStoredPedestal = energyStoredPedestal + transferRate;
                                                //world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setEnergyStored(coinMainPedestal,xpRemainingMainPedestal);
                                                tileMainPedestal.update();
                                                //world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setEnergyStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                                tileStoredPedestal.update();

                                            }
                                            else
                                            {
                                                //If we have less then X levels, just send them all.
                                                int xpRemainingMainPedestal = 0;
                                                int xpRemainingStoredPedestal = energyStoredPedestal + energyMainPedestal;
                                                //world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setEnergyStored(coinMainPedestal,xpRemainingMainPedestal);
                                                tileMainPedestal.update();
                                                //world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setEnergyStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                                tileStoredPedestal.update();
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
    }


    public boolean addEnergy(ItemStack coin, int energyIn, boolean simulate)
    {
        int getMaxEnergyValue = getEnergyBuffer(coin);
        int currentEnergy = getEnergyStored(coin);
        int newEnergyValue = currentEnergy + energyIn;
        if(getMaxEnergyValue>=newEnergyValue)
        {
            if(!simulate)
            {
                setEnergyStored(coin,newEnergyValue);
            }
            return true;
        }
        return false;
    }

    public boolean removeEnergy(ItemStack coin, int energyOut, boolean simulate)
    {
        int getMaxEnergyValue = getEnergyBuffer(coin);
        int currentEnergy = getEnergyStored(coin);
        int newEnergyValue = currentEnergy + energyOut;
        if(newEnergyValue>=0)
        {
            if(!simulate)
            {
                setEnergyStored(coin,newEnergyValue);
            }
            return true;
        }
        return false;
    }

    public void setEnergyStored(ItemStack stack, int energy)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("energy",energy);
        stack.setTag(compound);
    }

    public boolean hasEnergy(ItemStack stack)
    {
        return getEnergyStored(stack)>0;
    }

    public int getEnergyStored(ItemStack stack)
    {
        int storedEnergy = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            storedEnergy = getCompound.getInt("energy");
        }
        return storedEnergy;
    }

    public boolean hasMaxEnergySet(ItemStack stack)
    {
        boolean returner = false;
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("maxenergy"))
            {
                returner = true;
            }
        }
        return returner;
    }


    public void writeMaxEnergyToNBT(ItemStack stack, int value)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("maxenergy",value);
        stack.setTag(compound);
    }

    public int readMaxEnergyFromNBT(ItemStack stack)
    {
        int maxenergy = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            maxenergy = getCompound.getInt("maxenergy");
        }
        return maxenergy;
    }

    public int getEnergyBuffer(ItemStack stack) {
        int energyBuffer = 10000;
        switch (getCapacityModifier(stack))
        {
            case 0:
                energyBuffer = 10000;
                break;
            case 1:
                energyBuffer = 20000;
                break;
            case 2:
                energyBuffer = 40000;
                break;
            case 3:
                energyBuffer = 60000;
                break;
            case 4:
                energyBuffer = 80000;
                break;
            case 5:
                energyBuffer = 100000;
                break;
            default: energyBuffer = 10000;
        }

        return  energyBuffer;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!world.isBlockPowered(pos))
        {
            if(hasEnergy(pedestal.getCoinOnPedestal()))
            {
                spawnParticleAroundPedestalBase(world,tick,pos,1.0f,0.0f,0.0f,1.0f);
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

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".chat_rfstored");
        xpstored.appendString(""+ getEnergyStored(stack) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored,Util.DUMMY_UUID);

        TranslationTextComponent energyRate = new TranslationTextComponent(getTranslationKey() + ".chat_rfrate");
        energyRate.appendString(""+ getEnergyTransferRate(stack) +"");
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
        xpstored.appendString(""+ getEnergyStored(stack) +"");
        //xpstored.mergeStyle(TextFormatting.GREEN)
        xpstored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(xpstored);

        TranslationTextComponent xpcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_rfcapacity");
        xpcapacity.appendString(""+ getEnergyBuffer(stack) +"");
        xpcapacity.mergeStyle(TextFormatting.AQUA);
        tooltip.add(xpcapacity);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + getEnergyTransferRate(stack) + "");
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

}
