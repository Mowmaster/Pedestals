package com.mowmaster.pedestals.Configs;

import com.mowmaster.pedestals.pedestals;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

public class PedestalConfig
{
    public static class Common {


        public final ForgeConfigSpec.IntValue effectMaxPotency;



        Common(ForgeConfigSpec.Builder builder) {

            builder.comment("Potency Setting").push("Potency");

            effectMaxPotency = builder
                    .comment("Max Effect Potency")
                    .defineInRange("maxPotency", 5, 0, Integer.MAX_VALUE);
            builder.pop();
        }
    }

    public static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        pedestals.LOGGER.debug("Loaded Dust's config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
        pedestals.LOGGER.debug("Dust's config just got changed on the file system!");
    }
}
