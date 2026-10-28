package com.mowmaster.pedestals.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Transport.EnergyTransportType;
import com.mowmaster.pedestals.Transport.ExperienceTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Optional;

// What a recipe gives back to the pedestal (the unbottler): a fluid, energy and experience. All optional.
//   "fluid": {"id": "minecraft:water", "amount": 1000}, "energy": 0, "experience": 7
public record MachineReturn(Optional<FluidStack> fluid, int energy, int experience)
{
    public static final MapCodec<MachineReturn> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FluidStack.CODEC.optionalFieldOf("fluid").forGetter(MachineReturn::fluid),
            Codec.INT.optionalFieldOf("energy", 0).forGetter(MachineReturn::energy),
            Codec.INT.optionalFieldOf("experience", 0).forGetter(MachineReturn::experience))
            .apply(instance, MachineReturn::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MachineReturn> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(FluidStack.STREAM_CODEC), MachineReturn::fluid,
            ByteBufCodecs.VAR_INT, MachineReturn::energy,
            ByteBufCodecs.VAR_INT, MachineReturn::experience,
            MachineReturn::new);

    public boolean isEmpty()
    {
        return fluid.isEmpty() && energy <= 0 && experience <= 0;
    }

    // Puts it all in the pedestal as part of the transaction; false if any of it doesn't fit
    public boolean give(BasePedestalBlockEntity pedestal, TransactionContext transaction)
    {
        if (fluid.isPresent() && !fluid.get().isEmpty())
        {
            FluidStack stack = fluid.get();
            if (pedestal.getStorage(PedestalTransportTypes.FLUIDS.get()).insert(FluidResource.of(stack), stack.getAmount(), transaction) < stack.getAmount()) return false;
        }
        if (energy > 0 && pedestal.getStorage(PedestalTransportTypes.ENERGY.get()).insert(EnergyTransportType.ENERGY, energy, transaction) < energy) return false;
        if (experience > 0 && pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get()).insert(ExperienceTransportType.EXPERIENCE, experience, transaction) < experience) return false;
        return true;
    }
}
