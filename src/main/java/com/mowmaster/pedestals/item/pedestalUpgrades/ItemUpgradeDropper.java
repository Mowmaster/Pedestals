package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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

public class ItemUpgradeDropper extends ItemUpgradeBase
{
    public ItemUpgradeDropper(Item.Properties builder) {super(builder.tab(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    @Override
    public Boolean canAcceptCapacity() {
        return true;
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

            int speed = getOperationSpeed(coinInPedestal);
            if(!world.hasNeighborSignal(pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(world,pedestalPos,itemInPedestal,coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack itemInPedestal, ItemStack coinOnPedestal)
    {
        int rate = getItemTransferRate(coinOnPedestal);
        int actualRate = (itemInPedestal.getCount() < rate)?(itemInPedestal.getCount()):(rate);

        int range = getRangeSmall(coinOnPedestal);
        if(!getStackInPedestal(world,posOfPedestal).isEmpty())//hasItem
        {
            ItemStack itemToSummon = getStackInPedestal(world,posOfPedestal).copy();
            itemToSummon.setCount(actualRate);
            ItemEntity itemEntity = new ItemEntity(world,getBlockPosOfBlockBelow(world,posOfPedestal,-range).getX() + 0.5,getBlockPosOfBlockBelow(world,posOfPedestal,-range).getY(),getBlockPosOfBlockBelow(world,posOfPedestal,-range).getZ() + 0.5,itemToSummon);
            itemEntity.setMotion(0,0,0);
            world.addEntity(itemEntity);
            this.removeFromPedestal(world,posOfPedestal,itemToSummon.getCount());
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getDescriptionId() + ".tooltip_name");
        name.withStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.NIL_UUID);

        TranslationTextComponent range = new TranslationTextComponent(getDescriptionId() + ".chat_range");
        range.append("" +  getRangeSmall(stack) + "");
        range.withStyle(TextFormatting.WHITE);
        player.sendMessage(range,Util.NIL_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".chat_rate");
        rate.append("" +  getItemTransferRate(stack) + "");
        rate.withStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.NIL_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".chat_speed");
        speed.append(getOperationSpeedString(stack));
        speed.withStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.NIL_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent rate = new TranslationTextComponent(getDescriptionId() + ".tooltip_rate");
        rate.append("" + getItemTransferRate(stack) + "");
        TranslationTextComponent range = new TranslationTextComponent(getDescriptionId() + ".tooltip_range");
        range.append("" + getRangeSmall(stack) + "");
        TranslationTextComponent speed = new TranslationTextComponent(getDescriptionId() + ".tooltip_speed");
        speed.append(getOperationSpeedString(stack));

        rate.withStyle(TextFormatting.GRAY);
        range.withStyle(TextFormatting.WHITE);
        speed.withStyle(TextFormatting.RED);

        tooltip.add(rate);
        tooltip.add(range);
        tooltip.add(speed);
    }

    public static final Item DROPPER = new ItemUpgradeDropper(new Item.Properties().stacksTo(64).tab(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/dropper"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(DROPPER);
    }


}
