package com.mowmaster.pedestals.Recipes;

import com.mowmaster.pedestals.PedestalReferences;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// The pedestals' own recipe types (same ids as 1.20, so "type": "pedestals:bottler" etc. in recipe JSON).
// A RecipeType is only a name; a RecipeSerializer (a record of a JSON codec and a network codec) reads the JSON.
public class PedestalRecipes
{
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, PedestalReferences.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, PedestalReferences.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<BottlerRecipe>> BOTTLER = type("bottler");
    public static final DeferredHolder<RecipeType<?>, RecipeType<UnbottlerRecipe>> UNBOTTLER = type("unbottler");
    public static final DeferredHolder<RecipeType<?>, RecipeType<FluidConverterRecipe>> FLUID_CONVERTER = type("fluidconverter");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MaterialGeneratorRecipe>> MATERIAL_GENERATOR = type("cobblegen");
    public static final DeferredHolder<RecipeType<?>, RecipeType<UpgradeModificationRecipe>> UPGRADE_MODIFICATION = type("upgrademodification_global");
    public static final DeferredHolder<RecipeType<?>, RecipeType<EntityFilterRecipe>> ENTITY_FILTER = type("entity_filter");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BottlerRecipe>> BOTTLER_SERIALIZER =
            SERIALIZERS.register("bottler", () -> new RecipeSerializer<>(BottlerRecipe.MAP_CODEC, BottlerRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<UnbottlerRecipe>> UNBOTTLER_SERIALIZER =
            SERIALIZERS.register("unbottler", () -> new RecipeSerializer<>(UnbottlerRecipe.MAP_CODEC, UnbottlerRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FluidConverterRecipe>> FLUID_CONVERTER_SERIALIZER =
            SERIALIZERS.register("fluidconverter", () -> new RecipeSerializer<>(FluidConverterRecipe.MAP_CODEC, FluidConverterRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MaterialGeneratorRecipe>> MATERIAL_GENERATOR_SERIALIZER =
            SERIALIZERS.register("cobblegen", () -> new RecipeSerializer<>(MaterialGeneratorRecipe.MAP_CODEC, MaterialGeneratorRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<UpgradeModificationRecipe>> UPGRADE_MODIFICATION_SERIALIZER =
            SERIALIZERS.register("upgrademodification_global", () -> new RecipeSerializer<>(UpgradeModificationRecipe.MAP_CODEC, UpgradeModificationRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EntityFilterRecipe>> ENTITY_FILTER_SERIALIZER =
            SERIALIZERS.register("entity_filter", () -> new RecipeSerializer<>(EntityFilterRecipe.MAP_CODEC, EntityFilterRecipe.STREAM_CODEC));

    // RecipeType has no methods to implement, so "new RecipeType<>() {...}" makes one on the spot (an anonymous
    // class); toString gives it a readable name in logs
    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> type(String name)
    {
        return TYPES.register(name, id -> new RecipeType<T>()
        {
            @Override
            public String toString()
            {
                return id.toString();
            }
        });
    }

    public static void register(IEventBus eventBus)
    {
        TYPES.register(eventBus);
        SERIALIZERS.register(eventBus);
    }
}
