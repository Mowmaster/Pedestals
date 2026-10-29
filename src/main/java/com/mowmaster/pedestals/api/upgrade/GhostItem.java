package com.mowmaster.pedestals.api.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

// An item an upgrade wants to show the player but doesn't have: a missing input, drawn as a floating, pulsing item
// with a label (the amount needed) above it. Upgrades set these with BasePedestalBlockEntity#setGhosts; the pedestal
// syncs them to clients and its renderer draws them.
// above: the block to show it above (an input pedestal). Empty = circling the upgrade's own pedestal.
public record GhostItem(Optional<BlockPos> above, ItemStack stack, Component label)
{
    public static final Codec<GhostItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.optionalFieldOf("above").forGetter(GhostItem::above),
            ItemStack.CODEC.fieldOf("stack").forGetter(GhostItem::stack),
            ComponentSerialization.CODEC.fieldOf("label").forGetter(GhostItem::label))
            .apply(instance, GhostItem::new));

    // Records compare their fields with equals, but ItemStack doesn't have one (two equal stacks aren't "equal"), so
    // lists of ghosts are compared with this instead
    public static boolean same(List<GhostItem> first, List<GhostItem> second)
    {
        if (first.size() != second.size()) return false;
        for (int index = 0; index < first.size(); index++)
        {
            GhostItem a = first.get(index);
            GhostItem b = second.get(index);
            if (!a.above().equals(b.above()) || !ItemStack.matches(a.stack(), b.stack()) || !a.label().equals(b.label())) return false;
        }
        return true;
    }
}
