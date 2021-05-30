package com.mowmaster.pedestals.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

public class PedestalFakePlayer extends FakePlayer
{

    private static WeakReference<PedestalFakePlayer> INSTANCE;
    private BlockPos fakePos;
    private ItemStack heldItem;

    public PedestalFakePlayer(ServerWorld world, @Nullable UUID getPlayerUUID, @Nullable String getPlayerName, @Nullable BlockPos setPos, @Nullable ItemStack toolHeld) {
        //Think might fix some issues with players showing up as "Pedestals"???
        super(world, new GameProfile((getPlayerUUID != null)?(getPlayerUUID):(Util.DUMMY_UUID),(getPlayerName != null)?(getPlayerName):("[Pedestals]")));
        //super(world, new GameProfile((getplayerUUID != null)?(getplayerUUID):(Util.DUMMY_UUID),"[Pedestals]"));
        this.fakePos = (setPos !=null)?(setPos):(BlockPos.ZERO);
        this.heldItem = (toolHeld !=null )?(toolHeld):(ItemStack.EMPTY);


    }


    //Set all sounds to silent???
    //Thanks again Loth: https://github.com/Lothrazar/Cyclic/blob/4ce8b97b8851d207af7712425f9f58506829583e/src/main/java/com/lothrazar/cyclic/util/UtilFakePlayer.java#L66
    //Didnt know that was a thing :D
    @Override
    public void setSilent(boolean isSilent) {
        super.setSilent(true);
    }

    @Override
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
    public void setHeldItem(Hand hand, ItemStack stack) {
        super.setHeldItem(hand, stack);
    }
}
