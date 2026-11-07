package com.mowmaster.ironpedestals.Items.Upgrades;

import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import com.mowmaster.mowlib.util.MowLibOwner;
import com.mowmaster.mowlib.workcard.WorkCardType;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.ironpedestals.Config;
import com.mowmaster.ironpedestals.Items.Augments.AugmentType;
import com.mowmaster.ironpedestals.Items.PedestalDataComponents;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import com.mowmaster.ironpedestals.Transport.PedestalTransportRules;
import com.mowmaster.ironpedestals.api.upgrade.IPedestalUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

// Shared upgrade behaviour (1.20's ItemUpgradeBase). Subclasses do their work in upgradeAction, which runs every
// (TICKS_TO_TRANSFER - speed) ticks while the pedestal isn't powered.
//
// Tiers (UpgradeTier, in the upgrade_tier component): speed, transfer rates, block capacity and super speed come
// from the tier. Range and area come from the pedestal's range. The special modifiers (entity containers, remote
// storage...) are unlocked with the Upgrade smithing template (UpgradeSmithingRecipe).
// Modifiers: pass the names the upgrade uses to the constructor (UpgradeModifiers.SPEED, ANY_CAPACITY...); a tier
// or the template only affects the ones listed.
// Transport modes: upgrades that move every transport type (import, export, magnet...) override
// usesTransportModes. With the upgrade in the OFFHAND, right-click toggles the selected type on/off and
// sneak + right-click selects the next type (the item model shows which).
// Fuel and tools: override getFuelCost / getToolRules with the upgrade's config values.
public abstract class ItemUpgradeBase extends Item implements IPedestalUpgrade
{
    private static final String MODID = IronPedestalReferences.MODID;

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

    // Ticks between operations: the pedestal's transfer speed, less the tier's speed (at least 1)
    public int getOperationTicks(ItemStack upgrade)
    {
        return Math.max(1, Config.TICKS_TO_TRANSFER.getAsInt() - getModifier(upgrade, UpgradeModifiers.SPEED));
    }

    // ---- Added / removed ----

    @Override
    public void onAdded(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade, @Nullable Player player)
    {
        if (player != null) upgrade.set(PedestalDataComponents.UPGRADE_OWNER.get(), MowLibOwner.of(player));
    }

    @Override
    public void onRemoved(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        upgrade.remove(PedestalDataComponents.UPGRADE_OWNER.get());
    }

    // ---- Tier and modifiers ----

    public static UpgradeTier getTier(ItemStack upgrade)
    {
        return UpgradeTier.of(upgrade);
    }

    public static UpgradeModifiers getModifiers(ItemStack upgrade)
    {
        return upgrade.getOrDefault(PedestalDataComponents.UPGRADE_MODIFIERS.get(), UpgradeModifiers.EMPTY);
    }

    // Whether this upgrade uses this modifier (the smithing template and the tooltip check this)
    public boolean canModify(ItemStack upgrade, String modifier)
    {
        return modifiers.contains(modifier) || (modifiers.contains(UpgradeModifiers.ANY_CAPACITY) && capacityType(modifier) != null);
    }

    // The speed is the tier's ticks_reduced times this (the furnace upgrades override it)
    protected int speedMultiplier()
    {
        return 1;
    }

    // The modifier's value, 0 if the upgrade doesn't use it:
    //   speed          the tier's ticks_reduced (x speedMultiplier)
    //   super speed    1 from upgrade_super_speed_tier on
    //   block capacity the tier's item rate / upgrade_block_capacity_divisor (0 at tier 0)
    //   capacities     the tier's rate for that type, in the config's units (experience: levels; 0 at tier 0)
    //   anything else  what the smithing template stored (UpgradeModifiers.SPECIALS)
    public int getModifier(ItemStack upgrade, String modifier)
    {
        if (!canModify(upgrade, modifier)) return 0;
        UpgradeTier tier = getTier(upgrade);
        Config.UpgradeTierValues values = tier.config();
        if (modifier.equals(UpgradeModifiers.SPEED)) return values.ticksReduced().getAsInt() * speedMultiplier();
        if (modifier.equals(UpgradeModifiers.SUPER_SPEED)) return tier.tier() >= Config.SUPER_SPEED_TIER.getAsInt() ? 1 : 0;
        if (modifier.equals(UpgradeModifiers.BLOCK_CAPACITY))
        {
            return tier.tier() == 0 ? 0 : values.itemRate().getAsInt() / Config.BLOCK_CAPACITY_DIVISOR.getAsInt();
        }
        TransportType<?> type = capacityType(modifier);
        if (type != null) return tier.tier() == 0 ? 0 : tierRate(values, type);
        return getModifiers(upgrade).get(modifier);
    }

