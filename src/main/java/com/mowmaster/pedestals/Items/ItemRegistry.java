package com.mowmaster.pedestals.Items;

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
import com.mowmaster.pedestals.Items.Tools.FilterToolItem;
import com.mowmaster.pedestals.Items.Tools.LinkingToolItem;
import com.mowmaster.pedestals.PedestalReferences;
import net.minecraft.world.item.Item;
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

    public static void register(IEventBus eventBus)
    {
        PEDESTAL_ITEMS.register(eventBus);
    }
}
