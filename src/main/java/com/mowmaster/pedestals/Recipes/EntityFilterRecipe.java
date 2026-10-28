package com.mowmaster.pedestals.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.Optional;

// Which mobs the attacker and fan work on, picked by the block under the pedestal (1.20's mowlib:entity_filter).
// Either a whole category ("monster", "creature", "water_creature", "misc"...) or one entity type, optionally only
// babies:
//   {"type": "pedestals:entity_filter", "input": "minecraft:coal_block", "category": "misc"}
//   {"type": "pedestals:entity_filter", "input": "minecraft:brown_glazed_terracotta", "entity": "minecraft:wolf", "baby": true}
// With no matching block below, those upgrades work on every mob.
public final class EntityFilterRecipe extends PedestalMachineRecipe<SingleRecipeInput>
{
    public static final MapCodec<EntityFilterRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(EntityFilterRecipe::input),
            MobCategory.CODEC.optionalFieldOf("category").forGetter(EntityFilterRecipe::category),
            Identifier.CODEC.optionalFieldOf("entity").forGetter(EntityFilterRecipe::entity),
            Codec.BOOL.optionalFieldOf("baby", false).forGetter(EntityFilterRecipe::baby))
            .apply(instance, EntityFilterRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityFilterRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, EntityFilterRecipe::input,
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(MobCategory.CODEC)), EntityFilterRecipe::category,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), EntityFilterRecipe::entity,
            ByteBufCodecs.BOOL, EntityFilterRecipe::baby,
            EntityFilterRecipe::new);

    private final Ingredient input;
    private final Optional<MobCategory> category;
    private final Optional<Identifier> entity;
    private final boolean baby;

    public EntityFilterRecipe(Ingredient input, Optional<MobCategory> category, Optional<Identifier> entity, boolean baby)
    {
        this.input = input;
        this.category = category;
        this.entity = entity;
        this.baby = baby;
    }

    public Ingredient input() { return input; }
    public Optional<MobCategory> category() { return category; }
    public Optional<Identifier> entity() { return entity; }
    public boolean baby() { return baby; }

    // Is this mob one the filter picks?
    public boolean allows(Entity target)
    {
        if (category.isPresent() && target.getType().getCategory() != category.get()) return false;
        if (entity.isPresent() && !BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).equals(entity.get())) return false;
        return !baby || (target instanceof LivingEntity living && living.isBaby());
    }

    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level level)
    {
        return input.test(recipeInput.item());
    }

    @Override
    public RecipeSerializer<EntityFilterRecipe> getSerializer()
    {
        return PedestalRecipes.ENTITY_FILTER_SERIALIZER.get();
    }

    @Override
    public RecipeType<EntityFilterRecipe> getType()
    {
        return PedestalRecipes.ENTITY_FILTER.get();
    }
}
