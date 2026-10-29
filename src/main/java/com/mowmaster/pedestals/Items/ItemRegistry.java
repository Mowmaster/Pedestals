package com.mowmaster.pedestals.Items;

import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Augments.AugmentItem;
import com.mowmaster.pedestals.Items.Augments.AugmentType;
import com.mowmaster.pedestals.Items.Filters.DurabilityFilterItem;
import com.mowmaster.pedestals.Items.Filters.EnchantedCountFilterItem;
import com.mowmaster.pedestals.Items.Filters.EnchantedExactFilterItem;
import com.mowmaster.pedestals.Items.Filters.EnchantedFilterItem;
import com.mowmaster.pedestals.Items.Filters.EnchantedFuzzyFilterItem;
import com.mowmaster.pedestals.Items.Filters.FoodFilterItem;
import com.mowmaster.pedestals.Items.Filters.ItemFilterItem;
import com.mowmaster.pedestals.Items.Filters.ItemStackFilterItem;
import com.mowmaster.pedestals.Items.Filters.ModFilterItem;
import com.mowmaster.pedestals.Items.Filters.RestrictedFilterItem;
import com.mowmaster.pedestals.Items.Storage.StoragePacketItem;
import com.mowmaster.pedestals.Items.Tools.AugmentToolItem;
import com.mowmaster.pedestals.Items.Tools.FilterToolItem;
import com.mowmaster.pedestals.Items.Tools.LinkingToolItem;
import com.mowmaster.pedestals.Items.Tools.PedestalToolItem;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.*;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardAreaItem;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardLocationsItem;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardPedestalsItem;
import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ItemRegistry {

    public static final DeferredRegister.Items PEDESTAL_ITEMS = DeferredRegister.createItems(PedestalReferences.MODID);

    // ---- Tools (same ids as 1.20) ----
    public static final DeferredItem<LinkingToolItem> TOOL_LINKINGTOOL = PEDESTAL_ITEMS.registerItem("tool_linkingtool",
            properties -> new LinkingToolItem(properties, false));
    public static final DeferredItem<LinkingToolItem> TOOL_LINKINGTOOLBACKWARDS = PEDESTAL_ITEMS.registerItem("tool_linkingtoolbackwards",
            properties -> new LinkingToolItem(properties, true));
    // Was mowlib:tool_filter
    public static final DeferredItem<FilterToolItem> TOOL_FILTERTOOL = PEDESTAL_ITEMS.registerItem("tool_filter", FilterToolItem::new);
    // In the offhand, left-click a pedestal to take its upgrade / tool / work card out (the work tool was mowlib:tool_work)
    public static final DeferredItem<PedestalToolItem> TOOL_UPGRADETOOL = PEDESTAL_ITEMS.registerItem("tool_upgradetool", PedestalToolItem::new);
    public static final DeferredItem<PedestalToolItem> TOOL_TOOLSWAPPER = PEDESTAL_ITEMS.registerItem("tool_toolswapper", PedestalToolItem::new);
    public static final DeferredItem<PedestalToolItem> TOOL_WORKTOOL = PEDESTAL_ITEMS.registerItem("tool_work", PedestalToolItem::new);

    // ---- Filters (were mowlib:filter_*; same names, now in the pedestals namespace) ----
    // The blank filter is only a crafting ingredient for the others
    public static final DeferredItem<Item> FILTER_BASE = PEDESTAL_ITEMS.registerSimpleItem("filter_base");
    public static final DeferredItem<ItemFilterItem> FILTER_ITEM = PEDESTAL_ITEMS.registerItem("filter_item", ItemFilterItem::new);
    public static final DeferredItem<ItemStackFilterItem> FILTER_ITEMSTACK = PEDESTAL_ITEMS.registerItem("filter_itemstack", ItemStackFilterItem::new);
    public static final DeferredItem<ModFilterItem> FILTER_MOD = PEDESTAL_ITEMS.registerItem("filter_mod", ModFilterItem::new);
    public static final DeferredItem<RestrictedFilterItem> FILTER_RESTRICTED = PEDESTAL_ITEMS.registerItem("filter_restricted", RestrictedFilterItem::new);
    public static final DeferredItem<DurabilityFilterItem> FILTER_DURABILITY = PEDESTAL_ITEMS.registerItem("filter_durability", DurabilityFilterItem::new);
    public static final DeferredItem<EnchantedFilterItem> FILTER_ENCHANTED = PEDESTAL_ITEMS.registerItem("filter_enchanted", EnchantedFilterItem::new);
    public static final DeferredItem<EnchantedCountFilterItem> FILTER_ENCHANTED_COUNT = PEDESTAL_ITEMS.registerItem("filter_enchantedcount", EnchantedCountFilterItem::new);
    public static final DeferredItem<EnchantedExactFilterItem> FILTER_ENCHANTED_EXACT = PEDESTAL_ITEMS.registerItem("filter_enchantedexact", EnchantedExactFilterItem::new);
    public static final DeferredItem<EnchantedFuzzyFilterItem> FILTER_ENCHANTED_FUZZY = PEDESTAL_ITEMS.registerItem("filter_enchantedfuzzy", EnchantedFuzzyFilterItem::new);
    public static final DeferredItem<FoodFilterItem> FILTER_FOOD = PEDESTAL_ITEMS.registerItem("filter_food", FoodFilterItem::new);

    // For the creative tab, in display order
    public static final List<DeferredItem<? extends Item>> FILTERS = List.of(FILTER_BASE, FILTER_ITEM, FILTER_ITEMSTACK, FILTER_MOD,
            FILTER_RESTRICTED, FILTER_DURABILITY, FILTER_ENCHANTED, FILTER_ENCHANTED_COUNT, FILTER_ENCHANTED_EXACT,
            FILTER_ENCHANTED_FUZZY, FILTER_FOOD);

    // ---- Work cards (were mowlib:workcard_*) ----
    public static final DeferredItem<WorkCardAreaItem> WORKCARD_AREA = PEDESTAL_ITEMS.registerItem("workcard_area", WorkCardAreaItem::new);
    public static final DeferredItem<WorkCardLocationsItem> WORKCARD_LOCATIONS = PEDESTAL_ITEMS.registerItem("workcard_locations", WorkCardLocationsItem::new);
    public static final DeferredItem<WorkCardPedestalsItem> WORKCARD_PEDESTALS = PEDESTAL_ITEMS.registerItem("workcard_belocations", WorkCardPedestalsItem::new);

    // ---- Storage packets, one per transport type (same ids as 1.20's mechanical storage items) ----
    // The transport type holders are Suppliers, so the item finds its type once registries are done
    public static final DeferredItem<StoragePacketItem> MECHANICAL_STORAGE_ITEM = PEDESTAL_ITEMS.registerItem("mechanical_storage_item",
            properties -> new StoragePacketItem(properties, PedestalTransportTypes.ITEMS));
    public static final DeferredItem<StoragePacketItem> MECHANICAL_STORAGE_FLUID = PEDESTAL_ITEMS.registerItem("mechanical_storage_fluid",
            properties -> new StoragePacketItem(properties, PedestalTransportTypes.FLUIDS));
    public static final DeferredItem<StoragePacketItem> MECHANICAL_STORAGE_ENERGY = PEDESTAL_ITEMS.registerItem("mechanical_storage_energy",
            properties -> new StoragePacketItem(properties, PedestalTransportTypes.ENERGY));
    public static final DeferredItem<StoragePacketItem> MECHANICAL_STORAGE_XP = PEDESTAL_ITEMS.registerItem("mechanical_storage_xp",
            properties -> new StoragePacketItem(properties, PedestalTransportTypes.EXPERIENCE));

    // ---- Upgrades (same ids as 1.20) ----
    // The blank coin every upgrade is crafted from; it isn't an upgrade itself
    public static final DeferredItem<Item> PEDESTAL_UPGRADE_BASE = PEDESTAL_ITEMS.registerSimpleItem("upgrade_pedestal_base");
    public static final DeferredItem<ItemUpgradeImport> PEDESTAL_UPGRADE_IMPORT = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_import", ItemUpgradeImport::new);
    public static final DeferredItem<ItemUpgradeExport> PEDESTAL_UPGRADE_EXPORT = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_export", ItemUpgradeExport::new);
    public static final DeferredItem<ItemUpgradeMagnet> PEDESTAL_UPGRADE_MAGNET = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_magnet", ItemUpgradeMagnet::new);
    public static final DeferredItem<ItemUpgradeBreaker> PEDESTAL_UPGRADE_BREAKER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_breaker", ItemUpgradeBreaker::new);
    public static final DeferredItem<ItemUpgradePlacer> PEDESTAL_UPGRADE_PLACER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_placer", ItemUpgradePlacer::new);
    public static final DeferredItem<ItemUpgradeFiller> PEDESTAL_UPGRADE_FILLER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_filler", ItemUpgradeFiller::new);
    public static final DeferredItem<ItemUpgradeQuarry> PEDESTAL_UPGRADE_QUARRY = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_quarry", ItemUpgradeQuarry::new);
    public static final DeferredItem<ItemUpgradeChopper> PEDESTAL_UPGRADE_CHOPPER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_chopper", ItemUpgradeChopper::new);
    public static final DeferredItem<ItemUpgradeHarvester> PEDESTAL_UPGRADE_HARVESTER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_harvester", ItemUpgradeHarvester::new);
    public static final DeferredItem<ItemUpgradePlanter> PEDESTAL_UPGRADE_PLANTER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_planter", ItemUpgradePlanter::new);
    public static final DeferredItem<ItemUpgradeFertilizer> PEDESTAL_UPGRADE_FERTILIZER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_fertilizer", ItemUpgradeFertilizer::new);
    public static final DeferredItem<ItemUpgradeHiveHarvester> PEDESTAL_UPGRADE_HIVEHARVESTER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_hiveharvester", ItemUpgradeHiveHarvester::new);
    public static final DeferredItem<ItemUpgradeVoid> PEDESTAL_UPGRADE_VOID = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_void", ItemUpgradeVoid::new);
    public static final DeferredItem<ItemUpgradeRecycler> PEDESTAL_UPGRADE_RECYCLER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_recycler", ItemUpgradeRecycler::new);
    public static final DeferredItem<ItemUpgradeFluidConverter> PEDESTAL_UPGRADE_FLUIDCONVERTER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_fluidconverter", ItemUpgradeFluidConverter::new);
    public static final DeferredItem<ItemUpgradeDropper> PEDESTAL_UPGRADE_DROPPER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_dropper", ItemUpgradeDropper::new);
    public static final DeferredItem<ItemUpgradePump> PEDESTAL_UPGRADE_PUMP = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_pump", ItemUpgradePump::new);
    public static final DeferredItem<ItemUpgradeDrain> PEDESTAL_UPGRADE_DRAIN = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_drain", ItemUpgradeDrain::new);
    // "sheerer" is 1.20's spelling; the id stays so recipes and worlds keep working
    public static final DeferredItem<ItemUpgradeShearer> PEDESTAL_UPGRADE_SHEARER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_sheerer", ItemUpgradeShearer::new);
    public static final DeferredItem<ItemUpgradeMilker> PEDESTAL_UPGRADE_MILKER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_milker", ItemUpgradeMilker::new);
    public static final DeferredItem<ItemUpgradeBreeder> PEDESTAL_UPGRADE_BREEDER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_breeder", ItemUpgradeBreeder::new);
    public static final DeferredItem<ItemUpgradeAttacker> PEDESTAL_UPGRADE_ATTACKER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_attacker", ItemUpgradeAttacker::new);
    public static final DeferredItem<ItemUpgradeFan> PEDESTAL_UPGRADE_FAN = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_fan", ItemUpgradeFan::new);
    public static final DeferredItem<ItemUpgradeBottler> PEDESTAL_UPGRADE_BOTTLER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_bottler", ItemUpgradeBottler::new);
    public static final DeferredItem<ItemUpgradeUnbottler> PEDESTAL_UPGRADE_UNBOTTLER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_unbottler", ItemUpgradeUnbottler::new);
    public static final DeferredItem<ItemUpgradePackager> PEDESTAL_UPGRADE_PACKAGER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_packager", ItemUpgradePackager::new);
    public static final DeferredItem<ItemUpgradeUnpackager> PEDESTAL_UPGRADE_UNPACKAGER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_unpackager", ItemUpgradeUnpackager::new);
    public static final DeferredItem<ItemUpgradeCrafter> PEDESTAL_UPGRADE_CRAFTER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_crafter", ItemUpgradeCrafter::new);
    public static final DeferredItem<ItemUpgradeModifications> PEDESTAL_UPGRADE_MODIFICATIONS = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_modifications", ItemUpgradeModifications::new);
    public static final DeferredItem<ItemUpgradeCooking<SmeltingRecipe>> PEDESTAL_UPGRADE_SMELTER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_smelter",
            properties -> new ItemUpgradeCooking<>(properties, RecipeType.SMELTING, Config.SMELTER_ENERGY));
    public static final DeferredItem<ItemUpgradeCooking<SmokingRecipe>> PEDESTAL_UPGRADE_SMOKER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_smoker",
            properties -> new ItemUpgradeCooking<>(properties, RecipeType.SMOKING, Config.SMOKER_ENERGY));
    public static final DeferredItem<ItemUpgradeCooking<BlastingRecipe>> PEDESTAL_UPGRADE_BLASTER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_blaster",
            properties -> new ItemUpgradeCooking<>(properties, RecipeType.BLASTING, Config.BLASTER_ENERGY));
    public static final DeferredItem<ItemUpgradeGenerator> PEDESTAL_UPGRADE_GENERATOR = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_generator", ItemUpgradeGenerator::new);
    public static final DeferredItem<ItemUpgradeMaterialGenerator> PEDESTAL_UPGRADE_COBBLEGEN = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_cobblegen", ItemUpgradeMaterialGenerator::new);
    // From 1.16 (coin/xpenchanter)
    public static final DeferredItem<ItemUpgradeEnchanter> PEDESTAL_UPGRADE_ENCHANTER = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_enchanter", ItemUpgradeEnchanter::new);
    // From 1.16 (coin/xpanvil)
    public static final DeferredItem<ItemUpgradeAnvil> PEDESTAL_UPGRADE_ANVIL = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_anvil", ItemUpgradeAnvil::new);
    public static final DeferredItem<ItemUpgradeSacrifice> PEDESTAL_UPGRADE_SACRIFICE = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_sacrifice", ItemUpgradeSacrifice::new);
    public static final DeferredItem<ItemUpgradeReactor> PEDESTAL_UPGRADE_REACTOR = PEDESTAL_ITEMS.registerItem("upgrade_pedestal_reactor", ItemUpgradeReactor::new);

    // Made by the sacrifice upgrade, burned by the reactor
    public static final DeferredItem<SacrificeItem> SACRIFICE = PEDESTAL_ITEMS.registerItem("sacrifice", SacrificeItem::new);

    // ---- Augments (same ids as 1.20) ----
    public static final DeferredItem<AugmentItem> AUGMENT_T1_SPEED = augment("augment_pedestal_t1_speed", AugmentType.SPEED, 1);
    public static final DeferredItem<AugmentItem> AUGMENT_T2_SPEED = augment("augment_pedestal_t2_speed", AugmentType.SPEED, 2);
    public static final DeferredItem<AugmentItem> AUGMENT_T3_SPEED = augment("augment_pedestal_t3_speed", AugmentType.SPEED, 3);
    public static final DeferredItem<AugmentItem> AUGMENT_T4_SPEED = augment("augment_pedestal_t4_speed", AugmentType.SPEED, 4);
    public static final DeferredItem<AugmentItem> AUGMENT_T1_CAPACITY = augment("augment_pedestal_t1_capacity", AugmentType.CAPACITY, 1);
    public static final DeferredItem<AugmentItem> AUGMENT_T2_CAPACITY = augment("augment_pedestal_t2_capacity", AugmentType.CAPACITY, 2);
    public static final DeferredItem<AugmentItem> AUGMENT_T3_CAPACITY = augment("augment_pedestal_t3_capacity", AugmentType.CAPACITY, 3);
    public static final DeferredItem<AugmentItem> AUGMENT_T4_CAPACITY = augment("augment_pedestal_t4_capacity", AugmentType.CAPACITY, 4);
    public static final DeferredItem<AugmentItem> AUGMENT_T1_STORAGE = augment("augment_pedestal_t1_storage", AugmentType.STORAGE, 1);
    public static final DeferredItem<AugmentItem> AUGMENT_T2_STORAGE = augment("augment_pedestal_t2_storage", AugmentType.STORAGE, 2);
    public static final DeferredItem<AugmentItem> AUGMENT_T3_STORAGE = augment("augment_pedestal_t3_storage", AugmentType.STORAGE, 3);
    public static final DeferredItem<AugmentItem> AUGMENT_T4_STORAGE = augment("augment_pedestal_t4_storage", AugmentType.STORAGE, 4);
    public static final DeferredItem<AugmentItem> AUGMENT_T1_RANGE = augment("augment_pedestal_t1_range", AugmentType.RANGE, 1);
    public static final DeferredItem<AugmentItem> AUGMENT_T2_RANGE = augment("augment_pedestal_t2_range", AugmentType.RANGE, 2);
    public static final DeferredItem<AugmentItem> AUGMENT_T3_RANGE = augment("augment_pedestal_t3_range", AugmentType.RANGE, 3);
    public static final DeferredItem<AugmentItem> AUGMENT_T4_RANGE = augment("augment_pedestal_t4_range", AugmentType.RANGE, 4);
    public static final DeferredItem<AugmentItem> AUGMENT_ROUNDROBIN = augment("augment_pedestal_roundrobin", AugmentType.ROUND_ROBIN, 0);
    public static final DeferredItem<AugmentItem> AUGMENT_RENDERDIFFUSER = augment("augment_pedestal_renderdiffuser", AugmentType.DIFFUSER, 0);
    public static final DeferredItem<AugmentItem> AUGMENT_NOCOLLIDE = augment("augment_pedestal_nocollide", AugmentType.COLLIDE, 0);

    // The augment tools, one per kind (ids tool_augments_<key>)
    public static final DeferredItem<AugmentToolItem> TOOL_AUGMENTS_SPEED = registerAugmentTool(AugmentType.SPEED);
    public static final DeferredItem<AugmentToolItem> TOOL_AUGMENTS_CAPACITY = registerAugmentTool(AugmentType.CAPACITY);
    public static final DeferredItem<AugmentToolItem> TOOL_AUGMENTS_STORAGE = registerAugmentTool(AugmentType.STORAGE);
    public static final DeferredItem<AugmentToolItem> TOOL_AUGMENTS_RANGE = registerAugmentTool(AugmentType.RANGE);
    public static final DeferredItem<AugmentToolItem> TOOL_AUGMENTS_ROUNDROBIN = registerAugmentTool(AugmentType.ROUND_ROBIN);
    public static final DeferredItem<AugmentToolItem> TOOL_AUGMENTS_DIFFUSER = registerAugmentTool(AugmentType.DIFFUSER);
    public static final DeferredItem<AugmentToolItem> TOOL_AUGMENTS_COLLIDE = registerAugmentTool(AugmentType.COLLIDE);

    private static DeferredItem<AugmentItem> augment(String id, AugmentType type, int tier)
    {
        return PEDESTAL_ITEMS.registerItem(id, properties -> new AugmentItem(properties, type, tier));
    }

    private static DeferredItem<AugmentToolItem> registerAugmentTool(AugmentType type)
    {
        return PEDESTAL_ITEMS.registerItem("tool_augments_" + type.key(), properties -> new AugmentToolItem(properties, type));
    }

    // The tool for an augment kind
    public static Item augmentTool(AugmentType type)
    {
        return switch (type)
        {
            case SPEED -> TOOL_AUGMENTS_SPEED.get();
            case CAPACITY -> TOOL_AUGMENTS_CAPACITY.get();
            case STORAGE -> TOOL_AUGMENTS_STORAGE.get();
            case RANGE -> TOOL_AUGMENTS_RANGE.get();
            case ROUND_ROBIN -> TOOL_AUGMENTS_ROUNDROBIN.get();
            case DIFFUSER -> TOOL_AUGMENTS_DIFFUSER.get();
            case COLLIDE -> TOOL_AUGMENTS_COLLIDE.get();
        };
    }

    // For the creative tab, in display order (1.20's order)
    public static final List<DeferredItem<? extends Item>> UPGRADES = List.of(PEDESTAL_UPGRADE_BASE, PEDESTAL_UPGRADE_IMPORT,
            PEDESTAL_UPGRADE_EXPORT, PEDESTAL_UPGRADE_MAGNET, PEDESTAL_UPGRADE_BREAKER, PEDESTAL_UPGRADE_PLACER, PEDESTAL_UPGRADE_FILLER,
            PEDESTAL_UPGRADE_QUARRY, PEDESTAL_UPGRADE_CHOPPER, PEDESTAL_UPGRADE_HARVESTER, PEDESTAL_UPGRADE_PLANTER,
            PEDESTAL_UPGRADE_FERTILIZER, PEDESTAL_UPGRADE_HIVEHARVESTER, PEDESTAL_UPGRADE_VOID, PEDESTAL_UPGRADE_RECYCLER,
            PEDESTAL_UPGRADE_FLUIDCONVERTER, PEDESTAL_UPGRADE_DROPPER, PEDESTAL_UPGRADE_PUMP, PEDESTAL_UPGRADE_DRAIN,
            PEDESTAL_UPGRADE_SHEARER, PEDESTAL_UPGRADE_MILKER, PEDESTAL_UPGRADE_BREEDER, PEDESTAL_UPGRADE_ATTACKER,
            PEDESTAL_UPGRADE_FAN, PEDESTAL_UPGRADE_BOTTLER, PEDESTAL_UPGRADE_UNBOTTLER, PEDESTAL_UPGRADE_PACKAGER,
            PEDESTAL_UPGRADE_UNPACKAGER, PEDESTAL_UPGRADE_CRAFTER, PEDESTAL_UPGRADE_MODIFICATIONS, PEDESTAL_UPGRADE_SMELTER,
            PEDESTAL_UPGRADE_SMOKER, PEDESTAL_UPGRADE_BLASTER, PEDESTAL_UPGRADE_GENERATOR, PEDESTAL_UPGRADE_COBBLEGEN,
            PEDESTAL_UPGRADE_ENCHANTER, PEDESTAL_UPGRADE_ANVIL, PEDESTAL_UPGRADE_SACRIFICE, PEDESTAL_UPGRADE_REACTOR);

    public static final List<DeferredItem<? extends Item>> AUGMENTS = List.of(AUGMENT_T1_SPEED, AUGMENT_T2_SPEED, AUGMENT_T3_SPEED, AUGMENT_T4_SPEED, AUGMENT_T1_CAPACITY, AUGMENT_T2_CAPACITY, AUGMENT_T3_CAPACITY, AUGMENT_T4_CAPACITY, AUGMENT_T1_STORAGE, AUGMENT_T2_STORAGE, AUGMENT_T3_STORAGE, AUGMENT_T4_STORAGE, AUGMENT_T1_RANGE, AUGMENT_T2_RANGE, AUGMENT_T3_RANGE, AUGMENT_T4_RANGE,
            AUGMENT_ROUNDROBIN, AUGMENT_RENDERDIFFUSER, AUGMENT_NOCOLLIDE);

    public static final List<DeferredItem<? extends Item>> AUGMENT_TOOLS = List.of(TOOL_AUGMENTS_SPEED, TOOL_AUGMENTS_CAPACITY, TOOL_AUGMENTS_STORAGE,
            TOOL_AUGMENTS_RANGE, TOOL_AUGMENTS_ROUNDROBIN, TOOL_AUGMENTS_DIFFUSER, TOOL_AUGMENTS_COLLIDE);

    public static final List<DeferredItem<? extends Item>> WORKCARDS = List.of(WORKCARD_AREA, WORKCARD_LOCATIONS, WORKCARD_PEDESTALS);

    public static final List<DeferredItem<? extends Item>> PACKETS = List.of(MECHANICAL_STORAGE_ITEM, MECHANICAL_STORAGE_FLUID,
            MECHANICAL_STORAGE_ENERGY, MECHANICAL_STORAGE_XP);

    public static void register(IEventBus eventBus)
    {
        PEDESTAL_ITEMS.register(eventBus);
    }
}
