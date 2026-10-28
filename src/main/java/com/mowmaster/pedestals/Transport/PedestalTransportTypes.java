package com.mowmaster.pedestals.Transport;

import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

// Our own registry, "pedestals:transport_type", so other mods can add transport types the same way they add items.
// Order in the registry is the order pedestals try types each transfer and filters cycle through modes.
public class PedestalTransportTypes
{
    public static final ResourceKey<Registry<PedestalTransportType<?>>> KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(PedestalReferences.MODID, "transport_type"));

    // sync(true): the server tells clients the ids, so both sides agree on every type
    public static final Registry<PedestalTransportType<?>> REGISTRY = new RegistryBuilder<>(KEY).sync(true).create();

    public static final DeferredRegister<PedestalTransportType<?>> TYPES = DeferredRegister.create(KEY, PedestalReferences.MODID);

    public static final DeferredHolder<PedestalTransportType<?>, ItemTransportType> ITEMS = TYPES.register("items", ItemTransportType::new);
    public static final DeferredHolder<PedestalTransportType<?>, FluidTransportType> FLUIDS = TYPES.register("fluids", FluidTransportType::new);
    public static final DeferredHolder<PedestalTransportType<?>, EnergyTransportType> ENERGY = TYPES.register("energy", EnergyTransportType::new);
    public static final DeferredHolder<PedestalTransportType<?>, ExperienceTransportType> EXPERIENCE = TYPES.register("experience", ExperienceTransportType::new);

    public static void register(IEventBus eventBus)
    {
        // The registry itself has to exist before anything (ours or another mod's) registers into it
        eventBus.addListener(NewRegistryEvent.class, event -> event.register(REGISTRY));
        TYPES.register(eventBus);
    }
}
