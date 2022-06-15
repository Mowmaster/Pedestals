package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.mowlib.MowLibUtils.TooltipUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class AugmentTieredStorage extends AugmentBase{

    private int itemAmount;
    private int fluidAmount;
    private int energyAmount;
    private int experienceAmount;
    private int allowedToInsert;
    public AugmentTieredStorage(Properties p_41383_, int amountStacks, int amountFluids, int amountEnergy, int amountXPLevels, int allowedInsert) {
        super(p_41383_);
        this.itemAmount = amountStacks;
        this.fluidAmount = amountFluids;
        this.energyAmount = amountEnergy;
        this.experienceAmount = amountXPLevels;
        this.allowedToInsert = allowedInsert;
    }

    public int getAdditionalItemStoragePerItem()
    {
        return itemAmount;
    }
    public int getAdditionalFluidStoragePerItem()
    {
        return fluidAmount;
    }
    public int getAdditionalEnergyStoragePerItem()
    {
        return energyAmount;
    }
    public int getAdditionalXpStoragePerItem()
    {
        return experienceAmount;
    }

    public int getAllowedInsertAmount()
    {
        return allowedToInsert;
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
            listed.add(""+storageAugment.getAdditionalItemStoragePerItem()+"");

            colors.add(ChatFormatting.AQUA);
            listed.add(MODID + ".augments_storage_fluidrate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+storageAugment.getAdditionalFluidStoragePerItem()+"");

            colors.add(ChatFormatting.RED);
            listed.add(MODID + ".augments_storage_energyrate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+storageAugment.getAdditionalEnergyStoragePerItem()+"");

            colors.add(ChatFormatting.GREEN);
            listed.add(MODID + ".augments_storage_xprate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+storageAugment.getAdditionalXpStoragePerItem()+"");

            colors.add(ChatFormatting.LIGHT_PURPLE);
            listed.add(MODID + ".augments_insertable");
            colors.add(ChatFormatting.GOLD);
            listed.add(""+storageAugment.getAllowedInsertAmount()+"");


            TooltipUtils.addTooltipShiftMessageMultiWithStyle(MODID+".augments",p_41423_,listed,colors);
        }
    }


}
