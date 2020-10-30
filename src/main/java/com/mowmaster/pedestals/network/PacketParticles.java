package com.mowmaster.pedestals.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.client.world.ClientWorld;

import java.util.function.Supplier;

//To learn how this works and for testing purposes:
//https://github.com/baileyholl/Ars-Nouveau/blob/0cdb8fbb483ca0f945de26c633955cfb1c05c925/src/main/java/com/hollingsworth/arsnouveau/common/network/PacketANEffect.java#L17

public class PacketParticles
{
    private final EffectType type;
    private final double x;
    private final double y;
    private final double z;
    private final int[] args;

    public PacketParticles(EffectType type, double x, double y, double z, int... args) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.args = args;
    }

    public PacketParticles(EffectType type, BlockPos pos, int... args){
        this(type, pos.getX(), pos.getY(), pos.getZ(), args);
    }

    public static PacketParticles decode(PacketBuffer buf) {
        EffectType type = EffectType.values()[buf.readByte()];
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        int[] args = new int[type.argCount];

        for (int i = 0; i < args.length; i++) {
            args[i] = buf.readVarInt();
        }
        return new PacketParticles(type, x, y, z, args);
    }

    public static void encode(PacketParticles msg, PacketBuffer buf) {
        buf.writeByte(msg.type.ordinal());
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);

        for (int i = 0; i < msg.type.argCount; i++) {
            buf.writeVarInt(msg.args[i]);
        }
    }

    public static class Handler {
        public static void handle(final PacketParticles message, final Supplier<NetworkEvent.Context> ctx) {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                ctx.get().setPacketHandled(true);
                return;
            }
            ctx.get().enqueueWork(new Runnable() {
                // Use anon - lambda causes classloading issues
                @Override
                public void run() {
                    Minecraft mc = Minecraft.getInstance();
                    ClientWorld world = mc.world;
                    switch (message.type){
                        case BONEMEAL:{
                            for(int i =0; i < 10; i++){
                                double d0 = message.x +0.5; //+ world.rand.nextFloat();
                                double d1 = message.y +1.2;//+ world.rand.nextFloat() ;
                                double d2 = message.z +.5 ; //+ world.rand.nextFloat();
                                world.addParticle(new RedstoneParticleData(0.0f,1.0f,0.0f,1.0f),d0, d1, d2, (world.rand.nextFloat() * 1 - 0.5)/3, (world.rand.nextFloat() * 1 - 0.5)/3, (world.rand.nextFloat() * 1 - 0.5)/3);
                            }
                            break;
                        }
                        case TICKED:{
                            for(int i =0; i < 10; i++){
                                double d0 = message.x +0.5; //+ world.rand.nextFloat();
                                double d1 = message.y +1.2;//+ world.rand.nextFloat() ;
                                double d2 = message.z +.5 ; //+ world.rand.nextFloat();
                                world.addParticle(new RedstoneParticleData(1.0f,1.0f,1.0f,1.0f),d0, d1, d2, (world.rand.nextFloat() * 1 - 0.5)/3, (world.rand.nextFloat() * 1 - 0.5)/3, (world.rand.nextFloat() * 1 - 0.5)/3);
                            }
                            break;
                        }
                    }

                };
            });
            ctx.get().setPacketHandled(true);

        }
    }
    public enum EffectType {
        BONEMEAL(0),
        TICKED(0)
        ;

        private final int argCount;

        EffectType(int argCount) {
            this.argCount = argCount;
        }
    }
}
