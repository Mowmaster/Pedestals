package com.mowmaster.ironpedestals.Blocks.Pedestal;

import com.mowmaster.ironpedestals.Config;
import com.mowmaster.mowlib.datamaps.CBBlockColor;
import com.mowmaster.mowlib.datamaps.CBDataMaps;
import com.mowmaster.mowlib.tint.CBTintBlockEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

// The pedestal tiers (they replace 1.20's speed, capacity, storage and range augments). Each tier is its own block
// (block id = id()), crafted from 8 of the tier's material around the tier before it (recipe/pedestals/).
// What a tier does is all in the config: config() = Config.TIERS.get(tier) (tier 0 = the [pedestal] section's base values).
//
// Colour: an undyed pedestal shows its tier's colour, taken from MowLib's mowlib:block_colors data map for the tier's
// block (the same colour a Color Picker or a picked-block Custom Dye gives). fallbackColor is used until data maps
// are loaded, and is also the item model's tint (assets/ironpedestals/items/<id>.json, "default"; ARGB).
// Dyeing still works on every tier.
public enum PedestalTier
{
    BASE(0, "block_pedestal", null, CBTintBlockEntity.DEFAULT_TINT),
    COPPER(1, "block_pedestal_copper", PedestalTier::copperBlock, 0xC06B4F),
    IRON(2, "block_pedestal_iron", () -> Blocks.IRON_BLOCK, 0xDCDCDC),
    GOLD(3, "block_pedestal_gold", () -> Blocks.GOLD_BLOCK, 0xF6D03D),
    DIAMOND(4, "block_pedestal_diamond", () -> Blocks.DIAMOND_BLOCK, 0x62EDE4),
    NETHERITE(5, "block_pedestal_netherite", () -> Blocks.NETHERITE_BLOCK, 0x423D3F),
    NETHERSTAR(6, "block_pedestal_netherstar", PedestalTier::purpleConcrete, 0x641F9C);

    // ---- Vanilla blocks whose names differ between Minecraft versions (UpgradeTier uses these too) ----
    // A method reference (PedestalTier::copperBlock) is a Supplier<Block> here: Java turns it into a lambda that
    // calls the method, so the block is only looked up when the colour is first needed.

    // The plain, unoxidised copper block. 26.2 made COPPER_BLOCK every oxidation stage (a WeatheringCopperCollection);
    // weathering().unaffected() is the plain one (same call NeoForge's own tags use). Before 26.2 it was a Block.
    public static Block copperBlock()
    {
        //? if >=26.2 {
        return Blocks.COPPER_BLOCK.weathering().unaffected();
        //?} else
        //return Blocks.COPPER_BLOCK;
    }

    // Purple concrete. 26.2 made the 16 concretes one collection by dye colour (pick(colour)); before, one field each.
    public static Block purpleConcrete()
    {
        //? if >=26.2 {
        return Blocks.CONCRETE.pick(DyeColor.PURPLE);
        //?} else
        //return Blocks.PURPLE_CONCRETE;
    }
    private final int tier;
    private final String id;
    private final @Nullable Supplier<Block> colorBlock;
    private final int fallbackColor;

    PedestalTier(int tier, String id, @Nullable Supplier<Block> colorBlock, int fallbackColor)
    {
        this.tier = tier;
        this.id = id;
        this.colorBlock = colorBlock;
        this.fallbackColor = fallbackColor;
    }

    public int tier()
    {
        return tier;
    }

    // The block's (and its item's) registry path
    public String id()
    {
        return id;
    }

    // This tier's numbers, read from the config every time (so config changes apply straight away).
    // Not called values(): every enum already has a static values() (all its constants), so that name is taken.
    public Config.TierValues config()
    {
        return Config.TIERS.get(tier);
    }

    // The colour an undyed pedestal of this tier shows (white for tier 0: the texture as it is)
    public int color()
    {
        if (colorBlock == null) return fallbackColor;
        // builtInRegistryHolder(): the block's registry entry, which is what data map values are attached to
        CBBlockColor entry = colorBlock.get().builtInRegistryHolder().getData(CBDataMaps.BLOCK_COLORS);
        return entry != null ? entry.rgb() : fallbackColor;
    }
}
