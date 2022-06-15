package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.mowlib.MowLibUtils.TooltipUtils;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class AugmentTieredCapacity extends AugmentBase{

    private int itemAmount;
    private int fluidAmount;
    private int energyAmount;
    private int experienceAmount;
    private int allowedToInsert;
    public AugmentTieredCapacity(Properties p_41383_, int amountItems, int amountFluids, int amountEnergy, int amountXPLevels, int allowedInsert) {
        super(p_41383_);
        this.itemAmount = amountItems;
        this.fluidAmount = amountFluids;
        this.energyAmount = amountEnergy;
        this.experienceAmount = amountXPLevels;
        this.allowedToInsert = allowedInsert;
    }

    public int getAdditionalItemTransferRatePerItem()
    {
        return itemAmount;
    }
    public int getAdditionalFluidTransferRatePerItem()
    {
        return fluidAmount;
    }
    public int getAdditionalEnergyTransferRatePerItem()
    {
        return energyAmount;
    }
    public int getAdditionalXpTransferRatePerItem()
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

        if(p_41421_.getItem() instanceof AugmentTieredCapacity capacityAugment)
        {
            List<String> listed = new ArrayList<>();
            List<ChatFormatting> colors = new ArrayList<>();

            colors.add(ChatFormatting.YELLOW);
            listed.add(MODID + ".augments_capacity_itemrate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+capacityAugment.getAdditionalItemTransferRatePerItem()+"");

            colors.add(ChatFormatting.AQUA);
            listed.add(MODID + ".augments_capacity_fluidrate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+capacityAugment.getAdditionalFluidTransferRatePerItem()+"");

            colors.add(ChatFormatting.RED);
            listed.add(MODID + ".augments_capacity_energyrate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+capacityAugment.getAdditionalEnergyTransferRatePerItem()+"");

            colors.add(ChatFormatting.GREEN);
            listed.add(MODID + ".augments_capacity_xprate");
            colors.add(ChatFormatting.WHITE);
            listed.add(""+capacityAugment.getAdditionalXpTransferRatePerItem()+"");

            colors.add(ChatFormatting.LIGHT_PURPLE);
            listed.add(MODID + ".augments_insertable");
            colors.add(ChatFormatting.GOLD);
            listed.add(""+capacityAugment.getAllowedInsertAmount()+"");

            TooltipUtils.addTooltipShiftMessageMultiWithStyle(MODID+".augments",p_41423_,listed,colors);
        }
    }


}
