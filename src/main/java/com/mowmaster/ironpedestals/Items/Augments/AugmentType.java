package com.mowmaster.ironpedestals.Items.Augments;

import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.minecraft.resources.Identifier;

// The pedestal augments. They're switches on the pedestal now (the menu's buttons), not items: on or off, one of
// each. key: used in save data and lang keys. (1.20's speed, capacity, storage and range augments are pedestal tiers.)
//
// The particle & render augment (DIFFUSER) also has a mode, saying what it hides:
//   0 particles, 1 item, 2 upgrade, 3 particles + item, 4 particles + upgrade, 5 item + upgrade, 6 all three
public enum AugmentType
{
    ROUND_ROBIN("roundrobin"), // links take turns instead of the first one getting everything
    DIFFUSER("diffuser"),      // hides particles and/or the item and upgrade renders
    COLLIDE("collide");        // upgrades also work on entities touching the pedestal

    public static final int DIFFUSER_MODES = 7;
    // The diffuser's icon per mode (textures/item/augments/augment_renderdiffuser_<suffix>.png)
    private static final String[] DIFFUSER_SUFFIXES = {"p", "i", "u", "pi", "pu", "iu", "piu"};

    private final String key;

    AugmentType(String key)
    {
        this.key = key;
    }

    public String key()
    {
        return key;
    }

    // The button's icon: the old augment item's texture (the diffuser's shows its mode). blit draws any texture by
    // its full path, so these don't need to be GUI sprites.
    public Identifier icon(int diffuserMode)
    {
        String path = switch (this)
        {
            case ROUND_ROBIN -> "roundrobin";
            case COLLIDE -> "nocollide";
            case DIFFUSER -> "augment_renderdiffuser_" + DIFFUSER_SUFFIXES[Math.floorMod(diffuserMode, DIFFUSER_MODES)];
        };
        return Identifier.fromNamespaceAndPath(IronPedestalReferences.MODID, "textures/item/augments/" + path + ".png");
    }

    // ---- What a diffuser mode hides ----

    public static boolean hidesParticles(int mode)
    {
        return mode == 0 || mode == 3 || mode == 4 || mode == 6;
    }

    public static boolean hidesItem(int mode)
    {
        return mode == 1 || mode == 3 || mode == 5 || mode == 6;
    }

    public static boolean hidesUpgrade(int mode)
    {
        return mode == 2 || mode == 4 || mode == 5 || mode == 6;
    }

    // ironpedestals.mode_augment_<name>: the mode's name ("Disable: Particles"...)
    public static String diffuserModeKey(int mode)
    {
        String[] names = {"particle", "item", "upgrade", "pi", "pu", "iu", "piu"};
        return IronPedestalReferences.MODID + ".mode_augment_" + names[Math.floorMod(mode, DIFFUSER_MODES)];
    }
}
