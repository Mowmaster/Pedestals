package com.mowmaster.ironpedestals.ClientSide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.ironpedestals.Items.Upgrades.UpgradeTier;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// Item model tints for upgrades, by tier. Every upgrade's item model definition (items/upgrade_pedestal_*.json) has
//   "tints": [{"type": "ironpedestals:upgrade_tier"}, {"type": "ironpedestals:upgrade_tier", "symbol": true}]
// tintindex 0 (the greyscale plate and frame): the tier's colour (UpgradeTier#color).
// tintindex 1 (the black parts of the symbol, stored white): near-black on light tiers, light grey on dark ones
// (netherite), so the symbol stays readable. Coloured parts of a symbol have no tint.
// Registered in IronPedestalsClient. Tints are 0xAARRGGBB, so colours get a full alpha.
public record UpgradeTierTint(boolean symbol) implements ItemTintSource
{
    // How dark a tier colour must be (0-255 brightness) for its symbols to turn light
    private static final int DARK_TIER = 90;

    public static final MapCodec<UpgradeTierTint> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("symbol", false).forGetter(UpgradeTierTint::symbol))
            .apply(instance, UpgradeTierTint::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity)
    {
        int color = UpgradeTier.of(stack).color();
        if (!symbol) return 0xFF000000 | color;
        return brightness(color) < DARK_TIER ? 0xFFD8D8D8 : 0xFF1A1A1A;
    }

    // Perceived brightness of an RGB colour, 0-255
    private static int brightness(int rgb)
    {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return (red * 299 + green * 587 + blue * 114) / 1000;
    }

    @Override
    public MapCodec<UpgradeTierTint> type()
    {
        return MAP_CODEC;
    }
}
