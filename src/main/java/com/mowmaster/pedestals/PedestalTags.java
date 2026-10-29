package com.mowmaster.pedestals;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

// The tags upgrades check (same names as 1.20; the files are in data/pedestals/tags/<block|item|fluid>/).
// TagKey is just the tag's name; whether something is in it is checked with state.is(tag) / stack.is(tag).
public class PedestalTags
{
    // Breakers, quarries, chopper...: never break these
    public static final TagKey<Block> CANNOT_BREAK = block("pedestals_cannot_break");
    // The chopper may break these, the quarry may not (logs, leaves, mushrooms...)
    public static final TagKey<Block> CAN_CHOP_CANT_QUARRY = block("pedestals_can_chop_cant_quarry");
    // Placers, fillers, planters and the drain never place these
    public static final TagKey<Block> CANNOT_PLACE = block("pedestals_cannot_place");
    // The pump leaves these alone
    public static final TagKey<Block> CANT_PUMP = block("pedestals_cant_pump");
    // The recycler won't take these
    public static final TagKey<Item> CANNOT_RECYCLE = item("pedestals_cannot_recycle");
    // The XP anvil only gives these enchantments they could normally take (no conflicting ones, nothing on items that
    // can't be enchanted); if nothing else applies, the item just comes out
    public static final TagKey<Item> ANVIL_BLACKLIST = item("pedestals_anvil_blacklist");
    // All upgrades (for recipe ingredients)
    public static final TagKey<Item> UPGRADES = item("upgrades");
    // The drain won't place these fluids
    public static final TagKey<Fluid> CANNOT_PLACE_FLUID = TagKey.create(Registries.FLUID, id("pedestals_cannot_place_fluid"));

    private static TagKey<Block> block(String name)
    {
        return TagKey.create(Registries.BLOCK, id(name));
    }

    private static TagKey<Item> item(String name)
    {
        return TagKey.create(Registries.ITEM, id(name));
    }

    private static Identifier id(String name)
    {
        return Identifier.fromNamespaceAndPath(PedestalReferences.MODID, name);
    }
}
