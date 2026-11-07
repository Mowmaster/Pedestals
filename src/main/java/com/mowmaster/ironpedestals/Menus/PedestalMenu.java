package com.mowmaster.ironpedestals.Menus;

import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.Transport.PedestalItemStorage;
import com.mowmaster.ironpedestals.api.upgrade.IPedestalUpgrade;
import com.mowmaster.mowlib.transport.TransportStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;

// The pedestal's menu (empty-hand right-click): its item stacks, the upgrade / tool / filter / work card slots, the
// three augment slots, and the player's inventory. Server and client each build one: the server's from the block
// entity (PedestalMenu(id, inventory, pedestal)), the client's from the position the server sends (fromNetwork),
// reading the client's copy of the same block entity. Vanilla then keeps the slots in step.
//
// Slot order (quickMoveStack relies on it): the pedestal's stacks, the 7 parts (PedestalPart order), then the
// player's 27 inventory slots and 9 hotbar slots. Stacks past the first 27 are on further pages: only the shown
// page's slots are active (the screen picks the page; it's client-only, the server doesn't need it).
public class PedestalMenu extends AbstractContainerMenu
{
    // Layout (pixels from the screen's top-left), shared with PedestalScreen
    public static final int COLUMNS = 9;
    public static final int ROWS = 3;
    public static final int PAGE_SIZE = COLUMNS * ROWS;
    public static final int ITEMS_X = 8;
    public static final int ITEMS_Y = 18;
    public static final int PARTS_Y = 80;
    public static final int AUGMENTS_X = 116;
    public static final int INVENTORY_Y = 112;
    public static final int WIDTH = 176;
    public static final int HEIGHT = 194;
    private static final int PARTS = PedestalPart.values().length;

    private final BasePedestalBlockEntity pedestal;
    private final int storageSize;
    private int page;

    public PedestalMenu(int containerId, Inventory inventory, BasePedestalBlockEntity pedestal)
    {
        super(PedestalMenus.PEDESTAL.get(), containerId);
        this.pedestal = pedestal;
        TransportStorage<ItemResource> items = pedestal.getItemStorage();
        this.storageSize = items.size();

        // The pedestal's stacks, 9 across, 3 rows to a page. The storage is always PedestalItemStorage (the pedestal
        // makes it); its set(index, resource, amount) is the IndexModifier the slot writes through.
        PedestalItemStorage storage = (PedestalItemStorage) items;
        for (int index = 0; index < storageSize; index++)
        {
            int slotPage = index / PAGE_SIZE;
            int x = ITEMS_X + (index % COLUMNS) * 18;
            int y = ITEMS_Y + ((index / COLUMNS) % ROWS) * 18;
            addSlot(new PedestalSlots.Storage(storage, storage::set, index, x, y, () -> page == slotPage));
        }

        // Upgrade, tool, filter, then the work card (shown only while the upgrade uses one, or one is in), and the
        // three augments on the right
        PedestalPartsContainer parts = new PedestalPartsContainer(pedestal, inventory.player);
        addSlot(new PedestalSlots.Part(parts, PedestalPart.UPGRADE, ITEMS_X, PARTS_Y, () -> true));
        addSlot(new PedestalSlots.Part(parts, PedestalPart.TOOL, ITEMS_X + 18, PARTS_Y, () -> true));
        addSlot(new PedestalSlots.Part(parts, PedestalPart.WORK_CARD, ITEMS_X + 54, PARTS_Y, this::showsWorkCard));
        addSlot(new PedestalSlots.Part(parts, PedestalPart.FILTER, ITEMS_X + 36, PARTS_Y, () -> true));
        addSlot(new PedestalSlots.Part(parts, PedestalPart.ROUND_ROBIN, AUGMENTS_X, PARTS_Y, () -> true));
        addSlot(new PedestalSlots.Part(parts, PedestalPart.DIFFUSER, AUGMENTS_X + 18, PARTS_Y, () -> true));
        addSlot(new PedestalSlots.Part(parts, PedestalPart.COLLIDE, AUGMENTS_X + 36, PARTS_Y, () -> true));

        // The player's inventory and hotbar (vanilla's helper: 3 rows, then the hotbar 4 pixels lower)
        addStandardInventorySlots(inventory, ITEMS_X, INVENTORY_Y);
    }

