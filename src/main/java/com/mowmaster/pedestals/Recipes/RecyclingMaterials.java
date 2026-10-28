package com.mowmaster.pedestals.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.List;
import java.util.Optional;

// What the recycler gives back for an item, from the pedestals:recycling data map
// (data/<namespace>/data_maps/item/recycling.json). Pedestals' own file was made from the vanilla crafting recipes.
//   "minecraft:iron_pickaxe": {"results": [{"id": "minecraft:iron_ingot", "count": 3}, {"id": "minecraft:stick", "count": 2}]}
// results: the crafting materials, the most important first (with little room in the pedestal, the first one wins).
// salvage (optional): what an item under 5% durability gives instead. Without it the item's furnace result
// (an iron pickaxe smelts to an iron nugget) is used, or a stick if it has none.
// Another datapack can add entries, change them (a later pack's entry for the same item wins) or take them out with
// "remove": ["minecraft:iron_pickaxe"]; mods can add their tools in their own data_maps/item/recycling.json.
public record RecyclingMaterials(List<ItemStackTemplate> results, Optional<ItemStackTemplate> salvage)
{
    public static final Codec<RecyclingMaterials> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStackTemplate.CODEC.listOf().fieldOf("results").forGetter(RecyclingMaterials::results),
            ItemStackTemplate.CODEC.optionalFieldOf("salvage").forGetter(RecyclingMaterials::salvage))
            .apply(instance, RecyclingMaterials::new));
}
