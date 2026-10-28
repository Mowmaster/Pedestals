package com.mowmaster.pedestals.Transport;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.api.transport.AmountResource;
import com.mowmaster.pedestals.api.transport.AmountStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import com.mowmaster.pedestals.api.transport.PedestalStorageHost;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
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

    // As in 1.20: the rate is in levels, counted from level 0 (1 level = 7 points)
    @Override
    public int getTransferRate(PedestalStorageHost host)
    {
        return pointsForLevel(Config.XP_TRANSFER_RATE.getAsInt());
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

    public static class Storage extends AmountStorage
    {
        public Storage(PedestalStorageHost host, ExperienceTransportType type)
        {
            super(host, type, EXPERIENCE);
        }

        @Override
        public int getCapacity()
        {
            return pointsForLevel(Config.XP_STORAGE.getAsInt());
        }

        // Broken pedestals drop their experience as orbs
        @Override
        public void onPedestalRemoved(Level level, BlockPos pos)
        {
            if (amount > 0 && level instanceof ServerLevel serverLevel)
            {
                ExperienceOrb.award(serverLevel, Vec3.atCenterOf(pos), amount);
            }
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
}
