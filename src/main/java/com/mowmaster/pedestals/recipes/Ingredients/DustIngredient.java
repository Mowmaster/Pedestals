package com.mowmaster.pedestals.Recipes.Ingredients;

import com.google.gson.JsonObject;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class DustIngredient {

    private static final String KEY_DUST = "dust";
    private static final int COLOR_DEFAULT = -1;
    private static final int AMOUNT_DEFAULT = 0;
    private final DustMagic dustMagic;

    public DustIngredient(int color, int amount) {
        dustMagic = (new DustMagic(color,amount) == null) ? DustMagic.EMPTY : new DustMagic(color,amount);
    }

    public DustIngredient(DustMagic magic) {
        dustMagic = (magic == null) ? DustMagic.EMPTY : magic;
    }

    public DustMagic getDustMagic() {
        return dustMagic;
    }

    public boolean hasDust() {
        return !dustMagic.isEmpty();
    }

    public static DustIngredient parseData(final JsonObject recipeJson) {
        if (!recipeJson.has(KEY_DUST)) {
            return new DustIngredient(COLOR_DEFAULT,AMOUNT_DEFAULT);
        }
        else if (recipeJson.get(KEY_DUST).isJsonObject()) {
            JsonObject dustJson = recipeJson.get(KEY_DUST).getAsJsonObject();
            return new DustIngredient(dustJson.get("dustColor").getAsInt(),dustJson.get("dustAmount").getAsInt());
        }
        else {
            return new DustIngredient(COLOR_DEFAULT,AMOUNT_DEFAULT);
        }
    }

    public List<DustMagic> getMatchingDust() {
        List<DustMagic> me = new ArrayList<>();
        return me;
    }

    public static DustIngredient readFromPacket(FriendlyByteBuf buffer) {
        return new DustIngredient(buffer.readInt(), buffer.readInt());
    }

    public void writeToPacket(FriendlyByteBuf buf)
    {
        buf.writeVarInt(dustMagic.getDustColor());
        buf.writeVarInt(dustMagic.getDustAmount());
    }
}