    public boolean hasModifier(ItemStack upgrade, String modifier)
    {
        return getModifier(upgrade, modifier) >= 1;
    }

    // A tier's rate for one of the four built-in types (other types: 0, so the upgrade's base rate is used)
    private static int tierRate(Config.UpgradeTierValues values, TransportType<?> type)
    {
        if (type == TransportTypes.ITEMS.get()) return values.itemRate().getAsInt();
        if (type == TransportTypes.FLUIDS.get()) return values.fluidRate().getAsInt();
        if (type == TransportTypes.ENERGY.get()) return values.energyRate().getAsInt();
        if (type == TransportTypes.EXPERIENCE.get()) return values.xpRate().getAsInt();
        return 0;
    }

    // An upgrade's rate for a type: tier 0 (or a type the tier has no value for) uses the upgrade's own base rate;
    // other tiers use the tier's rate (experience turned from levels into points by the type's rules)
    public int getRate(ItemStack upgrade, TransportType<?> type, int baseRate)
    {
        PedestalTransportRules rules = PedestalTransportRules.of(type);
        int tierValue = getModifier(upgrade, rules.capacityModifier(type));
        return tierValue > 0 ? rules.upgradeRate(0, tierValue) : baseRate;
    }

    // The type a capacity modifier belongs to, or null if it isn't one
    private static @Nullable TransportType<?> capacityType(String modifier)
    {
        for (TransportType<?> type : TransportTypes.REGISTRY)
        {
            if (PedestalTransportRules.of(type).capacityModifier(type).equals(modifier)) return type;
        }
        return null;
    }

    // The special modifiers (smithing template) this upgrade uses, sorted by name
    public List<String> getSpecialModifiers(ItemStack upgrade)
    {
        return UpgradeModifiers.SPECIALS.keySet().stream().filter(modifier -> canModify(upgrade, modifier)).sorted().toList();
    }

    // ---- Work cards ----

    // How far work card positions may be: the pedestal's work range (0 without a card type)
    @Override
    public int getWorkRange(BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        return getWorkCardType(upgrade) == null ? 0 : pedestal.getTierValues().workRange().getAsInt();
    }

    // The largest area card box the upgrade works with when REQUIRE_SIZED_AREA is on: the pedestal's range too
    public int getAreaSize(BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        return pedestal.getTierValues().workRange().getAsInt();
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
        return mode != null ? mode : new UpgradeMode(TransportTypes.ITEMS.getId(), List.of());
    }

    private static void setMode(ItemStack upgrade, UpgradeMode mode)
    {
        upgrade.set(PedestalDataComponents.UPGRADE_MODE.get(), mode);
        upgrade.set(PedestalDataComponents.UPGRADE_MODE_DISPLAY.get(), mode.display());
    }

    // Whether the upgrade works on this type (always true for upgrades without transport modes)
    public boolean canTransport(ItemStack upgrade, TransportType<?> type)
    {
        return !usesTransportModes() || getMode(upgrade).isEnabled(type.getId());
    }

