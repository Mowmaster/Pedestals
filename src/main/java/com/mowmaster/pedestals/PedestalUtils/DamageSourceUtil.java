package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

public class DamageSourceUtil {

    private DamageSourceUtil() {
    }

    private static Registry<DamageType> damageTypes(Level level) {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
    }

    public static DamageSource getDamageSource(Level level, ResourceKey<DamageType> damageType, Player player) {
        return new DamageSource(damageTypes(level).getHolderOrThrow(damageType), player);
    }
}
