package com.mowmaster.ironpedestals.Blocks.Pedestal;

import com.mojang.authlib.GameProfile;
import com.mowmaster.mowlib.util.MowLibFakePlayer;
import com.mowmaster.mowlib.util.MowLibOwner;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

// The "player" a pedestal's upgrades act as when they break, place, use or attack: MowLib's fake player (the
// upgrade owner's id and name, held tool attributes, full-strength attacks), with Pedestals' own name for upgrades
// that have no owner.
public class PedestalFakePlayer extends MowLibFakePlayer
{
    // Used when the upgrade has no owner (e.g. put in by another mod's machine). The id comes from the name, so it's
    // the same UUID as before the move to MowLib and claims made for it still match.
    private static final GameProfile DEFAULT_PROFILE = MowLibFakePlayer.profile("[Pedestals]");

    public PedestalFakePlayer(ServerLevel level, @Nullable MowLibOwner owner)
    {
        super(level, owner, DEFAULT_PROFILE);
    }
}
