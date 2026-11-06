package com.mowmaster.pedestals.Items.Upgrades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

// A transport upgrade's settings (import, export, void, magnet, dropper, packager...): which transport types it
// works on. 1.20 had one boolean per mode (items/fluids/energy/xp/dust); here it's the ids of the turned-off types,
// so add-on types work too. selected = the type the offhand clicks currently change.
// Type ids are loaded as the type's current id: upgrades saved before the types moved to MowLib say pedestals:items,
// which is an alias of mowlib:items (see PedestalTransportTypeAliases).
public record UpgradeMode(Identifier selected, List<Identifier> disabled)
{
    // An id loaded through this becomes the type's current one; ids of types that aren't there stay as they are
    private static final Codec<Identifier> TYPE_ID = Identifier.CODEC.xmap(UpgradeMode::currentId, id -> id);

    public static final Codec<UpgradeMode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TYPE_ID.fieldOf("selected").forGetter(UpgradeMode::selected),
            TYPE_ID.listOf().optionalFieldOf("disabled", List.of()).forGetter(UpgradeMode::disabled))
            .apply(instance, UpgradeMode::new));

    // Registry#getValue follows aliases, so an old id finds the type and getId gives its id now
    private static Identifier currentId(Identifier id)
    {
        TransportType<?> type = TransportTypes.REGISTRY.getValue(id);
        return type != null ? type.getId() : id;
    }

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
    // "when": {"type": "mowlib:items", "enabled": true} in items/upgrade_pedestal_import.json
    public record Display(Identifier type, boolean enabled)
    {
        public static final Codec<Display> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TYPE_ID.fieldOf("type").forGetter(Display::type),
                Codec.BOOL.fieldOf("enabled").forGetter(Display::enabled))
                .apply(instance, Display::new));

        public static final StreamCodec<ByteBuf, Display> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, Display::type,
                ByteBufCodecs.BOOL, Display::enabled,
                Display::new);
    }
}
