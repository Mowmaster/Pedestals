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

// Material generator (1.20's "cobblegen") upgrade: the block under the pedestal decides what's made. The result is
// a block, and the pedestal gets what mining it would drop with the pedestal's tool (stone -> cobblestone).
//   {"type": "pedestals:cobblegen", "block_below": "minecraft:crying_obsidian",
//    "fluid": {"ingredient": "minecraft:lava", "amount": 1000}, "result": {"id": "minecraft:obsidian"}}
public final class MaterialGeneratorRecipe extends PedestalMachineRecipe<PedestalRecipeInput>
{
    public static final MapCodec<MaterialGeneratorRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("block_below").forGetter(MaterialGeneratorRecipe::blockBelow),
            MachineCost.MAP_CODEC.forGetter(MaterialGeneratorRecipe::cost),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(MaterialGeneratorRecipe::result))
            .apply(instance, MaterialGeneratorRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MaterialGeneratorRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, MaterialGeneratorRecipe::blockBelow,
            MachineCost.STREAM_CODEC, MaterialGeneratorRecipe::cost,
            ItemStackTemplate.STREAM_CODEC, MaterialGeneratorRecipe::result,
            MaterialGeneratorRecipe::new);

    private final Ingredient blockBelow;
    private final MachineCost cost;
    private final ItemStackTemplate result;

    public MaterialGeneratorRecipe(Ingredient blockBelow, MachineCost cost, ItemStackTemplate result)
    {
        this.blockBelow = blockBelow;
        this.cost = cost;
        this.result = result;
    }

    public Ingredient blockBelow() { return blockBelow; }
    public MachineCost cost() { return cost; }
    public ItemStackTemplate result() { return result; }

    // The input's item is the block below as an item
    @Override
    public boolean matches(PedestalRecipeInput recipeInput, Level level)
    {
        return blockBelow.test(recipeInput.item());
    }

    @Override
    public RecipeSerializer<MaterialGeneratorRecipe> getSerializer()
    {
        return PedestalRecipes.MATERIAL_GENERATOR_SERIALIZER.get();
    }

    @Override
    public RecipeType<MaterialGeneratorRecipe> getType()
    {
        return PedestalRecipes.MATERIAL_GENERATOR.get();
    }
}
