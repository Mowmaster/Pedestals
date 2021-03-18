package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
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
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeExpDropper extends ItemUpgradeBaseExp
{
    public int range = 0;

    public ItemUpgradeExpDropper(Item.Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    public int getTransferRate(ItemStack stack)
    {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int overEnchanted = capacityOver*5;
        int summonRate = 1;
        switch (capacityOver)
        {
            case 0:
                summonRate = 1;//1
                break;
            case 1:
                summonRate=5;//2
                break;
            case 2:
                summonRate = 10;//4
                break;
            case 3:
                summonRate = 15;//6
                break;
            case 4:
                summonRate = 20;//8
                break;
            case 5:
                summonRate=25;//10
                break;
            default: summonRate=(overEnchanted>maxLVLStored)?(maxLVLStored):(overEnchanted);
        }

        return  summonRate;
    }

    @Override
    public int getWorkAreaX(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRangeSmall(coin);
        return getBlockPosOfBlockBelow(world,pos,-range).getX();
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRangeSmall(coin);
        return new int[]{getBlockPosOfBlockBelow(world,pos,-range).getY(),1};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRangeSmall(coin);
        return getBlockPosOfBlockBelow(world,pos,-range).getZ();
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isClientSide)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getBlockPos();

            int getMaxXpValue = getExpCountByLevel(getExpBuffer(coinInPedestal));
            if(!hasMaxXpSet(coinInPedestal)) {setMaxXP(coinInPedestal,getMaxXpValue);}

            int speed = getOperationSpeed(coinInPedestal);
            if(!world.hasNeighborSignal(pedestalPos))
            {
                if (world.getGameTime()%speed == 0 && getXPStored(coinInPedestal)>0) {
                    upgradeAction(world, coinInPedestal, pedestalPos);
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int rate = getExpCountByLevel(getTransferRate(coinInPedestal));
        int range = getRangeSmall(coinInPedestal);

        TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
        if(pedestalInv instanceof PedestalTileEntity) {
            int currentlyStoredExp = getXPStored(coinInPedestal);
            if(currentlyStoredExp > 0)
            {
                if(currentlyStoredExp < rate)
                {
                    rate = currentlyStoredExp;
                }

                ExperienceOrbEntity expEntity = new ExperienceOrbEntity(world,getBlockPosOfBlockBelow(world,posOfPedestal,-range).getX() + 0.5,getBlockPosOfBlockBelow(world,posOfPedestal,-range).getY(),getBlockPosOfBlockBelow(world,posOfPedestal,-range).getZ() + 0.5,rate);
                expEntity.setMotion(0D,0D,0D);

                int getExpLeftInPedestal = currentlyStoredExp - rate;
                world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.BLOCKS, 0.25F, 1.0F);
                setXPStored(coinInPedestal,getExpLeftInPedestal);
                world.addEntity(expEntity);
            }
        }
    }

    @Override
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn) {

    }

    @Override
    public int getExpBuffer(ItemStack stack)
    {
        int capacityOver = getCapacityModifierOverEnchanted(stack);
        int overEnchanted = (capacityOver*5)+5;

        //20k being the max before we get close to int overflow
        return  (overEnchanted>=maxLVLStored)?(maxLVLStored):(overEnchanted);
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();
        int tr = getTransferRate(stack);

        TranslationTextComponent name = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        name.withStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.NIL_UUID);

        TranslationTextComponent xpstored = new TranslationTextComponent(getDescriptionId() + ".chat_xp");
        xpstored.append(""+ getExpLevelFromCount(getXPStored(stack)) +"");
        xpstored.withStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored,Util.NIL_UUID);

        TranslationTextComponent range = new TranslationTextComponent(getDescriptionId() + ".chat_range");
        range.append("" +  getRangeSmall(stack) + "");
        range.withStyle(TextFormatting.WHITE);
        player.sendMessage(range, Util.NIL_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".chat_rate");
        rate.append("" +  tr + "");
        rate.withStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.NIL_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".chat_speed");
        speed.append(getOperationSpeedString(stack));
        speed.withStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.NIL_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int tr = getTransferRate(stack);
        TranslationTextComponent range = new TranslationTextComponent(getDescriptionId() + ".tooltip_range");
        range.append("" +  getRangeSmall(stack) + "");
        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".tooltip_rate");
        rate.append("" +  tr + "");

        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".tooltip_speed");
        speed.append(getOperationSpeedString(stack));

        range.withStyle(TextFormatting.WHITE);
        rate.withStyle(TextFormatting.GRAY);
        speed.withStyle(TextFormatting.RED);

        tooltip.add(range);
        tooltip.add(rate);
        tooltip.add(speed);
    }

    public static final Item XPDROPPER = new ItemUpgradeExpDropper(new Item.Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xpdropper"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPDROPPER);
    }


}
