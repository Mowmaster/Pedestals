package com.mowmaster.pedestals.network;

import com.mowmaster.pedestals.references.Reference;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

//For learning and testing:
//https://github.com/baileyholl/Ars-Nouveau/blob/0cdb8fbb483ca0f945de26c633955cfb1c05c925/src/main/java/com/hollingsworth/arsnouveau/common/network/Networking.java#L87
public class PacketHandler {
    public static SimpleChannel INSTANCE;

    private static int ID = 0;
    public static int nextID(){return ID++;}
    public static void registerMessages(){
        ////System.out.println("Registering packets!!");
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Reference.MODID, "network"), () -> "1.0", s->true, s->true);

        INSTANCE.registerMessage(nextID(),
                PacketParticles.class,
                PacketParticles::encode,
                PacketParticles::decode,
                PacketParticles.Handler::handle);
    }

    public static void sendToNearby(World world, BlockPos pos, Object toSend){
        if (world instanceof ServerWorld) {
            ServerWorld ws = (ServerWorld) world;
            ws.getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false)
                    .filter(p -> p.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64)
                    .forEach(p -> INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), toSend));
        }
    }

    public static void sendToNearby(World world, Entity e, Object toSend) {
        sendToNearby(world, e.getPosition(), toSend);
    }
}
