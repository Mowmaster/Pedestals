package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;

public class ItemUpgradeBaseEnergyMachine extends ItemUpgradeBaseEnergy {

    public final int rfCostPerItemSmelted = 2500;

    public ItemUpgradeBaseEnergyMachine(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public int getCapacityModifier(ItemStack stack)
    {
        int capacity = 0;
        if(hasEnchant(stack))
        {
            capacity = (getCapacityModifierOverEnchanted(stack) > 10)?(10):(getCapacityModifierOverEnchanted(stack));
        }
        return capacity;
    }

    @Override
    public int getEnergyBuffer(ItemStack stack) {
        int capacityOver = getCapacityModifierOverEnchanted(stack);

        int energyBuffer = (capacityOver*20000);;
        switch (capacityOver)
        {
            case 0:
                energyBuffer = 10000;
                break;
            case 1:
                energyBuffer = 20000;
                break;
            case 2:
                energyBuffer = 40000;
                break;
            case 3:
                energyBuffer = 60000;
                break;
            case 4:
                energyBuffer = 80000;
                break;
            case 5:
                energyBuffer = 100000;
                break;
            default: energyBuffer = (energyBuffer> maxStored)?(maxStored):(energyBuffer);
        }

        return  energyBuffer;
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
                itemsPerSmelt=4;
                break;
            case 2:
                itemsPerSmelt = 8;
                break;
            case 3:
                itemsPerSmelt = 12;
                break;
            case 4:
                itemsPerSmelt = 16;
                break;
            case 5:
                itemsPerSmelt=24;
                break;
            case 6:
                itemsPerSmelt=32;
                break;
            case 7:
                itemsPerSmelt=40;
                break;
            case 8:
                itemsPerSmelt=48;
                break;
            case 9:
                itemsPerSmelt=56;
                break;
            case 10:
                itemsPerSmelt=64;
                break;
            default: itemsPerSmelt=1;
        }

        return  itemsPerSmelt;
    }

    @Override
    public int intOperationalSpeedModifier(ItemStack stack)
    {
        int rate = 0;
        if(hasEnchant(stack))
        {
            rate = (intOperationalSpeedModifierOverride(stack) > 10)?(10):(intOperationalSpeedModifierOverride(stack));
        }
        return rate;
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
                smeltingSpeed = 40;//5x faster
                break;
            case 4:
                smeltingSpeed = 33;//6x faster
                break;
            case 5:
                smeltingSpeed=20;//10x faster
                break;
            case 6:
                smeltingSpeed=10;//20x faster
                break;
            case 7:
                smeltingSpeed=5;//40x faster
                break;
            case 8:
                smeltingSpeed=3;//60x faster
                break;
            case 9:
                smeltingSpeed=2;//100x faster
                break;
            case 10:
                smeltingSpeed=1;//200x faster
                break;
            default: smeltingSpeed=200;
        }

        return  smeltingSpeed;
    }

    @Override
    public String getOperationSpeedString(ItemStack stack)
    {
        TranslationTextComponent normal = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_0");
        TranslationTextComponent twox = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_1");
        TranslationTextComponent fourx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_2");
        TranslationTextComponent fivex = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_3");
        TranslationTextComponent sixx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_4");
        TranslationTextComponent tenx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_5");
        TranslationTextComponent twentyx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_6");
        TranslationTextComponent fourtyx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_7");
        TranslationTextComponent sixtyx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_8");
        TranslationTextComponent onehunx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_9");
        TranslationTextComponent twohunx = new TranslationTextComponent(Reference.MODID + ".upgrade_machine_tooltips" + ".speed_10");
        String str = normal.getString();
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                str = normal.getString();//normal speed
                break;
            case 1:
                str = twox.getString();//2x faster
                break;
            case 2:
                str = fourx.getString();//4x faster
                break;
            case 3:
                str = fivex.getString();//5x faster
                break;
            case 4:
                str = sixx.getString();//6x faster
                break;
            case 5:
                str = tenx.getString();//10x faster
                break;
            case 6:
                str = twentyx.getString();//20x faster
                break;
            case 7:
                str = fourtyx.getString();//40x faster
                break;
            case 8:
                str = sixtyx.getString();//60x faster
                break;
            case 9:
                str = onehunx.getString();//100x faster
                break;
            case 10:
                str = twohunx.getString();//200x faster
                break;
            default: str = normal.getString();;
        }

        return  str;
    }

    public int removeEnergyFuel(PedestalTileEntity pedestal, int amountToRemove, boolean simulate)
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

    @Override
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        //do nothing
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!pedestal.hasParticleDiffuser())
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
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        //Display Fuel Left
        int fuelValue = getEnergyStored(pedestal.getCoinOnPedestal());
        TranslationTextComponent fuel = new TranslationTextComponent(getTranslationKey() + ".chat_fuel");
        fuel.appendString("" + fuelValue/2500 + "");
        fuel.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(fuel,Util.DUMMY_UUID);

        TranslationTextComponent energyRate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        energyRate.appendString(""+ getItemTransferRate(stack) +"");
        energyRate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(energyRate, Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        //super.addInformation(stack, worldIn, tooltip, flagIn);
        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        if(getAdvancedModifier(stack)<=0 && (intOperationalSpeedOver(stack) >5 || getCapacityModifierOver(stack) >5 || getAreaModifierUnRestricted(stack) >5 || getRangeModifier(stack) >5))
        {
            TranslationTextComponent warning = new TranslationTextComponent(Reference.MODID + ".advanced_warning");
            warning.mergeStyle(TextFormatting.RED);
            tooltip.add(warning);
        }

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
