package com.mowmaster.pedestals.recipes.ingredients;

import com.google.gson.JsonObject;

public class EnergyIngredient {

    private static final String KEY_ENERGY = "energy";
    private static final int RFPT_DEFAULT = 0;
    private int energyNeeded;

    public EnergyIngredient(int rf) {
        setEnergyNeeded(rf);
    }

    public EnergyIngredient(final JsonObject recipeJson) {
        parseData(recipeJson);
    }

    private void parseData(final JsonObject recipeJson) {
        if (!recipeJson.has(KEY_ENERGY)) {
            setEnergyNeeded(RFPT_DEFAULT);
        }
        else if (recipeJson.get(KEY_ENERGY).isJsonObject()) {
            JsonObject energyJson = recipeJson.get(KEY_ENERGY).getAsJsonObject();
            setEnergyNeeded(energyJson.get("energyValue").getAsInt());
        }
        else {
            setEnergyNeeded(recipeJson.get(KEY_ENERGY).getAsInt());
        }
    }

    private void setEnergyNeeded(int rf) {
        this.energyNeeded = Math.max(0, rf); // not negative, can be zero for free cost
    }

    public int getEnergyNeeded() {
        return this.energyNeeded;
    }
}
