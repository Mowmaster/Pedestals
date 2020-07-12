package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.pedestals;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;

public class ItemUpgradeBaseMachine extends ItemUpgradeBase {

    public final int burnTimeCostPerItemSmelted = 200;

    public ItemUpgradeBaseMachine(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public int getItemTransferRate(ItemStack stack)
    {
        int itemsPerSmelt = 1;
        switch (getCapacityModifier(stack))
        {
            case 0:
                itemsPerSmelt = 1;
                break;
            case 1:
                itemsPerSmelt=2;
                break;
            case 2:
                itemsPerSmelt = 4;
                break;
            case 3:
                itemsPerSmelt = 8;
                break;
            case 4:
                itemsPerSmelt = 12;
                break;
            case 5:
                itemsPerSmelt=16;
                break;
            default: itemsPerSmelt=1;
        }

        return  itemsPerSmelt;
    }

    public int getSmeltingSpeed(ItemStack stack)
    {
        int smeltingSpeed = 200;
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                smeltingSpeed = 200;//normal speed
                break;
            case 1:
                smeltingSpeed=100;//2x faster
                break;
            case 2:
                smeltingSpeed = 50;//4x faster
                break;
            case 3:
                smeltingSpeed = 33;//6x faster
                break;
            case 4:
                smeltingSpeed = 20;//10x faster
                break;
            case 5:
                smeltingSpeed=10;//20x faster
                break;
            default: smeltingSpeed=200;
        }

        return  smeltingSpeed;
    }

    public int removeFuel(TilePedestal pedestal, int amountToRemove, boolean simulate)
    {
        int fuelLeft = pedestal.getStoredValueForUpgrades();
        int amountToSet = fuelLeft - amountToRemove;
        if(amountToRemove >= fuelLeft) amountToSet = -1;
        if(!simulate)
        {
            if(amountToSet == -1) amountToSet = 0;
            pedestal.setStoredValueForUpgrades(amountToSet);
        }

        return amountToSet;
    }

    public static int getItemFuelBurnTime(ItemStack fuel)
    {
        if (fuel.isEmpty()) return 0;
        else
        {
            int burnTime = ForgeHooks.getBurnTime(fuel);
            return burnTime;
        }
    }

    @Override
    public void actionOnCollideWithBlock(World world, TilePedestal tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack getItemStack = ((ItemEntity) entityIn).getItem();
            if(getItemFuelBurnTime(getItemStack)>0)
            {
                int CurrentBurnTime = tilePedestal.getStoredValueForUpgrades();
                int getBurnTimeForStack = getItemFuelBurnTime(getItemStack) * getItemStack.getCount();
                tilePedestal.setStoredValueForUpgrades(CurrentBurnTime + getBurnTimeForStack);
                if(getItemStack.getItem().equals(Items.LAVA_BUCKET))
                {
                    ItemStack getReturned = new ItemStack(Items.BUCKET,getItemStack.getCount());
                    ItemEntity items1 = new ItemEntity(world, posPedestal.getX() + 0.5, posPedestal.getY() + 1.0, posPedestal.getZ() + 0.5, getReturned);
                    entityIn.remove();
                    world.addEntity(items1);
                }

                entityIn.remove();
            }
        }
    }

    public String getSmeltingSpeedString(ItemStack stack)
    {
        String s3 = "Normal Speed";
        switch (getSmeltingSpeed(stack))
        {
            case 200:
                s3 = "Normal Speed";
                break;
            case 100:
                s3="2x Faster";
                break;
            case 50:
                s3 = "4x Faster";
                break;
            case 33:
                s3 = "6x Faster";
                break;
            case 20:
                s3 = "10x Faster";
                break;
            case 10:
                s3="20x Faster";
                break;
            default: s3= "Normal Speed";
        }
        return s3;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(TilePedestal pedestal, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!world.isBlockPowered(pos))
        {
            int fuelValue = pedestal.getStoredValueForUpgrades();

            double d0 = (double)getPosOfBlockBelow(world,pos,-1 ).getX() + 0.55D - (double)(rand.nextFloat() * 0.1F);
            double d1 = (double)getPosOfBlockBelow(world,pos,-1 ).getY() + 0.0D - (double)(rand.nextFloat() * 0.1F);
            double d2 = (double)getPosOfBlockBelow(world,pos,-1 ).getZ() + 0.55D - (double)(rand.nextFloat() * 0.1F);
            double d3 = (double)(0.4F - (rand.nextFloat() + rand.nextFloat()) * 0.4F);

            if(fuelValue > 0)
            {
                world.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + 0.5D, (double)pos.getY() + 1.0D, (double)pos.getZ() + 0.5D,0, 0, 0);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int s2 = getItemTransferRate(stack);
        String trr = getSmeltingSpeedString(stack);
        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.func_240702_b_(""+s2+"");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.func_240702_b_(trr);

        rate.func_240699_a_(TextFormatting.GRAY);
        speed.func_240699_a_(TextFormatting.RED);

        tooltip.add(rate);
        tooltip.add(speed);
    }

}
