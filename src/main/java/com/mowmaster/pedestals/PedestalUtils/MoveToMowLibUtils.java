package com.mowmaster.pedestals.PedestalUtils;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Optional;

public class MoveToMowLibUtils
{
    public static void modeBasedTextOutputPopup(Player player, int mode, boolean localized, String modid, List<String> modeTextList, List<ChatFormatting> modeColorList)
    {
        modeTextList.add(".error");
        modeColorList.add(ChatFormatting.DARK_RED);
        int getMode = (mode>=modeTextList.size())?(modeTextList.size()-1):(mode);
        MutableComponent type;
        if(localized) { type = Component.translatable(modid + modeTextList.get(getMode)); }
        else { type = Component.literal(modeTextList.get(getMode)); }
        type.withStyle(modeColorList.get(getMode));
        player.displayClientMessage(type, true);
    }

    public static void modeBasedTextOutputTooltip(int mode, boolean localized, String modid, List<String> modeTextList, ChatFormatting textColor, List<Component> comp)
    {
        modeTextList.add(".error");
        int getMode = (mode>=modeTextList.size())?(modeTextList.size()-1):(mode);
        MutableComponent type;
        if(localized) { type = Component.translatable(modid + modeTextList.get(getMode)); }
        else { type = Component.literal(modeTextList.get(getMode)); }
        type.withStyle(textColor);
        comp.add(type);
    }

    /*
     * Begin RecipeUtils
     */
    public static void resetCachedAbstractCooking(String modID, ItemStack stackToStoreNBT) {
        CompoundTag tag = stackToStoreNBT.getOrCreateTag();
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "cook_ingredient");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "cook_result");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "cook_time");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "cook_xp_gain");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "cook_cached");
    }

    private static boolean hasCachedAbstractCooking(String modID, ItemStack stackToStoreNBT) {
        return MowLibCompoundTagUtils.readBooleanFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_cached");
    }

    // NOTE: This SHOULD NOT be called if `hasCachedAbstractCooking` returns `false`.
    private static boolean cachedAbstractCookingHasSameInput(String modID, ItemStack stackToStoreNBT, ItemStack input) {
        ItemStack cachedInput = MowLibCompoundTagUtils.readItemStackFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_ingredient");
        return ItemHandlerHelper.canItemStacksStack(input, cachedInput) ||
            input.isEmpty() && cachedInput.isEmpty(); // ItemStack.EMPTY doesn't "stack" but we want to consider them a match
    }

    private static <T extends AbstractCookingRecipe> ItemStack getAndCacheAbstractCookingResult(String modID, Level level, ItemStack stackToStoreNBT, ItemStack input, RecipeType<T> recipeType) {
        Container container = new SimpleContainer(input);

        RecipeManager recipeManager = level.getRecipeManager();
        Optional<T> result = recipeManager.getRecipeFor(recipeType, container, level);
        if (result.isPresent()) {
            T recipe = result.get();
            ItemStack resultItem = recipe.getResultItem();
            CompoundTag tag = stackToStoreNBT.getOrCreateTag();
            MowLibCompoundTagUtils.writeItemStackToNBT(modID, tag, input, "cook_ingredient");
            MowLibCompoundTagUtils.writeItemStackToNBT(modID, tag, resultItem,"cook_result");
            MowLibCompoundTagUtils.writeIntegerToNBT(modID, tag, recipe.getCookingTime(), "cook_time");
            MowLibCompoundTagUtils.writeIntegerToNBT(modID, tag, Math.round(recipe.getExperience()), "cook_xp_gain");
            MowLibCompoundTagUtils.writeBooleanToNBT(modID, tag, true, "cook_cached");
            return resultItem;
        } else {
            MowLibCompoundTagUtils.writeBooleanToNBT(modID, stackToStoreNBT.getOrCreateTag(), false,"cook_cached");
            return ItemStack.EMPTY;
        }
    }

    public static <T extends AbstractCookingRecipe> ItemStack getAbstractCookingResult(String modID, Level level, ItemStack stackToStoreNBT, ItemStack ingredient, RecipeType<T> recipeType) {
        if (hasCachedAbstractCooking(modID, stackToStoreNBT) && cachedAbstractCookingHasSameInput(modID, stackToStoreNBT, ingredient)) {
            return MowLibCompoundTagUtils.readItemStackFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_result");
        }
        return getAndCacheAbstractCookingResult(modID, level, stackToStoreNBT, ingredient, recipeType);
    }

    // NOTE: This SHOULD NOT be called if `getAbstractCookingResult` returns `ItemStack.EMPTY`.
    public static int getXpGainFromCachedRecipe(String modID, ItemStack stackToStoreNBT)  {
        return MowLibCompoundTagUtils.readIntegerFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_xp_gain");
    }

    // NOTE: This SHOULD NOT be called if `getAbstractCookingResult` returns `ItemStack.EMPTY`.`
    public static int getCookTimeRequired(String modID, ItemStack stackToStoreNBT) {
        return MowLibCompoundTagUtils.readIntegerFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_time");
    }

    public static int getCookTimeElapsed(String modID, ItemStack stackToStoreNBT) {
        return MowLibCompoundTagUtils.readIntegerFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_time_elapsed");
    }
    public static void setCookTimeElapsed(String modID, ItemStack stackToStoreNBT, int timeElapsed) {
        MowLibCompoundTagUtils.writeIntegerToNBT(modID, stackToStoreNBT.getOrCreateTag(), timeElapsed, "cook_time_elapsed");
    }
    /*
     * End RecipeUtils
     */
}
