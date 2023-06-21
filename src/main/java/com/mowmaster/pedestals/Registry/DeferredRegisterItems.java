package com.mowmaster.pedestals.Registry;

import com.mowmaster.pedestals.Items.Augments.*;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.*;
import com.mowmaster.pedestals.Items.Misc.ModifierGetterItem;
import com.mowmaster.pedestals.Items.Tools.Augment.*;
import com.mowmaster.pedestals.Items.Tools.Linking.LinkingTool;
import com.mowmaster.pedestals.Items.Tools.Linking.LinkingToolBackwards;
import com.mowmaster.pedestals.Items.Tools.Upgrade.*;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.*;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBlockBreaker;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


public class DeferredRegisterItems
{
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);


    public static final RegistryObject<Item> TOOL_LINKINGTOOL = ITEMS.register("tool_linkingtool",
            () -> new LinkingTool(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_LINKINGTOOLBACKWARDS = ITEMS.register("tool_linkingtoolbackwards",
            () -> new LinkingToolBackwards(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_UPGRADETOOL = ITEMS.register("tool_upgradetool",
            () -> new UpgradeTool(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_TOOLSWAPPER = ITEMS.register("tool_toolswapper",
            () -> new ToolSwapper(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TOOL_AUGMENTS_SPEED = ITEMS.register("tool_augments_speed",
            () -> new AugmentTool_Speed(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_AUGMENTS_CAPACITY = ITEMS.register("tool_augments_capacity",
            () -> new AugmentTool_Capacity(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_AUGMENTS_STORAGE = ITEMS.register("tool_augments_storage",
            () -> new AugmentTool_Storage(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_AUGMENTS_RANGE = ITEMS.register("tool_augments_range",
            () -> new AugmentTool_Range(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_AUGMENTS_DIFFUSER = ITEMS.register("tool_augments_diffuser",
            () -> new AugmentTool_Diffuser(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_AUGMENTS_ROUNDROBIN = ITEMS.register("tool_augments_roundrobin",
            () -> new AugmentTool_Robin(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_AUGMENTS_COLLIDE = ITEMS.register("tool_augments_collide",
            () -> new AugmentTool_Collide(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TOOL_MANIFEST = ITEMS.register("tool_manifesttool",
            () -> new PedestalManifestTool(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> PEDESTAL_UPGRADE_BASE = ITEMS.register("upgrade_pedestal_base",
            () -> new ItemUpgradeBase(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_IMPORT = ITEMS.register("upgrade_pedestal_import",
            () -> new ItemUpgradeImport(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_EXPORT = ITEMS.register("upgrade_pedestal_export",
            () -> new ItemUpgradeExport(new Item.Properties()));

    public static final RegistryObject<Item> PEDESTAL_UPGRADE_MAGNET = ITEMS.register("upgrade_pedestal_magnet",
            () -> new ItemUpgradeMagnet(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_BREAKER = ITEMS.register("upgrade_pedestal_breaker",
            () -> new ItemUpgradeBlockBreaker(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_PLACER = ITEMS.register("upgrade_pedestal_placer",
            () -> new ItemUpgradeBlockPlacer(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_FILLER = ITEMS.register("upgrade_pedestal_filler",
            () -> new ItemUpgradeFiller(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_QUARRY = ITEMS.register("upgrade_pedestal_quarry",
            () -> new ItemUpgradeQuarry(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_CHOPPER = ITEMS.register("upgrade_pedestal_chopper",
            () -> new ItemUpgradeChopper(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_HARVESTER = ITEMS.register("upgrade_pedestal_harvester",
            () -> new ItemUpgradeHarvester(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_PLANTER = ITEMS.register("upgrade_pedestal_planter",
            () -> new ItemUpgradePlanter(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_FERTILIZER = ITEMS.register("upgrade_pedestal_fertilizer",
            () -> new ItemUpgradeFertilizer(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_HIVEHARVESTER = ITEMS.register("upgrade_pedestal_hiveharvester",
            () -> new ItemUpgradeHiveHarvester(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_VOID = ITEMS.register("upgrade_pedestal_void",
            () -> new ItemUpgradeVoid(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_RECYCLER = ITEMS.register("upgrade_pedestal_recycler",
            () -> new ItemUpgradeRecycler(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_FLUIDCONVERTER = ITEMS.register("upgrade_pedestal_fluidconverter",
            () -> new ItemUpgradeFluidConverter(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_DROPPER = ITEMS.register("upgrade_pedestal_dropper",
            () -> new ItemUpgradeDropper(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_PUMP = ITEMS.register("upgrade_pedestal_pump",
            () -> new ItemUpgradePump(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_DRAIN = ITEMS.register("upgrade_pedestal_drain",
            () -> new ItemUpgradeDrain(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_SHEERER = ITEMS.register("upgrade_pedestal_sheerer",
            () -> new ItemUpgradeSheerer(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_MILKER = ITEMS.register("upgrade_pedestal_milker",
            () -> new ItemUpgradeMilker(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_BREEDER = ITEMS.register("upgrade_pedestal_breeder",
            () -> new ItemUpgradeBreeder(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_ATTACKER = ITEMS.register("upgrade_pedestal_attacker",
            () -> new ItemUpgradeAttacker(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_FAN = ITEMS.register("upgrade_pedestal_fan",
            () -> new ItemUpgradeFan(new Item.Properties()));

    public static final RegistryObject<Item> PEDESTAL_UPGRADE_BOTTLER = ITEMS.register("upgrade_pedestal_bottler",
            () -> new ItemUpgradeBottler(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_UNBOTTLER = ITEMS.register("upgrade_pedestal_unbottler",
            () -> new ItemUpgradeUnBottler(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_PACKAGER = ITEMS.register("upgrade_pedestal_packager",
            () -> new ItemUpgradePackager(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_UNPACKAGER = ITEMS.register("upgrade_pedestal_unpackager",
            () -> new ItemUpgradeUnPackager(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_CRAFTER = ITEMS.register("upgrade_pedestal_crafter",
            () -> new ItemUpgradeCrafter(new Item.Properties()));

    public static final RegistryObject<Item> PEDESTAL_UPGRADE_MODIFICATIONS = ITEMS.register("upgrade_pedestal_modifications",
            () -> new ItemUpgradeModifications(new Item.Properties()));

    public static final RegistryObject<Item> PEDESTAL_UPGRADE_SMELTER = ITEMS.register("upgrade_pedestal_smelter",
            () -> new ItemUpgradeSmelter(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_SMOKER = ITEMS.register("upgrade_pedestal_smoker",
            () -> new ItemUpgradeSmoker(new Item.Properties()));
    public static final RegistryObject<Item> PEDESTAL_UPGRADE_BLASTER = ITEMS.register("upgrade_pedestal_blaster",
            () -> new ItemUpgradeBlastFurnace(new Item.Properties()));

    public static final RegistryObject<Item> PEDESTAL_UPGRADE_RFGENERATOR = ITEMS.register("upgrade_pedestal_generator",
            () -> new ItemUpgradeGenerator_FurnaceFuels(new Item.Properties()));


    public static final RegistryObject<Item> PEDESTAL_UPGRADE_COBBLEGEN = ITEMS.register("upgrade_pedestal_cobblegen",
            () -> new ItemUpgradeMaterialGenerator(new Item.Properties()));

    public static final RegistryObject<Item> AUGMENT_PEDESTAL_ROUNDROBIN = ITEMS.register("augment_pedestal_roundrobin",
            () -> new AugmentBase(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_RENDERDIFFUSER = ITEMS.register("augment_pedestal_renderdiffuser",
            () -> new AugmentRenderDiffuser(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_NOCOLLIDE = ITEMS.register("augment_pedestal_nocollide",
            () -> new AugmentBase(new Item.Properties()));



    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T1_CAPACITY = ITEMS.register("augment_pedestal_t1_capacity",
            () -> new AugmentTieredCapacity(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T2_CAPACITY = ITEMS.register("augment_pedestal_t2_capacity",
            () -> new AugmentTieredCapacity(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T3_CAPACITY = ITEMS.register("augment_pedestal_t3_capacity",
            () -> new AugmentTieredCapacity(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T4_CAPACITY = ITEMS.register("augment_pedestal_t4_capacity",
            () -> new AugmentTieredCapacity(new Item.Properties()));

    //Increases storage, stacks, and max amount for fluids, energy, and xp
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T1_STORAGE = ITEMS.register("augment_pedestal_t1_storage",
            () -> new AugmentTieredStorage(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T2_STORAGE = ITEMS.register("augment_pedestal_t2_storage",
            () -> new AugmentTieredStorage(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T3_STORAGE = ITEMS.register("augment_pedestal_t3_storage",
            () -> new AugmentTieredStorage(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T4_STORAGE = ITEMS.register("augment_pedestal_t4_storage",
            () -> new AugmentTieredStorage(new Item.Properties()));


    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T1_SPEED = ITEMS.register("augment_pedestal_t1_speed",
            () -> new AugmentTieredSpeed(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T2_SPEED = ITEMS.register("augment_pedestal_t2_speed",
            () -> new AugmentTieredSpeed(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T3_SPEED = ITEMS.register("augment_pedestal_t3_speed",
            () -> new AugmentTieredSpeed(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T4_SPEED = ITEMS.register("augment_pedestal_t4_speed",
            () -> new AugmentTieredSpeed(new Item.Properties()));

    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T1_RANGE = ITEMS.register("augment_pedestal_t1_range",
            () -> new AugmentTieredRange(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T2_RANGE = ITEMS.register("augment_pedestal_t2_range",
            () -> new AugmentTieredRange(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T3_RANGE = ITEMS.register("augment_pedestal_t3_range",
            () -> new AugmentTieredRange(new Item.Properties()));
    public static final RegistryObject<Item> AUGMENT_PEDESTAL_T4_RANGE = ITEMS.register("augment_pedestal_t4_range",
            () -> new AugmentTieredRange(new Item.Properties()));

    public static final RegistryObject<Item> MECHANICAL_STORAGE_ITEM = ITEMS.register("mechanical_storage_item",
            () -> new BaseItemBulkStorageItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MECHANICAL_STORAGE_FLUID = ITEMS.register("mechanical_storage_fluid",
            () -> new BaseFluidBulkStorageItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MECHANICAL_STORAGE_ENERGY = ITEMS.register("mechanical_storage_energy",
            () -> new BaseEnergyBulkStorageItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MECHANICAL_STORAGE_XP = ITEMS.register("mechanical_storage_xp",
            () -> new BaseXpBulkStorageItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MECHANICAL_STORAGE_DUST = ITEMS.register("mechanical_storage_dust",
            () -> new BaseDustBulkStorageItem(new Item.Properties().stacksTo(1)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
