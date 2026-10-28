package com.mowmaster.pedestals;

import com.mowmaster.pedestals.Recipes.RecyclingMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

// NeoForge data maps: a value attached to registry entries from JSON, which datapacks can add to, change or remove
// (like tags, but each entry carries data). Read one with holder.getData(TYPE), e.g. stack.typeHolder().getData(RECYCLING).
// Only the server uses these, so they aren't synced to clients.
@EventBusSubscriber(modid = PedestalReferences.MODID)
public class PedestalDataMaps
{
    // data/<namespace>/data_maps/item/recycling.json
    public static final DataMapType<Item, RecyclingMaterials> RECYCLING = DataMapType.builder(
            Identifier.fromNamespaceAndPath(PedestalReferences.MODID, "recycling"), Registries.ITEM, RecyclingMaterials.CODEC).build();

    @SubscribeEvent
    public static void register(RegisterDataMapTypesEvent event)
    {
        event.register(RECYCLING);
    }
}
