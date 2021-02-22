package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.loot.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnergyGeneratorMob extends ItemUpgradeBaseEnergy
{

    public ItemUpgradeEnergyGeneratorMob(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canSendItem(PedestalTileEntity tile)
    {
        return tile.getStoredValueForUpgrades()>0;
    }

    public int getEnergyBuffer(ItemStack stack) {
        return  40000;
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

            if(!world.isBlockPowered(pedestalPos))
            {
                //Always send energy, as fast as we can within the Pedestal Energy Network
                upgradeActionSendEnergy(pedestal);
                //only run 1 per second
                if (world.getGameTime()%20 == 0) {
                    upgradeAction(world,pedestalPos,itemInPedestal,coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinInPedestal)
    {
        /*
        Okay so basically this generator bases its speed and efficiency off of mobs 'connected' to it
        It will consume 1 mob drop as fuel as well as damage the corrosponding mob
        Any mobs can be used, but the more variety and the different types will only improve generation

        speed enchant will make the generator run faster.
        capacity enchant will modify the base fuel efficiency

        'Connected Mobs' basically means every "cycle" the generator runs, it will check 'below' all its linked pedestals for mob entities,
        if it finds an entity it will save it to a list, as it builds its list it will also be checking to see if any mobs in the list are similar types,
        for each different type of mob there will be a modifier that multiplies the base generation. More diverse the mobs, the more energy per tick.
        {Im thinking 1type: 1.0, 2:2, 3:4, 4:8. 5:16, 6:32. 7:64, 8:128} but i should probably calculate my max vanilla diversity:power combination and set it to a max of 20k/t since thats the max the export can output???
        ALSO for each mob it will check thats mobs 'difficulty', passive ones will be worth less, while hostile ones will be worth more. this will give a flat additive base power gen.
        {i might have to make a specific class by class 'base fuel'rate for this. no idea if that could be jsoned, but hopefully???}

        Once the list is built the generator will randomly select a mob from the list, it will get a list of that mobs drops, and then select 1 from the drops(probably the first item in the list).
        It will then request this item as fuel (Displaying the item above the generator)[maybe also output redstone = to which number in the list the mob is? the list will be in the order of connected pedestals]
        upon being given an item, it will verify that it was the correct item, if not that item might be voided(maybe??? unless i can dynamically set that in the onCollide)
        if the correct item was received it will damage the corrosponding entity for 1 heart, and then start to generate power. Once all the 'fuel' has been exhausted it will repeat the list and request process all over again.

        Mobs connected could die, as long as a mob is available before the generator runs its initial check for the cycle, it will be okay.
        {Should i make an additional bonus if the generator kills the mob??? this might give people a choice, to try to keep the mobs alive, or to make a system to refill when mobs die???)

        I want to do fancy effects with this, like making the selected mob glow, the item for fuel being displayed, showing the generator sending particles to 'hurt' the mob, and maybe particles showing the life force being consumed.
        Effects on the generator that maybe shows the diversity or difficulty.
        Generator running effects???
         */


        /*
        With this first test of getting mobs in the area, we can get attributes and the loot table, which is awesome.
        Basically with this since for whatever reason the loot tabel thing likes to return ItemStack.EMPTY we could just
        Have the generator run the code until it gets a result, and maybe have a counter so if it doesnt get a result after ~5-10 runs then default it to something.
        In theory if we just went with a custom recipe handler based off the mobs registry info (somehow) then packs could specify the specific drops of any entity, and if they didnt
        then i could just assume the drops based off the loot table.
        The generator maybe needs to store a list of the mobs at which location, so we can then randomly pick a location, check if the mob is still alive(attach a custom attribute???), and if so we could just store the drop item so we dont have to request it each time.

        To get a drop to display i might need to make a custom renderer,

        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,1,3);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,1,3);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);
        List<LivingEntity> entityList = world.getEntitiesWithinAABB(LivingEntity.class,getBox);
        for(LivingEntity getEntityFromList : entityList)
        {
            System.out.println(getEntityFromList.getDisplayName().getString());
            System.out.println(getEntityFromList.getLootTableResourceLocation());
            System.out.println(getEntityFromList.getAttribute(Attributes.MAX_HEALTH).getValue());

            FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) world,new GameProfile(getPlayerFromCoin(coinInPedestal),"[Pedestals]"));
            fakePlayer.setPosition(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ());
            if (itemInPedestal.getItem() instanceof SwordItem) {
                fakePlayer.setHeldItem(Hand.MAIN_HAND, itemInPedestal);
            }
            DamageSource source = DamageSource.causePlayerDamage(fakePlayer);
            LootTable table = world.getServer().getLootTableManager().getLootTableFromLocation(getEntityFromList.getLootTableResourceLocation());
            LootContext.Builder context = new LootContext.Builder((ServerWorld) world)
                    .withRandom(world.rand)
                    .withParameter(LootParameters.THIS_ENTITY, getEntityFromList)
                    .withParameter(LootParameters.DAMAGE_SOURCE, source)
                    .withParameter(LootParameters.field_237457_g_, new Vector3d(posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ()))
                    .withParameter(LootParameters.KILLER_ENTITY, fakePlayer)
                    .withParameter(LootParameters.LAST_DAMAGE_PLAYER, fakePlayer)
                    .withNullableParameter(LootParameters.DIRECT_KILLER_ENTITY, fakePlayer);
            //table.generate(context.build(LootParameterSets.ENTITY)).forEach(stack -> System.out.println(stack.getDisplayName().getString()));

            ItemStack stackDrop = ItemStack.EMPTY;
            stackDrop = IntStream.range(0,table.generate(context.build(LootParameterSets.ENTITY)).size())//Int Range
                    .mapToObj(table.generate(context.build(LootParameterSets.ENTITY))::get)//Function being applied to each interval
                    .filter(itemStack -> !itemStack.isEmpty())
                    .findFirst().orElse(ItemStack.EMPTY);
            System.out.println(stackDrop.getDisplayName().getString());

        }
        */
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

    //Any item fuel value will be 1600
    public static int getItemFuelBurnTime(ItemStack fuel)
    {
        if (fuel.isEmpty()) return 0;
        else
        {
            return 1600;
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
                    /*if(getItemFuelBurnTime(getItemStack)>0)
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
                    }*/
                }
            }
        }
    }

    /*@Override
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
    }*/

    public static final Item RFMOBGEN = new ItemUpgradeEnergyGeneratorMob(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rfmobgen"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RFMOBGEN);
    }


}
