package com.mowmaster.ironpedestals.Blocks.Pedestal;

import com.mowmaster.ironpedestals.IronPedestalReferences;
import com.mowmaster.mowlib.filter.FilterDirection;
import com.mowmaster.mowlib.filter.IFilterHolder;
import com.mowmaster.mowlib.filter.ITransportFilter;
import com.mowmaster.mowlib.ghost.GhostItem;
import com.mowmaster.mowlib.storage.StorageInteractions;
import com.mowmaster.mowlib.tint.CBTintData;
import com.mowmaster.mowlib.tint.ITintHolder;
import com.mowmaster.mowlib.transport.EnergyTransportType;
import com.mowmaster.mowlib.transport.ExperienceTransportType;
import com.mowmaster.mowlib.transport.ItemTransportType;
import com.mowmaster.mowlib.transport.TransportContents;
import com.mowmaster.mowlib.transport.TransportStorage;
import com.mowmaster.mowlib.transport.TransportStorageHost;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import com.mowmaster.mowlib.workcard.IWorkCard;
import com.mowmaster.mowlib.workcard.IWorkCardHolder;
import com.mowmaster.mowlib.workcard.WorkCardType;
import com.mowmaster.ironpedestals.Blocks.libBlockEntityRegistry;
import com.mowmaster.ironpedestals.Config;
import com.mowmaster.ironpedestals.Items.Augments.AugmentType;
import com.mowmaster.ironpedestals.Items.PedestalDataComponents;
import com.mowmaster.ironpedestals.Items.Storage.StoragePacketItem;
import com.mowmaster.ironpedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.ironpedestals.Transport.PedestalFluidStorage;
import com.mowmaster.ironpedestals.Transport.PedestalItemStorage;
import com.mowmaster.ironpedestals.Transport.PedestalTransportRules;
import com.mowmaster.ironpedestals.api.upgrade.IPedestalUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

// The pedestal's storage, links, filter, upgrade and colour.
// Storage: one TransportStorage per registered transport type (items, fluids, energy, experience, and any type
// another mod registers; the types are MowLib's), each exposed to pipes through its capability (PedestalCapabilities).
// How much a link moves and how big each storage is are Pedestals' own numbers: PedestalTransportRules.
// Links: every TICKS_TO_TRANSFER ticks it sends to the first linked pedestal that accepts anything, every type at once.
// Upgrades: one upgrade, one tool and one work card, all put in from the offhand. The upgrade is ticked every
// server tick (IPedestalUpgrade#tick) and keeps its per-pedestal state in getUpgradeData().
// Tier (PedestalTier, from the block): ticks between link transfers, link transfer rates, item stacks and how much
// each holds, fluid tanks, storage sizes and the link / work card range, all from Config.TIERS.
// Augments (AugmentType; switched on and off with the menu's buttons, no items): round robin (links take turns), particle & render (hides particles /
// the item / the upgrade) and collision (upgrades also act on touching entities).
// Not ported yet: redstone counts.
public class BasePedestalBlockEntity extends BlockEntity implements ITintHolder, TransportStorageHost, IFilterHolder, IWorkCardHolder
{
    // LinkedHashMap keeps the registry order, so types are always tried in the same order
    private final Map<TransportType<?>, TransportStorage<?>> storages = new LinkedHashMap<>();
    private final CBTintData tints = new CBTintData(this, 1);
    // Pedestals this one sends to
    private final List<BlockPos> links = new ArrayList<>();
    private ItemStack filter = ItemStack.EMPTY;
    private int transferTicks;
    // A storage that doesn't sync straight away changed since the last client update (see onStorageChanged)
    private boolean clientUpdatePending;

    private ItemStack upgrade = ItemStack.EMPTY;
    private ItemStack tool = ItemStack.EMPTY;
    private ItemStack workCard = ItemStack.EMPTY;
    // The augments that are on. EnumSet: a set of an enum's values (stored as bits, kept in the enum's order).
    private final EnumSet<AugmentType> augments = EnumSet.noneOf(AugmentType.class);
    // The particle & render augment's mode (what it hides, see AugmentType#hidesParticles...)
    private int diffuserMode;
    // With the round robin augment: the link to try first next time
    private int roundRobinNext;
    // Items the upgrade is missing, shown to players (synced to clients)
    private List<GhostItem> ghosts = List.of();
    // The upgrade's own saved state for this pedestal (1.20 kept it on the upgrade item's NBT)
    private CompoundTag upgradeData = new CompoundTag();
    // Worked out from the work card when first needed; cleared whenever the upgrade, tool or card changes.
    // "transient" in spirit: never saved.
    private @Nullable List<BlockPos> workPositions;
    private @Nullable PedestalFakePlayer fakePlayer;

    public BasePedestalBlockEntity(BlockPos pos, BlockState state)
    {
        super(libBlockEntityRegistry.PEDESTAL.get(), pos, state);
        for (TransportType<?> type : TransportTypes.REGISTRY)
        {
            storages.put(type, createStorage(type));
        }
    }

