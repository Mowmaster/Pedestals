package com.mowmaster.pedestals.Registry;


import com.mowmaster.pedestals.Items.Augments.AugmentRenderDiffuser;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import com.mowmaster.pedestals.Items.Filters.FilterItem;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeExport;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeImport;
import com.mowmaster.pedestals.PedestalUtils.PedestalModesAndTypes;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemModelPropertiesDust
{
    public static void dustItemModes(Item item)
    {
        ItemProperties.register(item, new ResourceLocation(MODID + ":upgrade_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> {
            return ItemUpgradeImport.getUpgradeModeForRender(p_174625_);});
        ItemProperties.register(item, new ResourceLocation(MODID + ":upgrade_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> {
            return ItemUpgradeExport.getUpgradeModeForRender(p_174625_);});

        ItemProperties.register(item, new ResourceLocation(MODID + ":filter_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> {
            return PedestalModesAndTypes.getModeFromStack(p_174625_);});
        ItemProperties.register(item, new ResourceLocation(MODID + ":filter_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> {
            return PedestalModesAndTypes.getModeFromStack(p_174625_);});

        ItemProperties.register(item, new ResourceLocation(MODID + ":augment_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> {
            return AugmentRenderDiffuser.getAugmentMode(p_174625_);});
    }
}
