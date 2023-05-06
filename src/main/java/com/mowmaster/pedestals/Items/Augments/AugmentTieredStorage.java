package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

import net.minecraft.world.item.Item.Properties;

public class AugmentTieredStorage extends AugmentBase{

    public AugmentTieredStorage(Properties p_41383_) {
        super(p_41383_);
    }

    public static int getAdditionalItemStoragePerItem(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get())) return PedestalConfig.COMMON.augment_t1StorageItem.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get())) return PedestalConfig.COMMON.augment_t2StorageItem.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get())) return PedestalConfig.COMMON.augment_t3StorageItem.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get())) return PedestalConfig.COMMON.augment_t4StorageItem.get();
        return 0;
    }
    public static int getAdditionalFluidStoragePerItem(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get())) return PedestalConfig.COMMON.augment_t1StorageFluid.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get())) return PedestalConfig.COMMON.augment_t2StorageFluid.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get())) return PedestalConfig.COMMON.augment_t3StorageFluid.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get())) return PedestalConfig.COMMON.augment_t4StorageFluid.get();
        return 0;
    }
    public static int getAdditionalEnergyStoragePerItem(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get())) return PedestalConfig.COMMON.augment_t1StorageEnergy.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get())) return PedestalConfig.COMMON.augment_t2StorageEnergy.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get())) return PedestalConfig.COMMON.augment_t3StorageEnergy.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get())) return PedestalConfig.COMMON.augment_t4StorageEnergy.get();
        return 0;
    }
    public static int getAdditionalXpStoragePerItem(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get())) return PedestalConfig.COMMON.augment_t1StorageXp.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get())) return PedestalConfig.COMMON.augment_t2StorageXp.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get())) return PedestalConfig.COMMON.augment_t3StorageXp.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get())) return PedestalConfig.COMMON.augment_t4StorageXp.get();
        return 0;
    }
    public static int getAdditionalDustStoragePerItem(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get())) return PedestalConfig.COMMON.augment_t1StorageDust.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get())) return PedestalConfig.COMMON.augment_t2StorageDust.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get())) return PedestalConfig.COMMON.augment_t3StorageDust.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get())) return PedestalConfig.COMMON.augment_t4StorageDust.get();
        return 0;
    }

    public static int getAllowedInsertAmount(ItemStack stack)
    {
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get())) return PedestalConfig.COMMON.augment_t1StorageInsertSize.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get())) return PedestalConfig.COMMON.augment_t2StorageInsertSize.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get())) return PedestalConfig.COMMON.augment_t3StorageInsertSize.get();
        if(stack.is(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get())) return PedestalConfig.COMMON.augment_t4StorageInsertSize.get();
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        List<String> listed = new ArrayList<>();
        List<ChatFormatting> colors = new ArrayList<>();

        colors.add(ChatFormatting.YELLOW);
        listed.add(MODID + ".augments_storage_itemrate");
        colors.add(ChatFormatting.WHITE);
        listed.add(String.valueOf(AugmentTieredStorage.getAdditionalItemStoragePerItem(p_41421_)));

        colors.add(ChatFormatting.AQUA);
        listed.add(MODID + ".augments_storage_fluidrate");
        colors.add(ChatFormatting.WHITE);
        listed.add(String.valueOf(AugmentTieredStorage.getAdditionalFluidStoragePerItem(p_41421_)));

        colors.add(ChatFormatting.RED);
        listed.add(MODID + ".augments_storage_energyrate");
        colors.add(ChatFormatting.WHITE);
        listed.add(String.valueOf(AugmentTieredStorage.getAdditionalEnergyStoragePerItem(p_41421_)));

        colors.add(ChatFormatting.GREEN);
        listed.add(MODID + ".augments_storage_xprate");
        colors.add(ChatFormatting.WHITE);
        listed.add(String.valueOf(AugmentTieredStorage.getAdditionalXpStoragePerItem(p_41421_)));;

        colors.add(ChatFormatting.LIGHT_PURPLE);
        listed.add(MODID + ".augments_storage_dustrate");
        colors.add(ChatFormatting.WHITE);
        listed.add(String.valueOf(AugmentTieredStorage.getAdditionalDustStoragePerItem(p_41421_)));

        colors.add(ChatFormatting.GOLD);
        listed.add(MODID + ".augments_insertable");
        colors.add(ChatFormatting.WHITE);
        listed.add(String.valueOf(AugmentTieredStorage.getAllowedInsertAmount(p_41421_)));

        MowLibTooltipUtils.addTooltipShiftMessageMultiWithStyle(MODID,p_41423_,listed,colors);
    }


}
