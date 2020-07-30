package com.mowmaster.pedestals.recipes;

import com.google.gson.JsonObject;
import com.mowmaster.pedestals.references.Reference;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class CrusherRecipe implements IRecipe<IInventory> {

    public static final IRecipeType<CrusherRecipe> recipeType = IRecipeType.register(Reference.MODID + ":crusher");
    public static final Serializer serializer = new Serializer();

    private final ResourceLocation recipeId;
    private Ingredient ingredient;
    private ItemStack result;

    public CrusherRecipe(ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        ItemStack stack = inv.getStackInSlot(0);
        return ingredient.test(stack);
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return recipeId;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Override
    public IRecipeType<?> getType() {
        return recipeType;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CrusherRecipe> {

        public static ArrayList<Item> ingredientList = new ArrayList<>();

        @Override
        public CrusherRecipe read(ResourceLocation recipeId, JsonObject json) {

            CrusherRecipe recipe = new CrusherRecipe(recipeId);

            recipe.ingredient = Ingredient.deserialize(json.get("ingredient"));
            //recipe.ingredientCount = JSONUtils.getInt(json.get("ingredient").getAsJsonObject(), "count", 1);

            for (ItemStack stack : recipe.ingredient.getMatchingStacks()) {
                if (!ingredientList.contains(stack.getItem())) ingredientList.add(stack.getItem());
            }

            ResourceLocation itemResourceLocation = ResourceLocation.create(JSONUtils.getString(json.get("result").getAsJsonObject(), "item", "minecraft:empty"), ':');
            int itemAmount = JSONUtils.getInt(json.get("result").getAsJsonObject(), "count", 0);
            recipe.result = new ItemStack(ForgeRegistries.ITEMS.getValue(itemResourceLocation), itemAmount);

            return recipe;
        }

        @Nullable
        @Override
        public CrusherRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            CrusherRecipe recipe = new CrusherRecipe(recipeId);
            recipe.ingredient = Ingredient.read(buffer);
            recipe.result = buffer.readItemStack();
            return recipe;
        }

        @Override
        public void write(PacketBuffer buffer, CrusherRecipe recipe) {
            recipe.ingredient.write(buffer);
            buffer.writeItemStack(recipe.getResult());
        }
    }
}