package com.mowmaster.ironpedestals.Items.Upgrades;

import com.mowmaster.ironpedestals.Config;
import com.mowmaster.ironpedestals.Items.PedestalDataComponents;
import com.mowmaster.mowlib.datamaps.CBBlockColor;
import com.mowmaster.mowlib.datamaps.CBDataMaps;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

// Upgrade tiers. Unlike pedestal tiers these aren't separate items: every upgrade carries its tier in the
// ironpedestals:upgrade_tier component (absent = tier 0), raised by crafting it inside 8 of the next tier's material
// (UpgradeTierRecipe, recipe/upgrade_tiers/). What a tier does is in the config: config() = Config.UPGRADE_TIERS.get(tier).
//
// Colour: the upgrade's greyscale plate and frame are tinted with the tier block's mowlib:block_colors entry
// (client/UpgradeTierTint); fallbackColor is used until data maps are loaded.
public enum UpgradeTier
{
    BASE(0, "stone", () -> Blocks.STONE, 0x7D7D7D),
    // 26.x: COPPER_BLOCK is every oxidation stage of the copper block; weathering().unaffected() is the plain one
    COPPER(1, "copper", () -> Blocks.COPPER_BLOCK.weathering().unaffected(), 0xC06B4F),
    IRON(2, "iron", () -> Blocks.IRON_BLOCK, 0xDCDCDC),
    GOLD(3, "gold", () -> Blocks.GOLD_BLOCK, 0xF6D03D),
    DIAMOND(4, "diamond", () -> Blocks.DIAMOND_BLOCK, 0x62EDE4),
    NETHERITE(5, "netherite", () -> Blocks.NETHERITE_BLOCK, 0x423D3F),
    // 26.x: the 16 concretes are one collection by dye colour
    NETHERSTAR(6, "netherstar", () -> Blocks.CONCRETE.pick(DyeColor.PURPLE), 0x641F9C);

    public static final int MAX = 6;

    private final int tier;
    private final String key;
    private final Supplier<Block> colorBlock;
    private final int fallbackColor;

    UpgradeTier(int tier, String key, Supplier<Block> colorBlock, int fallbackColor)
    {
        this.tier = tier;
        this.key = key;
        this.colorBlock = colorBlock;
        this.fallbackColor = fallbackColor;
    }

    public int tier()
    {
        return tier;
    }

    // For lang keys: ironpedestals.upgrade_tier.<key>
    public String key()
    {
        return key;
    }

    // This tier's numbers, read from the config every time. (Not called values(): every enum has a static values().)
    public Config.UpgradeTierValues config()
    {
        return Config.UPGRADE_TIERS.get(tier);
    }

    // The tint for this tier
    public int color()
    {
        CBBlockColor entry = colorBlock.get().builtInRegistryHolder().getData(CBDataMaps.BLOCK_COLORS);
        return entry != null ? entry.rgb() : fallbackColor;
    }

    public static UpgradeTier byTier(int tier)
    {
        return values()[Math.clamp(tier, 0, MAX)];
    }

    // An upgrade stack's tier (tier 0 without the component)
    public static UpgradeTier of(ItemStack stack)
    {
        return byTier(stack.getOrDefault(PedestalDataComponents.UPGRADE_TIER.get(), 0));
    }

    // A copy of the stack (count 1) at this tier
    public ItemStack applyTo(ItemStack stack)
    {
        ItemStack tiered = stack.copyWithCount(1);
        if (tier == 0) tiered.remove(PedestalDataComponents.UPGRADE_TIER.get());
        else tiered.set(PedestalDataComponents.UPGRADE_TIER.get(), tier);
        return tiered;
    }
}
