package com.mowmaster.pedestals.PedestalUtils;


import net.minecraftforge.fml.ModList;

public class References {
    public static final String MODID = "pedestals";
    public static final String MODNAME = "Pedestals";

    public static boolean isDustLoaded()
    {
        return ModList.get().isLoaded("effectscrolls");
    }
    public static boolean isQuarkLoaded()
    {
        return ModList.get().isLoaded("quark");
    }
}
