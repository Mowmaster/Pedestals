package com.mowmaster.pedestals.item;

import com.mowmaster.pedestals.blocks.*;
import com.mowmaster.pedestals.item.pedestalUpgrades.*;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;

public class ItemRegistry
{
    public static void onItemRegistryReady(RegistryEvent.Register<Item> e)
    {
        //ItemGuideBook.onItemRegistryReady(e);
        BlockPedestalTE.onItemRegistryReady(e);
        PedestalBlock.onItemRegistryReady(e);

        ItemLinkingTool.onItemRegistryReady(e);
        ItemTagTool.onItemRegistryReady(e);
        ItemUpgradeTool.onItemRegistryReady(e);
        //ItemDevTool.onItemRegistryReady(e);
        ItemColorPallet.onItemRegistryReady(e);
        ItemEnchantableBook.onItemRegistryReady(e);
        ItemPedestalUpgrades.onItemRegistryReady(e);
        ItemUpgradeDefault.onItemRegistryReady(e);

        ItemUpgradeDropper.onItemRegistryReady(e);
        ItemUpgradePlacer.onItemRegistryReady(e);
        ItemUpgradeBreaker.onItemRegistryReady(e);
        ItemUpgradeChopper.onItemRegistryReady(e);
        ItemUpgradeChopperShrooms.onItemRegistryReady(e);

        ItemUpgradeEffectGrower.onItemRegistryReady(e);
        ItemUpgradeEffectHarvester.onItemRegistryReady(e);
        ItemUpgradeEffectPlanter.onItemRegistryReady(e);
        ItemUpgradeEffectMagnet.onItemRegistryReady(e);
        ItemUpgradeFilteredMagnetItem.onItemRegistryReady(e);
        ItemUpgradeFilteredMagnetItemBlacklist.onItemRegistryReady(e);
        ItemUpgradeFilteredMagnetItemStack.onItemRegistryReady(e);
        ItemUpgradeFilteredMagnetItemStackBlacklist.onItemRegistryReady(e);
        ItemUpgradeFilteredMagnetMod.onItemRegistryReady(e);
        ItemUpgradeFilteredMagnetModBlacklist.onItemRegistryReady(e);

        ItemUpgradeExpCollector.onItemRegistryReady(e);
        ItemUpgradeExpRelay.onItemRegistryReady(e);
        ItemUpgradeExpTank.onItemRegistryReady(e);
        ItemUpgradeExpBottler.onItemRegistryReady(e);
        ItemUpgradeExpDropper.onItemRegistryReady(e);
        ItemUpgradeExpEnchanter.onItemRegistryReady(e);
        ItemUpgradeExpAnvil.onItemRegistryReady(e);
        ItemUpgradeExpGrindstone.onItemRegistryReady(e);

        ItemUpgradeEnergyImport.onItemRegistryReady(e);
        ItemUpgradeEnergyExport.onItemRegistryReady(e);
        ItemUpgradeEnergyRelay.onItemRegistryReady(e);
        ItemUpgradeEnergyTank.onItemRegistryReady(e);
        ItemUpgradeEnergyCrusher.onItemRegistryReady(e);
        ItemUpgradeEnergyFurnace.onItemRegistryReady(e);
        ItemUpgradeEnergySawMill.onItemRegistryReady(e);
        ItemUpgradeEnergyQuarry.onItemRegistryReady(e);
        ItemUpgradeEnergyQuarryBlacklist.onItemRegistryReady(e);

        ItemUpgradeImport.onItemRegistryReady(e);
        ItemUpgradeFilteredImport.onItemRegistryReady(e);
        ItemUpgradeExport.onItemRegistryReady(e);
        ItemUpgradeFilteredExportItem.onItemRegistryReady(e);
        ItemUpgradeFilteredExportItemStack.onItemRegistryReady(e);
        ItemUpgradeFilteredExportMod.onItemRegistryReady(e);
        ItemUpgradeFilteredExportFood.onItemRegistryReady(e);
        ItemUpgradeFilteredExportEnchanted.onItemRegistryReady(e);
        ItemUpgradeRestock.onItemRegistryReady(e);
        ItemUpgradeFilteredRestock.onItemRegistryReady(e);

        ItemUpgradeFilterItem.onItemRegistryReady(e);
        ItemUpgradeFilterItemBlacklist.onItemRegistryReady(e);
        ItemUpgradeFilterItemStack.onItemRegistryReady(e);
        ItemUpgradeFilterItemStackBlacklist.onItemRegistryReady(e);
        ItemUpgradeFilterTag.onItemRegistryReady(e);
        ItemUpgradeFilterTagBlacklist.onItemRegistryReady(e);
        ItemUpgradeFilterMod.onItemRegistryReady(e);
        ItemUpgradeFilterModBlacklist.onItemRegistryReady(e);
        ItemUpgradeFilterFood.onItemRegistryReady(e);
        ItemUpgradeFilterFoodBlacklist.onItemRegistryReady(e);
        ItemUpgradeFilterEnchanted.onItemRegistryReady(e);
        ItemUpgradeFilterEnchantedBlacklist.onItemRegistryReady(e);
        ItemUpgradeFilterEnchantedFuzzy.onItemRegistryReady(e);
        ItemUpgradeFilterEnchantedFuzzyBlacklist.onItemRegistryReady(e);
        ItemUpgradeFilterEnchantedExact.onItemRegistryReady(e);
        ItemUpgradeFilterEnchantedExactBlacklist.onItemRegistryReady(e);
        ItemUpgradeFilterEnchantedCount.onItemRegistryReady(e);

        ItemUpgradeTeleporter.onItemRegistryReady(e);
        ItemUpgradeFurnace.onItemRegistryReady(e);
        ItemUpgradeCrusher.onItemRegistryReady(e);
        ItemUpgradeSawMill.onItemRegistryReady(e);
        ItemUpgradeQuarry.onItemRegistryReady(e);
        ItemUpgradeQuarryBlacklist.onItemRegistryReady(e);
        ItemUpgradeCrafter.onItemRegistryReady(e);
        ItemCraftingPlaceholder.onItemRegistryReady(e);
        ItemUpgradeCobbleGen.onItemRegistryReady(e);
        ItemUpgradeVoid.onItemRegistryReady(e);
        ItemUpgradeAttacker.onItemRegistryReady(e);
        ItemUpgradeEffect.onItemRegistryReady(e);
        ItemUpgradeFan.onItemRegistryReady(e);

        ItemUpgradeMilker.onItemRegistryReady(e);
        ItemUpgradeShearer.onItemRegistryReady(e);

        ItemDust.onItemRegistryReady(e);
    }

    public static void onItemColorsReady(ColorHandlerEvent.Item event)
    {
        BlockPedestalTE.handleItemColors(event);
        PedestalBlock.handleItemColors(event);
        ItemColorPallet.handleItemColors(event);
    }
}
