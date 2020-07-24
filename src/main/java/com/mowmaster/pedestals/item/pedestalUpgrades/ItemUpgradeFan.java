package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

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

    public int getRangeHeight(ItemStack stack)
    {
        return getHeight(stack);
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

        return  transferRate-1;
    }

    protected void addMotion(World world, BlockPos posPedestal,double speed, Entity entity) {
        BlockState state = world.getBlockState(posPedestal);
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
                intOperationalSpeed = 0.25;//normal speed
                break;
            case 1:
                intOperationalSpeed=0.5;//2x faster
                break;
            case 2:
                intOperationalSpeed = 1.0;//4x faster
                break;
            case 3:
                intOperationalSpeed = 1.5;//6x faster
                break;
            case 4:
                intOperationalSpeed = 2.0;//10x faster
                break;
            case 5:
                intOperationalSpeed=2.5;//20x faster
                break;
            default: intOperationalSpeed=0.25;
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

        List<LivingEntity> itemList = world.getEntitiesWithinAABB(LivingEntity.class,getBox);
        for(LivingEntity getEntityFromList : itemList)
        {

            if(getBaseBlockBelow(world,posOfPedestal).equals(Blocks.field_235397_ng_))
            {
                speed *= 2;
            }

            if(getTargetEntity(world,posOfPedestal,getEntityFromList) != null)
            {
                //Do Stuff
                addMotion(world,posOfPedestal,speed,getTargetEntity(world,posOfPedestal,getEntityFromList));
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int s3 = getRangeWidth(stack);
        int s4 = getRangeHeight(stack);

        String tr = "" + (s3+s3+1) + "";
        String trr = "" + (s4+1) + "";

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
