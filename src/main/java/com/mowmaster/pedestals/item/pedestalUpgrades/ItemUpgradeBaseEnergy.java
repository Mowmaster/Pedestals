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
import net.minecraft.util.math.MathHelper;
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

    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            if(hasEnergy(coin))
            {
                float f = (float)getEnergyStored(coin)/(float)readMaxEnergyFromNBT(coin);
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    public void setMaxEnergy(ItemStack stack, int value)
    {
        writeMaxEnergyToNBT(stack, value);
    }

    public int getEnergyTransferRate(ItemStack stack)
    {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int overEnchanted = (capacityOver*20000);
        switch (capacityOver)
        {
            case 0:return 5000;
            case 1:return 10000;
            case 2:return 30000;
            case 3:return 50000;
            case 4:return 40000;
            case 5:return 60000;
            case 6:return 80000;
            case 7:return 100000;
            case 8:return 120000;
            case 9:return 140000;
            case 10:return 160000;
            default:return (overEnchanted>maxStored)?(maxStored):(overEnchanted);
        }
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

    public boolean itemHasMaxEnergy(ItemStack itemToCheck)
    {

        LazyOptional<IEnergyStorage> cap = itemToCheck.getCapability(CapabilityEnergy.ENERGY);
        if(cap.isPresent())
        {
            IEnergyStorage handler = cap.orElse(null);
            int max = handler.getMaxEnergyStored();
            int current = handler.getEnergyStored();

            return current>=max;
        }

        return false;
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

    public void upgradeActionSendEnergy(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinMainPedestal = pedestal.getCoinOnPedestal();
        BlockPos posMainPedestal = pedestal.getPos();

        int energyMainPedestal = getEnergyStored(coinMainPedestal);
        if(energyMainPedestal>0)
        {
            //Grab the connected pedestals to send to
            if(pedestal.getNumberOfStoredLocations()>0)
            {
                for(int i=0; i<pedestal.getNumberOfStoredLocations();i++)
                {
                    BlockPos posStoredPedestal = pedestal.getStoredPositionAt(i);
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
                                    ItemUpgradeBaseEnergy itemEB = ((ItemUpgradeBaseEnergy)coinStoredPedestal.getItem());
                                    int energyMaxStoredPedestal = itemEB.readMaxEnergyFromNBT(coinStoredPedestal);
                                    int energyStoredPedestal = itemEB.getEnergyStored(coinStoredPedestal);
                                    int energySpaceInTargetPedestal = energyMaxStoredPedestal - energyStoredPedestal;

                                    if(energySpaceInTargetPedestal > 0)
                                    {
                                        int transferRate = (getEnergyTransferRate(coinMainPedestal) <= energySpaceInTargetPedestal)?(getEnergyTransferRate(coinMainPedestal)):(energySpaceInTargetPedestal);

                                        if(itemEB.addEnergy(coinStoredPedestal,transferRate,true) && removeEnergy(coinMainPedestal,transferRate,true))
                                        {
                                            removeEnergy(coinMainPedestal,transferRate,false);
                                            pedestal.update();
                                            addEnergy(coinStoredPedestal,transferRate,false);
                                            tileStoredPedestal.update();
                                        }
                                        else
                                        {
                                            int energyLeftInMain = (getEnergyStored(coinMainPedestal) < getEnergyTransferRate(coinMainPedestal))?(getEnergyStored(coinMainPedestal)):(getEnergyTransferRate(coinMainPedestal));
                                            energyStoredPedestal = itemEB.getEnergyStored(coinStoredPedestal);
                                            energySpaceInTargetPedestal = energyMaxStoredPedestal - energyStoredPedestal;
                                            transferRate = (energyLeftInMain <= energySpaceInTargetPedestal)?(energyLeftInMain):(energySpaceInTargetPedestal);
                                            int energyLeftToSend = (energyLeftInMain<=transferRate)?(energyLeftInMain):(transferRate);

                                            if(itemEB.addEnergy(coinStoredPedestal,energyLeftToSend,true) && removeEnergy(coinMainPedestal,energyLeftToSend,true))
                                            {
                                                removeEnergy(coinMainPedestal,energyLeftToSend,false);
                                                pedestal.update();
                                                itemEB.addEnergy(coinStoredPedestal,energyLeftToSend,false);
                                                tileStoredPedestal.update();
                                            }
                                        }

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

    public void sendEnergyToPedestal(PedestalTileEntity senderPedestal,PedestalTileEntity receiverPedestal)
    {
        World world = senderPedestal.getWorld();
        ItemStack coinMainPedestal = senderPedestal.getCoinOnPedestal();
        BlockPos posMainPedestal = senderPedestal.getPos();

        int energyMainPedestal = getEnergyStored(coinMainPedestal);
        if(energyMainPedestal>0)
        {
            BlockPos posStoredPedestal = receiverPedestal.getPos();
            //Make sure pedestal ISNOT powered and IS loaded in world
            if(!world.isBlockPowered(posStoredPedestal) && world.isBlockLoaded(posStoredPedestal))
            {
                if(posStoredPedestal != posMainPedestal)
                {
                    PedestalTileEntity tileStoredPedestal = receiverPedestal;
                    ItemStack coinStoredPedestal = tileStoredPedestal.getCoinOnPedestal();
                    //Check if pedestal to send to can even be sent exp
                    if(coinStoredPedestal.getItem() instanceof ItemUpgradeBaseEnergy)
                    {
                        ItemUpgradeBaseEnergy itemEB = ((ItemUpgradeBaseEnergy)coinStoredPedestal.getItem());
                        int energyMaxStoredPedestal = itemEB.readMaxEnergyFromNBT(coinStoredPedestal);
                        int energyStoredPedestal = itemEB.getEnergyStored(coinStoredPedestal);
                        int energySpaceInTargetPedestal = energyMaxStoredPedestal - energyStoredPedestal;

                        if(energySpaceInTargetPedestal > 0)
                        {
                            int transferRate = (getEnergyTransferRate(coinMainPedestal) <= energySpaceInTargetPedestal)?(getEnergyTransferRate(coinMainPedestal)):(energySpaceInTargetPedestal);

                            if(itemEB.addEnergy(coinStoredPedestal,transferRate,true) && removeEnergy(coinMainPedestal,transferRate,true))
                            {
                                removeEnergy(coinMainPedestal,transferRate,false);
                                senderPedestal.update();
                                addEnergy(coinStoredPedestal,transferRate,false);
                                tileStoredPedestal.update();
                            }
                            else
                            {
                                int energyLeftInMain = (getEnergyStored(coinMainPedestal) < getEnergyTransferRate(coinMainPedestal))?(getEnergyStored(coinMainPedestal)):(getEnergyTransferRate(coinMainPedestal));
                                energyStoredPedestal = itemEB.getEnergyStored(coinStoredPedestal);
                                energySpaceInTargetPedestal = energyMaxStoredPedestal - energyStoredPedestal;
                                transferRate = (energyLeftInMain <= energySpaceInTargetPedestal)?(energyLeftInMain):(energySpaceInTargetPedestal);
                                int energyLeftToSend = (energyLeftInMain<=transferRate)?(energyLeftInMain):(transferRate);

                                if(itemEB.addEnergy(coinStoredPedestal,energyLeftToSend,true) && removeEnergy(coinMainPedestal,energyLeftToSend,true))
                                {
                                    removeEnergy(coinMainPedestal,energyLeftToSend,false);
                                    senderPedestal.update();
                                    itemEB.addEnergy(coinStoredPedestal,energyLeftToSend,false);
                                    tileStoredPedestal.update();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public int availableEnergySpaceInCoin(ItemStack coin)
    {
        int getMaxEnergy = readMaxEnergyFromNBT(coin);
        int getCurrentEnergy = getEnergyStored(coin);
        int difference = getMaxEnergy-getCurrentEnergy;

        //If coin already has energy then get the difference
        if(hasEnergy(coin))
        {
            return difference;
        }
        //Otherwise just return the max allowed
        else return getMaxEnergy;
    }


    public boolean addEnergy(ItemStack coin, int energyIn, boolean simulate)
    {
        int getMaxEnergyValue = readMaxEnergyFromNBT(coin);
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

    //returns amount filled
    public int fillEnergy(ItemStack coin, int energyIn, boolean simulate)
    {
        int returner = 0;
        int getMaxEnergyValue = readMaxEnergyFromNBT(coin);
        int currentEnergy = getEnergyStored(coin);
        int getSpace = getMaxEnergyValue-currentEnergy;
        int newEnergyValue = Math.min(getSpace,energyIn);
        if(newEnergyValue>0)
        {
            if(!simulate)
            {
                addEnergy(coin,newEnergyValue,simulate);
            }
            returner = newEnergyValue;
        }
        return returner;
    }

    public boolean removeEnergy(ItemStack coin, int energyOut, boolean simulate)
    {
        int getMaxEnergyValue = readMaxEnergyFromNBT(coin);
        int currentEnergy = getEnergyStored(coin);
        int energyDiff = currentEnergy - energyOut;
        int newEnergyValue = ((energyDiff)>0)?(energyDiff):(0);
        //Dont use newEnergy here unless we want to be lazy about pulling out more energy then inside
        if(energyDiff>=0)
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
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int overEnchanted = (capacityOver*20000);
        switch (capacityOver)
        {
            case 0:return 5000;
            case 1:return 10000;
            case 2:return 30000;
            case 3:return 50000;
            case 4:return 40000;
            case 5:return 60000;
            case 6:return 80000;
            case 7:return 100000;
            case 8:return 120000;
            case 9:return 140000;
            case 10:return 160000;
            default:return (overEnchanted>maxStored)?(maxStored):(overEnchanted);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!pedestal.hasParticleDiffuser())
        {
            if(!world.isBlockPowered(pos))
            {
                if(hasEnergy(pedestal.getCoinOnPedestal()))
                {
                    spawnParticleAroundPedestalBase(world,tick,pos,1.0f,0.0f,0.0f,1.0f);
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
        xpstored.appendString(""+ getEnergyStored(stack) +"");
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
