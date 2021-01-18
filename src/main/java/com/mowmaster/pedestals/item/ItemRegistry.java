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
        PedestalBlock.onItemRegistryReady(e);

        ItemLinkingTool.onItemRegistryReady(e);
        ItemLinkingToolBackwards.onItemRegistryReady(e);
        ItemDevTool.onItemRegistryReady(e);
        ItemTagTool.onItemRegistryReady(e);
        ItemUpgradeTool.onItemRegistryReady(e);
        ItemToolSwapper.onItemRegistryReady(e);
        ItemDevTool.onItemRegistryReady(e);
        ItemColorPallet.onItemRegistryReady(e);
        ItemEnchantableBook.onItemRegistryReady(e);
        ItemPedestalUpgrades.onItemRegistryReady(e);
        ItemUpgradeDefault.onItemRegistryReady(e);

        ItemUpgradeRestriction.onItemRegistryReady(e);
        ItemUpgradeDropper.onItemRegistryReady(e);
        ItemUpgradePlacer.onItemRegistryReady(e);
        ItemUpgradeBreaker.onItemRegistryReady(e);
        ItemUpgradeChopper.onItemRegistryReady(e);
        ItemUpgradeChopperShrooms.onItemRegistryReady(e);

        ItemUpgradeEffectGrower.onItemRegistryReady(e);
        ItemUpgradeEffectHarvester.onItemRegistryReady(e);
        ItemUpgradeHarvesterBeeHives.onItemRegistryReady(e);
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
        ItemUpgradeExpRelayBlocked.onItemRegistryReady(e);
        ItemUpgradeExpTank.onItemRegistryReady(e);
        ItemUpgradeExpBottler.onItemRegistryReady(e);
        ItemUpgradeExpDropper.onItemRegistryReady(e);
        ItemUpgradeExpEnchanter.onItemRegistryReady(e);
        ItemUpgradeExpAnvil.onItemRegistryReady(e);
        ItemUpgradeExpGrindstone.onItemRegistryReady(e);

        ItemUpgradeEnergyGenerator.onItemRegistryReady(e);
        ItemUpgradeEnergyGeneratorExp.onItemRegistryReady(e);
        ItemUpgradeEnergyGeneratorMob.onItemRegistryReady(e);
        ItemUpgradeEnergyImport.onItemRegistryReady(e);
        ItemUpgradeEnergyExport.onItemRegistryReady(e);
        ItemUpgradeEnergyRelay.onItemRegistryReady(e);
        ItemUpgradeEnergyRelayBlocked.onItemRegistryReady(e);
        ItemUpgradeEnergyTank.onItemRegistryReady(e);
        ItemUpgradeEnergyCrusher.onItemRegistryReady(e);
        ItemUpgradeEnergyFurnace.onItemRegistryReady(e);
        ItemUpgradeEnergySawMill.onItemRegistryReady(e);
        ItemUpgradeEnergyQuarry.onItemRegistryReady(e);
        ItemUpgradeEnergyQuarryBlacklist.onItemRegistryReady(e);

        ItemUpgradeFluidImport.onItemRegistryReady(e);
        ItemUpgradeFluidExport.onItemRegistryReady(e);
        ItemUpgradeFluidPump.onItemRegistryReady(e);
        ItemUpgradeFluidDrain.onItemRegistryReady(e);
        ItemUpgradeFluidTank.onItemRegistryReady(e);
        ItemUpgradeFluidRelay.onItemRegistryReady(e);
        ItemUpgradeFluidRelayBlocked.onItemRegistryReady(e);
        ItemUpgradeFluidFilter.onItemRegistryReady(e);
        ItemUpgradeFluidFilterBlacklist.onItemRegistryReady(e);
        ItemUpgradeFluidFilteredImport.onItemRegistryReady(e);
        ItemUpgradeFluidPumpFilter.onItemRegistryReady(e);
        ItemUpgradeFluidPumpFilterBlacklist.onItemRegistryReady(e);
        ItemUpgradeFluidCrafter.onItemRegistryReady(e);
        ItemUpgradeMilker.onItemRegistryReady(e);
        ItemUpgradeExpFluidConverter.onItemRegistryReady(e);

        ItemUpgradeEnderFilteredImporterFood.onItemRegistryReady(e);
        ItemUpgradeEnderFilteredImporterEnchanted.onItemRegistryReady(e);
        ItemUpgradeEnderFilteredImporterMod.onItemRegistryReady(e);
        ItemUpgradeEnderFilteredImporterFuzzy.onItemRegistryReady(e);
        ItemUpgradeEnderFilteredImporterExact.onItemRegistryReady(e);
        ItemUpgradeEnderFilteredExporter.onItemRegistryReady(e);
        ItemUpgradeEnderImporter.onItemRegistryReady(e);
        ItemUpgradeEnderExporter.onItemRegistryReady(e);
        ItemUpgradeEnderFilteredRestock.onItemRegistryReady(e);

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
        ItemUpgradeItemTank.onItemRegistryReady(e);

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
        ItemUpgradeFilterDurability.onItemRegistryReady(e);
        ItemUpgradeFilterDurabilityLess.onItemRegistryReady(e);
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
        ItemUpgradeCompactingCrafter.onItemRegistryReady(e);
        ItemUpgradeCrafter.onItemRegistryReady(e);
        ItemUpgradeRecycler.onItemRegistryReady(e);
        ItemCraftingPlaceholder.onItemRegistryReady(e);
        ItemUpgradeCobbleGen.onItemRegistryReady(e);
        ItemUpgradeVoid.onItemRegistryReady(e);
        ItemUpgradeEnergyVoid.onItemRegistryReady(e);
        ItemUpgradeFluidVoid.onItemRegistryReady(e);
        ItemUpgradeAttacker.onItemRegistryReady(e);
        ItemUpgradeEffect.onItemRegistryReady(e);
        ItemUpgradeFan.onItemRegistryReady(e);
        ItemUpgradeShearer.onItemRegistryReady(e);

        ItemDust.onItemRegistryReady(e);
    }

    public static void onItemColorsReady(ColorHandlerEvent.Item event)
    {
        PedestalBlock.handleItemColors(event);
        //PedestalBlock.handleItemColors(event);
        ItemColorPallet.handleItemColors(event);
    }
}
