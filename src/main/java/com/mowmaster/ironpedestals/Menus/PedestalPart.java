package com.mowmaster.ironpedestals.Menus;

import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.Items.ItemRegistry;
import com.mowmaster.ironpedestals.api.upgrade.IPedestalUpgrade;
import com.mowmaster.mowlib.filter.ITransportFilter;
import com.mowmaster.mowlib.registry.MowLibItems;
import com.mowmaster.mowlib.workcard.IWorkCard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

// The one-item slots of a pedestal besides its item storage, in the order the menu adds them: the upgrade, tool,
// work card and filter (the augments are buttons now, see PedestalMenu#clickMenuButton). Each knows what may go in,
// how to read, put and take its item on
// the block entity (so inserting runs the same code as the offhand clicks: an upgrade's onAdded, the filter's
// whitelist band...), and which item the screen shows faded in it while it's empty (the "ghost").
public enum PedestalPart
{
    UPGRADE(() -> ItemRegistry.PEDESTAL_UPGRADE_BASE.get()),
    TOOL(() -> Items.IRON_PICKAXE),
    WORK_CARD(() -> MowLibItems.WORKCARD_BASE.get()),
    FILTER(() -> MowLibItems.FILTER_BASE.get());

    private final Supplier<ItemLike> ghost;

    PedestalPart(Supplier<ItemLike> ghost)
    {
        this.ghost = ghost;
    }

    public ItemStack ghost()
    {
        return new ItemStack(ghost.get());
    }

    public boolean mayPlace(ItemStack stack)
    {
        if (stack.isEmpty()) return false;
        return switch (this)
        {
            case UPGRADE -> stack.getItem() instanceof IPedestalUpgrade;
            case TOOL -> BasePedestalBlockEntity.isAllowedTool(stack);
            case WORK_CARD -> stack.getItem() instanceof IWorkCard;
            case FILTER -> stack.getItem() instanceof ITransportFilter;
        };
    }

    public ItemStack get(BasePedestalBlockEntity pedestal)
    {
        return switch (this)
        {
            case UPGRADE -> pedestal.getUpgrade();
            case TOOL -> pedestal.getTool();
            case WORK_CARD -> pedestal.getWorkCard();
            case FILTER -> pedestal.getFilter();
        };
    }

    // Takes the part out (EMPTY if there's none)
    public ItemStack take(BasePedestalBlockEntity pedestal)
    {
        if (get(pedestal).isEmpty()) return ItemStack.EMPTY;
        return switch (this)
        {
            case UPGRADE -> pedestal.removeUpgrade();
            case TOOL -> pedestal.removeTool();
            case WORK_CARD -> pedestal.removeWorkCard();
            case FILTER -> pedestal.removeFilter();
        };
    }

    // Puts one of the stack in (the stack itself is left alone). player: the upgrade's owner, may be null.
    public void put(BasePedestalBlockEntity pedestal, ItemStack stack, @Nullable Player player)
    {
        if (stack.isEmpty()) return;
        // The insert methods split one item off the stack they're given, so they get a copy
        ItemStack one = stack.copyWithCount(1);
        switch (this)
        {
            case UPGRADE -> pedestal.insertUpgrade(one, player);
            case TOOL -> pedestal.insertTool(one);
            case WORK_CARD -> pedestal.insertWorkCard(one);
            case FILTER -> pedestal.insertFilter(one);
        }
    }
}
