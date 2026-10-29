package com.mowmaster.pedestals.api.upgrade;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// Implemented by upgrade items. A pedestal holds one upgrade and calls it every server tick.
// Most upgrades should extend ItemUpgradeBase instead, which adds the work speed, modifiers, fuel, tools, mode
// switching and tooltips; this interface is what the pedestal itself needs, so another mod can write an upgrade
// from scratch if it wants to.
// Per-pedestal state (positions reached, timers...) goes in pedestal.getUpgradeData(), not on the upgrade item:
// the pedestal clears it whenever the upgrade, tool or work card changes.
public interface IPedestalUpgrade
{
    // Every server tick while the upgrade is in a pedestal
    void tick(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade);

    // Just put in a pedestal. player is null when it wasn't a player (e.g. loaded from an old world).
    default void onAdded(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade, @Nullable Player player)
    {
    }

    // About to be taken out, or dropped because the pedestal broke
    default void onRemoved(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
    }

    // Just before the pedestal throws away its upgrade data (the tool or work card changed, or a new upgrade went in).
    // Not called when the upgrade itself is taken out: onRemoved runs then instead.
    default void onUpgradeDataReset(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
    }

    // The comparator output while this upgrade is in (0-15), or -1 to use the pedestal's item fill level
    default int getComparatorSignal(BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        return -1;
    }

    // Which work cards the upgrade uses (null = none), and how far away their positions may be
    default @Nullable WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return null;
    }

    default int getWorkRange(ItemStack upgrade)
    {
        return 0;
    }

    // The tool used when none is inserted (e.g. the breaker acts like a stone pickaxe)
    default ItemStack getDefaultTool()
    {
        return ItemStack.EMPTY;
    }
}
