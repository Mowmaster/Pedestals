package com.mowmaster.pedestals.Recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

// Bottler upgrade: an item from the inventory below plus what's in the pedestal makes the result
// (glass bottle + 11 experience = bottle o' enchanting, bucket + 1000mB water = water bucket).
//   {"type": "pedestals:bottler", "input": "minecraft:glass_bottle", "experience": 11, "result": {"id": "minecraft:experience_bottle"}}
// (Not a record: records can't extend a class, and this one extends PedestalMachineRecipe for its defaults.)
public final class BottlerRecipe extends PedestalMachineRecipe<PedestalRecipeInput>
{
    public static final MapCodec<BottlerRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(BottlerRecipe::input),
            MachineCost.MAP_CODEC.forGetter(BottlerRecipe::cost),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(BottlerRecipe::result))
            .apply(instance, BottlerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BottlerRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, BottlerRecipe::input,
            MachineCost.STREAM_CODEC, BottlerRecipe::cost,
            ItemStackTemplate.STREAM_CODEC, BottlerRecipe::result,
            BottlerRecipe::new);

    private final Ingredient input;
    private final MachineCost cost;
    private final ItemStackTemplate result;

    public BottlerRecipe(Ingredient input, MachineCost cost, ItemStackTemplate result)
    {
        this.input = input;
        this.cost = cost;
        this.result = result;
    }

    public Ingredient input() { return input; }
    public MachineCost cost() { return cost; }
    public ItemStackTemplate result() { return result; }

    // The item matches and the pedestal has the right fluid (whether there's enough is checked when it runs)
    @Override
    public boolean matches(PedestalRecipeInput recipeInput, Level level)
    {
        return input.test(recipeInput.item()) && cost.fluidMatches(recipeInput.pedestal());
    }

    @Override
    public RecipeSerializer<BottlerRecipe> getSerializer()
    {
        return PedestalRecipes.BOTTLER_SERIALIZER.get();
    }

    @Override
    public RecipeType<BottlerRecipe> getType()
    {
        return PedestalRecipes.BOTTLER.get();
    }
}
