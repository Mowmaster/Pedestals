package com.mowmaster.pedestals.registry;


import com.mowmaster.pedestals.items.augments.AugmentRenderDiffuser;
import com.mowmaster.pedestals.items.filters.BaseFilter;
import com.mowmaster.pedestals.items.tools.augment.PedestalManifestTool;
import com.mowmaster.pedestals.items.upgrades.pedestal.ItemUpgradeExport;
import com.mowmaster.pedestals.items.upgrades.pedestal.ItemUpgradeImport;
import com.mowmaster.pedestals.items.upgrades.pedestal.ItemUpgradeMagnet;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;

public class ItemModelPropertiesDust
{
    public static void dustItemModes(Item item)
    {
        ItemProperties.register(item, new ResourceLocation(MODID + ":upgrade_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> ItemUpgradeImport.getUpgradeModeForRender(p_174625_));
        ItemProperties.register(item, new ResourceLocation(MODID + ":upgrade_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> ItemUpgradeExport.getUpgradeModeForRender(p_174625_));
        ItemProperties.register(item, new ResourceLocation(MODID + ":upgrade_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> ItemUpgradeMagnet.getUpgradeModeForRender(p_174625_));

        ItemProperties.register(item, new ResourceLocation(MODID + ":filter_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> BaseFilter.getFilterModeForRender(p_174625_));
        ItemProperties.register(item, new ResourceLocation(MODID + ":filter_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> BaseFilter.getFilterModeForRender(p_174625_));

        ItemProperties.register(item, new ResourceLocation(MODID + ":augment_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> AugmentRenderDiffuser.getAugmentMode(p_174625_));

        ItemProperties.register(item, new ResourceLocation(MODID + ":manifest_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> PedestalManifestTool.getManifestType(p_174625_));
    }
}
