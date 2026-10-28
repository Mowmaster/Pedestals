package com.mowmaster.pedestals.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import java.util.Optional;

// Fluid converter upgrade: turns fluid in the pedestal into an item and/or energy and experience (experience fluids
// from other mods become pedestal experience).
//   {"type": "pedestals:fluidconverter", "fluid": {"ingredient": "minecraft:lava", "amount": 1000}, "result": {"id": "minecraft:charcoal"}}
//   {"type": "pedestals:fluidconverter", "fluid": {"ingredient": "cofh_core:experience", "amount": 250}, "experience": 11}
public final class FluidConverterRecipe extends PedestalMachineRecipe<PedestalRecipeInput>
{
    public static final MapCodec<FluidConverterRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SizedFluidIngredient.CODEC.fieldOf("fluid").forGetter(FluidConverterRecipe::fluid),
            Codec.INT.optionalFieldOf("energy", 0).forGetter(FluidConverterRecipe::energy),
            Codec.INT.optionalFieldOf("experience", 0).forGetter(FluidConverterRecipe::experience),
            ItemStackTemplate.CODEC.optionalFieldOf("result").forGetter(FluidConverterRecipe::result))
            .apply(instance, FluidConverterRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidConverterRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedFluidIngredient.STREAM_CODEC, FluidConverterRecipe::fluid,
            ByteBufCodecs.VAR_INT, FluidConverterRecipe::energy,
            ByteBufCodecs.VAR_INT, FluidConverterRecipe::experience,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), FluidConverterRecipe::result,
            FluidConverterRecipe::new);

    private final SizedFluidIngredient fluid;
    private final int energy;
    private final int experience;
    private final Optional<ItemStackTemplate> result;

    public FluidConverterRecipe(SizedFluidIngredient fluid, int energy, int experience, Optional<ItemStackTemplate> result)
    {
        this.fluid = fluid;
        this.energy = energy;
        this.experience = experience;
        this.result = result;
    }

    public SizedFluidIngredient fluid() { return fluid; }
    public int energy() { return energy; }
    public int experience() { return experience; }
    public Optional<ItemStackTemplate> result() { return result; }

    // What the pedestal pays
    public MachineCost cost()
    {
        return new MachineCost(Optional.of(fluid), 0, 0);
    }

    // What the pedestal gets besides the item
    public MachineReturn returned()
    {
        return new MachineReturn(Optional.empty(), energy, experience);
    }

    @Override
    public boolean matches(PedestalRecipeInput recipeInput, Level level)
    {
        FluidResource stored = recipeInput.pedestal().getStorage(PedestalTransportTypes.FLUIDS.get()).getResource(0);
        return !stored.isEmpty() && fluid.ingredient().test(stored.toStack(fluid.amount()));
    }

    @Override
    public RecipeSerializer<FluidConverterRecipe> getSerializer()
    {
        return PedestalRecipes.FLUID_CONVERTER_SERIALIZER.get();
    }

    @Override
    public RecipeType<FluidConverterRecipe> getType()
    {
        return PedestalRecipes.FLUID_CONVERTER.get();
    }
}
