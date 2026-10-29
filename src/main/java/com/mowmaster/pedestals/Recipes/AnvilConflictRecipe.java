package com.mowmaster.pedestals.Recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

// What the XP anvil charges for putting two enchantments that normally exclude each other on one item (Sharpness
// with Smite...). Either order matches. A new enchantment that conflicts with several on the item pays each pair's
// recipe; a pair with no recipe can't be combined.
//   {"type": "pedestals:anvil_conflict", "first": "minecraft:sharpness", "second": "minecraft:smite",
//    "ingredients": [{"ingredient": "minecraft:nether_star"}], "experience_levels": 10}
// "first" and "second" take one id, a list, or a tag. The cost fields are AnvilCost's ("levels" there is the new
// enchantment's level).
public final class AnvilConflictRecipe extends PedestalMachineRecipe<AnvilConflictRecipe.Input>
{
    public static final MapCodec<AnvilConflictRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.holderSet(Registries.ENCHANTMENT).fieldOf("first").forGetter(AnvilConflictRecipe::first),
            RegistryCodecs.holderSet(Registries.ENCHANTMENT).fieldOf("second").forGetter(AnvilConflictRecipe::second),
            AnvilCost.MAP_CODEC.forGetter(AnvilConflictRecipe::cost))
            .apply(instance, AnvilConflictRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AnvilConflictRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.ENCHANTMENT), AnvilConflictRecipe::first,
            ByteBufCodecs.holderSet(Registries.ENCHANTMENT), AnvilConflictRecipe::second,
            AnvilCost.STREAM_CODEC, AnvilConflictRecipe::cost,
            AnvilConflictRecipe::new);

    private final HolderSet<Enchantment> first;
    private final HolderSet<Enchantment> second;
    private final AnvilCost cost;

    public AnvilConflictRecipe(HolderSet<Enchantment> first, HolderSet<Enchantment> second, AnvilCost cost)
    {
        this.first = first;
        this.second = second;
        this.cost = cost;
    }

    public HolderSet<Enchantment> first() { return first; }
    public HolderSet<Enchantment> second() { return second; }
    public AnvilCost cost() { return cost; }

    @Override
    public boolean matches(Input input, Level level)
    {
        return (first.contains(input.adding()) && second.contains(input.existing()))
                || (first.contains(input.existing()) && second.contains(input.adding()));
    }

    @Override
    public RecipeSerializer<AnvilConflictRecipe> getSerializer()
    {
        return PedestalRecipes.ANVIL_CONFLICT_SERIALIZER.get();
    }

    @Override
    public RecipeType<AnvilConflictRecipe> getType()
    {
        return PedestalRecipes.ANVIL_CONFLICT.get();
    }

    // The enchantment being added and one already on the item that it conflicts with
    public record Input(Holder<Enchantment> adding, Holder<Enchantment> existing) implements RecipeInput
    {
        @Override
        public ItemStack getItem(int index)
        {
            return ItemStack.EMPTY;
        }

        @Override
        public int size()
        {
            return 0;
        }
    }
}
