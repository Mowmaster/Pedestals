package com.mowmaster.pedestals.recipes;


import com.mowmaster.pedestals.references.Reference;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class PedestalsSerializers {

    public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Reference.MODID);

    //registry name is what type should be used in json recipes
    public static final RegistryObject<IRecipeSerializer<?>> CRUSHING = RECIPES.register("pedestal_crushing", () ->
            CrusherRecipe.serializer);

    public static final RegistryObject<IRecipeSerializer<?>> SAWING = RECIPES.register("pedestal_sawing", () ->
            SawMillRecipe.serializer);

    public static final RegistryObject<IRecipeSerializer<?>> COLORING = RECIPES.register("pedestal_coloring", () ->
            ColoredPedestalRecipe.serializer);
}
