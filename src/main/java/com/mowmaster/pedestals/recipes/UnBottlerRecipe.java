package com.mowmaster.pedestals.Recipes;

import com.google.gson.JsonObject;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.pedestals.PedestalUtils.RecipeUtil;
import com.mowmaster.pedestals.Recipes.Ingredients.DustIngredient;
import com.mowmaster.pedestals.Recipes.Ingredients.EnergyIngredient;
import com.mowmaster.pedestals.Recipes.Ingredients.ExperienceIngredient;
import com.mowmaster.pedestals.Recipes.Ingredients.FluidTagIngredient;
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
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class UnBottlerRecipe implements Recipe<Container>
{
    @ObjectHolder(registryName = "forge:recipe_serializer", value = MODID + ":unbottler")

    private final String group;
    private final ResourceLocation id;
    @Nullable
    private final Ingredient inputStack;
    public final FluidTagIngredient fluidIng;
    private final EnergyIngredient energy;
    private final ExperienceIngredient experience;
    private final DustIngredient dust;
    private final ItemStack generatedItemOrBlock;

    public UnBottlerRecipe(ResourceLocation id, String group, @Nullable Ingredient inputStack, @Nullable FluidTagIngredient fluidIng, @Nullable EnergyIngredient energy, @Nullable ExperienceIngredient experience, @Nullable DustIngredient dust, ItemStack generatedItemOrBlock)
    {
        this.group = group;
        this.id = id;
        this.inputStack = inputStack;
        this.fluidIng = fluidIng;
        this.energy = energy;
        this.experience = experience;
        this.dust = dust;
        this.generatedItemOrBlock = generatedItemOrBlock;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static Collection<UnBottlerRecipe> getAllRecipes(Level world)
    {
        return world.getRecipeManager().getAllRecipesFor(UnBottlerRecipe.Type.INSTANCE);
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
        allIngredients.add(inputStack != null ? inputStack : Ingredient.EMPTY);
        return allIngredients;
    }


    @Override
    public boolean matches(Container p_44002_, Level p_44003_) {
        Container cont = p_44002_;
        if(inputStack == null)return false;
        //Dont allow recipes that dont return anything to be allowed
        if(fluidIng == null && energy == null && experience  == null && dust  == null) { return false; }
        if((fluidIng != null && fluidIng.getFluidStack().isEmpty()) && energy.getEnergyNeeded()<=0 && experience.getExperienceRequired()<=0 && dust.getDustMagic().isEmpty())return false;

        //Dont check for the result item, we can allow that to be blank if necessary
        if(inputStack.test(cont.getItem(0)))return true;
        else return false;
    }

    @Override
    public ItemStack assemble(Container p_44001_) {
        return getResultItem().copy();
    }

    @Override
    public ItemStack getResultItem()
    {
        if(generatedItemOrBlock != null || !generatedItemOrBlock.isEmpty())return generatedItemOrBlock;
        return ItemStack.EMPTY;
    }

    public FluidStack getFluidReturned()
    {
        if(fluidIng != null)return fluidIng.getFluidStack();
        else return FluidStack.EMPTY;
    }

    public int getEnergyReturned()
    {
        return energy.getEnergyNeeded();
    }

    public int getExperienceReturned()
    {
        return experience.getExperienceRequired();
    }

    public DustMagic getDustReturned()
    {
        return dust.getDustMagic();
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return UnBottlerRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return UnBottlerRecipe.Type.INSTANCE;
    }

    public static class Type implements RecipeType<UnBottlerRecipe> {
        private Type() { }
        public static final UnBottlerRecipe.Type INSTANCE = new UnBottlerRecipe.Type();
        public static final String ID = "unbottler";
    }

    @Override
    public ItemStack getToastSymbol()
    {
        return new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_UNBOTTLER.get());
    }

    public FluidStack getFluidStack() { return fluidIng.getFluidStack(); }

    public int getEnergy()
    {
        return energy.getEnergyNeeded();
    }

    public int getExperience()
    {
        return experience.getExperienceRequired();
    }

    public DustMagic getDustMagic()
    {
        return dust.getDustMagic();
    }

    public Ingredient getPattern()
    {
        return inputStack != null ? inputStack : Ingredient.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<UnBottlerRecipe> {
        public static final UnBottlerRecipe.Serializer INSTANCE = new UnBottlerRecipe.Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID,"unbottler");

        protected UnBottlerRecipe createRecipe(ResourceLocation recipeId, String group, @Nullable Ingredient inputStack, @Nullable FluidTagIngredient fluidIng, @Nullable EnergyIngredient energy, @Nullable ExperienceIngredient experience, @Nullable DustIngredient dust, ItemStack generatedItemOrBlock)
        {
            return new UnBottlerRecipe(recipeId, group, inputStack, fluidIng, energy, experience, dust, generatedItemOrBlock);
        }

        @Override
        public UnBottlerRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient inputStack = json.has("inputStack") ? CraftingHelper.getIngredient(json.get("inputStack")) : null;
            FluidTagIngredient fluidTagIngredient = json.has("inputFluidStack") ? RecipeUtil.parseFluid(json,"inputFluidStack") : null;
            EnergyIngredient energyIngredient = new EnergyIngredient(json);
            ExperienceIngredient experienceIngredient = new ExperienceIngredient(json);
            DustIngredient dustIngredient = DustIngredient.parseData(json);
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            return createRecipe(recipeId, group, inputStack, fluidTagIngredient, energyIngredient, experienceIngredient, dustIngredient, result);
        }

        @Override
        public UnBottlerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);
            boolean hasInput = buffer.readBoolean();
            Ingredient inputStack = hasInput ? Ingredient.fromNetwork(buffer) : null;
            FluidTagIngredient ingredientFluid = FluidTagIngredient.readFromPacket(buffer);
            EnergyIngredient energyIngredient = new EnergyIngredient(buffer.readInt());
            ExperienceIngredient experienceIngredient = new ExperienceIngredient(buffer.readInt());
            DustIngredient dustIngredient = new DustIngredient(buffer.readInt(),buffer.readInt());
            ItemStack result = buffer.readItem();
            return createRecipe(recipeId, group, inputStack, ingredientFluid, energyIngredient, experienceIngredient, dustIngredient, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, UnBottlerRecipe recipe)
        {
            buffer.writeUtf(recipe.group);
            boolean hasInput = recipe.inputStack != null;
            buffer.writeBoolean(hasInput);
            recipe.fluidIng.writeToPacket(buffer);
            buffer.writeInt(recipe.energy.getEnergyNeeded());
            buffer.writeInt(recipe.experience.getExperienceRequired());
            buffer.writeInt(recipe.dust.getDustMagic().getDustColor());
            buffer.writeInt(recipe.dust.getDustMagic().getDustAmount());
            buffer.writeItem(recipe.generatedItemOrBlock);
        }

        public RecipeSerializer<?> setRegistryName(ResourceLocation name) {
            return INSTANCE;
        }

        @Nullable
        public ResourceLocation getRegistryName() {
            return ID;
        }

        public Class<RecipeSerializer<?>> getRegistryType() {
            return UnBottlerRecipe.Serializer.castClass(RecipeSerializer.class);
        }

        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>)cls;
        }
    }
}
