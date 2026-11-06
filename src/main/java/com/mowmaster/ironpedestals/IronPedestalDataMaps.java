package com.mowmaster.ironpedestals;

import com.mowmaster.ironpedestals.Items.Upgrades.ReactorEffect;
import com.mowmaster.ironpedestals.Recipes.RecyclingMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

// NeoForge data maps: a value attached to registry entries from JSON, which datapacks can add to, change or remove
// (like tags, but each entry carries data). Read one with holder.getData(TYPE), e.g. stack.typeHolder().getData(RECYCLING).
// Only the server uses these, so they aren't synced to clients.
@EventBusSubscriber(modid = IronPedestalReferences.MODID)
public class IronPedestalDataMaps
{
    // data/<namespace>/data_maps/item/recycling.json
    public static final DataMapType<Item, RecyclingMaterials> RECYCLING = DataMapType.builder(
            Identifier.fromNamespaceAndPath(IronPedestalReferences.MODID, "recycling"), Registries.ITEM, RecyclingMaterials.CODEC).build();

    // data/<namespace>/data_maps/mob_effect/reactor_effects.json: the effects the sacrifice reactor gives
    public static final DataMapType<MobEffect, ReactorEffect> REACTOR_EFFECTS = DataMapType.builder(
            Identifier.fromNamespaceAndPath(IronPedestalReferences.MODID, "reactor_effects"), Registries.MOB_EFFECT, ReactorEffect.CODEC).build();

    @SubscribeEvent
    public static void register(RegisterDataMapTypesEvent event)
    {
        event.register(RECYCLING);
        event.register(REACTOR_EFFECTS);
    }
}
