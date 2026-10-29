package com.mowmaster.pedestals.Items.Upgrades;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Augments.AugmentType;
import com.mowmaster.pedestals.Items.PedestalDataComponents;
import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.Transport.ExperienceTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import com.mowmaster.pedestals.api.upgrade.IPedestalUpgrade;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

// Shared upgrade behaviour (1.20's ItemUpgradeBase). Subclasses do their work in upgradeAction, which runs every
// (TICKS_TO_TRANSFER - speed modifier) ticks while the pedestal isn't powered.
//
// Modifiers: pass the names the upgrade accepts to the constructor (UpgradeModifiers.SPEED, ANY_CAPACITY...);
// the modification upgrade can only add those.
// Transport modes: upgrades that move every transport type (import, export, magnet...) override
// usesTransportModes. With the upgrade in the OFFHAND, right-click toggles the selected type on/off and
// sneak + right-click selects the next type (the item model shows which).
// Fuel and tools: override getFuelCost / getToolRules with the upgrade's config values.
public abstract class ItemUpgradeBase extends Item implements IPedestalUpgrade
{
    private static final String MODID = PedestalReferences.MODID;

    private final Set<String> modifiers;

    protected ItemUpgradeBase(Properties properties, String... modifiers)
    {
        super(properties);
        this.modifiers = Set.of(modifiers);
    }

    // ---- Ticking ----

    @Override
    public void tick(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        if (pedestal.isPowered()) return;
        if (level.getGameTime() % getOperationTicks(upgrade) != 0) return;
        upgradeAction(level, pedestal, upgrade);
        if (pedestal.hasAugment(AugmentType.COLLIDE))
        {
            List<Entity> touching = level.getEntities((Entity) null, new AABB(pedestal.getBlockPos()), Entity::isAlive);
            if (!touching.isEmpty()) onCollide(level, pedestal, upgrade, touching);
        }
    }

