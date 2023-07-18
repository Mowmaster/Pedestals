package com.mowmaster.pedestals.items.augments;

import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.pedestals.configs.PedestalConfig;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;
import static com.mowmaster.pedestals.pedestalutils.References.isDustLoaded;

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

        List<String>listed = new ArrayList<String>(Arrays.asList(MODID + ".augments_storage_itemrate",String.valueOf(AugmentTieredStorage.getAdditionalItemStoragePerItem(p_41421_)),MODID + ".augments_storage_fluidrate",String.valueOf(AugmentTieredStorage.getAdditionalFluidStoragePerItem(p_41421_)),MODID + ".augments_storage_energyrate",String.valueOf(AugmentTieredStorage.getAdditionalEnergyStoragePerItem(p_41421_)),MODID + ".augments_storage_xprate",String.valueOf(AugmentTieredStorage.getAdditionalXpStoragePerItem(p_41421_)),MODID + ".augments_storage_dustrate",String.valueOf(AugmentTieredStorage.getAdditionalDustStoragePerItem(p_41421_)),MODID + ".augments_insertable",String.valueOf(AugmentTieredStorage.getAllowedInsertAmount(p_41421_))));
        List<ChatFormatting>colors = new ArrayList<ChatFormatting>(Arrays.asList(ChatFormatting.YELLOW,ChatFormatting.WHITE,ChatFormatting.AQUA,ChatFormatting.WHITE,ChatFormatting.RED,ChatFormatting.WHITE,ChatFormatting.GREEN,ChatFormatting.WHITE,ChatFormatting.LIGHT_PURPLE,ChatFormatting.WHITE,ChatFormatting.GOLD,ChatFormatting.WHITE));

        if(isDustLoaded())
        {
            listed = new ArrayList<String>(Arrays.asList(MODID + ".augments_storage_itemrate",String.valueOf(AugmentTieredStorage.getAdditionalItemStoragePerItem(p_41421_)),MODID + ".augments_storage_fluidrate",String.valueOf(AugmentTieredStorage.getAdditionalFluidStoragePerItem(p_41421_)),MODID + ".augments_storage_energyrate",String.valueOf(AugmentTieredStorage.getAdditionalEnergyStoragePerItem(p_41421_)),MODID + ".augments_storage_xprate",String.valueOf(AugmentTieredStorage.getAdditionalXpStoragePerItem(p_41421_)),MODID + ".augments_insertable",String.valueOf(AugmentTieredStorage.getAllowedInsertAmount(p_41421_))));
            colors = new ArrayList<ChatFormatting>(Arrays.asList(ChatFormatting.YELLOW,ChatFormatting.WHITE,ChatFormatting.AQUA,ChatFormatting.WHITE,ChatFormatting.RED,ChatFormatting.WHITE,ChatFormatting.GREEN,ChatFormatting.WHITE,ChatFormatting.LIGHT_PURPLE,ChatFormatting.WHITE,ChatFormatting.GOLD,ChatFormatting.GOLD));
        }

        MowLibTooltipUtils.addTooltipShiftMessageMultiWithStyle(MODID,p_41423_,listed,colors);
    }


}
