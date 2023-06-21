package com.mowmaster.pedestals.Recipes;

import com.google.gson.JsonObject;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMultiContainer;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class UpgradeModificationRecipe implements Recipe<Container>
{
    @ObjectHolder(registryName = "forge:recipe_serializer", value = MODID + ":upgrademodification")

    private final String group;
    private final ResourceLocation id;
    @Nullable
    private final Ingredient upgradeInput;
    private final Ingredient infusionInputIngredients;
    private final String resultModificationName;
    private final int resultModificationValue;
    private final int resultModificationMaxValue;



    //Someday add in a bool value to set as something fake players could do too or not???

    public UpgradeModificationRecipe(ResourceLocation id, String group, @Nullable Ingredient upgradeInput, @Nullable Ingredient infusionInputIngredients, String resultModificationName, int resultModificationValue, int resultModificationMaxValue)
    {
        this.group = group;
        this.id = id;
        this.upgradeInput = upgradeInput;
        this.infusionInputIngredients = infusionInputIngredients;
        this.resultModificationName = resultModificationName;
        this.resultModificationValue = resultModificationValue;
        this.resultModificationMaxValue = resultModificationMaxValue;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static Collection<UpgradeModificationRecipe> getAllRecipes(Level world)
    {
        return world.getRecipeManager().getAllRecipesFor(UpgradeModificationRecipe.Type.INSTANCE);
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
        allIngredients.add(upgradeInput != null ? upgradeInput : Ingredient.EMPTY);
        allIngredients.add(infusionInputIngredients != null ? infusionInputIngredients : Ingredient.EMPTY);
        return allIngredients;
    }

    @Override
    public boolean matches(Container inv, Level worldIn)
    {
        ItemStack stacks[] = infusionInputIngredients.getItems();
        //Immediate fail if input container is less then recipe size needed.
        if(inv.getContainerSize()<stacks.length)return false;
        int matchCounter = 0;
        for(int i=0;i< stacks.length;i++)
        {
            if(stacks[i].isEmpty() && inv.getItem(i+1).isEmpty()){
                matchCounter++;
            }
            else if(stacks[i].is(inv.getItem(i+1).getItem())){
                matchCounter++;
            }
        }
        //If we have more inv slots filled then needed, then fail recipe, if all slots are empty then it'll be fine.
        if(stacks.length < inv.getContainerSize())
        {
            for(int i=(stacks.length + 1);i < inv.getContainerSize();i++)
            {
                if(!inv.getItem(i).isEmpty())matchCounter++;
            }
        }

        return upgradeInput.test(inv.getItem(0)) && matchCounter==stacks.length;
    }

    @Override
    public ItemStack assemble(Container p_44001_, RegistryAccess p_267165_) {
        return getResultItem().copy();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        return new ItemStack(Items.BARRIER);
    }

    public ItemStack getResultItem() {
        return new ItemStack(Items.BARRIER);
    }

    public String getResultModificationName()
    {
        return resultModificationName;
    }
    public int getResultModificationAmount()
    {
        return resultModificationValue;
    }
    public int getResultModificationMaxAmount()
    {
        return resultModificationMaxValue;
    }

    public boolean getResultingModifiedItem(ItemStack inputUpgradeStack)
    {
        if(inputUpgradeStack.getItem() instanceof ItemUpgradeBase baseUpgrade)
        {
            if(baseUpgrade.canAddModifierToUpgrade(inputUpgradeStack,getResultModificationName()))
            {
                int value = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,inputUpgradeStack.getOrCreateTag(),getResultModificationName());
                if(value>=getResultModificationMaxAmount())return false;
                int newValue = value + getResultModificationAmount();
                if(newValue > getResultModificationMaxAmount())newValue = getResultModificationMaxAmount();

                MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID,inputUpgradeStack.getOrCreateTag(),newValue,getResultModificationName());
                return true;
            }
        }
        return false;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return UpgradeModificationRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return UpgradeModificationRecipe.Type.INSTANCE;
    }

    public static class Type implements RecipeType<UpgradeModificationRecipe> {
        private Type() { }
        public static final UpgradeModificationRecipe.Type INSTANCE = new UpgradeModificationRecipe.Type();
        public static final String ID = "upgrademodification";
    }

    @Override
    public ItemStack getToastSymbol()
    {
        return new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_MODIFICATIONS.get());
    }

    public Ingredient getPattern()
    {
        return upgradeInput != null ? upgradeInput : Ingredient.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<UpgradeModificationRecipe> {
        public static final UpgradeModificationRecipe.Serializer INSTANCE = new UpgradeModificationRecipe.Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID,"upgrademodification");

        protected UpgradeModificationRecipe createRecipe(ResourceLocation recipeId, String group, Ingredient upgradeInput, Ingredient infusionInputIngredients, String resultModificationName, int resultModificationValue, int resultModificationMaxValue)
        {
            return new UpgradeModificationRecipe(recipeId, group, upgradeInput, infusionInputIngredients, resultModificationName, resultModificationValue, resultModificationMaxValue);
        }

        @Override
        public UpgradeModificationRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient upgradeInput = json.has("upgradeInput") ? CraftingHelper.getIngredient(json.get("upgradeInput"), false) : null;
            Ingredient infusionInputIngredients = json.has("ingredients") ? CraftingHelper.getIngredient(json.get("ingredients"), false) : null;
            String resultModificationName = GsonHelper.getAsString(json, "resultModificationName", "");
            int resultModificationValue = json.has("resultModificationValue") ? GsonHelper.getAsInt(json,"resultModificationValue") : (0);
            int resultModificationMaxValue = json.has("resultModificationMaxValue") ? GsonHelper.getAsInt(json,"resultModificationMaxValue") : (0);
            return createRecipe(recipeId, group, upgradeInput, infusionInputIngredients, resultModificationName, resultModificationValue, resultModificationMaxValue);
        }

        @Override
        public UpgradeModificationRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);
            boolean hasUpgradeInput = buffer.readBoolean();
            Ingredient upgradeInput = hasUpgradeInput ? Ingredient.fromNetwork(buffer) : null;
            boolean hasInfusionInputIngredients = buffer.readBoolean();
            Ingredient infusionInputIngredients = hasInfusionInputIngredients ? Ingredient.fromNetwork(buffer) : null;
            String resultModificationName = buffer.readUtf(32767);
            int resultModificationValue = buffer.readInt();
            int resultModificationMaxValue = buffer.readInt();
            return createRecipe(recipeId, group, upgradeInput, infusionInputIngredients, resultModificationName, resultModificationValue, resultModificationMaxValue);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, UpgradeModificationRecipe recipe)
        {
            buffer.writeUtf(recipe.group);
            boolean hasUpgradeInput = recipe.upgradeInput != null;
            buffer.writeBoolean(hasUpgradeInput);
            if (hasUpgradeInput) recipe.upgradeInput.toNetwork(buffer);
            boolean hasInfusionInputIngredients = recipe.infusionInputIngredients != null;
            buffer.writeBoolean(hasInfusionInputIngredients);
            if (hasInfusionInputIngredients) recipe.infusionInputIngredients.toNetwork(buffer);
            buffer.writeUtf(recipe.resultModificationName);
            buffer.writeInt(recipe.resultModificationValue);
            buffer.writeInt(recipe.resultModificationMaxValue);
        }

        public RecipeSerializer<?> setRegistryName(ResourceLocation name) {
            return INSTANCE;
        }

        @Nullable
        public ResourceLocation getRegistryName() {
            return ID;
        }

        public Class<RecipeSerializer<?>> getRegistryType() {
            return UpgradeModificationRecipe.Serializer.castClass(RecipeSerializer.class);
        }

        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>)cls;
        }
    }
}