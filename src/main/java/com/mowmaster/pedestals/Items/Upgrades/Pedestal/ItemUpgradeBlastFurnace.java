package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

public class ItemUpgradeBlastFurnace extends ItemUpgradeAbstractCookingBase<BlastingRecipe> {
    public ItemUpgradeBlastFurnace(Properties p_41383_) { super(p_41383_, RecipeType.BLASTING); }

    @Override
    public int baseEnergyCost() { return PedestalConfig.COMMON.upgrade_blast_baseEnergyCost.get(); }
}