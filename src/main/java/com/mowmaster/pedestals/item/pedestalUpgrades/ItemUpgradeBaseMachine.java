package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;

public class ItemUpgradeBaseMachine extends ItemUpgradeBase {

    public final int burnTimeCostPerItemSmelted = 200;

    public ItemUpgradeBaseMachine(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public boolean canAcceptAdvanced() {
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
            int fuelStored = getFuelStored(coin);
            if(fuelStored>0)
            {
                float f = (float)fuelStored/(float)getMaxFuelDeviderBasedOnFuelStored(fuelStored);
                intItem = MathHelper.floor(f*15.0F);
            }
        }

        return intItem;
    }

    @Override
    public int getCapacityModifier(ItemStack stack)
    {
        int capacity = 0;
        if(hasEnchant(stack))
        {
            capacity = (getCapacityModifierOverEnchanted(stack) > 10)?(10):(getCapacityModifierOverEnchanted(stack));
        }
        return capacity;
    }

    @Override
    public int getItemTransferRate(ItemStack stack)
    {
        int itemsPerSmelt = 1;
        switch (getCapacityModifier(stack))
        {
            case 0:
                itemsPerSmelt = 1;
                break;
            case 1:
                itemsPerSmelt=4;
                break;
            case 2:
                itemsPerSmelt = 8;
                break;
            case 3:
                itemsPerSmelt = 12;
                break;
            case 4:
                itemsPerSmelt = 16;
                break;
            case 5:
                itemsPerSmelt=24;
                break;
            case 6:
                itemsPerSmelt=32;
                break;
            case 7:
                itemsPerSmelt=40;
                break;
            case 8:
                itemsPerSmelt=48;
                break;
            case 9:
                itemsPerSmelt=56;
                break;
            case 10:
                itemsPerSmelt=64;
                break;
            default: itemsPerSmelt=1;
        }

        return  itemsPerSmelt;
    }

    @Override
    public int intOperationalSpeedModifier(ItemStack stack)
    {
        int rate = 0;
        if(hasEnchant(stack))
        {
            rate = (intOperationalSpeedModifierOverride(stack) > 10)?(10):(intOperationalSpeedModifierOverride(stack));
        }
        return rate;
    }

    public int getSmeltingSpeed(ItemStack stack)
    {
        int smeltingSpeed = 200;
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                smeltingSpeed = 200;//normal speed
                break;
            case 1:
                smeltingSpeed=100;//2x faster
                break;
            case 2:
                smeltingSpeed = 50;//4x faster
                break;
            case 3:
                smeltingSpeed = 40;//5x faster
                break;
            case 4:
                smeltingSpeed = 33;//6x faster
                break;
            case 5:
                smeltingSpeed=20;//10x faster
                break;
            case 6:
                smeltingSpeed=10;//20x faster
                break;
            case 7:
                smeltingSpeed=5;//40x faster
                break;
            case 8:
                smeltingSpeed=3;//60x faster
                break;
            case 9:
                smeltingSpeed=2;//100x faster
                break;
            case 10:
                smeltingSpeed=1;//200x faster
                break;
            default: smeltingSpeed=200;
        }

