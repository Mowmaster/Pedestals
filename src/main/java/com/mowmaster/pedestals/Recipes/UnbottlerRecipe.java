package com.mowmaster.pedestals.Recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

// Unbottler upgrade: empties an item from the inventory below into the pedestal (water bucket -> 1000mB water
// + an empty bucket, bottle o' enchanting -> experience). The result item is optional.
//   {"type": "pedestals:unbottler", "input": "minecraft:water_bucket", "fluid": {"id": "minecraft:water", "amount": 1000},
//    "result": {"id": "minecraft:bucket"}}
public final class UnbottlerRecipe extends PedestalMachineRecipe<PedestalRecipeInput>
{
    public static final MapCodec<UnbottlerRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(UnbottlerRecipe::input),
            MachineReturn.MAP_CODEC.forGetter(UnbottlerRecipe::returned),
            ItemStackTemplate.CODEC.optionalFieldOf("result").forGetter(UnbottlerRecipe::result))
            .apply(instance, UnbottlerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnbottlerRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, UnbottlerRecipe::input,
            MachineReturn.STREAM_CODEC, UnbottlerRecipe::returned,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), UnbottlerRecipe::result,
            UnbottlerRecipe::new);

    private final Ingredient input;
    private final MachineReturn returned;
    private final Optional<ItemStackTemplate> result;

    public UnbottlerRecipe(Ingredient input, MachineReturn returned, Optional<ItemStackTemplate> result)
    {
        this.input = input;
        this.returned = returned;
        this.result = result;
    }

    public Ingredient input() { return input; }
    public MachineReturn returned() { return returned; }
    public Optional<ItemStackTemplate> result() { return result; }

    @Override
    public boolean matches(PedestalRecipeInput recipeInput, Level level)
    {
        return input.test(recipeInput.item());
    }

    @Override
    public RecipeSerializer<UnbottlerRecipe> getSerializer()
    {
        return PedestalRecipes.UNBOTTLER_SERIALIZER.get();
    }

    @Override
    public RecipeType<UnbottlerRecipe> getType()
    {
        return PedestalRecipes.UNBOTTLER.get();
    }
}
