package com.mowmaster.ironpedestals.Transport;

import com.mowmaster.ironpedestals.IronPedestalReferences;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

// The transport types were ironpedestals:items, ironpedestals:fluids... before they moved to MowLib (mowlib:items...).
// Those ids are saved inside filters and storage packets, so they're kept as aliases of the MowLib ones. This
// register adds nothing to MowLib's transport type registry; it only carries the aliases into it.
public class PedestalTransportTypeAliases
{
    public static final DeferredRegister<TransportType<?>> ALIASES = DeferredRegister.create(TransportTypes.KEY, IronPedestalReferences.MODID);

    public static void register(IEventBus eventBus)
    {
        for (String id : List.of("items", "fluids", "energy", "experience"))
        {
            ALIASES.addAlias(Identifier.fromNamespaceAndPath(IronPedestalReferences.MODID, id), MowLibReferences.id(id));
        }
        ALIASES.register(eventBus);
    }
}