        return  smeltingSpeed;
    }

    @Override
    public String getOperationSpeedString(ItemStack stack)
    {
        TranslationTextComponent normal = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_0");
        TranslationTextComponent twox = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_1");
        TranslationTextComponent fourx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_2");
        TranslationTextComponent fivex = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_3");
        TranslationTextComponent sixx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_4");
        TranslationTextComponent tenx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_5");
        TranslationTextComponent twentyx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_6");
        TranslationTextComponent fourtyx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_7");
        TranslationTextComponent sixtyx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_8");
        TranslationTextComponent onehunx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_9");
        TranslationTextComponent twohunx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_10");
        String str = normal.getString();
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                str = normal.getString();//normal speed
                break;
            case 1:
                str = twox.getString();//2x faster
                break;
            case 2:
                str = fourx.getString();//4x faster
                break;
            case 3:
                str = fivex.getString();//6x faster
                break;
            case 4:
                str = sixx.getString();//6x faster
                break;
            case 5:
                str = tenx.getString();//10x faster
                break;
            case 6:
                str = twentyx.getString();//20x faster
                break;
            case 7:
                str = fourtyx.getString();//40x faster
                break;
            case 8:
                str = sixtyx.getString();//60x faster
                break;
            case 9:
                str = onehunx.getString();//100x faster
                break;
            case 10:
                str = twohunx.getString();//200x faster
                break;
            default: str = normal.getString();;
        }

        return  str;
    }

    public void setFuelStored(ItemStack stack, int fuel)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("fuel",fuel);
        stack.setTag(compound);
    }

    public boolean hasFuel(ItemStack stack)
    {
        return getFuelStored(stack)>0;
    }

    public int getFuelStored(ItemStack stack)
    {
        int storedFuel = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            storedFuel = getCompound.getInt("fuel");
        }
        return storedFuel;
    }

    public void setMaxFuel(ItemStack stack, int amountMax)
    {
        writeMaxFuelToNBT(stack,amountMax);
    }

    public boolean hasMaxFuelSet(ItemStack stack)
    {
        boolean returner = false;
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("maxfuel"))
            {
                returner = true;
            }
        }
        return returner;
    }

    public void writeMaxFuelToNBT(ItemStack stack, int value)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("maxfuel",value);
        stack.setTag(compound);
    }

    public int readMaxFuelFromNBT(ItemStack stack)
    {
        int maxfuel = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            maxfuel = getCompound.getInt("maxfuel");
        }
        return maxfuel;
    }

    public boolean addFuel(PedestalTileEntity pedestal, int amountToAdd, boolean simulate)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        if(hasMaxFuelSet(coin))
        {
            int maxFuel = readMaxFuelFromNBT(coin);
            int currentFuel = getFuelStored(coin);
            int addAmount = currentFuel + amountToAdd;
            if(maxFuel > addAmount)
            {
                if(!simulate)
                {
                    setFuelStored(coin,addAmount);
                    pedestal.update();
                    return true;
                }
                //return true if fuel could be added for simulation requests
                return true;
            }
        }

        return false;
    }

    public boolean removeFuel(World world, BlockPos posPedestal, int amountToRemove, boolean simulate)
    {
        TileEntity entity = world.getTileEntity(posPedestal);
        if(entity instanceof PedestalTileEntity)
        {
            PedestalTileEntity pedestal = (PedestalTileEntity)entity;
            return removeFuel(pedestal,amountToRemove,simulate);
        }

        return false;
    }

    public boolean removeFuel(PedestalTileEntity pedestal, int amountToRemove, boolean simulate)
    {

        ItemStack coin = pedestal.getCoinOnPedestal();
        if(hasFuel(coin))
        {
            int fuelLeft = getFuelStored(coin);
            int amountToSet = fuelLeft - amountToRemove;
            if(fuelLeft >= amountToRemove)
            {
                if(!simulate)
                {
                    if(amountToSet == -1) amountToSet = 0;
                    setFuelStored(coin,amountToSet);
                    pedestal.update();
                    return true;
                    //pedestal.setStoredValueForUpgrades(amountToSet);
                }
                return true;
            }

        }

        return false;
    }

    public static int getItemFuelBurnTime(ItemStack fuel)
    {
        if (fuel.isEmpty()) return 0;
        else
        {
            int burnTime = ForgeHooks.getBurnTime(fuel);
            return burnTime;
        }
    }

    @Override
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
            if(getItemFuelBurnTime(getItemStack)>0)
            {
                int getBurnTimeForStack = getItemFuelBurnTime(getItemStack) * getItemStack.getCount();
                if(addFuel(tilePedestal,getBurnTimeForStack,true))
                {
                    addFuel(tilePedestal,getBurnTimeForStack,false);
                    if(getItemStack.getItem().equals(Items.LAVA_BUCKET))
                    {
                        ItemStack getReturned = new ItemStack(Items.BUCKET,getItemStack.getCount());
                        ItemEntity items1 = new ItemEntity(world, posPedestal.getX() + 0.5, posPedestal.getY() + 1.0, posPedestal.getZ() + 0.5, getReturned);
                        if(!tilePedestal.hasMuffler())world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.25F, 1.0F);
                        entityIn.remove();
                        world.addEntity(items1);
                    }

                    if(!tilePedestal.hasMuffler())world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.25F, 1.0F);
                    entityIn.remove();
                }
            }
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
                int fuelValue = getFuelStored(pedestal.getCoinOnPedestal());

                if(fuelValue >= 200)
                {
                    spawnParticleAroundPedestalBase(world,tick,pos, ParticleTypes.FLAME);
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
        player.sendMessage(name, Util.DUMMY_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString(""+getItemTransferRate(stack)+"");
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

        //Display Fuel Left
        int fuelLeft = getFuelStored(pedestal.getCoinOnPedestal());
        TranslationTextComponent fuel = new TranslationTextComponent(getTranslationKey() + ".chat_fuel");
        fuel.appendString("" + fuelLeft/200 + "");
        fuel.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(fuel,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int s2 = getItemTransferRate(stack);
        String trr = getOperationSpeedString(stack);
        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString(""+s2+"");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(trr);

        rate.mergeStyle(TextFormatting.GRAY);

        TranslationTextComponent fuelStored = new TranslationTextComponent(getTranslationKey() + ".tooltip_fuelstored");
        fuelStored.appendString(""+ getFuelStored(stack) +"");
        fuelStored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(fuelStored);

        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(rate);
        tooltip.add(speed);
    }

}
