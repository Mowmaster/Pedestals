package com.mowmaster.pedestals.Items;

import com.mowmaster.pedestals.PedestalReferences;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ItemRegistry {

    public static final DeferredRegister.Items PEDESTAL_ITEMS = DeferredRegister.createItems(PedestalReferences.MODID);



    public static void register(IEventBus eventBus)
    {
        PEDESTAL_ITEMS.register(eventBus);
    }
}
