package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeBaseEnergyMachine extends ItemUpgradeBaseEnergy {

    public final int rfCostPerItemSmelted = 2500;

    public ItemUpgradeBaseEnergyMachine(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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

    public int removeEnergyFuel(TilePedestal pedestal, int amountToRemove, boolean simulate)
    {
        int amountToSet = 0;
        ItemStack coin = pedestal.getCoinOnPedestal();
        int fuelLeft = getEnergyStored(coin);
        amountToSet = fuelLeft - amountToRemove;
        if(amountToRemove > fuelLeft) amountToSet = -1;
        if(!simulate)
        {
            if(amountToSet == -1) amountToSet = 0;
            setEnergyStored(coin,amountToSet);
            pedestal.update();
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
                    world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.25F, 1.0F);
                    entityIn.remove();
                    world.addEntity(items1);
                }

                world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.25F, 1.0F);
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
    public void onRandomDisplayTick(TilePedestal pedestal,int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!world.isBlockPowered(pos))
        {
            int fuelValue = getEnergyStored(pedestal.getCoinOnPedestal());

            //More than 1 smelt worth
            if(fuelValue >= rfCostPerItemSmelted)
            {
                spawnParticleAroundPedestalBase(world,tick,pos,1.0f,0.0f,0.0f,1.0f);
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

        //Display Fuel Left
        int fuelValue = getEnergyStored(pedestal.getCoinOnPedestal());
        TranslationTextComponent fuel = new TranslationTextComponent(getTranslationKey() + ".chat_fuel");
        fuel.appendString("" + fuelValue/2500 + "");
        fuel.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(fuel,player.getUniqueID());

        TranslationTextComponent energyRate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        energyRate.appendString(""+ getItemTransferRate(stack) +"");
        energyRate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(energyRate,player.getUniqueID());

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

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".tooltip_rfstored");
        xpstored.appendString(""+ getEnergyStored(stack) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(xpstored);

        TranslationTextComponent xpcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_rfcapacity");
        xpcapacity.appendString(""+ getEnergyBuffer(stack) +"");
        xpcapacity.mergeStyle(TextFormatting.AQUA);
        tooltip.add(xpcapacity);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + getItemTransferRate(stack) + "");
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

}
