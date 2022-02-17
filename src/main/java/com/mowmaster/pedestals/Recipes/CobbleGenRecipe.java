package com.mowmaster.pedestals.Recipes;

import com.google.gson.JsonObject;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class CobbleGenRecipe implements Recipe<Container>
{
    @ObjectHolder(MODID + ":cobbleGen")
    public static RecipeSerializer<?> SERIALIZER = new Serializer();

    public static RecipeType<CobbleGenRecipe> COBBLE_GENERATOR = RecipeType.register(MODID + ":cobbleGen");

    private final String group;
    private final ResourceLocation id;
    @Nullable
    private final Ingredient blockBelow;
    private final ItemStack generatedItemOrBlock;
    private final int energy;
    private final int experience;
    private final String fluidName;
    private final int fluid;

    public CobbleGenRecipe(ResourceLocation id, String group, @Nullable Ingredient blockBelow, ItemStack generatedItemOrBlock, @Nullable int energy, @Nullable int experience, @Nullable String fluidName, @Nullable int fluid)
    {
        this.group = group;
        this.id = id;
        this.blockBelow = blockBelow;
        this.generatedItemOrBlock = generatedItemOrBlock;
        this.energy = energy;
        this.experience = experience;
        this.fluidName = fluidName;
        this.fluid = fluid;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static Collection<CobbleGenRecipe> getAllRecipes(Level world)
    {
        return world.getRecipeManager().getAllRecipesFor(COBBLE_GENERATOR);
    }

    @Override
    public String getGroup()
    {
        return group;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        NonNullList<Ingredient> allIngredients = NonNullList.create();
        allIngredients.add(blockBelow != null ? blockBelow : Ingredient.EMPTY);
        return allIngredients;
    }

    @Override
    public boolean matches(Container inv, Level worldIn)
    {
        ItemStack inputStack = inv.getItem(0);
        return blockBelow.test(inputStack);
    }

    @Override
    public ItemStack assemble(Container inv)
    {
        return getResultItem().copy();
    }

    @Override
    public ItemStack getResultItem()
    {
        return generatedItemOrBlock;
    }

    public int getResultEnergyNeeded()
    {
        return getColor();
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return COBBLE_GENERATOR;
    }

    @Override
    public ItemStack getToastSymbol()
    {
        return new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_COBBLEGEN.get());
    }

    public int getColor()
    {
        return energy >= 0 ? energy : 16777215;
    }

    public Ingredient getPattern()
    {
        return blockBelow != null ? blockBelow : Ingredient.EMPTY;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<CobbleGenRecipe>
    {
        protected CobbleGenRecipe createRecipe(ResourceLocation recipeId, String group, Ingredient blockBelow, int energy, ItemStack result)
        {
            return new CobbleGenRecipe(recipeId, group, blockBelow, energy, result);
        }

        @Override
        public CobbleGenRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient blockBelow = json.has("blockBelow") ? CraftingHelper.getIngredient(json.get("blockBelow")) : null;
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            int energy = json.has("energy") ? GsonHelper.getAsInt(json,"energy") : 16777215;
            return createRecipe(recipeId, group, blockBelow, energy, result);
        }

        @Override
        public CobbleGenRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);
            boolean hasInput = buffer.readBoolean();
            Ingredient blockBelow = hasInput ? Ingredient.fromNetwork(buffer) : null;
            ItemStack result = buffer.readItem();
            int energy = buffer.readInt();
            return createRecipe(recipeId, group,  blockBelow, energy, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CobbleGenRecipe recipe)
        {
            buffer.writeUtf(recipe.group);
            boolean hasInput = recipe.blockBelow != null;
            buffer.writeBoolean(hasInput);
            if (hasInput) recipe.blockBelow.toNetwork(buffer);
            buffer.writeItem(recipe.generatedItemOrBlock);
            buffer.writeInt(recipe.energy);
        }
    }
}