    // The client's menu: the server wrote the pedestal's position after the menu id (IMenuTypeExtension#create).
    // The client's copy of the block entity is there already (it's what the player clicked).
    public static PedestalMenu fromNetwork(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer)
    {
        BlockPos pos = buffer.readBlockPos();
        if (!(inventory.player.level().getBlockEntity(pos) instanceof BasePedestalBlockEntity pedestal))
        {
            throw new IllegalStateException("No pedestal at " + pos + " for its menu");
        }
        return new PedestalMenu(containerId, inventory, pedestal);
    }

    public BasePedestalBlockEntity getPedestal()
    {
        return pedestal;
    }

    // ---- Pages of item stacks (client) ----

    public int getPage()
    {
        return page;
    }

    public int getPageCount()
    {
        return Math.max(1, (storageSize + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public void setPage(int page)
    {
        this.page = Math.clamp(page, 0, getPageCount() - 1);
    }

    public int getStorageSize()
    {
        return storageSize;
    }

    // The work card slot is only there while the upgrade uses work cards (or a card is in, so it can come out)
    public boolean showsWorkCard()
    {
        ItemStack upgrade = pedestal.getUpgrade();
        boolean usesCard = upgrade.getItem() instanceof IPedestalUpgrade pedestalUpgrade && pedestalUpgrade.getWorkCardType(upgrade) != null;
        return usesCard || pedestal.hasWorkCard();
    }

    // ---- Shift-click ----

    // From the player: into the matching empty part slots first (upgrade, tool, filter, work card, augment), then
    // into the pedestal's stacks. From the pedestal: into the player's inventory, one normal stack at a time (vanilla
    // repeats a shift-click while it keeps returning the same item, so a big pedestal slot empties in one click).
    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        int partsStart = storageSize;
        int inventoryStart = partsStart + PARTS;
        int end = inventoryStart + 36;

        if (index < inventoryStart)
        {
            ItemStack inSlot = slot.getItem();
            ItemStack moving = inSlot.copyWithCount(Math.min(inSlot.getCount(), inSlot.getMaxStackSize()));
            int before = moving.getCount();
            moveItemStackTo(moving, inventoryStart, end, true);
            int moved = before - moving.getCount();
            if (moved <= 0) return ItemStack.EMPTY;
            slot.remove(moved);
            slot.setChanged();
            // A part holds one item, so there's nothing to repeat for (and on the client its slot only empties when
            // the server's update arrives, so repeating would keep "moving" it)
            return index < partsStart ? inSlot.copyWithCount(moved) : ItemStack.EMPTY;
        }

        // From the player's inventory
        ItemStack stack = slot.getItem().copy();
        for (int partIndex = partsStart; partIndex < inventoryStart && !stack.isEmpty(); partIndex++)
        {
            Slot part = slots.get(partIndex);
            if (part.isActive() && !part.hasItem() && part.mayPlace(stack)) part.setByPlayer(stack.split(1));
        }
        if (!stack.isEmpty()) stack.shrink(pedestal.insertItem(stack, false));
        if (stack.getCount() == slot.getItem().getCount()) return ItemStack.EMPTY;
        slot.setByPlayer(stack);
        return ItemStack.EMPTY;
    }

    // Open while the pedestal is there, the player is close, and its storage still has the size the menu was made
    // with (a tier config change can resize it; the slots would no longer line up)
    @Override
    public boolean stillValid(Player player)
    {
        return !pedestal.isRemoved()
                && player.level().getBlockEntity(pedestal.getBlockPos()) == pedestal
                && player.distanceToSqr(Vec3.atCenterOf(pedestal.getBlockPos())) <= 64
                && pedestal.getItemStorage().size() == storageSize;
    }
}
