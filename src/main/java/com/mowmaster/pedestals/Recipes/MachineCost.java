package com.mowmaster.pedestals.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Transport.EnergyTransportType;
import com.mowmaster.pedestals.Transport.ExperienceTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalStorage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Optional;

// What a machine recipe takes from the pedestal it runs in: a fluid (any fluid the ingredient accepts, e.g.
// "#c:water"), energy and experience. All optional. In recipe JSON these sit next to the recipe's other fields:
//   "fluid": {"ingredient": "minecraft:water", "amount": 250}, "energy": 100, "experience": 11
// (1.20 spelled them inputFluidStack / energy.energyValue / experience.experienceValue.)
public record MachineCost(Optional<SizedFluidIngredient> fluid, int energy, int experience)
{
    public static final MachineCost NONE = new MachineCost(Optional.empty(), 0, 0);

    public static final MapCodec<MachineCost> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SizedFluidIngredient.CODEC.optionalFieldOf("fluid").forGetter(MachineCost::fluid),
            Codec.INT.optionalFieldOf("energy", 0).forGetter(MachineCost::energy),
            Codec.INT.optionalFieldOf("experience", 0).forGetter(MachineCost::experience))
            .apply(instance, MachineCost::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MachineCost> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(SizedFluidIngredient.STREAM_CODEC), MachineCost::fluid,
            ByteBufCodecs.VAR_INT, MachineCost::energy,
            ByteBufCodecs.VAR_INT, MachineCost::experience,
            MachineCost::new);

    public boolean isFree()
    {
        return fluid.isEmpty() && energy <= 0 && experience <= 0;
    }

    // Does the pedestal hold the right fluid at all? (Recipes match on this, so a bottle picks the right recipe for
    // the fluid that's in the pedestal.)
    public boolean fluidMatches(BasePedestalBlockEntity pedestal)
    {
        if (fluid.isEmpty()) return true;
        FluidResource stored = pedestal.getStorage(PedestalTransportTypes.FLUIDS.get()).getResource(0);
        return !stored.isEmpty() && fluid.get().ingredient().test(stored.toStack(fluid.get().amount()));
    }

    // Takes times x the cost from the pedestal as part of the transaction; false if there isn't enough
    public boolean take(BasePedestalBlockEntity pedestal, int times, TransactionContext transaction)
    {
        if (fluid.isPresent())
        {
            PedestalStorage<FluidResource> fluids = pedestal.getStorage(PedestalTransportTypes.FLUIDS.get());
            FluidResource stored = fluids.getResource(0);
            int amount = fluid.get().amount() * times;
            if (!fluidMatches(pedestal) || fluids.extract(stored, amount, transaction) < amount) return false;
        }
        if (energy > 0 && pedestal.getStorage(PedestalTransportTypes.ENERGY.get()).extract(EnergyTransportType.ENERGY, energy * times, transaction) < energy * times) return false;
        if (experience > 0 && pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get()).extract(ExperienceTransportType.EXPERIENCE, experience * times, transaction) < experience * times) return false;
        return true;
    }

    // How many times over the pedestal could pay (up to max)
    public int timesAffordable(BasePedestalBlockEntity pedestal, int max)
    {
        int times = max;
        while (times > 0)
        {
            try (Transaction transaction = Transaction.openRoot())
            {
                if (take(pedestal, times, transaction)) return times; // closed without commit: only a check
            }
            times--;
        }
        return 0;
    }
}
