package com.mowmaster.pedestals.Recipes;

import com.google.gson.JsonObject;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class CobbleGenRecipe implements Recipe<Container>
{
    @ObjectHolder(registryName = "forge:recipe_serializer", value = MODID + ":cobblegen")

    private final String group;
    private final ResourceLocation id;
    @Nullable
    private final Ingredient blockBelow;
    private final ItemStack generatedItemOrBlock;
    private final int energy;
    private final int experience;
    private final String fluidName;
    private final int fluid;
    private final int dustColor;
    private final int dustAmount;

    public CobbleGenRecipe(ResourceLocation id, String group, @Nullable Ingredient blockBelow, ItemStack generatedItemOrBlock, @Nullable int energy, @Nullable int experience, @Nullable String fluidName, @Nullable int fluid, @Nullable int dustColor, @Nullable int dustAmount)
    {
        this.group = group;
        this.id = id;
        this.blockBelow = blockBelow;
        this.generatedItemOrBlock = generatedItemOrBlock;
        this.energy = energy;
        this.experience = experience;
        this.fluidName = fluidName;
        this.fluid = fluid;
        this.dustColor = dustColor;
        this.dustAmount = dustAmount;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static Collection<CobbleGenRecipe> getAllRecipes(Level world)
    {
        return world.getRecipeManager().getAllRecipesFor(CobbleGenRecipe.Type.INSTANCE);
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
        return (inputStack.isEmpty() && blockBelow==null)?(true):(blockBelow.test(inputStack));
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
        return getEnergy();
    }

    public int getResultExperienceNeeded()
    {
        return getExperience();
    }

    public FluidStack getResultFluidNeeded()
    {
        return getFluidStack();
    }

    public DustMagic getResultDustNeeded()
    {
        return getDustMagic();
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CobbleGenRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return CobbleGenRecipe.Type.INSTANCE;
    }

    public static class Type implements RecipeType<CobbleGenRecipe> {
        private Type() { }
        public static final CobbleGenRecipe.Type INSTANCE = new CobbleGenRecipe.Type();
        public static final String ID = "cobblegen";
    }

    @Override
    public ItemStack getToastSymbol()
    {
        return new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_COBBLEGEN.get());
    }

    public int getEnergy()
    {
        return energy >= 0 ? energy : 0;
    }

    public int getExperience()
    {
        return experience >= 0 ? experience : 0;
    }

    public FluidStack getFluidStack() { return (fluidName != "" && fluid > 0)?(new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName)),fluid)):(FluidStack.EMPTY); }

    public DustMagic getDustMagic()
    {
        DustMagic dust = new DustMagic(dustColor,dustAmount);
        return (!dust.isEmpty())?(dust):(DustMagic.EMPTY);
    }

    public Ingredient getPattern()
    {
        return blockBelow != null ? blockBelow : Ingredient.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<CobbleGenRecipe> {
        public static final CobbleGenRecipe.Serializer INSTANCE = new CobbleGenRecipe.Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID,"cobblegen");

        protected CobbleGenRecipe createRecipe(ResourceLocation recipeId, String group, Ingredient blockBelow, ItemStack result, @Nullable int energy, @Nullable int experience, @Nullable String fluidName, @Nullable int fluid, @Nullable int dustColor, @Nullable int dustAmount)
        {
            return new CobbleGenRecipe(recipeId, group, blockBelow, result, energy, experience, fluidName, fluid, dustColor, dustAmount);
        }

        @Override
        public CobbleGenRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient blockBelow = json.has("blockBelow") ? CraftingHelper.getIngredient(json.get("blockBelow")) : null;
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            int energy = json.has("energy") ? GsonHelper.getAsInt(json,"energy") : 0;
            int experience = json.has("experience") ? GsonHelper.getAsInt(json,"experience") : 0;
            String fluidName = json.has("fluidName") ? GsonHelper.getAsString(json,"fluidName") : "";
            int fluid = json.has("fluid") ? GsonHelper.getAsInt(json,"fluid") : 0;
            int dustColor = json.has("dustcolor") ? GsonHelper.getAsInt(json,"dustcolor") : 0;
            int dustAmount = json.has("dustamount") ? GsonHelper.getAsInt(json,"dustamount") : 0;
            return createRecipe(recipeId, group, blockBelow, result, energy, experience, fluidName, fluid, dustColor, dustAmount);
        }

        @Override
        public CobbleGenRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);
            boolean hasInput = buffer.readBoolean();
            Ingredient blockBelow = hasInput ? Ingredient.fromNetwork(buffer) : null;
            ItemStack result = buffer.readItem();
            int energy = buffer.readInt();
            int experience = buffer.readInt();
            String fluidName = buffer.readUtf(32767);
            int fluid = buffer.readInt();
            int dustColor = buffer.readInt();
            int dustAmount = buffer.readInt();
            return createRecipe(recipeId, group,  blockBelow, result, energy, experience, fluidName, fluid, dustColor, dustAmount);
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
            buffer.writeInt(recipe.experience);
            buffer.writeUtf(recipe.fluidName);
            buffer.writeInt(recipe.fluid);
            buffer.writeInt(recipe.dustColor);
            buffer.writeInt(recipe.dustAmount);
        }

        public RecipeSerializer<?> setRegistryName(ResourceLocation name) {
            return INSTANCE;
        }

        @Nullable
        public ResourceLocation getRegistryName() {
            return ID;
        }

        public Class<RecipeSerializer<?>> getRegistryType() {
            return CobbleGenRecipe.Serializer.castClass(RecipeSerializer.class);
        }

        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>)cls;
        }
    }
}
