package com.mowmaster.pedestals.items.upgrades.pedestal;

import com.mowmaster.pedestals.configs.PedestalConfig;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class ItemUpgradeSmelter extends ItemUpgradeAbstractCookingBase<SmeltingRecipe> {
    public ItemUpgradeSmelter(Properties p_41383_) { super(p_41383_, RecipeType.SMELTING); }

    @Override
    public int baseEnergyCost() { return PedestalConfig.COMMON.upgrade_smelter_baseEnergyCost.get(); }
}