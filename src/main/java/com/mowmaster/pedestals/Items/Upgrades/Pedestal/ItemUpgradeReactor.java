package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mojang.serialization.Codec;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.SacrificeItem;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.ReactorEffect;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.PedestalDataMaps;
import com.mowmaster.pedestals.Transport.EnergyTransportType;
import com.mowmaster.pedestals.Transport.ExperienceTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.AmountResource;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

// Enchantment Sacrifice Reactor: burns overleveled enchantment sacrifices (from the sacrifice upgrade) for energy,
// using this pedestal's experience as fuel. The sacrifices sit on pedestals selected with a pedestals work card.
//
// Every second while a sacrifice burns it takes 20x the sacrifice's grindstone value in experience from this pedestal
// and makes upgrade_reactor_energy_per_xp energy per point, times:
//  - the tier bonus: the sacrifice's tier is its enchantment levels added up (0-9 = tier 1, 10-19 = tier 2...),
//    each tier above 1 adds upgrade_reactor_tier_bonus_percent;
//  - the variety bonus: each different kind of sacrifice among the recent ones (upgrade_reactor_recent_count) after
//    the first adds upgrade_reactor_variety_bonus_percent. A kind is its set of enchantments: Protection 6 and
//    Protection 10 are one kind, Protection 6 + Unbreaking 6 another;
//  - wear: each time a kind is burned again it gives upgrade_reactor_wear_percent less, until it drops out of the
//    recent ones (the experience cost stays the same).
// A sacrifice burns for tier x upgrade_reactor_burn_seconds_per_tier seconds. When the energy storage is full, or the
// experience runs out, it pauses (nothing is lost). Once a burn is over and the stored energy has all been taken out,
// the next sacrifice is used.
//
// The next sacrifice is picked at random among the selected pedestals holding one, and shown as a comparator signal:
// 1 for the first selected pedestal, up to 15. From the 16th pedestal on there's no signal, but that sacrifice burns
// twice as long.
// While burning it gives effects like a beacon, by tier, to players within upgrade_reactor_effect_range_base +
// tier x upgrade_reactor_effect_range_per_tier blocks (see the pedestals:reactor_effects data map and ReactorEffect).
public class ItemUpgradeReactor extends ItemUpgradeBase
{
    private static final String BURN_LEFT = "reactor_burn_left";       // experience points left to burn
    private static final String XP_PER_SECOND = "reactor_xp_per_second";
    private static final String ENERGY_PER_XP = "reactor_energy_per_xp";
    private static final String TIER = "reactor_tier";
    private static final String NEXT = "reactor_next";                 // index of the next sacrifice's pedestal, -1 = none
    private static final String RECENT = "reactor_recent";             // kinds of the last burned sacrifices, oldest first
    private static final String WEAR = "reactor_wear";                 // kind -> times burned while recent
    private static final int EFFECT_TICKS = 20 * 20;

    public ItemUpgradeReactor(Properties properties)
    {
        super(properties, UpgradeModifiers.RANGE);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.PEDESTALS;
    }

    // Works once a second, whatever the speed (the amounts are per second)
    @Override
    public void tick(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        if (pedestal.isPowered() || level.getGameTime() % 20 != 0) return;
        upgradeAction(level, pedestal, upgrade);
    }

    // 1-15 for the next sacrifice's pedestal, 0 when there's none or it's the 16th or later
    @Override
    public int getComparatorSignal(BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        int next = pedestal.getUpgradeData().getIntOr(NEXT, -1);
        return next >= 0 && next < 15 ? next + 1 : 0;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        CompoundTag data = pedestal.getUpgradeData();
        List<BlockPos> positions = pedestal.getWorkPositions();
        if (data.getIntOr(BURN_LEFT, 0) > 0) burn(level, pedestal, data);
        else if (energy(pedestal).getAmountAsInt(0) <= 0) consume(level, pedestal, data, positions);
        if (sacrificeAt(level, positions, data.getIntOr(NEXT, -1)) == null) chooseNext(level, pedestal, data, positions);
    }

