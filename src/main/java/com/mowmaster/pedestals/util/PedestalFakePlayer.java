package com.mowmaster.pedestals.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class PedestalFakePlayer extends FakePlayer
{

    private static WeakReference<PedestalFakePlayer> INSTANCE;
    private BlockPos fakePos = new BlockPos(0,0,0);
    private ItemStack heldItem = ItemStack.EMPTY;

    public PedestalFakePlayer(ServerWorld world, UUID getplayerUUID, BlockPos setPos, ItemStack toolHeld) {
        super(world, new GameProfile(getplayerUUID,"[Pedestals]"));
        this.fakePos = setPos;
        this.heldItem = toolHeld;
    }

    @Override
    public boolean isPotionApplicable(@Nonnull EffectInstance effect) {
        return false;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
    }

    @Override
    public void setHeldItem(Hand hand, ItemStack stack) {
        super.setHeldItem(hand, stack);
    }
}
