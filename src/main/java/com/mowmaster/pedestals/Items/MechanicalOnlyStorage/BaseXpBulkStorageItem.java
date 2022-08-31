package com.mowmaster.pedestals.Items.MechanicalOnlyStorage;

import com.mowmaster.mowlib.Items.BaseEnergyDropItem;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibXpUtils;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class BaseXpBulkStorageItem extends BaseEnergyDropItem {

    public BaseXpBulkStorageItem(Properties p_41383_) {
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
        return 5373775;
    }

    public int getMaxAllowedStorage()
    {
        return MowLibXpUtils.getExpCountByLevel(PedestalConfig.COMMON.pedestal_baseXpStorage.get()) + (MowLibXpUtils.getExpCountByLevel(PedestalConfig.COMMON.augment_t4StorageXp.get()) * PedestalConfig.COMMON.augment_t4StorageInsertSize.get());
    }

    public void setXp(ItemStack stack, int amount)
    {
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        CompoundTag returnerTag = writeIntegerToNBT(MODID, tag, amount,"_bulkxpstorage");
        stack.setTag(returnerTag);
    }

    public int getXp(ItemStack stack)
    {
        int currentStored = 0;
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        if(tag.contains(MODID+"_bulkxpstorage"))currentStored = readIntegerFromNBT(MODID, tag,"_bulkxpstorage");

        return currentStored;
    }

    public void addXp(ItemStack stack, int added)
    {
        int currentStored = 0;
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        if(tag.contains(MODID+"_bulkxpstorage"))currentStored = readIntegerFromNBT(MODID, tag,"_bulkxpstorage");
        int difference = currentStored + added;
        CompoundTag returnerTag = writeIntegerToNBT(MODID, tag, (difference>getMaxAllowedStorage())?(getMaxAllowedStorage()):(difference),"_bulkxpstorage");
        stack.setTag(returnerTag);
    }

    public void removeXp(ItemStack stack, int removed)
    {
        int currentStored = 0;
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag()) tag = stack.getTag();
        if(tag.contains(MODID+"_bulkxpstorage"))currentStored = readIntegerFromNBT(MODID, tag,"_bulkxpstorage");
        int difference = currentStored - removed;
        CompoundTag returnerTag = writeIntegerToNBT(MODID, tag, (difference>0)?(difference):(0),"_bulkxpstorage");
        stack.setTag(returnerTag);
    }

    public int getItemVariant(ItemStack stack) {
        if (getXp(stack) > 0) {
            double renderDevider = 100.0D * (double)(this.getXp(stack) / this.getMaxAllowedStorage());
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

            if(survival && PedestalConfig.COMMON.bulkstorage_xpDischarge_toggle.get())
            {
                int stored = getXp(p_41404_);
                if(stored>0)
                {
                    counter++;
                    if(counter>=20)
                    {
                        increaseDischargeCounter(p_41404_);
                        counter=0;
                    }
                }

                if(getDischargeCounter(p_41404_) >= PedestalConfig.COMMON.bulkstorage_xpDischarge.get())
                {
                    removeXp(p_41405_,p_41406_.getOnPos(), p_41404_);
                    p_41404_.shrink(1);
                }
            }
        }
    }

    public void removeXp(Level worldIn, BlockPos pos, ItemStack stack) {
        int xpStored = getXp(stack);
        if (xpStored > 0) {
            ExperienceOrb xpEntity = new ExperienceOrb(worldIn, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), xpStored);
            xpEntity.lerpMotion(0.0D, 0.0D, 0.0D);
            worldIn.addFreshEntity(xpEntity);
        }
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        int stored = this.getXp(p_41421_);
        if(stored>0)
        {
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(MODID + ".bulkstorage_xp"), ChatFormatting.GOLD);
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.literal("" + stored + ""), ChatFormatting.WHITE);
        }

        int charge = getDischargeCounter(p_41421_);
        if(charge>0)
        {
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(MODID + ".bulkstorage_xp_discharge"), ChatFormatting.RED);
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.literal("" + charge + "/" + PedestalConfig.COMMON.bulkstorage_xpDischarge.get() +""), ChatFormatting.WHITE);
        }
    }
}
