package com.mowmaster.pedestals.Items.Augments;

// The kinds of pedestal augment. The tiered ones stack (up to the tier's insertable amount, one tier at a time);
// the others are one per pedestal. key: used in save data, ids and lang keys.
public enum AugmentType
{
    SPEED("speed", true),             // fewer ticks between link transfers
    CAPACITY("capacity", true),       // higher link transfer rate, per transport type
    STORAGE("storage", true),         // more storage, per transport type (items: extra stacks)
    RANGE("range", true),             // longer linking range
    ROUND_ROBIN("roundrobin", false), // links take turns instead of the first one getting everything
    DIFFUSER("diffuser", false),      // hides particles and/or the item and upgrade renders
    COLLIDE("collide", false);        // upgrades also work on entities touching the pedestal

    private final String key;
    private final boolean tiered;

    AugmentType(String key, boolean tiered)
    {
        this.key = key;
        this.tiered = tiered;
    }

    public String key()
    {
        return key;
    }

    public boolean tiered()
    {
        return tiered;
    }

    // The tool that takes it out comes after this one in the tool's cycle
    public AugmentType next()
    {
        return values()[(ordinal() + 1) % values().length];
    }
}
