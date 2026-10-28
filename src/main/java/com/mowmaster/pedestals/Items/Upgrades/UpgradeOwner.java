package com.mowmaster.pedestals.Items.Upgrades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

// Who put the upgrade in (1.20's MowLibOwnerUtils NBT). The pedestal's fake player uses this id and name, so
// claim/protection mods treat the pedestal's work like that player's.
public record UpgradeOwner(UUID id, String name)
{
    public static final Codec<UpgradeOwner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(UpgradeOwner::id),
            Codec.STRING.fieldOf("name").forGetter(UpgradeOwner::name))
            .apply(instance, UpgradeOwner::new));

    public static final StreamCodec<ByteBuf, UpgradeOwner> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, UpgradeOwner::id,
            ByteBufCodecs.STRING_UTF8, UpgradeOwner::name,
            UpgradeOwner::new);
}
