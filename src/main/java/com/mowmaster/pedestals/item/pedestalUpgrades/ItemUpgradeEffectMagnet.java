package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEffectMagnet extends ItemUpgradeBase
{
    public ItemUpgradeEffectMagnet(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

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

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos))
            {
                if (tick%speed == 0) {
                    upgradeAction(world, itemInPedestal, coinInPedestal, pedestalPos);
                }
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int width = getAreaWidth(coinInPedestal);
        int height = (2*width)+1;
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);

        List<ItemEntity> itemList = world.getEntitiesWithinAABB(ItemEntity.class,getBox);
        for(ItemEntity getItemFromList : itemList)
        {
            if (itemInPedestal.equals(ItemStack.EMPTY))
            {
                world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);
                TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                if(pedestalInv instanceof TilePedestal) {
                    if(getItemFromList.getItem().getCount() <=64)
                    {
                        getItemFromList.remove();
                        ((TilePedestal) pedestalInv).addItem(getItemFromList.getItem());
                    }
                    else
                    {
                        int count = getItemFromList.getItem().getCount();
                        getItemFromList.getItem().setCount(count-64);
                        ItemStack getItemstacked = getItemFromList.getItem().copy();
                        getItemstacked.setCount(64);
                        ((TilePedestal) pedestalInv).addItem(getItemstacked);
                    }
                }
                break;
            }
        }
    }

    @Override
    public void actionOnCollideWithBlock(World world, TilePedestal tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
            ItemStack itemFromPedestal = getStackInPedestal(world,posPedestal);
            if(itemFromPedestal.isEmpty())
            {
                TileEntity pedestalInv = world.getTileEntity(posPedestal);
                if(pedestalInv instanceof TilePedestal) {
                    entityIn.remove();
                    ((TilePedestal) pedestalInv).addItem(getItemStack);
                }
            }
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,player.getUniqueID());

        int s3 = getAreaWidth(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,player.getUniqueID());

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,player.getUniqueID());
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
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        area.mergeStyle(TextFormatting.WHITE);
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(area);
        tooltip.add(speed);
    }

    public static final Item MAGNET = new ItemUpgradeEffectMagnet(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/magnet"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(MAGNET);
    }


}
