package com.mowmaster.pedestals.recipes;

import com.google.gson.JsonObject;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.pedestals.pedestalutils.RecipeUtil;
import com.mowmaster.pedestals.recipes.ingredients.DustIngredient;
import com.mowmaster.pedestals.recipes.ingredients.EnergyIngredient;
import com.mowmaster.pedestals.recipes.ingredients.ExperienceIngredient;
import com.mowmaster.pedestals.recipes.ingredients.FluidTagIngredient;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
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

import static com.mowmaster.pedestals.pedestalutils.References.MODID;

public class CobbleGenRecipe implements Recipe<Container>
{
    @ObjectHolder(registryName = "forge:recipe_serializer", value = MODID + ":cobblegen")

    private final String group;
    private final ResourceLocation id;
    @Nullable
    private final Ingredient blockBelow;
    private final ItemStack generatedItemOrBlock;
    public final FluidTagIngredient fluidIng;
    private final EnergyIngredient energy;
    private final ExperienceIngredient experience;
    private final DustIngredient dust;


    public CobbleGenRecipe(ResourceLocation id, String group, @Nullable Ingredient blockBelow, ItemStack generatedItemOrBlock, @Nullable FluidTagIngredient fluidIng, @Nullable EnergyIngredient energy, @Nullable ExperienceIngredient experience, @Nullable DustIngredient dust)
    {
        this.group = group;
        this.id = id;
        this.blockBelow = blockBelow;
        this.generatedItemOrBlock = generatedItemOrBlock;
        this.fluidIng = fluidIng;
        this.energy = energy;
        this.experience = experience;
        this.dust = dust;
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

    public FluidStack getFluidStack()
    {
        if(fluidIng != null)return fluidIng.getFluidStack();
        else return FluidStack.EMPTY;
    }

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
        return blockBelow != null ? blockBelow : Ingredient.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<CobbleGenRecipe> {
        public static final CobbleGenRecipe.Serializer INSTANCE = new CobbleGenRecipe.Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID,"cobblegen");

        protected CobbleGenRecipe createRecipe(ResourceLocation recipeId, String group, Ingredient blockBelow, ItemStack result, @Nullable FluidTagIngredient fluidIng, @Nullable EnergyIngredient energy, @Nullable ExperienceIngredient experience, @Nullable DustIngredient dust)
        {
            return new CobbleGenRecipe(recipeId, group, blockBelow, result, fluidIng, energy, experience, dust);
        }

        @Override
        public CobbleGenRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient blockBelow = json.has("blockBelow") ? CraftingHelper.getIngredient(json.get("blockBelow")) : null;
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            FluidTagIngredient fluidTagIngredient = json.has("inputFluidStack") ? RecipeUtil.parseFluid(json,"inputFluidStack") : null;
            EnergyIngredient energyIngredient = new EnergyIngredient(json);
            ExperienceIngredient experienceIngredient = new ExperienceIngredient(json);
            DustIngredient dustIngredient = DustIngredient.parseData(json);
            return createRecipe(recipeId, group, blockBelow, result, fluidTagIngredient, energyIngredient, experienceIngredient, dustIngredient);
        }

        @Override
        public CobbleGenRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);
            boolean hasInput = buffer.readBoolean();
            Ingredient blockBelow = hasInput ? Ingredient.fromNetwork(buffer) : null;
            ItemStack result = buffer.readItem();
            boolean hasInputFluid = buffer.readBoolean();
            FluidTagIngredient fluidTagIngredient = hasInputFluid ? FluidTagIngredient.readFromPacket(buffer) : null;
            EnergyIngredient energyIngredient = new EnergyIngredient(buffer.readInt());
            ExperienceIngredient experienceIngredient = new ExperienceIngredient(buffer.readInt());
            DustIngredient dustIngredient = new DustIngredient(buffer.readInt(),buffer.readInt());
            return createRecipe(recipeId, group,  blockBelow, result, fluidTagIngredient, energyIngredient, experienceIngredient, dustIngredient);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CobbleGenRecipe recipe)
        {
            buffer.writeUtf(recipe.group);
            boolean hasInput = recipe.blockBelow != null;
            buffer.writeBoolean(hasInput);
            if (hasInput) recipe.blockBelow.toNetwork(buffer);
            buffer.writeItem(recipe.generatedItemOrBlock);
            boolean hasInputFluid = recipe.fluidIng != null;
            buffer.writeBoolean(hasInputFluid);
            if (hasInputFluid) recipe.fluidIng.writeToPacket(buffer);
            buffer.writeInt(recipe.energy.getEnergyNeeded());
            buffer.writeInt(recipe.experience.getExperienceRequired());
            buffer.writeInt(recipe.dust.getDustMagic().getDustColor());
            buffer.writeInt(recipe.dust.getDustMagic().getDustAmount());
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
