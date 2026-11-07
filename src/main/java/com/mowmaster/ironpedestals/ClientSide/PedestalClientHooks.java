package com.mowmaster.ironpedestals.ClientSide;

import net.minecraft.core.BlockPos;

// Client-only calls made from common code (the block). Only ever called when level.isClientSide(), so on a dedicated
// server this class, and the screen classes it uses, are never loaded (Java loads a class the first time code that
// uses it actually runs).
public final class PedestalClientHooks
{
    private PedestalClientHooks() {}

    public static void openPedestalScreen(BlockPos pos)
    {
        PedestalScreen.open(pos);
    }
}
