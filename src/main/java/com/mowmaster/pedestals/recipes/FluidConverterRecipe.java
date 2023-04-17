package com.mowmaster.pedestals.Recipes;

import com.google.gson.JsonObject;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibMultiContainer;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class FluidConverterRecipe implements Recipe<MowLibMultiContainer>
{
    @ObjectHolder(registryName = "forge:recipe_serializer", value = MODID + ":fluidconverter")

    private final String group;
    private final ResourceLocation id;
    public final FluidTagIngredient fluidIng;
    private final EnergyIngredient energy;
    private final ExperienceIngredient experience;
    private final DustIngredient dust;
    private final ItemStack generatedItemOrBlock;

    public FluidConverterRecipe(ResourceLocation id, String group, @Nullable FluidTagIngredient fluidIng, @Nullable EnergyIngredient energy, @Nullable ExperienceIngredient experience, @Nullable DustIngredient dust, @Nullable ItemStack generatedItemOrBlock)
    {
        this.group = group;
        this.id = id;
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

    public static Collection<FluidConverterRecipe> getAllRecipes(Level world)
    {
        return world.getRecipeManager().getAllRecipesFor(FluidConverterRecipe.Type.INSTANCE);
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
        allIngredients.add(Ingredient.EMPTY);
        return allIngredients;
    }


    @Override
    public boolean matches(MowLibMultiContainer p_44002_, Level p_44003_) {
        MowLibMultiContainer cont = p_44002_;
        //Dont allow recipes that dont return anything to be allowed
        //System.out.println(fluidIng == null && energy == null && experience == null && dust == null);
        if(fluidIng == null && energy == null && experience == null && dust == null && generatedItemOrBlock == null)return false;
        //System.out.println(energy.getEnergyNeeded()<=0 && experience.getExperienceRequired()<=0 && dust.getDustMagic().isEmpty() && generatedItemOrBlock.isEmpty());
        if(energy.getEnergyNeeded()<=0 && experience.getExperienceRequired()<=0 && dust.getDustMagic().isEmpty() && generatedItemOrBlock.isEmpty())return false;

        //System.out.println(fluidIng != null && !fluidIng.getFluidStack().isEmpty());
        if(fluidIng != null && !fluidIng.getFluidStack().isEmpty())
        {
            //System.out.println(cont.getFluidStack().isEmpty());
            //System.out.println(fluidIng.getFluidStack().isFluidEqual(cont.getFluidStack()));
            if(cont.getFluidStack().isEmpty())
            {
                return false;
            }
            else if(fluidIng.getFluidStack().isFluidEqual(cont.getFluidStack()))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack assemble(MowLibMultiContainer p_44001_) {
        return getResultItem().copy();
    }

    @Override
    public ItemStack getResultItem()
    {
        if(generatedItemOrBlock == null)return ItemStack.EMPTY;

        if(generatedItemOrBlock != null || !generatedItemOrBlock.isEmpty())return generatedItemOrBlock;
        return ItemStack.EMPTY;
    }

    public FluidStack getFluidRequired()
    {
        return fluidIng != null ? fluidIng.getFluidStack() : FluidStack.EMPTY;
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
        return FluidConverterRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return FluidConverterRecipe.Type.INSTANCE;
    }

    public static class Type implements RecipeType<FluidConverterRecipe> {
        private Type() { }
        public static final FluidConverterRecipe.Type INSTANCE = new FluidConverterRecipe.Type();
        public static final String ID = "fluidconverter";
    }

    @Override
    public ItemStack getToastSymbol()
    {
        return new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_FLUIDCONVERTER.get());
    }

    public FluidStack getFluidStack() { return fluidIng != null ? fluidIng.getFluidStack() : FluidStack.EMPTY; }

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

    public FluidStack getPattern()
    {
        return fluidIng != null ? fluidIng.getFluidStack() : FluidStack.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<FluidConverterRecipe> {
        public static final FluidConverterRecipe.Serializer INSTANCE = new FluidConverterRecipe.Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID,"fluidconverter");

        protected FluidConverterRecipe createRecipe(ResourceLocation recipeId, String group, @Nullable FluidTagIngredient fluidIng, @Nullable EnergyIngredient energy, @Nullable ExperienceIngredient experience, @Nullable DustIngredient dust, ItemStack generatedItemOrBlock)
        {
            return new FluidConverterRecipe(recipeId, group, fluidIng, energy, experience, dust, generatedItemOrBlock);
        }

        @Override
        public FluidConverterRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            FluidTagIngredient fluidTagIngredient = json.has("inputFluidStack") ? RecipeUtil.parseFluid(json,"inputFluidStack") : null;
            EnergyIngredient energyIngredient = new EnergyIngredient(json);
            ExperienceIngredient experienceIngredient = new ExperienceIngredient(json);
            DustIngredient dustIngredient = DustIngredient.parseData(json);
            ItemStack result = json.has("result") ? CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true) : null;
            return createRecipe(recipeId, group, fluidTagIngredient, energyIngredient, experienceIngredient, dustIngredient, result);
        }

        @Override
        public FluidConverterRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);
            boolean hasInputFluid = buffer.readBoolean();
            FluidTagIngredient ingredientFluid = hasInputFluid ? FluidTagIngredient.readFromPacket(buffer) : null;
            EnergyIngredient energyIngredient = new EnergyIngredient(buffer.readInt());
            ExperienceIngredient experienceIngredient = new ExperienceIngredient(buffer.readInt());
            DustIngredient dustIngredient = new DustIngredient(buffer.readInt(),buffer.readInt());
            boolean hasResult = buffer.readBoolean();
            ItemStack result = hasResult ? buffer.readItem() : null;
            return createRecipe(recipeId, group, ingredientFluid, energyIngredient, experienceIngredient, dustIngredient, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FluidConverterRecipe recipe)
        {
            buffer.writeUtf(recipe.group);
            boolean hasInputFluid = recipe.fluidIng != null;
            buffer.writeBoolean(hasInputFluid);
            if (hasInputFluid) recipe.fluidIng.writeToPacket(buffer);
            buffer.writeInt(recipe.energy.getEnergyNeeded());
            buffer.writeInt(recipe.experience.getExperienceRequired());
            buffer.writeInt(recipe.dust.getDustMagic().getDustColor());
            buffer.writeInt(recipe.dust.getDustMagic().getDustAmount());
            boolean hasResultItem = recipe.generatedItemOrBlock != null;
            buffer.writeBoolean(hasResultItem);
            if (hasResultItem) buffer.writeItem(recipe.generatedItemOrBlock);
        }

        public RecipeSerializer<?> setRegistryName(ResourceLocation name) {
            return INSTANCE;
        }

        @Nullable
        public ResourceLocation getRegistryName() {
            return ID;
        }

        public Class<RecipeSerializer<?>> getRegistryType() {
            return FluidConverterRecipe.Serializer.castClass(RecipeSerializer.class);
        }

        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>)cls;
        }
    }
}
