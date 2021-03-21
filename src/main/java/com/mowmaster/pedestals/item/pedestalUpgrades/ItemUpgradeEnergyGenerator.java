package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnergyGenerator extends ItemUpgradeBaseEnergy
{

    public ItemUpgradeEnergyGenerator(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    public double getCapicityModifier(ItemStack stack)
    {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        double rate = (double)(((capacityOver)*0.01)+0.5);
        double rater = (rate > 0.9)?(0.9):(rate);

        double intModifier = 1.0;
        switch (capacityOver)
        {
            case 0:
                intModifier = 1.0;
                break;
            case 1:
                intModifier=0.9;
                break;
            case 2:
                intModifier = 0.8;
                break;
            case 3:
                intModifier = 0.7;
                break;
            case 4:
                intModifier = 0.6;
                break;
            case 5:
                intModifier=0.5;
                break;
            default: intModifier=(1.0-rater);
        }

        return  intModifier;
    }

    public int getEnergyBuffer(ItemStack stack) {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int buffer = (capacityOver*2000)+20000;
        return  (buffer>maxStored)?(maxStored):(buffer);
    }

    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            if(getFuelStored(coin)>0)
            {
                float f = (float)getFuelStored(coin)/(float)readMaxFuelFromNBT(coin);
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int getMaxFuelValue = 2000000000;
            if(!hasMaxFuelSet(coinInPedestal) || readMaxFuelFromNBT(coinInPedestal) != getMaxFuelValue) {setMaxFuel(coinInPedestal, getMaxFuelValue);}

            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                //Always send energy, as fast as we can within the Pedestal Energy Network
                upgradeActionSendEnergy(pedestal);
                //only run 1 per second
                    if(getFuelStored(coinInPedestal)>0 && getEnergyStored(coinInPedestal) < getEnergyBuffer(coinInPedestal))
                    {
                        if (world.getGameTime()%5 == 0) {
                            BlockPos directionalPos = getPosOfBlockBelow(world,pedestalPos,-1);
                            PacketHandler.sendToNearby(world,pedestalPos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,directionalPos.getX(),directionalPos.getY(),directionalPos.getZ(),145,145,145));
                        }

                        if (world.getGameTime()%20 == 0) {
                            upgradeAction(world,pedestalPos,itemInPedestal,coinInPedestal);
                        }
                    }
            }
        }
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {
        //Takes into account if it has Advanced or Not
        double speed = getOperationSpeedOverride(coinInPedestal);
        double capacityRate = getCapicityModifier(coinInPedestal);
        int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
        if(!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {setMaxEnergy(coinInPedestal, getMaxEnergyValue);}

        //Generator when it has fuel, make energy every second based on capacity and speed
        //20k per 1 coal is base fuel value (2500 needed in mod to process 1 item)
        //1 coal takes 1600 ticks to process default??? furnace uses 2500 per 10 seconds by default
        //so 12.5 energy per tick (250/sec)
        double speedMultiplier = (20/speed);
        int baseFuel = (int) (20 * speedMultiplier);
        int fuelConsumed = (int) Math.round(baseFuel * capacityRate);
        if(removeFuel(world,posOfPedestal,fuelConsumed,true))
        {
            doEnergyProcess(world,coinInPedestal,posOfPedestal,baseFuel,capacityRate);
        }
        else {
            int fuelLeft = getFuelStored(coinInPedestal);
            doEnergyProcess(world,coinInPedestal,posOfPedestal,fuelLeft,capacityRate);
        }
    }

    public void doEnergyProcess(World world, ItemStack coinInPedestal, BlockPos posOfPedestal, int baseFuel, double capacityRate)
    {
        int fuelConsumed = (int) Math.round(baseFuel * capacityRate);
        int energyMax = getEnergyBuffer(coinInPedestal);
        int energyCurrent = getEnergyStored(coinInPedestal);
        int estEnergyProduced = (int) Math.round(baseFuel * 12.5);
        if(addEnergy(coinInPedestal,estEnergyProduced,true))
        {
            removeFuel(world,posOfPedestal,fuelConsumed,false);
            addEnergy(coinInPedestal,estEnergyProduced,false);
        }
        else {
            int energyCanProduce = energyMax - energyCurrent;
            int fuelCanConsume = (int)Math.floor(energyCanProduce/12.5);
            estEnergyProduced = (int) Math.round(fuelCanConsume * 12.5);
            int actualFuelConsumed = (int)Math.round(fuelCanConsume * capacityRate);
            removeFuel(world,posOfPedestal,actualFuelConsumed,false);
            addEnergy(coinInPedestal,estEnergyProduced,false);
        }
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
        if(!world.isRemote)
        {
            if(!world.isBlockPowered(posPedestal))
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
                                world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                entityIn.remove();
                                world.addEntity(items1);
                            }

                            world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.25F, 1.0F);
                            entityIn.remove();
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
        player.sendMessage(name, Util.DUMMY_UUID);

        //Display Fuel Left
        int fuelLeft = getFuelStored(pedestal.getCoinOnPedestal());
        TranslationTextComponent fuel = new TranslationTextComponent(getTranslationKey() + ".chat_fuel");
        fuel.appendString("" + fuelLeft + "");
        fuel.mergeStyle(TextFormatting.DARK_GREEN);
        player.sendMessage(fuel,Util.DUMMY_UUID);

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".chat_rfstored");
        xpstored.appendString(""+ getEnergyStored(stack) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored,Util.DUMMY_UUID);

        int opSpeed = getOperationSpeed(stack);
        double capacityRate = getCapicityModifier(stack);
        double speedMultiplier = (20/opSpeed);
        int rfPerTick = (int) (12.5 * speedMultiplier);
        TranslationTextComponent energyRate = new TranslationTextComponent(getTranslationKey() + ".chat_rfrate");
        energyRate.appendString(""+ rfPerTick +"");
        energyRate.mergeStyle(TextFormatting.AQUA);
        player.sendMessage(energyRate,Util.DUMMY_UUID);

        int capacityRateModified = (int)(Math.round((1.0 - getCapicityModifier(stack))* 100));
        TranslationTextComponent rate2 = new TranslationTextComponent(getTranslationKey() + ".chat_rfrate2");
        rate2.appendString("" + capacityRateModified + "%");
        rate2.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate2,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        tooltip.add(name);

        TranslationTextComponent fuelStored = new TranslationTextComponent(getTranslationKey() + ".tooltip_fuelstored");
        fuelStored.appendString(""+ getFuelStored(stack) +"");
        fuelStored.mergeStyle(TextFormatting.DARK_GREEN);
        tooltip.add(fuelStored);

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

        int opSpeed = getOperationSpeed(stack);
        double speedMultiplier = (20/opSpeed);
        int rfPerTick = (int) (12.5 * speedMultiplier);
        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + rfPerTick + "");
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        int capacityRate = (int)(Math.round((1.0 - getCapicityModifier(stack))* 100));
        TranslationTextComponent rate2 = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate2");
        rate2.appendString("" + capacityRate + "%");
        rate2.mergeStyle(TextFormatting.DARK_GRAY);
        tooltip.add(rate2);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item RFFUELGEN = new ItemUpgradeEnergyGenerator(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rffuelgen"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RFFUELGEN);
    }


}
