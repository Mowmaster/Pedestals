package com.mowmaster.ironpedestals.Items.Upgrades;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

// An upgrade's modifiers, by name (1.20 kept these as ints in the stack's NBT, under the same names). Item components
// must never change once made, so this is a record with an unmodifiable map, and "changing" a value makes a new
// UpgradeModifiers (with) that is set back on the stack.
//
// Where each value comes from now (ItemUpgradeBase#getModifier):
//   speed, super speed, block capacity, capacities -> the upgrade's tier (UpgradeTier)
//   range, area                                    -> the pedestal's range (not modifiers any more)
//   the SPECIALS                                   -> stored here, set to their max by the Upgrade smithing template
// An upgrade lists the names it takes in its constructor (ItemUpgradeBase).
public record UpgradeModifiers(Map<String, Integer> values)
{
    // The 1.20 names
    public static final String SPEED = "upgradespeed";
    public static final String BLOCK_CAPACITY = "upgradeblockcapacity";
    public static final String MAGNET = "upgrademagnet";
    public static final String GENTLE_HARVEST = "upgradegentle";
    public static final String SUPER_SPEED = "upgradesuperspeed";
    public static final String ENTITY_CONTAINERS = "upgradeentitystorage";
    public static final String REMOTE_STORAGE = "upgraderemotestorage";
    public static final String OPERATE_TO_BEDROCK = "upgradeoperatetobedrock";
    // Enchanter upgrade
    public static final String TREASURE_CHANCE = "upgradetreasurechance";
    public static final String PURIFIED = "upgradepurified";
    // Recycler upgrade
    public static final String DROP_CHANCE = "upgradedropchance";
    public static final String KNOWLEDGE = "upgradeknowledge";
    // XP anvil upgrade
    public static final String MENDING = "upgrademending";
    // Capacity modifiers are one per transport type: see TransportType#getCapacityModifier
    // (upgradeitemcapacity, upgradefluidcapacity, upgradeenergycapacity, upgradexpcapacity, ...).
    // Upgrades list this to accept every type's capacity modifier.
    public static final String ANY_CAPACITY = "*capacity";

    // The modifiers the Upgrade smithing template unlocks, and the value it sets (the 1.20 recipes' max).
    // Map.of keeps no order, so the tooltip sorts by name.
    public static final Map<String, Integer> SPECIALS = Map.of(
            ENTITY_CONTAINERS, 1,
            REMOTE_STORAGE, 1,
            OPERATE_TO_BEDROCK, 1,
            GENTLE_HARVEST, 1,
            TREASURE_CHANCE, 10,
            PURIFIED, 10,
            DROP_CHANCE, 10,
            KNOWLEDGE, 10,
            MENDING, 1);

    public static final UpgradeModifiers EMPTY = new UpgradeModifiers(Map.of());

    // Saved as {"upgradespeed": 2, ...}
    public static final Codec<UpgradeModifiers> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT)
            .xmap(UpgradeModifiers::new, UpgradeModifiers::values);
    public static final StreamCodec<ByteBuf, UpgradeModifiers> STREAM_CODEC = ByteBufCodecs.<ByteBuf, String, Integer, Map<String, Integer>>map(
            HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT).map(UpgradeModifiers::new, UpgradeModifiers::values);

    // A record's "compact constructor": runs before the fields are set, here to keep a frozen copy of the map
    public UpgradeModifiers
    {
        values = Map.copyOf(values);
    }

    public int get(String name)
    {
        return values.getOrDefault(name, 0);
    }

    public boolean has(String name)
    {
        return get(name) >= 1;
    }

    public UpgradeModifiers with(String name, int value)
    {
        Map<String, Integer> changed = new HashMap<>(values);
        if (value == 0) changed.remove(name);
        else changed.put(name, value);
        return new UpgradeModifiers(changed);
    }
}