    // The upgrade's work, once per operation
    protected abstract void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade);

    // Ticks between operations: the pedestal's transfer speed, less the speed modifier (at least 1)
    public int getOperationTicks(ItemStack upgrade)
    {
        return Math.max(1, Config.TICKS_TO_TRANSFER.getAsInt() - getModifier(upgrade, UpgradeModifiers.SPEED));
    }

    // ---- Added / removed ----

    @Override
    public void onAdded(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade, @Nullable Player player)
    {
        if (player != null) upgrade.set(PedestalDataComponents.UPGRADE_OWNER.get(), new UpgradeOwner(player.getUUID(), player.getName().getString()));
    }

    @Override
    public void onRemoved(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        upgrade.remove(PedestalDataComponents.UPGRADE_OWNER.get());
    }

    // ---- Modifiers ----

    public static UpgradeModifiers getModifiers(ItemStack upgrade)
    {
        return upgrade.getOrDefault(PedestalDataComponents.UPGRADE_MODIFIERS.get(), UpgradeModifiers.EMPTY);
    }

    // Whether this upgrade takes this modifier (the modification recipes check this)
    public boolean canModify(ItemStack upgrade, String modifier)
    {
        return modifiers.contains(modifier) || (modifiers.contains(UpgradeModifiers.ANY_CAPACITY) && capacityType(modifier) != null);
    }

    // The modifier's value, 0 if the upgrade doesn't take it
    public int getModifier(ItemStack upgrade, String modifier)
    {
        return canModify(upgrade, modifier) ? getModifiers(upgrade).get(modifier) : 0;
    }

    public boolean hasModifier(ItemStack upgrade, String modifier)
    {
        return getModifier(upgrade, modifier) >= 1;
    }

    // An upgrade's rate for a type: the base rate plus that type's capacity modifier
    public int getRate(ItemStack upgrade, PedestalTransportType<?> type, int baseRate)
    {
        return type.getUpgradeRate(baseRate, getModifier(upgrade, type.getCapacityModifier()));
    }

    // The type a capacity modifier belongs to, or null if it isn't one
    private static @Nullable PedestalTransportType<?> capacityType(String modifier)
    {
        for (PedestalTransportType<?> type : PedestalTransportTypes.REGISTRY)
        {
            if (type.getCapacityModifier().equals(modifier)) return type;
        }
        return null;
    }

    // Every modifier this upgrade takes, with ANY_CAPACITY spelled out as each type's capacity modifier
    public List<String> getAllowedModifiers(ItemStack upgrade)
    {
        List<String> allowed = new ArrayList<>();
        for (String modifier : modifiers)
        {
            if (!modifier.equals(UpgradeModifiers.ANY_CAPACITY)) allowed.add(modifier);
        }
        if (modifiers.contains(UpgradeModifiers.ANY_CAPACITY))
        {
            for (PedestalTransportType<?> type : PedestalTransportTypes.REGISTRY) allowed.add(type.getCapacityModifier());
        }
        allowed.removeIf(modifier -> !canModify(upgrade, modifier));
        return allowed;
    }

    // ---- Work cards ----

    // How far work card positions may be: the base selection range plus the range modifier (0 without a card type)
    @Override
    public int getWorkRange(ItemStack upgrade)
    {
        return getWorkCardType(upgrade) == null ? 0 : Config.SELECTION_RANGE.getAsInt() + getModifier(upgrade, UpgradeModifiers.RANGE);
    }

    // The largest area card the upgrade works with when REQUIRE_SIZED_AREA is on (2 = a 3x3x3 box)
    public int getAreaSize(ItemStack upgrade)
    {
        return 2 + getModifier(upgrade, UpgradeModifiers.AREA);
    }

    // ---- Fuel ----

    // The upgrade's energy/experience costs (from Config), or null if it never uses fuel
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return null;
    }

    // Takes the fuel for one operation at distance blocks from the pedestal (simulate = only check)
    protected boolean useFuel(BasePedestalBlockEntity pedestal, int distance, boolean simulate)
    {
        Config.FuelCost cost = getFuelCost();
        return cost == null || pedestal.removeFuel(cost.energyCost(distance), cost.experienceCost(distance), simulate);
    }

    // Blocks between two positions, rounded (1.20's getDistanceBetweenPoints)
    protected static int distance(BlockPos from, BlockPos to)
    {
        return (int) Math.round(Math.sqrt(from.distSqr(to)));
    }

    // ---- Tools ----

    // Whether the upgrade needs/wears down tools (from Config), or null if it doesn't use tools
    protected @Nullable Config.ToolRules getToolRules()
    {
        return null;
    }

    // false if the config wants a tool and none is in, or the inserted one would break on its next use
    // (1.20 never broke tools: they stop at 1 durability). Shows white particles when it can't work.
    protected boolean canUseTool(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        Config.ToolRules rules = getToolRules();
        if (rules == null) return true;
        boolean ok = !rules.require().getAsBoolean() || pedestal.hasTool();
        if (ok && wearsTool(pedestal))
        {
            ItemStack tool = pedestal.getTool();
            ok = !tool.isDamageableItem() || tool.getMaxDamage() - tool.getDamageValue() > 1;
        }
        if (!ok) pedestal.spawnParticles(level, pedestal.getBlockPos().getX() + 0.5, pedestal.getBlockPos().getY() + 1.0, pedestal.getBlockPos().getZ() + 0.5, 0xFFFFFF);
        return ok;
    }

    protected boolean wearsTool(BasePedestalBlockEntity pedestal)
    {
        Config.ToolRules rules = getToolRules();
        return rules != null && rules.damage().getAsBoolean() && pedestal.hasTool();
    }

    // The pedestal's fake player, holding the tool: the pedestal's own stack if the config wears tools down
    // (vanilla then damages it as the player uses it), otherwise a copy that's thrown away.
    // Call finishWithTool after using it.
    protected PedestalFakePlayer playerWithTool(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        PedestalFakePlayer player = pedestal.getFakePlayer(level);
        player.holdTool(wearsTool(pedestal) ? pedestal.getTool() : pedestal.getToolStack().copy());
        return player;
    }

    protected void finishWithTool(BasePedestalBlockEntity pedestal, PedestalFakePlayer player)
    {
        // "==" on purpose: was it holding the pedestal's own stack (which may now be worn or broken)?
        boolean usedOwnTool = player.getMainHandItem() == pedestal.getTool();
        player.holdTool(ItemStack.EMPTY);
        if (usedOwnTool) pedestal.toolChanged();
    }

    // ---- Transport modes ----

    // true for upgrades that work on every transport type and let the player turn types on/off
    public boolean usesTransportModes()
    {
        return false;
    }

    public static UpgradeMode getMode(ItemStack upgrade)
    {
        UpgradeMode mode = upgrade.get(PedestalDataComponents.UPGRADE_MODE.get());
        return mode != null ? mode : new UpgradeMode(PedestalTransportTypes.ITEMS.getId(), List.of());
    }

    private static void setMode(ItemStack upgrade, UpgradeMode mode)
    {
        upgrade.set(PedestalDataComponents.UPGRADE_MODE.get(), mode);
        upgrade.set(PedestalDataComponents.UPGRADE_MODE_DISPLAY.get(), mode.display());
    }

    // Whether the upgrade works on this type (always true for upgrades without transport modes)
    public boolean canTransport(ItemStack upgrade, PedestalTransportType<?> type)
    {
        return !usesTransportModes() || getMode(upgrade).isEnabled(type.getId());
    }

    // The types this upgrade works on, in registry order
    public List<PedestalTransportType<?>> getTransportTypes(ItemStack upgrade)
    {
        List<PedestalTransportType<?>> types = new ArrayList<>();
        for (PedestalTransportType<?> type : PedestalTransportTypes.REGISTRY)
        {
            if (canTransport(upgrade, type)) types.add(type);
        }
        return types;
    }

    // In the OFFHAND: right-click toggles the selected type, sneak + right-click selects the next one
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        if (hand != InteractionHand.OFF_HAND || !usesTransportModes()) return super.use(level, player, hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack upgrade = player.getItemInHand(hand);
        UpgradeMode mode = getMode(upgrade);
        if (player.isShiftKeyDown())
        {
            mode = mode.select(nextType(mode.selected()).getId());
            setMode(upgrade, mode);
            player.sendOverlayMessage(Component.translatable(MODID + ".mode_changed").append(typeName(mode.selected())));
        }
        else
        {
            mode = mode.toggleSelected();
            setMode(upgrade, mode);
            boolean enabled = mode.isEnabled(mode.selected());
            player.sendOverlayMessage(Component.empty().append(typeName(mode.selected())).append(" ")
                    .append(Component.translatable(MODID + (enabled ? ".transport_mode_changed_true" : ".transport_mode_changed_false")))
                    .withStyle(enabled ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY));
        }
        return InteractionResult.SUCCESS;
    }

    // The type after this one in the registry, wrapping round to the first
    private static PedestalTransportType<?> nextType(net.minecraft.resources.Identifier current)
    {
        List<PedestalTransportType<?>> types = PedestalTransportTypes.REGISTRY.stream().toList();
        for (int index = 0; index < types.size(); index++)
        {
            if (types.get(index).getId().equals(current)) return types.get((index + 1) % types.size());
        }
        return types.getFirst();
    }

    private static Component typeName(net.minecraft.resources.Identifier id)
    {
        PedestalTransportType<?> type = PedestalTransportTypes.REGISTRY.getValue(id);
        return type != null ? type.getName() : Component.literal(id.toString());
    }

    // ---- Helpers for subclasses ----

    // The item handler of the block the pedestal stands on, seen from the pedestal's side.
    // null if there's none, or it's another pedestal (1.20 never pulled from pedestals below).
    protected static @Nullable ResourceHandler<ItemResource> getInventoryBelow(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        BlockPos below = pedestal.getPosBelow();
        if (level.getBlockEntity(below) instanceof BasePedestalBlockEntity) return null;
        return level.getCapability(Capabilities.Item.BLOCK, below, pedestal.getFacing());
    }

    // A type's handler at pos: the block's, or (with entities = true, for the entity container modifier) the first
    // entity's in that block space, like a chest minecart
    protected static <R extends Resource> @Nullable ResourceHandler<R> findHandler(ServerLevel level, BlockPos pos, @Nullable Direction side,
                                                                                    PedestalTransportType<R> type, boolean entities)
    {
        ResourceHandler<R> handler = type.getBlockHandler(level, pos, side);
        if (handler != null || !entities) return handler;
        for (Entity entity : level.getEntities((Entity) null, new AABB(pos), Entity::isAlive))
        {
            handler = type.getEntityHandler(entity, side);
            if (handler != null) return handler;
        }
        return null;
    }

    // A position counter kept in the pedestal's upgrade data: returns the current index (0 to size-1), and moves
    // it on by one for next time (1.20's _numposition)
    protected static int nextIndex(BasePedestalBlockEntity pedestal, String key, int size)
    {
        if (size <= 0) return 0;
        int current = pedestal.getUpgradeData().getIntOr(key, 0);
        if (current >= size || current < 0) current = 0;
        pedestal.getUpgradeData().putInt(key, (current + 1) % size);
        pedestal.upgradeDataChanged();
        return current;
    }

    // ---- Collision augment ----

    // For upgrades that pull things in (import, magnet), with the collision augment: for every enabled type, from
    // what touches the pedestal. Things lying on it: whatever the type takes from them (items, xp orbs, a bucket's
    // fluid...). Players standing on it (not sneaking): the first inventory stack that fits, experience, and other
    // types out of the items they carry (buckets, batteries).
    protected void pullFromTouching(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade, List<Entity> touching)
    {
        for (Entity entity : touching)
        {
            for (PedestalTransportType<?> type : getTransportTypes(upgrade))
            {
                if (!entity.isAlive()) break;
                int rate = type.getTransferRate(pedestal);
                if (entity instanceof Player player)
                {
                    if (!player.isShiftKeyDown() && !player.isSpectator()) pullFromPlayer(pedestal, type, player, rate);
                }
                else if (!(entity instanceof LivingEntity)) absorbFrom(level, pedestal, type, entity, rate);            }
        }
    }

    private static <R extends Resource> void absorbFrom(ServerLevel level, BasePedestalBlockEntity pedestal, PedestalTransportType<R> type, Entity entity, int rate)
    {
        type.absorbEntity(level, entity, pedestal.getStorage(type), rate);
    }

    private static <R extends Resource> void pullFromPlayer(BasePedestalBlockEntity pedestal, PedestalTransportType<R> type, Player player, int rate)
    {
        Inventory inventory = player.getInventory();
        if (type == PedestalTransportTypes.ITEMS.get())
        {
            // The main inventory only (slots 0-35), never armour or the offhand
            for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++)
            {
                ItemStack stack = inventory.getItem(slot);
                if (stack.isEmpty()) continue;
                int inserted = pedestal.insertItem(stack.copyWithCount(Math.min(stack.getCount(), rate)), false);
                if (inserted <= 0) continue;
                stack.shrink(inserted);
                inventory.setChanged();
                return;
            }
            return;
        }
        if (type == PedestalTransportTypes.EXPERIENCE.get())
        {
            int amount = Math.min(rate, player.totalExperience);
            if (amount <= 0) return;
            int inserted;
            try (Transaction transaction = Transaction.openRoot())
            {
                inserted = pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get()).insert(ExperienceTransportType.EXPERIENCE, amount, transaction);
                if (inserted <= 0) return;
                transaction.commit();
            }
            // giveExperiencePoints only steps down one level per call, so it's taken a point at a time
            for (int point = 0; point < inserted; point++) player.giveExperiencePoints(-1);
            return;
        }
        PedestalStorage<R> storage = pedestal.getStorage(type);
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++)
        {
            if (inventory.getItem(slot).isEmpty()) continue;
            ResourceHandler<R> handler = type.getItemHandler(ItemAccess.forPlayerSlot(player, slot));
            if (handler == null) continue;
            try (Transaction transaction = Transaction.openRoot())
            {
                if (type.transfer(handler, storage, rate, transaction) <= 0) continue;
                transaction.commit();
                return;
            }
        }
    }

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);

        if (usesTransportModes())
        {
            UpgradeMode mode = getMode(stack);
            tooltip.accept(Component.translatable(MODID + ".upgrade_tooltip_mode").append(typeName(mode.selected())).withStyle(ChatFormatting.GOLD));
            for (PedestalTransportType<?> type : PedestalTransportTypes.REGISTRY)
            {
                boolean enabled = mode.isEnabled(type.getId());
                tooltip.accept(Component.literal(" ").append(type.getName()).append(": ")
                        .append(Component.translatable(MODID + (enabled ? ".upgrade_tooltip_type_enabled" : ".upgrade_tooltip_type_disabled"))
                                .withStyle(enabled ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY)));
            }
        }

        WorkCardType cardType = getWorkCardType(stack);
        if (cardType != null)
        {
            tooltip.accept(Component.translatable(MODID + ".upgrade_tooltip_workcard")
                    .append(Component.translatable(MODID + ".workcard_type." + cardType.name().toLowerCase())).withStyle(ChatFormatting.GRAY));
        }

        Config.FuelCost cost = getFuelCost();
        if (cost != null)
        {
            int energy = cost.energyCost(0);
            int experience = cost.experienceCost(0);
            if (energy > 0) tooltip.accept(Component.translatable(MODID + ".upgrade_tooltip_energy_cost", energy).withStyle(ChatFormatting.RED));
            if (experience > 0) tooltip.accept(Component.translatable(MODID + ".upgrade_tooltip_xp_cost", experience).withStyle(ChatFormatting.GREEN));
        }

        Config.ToolRules rules = getToolRules();
        if (rules != null && rules.require().getAsBoolean())
        {
            tooltip.accept(Component.translatable(MODID + ".upgrade_tooltip_tool_required").withStyle(ChatFormatting.GRAY));
        }

        UpgradeModifiers values = getModifiers(stack);
        for (String modifier : getAllowedModifiers(stack))
        {
            int value = values.get(modifier);
            MutableComponent line = modifierName(modifier).copy().append(": ");
            if (value > 0) line.append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.WHITE));
            else line.append(Component.translatable(MODID + ".upgrade_tooltip_modifier_allowed").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(line.withStyle(ChatFormatting.AQUA));
        }
    }

    // "Speed", "Range", "Fluids Capacity"... (pedestals.modifier.<name>; capacity modifiers use the type's name)
    public static Component modifierName(String modifier)
    {
        PedestalTransportType<?> type = capacityType(modifier);
        if (type != null) return Component.translatable(MODID + ".modifier.capacity", type.getName());
        return Component.translatableWithFallback(MODID + ".modifier." + modifier, modifier);
    }
}
