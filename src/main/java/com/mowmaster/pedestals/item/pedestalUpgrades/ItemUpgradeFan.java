package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
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
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeFan extends ItemUpgradeBase
{
    public ItemUpgradeFan(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptRange() {
        return true;
    }

    @Override
    public Boolean canAcceptArea() {
        return true;
    }

    public int getAreaWidth(ItemStack stack)
    {
        return  getAreaModifier(stack);
    }

    public int getHeight(ItemStack stack)
    {
        int transferRate = 4;
        switch (getRangeModifier(stack))
        {
            case 0:
                transferRate = 4;
                break;
            case 1:
                transferRate=8;
                break;
            case 2:
                transferRate = 12;
                break;
            case 3:
                transferRate = 16;
                break;
            case 4:
                transferRate = 24;
                break;
            case 5:
                transferRate=32;
                break;
            default: transferRate=4;
        }

        return  transferRate;
    }

    protected void useFanOnEntities(World world, BlockPos posOfPedestal,TilePedestal pedestal, double speed,AxisAlignedBB getBox) {
        List<LivingEntity> entityList = world.getEntitiesWithinAABB(LivingEntity.class, getBox);

        if(entityList.size()==0)pedestal.setStoredValueForUpgrades(0);

        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
        for (LivingEntity entity : entityList) {
            LivingEntity getEntity = getTargetEntity(world,posOfPedestal,entity);
            if(getEntity != null)
            {
                if(getEntity instanceof PlayerEntity)
                {
                    if(!((PlayerEntity) getEntity).abilities.isFlying && !((PlayerEntity) getEntity).isCrouching())
                    {
                        addMotion(world,posOfPedestal,speed,enumfacing,getEntity);
                        pedestal.setStoredValueForUpgrades(1);
                    }
                }
                else
                {
                    addMotion(world,posOfPedestal,speed,enumfacing,getEntity);
                    pedestal.setStoredValueForUpgrades(1);
                }
                if (enumfacing == Direction.UP) {
                    getEntity.fallDistance = 0;
                }
            }
        }

    }

    protected void addMotion(World world, BlockPos posOfPedestal, double speed, Direction enumfacing, LivingEntity entity) {

        switch (enumfacing) {
            case DOWN:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y - speed, entity.getMotion().z);
                break;
            case UP:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y + speed, entity.getMotion().z);
                break;
            case NORTH:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y, entity.getMotion().z - speed);
                break;
            case SOUTH:
                entity.setMotion(entity.getMotion().x, entity.getMotion().y, entity.getMotion().z + speed);
                break;
            case WEST:
                entity.setMotion(entity.getMotion().x - speed, entity.getMotion().y, entity.getMotion().z);
                break;
            case EAST:
                entity.setMotion(entity.getMotion().x + speed, entity.getMotion().y, entity.getMotion().z);
                break;
        }
    }

    public double getFanSpeed(ItemStack stack)
    {
        double intOperationalSpeed = 0.25;
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                intOperationalSpeed = 0.1;//normal speed
                break;
            case 1:
                intOperationalSpeed=0.2;//2x faster
                break;
            case 2:
                intOperationalSpeed = 0.4;//4x faster
                break;
            case 3:
                intOperationalSpeed = 0.6;//6x faster
                break;
            case 4:
                intOperationalSpeed = 1.0;//10x faster
                break;
            case 5:
                intOperationalSpeed=2.0;//20x faster
                break;
            default: intOperationalSpeed=0.1;
        }

        return  intOperationalSpeed;
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {

        if(!world.isBlockPowered(pedestalPos))
        {
            TileEntity entity = world.getTileEntity(pedestalPos);
            if(entity instanceof TilePedestal)
            {
                TilePedestal ped = ((TilePedestal)entity);
                upgradeAction(world, pedestalPos, ped);
                if(ped.getStoredValueForUpgrades() > 0)
                {
                    int speedSound = getOperationSpeed(coinInPedestal);
                    if (tick%speedSound == 0) {
                        world.playSound((PlayerEntity) null, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), SoundEvents.ENTITY_PHANTOM_FLAP, SoundCategory.BLOCKS, 0.25F, 1.0F);
                    }
                }
            }
        }
    }


    public void upgradeAction(World world, BlockPos posOfPedestal, TilePedestal pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        int width = getAreaWidth(coin);
        int height = getHeight(coin);
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);
        double speed = getFanSpeed(coin);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);

        if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.NETHERITE_BLOCK))
        {
            speed *= 2;
        }

        useFanOnEntities(world,posOfPedestal,pedestal,speed,getBox);
    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        int s3 = getAreaWidth(stack);
        int s4 = getHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.appendString(tr);
        area.appendString(areax.getString());
        area.appendString("" + s4 + "");
        area.appendString(areax.getString());
        area.appendString(tr);
        area.mergeStyle(TextFormatting.WHITE);
        player.sendMessage(area,Util.DUMMY_UUID);

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
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int s3 = getAreaWidth(stack);
        int s4 = getHeight(stack);

        String tr = "" + (s3+s3+1) + "";
        String trr = "" + (s4) + "";

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
        tooltip.add(area);

        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item FAN = new ItemUpgradeFan(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fan"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FAN);
    }

}
