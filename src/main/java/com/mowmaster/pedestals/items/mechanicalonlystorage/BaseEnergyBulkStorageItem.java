package com.mowmaster.pedestals.items.mechanicalonlystorage;

import com.mowmaster.mowlib.Items.BaseEnergyDropItem;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.pedestals.configs.PedestalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;

public class BaseEnergyBulkStorageItem extends BaseEnergyDropItem implements IBulkItem {

    public BaseEnergyBulkStorageItem(Properties p_41383_) {
        super(p_41383_);
    }

    private int getDischargeCounter(ItemStack stack){
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        if(tag.contains(MODID+"_dischargeCounter")){
            return readIntegerFromNBT(MODID,tag,"_dischargeCounter");
        }
        else {
            setDischargeCounter(stack);
            return 0;
        }
    }
    private void increaseDischargeCounter(ItemStack stack){
        int current = 0;
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        if(tag.contains(MODID+"_dischargeCounter"))current = readIntegerFromNBT(MODID,tag,"_dischargeCounter");
        writeIntegerToNBT(MODID,tag,(current+1),"_dischargeCounter");
    }
    private void setDischargeCounter(ItemStack stack){
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        writeIntegerToNBT(MODID,tag,0,"_dischargeCounter");
    }

    ///
    // Remove later once mowlib updates
    ///
    public static CompoundTag writeIntegerToNBT(String ModID, @Nullable CompoundTag inputNBT, int input, String intName) {
        CompoundTag compound = inputNBT != null ? inputNBT : new CompoundTag();
        compound.putInt(ModID + intName, input);
        return compound;
    }

    public static int readIntegerFromNBT(String ModID, CompoundTag inputNBT, String intName)
    {
        if(inputNBT.contains(ModID + intName))
        {
            return inputNBT.getInt(ModID + intName);
        }

        return 0;
    }
    ///
    // Remove later once mowlib updates
    ///

    public static int getItemColor()
    {
        return 16723484;
    }

    public int getMaxAllowedStorage()
    {
        return PedestalConfig.COMMON.pedestal_baseEnergyStorage.get() + (PedestalConfig.COMMON.augment_t4StorageEnergy.get() * PedestalConfig.COMMON.augment_t4StorageInsertSize.get());
    }

    public void setEnergy(ItemStack stack, int amount)
    {
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        CompoundTag returnerTag = MowLibCompoundTagUtils.writeIntegerToNBT(MODID, tag, amount,"_bulkenergystorage");
        stack.setTag(returnerTag);
    }

    public int getEnergy(ItemStack stack)
    {
        int currentStored = 0;
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        if(tag.contains(MODID+"_bulkenergystorage"))currentStored = readIntegerFromNBT(MODID, tag,"_bulkenergystorage");

        return currentStored;
    }

    public void addEnergy(ItemStack stack, int added)
    {
        int currentStored = 0;
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        if(tag.contains(MODID+"_bulkenergystorage"))currentStored = readIntegerFromNBT(MODID, tag,"_bulkenergystorage");
        int difference = currentStored + added;
        CompoundTag returnerTag = MowLibCompoundTagUtils.writeIntegerToNBT(MODID, tag, (difference>getMaxAllowedStorage())?(getMaxAllowedStorage()):(difference),"_bulkenergystorage");
        stack.setTag(returnerTag);
    }

    public void removeEnergy(ItemStack stack, int removed)
    {
        int currentStored = 0;
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        if(tag.contains(MODID+"_bulkenergystorage"))currentStored = readIntegerFromNBT(MODID, tag,"_bulkenergystorage");
        int difference = currentStored - removed;
        CompoundTag returnerTag = MowLibCompoundTagUtils.writeIntegerToNBT(MODID, tag, (difference>0)?(difference):(0),"_bulkenergystorage");
        stack.setTag(returnerTag);
    }

    public int getItemVariant(ItemStack stack) {
        if (getEnergy(stack) > 0) {
            double renderDevider = 100.0D * (double)(this.getEnergy(stack) / this.getMaxAllowedStorage());
            if (renderDevider <= 25.0D) {
                return 0;
            }

            if (renderDevider <= 50.0D) {
                return 1;
            }

            if (renderDevider <= 75.0D) {
                return 2;
            }

            if (renderDevider >= 100.0D) {
                return 3;
            }
        }

        return 0;
    }

    private int counter = 0;
    @Override
    public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_) {
        super.inventoryTick(p_41404_, p_41405_, p_41406_, p_41407_, p_41408_);

        if(!p_41405_.isClientSide())
        {
            boolean survival = true;
            if(p_41406_ instanceof Player player)
            {
                if(player.isCreative()) survival = false;
            }

            if(survival && PedestalConfig.COMMON.bulkstorage_energyDischarge_toggle.get())
            {
                int stored = getEnergy(p_41404_);
                if(stored>5000)
                {
                    counter++;
                    if(counter>=20)
                    {
                        increaseDischargeCounter(p_41404_);
                        counter=0;
                    }
                }

                if(getDischargeCounter(p_41404_) >= PedestalConfig.COMMON.bulkstorage_energyDischarge.get())
                {
                    removeEnergy(p_41405_,p_41406_.getOnPos());
                    p_41404_.shrink(1);
                }
            }
        }
    }

    public void removeEnergy(Level worldIn, BlockPos pos) {
        Random rand = new Random();
        int count = getItemVariant()+1;
        while(count > 0) {
            LightningBolt lightningbolt = (LightningBolt)EntityType.LIGHTNING_BOLT.create(worldIn);
            lightningbolt.moveTo(Vec3.atBottomCenterOf(pos.offset(rand.nextInt(10), -1, rand.nextInt(10))));
            lightningbolt.setCause((ServerPlayer)null);
            worldIn.addFreshEntity(lightningbolt);
            worldIn.playSound((Player)null, pos, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
            count--;
        }
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        int stored = getEnergy(p_41421_);
        if(stored>0)
        {
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(MODID + ".bulkstorage_energy"), ChatFormatting.GOLD);
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.literal("" + stored + ""), ChatFormatting.WHITE);
        }

        int charge = getDischargeCounter(p_41421_);
        if(charge>0)
        {
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(MODID + ".bulkstorage_energy_discharge"), ChatFormatting.RED);
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.literal("" + charge + "/" + PedestalConfig.COMMON.bulkstorage_energyDischarge.get() +""), ChatFormatting.WHITE);
        }
    }
}
