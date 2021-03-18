package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.Block;
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
    public ItemUpgradeEffect(Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return false;
    }

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    public int getAreaWidth(ItemStack stack)
    {
        int areaWidth = 0;
        int aW = getAreaModifier(stack);
        areaWidth = ((aW)+1);
        return  areaWidth;
    }

    public int getHeight(ItemStack stack)
    {
        return  getRangeTiny(stack);
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{(getHeight(coin)),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isClientSide)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getBlockPos();

            int getMaxFuelValue = 2000000000;
            if(!hasMaxFuelSet(coinInPedestal) || readMaxFuelFromNBT(coinInPedestal) != getMaxFuelValue) {setMaxFuel(coinInPedestal, getMaxFuelValue);}

            int speed = getOperationSpeed(coinInPedestal);
            if(!world.hasNeighborSignal(pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal);
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



    public void upgradeAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getLevel();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos posOfPedestal = pedestal.getBlockPos();
        int width = getAreaWidth(coinInPedestal);
        int height = getHeight(coinInPedestal);
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getBlockPosRangePosEntity(world,posOfPedestal,width,height);
        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);
        if(!hasFilterBlock(coinInPedestal)) {writeFilterBlockToNBT(pedestal);}
        Block filterBlock = readFilterBlockFromNBT(coinInPedestal);

        List<EffectInstance> instance = getEffectFromPedestal(itemInPedestal,0);
        if(instance.size() > 0)
        {
            List<LivingEntity> entityList = world.getEntitiesWithinAABB(LivingEntity.class,getBox);
            if(entityList.size() > 0)
            {
                for(LivingEntity getEntityFromList : entityList)
                {
                    if(filterBlock.equals(Blocks.NETHERITE_BLOCK))
                    {
                        instance = getEffectFromPedestal(itemInPedestal,1);
                    }

                    if(getTargetEntity(filterBlock,getEntityFromList) != null)
                    {
                        if(!hasPotionEffect(getTargetEntity(filterBlock,getEntityFromList),instance))
                        {
                            for(int i=0; i<instance.size(); i++)
                            {
                                if(removeFuel(world,posOfPedestal,(instance.get(i).getAmplifier()+1),true))
                                {
                                    if(getTargetEntity(filterBlock,getEntityFromList).addPotionEffect(instance.get(i)))
                                    {
                                        world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                        removeFuel(world,posOfPedestal,(instance.get(i).getAmplifier()+1),false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void onPedestalBelowNeighborChanged(PedestalTileEntity pedestal, BlockState blockChanged, BlockPos blockChangedPos)
    {
        BlockPos blockBelow = getBlockPosOfBlockBelow(pedestal.getLevel(),pedestal.getBlockPos(),1);
        if(blockBelow.equals(blockChangedPos))
        {
            writeFilterBlockToNBT(pedestal);
        }
    }

    @Override
    public String getOperationSpeedString(ItemStack stack)
    {
        TranslationTextComponent normal = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_0");
        TranslationTextComponent twox = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_1");
        TranslationTextComponent fourx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_2");
        TranslationTextComponent sixx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_3");
        TranslationTextComponent tenx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_4");
        TranslationTextComponent twentyx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_5");
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
                str = sixx.getString();//6x faster
                break;
            case 4:
                str = tenx.getString();//10x faster
                break;
            case 5:
                str = twentyx.getString();//20x faster
                break;
            default: str = normal.getString();;
        }

        return  str;
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
        if(!world.hasNeighborSignal(pos))
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
        Block filterBlock = (hasFilterBlock(stack))?(readFilterBlockFromNBT(stack)):(Blocks.AIR);

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";

        TranslationTextComponent name = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        name.withStyle(TextFormatting.GOLD);
        player.sendMessage(name, Util.NIL_UUID);

        TranslationTextComponent area = new TranslationTextComponent(getDescriptionId() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getDescriptionId() + ".chat_areax");
        area.append(tr);
        area.append(areax.getString());
        area.append("" + getHeight(stack) + "");
        area.append(areax.getString());
        area.append(tr);
        area.withStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.NIL_UUID);


        //Display Fuel Left
        int fuelLeft = getFuelStored(pedestal.getCoinOnPedestal());
        TranslationTextComponent fuel = new TranslationTextComponent(getDescriptionId() + ".chat_fuel");
        fuel.append("" + fuelLeft + "");
        fuel.withStyle(TextFormatting.GREEN);
        player.sendMessage(fuel,Util.NIL_UUID);

        //Displays what effects are in pedestal
        List<EffectInstance> instance = getEffectFromPedestal(pedestal.getItemInPedestal(),1);
        TranslationTextComponent effect = new TranslationTextComponent(getDescriptionId() + ".chat_effect");
        effect.withStyle(TextFormatting.AQUA);
        player.sendMessage(effect,Util.NIL_UUID);
        for(int i = 0; i < instance.size();i++)
        {
            TranslationTextComponent effects = new TranslationTextComponent(instance.get(i).getPotion().getDisplayName().getString());
            effects.withStyle(TextFormatting.GRAY);
            player.sendMessage(effects,Util.NIL_UUID);
        }

        TranslationTextComponent entityType = new TranslationTextComponent(getDescriptionId() + ".chat_entity");
        entityType.append(getTargetEntity(filterBlock));
        entityType.withStyle(TextFormatting.YELLOW);
        player.sendMessage(entityType,Util.NIL_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".chat_speed");
        speed.append(getOperationSpeedString(stack));
        speed.withStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.NIL_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        TranslationTextComponent t = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        t.withStyle(TextFormatting.GOLD);
        tooltip.add(t);

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getDescriptionId() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getDescriptionId() + ".tooltip_areax");
        area.append(tr);
        area.append(areax.getString());
        area.append("" + getHeight(stack) + "");
        area.append(areax.getString());
        area.append(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".tooltip_speed");
        speed.append(getOperationSpeedString(stack));

        area.withStyle(TextFormatting.WHITE);
        speed.withStyle(TextFormatting.RED);

        tooltip.add(area);

        TranslationTextComponent fuelStored = new TranslationTextComponent(getDescriptionId() + ".tooltip_fuelstored");
        fuelStored.append(""+ getFuelStored(stack) +"");
        fuelStored.withStyle(TextFormatting.GREEN);
        tooltip.add(fuelStored);

        tooltip.add(speed);
    }

    public static final Item EFFECT = new ItemUpgradeEffect(new Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/effect"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(EFFECT);
    }


}
