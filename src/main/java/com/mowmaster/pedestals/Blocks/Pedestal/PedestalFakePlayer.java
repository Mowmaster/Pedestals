package com.mowmaster.pedestals.Blocks.Pedestal;

import com.mojang.authlib.GameProfile;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeOwner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

// The "player" a pedestal's upgrades act as when they break, place, use or attack (replaces MowLibFakePlayer).
// It uses the upgrade owner's id and name, so protection mods treat it like that player.
// NeoForge's FakePlayer never ticks, so two things a normal player gets from ticking are done here instead:
// attacks are always fully charged, and the held tool's attributes (attack damage, mining speed...) are applied
// whenever the held item is set with holdTool.
public class PedestalFakePlayer extends FakePlayer
{
    // Used when the upgrade has no owner (e.g. put in by another mod's machine)
    private static final GameProfile DEFAULT_PROFILE = new GameProfile(
            UUID.nameUUIDFromBytes("[Pedestals]".getBytes(StandardCharsets.UTF_8)), "[Pedestals]");

    private ItemStack heldTool = ItemStack.EMPTY;

    public PedestalFakePlayer(ServerLevel level, @Nullable UpgradeOwner owner)
    {
        super(level, owner == null ? DEFAULT_PROFILE : new GameProfile(owner.id(), owner.name()));
    }

    // Stands it on the pedestal
    public void standOn(BlockPos pedestalPos)
    {
        snapTo(pedestalPos.getX() + 0.5, pedestalPos.getY() + 0.5, pedestalPos.getZ() + 0.5);
    }

    // Puts the tool in the main hand and swaps the old tool's attribute modifiers for the new one's.
    // Pass the pedestal's own stack to have it worn down, or a copy to leave it untouched.
    public void holdTool(ItemStack tool)
    {
        // Each tool modifier is applied under its own id, so removing by id takes exactly the old ones off
        heldTool.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            AttributeInstance instance = getAttributes().getInstance(attribute);
            if (instance != null) instance.removeModifier(modifier.id());
        });
        setItemInHand(InteractionHand.MAIN_HAND, tool);
        tool.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            AttributeInstance instance = getAttributes().getInstance(attribute);
            if (instance != null) instance.addOrUpdateTransientModifier(modifier);
        });
        heldTool = tool;
    }

    // Every attack is a full-strength one (a ticking player recharges between swings)
    @Override
    public float getAttackStrengthScale(float partialTicks)
    {
        return 1.0F;
    }
}
