package com.mowmaster.pedestals.item.deprecated;

import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFilteredMagnetItemStack extends ItemUpgradeBase
{
    public ItemUpgradeFilteredMagnetItemStack(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    @Override
    public Boolean canAcceptRange() {return true;}

    @Override
    public Boolean canAcceptAdvanced() {
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
    public int getRangeModifier(ItemStack stack)
    {
        int range = 0;
        if(hasEnchant(stack))
        {
            range = EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistry.RANGE,stack);
        }
        return range;
    }

    public int getRangeHeight(ItemStack stack)
    {
        return getHeight(stack);
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
        return new int[]{getRangeHeight(coin),0};
    }

    @Override
    public int getWorkAreaZ(World world, BlockPos pos, ItemStack coin)
    {
        return getAreaWidth(coin);
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int speed = getOperationSpeed(coinInPedestal);
            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal);
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos posOfPedestal = pedestal.getPos();
        int width = getAreaWidth(coinInPedestal);
        int height = getRangeHeight(coinInPedestal);
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);

        List<ItemEntity> itemList = world.getEntitiesWithinAABB(ItemEntity.class,getBox);

        ItemStack coin = pedestal.getCoinOnPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(coin);
        if(!(stackCurrent.size()>0))
        {
            stackCurrent = buildFilterQueue(pedestal);
            writeFilterQueueToNBT(coin,stackCurrent);
        }

        upgradeActionMagnet(pedestal,world,itemList, itemInPedestal, posOfPedestal);
    }

    @Override
    public ItemStack getFilterReturnStack(List<ItemStack> stack, ItemStack incoming)
    {
        int range = stack.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stack)::get)//Function being applied to each interval
                .filter(itemStack -> doItemsMatch(itemStack,incoming))
                .findFirst().orElse(ItemStack.EMPTY);

        return itemFromInv;
    }

    @Override
    public void actionOnCollideWithBlock(PedestalTileEntity pedestal, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
            ItemStack itemFromPedestal = pedestal.getItemInPedestal();
            if(itemFromPedestal.isEmpty())
            {
                ItemStack coin = pedestal.getCoinOnPedestal();
                List<ItemStack> stackCurrent = readFilterQueueFromNBT(coin);
                if(!(stackCurrent.size()>0))
                {
                    stackCurrent = buildFilterQueue(pedestal);
                    writeFilterQueueToNBT(coin,stackCurrent);
                }

                ItemStack itemFromInv = getFilterReturnStack(stackCurrent, getItemStack);

                if(!itemFromInv.isEmpty())
                {
                    entityIn.remove();
                    pedestal.addItem(getItemStack);
                }
            }
        }
    }

    @Override
    public void onPedestalNeighborChanged(PedestalTileEntity pedestal) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<ItemStack> stackIn = buildFilterQueue(pedestal);
        if(filterQueueSize(coin)>0)
        {
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(coin);
            if(!doesFilterAndQueueMatch(stackIn,stackCurrent))
            {
                writeFilterQueueToNBT(coin,stackIn);
            }
        }
        else
        {
            writeFilterQueueToNBT(coin,stackIn);
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
        String trr = "" + getRangeHeight(stack) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(trr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area, Util.DUMMY_UUID);

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

    public static final Item FMAGNETITEMSTACK = new ItemUpgradeFilteredMagnetItemStack(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fmagnetitemstack"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FMAGNETITEMSTACK);
    }


}
