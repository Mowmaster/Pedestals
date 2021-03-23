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

    public ItemUpgradeExpDropper(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

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
        return getPosOfBlockBelow(world,pos,-range).getX();
    }

    @Override
    public int[] getWorkAreaY(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRangeSmall(coin);
        return new int[]{getPosOfBlockBelow(world,pos,-range).getY(),1};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        int range = getRangeSmall(coin);
        return getPosOfBlockBelow(world,pos,-range).getZ();
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int getMaxXpValue = getExpCountByLevel(getExpBuffer(coinInPedestal));
            if(!hasMaxXpSet(coinInPedestal)) {setMaxXP(coinInPedestal,getMaxXpValue);}

            int speed = getOperationSpeed(coinInPedestal);
            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
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
            PedestalTileEntity pedestal = ((PedestalTileEntity) pedestalInv);
            int currentlyStoredExp = getXPStored(coinInPedestal);
            if(currentlyStoredExp > 0)
            {
                if(currentlyStoredExp < rate)
                {
                    rate = currentlyStoredExp;
                }

                ExperienceOrbEntity expEntity = new ExperienceOrbEntity(world,getPosOfBlockBelow(world,posOfPedestal,-range).getX() + 0.5,getPosOfBlockBelow(world,posOfPedestal,-range).getY(),getPosOfBlockBelow(world,posOfPedestal,-range).getZ() + 0.5,rate);
                expEntity.setMotion(0D,0D,0D);

                int getExpLeftInPedestal = currentlyStoredExp - rate;
                if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.BLOCKS, 0.25F, 1.0F);
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

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".chat_xp");
        xpstored.appendString(""+ getExpLevelFromCount(getXPStored(stack)) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored,Util.DUMMY_UUID);

        TranslationTextComponent range = new TranslationTextComponent(getTranslationKey() + ".chat_range");
        range.appendString("" +  getRangeSmall(stack) + "");
        range.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(range, Util.DUMMY_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString("" +  tr + "");
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

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

        int tr = getTransferRate(stack);
        TranslationTextComponent range = new TranslationTextComponent(getTranslationKey() + ".tooltip_range");
        range.appendString("" +  getRangeSmall(stack) + "");
        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" +  tr + "");

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        range.mergeStyle(TextFormatting.WHITE);
        rate.mergeStyle(TextFormatting.GRAY);
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(range);
        tooltip.add(rate);
        tooltip.add(speed);
    }

    public static final Item XPDROPPER = new ItemUpgradeExpDropper(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/xpdropper"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(XPDROPPER);
    }


}
