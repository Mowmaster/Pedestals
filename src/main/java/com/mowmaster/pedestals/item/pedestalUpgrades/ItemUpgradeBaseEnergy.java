package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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

import javax.annotation.Nullable;
import java.util.List;

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

    public static boolean isEnergyItemInsert(ICapabilityProvider tile)
    {
        return isEnergyItemInsert(tile, null);
    }

    public static boolean isEnergyItemInsert(ICapabilityProvider tile, @Nullable Direction facing)
    {
        if(tile==null)
            return false;
        return tile.getCapability(CapabilityEnergy.ENERGY, facing)
                .map(IEnergyStorage::canReceive)
                .orElse(false);
    }

    public static boolean isEnergyItemExtract(ICapabilityProvider tile)
    {
        return isEnergyItemExtract(tile, null);
    }

    public static boolean isEnergyItemExtract(ICapabilityProvider tile, @Nullable Direction facing)
    {
        if(tile==null)
            return false;
        return tile.getCapability(CapabilityEnergy.ENERGY, facing)
                .map(IEnergyStorage::canExtract)
                .orElse(false);
    }

    public static int getMaxEnergyInStack(ICapabilityProvider stack, @Nullable Direction side)
    {
        if(stack==null)
            return 0;
        return stack.getCapability(CapabilityEnergy.ENERGY, side)
                .map(IEnergyStorage::getMaxEnergyStored)
                .orElse(0);
    }

    public static int getEnergyInStack(ICapabilityProvider stack)
    {
        return getEnergyInStack(stack, null);
    }

    public static int getEnergyInStack(ICapabilityProvider stack, @Nullable Direction side)
    {
        if(stack==null)
            return 0;
        return stack.getCapability(CapabilityEnergy.ENERGY, side)
                .map(IEnergyStorage::getEnergyStored)
                .orElse(0);
    }

    public static int insertEnergyIntoStack(ICapabilityProvider stack, int energy, boolean simulate)
    {
        return insertEnergyIntoStack(stack, null, energy, simulate);
    }

    public static int insertEnergyIntoStack(ICapabilityProvider stack, @Nullable Direction facing, int energy, boolean simulate)
    {
        if(stack==null)
            return 0;
        return stack.getCapability(CapabilityEnergy.ENERGY, facing)
                .map(storage -> storage.receiveEnergy(energy, simulate))
                .orElse(0);
    }

    public static int extractEnergyFromStack(ICapabilityProvider stack, int energy, boolean simulate)
    {
        return extractEnergyFromStack(stack, null, energy, simulate);
    }

    public static int extractEnergyFromStack(ICapabilityProvider stack, @Nullable Direction facing, int energy, boolean simulate)
    {
        if(stack==null)
            return 0;
        return stack.getCapability(CapabilityEnergy.ENERGY, facing)
                .map(storage -> storage.extractEnergy(energy, simulate))
                .orElse(0);
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
        if(pedestalInv instanceof TilePedestal) {
            TilePedestal tileMainPedestal = ((TilePedestal) pedestalInv);
            //If this Pedestal has any Exp
            int xpMainPedestal = getEnergyStored(coinMainPedestal);
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
                                    if(coinStoredPedestal.getItem() instanceof ItemUpgradeBaseEnergy)
                                    {
                                        int energyMaxStoredPedestal = ((ItemUpgradeBaseEnergy)coinStoredPedestal.getItem()).readMaxEnergyFromNBT(coinStoredPedestal);
                                        int energyStoredPedestal = getEnergyStored(coinStoredPedestal);
                                        //if Stored Pedestal has room for exp (will be lazy sending exp here)
                                        if(energyStoredPedestal < energyMaxStoredPedestal)
                                        {
                                            int transferRate = (getEnergyTransferRate(coinMainPedestal) <= energyMaxStoredPedestal)?(getEnergyTransferRate(coinMainPedestal)):(energyMaxStoredPedestal);
                                            //If we have more then X levels in the pedestal we're sending from
                                            if(xpMainPedestal >= transferRate)
                                            {
                                                int xpRemainingMainPedestal = xpMainPedestal - transferRate;
                                                int xpRemainingStoredPedestal = energyStoredPedestal + transferRate;
                                                //world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setEnergyStored(coinMainPedestal,xpRemainingMainPedestal);
                                                //world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setEnergyStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                            }
                                            else
                                            {
                                                //If we have less then X levels, just send them all.
                                                int xpRemainingMainPedestal = 0;
                                                int xpRemainingStoredPedestal = energyStoredPedestal + xpMainPedestal;
                                                //world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setEnergyStored(coinMainPedestal,xpRemainingMainPedestal);
                                                //world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setEnergyStored(coinStoredPedestal,xpRemainingStoredPedestal);
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

    public int getEnergyBuffer(ItemStack stack)
    {
        //This is default max transfer rate
        return  20000;
    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.func_240699_a_(TextFormatting.GOLD);
        player.sendMessage(name,player.getUniqueID());

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".chat_rfstored");
        xpstored.func_240702_b_(""+ getEnergyStored(stack) +"");
        xpstored.func_240699_a_(TextFormatting.GREEN);
        player.sendMessage(xpstored,player.getUniqueID());

        TranslationTextComponent energyRate = new TranslationTextComponent(getTranslationKey() + ".chat_rfrate");
        energyRate.func_240702_b_(""+ getEnergyTransferRate(stack) +"");
        energyRate.func_240699_a_(TextFormatting.AQUA);
        player.sendMessage(energyRate,player.getUniqueID());

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));
        speed.func_240699_a_(TextFormatting.RED);
        player.sendMessage(speed,player.getUniqueID());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".tooltip_rfstored");
        xpstored.func_240702_b_(""+ getEnergyStored(stack) +"");
        xpstored.func_240699_a_(TextFormatting.GREEN);
        tooltip.add(xpstored);

        TranslationTextComponent xpcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_rfcapacity");
        xpcapacity.func_240702_b_(""+ getEnergyBuffer(stack) +"");
        xpcapacity.func_240699_a_(TextFormatting.AQUA);
        tooltip.add(xpcapacity);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.func_240702_b_("" + getEnergyTransferRate(stack) + "");
        rate.func_240699_a_(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));
        speed.func_240699_a_(TextFormatting.RED);
        tooltip.add(speed);
    }

}
