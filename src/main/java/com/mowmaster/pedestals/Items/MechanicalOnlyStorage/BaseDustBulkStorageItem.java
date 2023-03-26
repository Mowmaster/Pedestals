package com.mowmaster.pedestals.Items.MechanicalOnlyStorage;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.BaseDustDropItem;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class BaseDustBulkStorageItem extends BaseDustDropItem implements IBulkItem {

    public BaseDustBulkStorageItem(Properties p_41383_) {
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
        return 13659647;
    }

    public int getMaxAllowedStorage()
    {
        return PedestalConfig.COMMON.pedestal_baseDustStorage.get() + (PedestalConfig.COMMON.augment_t4StorageDust.get() * PedestalConfig.COMMON.augment_t4StorageInsertSize.get());
    }

    public int getItemVariant(ItemStack stack) {
        DustMagic dust = DustMagic.getDustMagicInItemStack(stack);
        if (!dust.isEmpty()) {
            double renderDevider = 100.0D * (double)(dust.getDustAmount() / this.getMaxAllowedStorage());
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

            if(survival && PedestalConfig.COMMON.bulkstorage_dustDischarge_toggle.get())
            {
                DustMagic dust = DustMagic.getDustMagicInItemStack(p_41404_);
                if(!dust.isEmpty())
                {
                    counter++;
                    if(counter>=20)
                    {
                        increaseDischargeCounter(p_41404_);
                        counter=0;
                    }
                }

                if(getDischargeCounter(p_41404_) >= PedestalConfig.COMMON.bulkstorage_dustDischarge.get())
                {
                    if(p_41406_ instanceof LivingEntity livingEntity)
                    {
                        removeDust(livingEntity,p_41404_);
                    }
                    p_41404_.shrink(1);
                }
            }
        }
    }

    //Maybe replace this with the actual reciped effects???(Would have to make part of mowlib natively though)
    public MobEffect getEffect()
    {
        Random rand = new Random();
        Map<Integer, MobEffect> NEGEFFECT = Map.ofEntries(
                Map.entry(0, MobEffects.BAD_OMEN),
                Map.entry(1,MobEffects.BLINDNESS),
                Map.entry(2,MobEffects.GLOWING),
                Map.entry(3,MobEffects.HUNGER),
                Map.entry(4,MobEffects.HARM),
                Map.entry(5,MobEffects.LEVITATION),
                Map.entry(6,MobEffects.DIG_SLOWDOWN),
                Map.entry(7,MobEffects.CONFUSION),
                Map.entry(8,MobEffects.POISON),
                Map.entry(9,MobEffects.MOVEMENT_SLOWDOWN),
                Map.entry(10,MobEffects.UNLUCK),
                Map.entry(11,MobEffects.WEAKNESS),
                Map.entry(12,MobEffects.WITHER),
                Map.entry(13,MobEffects.INVISIBILITY),
                Map.entry(14,MobEffects.DARKNESS),
                Map.entry(15,MobEffects.SLOW_FALLING)
        );

        return NEGEFFECT.getOrDefault(rand.nextInt(16),MobEffects.DARKNESS);
    }

    public void removeDust(LivingEntity entity, ItemStack stack) {
        DustMagic dust = DustMagic.getDustMagicInItemStack(stack);
        if(!dust.isEmpty())
        {
            entity.addEffect(new MobEffectInstance(getEffect(),dust.getDustAmount(),getItemVariant(stack)));
        }
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        DustMagic stored = DustMagic.getDustMagicInItemStack(p_41421_);
        if(!stored.isEmpty())
        {
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(MODID + ".bulkstorage_dust"), ChatFormatting.GOLD);
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.literal(MowLibColorReference.getColorName(stored.getDustColor()) + ": " + stored.getDustAmount()), ChatFormatting.WHITE);
        }

        int charge = getDischargeCounter(p_41421_);
        if(charge>0)
        {
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(MODID + ".bulkstorage_dust_discharge"), ChatFormatting.RED);
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.literal("" + charge + "/" + PedestalConfig.COMMON.bulkstorage_dustDischarge.get() +""), ChatFormatting.WHITE);
        }
    }
}
