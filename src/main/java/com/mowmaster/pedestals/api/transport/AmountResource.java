package com.mowmaster.pedestals.api.transport;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;

// The "resource" of a type that is only an amount (energy, experience): there's one non-empty value per type,
// so these types can use the same ResourceHandler machinery (transactions, moving) as items and fluids.
// A record is a short way to write a class whose fields are final and that gets equals/hashCode/toString for free.
public record AmountResource(@Nullable Identifier id) implements Resource
{
    public static final AmountResource EMPTY = new AmountResource(null);

    @Override
    public boolean isEmpty()
    {
        return id == null;
    }
}
