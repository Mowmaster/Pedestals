package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEffect extends ItemUpgradeBaseMachine
{
    public ItemUpgradeEffect(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return false;
    }

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    public int getAreaWidth(ItemStack stack)
    {
        int areaWidth = 0;
        int aW = getAreaModifier(stack);
        areaWidth = ((aW)+1);
        return  areaWidth;
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{((2*getAreaWidth(coin))+1),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int getMaxFuelValue = Integer.MAX_VALUE;
            if(!hasMaxFuelSet(coinInPedestal) || readMaxFuelFromNBT(coinInPedestal) != getMaxFuelValue) {setMaxFuel(coinInPedestal, getMaxFuelValue);}

            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos))
            {
                if (tick%speed == 0) {
                    upgradeAction(world, itemInPedestal, coinInPedestal, pedestalPos);
                }
            }
        }

    }

    public List<EffectInstance> getEffectFromPedestal(ItemStack itemInPedestal,int modifier)
    {
        List<EffectInstance> effectInstance = PotionUtils.getEffectsFromStack(itemInPedestal);
        List<EffectInstance> effectInstanceReturner = new ArrayList<>();
        if(!itemInPedestal.isEmpty() && effectInstance.size() > 0)
        {
            for(int i=0; i<effectInstance.size(); i++)
            {
                if(!effectInstance.get(i).getPotion().isInstant())
                {
                    Effect getEffect = PotionUtils.getEffectsFromStack(itemInPedestal).get(i).getPotion();
                    int getAmp = PotionUtils.getEffectsFromStack(itemInPedestal).get(i).getAmplifier() + modifier;
                    int getDuration = PotionUtils.getEffectsFromStack(itemInPedestal).get(i).getDuration() * ((modifier==0)?(1):(modifier+1));

                    effectInstanceReturner.add(new EffectInstance(getEffect,getDuration,getAmp,true,true));
                }
            }
        }
        return effectInstanceReturner;
    }

    public boolean hasPotionEffect(LivingEntity entityIn, List<EffectInstance> effectIn)
    {
        if(entityIn instanceof LivingEntity)
        {
            if(effectIn.size() > 0)
            {
                for(int i = 0;i < effectIn.size(); i++)
                {
                    if(entityIn.getActivePotionEffect(effectIn.get(i).getPotion()) != null)
                    {
                        if(entityIn.getActivePotionEffect(effectIn.get(i).getPotion()).getAmplifier() >= effectIn.get(i).getAmplifier() && entityIn.getActivePotionEffect(effectIn.get(i).getPotion()).getDuration() >= 100)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean removeFuel(World world, BlockPos posPedestal, int amountToRemove, boolean simulate)
    {

        TileEntity entity = world.getTileEntity(posPedestal);
        if(entity instanceof PedestalTileEntity)
        {
            PedestalTileEntity pedestal = (PedestalTileEntity)entity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            int fuelLeft = getFuelStored(coin);
            if(amountToRemove <= fuelLeft)
            {
                if(!simulate)
                {
                    removeFuel(pedestal,amountToRemove,simulate);
                    return true;
                    //pedestal.setStoredValueForUpgrades(amountToSet);
                }

                return true;
            }
        }

        return false;
    }



    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int width = getAreaWidth(coinInPedestal);
        int height = (2*width)+1;
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);

        List<EffectInstance> instance = getEffectFromPedestal(itemInPedestal,0);
        if(instance.size() > 0)
        {
            List<LivingEntity> entityList = world.getEntitiesWithinAABB(LivingEntity.class,getBox);
            for(LivingEntity getEntityFromList : entityList)
            {
                if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.NETHERITE_BLOCK))
                {
                    instance = getEffectFromPedestal(itemInPedestal,1);
                }

                if(getTargetEntity(world,posOfPedestal,getEntityFromList) != null)
                {
                    if(!hasPotionEffect(getTargetEntity(world,posOfPedestal,getEntityFromList),instance))
                    {
                        for(int i=0; i<instance.size(); i++)
                        {
                            if(removeFuel(world,posOfPedestal,(instance.get(i).getAmplifier()+1),true))
                            {
                                if(getTargetEntity(world,posOfPedestal,getEntityFromList).addPotionEffect(instance.get(i)))
                                {
                                    world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                    removeFuel(world,posOfPedestal,(instance.get(i).getAmplifier()+1),false);
                                }
                                /*Removed due to a suggestion in a stream #BlameSoaryn (I want to make it clear, i was on the fence about this anyway but left it in for "balance" reasons)
                                else
                                {
                                    removeFromPedestal(world,posOfPedestal,1);
                                    world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                    removeFuel(world,posOfPedestal,(instance.get(i).getAmplifier()+1),false);
                                }*/
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
            if(getItemStack.getItem().equals(Items.BLAZE_POWDER))
            {
                int getBurnTimeForStack = 4 * getItemStack.getCount();
                if(addFuel(tilePedestal,getBurnTimeForStack,true))
                {
                    addFuel(tilePedestal,getBurnTimeForStack,false);
                    entityIn.remove();
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!world.isBlockPowered(pos))
        {
            int fuelValue = getFuelStored(pedestal.getCoinOnPedestal());

            if(fuelValue > 0)
            {
                spawnParticleAroundPedestalBase(world,tick,pos, ParticleTypes.EFFECT);
            }
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();
        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name, Util.DUMMY_UUID);

        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.DUMMY_UUID);


        //Display Fuel Left
        int fuelLeft = getFuelStored(pedestal.getCoinOnPedestal());
        TranslationTextComponent fuel = new TranslationTextComponent(getTranslationKey() + ".chat_fuel");
        fuel.appendString("" + fuelLeft + "");
        fuel.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(fuel,Util.DUMMY_UUID);

        //Displays what effects are in pedestal
        List<EffectInstance> instance = getEffectFromPedestal(pedestal.getItemInPedestal(),1);
        TranslationTextComponent effect = new TranslationTextComponent(getTranslationKey() + ".chat_effect");
        effect.mergeStyle(TextFormatting.AQUA);
        player.sendMessage(effect,Util.DUMMY_UUID);
        for(int i = 0; i < instance.size();i++)
        {
            TranslationTextComponent effects = new TranslationTextComponent(instance.get(i).getPotion().getDisplayName().getString());
            effects.mergeStyle(TextFormatting.GRAY);
            player.sendMessage(effects,Util.DUMMY_UUID);
        }

        TranslationTextComponent entityType = new TranslationTextComponent(getTranslationKey() + ".chat_entity");
        entityType.appendString(getTargetEntity(pedestal.getWorld(),pedestal.getPos()));
        entityType.mergeStyle(TextFormatting.YELLOW);
        player.sendMessage(entityType,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        //super.addInformation(stack, worldIn, tooltip, flagIn);
        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        area.mergeStyle(TextFormatting.WHITE);
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(area);

        TranslationTextComponent fuelStored = new TranslationTextComponent(getTranslationKey() + ".tooltip_fuelstored");
        fuelStored.appendString(""+ getFuelStored(stack) +"");
        fuelStored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(fuelStored);

        tooltip.add(speed);
    }

    public static final Item EFFECT = new ItemUpgradeEffect(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/effect"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(EFFECT);
    }


}
