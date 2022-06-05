package com.mowmaster.pedestals.PedestalUtils;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class PedestalFakePlayer extends FakePlayer {
    private static WeakReference<PedestalFakePlayer> INSTANCE;
    private BlockPos fakePos;
    private ItemStack heldItem;

    public PedestalFakePlayer(ServerLevel world, @Nullable UUID getPlayerUUID, @Nullable String getPlayerName, @Nullable BasePedestalBlockEntity pedestal) {
        super(world, new GameProfile((getPlayerUUID != null)?(getPlayerUUID):(Util.NIL_UUID),(getPlayerName != null)?(getPlayerName):("[Pedestals]")));
        this.fakePos = (pedestal.getPos() !=null)?(pedestal.getPos()):(BlockPos.ZERO);
    }

    public PedestalFakePlayer(ServerLevel world, @Nullable UUID getPlayerUUID, @Nullable String getPlayerName, @Nullable BlockPos setPos, @Nullable ItemStack toolHeld) {
        super(world, new GameProfile((getPlayerUUID != null)?(getPlayerUUID):(Util.NIL_UUID),(getPlayerName != null)?(getPlayerName):("[Pedestals]")));
        this.fakePos = (setPos !=null)?(setPos):(BlockPos.ZERO);
    }


    //Set all sounds to silent???
    //Thanks again Loth: https://github.com/Lothrazar/Cyclic/blob/4ce8b97b8851d207af7712425f9f58506829583e/src/main/java/com/lothrazar/cyclic/util/UtilFakePlayer.java#L66
    //Didnt know that was a thing :D
    @Override
    public void setSilent(boolean isSilent) {
        super.setSilent(true);
    }

    /*@Override
    public boolean isPotionApplicable(@Nonnull EffectInstance effect) {
        return false;
    }

    @Override
    public BlockPos getPosition() {
        return fakePos;
    }

    @Override
    public Vector3d getPositionVec() {
        return new Vector3d(fakePos.getX(), fakePos.getY(), fakePos.getZ());
    }

    @Override
    protected void playEquipSound(ItemStack stack) {
        //do nothing
    }*/
}
