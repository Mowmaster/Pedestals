package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.network.PacketHandler;
import com.mowmaster.pedestals.network.PacketParticles;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEffectGrower extends ItemUpgradeBase
{
    public ItemUpgradeEffectGrower(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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

    public int getRangeHeight(ItemStack stack)
    {
        return getHeight(stack);
    }

    public int getHeight(ItemStack stack)
    {
        int height = 3;
        switch (getRangeModifier(stack))
        {
            case 0:
                height = 3;
                break;
            case 1:
                height=5;
                break;
            case 2:
                height = 7;
                break;
            case 3:
                height = 9;
                break;
            case 4:
                height = 11;
                break;
            case 5:
                height=13;
                break;
            default: height=3;
        }

        return  height;
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        return new int[]{getRangeHeight(coin),0};
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
            int width = getAreaWidth(coinInPedestal);
            int height = getRangeHeight(coinInPedestal);

            BlockPos negBlockPos = getNegRangePosEntity(world,pedestalPos,width,height);
            BlockPos posBlockPos = getPosRangePosEntity(world,pedestalPos,width,height);

            if(!world.isBlockPowered(pedestalPos)) {
                if (world.getGameTime() % speed == 0) {
                    int currentPosition = pedestal.getStoredValueForUpgrades();
                    BlockPos targetPos = getPosOfNextBlock(currentPosition,negBlockPos,posBlockPos);
                    BlockState targetBlock = world.getBlockState(targetPos);
                    upgradeAction(world, itemInPedestal, pedestalPos, targetPos, targetBlock);
                    pedestal.setStoredValueForUpgrades(currentPosition+1);
                    if(resetCurrentPosInt(currentPosition,negBlockPos,posBlockPos))
                    {
                        pedestal.setStoredValueForUpgrades(0);
                    }
                }


                /*for (int x = negBlockPos.getX(); x <= posBlockPos.getX(); x++) {
                    for (int z = negBlockPos.getZ(); z <= posBlockPos.getZ(); z++) {
                        for (int y = negBlockPos.getY(); y <= posBlockPos.getY(); y++) {
                            BlockPos posTargetBlock = new BlockPos(x, y, z);
                            BlockState targetBlock = world.getBlockState(posTargetBlock);
                            if (world.getGameTime()%speed == 0) {
                                ticked++;
                            }

                            if(ticked > 84)
                            {
                                upgradeAction(world, itemInPedestal, pedestalPos, posTargetBlock, targetBlock);
                                ticked=0;
                            }
                            else
                            {
                                ticked++;
                            }
                        }
                    }
                }*/
            }
        }

    }

    public void upgradeAction(World world, ItemStack itemInPedestal, BlockPos posOfPedestal, BlockPos posTarget, BlockState target)
    {
        ServerWorld sworld = world.getServer().func_241755_D_();
        ItemStack bonemeal = new ItemStack(Items.BONE_MEAL);
        Random rand = new Random();

        if(target.getBlock() instanceof IGrowable || target.getBlock() instanceof IPlantable)
        {
            if (target.getBlock() instanceof IGrowable) {
                if(((IGrowable) target.getBlock()).canGrow(world,posTarget,target,false))
                {
                    if(doItemsMatch(itemInPedestal,bonemeal))
                    {
                        ((IGrowable) target.getBlock()).grow(sworld,rand,posTarget,target);
                        TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                        if(pedestalInv instanceof PedestalTileEntity) {
                            ((PedestalTileEntity) pedestalInv).removeItem(1);
                            //PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.BONEMEAL,posTarget.getX(),posTarget.getY()-0.5f,posTarget.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                            PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,posTarget.getX(),posTarget.getY(),posTarget.getZ(),0,255,0));

                        }
                    }
                    else
                    {
                        PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,posTarget.getX(),posTarget.getY(),posTarget.getZ(),255,255,255));
                        //PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.TICKED,posTarget.getX(),posTarget.getY()-0.5f,posTarget.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                        target.randomTick((ServerWorld) world, posTarget, rand);
                        world.notifyBlockUpdate(posTarget, target, target, 2);
                    }
                }
            }
            else
            {
                PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.ANY_COLOR,posTarget.getX(),posTarget.getY(),posTarget.getZ(),255,255,255));
                //PacketHandler.sendToNearby(world,posOfPedestal,new PacketParticles(PacketParticles.EffectType.TICKED,posTarget.getX(),posTarget.getY()-0.5f,posTarget.getZ(),posOfPedestal.getX(),posOfPedestal.getY(),posOfPedestal.getZ(),5));
                target.randomTick((ServerWorld) world, posTarget, rand);
                world.notifyBlockUpdate(posTarget, target, target, 2);
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
        String trr = "" + getRangeHeight(stack) + "";
        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.DUMMY_UUID);

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
        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        String trr = "" + getRangeHeight(stack) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        area.mergeStyle(TextFormatting.WHITE);
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(area);
        tooltip.add(speed);
    }

    public static final Item GROWER = new ItemUpgradeEffectGrower(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/grower"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(GROWER);
    }


}