    // The types this upgrade works on, in registry order
    public List<TransportType<?>> getTransportTypes(ItemStack upgrade)
    {
        List<TransportType<?>> types = new ArrayList<>();
        for (TransportType<?> type : TransportTypes.REGISTRY)
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
    private static TransportType<?> nextType(net.minecraft.resources.Identifier current)
    {
        List<TransportType<?>> types = TransportTypes.REGISTRY.stream().toList();
        for (int index = 0; index < types.size(); index++)
        {
            if (types.get(index).getId().equals(current)) return types.get((index + 1) % types.size());
        }
        return types.getFirst();
    }

    private static Component typeName(net.minecraft.resources.Identifier id)
    {
        TransportType<?> type = TransportTypes.REGISTRY.getValue(id);
        return type != null ? type.getName() : Component.literal(id.toString());
    }

    // ---- Helpers for subclasses ----

    // The item handler of the block the pedestal stands on, seen from the pedestal's side.
    // null if there's none, or it's another pedestal (1.20 never pulled from ironpedestals below).
    protected static @Nullable ResourceHandler<ItemResource> getInventoryBelow(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        BlockPos below = pedestal.getPosBelow();
        if (level.getBlockEntity(below) instanceof BasePedestalBlockEntity) return null;
        return level.getCapability(Capabilities.Item.BLOCK, below, pedestal.getFacing());
    }

    // A type's handler at pos: the block's, or (with entities = true, for the entity container modifier) the first
    // entity's in that block space, like a chest minecart
    protected static <R extends Resource> @Nullable ResourceHandler<R> findHandler(ServerLevel level, BlockPos pos, @Nullable Direction side,
                                                                                    TransportType<R> type, boolean entities)
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
            for (TransportType<?> type : getTransportTypes(upgrade))
            {
                if (!entity.isAlive()) break;
                int rate = pedestal.getTransferRate(type);
                if (entity instanceof Player player)
                {
                    if (!player.isShiftKeyDown() && !player.isSpectator()) pullFromPlayer(pedestal, type, player, rate);
                }
                else if (!(entity instanceof LivingEntity)) absorbFrom(level, pedestal, type, entity, rate);
            }
        }
    }

    private static <R extends Resource> void absorbFrom(ServerLevel level, BasePedestalBlockEntity pedestal, TransportType<R> type, Entity entity, int rate)
    {
        type.absorbEntity(level, entity, pedestal.getStorage(type), rate);
    }

    // MowLib's types know how to take from a player: items the first main-inventory stack that fits, experience the
    // player's real XP, other types out of the items they carry
    private static <R extends Resource> void pullFromPlayer(BasePedestalBlockEntity pedestal, TransportType<R> type, Player player, int rate)
    {
        type.pullFromPlayer(player, pedestal.getStorage(type), rate);
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
            for (TransportType<?> type : TransportTypes.REGISTRY)
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
                    .append(cardType.getName()).withStyle(ChatFormatting.GRAY));
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

        // Tier: its speed and rates, then the specials this upgrade can have
        UpgradeTier tier = getTier(stack);
        tooltip.accept(Component.translatable(MODID + ".upgrade_tier", tier.tier(), Component.translatable(MODID + ".upgrade_tier." + tier.key()))
                .withStyle(ChatFormatting.GOLD));
        if (canModify(stack, UpgradeModifiers.SPEED))
        {
            tooltip.accept(Component.translatable(MODID + ".upgrade_tooltip_speed", getOperationTicks(stack)).withStyle(ChatFormatting.AQUA));
        }
        for (TransportType<?> type : TransportTypes.REGISTRY)
        {
            PedestalTransportRules typeRules = PedestalTransportRules.of(type);
            int value = getModifier(stack, typeRules.capacityModifier(type));
            if (value <= 0) continue;
            // ironpedestals.upgrade_tooltip_rate.<namespace>.<path>, e.g. "%s levels"; the plain number if missing
            String key = MODID + ".upgrade_tooltip_rate." + type.getId().getNamespace() + "." + type.getId().getPath();
            tooltip.accept(Component.literal(" ").append(type.getName()).append(": ")
                    .append(Component.translatableWithFallback(key, String.valueOf(value), value)).withStyle(ChatFormatting.AQUA));
        }
        int blocks = getModifier(stack, UpgradeModifiers.BLOCK_CAPACITY);
        if (blocks > 0) tooltip.accept(Component.translatable(MODID + ".upgrade_tooltip_blocks", blocks).withStyle(ChatFormatting.AQUA));
        if (hasModifier(stack, UpgradeModifiers.SUPER_SPEED))
        {
            tooltip.accept(Component.translatable(MODID + ".modifier." + UpgradeModifiers.SUPER_SPEED).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        for (String modifier : getSpecialModifiers(stack))
        {
            boolean unlocked = getModifiers(stack).get(modifier) > 0;
            tooltip.accept(modifierName(modifier).copy().append(": ")
                    .append(Component.translatable(MODID + (unlocked ? ".upgrade_tooltip_unlocked" : ".upgrade_tooltip_locked"))
                            .withStyle(unlocked ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY))
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    // "Speed", "Range", "Fluids Capacity"... (ironpedestals.modifier.<name>; capacity modifiers use the type's name)
    public static Component modifierName(String modifier)
    {
        TransportType<?> type = capacityType(modifier);
        if (type != null) return Component.translatable(MODID + ".modifier.capacity", type.getName());
        return Component.translatableWithFallback(MODID + ".modifier." + modifier, modifier);
    }
}
