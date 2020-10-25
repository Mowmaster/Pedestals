package com.mowmaster.pedestals.recipes;

import com.google.gson.JsonObject;
import com.mowmaster.pedestals.blocks.PedestalBlock;
import com.mowmaster.pedestals.crafting.CraftingPedestals;
import com.mowmaster.pedestals.item.ItemColorPallet;
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

public class ColoredPedestalRecipe implements IRecipe<IInventory> {

    public static final IRecipeType<ColoredPedestalRecipe> recipeType = IRecipeType.register(Reference.MODID + ":coloredpedestals");
    public static final Serializer serializer = new Serializer();

    private final ResourceLocation recipeId;
    private Ingredient ingredient;
    private int color;
    private ItemStack result;

    public ColoredPedestalRecipe(ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getColor() {
        return color;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        ItemStack stack = inv.getStackInSlot(0);
        int count = 0;
        if(stack.getTag().contains("color"))
        {
            count = stack.getTag().getInt("color");
        }
        stack.getCount();
        return ingredient.test(stack) && count >= color;
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        ItemStack stack = inv.getStackInSlot(0);
        int count = 0;
        if(stack.getItem() instanceof ItemColorPallet && stack.getTag().contains("color"))
        {
            count = stack.getTag().getInt("color");
        }
        return new ItemStack(CraftingPedestals.instance().getResult(count).getBlock());
    }

    @Override
    public boolean canFit(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(PedestalBlock.I_PEDESTAL_333);
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

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ColoredPedestalRecipe> {

        public static ArrayList<Item> ingredientList = new ArrayList<>();

        @Override
        public ColoredPedestalRecipe read(ResourceLocation recipeId, JsonObject json) {

            ColoredPedestalRecipe recipe = new ColoredPedestalRecipe(recipeId);

            recipe.ingredient = Ingredient.deserialize(json.get("ingredient"));
            recipe.color = JSONUtils.getInt(json.get("ingredient").getAsJsonObject(), "count", 1);

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
        public ColoredPedestalRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            ColoredPedestalRecipe recipe = new ColoredPedestalRecipe(recipeId);
            recipe.ingredient = Ingredient.read(buffer);
            recipe.color = buffer.readInt();
            recipe.result = buffer.readItemStack();
            return recipe;
        }

        @Override
        public void write(PacketBuffer buffer, ColoredPedestalRecipe recipe) {
            recipe.ingredient.write(buffer);
            buffer.writeInt(recipe.getColor());
            buffer.writeItemStack(recipe.getResult());
        }
    }
}