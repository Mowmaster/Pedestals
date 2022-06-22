package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.mowlib.MowLibUtils.TooltipUtils;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class AugmentTieredStorage extends AugmentBase{


    public AugmentTieredStorage(Properties p_41383_) {
        super(p_41383_);
    }

    public int getAdditionalItemStoragePerItem(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get()))return PedestalConfig.COMMON.augment_t1StorageItem.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get()))return PedestalConfig.COMMON.augment_t2StorageItem.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get()))return PedestalConfig.COMMON.augment_t3StorageItem.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get()))return PedestalConfig.COMMON.augment_t4StorageItem.get();
        return 0;
    }
    public int getAdditionalFluidStoragePerItem(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get()))return PedestalConfig.COMMON.augment_t1StorageFluid.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get()))return PedestalConfig.COMMON.augment_t2StorageFluid.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get()))return PedestalConfig.COMMON.augment_t3StorageFluid.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get()))return PedestalConfig.COMMON.augment_t4StorageFluid.get();
        return 0;
    }
    public int getAdditionalEnergyStoragePerItem(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get()))return PedestalConfig.COMMON.augment_t1StorageEnergy.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get()))return PedestalConfig.COMMON.augment_t2StorageEnergy.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get()))return PedestalConfig.COMMON.augment_t3StorageEnergy.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get()))return PedestalConfig.COMMON.augment_t4StorageEnergy.get();
        return 0;
    }
    public int getAdditionalXpStoragePerItem(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get()))return PedestalConfig.COMMON.augment_t1StorageXp.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get()))return PedestalConfig.COMMON.augment_t2StorageXp.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get()))return PedestalConfig.COMMON.augment_t3StorageXp.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get()))return PedestalConfig.COMMON.augment_t4StorageXp.get();
        return 0;
    }

    public int getAllowedInsertAmount(Item augment)
    {
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get()))return PedestalConfig.COMMON.augment_t1StorageInsertSize.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get()))return PedestalConfig.COMMON.augment_t2StorageInsertSize.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get()))return PedestalConfig.COMMON.augment_t3StorageInsertSize.get();
        if(augment.equals(DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get()))return PedestalConfig.COMMON.augment_t4StorageInsertSize.get();
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        if(p_41421_.getItem() instanceof AugmentTieredStorage storageAugment)
        {
            List<String> listed = new ArrayList<>();
            List<ChatFormatting> colors = new ArrayList<>();

            colors.add(ChatFormatting.YELLOW);
            listed.add(MODID + ".augments_storage_itemrate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+storageAugment.getAdditionalItemStoragePerItem(p_41421_.getItem())+"");

            colors.add(ChatFormatting.AQUA);
            listed.add(MODID + ".augments_storage_fluidrate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+storageAugment.getAdditionalFluidStoragePerItem(p_41421_.getItem())+"");

            colors.add(ChatFormatting.RED);
            listed.add(MODID + ".augments_storage_energyrate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+storageAugment.getAdditionalEnergyStoragePerItem(p_41421_.getItem())+"");

            colors.add(ChatFormatting.GREEN);
            listed.add(MODID + ".augments_storage_xprate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+storageAugment.getAdditionalXpStoragePerItem(p_41421_.getItem())+"");

            colors.add(ChatFormatting.LIGHT_PURPLE);
            listed.add(MODID + ".augments_insertable");
            colors.add(ChatFormatting.GOLD);
            listed.add(""+storageAugment.getAllowedInsertAmount(p_41421_.getItem())+"");


            TooltipUtils.addTooltipShiftMessageMultiWithStyle(MODID+".augments",p_41423_,listed,colors);
        }
    }


}
