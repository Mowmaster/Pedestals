package com.mowmaster.pedestals.Items;

import com.mowmaster.pedestals.Items.Filters.FilterData;
import com.mowmaster.pedestals.Items.Storage.PacketContents;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeMode;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeOwner;
import com.mowmaster.pedestals.PedestalReferences;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Unit;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

// Item data components replace 1.20's stack NBT. persistent = saved with the item, networkSynchronized = sent to
// clients (needed for tooltips and item models).
public class PedestalDataComponents
{
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, PedestalReferences.MODID);

    // The transport type a filter is currently set to (its "mode"). Separate from FILTER_DATA so the item model
    // can switch texture on it (items/<filter>.json uses a "minecraft:component" select).
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> FILTER_MODE =
            COMPONENTS.registerComponentType("filter_mode", builder -> builder.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FilterData>> FILTER_DATA =
            COMPONENTS.registerComponentType("filter_data", builder -> builder.persistent(FilterData.CODEC).networkSynchronized(FilterData.STREAM_CODEC));

    // The pedestal a linking tool has selected
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> LINKED_POS =
            COMPONENTS.registerComponentType("linked_pos", builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

    // ---- Upgrades ----

    // Modifiers added by the modification upgrade (speed, range, area, capacities...)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UpgradeModifiers>> UPGRADE_MODIFIERS =
            COMPONENTS.registerComponentType("upgrade_modifiers", builder -> builder.persistent(UpgradeModifiers.CODEC).networkSynchronized(UpgradeModifiers.STREAM_CODEC));

    // Which transport types a transport upgrade works on, and which one the offhand clicks change
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UpgradeMode>> UPGRADE_MODE =
            COMPONENTS.registerComponentType("upgrade_mode", builder -> builder.persistent(UpgradeMode.CODEC).networkSynchronized(UpgradeMode.STREAM_CODEC));

    // The selected type and whether it's on, for the item model (kept in step with UPGRADE_MODE)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UpgradeMode.Display>> UPGRADE_MODE_DISPLAY =
            COMPONENTS.registerComponentType("upgrade_mode_display", builder -> builder.persistent(UpgradeMode.Display.CODEC).networkSynchronized(UpgradeMode.Display.STREAM_CODEC));

    // The player who put the upgrade in a pedestal (removed when it's taken out)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UpgradeOwner>> UPGRADE_OWNER =
            COMPONENTS.registerComponentType("upgrade_owner", builder -> builder.persistent(UpgradeOwner.CODEC).networkSynchronized(UpgradeOwner.STREAM_CODEC));

    // ---- Work cards: the selected positions (area cards keep two corners) ----

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<BlockPos>>> WORK_POSITIONS =
            COMPONENTS.registerComponentType("work_positions", builder -> builder.persistent(BlockPos.CODEC.listOf())
                    .networkSynchronized(BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list())));

    // ---- Storage packets ----

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PacketContents<?>>> PACKET_CONTENTS =
            COMPONENTS.registerComponentType("packet_contents", builder -> builder.persistent(PacketContents.CODEC).networkSynchronized(PacketContents.STREAM_CODEC));

    // On a sacrifice upgrade: present = mob mode, absent = enchantment mode (also picks the item model).
    // Unit is Minecraft's "no value" type, for components that only matter by being there.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> SACRIFICE_MOB =
            COMPONENTS.registerComponentType("sacrifice_mob", builder -> builder.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC));

    public static void register(IEventBus eventBus)
    {
        COMPONENTS.register(eventBus);
    }
}
