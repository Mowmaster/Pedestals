package com.mowmaster.pedestals.Items.Upgrades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// One effect the sacrifice reactor gives, from the pedestals:reactor_effects data map
// (data/<namespace>/data_maps/mob_effect/reactor_effects.json):
//   "minecraft:speed": {"tier": 1, "max_level": 3}
// The effect starts at level I from that tier, goes up one level every 5 tiers after it, and stops at max_level.
public record ReactorEffect(int tier, int maxLevel)
{
    public static final Codec<ReactorEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("tier").forGetter(ReactorEffect::tier),
            Codec.intRange(1, 255).optionalFieldOf("max_level", 1).forGetter(ReactorEffect::maxLevel))
            .apply(instance, ReactorEffect::new));

    // The amplifier at a reactor tier (0 = level I), or -1 if the tier is too low for it
    public int amplifierAt(int reactorTier)
    {
        if (reactorTier < tier) return -1;
        return Math.min(maxLevel - 1, (reactorTier - tier) / 5);
    }
}
