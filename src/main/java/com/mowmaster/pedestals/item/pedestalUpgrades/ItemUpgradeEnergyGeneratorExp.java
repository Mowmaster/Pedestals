package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.references.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnergyGeneratorExp extends ItemUpgradeBaseEnergy
{

    public ItemUpgradeEnergyGeneratorExp(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    public double getEnchantmentCapacityModifier(PedestalTileEntity pedestalTileEntity)
    {
        double rate = (double)((getEnchantmentPowerFromSorroundings(pedestalTileEntity))*0.01);
        double rater = (rate > 0.9)?(0.9):(rate);

        return  (1.0-rater);
    }

    public int getEnergyBuffer(ItemStack stack) {
        double speed = getOperationSpeedOverride(stack);
        //55800 is 30 levels of exp as fuel 1395*40
        double fuelSpeedMultiplier = Math.floor(getFuelStored(stack)/55800)*2;
        double speedMultiplier = (20/speed)*((fuelSpeedMultiplier>=1)?(fuelSpeedMultiplier):(1));
        return  (int)(3200*speedMultiplier);
    }

    public float getEnchantmentPowerFromSorroundings(PedestalTileEntity pedestalTileEntity)
    {
        World world = pedestalTileEntity.getWorld();
        BlockPos posOfPedestal = pedestalTileEntity.getPos();

        float enchantPower = 0;

        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (i > -2 && i < 2 && j == -1) {
                    j = 2;
                }
                for (int k = 0; k <= 2; ++k) {
                    BlockPos blockpos = posOfPedestal.add(i, k, j);
                    BlockState blockNearBy = world.getBlockState(blockpos);
                    if (blockNearBy.getBlock().getEnchantPowerBonus(blockNearBy, world, blockpos)>0)
                    {
                        enchantPower +=blockNearBy.getBlock().getEnchantPowerBonus(blockNearBy, world, blockpos);
                    }
                }
            }
        }

        return enchantPower;
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
                        if(!pedestal.hasParticleDiffuser())PacketHandler.sendToNearby(world,pedestalPos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,directionalPos.getX(),directionalPos.getY(),directionalPos.getZ(),145,145,145));
                    }

                    if (world.getGameTime()%20 == 0) {
                        upgradeAction(pedestal);
                    }
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        BlockPos posOfPedestal = pedestal.getPos();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        double speed = getOperationSpeedOverride(coinInPedestal);
        double capacityRate = getEnchantmentCapacityModifier(pedestal);
        int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
        if(!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {setMaxEnergy(coinInPedestal, getMaxEnergyValue);}

        //Generator when it has fuel, make energy every second based on capacity and speed
        //20k per 1 coal is base fuel value (2500 needed in mod to process 1 item)
        //1 coal takes 1600 ticks to process default??? furnace uses 2500 per 10 seconds by default
        //so 12.5 energy per tick (250/sec)
        double fuelSpeedMultiplier = Math.floor(getFuelStored(coinInPedestal)/55800)*2;
        double speedMultiplier = (20/speed)*((fuelSpeedMultiplier>=1)?(fuelSpeedMultiplier):(1));
        int baseFuel = (int) (20 * speedMultiplier);
        int fuelConsumed = (int) Math.ceil(baseFuel * capacityRate);
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
        int fuelConsumed = (int) Math.ceil(baseFuel * capacityRate);
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
            int actualFuelConsumed = (int)Math.ceil(fuelCanConsume * capacityRate);
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

    public int getItemsExpDisenchantAmount(ItemStack stack)
    {
        int exp = 0;
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer integer = entry.getValue();

            exp += enchantment.getMinEnchantability(integer.intValue());
        }
        return exp;
    }

    public int getItemFuelBurnTime(ItemStack fuel)
    {
        //TODO: add config for this later
        int expToFuelModifier = 40;
        if(fuel.isEnchanted() || fuel.getItem() instanceof EnchantedBookItem){
            return (getItemsExpDisenchantAmount(fuel)*expToFuelModifier);
        }
        else return 0;
    }

    @Override
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(!world.isRemote)
        {
            if(!world.isBlockPowered(posPedestal))
            {
                ItemStack itemInPedetsal = tilePedestal.getItemInPedestal();
                if(itemInPedetsal.isEmpty())
                {
                    if(entityIn instanceof ItemEntity)
                    {
                        ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
                        if(getItemFuelBurnTime(getItemStack)>0 && canThisPedestalReceiveItemStack(tilePedestal,world,posPedestal,getItemStack))
                        {
                            int getBurnTimeForStack = getItemFuelBurnTime(getItemStack) * getItemStack.getCount();
                            Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                            ItemStack stackToReturn = (getItemStack.getItem() instanceof EnchantedBookItem)?(new ItemStack(Items.BOOK)):(getItemStack.copy());
                            stackToReturn.setCount(1);
                            EnchantmentHelper.setEnchantments(enchantsNone,stackToReturn);
                            if(!stackToReturn.isEmpty())
                            {
                                if(addFuel(tilePedestal,getBurnTimeForStack,true) && tilePedestal.addItem(stackToReturn,true))
                                {
                                    addFuel(tilePedestal,getBurnTimeForStack,false);
                                    if(!tilePedestal.hasMuffler())world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                    entityIn.remove();
                                    tilePedestal.addItem(stackToReturn,false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        super.onRandomDisplayTick(pedestal,tick,stateIn,world,pos,rand);
        ItemStack coin = pedestal.getCoinOnPedestal();
        float level = getEnchantmentPowerFromSorroundings(pedestal);
        if(!pedestal.hasParticleDiffuser())
        {
            if(!world.isBlockPowered(pos))
            {
                for (int i = -2; i <= 2; ++i)
                {
                    for (int j = -2; j <= 2; ++j)
                    {
                        if (i > -2 && i < 2 && j == -1)
                        {
                            j = 2;
                        }

                        if (rand.nextInt(16) == 0)
                        {
                            for (int k = 0; k <= 2; ++k)
                            {
                                BlockPos blockpos = pos.add(i, k, j);

                                if (world.getBlockState(blockpos).getEnchantPowerBonus(world, pos) > 0) {
                                    if (!world.isAirBlock(pos.add(i / 2, 0, j / 2))) {
                                        break;
                                    }

                                    world.addParticle(ParticleTypes.ENCHANT, (double)pos.getX() + 0.5D, (double)pos.getY() + 2.0D, (double)pos.getZ() + 0.5D, (double)((float)i + rand.nextFloat()) - 0.5D, (double)((float)k - rand.nextFloat() - 1.0F), (double)((float)j + rand.nextFloat()) - 0.5D);
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
        double fuelSpeedMultiplier = Math.floor(getFuelStored(stack)/55800)*2;
        double speedMultiplier = (20/opSpeed)*((fuelSpeedMultiplier>=1)?(fuelSpeedMultiplier):(1));
        int rfPerTick = (int) (12.5 * speedMultiplier);
        TranslationTextComponent energyRate = new TranslationTextComponent(getTranslationKey() + ".chat_rfrate");
        energyRate.appendString(""+ rfPerTick +"");
        energyRate.mergeStyle(TextFormatting.AQUA);
        player.sendMessage(energyRate,Util.DUMMY_UUID);

        int capacityRateModified = (int)(Math.round((1.0 - getEnchantmentCapacityModifier(pedestal))* 100));
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

        if(getAdvancedModifier(stack)<=0 && (intOperationalSpeedOver(stack) >5 || getCapacityModifierOver(stack) >5 || getAreaModifierUnRestricted(stack) >5 || getRangeModifier(stack) >5))
        {
            TranslationTextComponent warning = new TranslationTextComponent(Reference.MODID + ".advanced_warning");
            warning.mergeStyle(TextFormatting.RED);
            tooltip.add(warning);
        }

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
        double fuelSpeedMultiplier = Math.floor(getFuelStored(stack)/55800)*2;
        double speedMultiplier = (20/opSpeed)*((fuelSpeedMultiplier>=1)?(fuelSpeedMultiplier):(1));
        int rfPerTick = (int) (12.5 * speedMultiplier);
        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + rfPerTick + "");
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent rate2 = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate2");
        //Its 0% unless theres bookshelves around it LOL
        rate2.appendString("" + 0 + "%");
        rate2.mergeStyle(TextFormatting.DARK_GRAY);
        tooltip.add(rate2);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item RFEXPGEN = new ItemUpgradeEnergyGeneratorExp(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rfexpgen"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RFEXPGEN);
    }


}
