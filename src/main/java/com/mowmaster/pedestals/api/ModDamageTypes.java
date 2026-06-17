package com.mowmaster.pedestals.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import com.mowmaster.pedestals.PedestalUtils.DamageSourceUtil;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


public class ModDamageTypes {
    // TODO: add the rest of damage types
    public static final ResourceKey<DamageType> PEDESTAL = registerKey("pedestal1");

    public static ResourceKey<DamageType> registerKey(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MODID, name));
    }

    public static boolean pedestal(Player player, Entity entity, float amount) {
        return entity.hurt(DamageSourceUtil.getDamageSource(entity.level(), PEDESTAL, player), amount);
    }
}
