package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import java.util.Map;

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
    public Boolean canAcceptCapacity() {
        return true;
    }

    public int getRangeWidth(ItemStack stack)
    {
        int rangeWidth = 0;
        int rW = getCapacityModifier(stack);
        rangeWidth = (rW);
        return  rangeWidth;
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

    protected void useFanOnEntities(World world, BlockPos posOfPedestal, double speed,AxisAlignedBB getBox) {
        List<LivingEntity> entityList = world.getEntitiesWithinAABB(LivingEntity.class, getBox);

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
                        addMotion(world,posOfPedestal,speed,getEntity);
                    }
                }
                else
                {
                    addMotion(world,posOfPedestal,speed,getEntity);
                }
                if (enumfacing == Direction.UP) {
                    getEntity.fallDistance = 0;
                }
            }
        }
    }

    protected void addMotion(World world, BlockPos posOfPedestal, double speed, LivingEntity entity) {
        if (entity instanceof PlayerEntity && entity.func_233570_aj_()) {
            return;
        }

        BlockState state = world.getBlockState(posOfPedestal);
        Direction enumfacing = state.get(FACING);
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
            upgradeAction(world, itemInPedestal, coinInPedestal, pedestalPos);
        }
    }


    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int width = getRangeWidth(coinInPedestal);
        int height = getHeight(coinInPedestal);
        BlockPos negBlockPos = getNegRangePosEntity(world,posOfPedestal,width,height);
        BlockPos posBlockPos = getPosRangePosEntity(world,posOfPedestal,width,height);
        double speed = getFanSpeed(coinInPedestal);

        AxisAlignedBB getBox = new AxisAlignedBB(negBlockPos,posBlockPos);

        if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.field_235397_ng_))
        {
            speed *= 2;
        }

        useFanOnEntities(world,posOfPedestal,speed,getBox);
    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.func_240699_a_(TextFormatting.GOLD);
        player.sendMessage(name,player.getUniqueID());

        int s3 = getRangeWidth(stack);
        int s4 = getHeight(stack);
        String tr = "" + (s3+s3+1) + "";
        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".chat_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".chat_areax");
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_("" + s4 + "");
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        area.func_240699_a_(TextFormatting.WHITE);
        player.sendMessage(area,player.getUniqueID());

        TranslationTextComponent entityType = new TranslationTextComponent(getTranslationKey() + ".chat_entity");
        entityType.func_240702_b_(getTargetEntity(pedestal.getWorld(),pedestal.getPos()));
        entityType.func_240699_a_(TextFormatting.YELLOW);
        player.sendMessage(entityType,player.getUniqueID());

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));
        speed.func_240699_a_(TextFormatting.RED);
        player.sendMessage(speed,player.getUniqueID());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int s3 = getRangeWidth(stack);
        int s4 = getHeight(stack);

        String tr = "" + (s3+s3+1) + "";
        String trr = "" + (s4) + "";

        TranslationTextComponent area = new TranslationTextComponent(getTranslationKey() + ".tooltip_area");
        TranslationTextComponent areax = new TranslationTextComponent(getTranslationKey() + ".tooltip_areax");
        area.func_240702_b_(tr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(trr);
        area.func_240702_b_(areax.getString());
        area.func_240702_b_(tr);
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.func_240702_b_(getOperationSpeedString(stack));

        area.func_240699_a_(TextFormatting.WHITE);
        tooltip.add(area);

        speed.func_240699_a_(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item FAN = new ItemUpgradeFan(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fan"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FAN);
    }

}