    // Items and fluids get Pedestals' own storages (bigger stack slots, several tanks: see PedestalItemStorage /
    // PedestalFluidStorage); every other type makes its own
    private TransportStorage<?> createStorage(TransportType<?> type)
    {
        if (type == TransportTypes.ITEMS.get()) return new PedestalItemStorage(this, TransportTypes.ITEMS.get());
        if (type == TransportTypes.FLUIDS.get()) return new PedestalFluidStorage(this, TransportTypes.FLUIDS.get());
        return type.createStorage(this);
    }

    // Saves, and sends the new contents to nearby clients
    public void update()
    {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    // ---- Tier ----

    // The tier comes from the block (each tier is its own block)
    public PedestalTier getTier()
    {
        return getBlockState().getBlock() instanceof BasePedestalBlock block ? block.getTier() : PedestalTier.BASE;
    }

    public Config.TierValues getTierValues()
    {
        return getTier().config();
    }

    // ---- Where it points ----

    public Direction getFacing()
    {
        BlockState state = getBlockState();
        return state.hasProperty(BasePedestalBlock.FACING) ? state.getValue(BasePedestalBlock.FACING) : Direction.UP;
    }

    // The block the pedestal stands on (1.20's getPosOfBlockBelow(level, pos, 1)): the inventory most upgrades use
    public BlockPos getPosBelow()
    {
        return worldPosition.relative(getFacing().getOpposite());
    }

    // The block the top of the pedestal points at
    public BlockPos getPosAbove()
    {
        return worldPosition.relative(getFacing());
    }

    // A redstone signal turns links and upgrades off
    public boolean isPowered()
    {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    // ---- Storage ----

    // The storage for a type. The cast is safe: the map only ever pairs a type with the storage it made.
    @SuppressWarnings("unchecked")
    public <R extends Resource> TransportStorage<R> getStorage(TransportType<R> type)
    {
        return (TransportStorage<R>) storages.get(type);
    }

    public TransportStorage<ItemResource> getItemStorage()
    {
        return getStorage(TransportTypes.ITEMS.get());
    }

    // How big a type's storage is (MowLib's storages ask this): the tier's value
    @Override
    public int getCapacity(TransportType<?> type)
    {
        return PedestalTransportRules.of(type).capacity(this);
    }

    // How much of one item a stack slot holds (PedestalItemStorage asks this): the tier's stack capacity for items
    // that normally stack to 64, the same share of it for items that stack to less (16 -> a quarter), and still 1
    // for unstackable items (upgrades count on one potion per slot)
    public int getItemSlotLimit(ItemResource resource)
    {
        int normal = Math.min(resource.getMaxStackSize(), Item.ABSOLUTE_MAX_STACK_SIZE);
        if (normal <= 1) return normal;
        long limit = (long) getTierValues().itemStackCapacity().getAsInt() * normal / Item.DEFAULT_MAX_STACK_SIZE;
        return (int) Math.max(1, Math.min(Integer.MAX_VALUE, limit));
    }

    // How many fluid tanks the pedestal has (PedestalFluidStorage asks this): the tier's value
    public int getFluidTankCount()
    {
        return getTierValues().fluidTanks().getAsInt();
    }

    // How much one link transfer moves: the tier's value
    public int getTransferRate(TransportType<?> type)
    {
        return PedestalTransportRules.of(type).transferRate(this);
    }

    // Items go to clients straight away (the renderer shows them). Fluids, energy, experience and add-on types change
    // far more often (every tick, for some), so they're only saved now and sent to clients at most twice a second
    // (serverTick), which keeps the menu's gauges and the sneak summary current.
    public void onStorageChanged(TransportType<?> type)
    {
        if (type.syncsToClient()) update();
        else
        {
            setChanged();
            clientUpdatePending = true;
        }
    }

    // Takes energy and experience for an upgrade's work, both or neither. false (and nothing taken) if there
    // isn't enough of either. simulate = only check.
    public boolean removeFuel(int energy, int experience, boolean simulate)
    {
        if (energy <= 0 && experience <= 0) return true;
        try (Transaction transaction = Transaction.openRoot())
        {
            if (energy > 0 && getStorage(TransportTypes.ENERGY.get()).extract(EnergyTransportType.ENERGY, energy, transaction) < energy) return false;
            if (experience > 0 && getStorage(TransportTypes.EXPERIENCE.get()).extract(ExperienceTransportType.EXPERIENCE, experience, transaction) < experience) return false;
            if (!simulate) transaction.commit();
            return true;
        }
    }

    // ---- Fluids (a tier may have several tanks, each holding its own fluid) ----

    // The fluid in the first tank whose fluid passes the test (EMPTY if none does)
    public FluidResource findFluid(Predicate<FluidResource> test)
    {
        TransportStorage<FluidResource> fluids = getStorage(TransportTypes.FLUIDS.get());
        for (int index = 0; index < fluids.size(); index++)
        {
            FluidResource stored = fluids.getResource(index);
            if (!stored.isEmpty() && test.test(stored)) return stored;
        }
        return FluidResource.EMPTY;
    }

    // How much of this fluid the pedestal holds, every tank together. Taking it out works across tanks too:
    // storage.extract(fluid, amount, transaction) without an index takes from every tank holding it.
    public int getFluidAmount(FluidResource fluid)
    {
        if (fluid.isEmpty()) return 0;
        TransportStorage<FluidResource> fluids = getStorage(TransportTypes.FLUIDS.get());
        long total = 0;
        for (int index = 0; index < fluids.size(); index++)
        {
            if (fluids.getResource(index).equals(fluid)) total += fluids.getAmountAsLong(index);
        }
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    // ---- Items (used by the block's clicks, the renderer and upgrades) ----

    public boolean hasItem()
    {
        return !getItemStorage().isEmpty();
    }

    // The stack shown on top: the first slot that has something
    public ItemStack getItemInPedestal()
    {
        TransportStorage<ItemResource> items = getItemStorage();
        for (int index = 0; index < items.size(); index++)
        {
            ItemStack stack = ItemUtil.getStack(items, index);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    // Copies of every stack in the pedestal
    public List<ItemStack> getItemStacks()
    {
        TransportStorage<ItemResource> items = getItemStorage();
        List<ItemStack> stacks = new ArrayList<>();
        for (int index = 0; index < items.size(); index++)
        {
            ItemStack stack = ItemUtil.getStack(items, index);
            if (!stack.isEmpty()) stacks.add(stack);
        }
        return stacks;
    }

    // Puts as much of the stack in as fits (and as the filter allows); returns how many went in
    public int insertItem(ItemStack stack, boolean simulate)
    {
        return StorageInteractions.insertStack(getItemStorage(), stack, simulate);
    }

    // true if the whole stack fits
    public boolean canInsertItem(ItemStack stack)
    {
        return stack.isEmpty() || insertItem(stack, true) == stack.getCount();
    }

    // Takes up to maxAmount (and at most one normal stack) from the last slot that has something, like 1.20
    public ItemStack removeItem(int maxAmount)
    {
        return StorageInteractions.extractStack(getItemStorage(), maxAmount);
    }

    // Takes up to amount of this exact item (any slots); returns how many were taken
    public int removeItem(ItemResource resource, int amount, boolean simulate)
    {
        if (resource.isEmpty() || amount <= 0) return 0;
        try (Transaction transaction = Transaction.openRoot())
        {
            int extracted = getItemStorage().extract(resource, amount, transaction);
            if (!simulate) transaction.commit();
            return extracted;
        }
    }

    public @Nullable Component describeItems()
    {
        return getItemStorage().describe();
    }

    // Everything but items, one line per type (fluid, energy, experience...), then the upgrade, tool and work card
    public List<Component> describeOtherContents()
    {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(IronPedestalReferences.MODID + ".pedestal.message_tier", getTier().tier(), getLinkingRange(), getTransferTicks())
                .withStyle(ChatFormatting.GOLD));
        storages.forEach((type, storage) -> {
            if (type == TransportTypes.ITEMS.get()) return; // "return" in a lambda just skips this entry
            Component line = storage.describe();
            if (line != null) lines.add(line);
        });
        if (hasUpgrade()) lines.add(Component.translatable(IronPedestalReferences.MODID + ".pedestal.message_upgrade").append(upgrade.getHoverName()).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (hasTool()) lines.add(Component.translatable(IronPedestalReferences.MODID + ".pedestal.message_tool").append(tool.getHoverName()).withStyle(ChatFormatting.GRAY));
        if (hasWorkCard()) lines.add(Component.translatable(IronPedestalReferences.MODID + ".pedestal.message_workcard").append(workCard.getHoverName()).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    // 0-15: the upgrade's own signal if it has one, otherwise how full the item slots are (same maths as a chest)
    public int getRedstoneSignal()
    {
        if (upgrade.getItem() instanceof IPedestalUpgrade pedestalUpgrade)
        {
            int signal = pedestalUpgrade.getComparatorSignal(this, upgrade);
            if (signal >= 0) return Math.min(signal, 15);
        }
        return ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(getItemStorage());
    }

    // Called by vanilla just before the block is removed (broken, exploded, replaced)
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state)
    {
        super.preRemoveSideEffects(pos, state);
        if (level == null) return;
        for (TransportType<?> type : storages.keySet()) dropStorage(level, pos, type);
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), filter);
        if (hasUpgrade()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), takeUpgrade());
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), tool);
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), workCard);
        if (level instanceof ServerLevel serverLevel) removeLinksTo(serverLevel, pos);
    }

    // A broken pedestal's links go: its own go with it, and every loaded pedestal close enough to link here stops
    // sending to it. Pedestals in unloaded chunks drop the link the next time they try to send (serverTick).
    // Looks through the chunks within the largest link range any tier has (block entities are kept per chunk).
    private static void removeLinksTo(ServerLevel level, BlockPos pos)
    {
        int range = 0;
        for (Config.TierValues tier : Config.TIERS) range = Math.max(range, tier.linkRange().getAsInt());
        int chunks = (range >> 4) + 1;
        int centreX = pos.getX() >> 4, centreZ = pos.getZ() >> 4;
        for (int chunkX = centreX - chunks; chunkX <= centreX + chunks; chunkX++)
        {
            for (int chunkZ = centreZ - chunks; chunkZ <= centreZ + chunks; chunkZ++)
            {
                if (!level.hasChunk(chunkX, chunkZ)) continue;
                for (BlockEntity blockEntity : level.getChunk(chunkX, chunkZ).getBlockEntities().values())
                {
                    if (blockEntity instanceof BasePedestalBlockEntity sender && !sender.worldPosition.equals(pos)) sender.removeLink(pos);
                }
            }
        }
    }

    // Items spill; the other types become a storage packet if Pedestals has one for them, otherwise whatever the
    // storage itself leaves behind (experience: orbs)
    private <R extends Resource> void dropStorage(Level level, BlockPos pos, TransportType<R> type)
    {
        TransportStorage<R> storage = getStorage(type);
        if (type != TransportTypes.ITEMS.get() && StoragePacketItem.forType(type) != null) StoragePacketItem.dropStorage(level, pos, type, storage);
        else storage.dropContents(level, pos);
    }

    // ---- Filter (IFilterHolder) ----

    @Override
    public boolean hasFilter()
    {
        return !filter.isEmpty();
    }

    @Override
    public ItemStack getFilter()
    {
        return filter;
    }

    // Takes one filter from the stack; false if there's already one or it isn't a filter
    @Override
    public boolean insertFilter(ItemStack stack)
    {
        if (hasFilter() || !(stack.getItem() instanceof ITransportFilter)) return false;
        filter = stack.split(1);
        filterChanged();
        return true;
    }

    @Override
    public ItemStack removeFilter()
    {
        ItemStack removed = filter;
        filter = ItemStack.EMPTY;
        filterChanged();
        return removed;
    }

    // Shows whitelist/blacklist on the block and resyncs
    private void filterChanged()
    {
        if (level != null)
        {
            int status = filterStatus();
            BlockState state = getBlockState();
            if (state.getValue(BasePedestalBlock.FILTER_STATUS) != status)
            {
                level.setBlock(worldPosition, state.setValue(BasePedestalBlock.FILTER_STATUS, status), Block.UPDATE_ALL);
            }
        }
        update();
    }

    // TransportStorageHost and IFilterHolder both have a default for these two, so Java makes the class pick one:
    // Interface.super.method(...) calls that interface's default (a filter only acts in the direction it's set to)
    @Override
    public <R extends Resource> boolean filterAllows(TransportType<R> type, R resource, FilterDirection direction)
    {
        return IFilterHolder.super.filterAllows(type, resource, direction);
    }

    // For upgrades that read the filter themselves (breakers, placers...): only NEUTRAL filters apply, as in 1.20
    public <R extends Resource> boolean machineFilterAllows(TransportType<R> type, R resource)
    {
        return filterAllows(type, resource, FilterDirection.NEUTRAL);
    }

    @Override
    public boolean machineFilterAllows(ItemStack stack)
    {
        return stack.isEmpty() || machineFilterAllows(TransportTypes.ITEMS.get(), ItemResource.of(stack));
    }

    @Override
    public <R extends Resource> int filterLimit(TransportType<R> type, R resource, int capacity)
    {
        return IFilterHolder.super.filterLimit(type, resource, capacity);
    }

    // ---- Upgrade ----

    public boolean hasUpgrade()
    {
        return !upgrade.isEmpty();
    }

    // The upgrade itself (not a copy): upgrades may change its components
    public ItemStack getUpgrade()
    {
        return upgrade;
    }

    // Takes one upgrade from the stack; false if there's already one or it isn't an upgrade
    public boolean insertUpgrade(ItemStack stack, @Nullable Player player)
    {
        if (hasUpgrade() || !(stack.getItem() instanceof IPedestalUpgrade pedestalUpgrade)) return false;
        upgrade = stack.split(1);
        resetUpgradeState();
        if (level instanceof ServerLevel serverLevel) pedestalUpgrade.onAdded(serverLevel, this, upgrade, player);
        update();
        return true;
    }

    public ItemStack removeUpgrade()
    {
        ItemStack removed = takeUpgrade();
        update();
        return removed;
    }

    private ItemStack takeUpgrade()
    {
        if (upgrade.getItem() instanceof IPedestalUpgrade pedestalUpgrade && level instanceof ServerLevel serverLevel)
        {
            pedestalUpgrade.onRemoved(serverLevel, this, upgrade);
        }
        ItemStack removed = upgrade;
        upgrade = ItemStack.EMPTY;
        resetUpgradeState();
        return removed;
    }

    // Per-pedestal state for the upgrade. Change it freely, then call upgradeDataChanged() so it's saved.
    public CompoundTag getUpgradeData()
    {
        return upgradeData;
    }

    public void upgradeDataChanged()
    {
        setChanged();
    }

    // The upgrade starts over: saved state, work positions and fake player are thrown away
    // (1.20's actionOnRemovedFromPedestal / resetCachedValidWorkCardPositions)
    public void resetUpgradeState()
    {
        // Lets the upgrade hand back anything it was keeping in its data (the XP anvil's collected inputs)
        if (upgrade.getItem() instanceof IPedestalUpgrade pedestalUpgrade && level instanceof ServerLevel serverLevel)
        {
            pedestalUpgrade.onUpgradeDataReset(serverLevel, this, upgrade);
        }
        upgradeData = new CompoundTag();
        workPositions = null;
        fakePlayer = null;
        ghosts = List.of();
        setChanged();
    }

    // ---- Tool ----

    // What counts as a tool (1.20's isAllowedTool): anything that mines or fights, bows, tridents and shears
    public static boolean isAllowedTool(ItemStack stack)
    {
        if (stack.isEmpty()) return false;
        if (stack.has(DataComponents.TOOL) || stack.has(DataComponents.WEAPON)) return true;
        if (stack.getItem() instanceof ProjectileWeaponItem || stack.getItem() instanceof TridentItem || stack.getItem() instanceof ShearsItem) return true;
        return stack.canPerformAction(ItemAbilities.SHEARS_HARVEST) || stack.canPerformAction(ItemAbilities.SWORD_SWEEP);
    }

    public boolean hasTool()
    {
        return !tool.isEmpty();
    }

    // The inserted tool itself (empty if none)
    public ItemStack getTool()
    {
        return tool;
    }

    // The tool upgrades use: the inserted one, or the upgrade's default tool
    public ItemStack getToolStack()
    {
        if (hasTool()) return tool;
        return upgrade.getItem() instanceof IPedestalUpgrade pedestalUpgrade ? pedestalUpgrade.getDefaultTool() : ItemStack.EMPTY;
    }

    public boolean insertTool(ItemStack stack)
    {
        if (hasTool() || !isAllowedTool(stack)) return false;
        tool = stack.split(1);
        resetUpgradeState();
        update();
        return true;
    }

    public ItemStack removeTool()
    {
        ItemStack removed = tool;
        tool = ItemStack.EMPTY;
        resetUpgradeState();
        update();
        return removed;
    }

    // Call after an upgrade wore the tool down (it may have broken: then the slot is empty)
    public void toolChanged()
    {
        if (tool.isEmpty()) tool = ItemStack.EMPTY;
        update();
    }

    // ---- Work card ----

    public boolean hasWorkCard()
    {
        return !workCard.isEmpty();
    }

    public ItemStack getWorkCard()
    {
        return workCard;
    }

    public boolean insertWorkCard(ItemStack stack)
    {
        if (hasWorkCard() || !(stack.getItem() instanceof IWorkCard)) return false;
        workCard = stack.split(1);
        resetUpgradeState();
        update();
        return true;
    }

    public ItemStack removeWorkCard()
    {
        ItemStack removed = workCard;
        workCard = ItemStack.EMPTY;
        resetUpgradeState();
        update();
        return removed;
    }

    // The work card's positions in range of the upgrade, if the card is a kind the upgrade uses. Worked out once and
    // kept until the upgrade or card changes (the card's positions can't change while it's in the pedestal), so
    // upgrades still need to check what's at each position every time.
    public List<BlockPos> getWorkPositions()
    {
        if (workPositions == null)
        {
            workPositions = List.of();
            if (upgrade.getItem() instanceof IPedestalUpgrade pedestalUpgrade && workCard.getItem() instanceof IWorkCard card)
            {
                WorkCardType wanted = pedestalUpgrade.getWorkCardType(upgrade);
                if (wanted != null && wanted.accepts(card.getWorkCardType()))
                {
                    workPositions = List.copyOf(card.getPositions(workCard, this, pedestalUpgrade.getWorkRange(this, upgrade)));
                }
            }
        }
        return workPositions;
    }

    // IWorkCardHolder: with require_sized_area on, an area card's box may be no bigger than the upgrade's area size
    @Override
    public int getMaxAreaSize()
    {
        if (!Config.REQUIRE_SIZED_AREA.getAsBoolean() || !(upgrade.getItem() instanceof ItemUpgradeBase base)) return -1;
        return base.getAreaSize(this, upgrade);
    }

    // An area work card's box (for finding entities), if the upgrade uses it and it's in range
    public Optional<AABB> getWorkArea()
    {
        if (upgrade.getItem() instanceof IPedestalUpgrade pedestalUpgrade && workCard.getItem() instanceof IWorkCard card)
        {
            WorkCardType wanted = pedestalUpgrade.getWorkCardType(upgrade);
            if (wanted != null && wanted.accepts(card.getWorkCardType()))
            {
                return card.getArea(workCard, this, pedestalUpgrade.getWorkRange(this, upgrade));
            }
        }
        return Optional.empty();
    }

    // ---- Ghost items ----

    public List<GhostItem> getGhosts()
    {
        return ghosts;
    }

    // Replaces the ghost items; clients are only sent an update when they actually changed
    public void setGhosts(List<GhostItem> newGhosts)
    {
        if (GhostItem.same(ghosts, newGhosts)) return;
        ghosts = List.copyOf(newGhosts);
        update();
    }

    // ---- Fake player ----

    // The player the upgrade acts as, standing on the pedestal. Made again when the upgrade changes (new owner).
    public PedestalFakePlayer getFakePlayer(ServerLevel serverLevel)
    {
        if (fakePlayer == null || fakePlayer.level() != serverLevel)
        {
            fakePlayer = new PedestalFakePlayer(serverLevel, upgrade.get(PedestalDataComponents.UPGRADE_OWNER.get()));
        }
        fakePlayer.standOn(worldPosition);
        return fakePlayer;
    }

    // ---- Particles (1.20 sent its own particle packets; vanilla's dust particle takes any colour) ----

    public void spawnParticles(ServerLevel serverLevel, double x, double y, double z, int rgb)
    {
        if (!canSpawnParticles()) return;
        serverLevel.sendParticles(new DustParticleOptions(rgb, 1.0F), x, y, z, 4, 0.1, 0.1, 0.1, 0);
    }

    public void spawnParticles(ServerLevel serverLevel, BlockPos pos, int rgb)
    {
        spawnParticles(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, rgb);
    }

    // ---- Links ----

    public List<BlockPos> getLinks()
    {
        return List.copyOf(links);
    }

    // The tier's link range
    public int getLinkingRange()
    {
        return getTierValues().linkRange().getAsInt();
    }

    // Within the range on every axis (a cube around the pedestal, as in 1.20)
    public boolean isInRange(BlockPos target)
    {
        return isInRange(target, getLinkingRange());
    }

    @Override
    public boolean isInRange(BlockPos target, int range)
    {
        return Math.abs(target.getX() - worldPosition.getX()) <= range
                && Math.abs(target.getY() - worldPosition.getY()) <= range
                && Math.abs(target.getZ() - worldPosition.getZ()) <= range;
    }

    // What toggleLink did, with the message the linking tools show. An enum can hold fields and methods like a class.
    public enum LinkResult
    {
        ADDED(true, null),
        REMOVED(true, ".tool_link_removed"),
        OUT_OF_RANGE(false, ".tool_link_distance"),
        SELF(false, ".tool_link_itsself"),
        FULL(false, ".tool_link_unsucess");

        private final boolean changed;
        private final @Nullable String messageKey;

        LinkResult(boolean changed, @Nullable String messageKey)
        {
            this.changed = changed;
            this.messageKey = messageKey;
        }

        public boolean changed()
        {
            return changed;
        }

        // receiving: the link was made from the receiving end (the linking tool's Receive From mode)
        public Component message(boolean receiving)
        {
            String key = messageKey != null ? messageKey : receiving ? ".tool_link_success_receive" : ".tool_link_success_send";
            return Component.translatable(IronPedestalReferences.MODID + key).withStyle(changed ? ChatFormatting.WHITE : ChatFormatting.RED);
        }
    }

    // Removes a link (the pedestal menu's X buttons); false if it wasn't linked
    public boolean removeLink(BlockPos target)
    {
        if (!links.remove(target)) return false;
        update();
        return true;
    }

    // Adds a link to target (if it's already there: ADDED, nothing changes). A pair of pedestals links one way only,
    // so a link back from target to this one is removed (that pedestal updates too).
    public LinkResult addLink(BlockPos target)
    {
        if (target.equals(worldPosition)) return LinkResult.SELF;
        if (links.contains(target)) return LinkResult.ADDED;
        if (!isInRange(target)) return LinkResult.OUT_OF_RANGE;
        if (links.size() >= Config.MAX_LINKS.getAsInt()) return LinkResult.FULL;
        links.add(target.immutable());
        update();
        if (level != null && level.getBlockEntity(target) instanceof BasePedestalBlockEntity other) other.removeLink(worldPosition);
        return LinkResult.ADDED;
    }

    // Adds a link to target, or removes it if it's already linked (the linking tool)
    public LinkResult toggleLink(BlockPos target)
    {
        if (target.equals(worldPosition)) return LinkResult.SELF;
        if (removeLink(target)) return LinkResult.REMOVED;
        return addLink(target);
    }

    // Turns the link between this pedestal and other round: this -> other becomes other -> this, and the other way.
    // Both pedestals update. If the new sender can't take the link (out of its range, no room), nothing changes.
    public LinkResult flipLink(BlockPos other)
    {
        if (level == null || !(level.getBlockEntity(other) instanceof BasePedestalBlockEntity otherPedestal)) return LinkResult.OUT_OF_RANGE;
        BasePedestalBlockEntity sender = links.contains(other) ? this : otherPedestal.links.contains(worldPosition) ? otherPedestal : null;
        if (sender == null) return LinkResult.OUT_OF_RANGE;
        BasePedestalBlockEntity receiver = sender == this ? otherPedestal : this;
        sender.removeLink(receiver.worldPosition);
        LinkResult result = receiver.addLink(sender.worldPosition);
        if (!result.changed())
        {
            sender.links.add(receiver.worldPosition.immutable()); // put it back as it was
            sender.update();
        }
        return result;
    }

    // ---- Pedestals loaded on the client ----
    // The client keeps a set of its loaded pedestals, so the link beams (ClientSide/PedestalBeams) and the menu's list
    // of pedestals sending here can find them. Only filled on the client; a set of objects, not positions, so each
    // dimension's pedestals are told apart by their level.

    public static final Set<BasePedestalBlockEntity> CLIENT_LOADED = ConcurrentHashMap.newKeySet();

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (level != null && level.isClientSide()) CLIENT_LOADED.add(this);
    }

    @Override
    public void onChunkUnloaded()
    {
        super.onChunkUnloaded();
        CLIENT_LOADED.remove(this);
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        CLIENT_LOADED.remove(this);
    }

    // The loaded pedestals (client) that send to this one
    public List<BasePedestalBlockEntity> getClientSenders()
    {
        List<BasePedestalBlockEntity> senders = new ArrayList<>();
        for (BasePedestalBlockEntity other : CLIENT_LOADED)
        {
            if (other != this && other.level == level && other.links.contains(worldPosition)) senders.add(other);
        }
        senders.sort(java.util.Comparator.comparingDouble(other -> other.worldPosition.distSqr(worldPosition)));
        return senders;
    }

    // ---- Ticking (server only, called every tick by the block's ticker) ----

    public void serverTick()
    {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Once a second: item stacks and fluid tanks follow the tier's config, so config changes apply on the fly
        if (serverLevel.getGameTime() % 20 == 0) resizeStorages(true);

        // Every half second at most: send clients the fluid / energy / experience changes (onStorageChanged)
        if (clientUpdatePending && serverLevel.getGameTime() % 10 == 0)
        {
            clientUpdatePending = false;
            update();
        }

        if (upgrade.getItem() instanceof IPedestalUpgrade pedestalUpgrade)
        {
            pedestalUpgrade.tick(serverLevel, this, upgrade);
        }

        if (links.isEmpty()) return;
        if (++transferTicks < getTransferTicks()) return;
        transferTicks = 0;
        if (isPowered()) return;

        // One receiver per transfer, like 1.20: the first that takes anything. With the round robin augment the
        // links take turns: the search starts after the last one that received.
        boolean roundRobin = hasAugment(AugmentType.ROUND_ROBIN);
        int count = links.size();
        int start = roundRobin ? Math.floorMod(roundRobinNext, count) : 0;
        List<BlockPos> gone = new ArrayList<>();
        for (int step = 0; step < count; step++)
        {
            int index = (start + step) % count;
            BlockPos target = links.get(index);
            if (!level.isLoaded(target)) continue;
            if (!(level.getBlockEntity(target) instanceof BasePedestalBlockEntity receiver))
            {
                gone.add(target); // the pedestal there is gone
                continue;
            }
            if (!isInRange(target) || level.hasNeighborSignal(target)) continue;
            if (sendTo(receiver))
            {
                if (roundRobin) roundRobinNext = index + 1;
                break;
            }
        }
        if (!gone.isEmpty())
        {
            links.removeAll(gone);
            setChanged();
        }
    }

    // Ticks between link transfers: the config's, less the tier's ticks_reduced (at least 1)
    public int getTransferTicks()
    {
        return Math.max(1, Config.TICKS_TO_TRANSFER.getAsInt() - getTierValues().ticksReduced().getAsInt());
    }

    // ---- Augments ----

    public boolean hasAugment(AugmentType type)
    {
        return augments.contains(type);
    }

    // Switches an augment on or off (the menu's buttons, PedestalMenu#clickMenuButton)
    public void toggleAugment(AugmentType type)
    {
        if (!augments.remove(type)) augments.add(type);
        update(); // clients need the augments too (the particle & render augment changes what's drawn)
    }

    public int getDiffuserMode()
    {
        return diffuserMode;
    }

    // The particle & render augment's next mode
    public void cycleDiffuserMode()
    {
        diffuserMode = (diffuserMode + 1) % AugmentType.DIFFUSER_MODES;
        update();
    }

    // Item stacks and fluid tanks follow the tier (the storages resize to what getCapacity / getFluidTankCount say).
    // shrink = false only grows them (when loading, where nothing can be dropped yet). Stacks that no longer fit drop
    // above the pedestal; fluid from tanks that no longer exist drops as a fluid packet.
    private void resizeStorages(boolean shrink)
    {
        BlockPos above = getPosAbove();
        if (getItemStorage() instanceof ItemTransportType.Storage itemStorage)
        {
            List<ItemStack> overflow = itemStorage.resize(shrink);
            if (level != null) overflow.forEach(stack -> Containers.dropItemStack(level, above.getX(), above.getY(), above.getZ(), stack));
        }
        if (getStorage(TransportTypes.FLUIDS.get()) instanceof PedestalFluidStorage fluidStorage)
        {
            List<FluidStack> overflow = fluidStorage.resize(shrink);
            if (level != null && !overflow.isEmpty())
            {
                // ResourceStack: NeoForge's (resource, amount) pair, what a packet's contents are made of
                List<ResourceStack<FluidResource>> contents = overflow.stream()
                        .map(stack -> new ResourceStack<>(FluidResource.of(stack), stack.getAmount())).toList();
                ItemStack packet = StoragePacketItem.create(new TransportContents<>(TransportTypes.FLUIDS.get(), contents));
                Containers.dropItemStack(level, above.getX(), above.getY(), above.getZ(), packet);
            }
        }
    }

    // The particle & render augment's choices (all true without one)
    public boolean canSpawnParticles()
    {
        return !hasAugment(AugmentType.DIFFUSER) || !AugmentType.hidesParticles(diffuserMode);
    }

    public boolean rendersItem()
    {
        return !hasAugment(AugmentType.DIFFUSER) || !AugmentType.hidesItem(diffuserMode);
    }

    public boolean rendersUpgrade()
    {
        return !hasAugment(AugmentType.DIFFUSER) || !AugmentType.hidesUpgrade(diffuserMode);
    }

    private boolean sendTo(BasePedestalBlockEntity receiver)
    {
        boolean sent = false;
        for (TransportType<?> type : storages.keySet())
        {
            // "sent |= x" is "sent = sent | x": every type gets its turn even after one has sent something
            sent |= send(type, receiver);
        }
        return sent;
    }

    // A generic helper so the storages' types line up: Java can't move between two TransportStorage<?> directly,
    // but inside here both are TransportStorage<R> for the same R
    private <R extends Resource> boolean send(TransportType<R> type, BasePedestalBlockEntity receiver)
    {
        TransportStorage<R> from = getStorage(type);
        TransportStorage<R> to = receiver.getStorage(type);
        if (from == null || to == null || from.isEmpty()) return false;
        try (Transaction transaction = Transaction.openRoot())
        {
            int moved = type.transfer(from, to, getTransferRate(type), transaction);
            transaction.commit();
            return moved > 0;
        }
    }

    // ---- Colour (ITintHolder) ----

    @Override public int getColor(int region) { return tints.getColor(region); }
    @Override public void setColor(int region, int color) { tints.setColor(region, color); }
    @Override public int getTint(int tintIndex) { return tints.getTint(tintIndex); }

    // ---- Saving ----
    // Each type saves under storage/<type id>, e.g. storage/mowlib:fluids, so add-on types can't clash. Pedestals saved
    // before the move to MowLib used storage/ironpedestals:<path> for the built-in types; loading falls back to that.

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);
        ValueOutput storageOutput = output.child("storage");
        storages.forEach((type, storage) -> storage.serialize(storageOutput.child(type.getId().toString())));
        if (!links.isEmpty()) output.store("links", BlockPos.CODEC.listOf(), links);
        if (!filter.isEmpty()) output.store("filter", ItemStack.CODEC, filter);
        if (!upgrade.isEmpty()) output.store("upgrade", ItemStack.CODEC, upgrade);
        if (!tool.isEmpty()) output.store("tool", ItemStack.CODEC, tool);
        if (!workCard.isEmpty()) output.store("work_card", ItemStack.CODEC, workCard);
        augments.forEach(type -> output.putBoolean("augment_" + type.key(), true));
        if (diffuserMode > 0) output.putInt("diffuser_mode", diffuserMode);
        if (roundRobinNext > 0) output.putInt("round_robin_next", roundRobinNext);
        if (!ghosts.isEmpty()) output.store("ghosts", GhostItem.CODEC.listOf(), ghosts);
        if (!upgradeData.isEmpty()) output.store("upgrade_data", CompoundTag.CODEC, upgradeData);
        tints.save(output);
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);
        augments.clear();
        for (AugmentType type : AugmentType.values())
        {
            String key = "augment_" + type.key();
            // Pedestals saved while augments were items kept the item under the same key: having one means it's on
            if (input.getBooleanOr(key, false) || input.read(key, CompoundTag.CODEC).isPresent()) augments.add(type);
        }
        diffuserMode = Math.clamp(input.getIntOr("diffuser_mode", 0), 0, AugmentType.DIFFUSER_MODES - 1);
        roundRobinNext = input.getIntOr("round_robin_next", 0);
        ValueInput storageInput = input.childOrEmpty("storage");
        storages.forEach((type, storage) -> storage.deserialize(storageFor(storageInput, type)));
        // A saved storage has the size it was saved with; make room for the tier's stacks and tanks
        resizeStorages(false);
        links.clear();
        input.read("links", BlockPos.CODEC.listOf()).ifPresent(links::addAll);
        filter = input.read("filter", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        upgrade = input.read("upgrade", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        tool = input.read("tool", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        workCard = input.read("work_card", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        ghosts = input.read("ghosts", GhostItem.CODEC.listOf()).orElse(List.of());
        upgradeData = input.read("upgrade_data", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        workPositions = null;
        tints.load(input);
    }

    // A type's saved storage: under its id, or for MowLib's built-in types under the id they had in Pedestals.
    // ValueInput#child is an Optional (empty when the key isn't there); Optional#or tries the next one.
    private static ValueInput storageFor(ValueInput storageInput, TransportType<?> type)
    {
        String id = type.getId().toString();
        return storageInput.child(id)
                .or(() -> storageInput.child(Identifier.fromNamespaceAndPath(IronPedestalReferences.MODID, type.getId().getPath()).toString()))
                .orElseGet(() -> storageInput.childOrEmpty(id));
    }

    // ---- Syncing to clients ----

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return saveCustomOnly(registries); // runs saveAdditional into a tag
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this); // uses getUpdateTag
    }

    // ---- Item <-> block (data components) ----

    // Placed from an item with a dyed_color: take that colour
    @Override
    protected void applyImplicitComponents(DataComponentGetter components)
    {
        super.applyImplicitComponents(components);
        DyedItemColor dyed = components.get(DataComponents.DYED_COLOR);
        if (dyed != null) tints.setColor(0, dyed.rgb());
    }

    // Block turned into an item (the loot table's copy_components, creative pick-block): keep the colour
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components)
    {
        super.collectImplicitComponents(components);
        if (isDyed(0)) components.set(DataComponents.DYED_COLOR, new DyedItemColor(getColor(0)));
    }
}
