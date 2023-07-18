package com.mowmaster.pedestals.pedestalutils;

import com.google.gson.JsonObject;
import com.mowmaster.pedestals.recipes.ingredients.FluidTagIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class RecipeUtil {

    public static FluidTagIngredient parseFluid(JsonObject json, String key) {
        JsonObject mix = json.get(key).getAsJsonObject();
        int count = mix.get("count").getAsInt();
        if (count < 1) {
            count = 1;
        }
        FluidStack fluidstack = FluidStack.EMPTY;
        if (mix.has("fluid")) {
            String fluidId = mix.get("fluid").getAsString(); // JSONUtils.getString(mix, "fluid");
            ResourceLocation resourceLocation = new ResourceLocation(fluidId);
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);
            fluidstack = (fluid == null) ? FluidStack.EMPTY : new FluidStack(fluid, count);
        }
        String ftag = mix.has("tag") ? mix.get("tag").getAsString() : "";
        return new FluidTagIngredient(fluidstack, ftag, count);
    }

    public static FluidStack getFluid(JsonObject fluidJson) {
        fluidJson.has("fluidTag");//      String fluidTag = fluidJson.get("fluidTag").getAsString();
        String fluidId = GsonHelper.getAsString(fluidJson, "fluid");
        ResourceLocation resourceLocation = new ResourceLocation(fluidId);
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);
        int count = fluidJson.get("count").getAsInt();
        if (count < 1) {
            count = 1;
        }
        return new FluidStack(Objects.requireNonNull(fluid), count);
    }
}