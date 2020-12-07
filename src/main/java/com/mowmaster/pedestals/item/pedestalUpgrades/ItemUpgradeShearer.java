package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeShearer extends ItemUpgradeBase
{
    public int rangeHeight = 1;

    public ItemUpgradeShearer(Properties builder) {super(builder.group(PEDESTALS_TAB));}


    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {return true;}

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
        return new int[]{2,0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(world, itemInPedestal,pedestalPos, coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, BlockPos pedestalPos, ItemStack coinInPedestal)
    {
        int width = getAreaWidth(coinInPedestal);
        BlockPos negBlockPos = getNegRangePosEntity(world,pedestalPos,width,rangeHeight);
        BlockPos posBlockPos = getPosRangePosEntity(world,pedestalPos,width,rangeHeight);
        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);
        //Entity Creature could be used to cover creepers for better with mods and such
        List<LivingEntity> baa = world.getEntitiesWithinAABB(LivingEntity.class,getBox);

        for(LivingEntity baaaaaa : baa)
        {
            if(baaaaaa instanceof IForgeShearable)
            {
                IForgeShearable baabaa = (IForgeShearable)baaaaaa;
                if (baabaa.isShearable(new ItemStack(Items.SHEARS),world,new BlockPos(baaaaaa.getPositionVec())))
                {
                    if(getStackInPedestal(world,pedestalPos).equals(ItemStack.EMPTY))
                    {
                        Random rando = new Random();
                        int i = 1 + rando.nextInt(3);
                        List<ItemStack> drops = baabaa.onSheared(null,new ItemStack(Items.SHEARS),world,new BlockPos(baaaaaa.getPositionVec()),0);

                        for (int j = 0; j < i; ++j)
                        {
                            if(drops.size()>0)
                            {
                                for (int d=0;d<drops.size();d++)
                                {
                                    if(itemInPedestal.isEmpty() || drops.get(d).equals(itemInPedestal) && canAddToPedestal(world,pedestalPos,drops.get(d)) >= drops.get(d).getCount())
                                    {
                                        BlockPos sheerie = baaaaaa.getPosition();
                                        PacketHandler.sendToNearby(world,pedestalPos,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,sheerie.getX(),sheerie.getY()+0.5,sheerie.getZ(),145,145,145));
                                        world.playSound((PlayerEntity) null, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                        addToPedestal(world,pedestalPos,drops.get(d));
                                    }
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
        player.sendMessage(name,Util.DUMMY_UUID);

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString("2");
        area.appendString(areax.getString());
        area.appendString(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));

        area.mergeStyle(TextFormatting.WHITE);
        speed.mergeStyle(TextFormatting.RED);

        player.sendMessage(area,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";

        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString("2");
        area.appendString(areax.getString());
        area.appendString(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        area.mergeStyle(TextFormatting.WHITE);
        tooltip.add(area);

        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item SHEARER = new ItemUpgradeShearer(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/shearer"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(SHEARER);
    }


}
