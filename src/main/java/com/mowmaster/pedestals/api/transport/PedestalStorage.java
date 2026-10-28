package com.mowmaster.pedestals.api.transport;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;

// A pedestal's storage for one transport type. It's a normal NeoForge ResourceHandler (so the same object can be
// handed to pipes as a capability), plus saving and a couple of pedestal hooks.
// Storages should check the pedestal's filter themselves (PedestalStorageHost#filterAllows / #filterLimit) in
// isValid/insert/extract, so filters apply to pipes and hoppers as well as to pedestal links.
public interface PedestalStorage<R extends Resource> extends ResourceHandler<R>, ValueIOSerializable
{
    default boolean isEmpty()
    {
        return ResourceHandlerUtil.isEmpty(this);
    }

    // The pedestal is being removed: drop what makes sense (items spill, xp becomes orbs...)
    void onPedestalRemoved(Level level, BlockPos pos);

    // One line for the pedestal's info message, or null when empty
    @Nullable Component describe();
}
