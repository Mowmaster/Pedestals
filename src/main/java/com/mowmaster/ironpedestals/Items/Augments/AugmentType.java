package com.mowmaster.ironpedestals.Items.Augments;

// The kinds of pedestal augment, one of each per pedestal. key: used in save data, ids and lang keys.
// (1.20's speed, capacity, storage and range augments are gone: pedestal tiers do that now, see PedestalTier.)
public enum AugmentType
{
    ROUND_ROBIN("roundrobin"), // links take turns instead of the first one getting everything
    DIFFUSER("diffuser"),      // hides particles and/or the item and upgrade renders
    COLLIDE("collide");        // upgrades also work on entities touching the pedestal

    private final String key;

    AugmentType(String key)
    {
        this.key = key;
    }

    public String key()
    {
        return key;
    }

    // The tool that takes it out comes after this one in the tool's cycle
    public AugmentType next()
    {
        return values()[(ordinal() + 1) % values().length];
    }
}
