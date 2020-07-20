package com.mowmaster.pedestals.util.compat.jei;

import com.mowmaster.pedestals.blocks.BlockPedestalTE;
import com.mowmaster.pedestals.item.*;
import com.mowmaster.pedestals.item.pedestalUpgrades.*;
import com.mowmaster.pedestals.references.Reference;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by KingMowmaster on 7/19/2020.
 */

@JeiPlugin
public class PedestalsPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_UID = new ResourceLocation(Reference.MODID, "plugin/main");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    private static void addInfoPage(IRecipeRegistration reg, Collection<Item> items, String name) {
        if (items.isEmpty()) return;
        String key = getDescKey(new ResourceLocation(Reference.MODID, name));
        List<ItemStack> stacks = items.stream().map(ItemStack::new).collect(Collectors.toList());
        reg.addIngredientInfo(stacks, VanillaTypes.ITEM, key);
    }

    public static void addValueInfoPage(IRecipeRegistration reg, Item item, String name, Object... values) {
        Collection<Item> items = Collections.singletonList(item);
        addValueInfoPage(reg, items, name, values);
    }

    private static void addValueInfoPage(IRecipeRegistration reg, Collection<Item> items, String name, Object... values) {
        if (items.isEmpty()) return;
        String key = getDescKey(new ResourceLocation(Reference.MODID, name));
        List<ItemStack> stacks = items.stream().map(ItemStack::new).collect(Collectors.toList());
        reg.addIngredientInfo(stacks, VanillaTypes.ITEM, I18n.format(key, values));
    }

    private static String getDescKey(ResourceLocation name) {
        return "jei." + name.getNamespace() + "." + name.getPath() + ".desc";
    }

    private static Item getItemFromIngredient(Ingredient ingredient) {
        return ingredient.getMatchingStacks()[0].getItem();
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        //Pedestals
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_000, "stone000");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_001, "stone001");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_002, "stone002");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_003, "stone003");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_010, "stone010");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_011, "stone011");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_012, "stone012");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_013, "stone013");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_020, "stone020");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_021, "stone021");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_022, "stone022");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_023, "stone023");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_030, "stone030");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_031, "stone031");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_032, "stone032");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_033, "stone033");

        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_100, "stone100");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_101, "stone101");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_102, "stone102");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_103, "stone103");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_110, "stone110");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_111, "stone111");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_112, "stone112");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_113, "stone113");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_120, "stone120");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_121, "stone121");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_122, "stone122");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_123, "stone123");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_130, "stone130");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_131, "stone131");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_132, "stone132");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_133, "stone133");

        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_200, "stone200");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_201, "stone201");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_202, "stone202");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_203, "stone203");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_210, "stone210");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_211, "stone211");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_212, "stone212");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_213, "stone213");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_220, "stone220");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_221, "stone221");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_222, "stone222");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_223, "stone223");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_230, "stone230");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_231, "stone231");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_232, "stone232");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_233, "stone233");

        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_300, "stone300");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_301, "stone301");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_302, "stone302");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_303, "stone303");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_310, "stone310");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_311, "stone311");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_312, "stone312");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_313, "stone313");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_320, "stone320");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_321, "stone321");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_322, "stone322");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_323, "stone323");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_330, "stone330");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_331, "stone331");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_332, "stone332");
        addValueInfoPage(registration, BlockPedestalTE.I_PEDESTAL_333, "stone333");
        //Items
        addValueInfoPage(registration, ItemColorPallet.COLORPALLET_DEFAULT, "itemcolorpalletdefault");
        addValueInfoPage(registration, ItemColorPallet.COLORPALLET, "itemcolorpallet");
        addValueInfoPage(registration, ItemCraftingPlaceholder.PLACEHOLDER, "placeholder");
        addValueInfoPage(registration, ItemLinkingTool.DEFAULT, "linkingtool");
        addValueInfoPage(registration, ItemPedestalUpgrades.SPEED, "upgradespeed");
        addValueInfoPage(registration, ItemPedestalUpgrades.CAPACITY, "upgradecapacity");
        addValueInfoPage(registration, ItemTagTool.TAG, "tagtool");
        //Upgrades
        addValueInfoPage(registration, ItemUpgradeBreaker.BREAKER, "breaker");
        addValueInfoPage(registration, ItemUpgradeChopper.CHOPPER, "chopper");
        addValueInfoPage(registration, ItemUpgradeChopperShrooms.CHOPPER, "choppershrooms");
        addValueInfoPage(registration, ItemUpgradeCobbleGen.COBBLE, "cobble");
        addValueInfoPage(registration, ItemUpgradeCrafter.CRAFTER_ONE, "crafter1");
        addValueInfoPage(registration, ItemUpgradeCrafter.CRAFTER_TWO, "crafter2");
        addValueInfoPage(registration, ItemUpgradeCrafter.CRAFTER_THREE, "crafter3");
        addValueInfoPage(registration, ItemUpgradeCrusher.CRUSHER, "crusher");
        addValueInfoPage(registration, ItemUpgradeDefault.DEFAULT, "default");
        addValueInfoPage(registration, ItemUpgradeDropper.DROPPER, "dropper");
        addValueInfoPage(registration, ItemUpgradeEffectGrower.GROWER, "grower");
        addValueInfoPage(registration, ItemUpgradeEffectHarvester.HARVESTER, "harvester");
        addValueInfoPage(registration, ItemUpgradeEffectPlanter.PLANTER, "planter");
        addValueInfoPage(registration, ItemUpgradeEffectMagnet.MAGNET, "magnet");
        addValueInfoPage(registration, ItemUpgradeExpCollector.XPMAGNET, "xpmagnet");
        addValueInfoPage(registration, ItemUpgradeExpRelay.XPRELAY, "xprelay");
        addValueInfoPage(registration, ItemUpgradeExpTank.XPTANK, "xptank");
        addValueInfoPage(registration, ItemUpgradeExpBottler.XPBOTTLER, "xpbottler");
        addValueInfoPage(registration, ItemUpgradeExpDropper.XPDROPPER, "xpdropper");
        addValueInfoPage(registration, ItemUpgradeExpEnchanter.XPENCHANTER, "xpenchanter");
        addValueInfoPage(registration, ItemUpgradeExpAnvil.XPANVIL, "xpanvil");
        addValueInfoPage(registration, ItemUpgradeImport.IMPORT, "import");
        addValueInfoPage(registration, ItemUpgradeExport.EXPORT, "export");
        addValueInfoPage(registration, ItemUpgradeRestock.RESTOCK, "restock");
        addValueInfoPage(registration, ItemUpgradeFilterEnchanted.ENCHANTED, "filterenchanted");
        addValueInfoPage(registration, ItemUpgradeFilterEnchantedBlacklist.ENCHANTED, "filterenchantedb");
        addValueInfoPage(registration, ItemUpgradeFilterItem.ITEM, "filteritem");
        addValueInfoPage(registration, ItemUpgradeFilterItemBlacklist.ITEM, "filteritemb");
        addValueInfoPage(registration, ItemUpgradeFilterItemStack.ITEMSTACK, "filteritemstack");
        addValueInfoPage(registration, ItemUpgradeFilterItemStackBlacklist.ITEMSTACK, "filteritemstackb");
        addValueInfoPage(registration, ItemUpgradeFilterMod.MOD, "filtermod");
        addValueInfoPage(registration, ItemUpgradeFilterModBlacklist.MOD, "filtermodb");
        addValueInfoPage(registration, ItemUpgradeFilterFood.FOOD, "filterfood");
        addValueInfoPage(registration, ItemUpgradeFilterFoodBlacklist.FOOD, "filterfoodb");
        addValueInfoPage(registration, ItemUpgradeFilterTag.TAG, "filtertag");
        addValueInfoPage(registration, ItemUpgradeFilterTagBlacklist.TAG, "filtertagb");
        addValueInfoPage(registration, ItemUpgradeFurnace.SMELTER, "smelter");
        addValueInfoPage(registration, ItemUpgradeSawMill.SAWMILL, "sawmill");
        addValueInfoPage(registration, ItemUpgradeQuarry.QUARRY, "quarry");
        addValueInfoPage(registration, ItemUpgradeQuarryBlacklist.QUARRY, "quarryb");
        addValueInfoPage(registration, ItemUpgradeMilker.MILKER, "milker");
        addValueInfoPage(registration, ItemUpgradeShearer.SHEARER, "shearer");
        addValueInfoPage(registration, ItemUpgradePlacer.PLACER, "placer");
        addValueInfoPage(registration, ItemUpgradeTeleporter.TELEPORTER, "teleporter");
        addValueInfoPage(registration, ItemUpgradeVoid.VOID, "void");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(new ItemStack(ItemUpgradeCrafter.CRAFTER_THREE), VanillaRecipeCategoryUid.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ItemUpgradeCrafter.CRAFTER_TWO), VanillaRecipeCategoryUid.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ItemUpgradeCrafter.CRAFTER_ONE), VanillaRecipeCategoryUid.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ItemUpgradeFurnace.SMELTER), VanillaRecipeCategoryUid.FURNACE);
        registration.addRecipeCatalyst(new ItemStack(ItemUpgradeExpAnvil.XPANVIL), VanillaRecipeCategoryUid.ANVIL);
    }





}
