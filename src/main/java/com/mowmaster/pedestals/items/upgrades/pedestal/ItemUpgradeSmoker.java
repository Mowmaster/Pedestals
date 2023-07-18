package com.mowmaster.pedestals.items.upgrades.pedestal;

import com.mowmaster.pedestals.configs.PedestalConfig;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;

public class ItemUpgradeSmoker extends ItemUpgradeAbstractCookingBase<SmokingRecipe> {
    public ItemUpgradeSmoker(Properties p_41383_) { super(p_41383_, RecipeType.SMOKING); }

    @Override
    public int baseEnergyCost() { return PedestalConfig.COMMON.upgrade_smoker_baseEnergyCost.get(); }
}