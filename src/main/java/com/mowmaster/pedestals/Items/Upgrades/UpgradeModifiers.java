package com.mowmaster.pedestals.Items.Upgrades;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

// The modifiers applied to an upgrade by the modification upgrade (1.20 kept these as ints in the stack's NBT, under
// the same names). Item components must never change once made, so this is a record with an unmodifiable map, and
// "changing" a value makes a new UpgradeModifiers (with) that is set back on the stack.
public record UpgradeModifiers(Map<String, Integer> values)
{
    // The 1.20 names: the modification recipes refer to these
    public static final String SPEED = "upgradespeed";
    public static final String DAMAGE_CAPACITY = "upgradedamagecapacity";
    public static final String BLOCK_CAPACITY = "upgradeblockcapacity";
    public static final String AREA = "upgradearea";
    public static final String RANGE = "upgraderange";
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
    // Capacity modifiers are one per transport type: see PedestalTransportType#getCapacityModifier
    // (upgradeitemcapacity, upgradefluidcapacity, upgradeenergycapacity, upgradexpcapacity, ...).
    // Upgrades list this to accept every type's capacity modifier.
    public static final String ANY_CAPACITY = "*capacity";

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
