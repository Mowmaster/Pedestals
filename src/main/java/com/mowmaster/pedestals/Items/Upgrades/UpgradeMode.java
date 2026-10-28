package com.mowmaster.pedestals.Items.Upgrades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

// A transport upgrade's settings (import, export, void, magnet, dropper, packager...): which transport types it
// works on. 1.20 had one boolean per mode (items/fluids/energy/xp/dust); here it's the ids of the turned-off types,
// so add-on types work too. selected = the type the offhand clicks currently change.
public record UpgradeMode(Identifier selected, List<Identifier> disabled)
{
    public static final Codec<UpgradeMode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("selected").forGetter(UpgradeMode::selected),
            Identifier.CODEC.listOf().optionalFieldOf("disabled", List.of()).forGetter(UpgradeMode::disabled))
            .apply(instance, UpgradeMode::new));

    public static final StreamCodec<ByteBuf, UpgradeMode> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, UpgradeMode::selected,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), UpgradeMode::disabled,
            UpgradeMode::new);

    public UpgradeMode
    {
        disabled = List.copyOf(disabled);
    }

    public boolean isEnabled(Identifier type)
    {
        return !disabled.contains(type);
    }

    public UpgradeMode select(Identifier type)
    {
        return new UpgradeMode(type, disabled);
    }

    public UpgradeMode toggleSelected()
    {
        List<Identifier> changed = new ArrayList<>(disabled);
        if (!changed.remove(selected)) changed.add(selected);
        return new UpgradeMode(selected, changed);
    }

    // What the item model shows: the selected type, and whether it's on
    public Display display()
    {
        return new Display(selected, isEnabled(selected));
    }

    // Its own component so the item model can match it exactly:
    // "when": {"type": "pedestals:items", "enabled": true} in items/upgrade_pedestal_import.json
    public record Display(Identifier type, boolean enabled)
    {
        public static final Codec<Display> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("type").forGetter(Display::type),
                Codec.BOOL.fieldOf("enabled").forGetter(Display::enabled))
                .apply(instance, Display::new));

        public static final StreamCodec<ByteBuf, Display> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, Display::type,
                ByteBufCodecs.BOOL, Display::enabled,
                Display::new);
    }
}
