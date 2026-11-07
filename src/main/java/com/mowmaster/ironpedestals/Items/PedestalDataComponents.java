package com.mowmaster.ironpedestals.Items;

import com.mojang.serialization.Codec;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.util.MowLibOwner;
import com.mowmaster.ironpedestals.Items.Upgrades.UpgradeMode;
import com.mowmaster.ironpedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.ironpedestals.Items.Upgrades.UpgradeTier;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Unit;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Item data components replace 1.20's stack NBT. persistent = saved with the item, networkSynchronized = sent to
// clients (needed for tooltips and item models).
public class PedestalDataComponents
{
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, IronPedestalReferences.MODID);

    // The pedestal a linking tool has selected
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> LINKED_POS =
            COMPONENTS.registerComponentType("linked_pos", builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

    // On a linking tool: the saved pedestal stays for many links (sneak + right-click); absent = used up by one link
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> LINK_STICKY =
            COMPONENTS.registerComponentType("link_sticky", builder -> builder.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC));

    // ---- Upgrades ----

    // The upgrade's tier, 1-6 (absent = tier 0; see UpgradeTier). Also picks the tint of its item model.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> UPGRADE_TIER =
            COMPONENTS.registerComponentType("upgrade_tier", builder -> builder.persistent(Codec.intRange(0, UpgradeTier.MAX)).networkSynchronized(ByteBufCodecs.VAR_INT));

    // The special modifiers unlocked with the Upgrade smithing template (entity containers, remote storage...).
    // Speed, capacities, super speed and block capacity come from the tier; range and area from the pedestal.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UpgradeModifiers>> UPGRADE_MODIFIERS =
            COMPONENTS.registerComponentType("upgrade_modifiers", builder -> builder.persistent(UpgradeModifiers.CODEC).networkSynchronized(UpgradeModifiers.STREAM_CODEC));

    // Which transport types a transport upgrade works on, and which one the offhand clicks change
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UpgradeMode>> UPGRADE_MODE =
            COMPONENTS.registerComponentType("upgrade_mode", builder -> builder.persistent(UpgradeMode.CODEC).networkSynchronized(UpgradeMode.STREAM_CODEC));

    // The selected type and whether it's on, for the item model (kept in step with UPGRADE_MODE)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UpgradeMode.Display>> UPGRADE_MODE_DISPLAY =
            COMPONENTS.registerComponentType("upgrade_mode_display", builder -> builder.persistent(UpgradeMode.Display.CODEC).networkSynchronized(UpgradeMode.Display.STREAM_CODEC));

    // The player who put the upgrade in a pedestal (removed when it's taken out)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MowLibOwner>> UPGRADE_OWNER =
            COMPONENTS.registerComponentType("upgrade_owner", builder -> builder.persistent(MowLibOwner.CODEC).networkSynchronized(MowLibOwner.STREAM_CODEC));

    // On a sacrifice upgrade: present = mob mode, absent = enchantment mode (also picks the item model).
    // Unit is Minecraft's "no value" type, for components that only matter by being there.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> SACRIFICE_MOB =
            COMPONENTS.registerComponentType("sacrifice_mob", builder -> builder.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC));

    // The particle & render augment's mode (0-6, see AugmentItem#hidesParticles etc.); absent = 0
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DIFFUSER_MODE =
            COMPONENTS.registerComponentType("diffuser_mode", builder -> builder.persistent(Codec.intRange(0, 6)).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static void register(IEventBus eventBus)
    {
        // Components that moved to MowLib (filters, work cards, storage packets): old items in saved worlds carry
        // the ironpedestals: ids, which these aliases point at the mowlib: ones. addAlias(old id, new id) has to happen
        // before the registry is built, so it's done here, before COMPONENTS is registered.
        alias("filter_mode", "filter_mode");
        alias("filter_data", "filter_data");
        alias("work_positions", "work_positions");
        alias("packet_contents", "transport_contents");
        COMPONENTS.register(eventBus);
    }

    private static void alias(String pedestalsId, String mowlibId)
    {
        COMPONENTS.addAlias(Identifier.fromNamespaceAndPath(IronPedestalReferences.MODID, pedestalsId), MowLibReferences.id(mowlibId));
    }
}
