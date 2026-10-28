package com.mowmaster.pedestals.Items;

import com.mowmaster.pedestals.Items.Filters.FilterData;
import com.mowmaster.pedestals.PedestalReferences;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

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

    public static void register(IEventBus eventBus)
    {
        COMPONENTS.register(eventBus);
    }
}