    // ---- Burning ----

    private static void burn(ServerLevel level, BasePedestalBlockEntity pedestal, CompoundTag data)
    {
        int left = data.getIntOr(BURN_LEFT, 0);
        int perSecond = data.getIntOr(XP_PER_SECOND, 0);
        double energyPerXp = data.getDoubleOr(ENERGY_PER_XP, 0);
        PedestalStorage<AmountResource> experience = pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get());
        PedestalStorage<AmountResource> energy = energy(pedestal);

        // As much of this second's experience as there is, and as there's room for the energy it makes
        int room = energy.getCapacityAsInt(0, EnergyTransportType.ENERGY) - energy.getAmountAsInt(0);
        int fitsRoom = energyPerXp <= 0 ? Integer.MAX_VALUE : (int) Math.min(Integer.MAX_VALUE, Math.floor(room / energyPerXp));
        int burning = Math.min(Math.min(perSecond, left), Math.min(experience.getAmountAsInt(0), fitsRoom));
        if (burning <= 0) return; // paused: no experience, or the energy storage is full

        int made = (int) Math.min(Integer.MAX_VALUE, Math.floor(burning * energyPerXp));
        try (Transaction transaction = Transaction.openRoot())
        {
            if (experience.extract(ExperienceTransportType.EXPERIENCE, burning, transaction) != burning) return;
            if (made > 0 && energy.insert(EnergyTransportType.ENERGY, made, transaction) != made) return;
            transaction.commit();
        }
        data.putInt(BURN_LEFT, left - burning);
        pedestal.upgradeDataChanged();
        // Refreshed every 4 seconds like a beacon; the effects last 20 seconds
        if (level.getGameTime() % 80 == 0) applyEffects(level, pedestal, data.getIntOr(TIER, 1));
    }

    private static void applyEffects(ServerLevel level, BasePedestalBlockEntity pedestal, int tier)
    {
        Map<ResourceKey<MobEffect>, ReactorEffect> effects = BuiltInRegistries.MOB_EFFECT.getDataMap(PedestalDataMaps.REACTOR_EFFECTS);
        if (effects.isEmpty()) return;
        int range = Config.REACTOR_RANGE_BASE.getAsInt() + Config.REACTOR_RANGE_PER_TIER.getAsInt() * tier;
        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(pedestal.getBlockPos()).inflate(range));
        if (players.isEmpty()) return;
        effects.forEach((key, effect) -> {
            int amplifier = effect.amplifierAt(tier);
            if (amplifier < 0) return; // "return" in a forEach lambda skips to the next entry
            BuiltInRegistries.MOB_EFFECT.get(key).ifPresent(holder -> {
                for (Player player : players) player.addEffect(new MobEffectInstance(holder, EFFECT_TICKS, amplifier, true, true));
            });
        });
    }

    // ---- Starting the next sacrifice ----

    private static void consume(ServerLevel level, BasePedestalBlockEntity pedestal, CompoundTag data, List<BlockPos> positions)
    {
        int next = data.getIntOr(NEXT, -1);
        if (sacrificeAt(level, positions, next) == null) next = chooseNext(level, pedestal, data, positions);
        BasePedestalBlockEntity source = sacrificeAt(level, positions, next);
        if (source == null) return;
        ItemStack sacrifice = firstSacrifice(source);
        try (Transaction transaction = Transaction.openRoot())
        {
            if (source.getItemStorage().extract(ItemResource.of(sacrifice), 1, transaction) != 1) return;
            transaction.commit();
        }

        int tier = SacrificeItem.totalLevels(sacrifice) / 10 + 1;
        int xpPerSecond = (int) Math.min(Integer.MAX_VALUE, 20L * SacrificeItem.grindstoneValue(sacrifice));
        long seconds = (long) tier * Config.REACTOR_BURN_SECONDS.getAsInt() * (next >= 15 ? 2 : 1);

        // Wear and variety come from the recent kinds
        String kind = SacrificeItem.kind(sacrifice);
        List<String> recent = new ArrayList<>(data.read(RECENT, Codec.STRING.listOf()).orElse(List.of()));
        Map<String, Integer> wear = new HashMap<>(data.read(WEAR, Codec.unboundedMap(Codec.STRING, Codec.INT)).orElse(Map.of()));
        int uses = recent.contains(kind) ? wear.getOrDefault(kind, 0) : 0;
        double wearFactor = Math.pow(1 - Config.REACTOR_WEAR.getAsInt() / 100.0, uses);
        wear.put(kind, uses + 1);
        recent.add(kind);
        while (recent.size() > Config.REACTOR_RECENT.getAsInt()) recent.removeFirst();
        wear.keySet().retainAll(recent);
        int kinds = new HashSet<>(recent).size();
        double variety = 1 + Config.REACTOR_VARIETY_BONUS.getAsInt() / 100.0 * (kinds - 1);
        double tierBonus = 1 + Config.REACTOR_TIER_BONUS.getAsInt() / 100.0 * (tier - 1);

        data.putInt(BURN_LEFT, (int) Math.min(Integer.MAX_VALUE, xpPerSecond * seconds));
        data.putInt(XP_PER_SECOND, xpPerSecond);
        data.putDouble(ENERGY_PER_XP, Config.REACTOR_ENERGY_PER_XP.getAsInt() * tierBonus * variety * wearFactor);
        data.putInt(TIER, tier);
        data.store(RECENT, Codec.STRING.listOf(), recent);
        data.store(WEAR, Codec.unboundedMap(Codec.STRING, Codec.INT), wear);
        pedestal.upgradeDataChanged();

        PedestalTransportTypes.ITEMS.get().sendTransferParticles(level, topOf(source), topOf(pedestal), ItemResource.of(sacrifice));
        level.playSound(null, pedestal.getBlockPos(), SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.5F, 0.8F);
        applyEffects(level, pedestal, tier);
        chooseNext(level, pedestal, data, positions);
    }

    // Picks the next sacrifice's pedestal at random and updates the comparator signal; returns its index (-1 = none)
    private static int chooseNext(ServerLevel level, BasePedestalBlockEntity pedestal, CompoundTag data, List<BlockPos> positions)
    {
        List<Integer> candidates = new ArrayList<>();
        for (int index = 0; index < positions.size(); index++)
        {
            if (sacrificeAt(level, positions, index) != null) candidates.add(index);
        }
        int next = candidates.isEmpty() ? -1 : candidates.get(level.getRandom().nextInt(candidates.size()));
        if (next != data.getIntOr(NEXT, -1))
        {
            data.putInt(NEXT, next);
            pedestal.upgradeDataChanged();
            level.updateNeighbourForOutputSignal(pedestal.getBlockPos(), pedestal.getBlockState().getBlock());
        }
        return next;
    }

    // The pedestal at that index of the work card, if it holds a sacrifice the reactor can burn
    private static @Nullable BasePedestalBlockEntity sacrificeAt(ServerLevel level, List<BlockPos> positions, int index)
    {
        if (index < 0 || index >= positions.size()) return null;
        if (!(level.getBlockEntity(positions.get(index)) instanceof BasePedestalBlockEntity source)) return null;
        return firstSacrifice(source).isEmpty() ? null : source;
    }

    private static ItemStack firstSacrifice(BasePedestalBlockEntity source)
    {
        for (ItemStack stack : source.getItemStacks())
        {
            if (SacrificeItem.isOverleveled(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    // ---- Small helpers ----

    private static PedestalStorage<AmountResource> energy(BasePedestalBlockEntity pedestal)
    {
        return pedestal.getStorage(PedestalTransportTypes.ENERGY.get());
    }

    private static Vec3 topOf(BasePedestalBlockEntity pedestal)
    {
        return Vec3.atCenterOf(pedestal.getBlockPos()).add(pedestal.getFacing().getUnitVec3().scale(0.5));
    }
}
