package com.mowmaster.pedestals.recipes;


import com.mowmaster.pedestals.references.Reference;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class PedestalsSerializers {

    public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Reference.MODID);

    public static final RegistryObject<IRecipeSerializer<?>> SMELTING_ADVANCED = RECIPES.register("pedestal_smelting_advanced", () ->
            SmeltingRecipeAdvanced.serializer);

    //registry name is what type should be used in json recipes
    public static final RegistryObject<IRecipeSerializer<?>> CRUSHING = RECIPES.register("pedestal_crushing", () ->
            CrusherRecipe.serializer);

    public static final RegistryObject<IRecipeSerializer<?>> CRUSHING_ADVANCED = RECIPES.register("pedestal_crushing_advanced", () ->
            CrusherRecipeAdvanced.serializer);

    public static final RegistryObject<IRecipeSerializer<?>> SAWING = RECIPES.register("pedestal_sawing", () ->
            SawMillRecipe.serializer);

    public static final RegistryObject<IRecipeSerializer<?>> SAWING_ADVANCED = RECIPES.register("pedestal_sawing_advanced", () ->
            SawMillRecipeAdvanced.serializer);

    public static final RegistryObject<IRecipeSerializer<?>> COBBLEGEN = RECIPES.register("pedestal_cobblegen", () ->
            CobbleGenRecipe.serializer);

    public static final RegistryObject<IRecipeSerializer<?>> COBBLEGEN_SILK = RECIPES.register("pedestal_cobblegensilk", () ->
            CobbleGenSilkRecipe.serializer);

    public static final RegistryObject<IRecipeSerializer<?>> COLORING = RECIPES.register("pedestal_coloring", () ->
            ColoredPedestalRecipe.serializer);

    public static final RegistryObject<IRecipeSerializer<?>> RECYCLER = RECIPES.register("pedestal_recycler", () ->
            RecyclerRecipe.serializer);

    public static final RegistryObject<IRecipeSerializer<?>> FLUIDTOXPCONVERTER = RECIPES.register("pedestal_fluid_to_xp", () ->
            FluidtoExpConverterRecipe.serializer);
}
