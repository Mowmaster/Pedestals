package com.mowmaster.pedestals.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.Optional;

// What the XP anvil's recipes charge (anvil_overlevel and anvil_conflict share it). In recipe JSON these fields sit
// next to the recipe's own:
//   "ingredients": [{"ingredient": "minecraft:netherite_ingot", "count": 1, "levels": "5-10"},
//                   {"ingredient": "minecraft:nether_star", "levels": "11"}],
//   "fluid": {"ingredient": "minecraft:lava", "amount": 1000}, "energy": 0, "experience_levels": 5
// The items, fluid and energy come from the anvil's input pedestals, the experience from the anvil pedestal.
// "levels" (optional) says at which enchantment levels an ingredient is needed: "5-10" (5 to 10), "11" or 11
// (11 and above). Without it the ingredient is always needed. All the fields are optional.
public record AnvilCost(List<AnvilIngredient> ingredients, Optional<SizedFluidIngredient> fluid, int energy, int experienceLevels)
{
    public static final MapCodec<AnvilCost> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AnvilIngredient.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(AnvilCost::ingredients),
            SizedFluidIngredient.CODEC.optionalFieldOf("fluid").forGetter(AnvilCost::fluid),
            Codec.INT.optionalFieldOf("energy", 0).forGetter(AnvilCost::energy),
            Codec.INT.optionalFieldOf("experience_levels", 0).forGetter(AnvilCost::experienceLevels))
            .apply(instance, AnvilCost::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AnvilCost> STREAM_CODEC = StreamCodec.composite(
            AnvilIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), AnvilCost::ingredients,
            ByteBufCodecs.optional(SizedFluidIngredient.STREAM_CODEC), AnvilCost::fluid,
            ByteBufCodecs.VAR_INT, AnvilCost::energy,
            ByteBufCodecs.VAR_INT, AnvilCost::experienceLevels,
            AnvilCost::new);

    public AnvilCost
    {
        ingredients = List.copyOf(ingredients);
    }

    // The ingredients needed when the enchantment ends up at this level
    public List<AnvilIngredient> ingredientsAt(int level)
    {
        return ingredients.stream().filter(ingredient -> ingredient.levels().isEmpty() || ingredient.levels().get().contains(level)).toList();
    }

    public record AnvilIngredient(Ingredient ingredient, int count, Optional<LevelRange> levels)
    {
        public static final Codec<AnvilIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(AnvilIngredient::ingredient),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("count", 1).forGetter(AnvilIngredient::count),
                LevelRange.CODEC.optionalFieldOf("levels").forGetter(AnvilIngredient::levels))
                .apply(instance, AnvilIngredient::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AnvilIngredient> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, AnvilIngredient::ingredient,
                ByteBufCodecs.VAR_INT, AnvilIngredient::count,
                ByteBufCodecs.optional(LevelRange.STREAM_CODEC), AnvilIngredient::levels,
                AnvilIngredient::new);
    }

    // min to max, inclusive. An open range ("11") has max = Integer.MAX_VALUE.
    public record LevelRange(int min, int max)
    {
        // Written as "5-10" or "11"; a plain number (11) is read too. comapFlatMap is "xmap that can fail":
        // DataResult.error explains a bad value in the log instead of crashing.
        private static final Codec<LevelRange> STRING_CODEC = Codec.STRING.comapFlatMap(LevelRange::parse, LevelRange::toText);
        public static final Codec<LevelRange> CODEC = Codec.withAlternative(STRING_CODEC, Codec.INT.xmap(min -> new LevelRange(min, Integer.MAX_VALUE), LevelRange::min));

        public static final StreamCodec<RegistryFriendlyByteBuf, LevelRange> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, LevelRange::min,
                ByteBufCodecs.VAR_INT, LevelRange::max,
                LevelRange::new);

        public boolean contains(int level)
        {
            return level >= min && level <= max;
        }

        private static DataResult<LevelRange> parse(String text)
        {
            try
            {
                String[] parts = text.trim().split("-", -1);
                if (parts.length == 1) return DataResult.success(new LevelRange(Integer.parseInt(parts[0].trim()), Integer.MAX_VALUE));
                if (parts.length == 2)
                {
                    int min = Integer.parseInt(parts[0].trim());
                    int max = parts[1].isBlank() ? Integer.MAX_VALUE : Integer.parseInt(parts[1].trim());
                    if (min <= max) return DataResult.success(new LevelRange(min, max));
                }
            }
            catch (NumberFormatException ignored)
            {
            }
            return DataResult.error(() -> "Not a level range (\"5-10\" or \"11\"): " + text);
        }

        private String toText()
        {
            return max == Integer.MAX_VALUE ? String.valueOf(min) : min + "-" + max;
        }
    }
}
