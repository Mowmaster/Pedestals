package com.mowmaster.pedestals.ClientSide;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

// The client-only half of the transport types' transfer particles. Only ever called on the client, from
// PedestalTransportType#addTransferParticles overrides, so the client classes used here never load on a server.
public final class ClientTransferParticles
{
    private ClientTransferParticles()
    {
    }

    // Bubbles in the fluid's colour (the same tint NeoForge gives buckets and tanks). Fluids without a tint (lava has
    // its colour in the texture) use the fluid block's own particles instead, which carry the texture.
    public static void fluid(Level level, Vec3 from, Vec3 to, FluidResource resource, RandomSource random)
    {
        FluidState state = resource.getFluid().defaultFluidState();
        FluidTintSource tint = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state).fluidTintSource();
        int color = tint != null ? tint.colorAsStack(resource.toStack(1000)) : -1;
        Vec3 direction = to.subtract(from).normalize().scale(0.08);
        for (int bubble = 0; bubble < 10; bubble++)
        {
            Vec3 at = from.lerp(to, random.nextDouble()).add(random.nextGaussian() * 0.06, random.nextGaussian() * 0.06, random.nextGaussian() * 0.06);
            if (color != -1 && (color & 0xFFFFFF) != 0xFFFFFF)
            {
                level.addParticle(new DustParticleOptions(color & 0xFFFFFF, 0.6F + random.nextFloat() * 0.6F), at.x, at.y, at.z, direction.x, direction.y, direction.z);
            }
            else
            {
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state.createLegacyBlock()), at.x, at.y, at.z, direction.x, direction.y, direction.z);
            }
        }
    }
}
