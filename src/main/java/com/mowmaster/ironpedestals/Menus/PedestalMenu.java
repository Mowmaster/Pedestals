package com.mowmaster.ironpedestals.Menus;

import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.Items.Augments.AugmentType;
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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import com.mowmaster.ironpedestals.Transport.PedestalFluidStorage;
import com.mowmaster.mowlib.storage.StorageInteractions;
import com.mowmaster.mowlib.transport.FluidTransportType;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;

import java.util.ArrayList;
import java.util.List;

// The pedestal's menu (empty-hand right-click): its item stacks, the upgrade / tool / filter / work card slots, and
// the player's inventory (the three augments are buttons the screen draws at AUGMENTS_X, see clickMenuButton). Server and client each build one: the server's from the block
// entity (PedestalMenu(id, inventory, pedestal)), the client's from the position the server sends (fromNetwork),
// reading the client's copy of the same block entity. Vanilla then keeps the slots in step.
//
// Slot order (quickMoveStack relies on it): the pedestal's stacks, the 4 parts (PedestalPart order), then the
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

        // Upgrade, tool, filter, then the work card (shown only while the upgrade uses one, or one is in)
        PedestalPartsContainer parts = new PedestalPartsContainer(pedestal, inventory.player);
        addSlot(new PedestalSlots.Part(parts, PedestalPart.UPGRADE, ITEMS_X, PARTS_Y, () -> true));
        addSlot(new PedestalSlots.Part(parts, PedestalPart.TOOL, ITEMS_X + 18, PARTS_Y, () -> true));
        addSlot(new PedestalSlots.Part(parts, PedestalPart.WORK_CARD, ITEMS_X + 54, PARTS_Y, this::showsWorkCard));
        addSlot(new PedestalSlots.Part(parts, PedestalPart.FILTER, ITEMS_X + 36, PARTS_Y, () -> true));

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

    // ---- The augment buttons ----

    // Button ids sent by the screen (vanilla's menu button message, MultiPlayerGameMode#handleInventoryButtonClick):
    // 0-2 switch that augment (AugmentType order) on or off, CYCLE_DIFFUSER picks the particle & render augment's
    // next mode. This runs on the server, which checks the id.
    public static final int CYCLE_DIFFUSER = 10;
    // FLUID_TANK + n: the cursor's bucket / tank used on fluid tank n (fill it or empty it)
    public static final int FLUID_TANK = 100;

    @Override
    public boolean clickMenuButton(Player player, int id)
    {
        AugmentType[] types = AugmentType.values();
        if (id >= 0 && id < types.length)
        {
            pedestal.toggleAugment(types[id]);
            return true;
        }
        if (id == CYCLE_DIFFUSER)
        {
            pedestal.cycleDiffuserMode();
            return true;
        }
        if (id >= FLUID_TANK && id < FLUID_TANK + 64) return useFluidContainer(player, id - FLUID_TANK);
        return false;
    }

    // The item on the cursor (a bucket, a tank...) used on one fluid tank: a full one empties into it, otherwise the
    // tank's fluid fills it. MowLib's cursorAccess lets the item change in place (bucket -> water bucket), and the
    // menu sends the new cursor item to the client. Server only.
    private boolean useFluidContainer(Player player, int tank)
    {
        if (getCarried().isEmpty() || !(pedestal.getStorage(TransportTypes.FLUIDS.get()) instanceof PedestalFluidStorage tanks)) return false;
        if (tank < 0 || tank >= tanks.size()) return false;
        FluidTransportType fluids = TransportTypes.FLUIDS.get();
        ResourceHandler<FluidResource> item = fluids.getItemHandler(StorageInteractions.cursorAccess(player));
        if (item == null) return false;
        // Just this tank (NeoForge's RangedResourceHandler view), so the fluid goes in or comes out of the one clicked
        ResourceHandler<FluidResource> tankHandler = tanks.tank(tank);
        if (StorageInteractions.move(fluids, item, tankHandler, Integer.MAX_VALUE) > 0) return true;
        return StorageInteractions.move(fluids, tankHandler, item, Integer.MAX_VALUE) > 0;
    }

    // ---- Gauges for the other storages ----

    // One gauge: a type's storage, and which of its slots (fluids: one gauge per tank; amount types like energy and
    // experience have one). TransportType<?>: the gauge works for any type, the screen reads it generically.
    public record Gauge(TransportType<?> type, int index) {}

    // Every storage but items, in registry order: each fluid tank, energy, experience, then add-on types
    public List<Gauge> gauges()
    {
        List<Gauge> gauges = new ArrayList<>();
        for (TransportType<?> type : TransportTypes.REGISTRY)
        {
            if (type == TransportTypes.ITEMS.get()) continue;
            int slots = pedestal.getStorage(type).size();
            for (int index = 0; index < slots; index++) gauges.add(new Gauge(type, index));
        }
        return gauges;
    }

    // ---- Shift-click ----

    // From the player: into the matching empty part slots first (upgrade, tool, filter, work card), then
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
