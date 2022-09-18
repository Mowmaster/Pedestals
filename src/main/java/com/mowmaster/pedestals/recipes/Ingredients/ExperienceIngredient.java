package com.mowmaster.pedestals.Recipes.Ingredients;

import com.google.gson.JsonObject;

public class ExperienceIngredient {

    private static final String KEY_EXPERIENCE = "experience";
    private static final int RFPT_DEFAULT = 0;
    private int expNeeded;

    public ExperienceIngredient(int rf) {
        setExperience(rf);
    }

    public ExperienceIngredient(final JsonObject recipeJson) {
        parseData(recipeJson);
    }

    private void parseData(final JsonObject recipeJson) {
        if (!recipeJson.has(KEY_EXPERIENCE)) {
            setExperience(RFPT_DEFAULT);
        }
        else if (recipeJson.get(KEY_EXPERIENCE).isJsonObject()) {
            JsonObject experenceJson = recipeJson.get(KEY_EXPERIENCE).getAsJsonObject();
            setExperience(experenceJson.get("experienceValue").getAsInt());
        }
        else {
            setExperience(recipeJson.get(KEY_EXPERIENCE).getAsInt());
        }
    }

    private void setExperience(int rf) {
        this.expNeeded = Math.max(0, rf);
    }

    public int getExperienceRequired() {
        return expNeeded;
    }
}
