package com.mowmaster.pedestals.Transport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Storage.StoragePacketItem;
import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.api.transport.AmountResource;
import com.mowmaster.pedestals.api.transport.AmountStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorageHost;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

// Stored as experience points; the config gives levels, like 1.20
public class ExperienceTransportType extends PedestalTransportType<AmountResource>
{
    public static final AmountResource EXPERIENCE = new AmountResource(Identifier.fromNamespaceAndPath(PedestalReferences.MODID, "experience"));

    // NeoForge has no experience capability, so this replaces MowLib's CapabilityExperience.
    // Other mods can look it up by this id ("pedestals:experience_handler"); the handler holds EXPERIENCE.
    public static final BlockCapability<ResourceHandler<AmountResource>, Direction> BLOCK = BlockCapability.createSided(
            Identifier.fromNamespaceAndPath(PedestalReferences.MODID, "experience_handler"), ResourceHandler.asClass());

    public ExperienceTransportType()
    {
        super(ChatFormatting.GREEN);
    }

    @Override
    public PedestalStorage<AmountResource> createStorage(PedestalStorageHost host)
    {
        return new Storage(host, this);
    }

    // As in 1.20: the rate is in levels, counted from level 0 (1 level = 7 points); capacity augments add levels
    @Override
    public int getTransferRate(PedestalStorageHost host)
    {
        return pointsForLevel(Config.XP_TRANSFER_RATE.getAsInt() + host.getTransferBonus(this));
    }

    // ---- Augments ----

    @Override
    public int getCapacityAugmentBonus(int tier)
    {
        return tier < 1 || tier > Config.CAPACITY_AUGMENTS.size() ? 0 : Config.CAPACITY_AUGMENTS.get(tier - 1).experience().getAsInt();
    }

    @Override
    public int getStorageAugmentBonus(int tier)
    {
        return tier < 1 || tier > Config.STORAGE_AUGMENTS.size() ? 0 : Config.STORAGE_AUGMENTS.get(tier - 1).experience().getAsInt();
    }

    @Override
    public void registerCapability(RegisterCapabilitiesEvent event, BlockEntityType<BasePedestalBlockEntity> pedestal)
    {
        event.registerBlockEntity(BLOCK, pedestal, (blockEntity, side) -> blockEntity.getStorage(this));
    }

    // Total points to get from level 0 to this level (vanilla's levelling formula, same as MowLibXpUtils)
    public static int pointsForLevel(int level)
    {
        if (level <= 0) return 0;
        long points;
        if (level <= 16) points = (long) level * level + 6L * level;
        else if (level <= 31) points = (long) (2.5 * level * level - 40.5 * level + 360);
        else points = (long) (4.5 * level * level - 162.5 * level + 2220);
        return (int) Math.min(points, Integer.MAX_VALUE);
    }

    // ---- Other things' storage: other pedestals and anything else offering pedestals:experience_handler ----

    @Override
    public @Nullable ResourceHandler<AmountResource> getBlockHandler(Level level, BlockPos pos, @Nullable Direction side)
    {
        return level.getCapability(BLOCK, pos, side);
    }

    // ---- Upgrades ----

    @Override
    public String getCapacityModifier()
    {
        return "upgradexpcapacity";
    }

    // The capacity modifier counts levels (1.20: base + getExpCountByLevel(modifier))
    @Override
    public int getUpgradeRate(int baseRate, int capacityModifier)
    {
        return baseRate + pointsForLevel(capacityModifier);
    }

    // Dropper: an experience orb worth up to amount
    @Override
    public boolean dropInWorld(ServerLevel level, BlockPos pos, PedestalStorage<AmountResource> from, int amount, @Nullable ServerPlayer cause)
    {
        int extracted;
        try (Transaction transaction = Transaction.openRoot())
        {
            extracted = from.extract(EXPERIENCE, amount, transaction);
            if (extracted <= 0) return false;
            transaction.commit();
        }
        Vec3 centre = Vec3.atCenterOf(pos);
        level.addFreshEntity(new ExperienceOrb(level, centre.x, centre.y, centre.z, extracted));
        return true;
    }

    // Magnet: experience orbs (whole orbs only: an orb's value can't be changed), and bottles o' enchanting, which
    // are worth 3-10 points each and leave glass bottles behind (as in 1.20)
    @Override
    public boolean absorbEntity(ServerLevel level, Entity entity, PedestalStorage<AmountResource> into, int amount)
    {
        if (entity instanceof ExperienceOrb orb)
        {
            int value = orb.getValue();
            if (value <= 0) return false;
            try (Transaction transaction = Transaction.openRoot())
            {
                if (into.insert(EXPERIENCE, value, transaction) < value) return false;
                transaction.commit();
            }
            orb.discard();
            return true;
        }
        if (entity instanceof ItemEntity itemEntity && itemEntity.getItem().is(Items.EXPERIENCE_BOTTLE))
        {
            int count = itemEntity.getItem().getCount();
            int value = 0;
            for (int bottle = 0; bottle < count; bottle++) value += 3 + level.getRandom().nextInt(8);
            try (Transaction transaction = Transaction.openRoot())
            {
                if (into.insert(EXPERIENCE, value, transaction) < value) return false;
                transaction.commit();
            }
            itemEntity.setItem(new ItemStack(Items.GLASS_BOTTLE, count));
            return true;
        }
        return false;
    }

    // ---- Packets: the resource is always EXPERIENCE, so nothing needs saving but the amount ----

    @Override
    public Codec<AmountResource> getResourceCodec()
    {
        return MapCodec.unitCodec(EXPERIENCE);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AmountResource> getResourceStreamCodec()
    {
        return StreamCodec.unit(EXPERIENCE);
    }

    public static class Storage extends AmountStorage
    {
        public Storage(PedestalStorageHost host, ExperienceTransportType type)
        {
            super(host, type, EXPERIENCE);
        }

        @Override
        public int getCapacity()
        {
            return pointsForLevel(Config.XP_STORAGE.getAsInt() + host.getStorageBonus(type));
        }

        // The experience drops as a mechanical experience storage packet (as in 1.20)
        @Override
        public void onPedestalRemoved(Level level, BlockPos pos)
        {
            StoragePacketItem.dropStorage(level, pos, type, this);
        }

        @Override
        public @Nullable Component describe()
        {
            if (amount <= 0) return null;
            return Component.translatable("pedestals.pedestal.message_experience")
                    .append(": " + amount + "/" + getAllowedCapacity())
                    .withStyle(type.getColor());
        }
    }

    // Glowing green and yellow specks, like experience orbs, flying to the target
    @Override
    public void addTransferParticles(Level level, Vec3 from, Vec3 to, @Nullable AmountResource resource, RandomSource random)
    {
        addTrailParticles(level, from, to, 0x80FF20, 4, random);
        addTrailParticles(level, from, to, 0xD4FF4A, 4, random);
    }
}
