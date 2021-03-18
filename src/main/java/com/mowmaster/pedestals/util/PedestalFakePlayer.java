package com.mowmaster.pedestals.util;

import com.mojang.authlib.GameProfile;
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
import java.util.UUID;

public class PedestalFakePlayer extends FakePlayer
{

    private static WeakReference<PedestalFakePlayer> INSTANCE;
    private BlockPos fakePos;
    private ItemStack heldItem;

    public PedestalFakePlayer(ServerWorld world, @Nullable UUID getplayerUUID, @Nullable BlockPos setPos, @Nullable ItemStack toolHeld) {
        super(world, new GameProfile((getplayerUUID != null)?(getplayerUUID):(Util.NIL_UUID),"[Pedestals]"));
        this.fakePos = (setPos !=null)?(setPos):(BlockPos.ZERO);
        this.heldItem = (toolHeld !=null )?(toolHeld):(ItemStack.EMPTY);
    }

    @Override
    public boolean isPotionApplicable(@Nonnull EffectInstance effect) {
        return false;
    }

    @Override
    public BlockPos getBlockPosition() {
        return fakePos;
    }

    @Override
    public Vector3d getBlockPositionVec() {
        return new Vector3d(fakePos.getX(), fakePos.getY(), fakePos.getZ());
    }

    @Override
    public void setHeldItem(Hand hand, ItemStack stack) {
        super.setHeldItem(hand, stack);
    }
}
