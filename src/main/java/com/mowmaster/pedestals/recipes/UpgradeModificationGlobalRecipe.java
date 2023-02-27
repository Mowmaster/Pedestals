package com.mowmaster.pedestals.Recipes;

import com.google.gson.JsonObject;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;


public class UpgradeModificationGlobalRecipe implements Recipe<Container>
{
    @ObjectHolder(registryName = "forge:recipe_serializer", value = MODID + ":upgrademodification_global")

    private final String group;
    private final ResourceLocation id;
    @Nullable
    private final Ingredient upgradeInput;
    private final Ingredient infusionInputIngredients;
    private final int inputIngredientsCount;
    private final String resultModificationName;
    private final int resultModificationValue;
    private final int resultModificationMaxValue;



    //Someday add in a bool value to set as something fake players could do too or not???

    public UpgradeModificationGlobalRecipe(ResourceLocation id, String group, @Nullable Ingredient upgradeInput, @Nullable Ingredient infusionInputIngredients, int inputIngredientsCount, String resultModificationName, int resultModificationValue, int resultModificationMaxValue)
    {
        this.group = group;
        this.id = id;
        this.upgradeInput = upgradeInput;
        this.infusionInputIngredients = infusionInputIngredients;
        this.inputIngredientsCount = inputIngredientsCount;
        this.resultModificationName = resultModificationName;
        this.resultModificationValue = resultModificationValue;
        this.resultModificationMaxValue = resultModificationMaxValue;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static Collection<UpgradeModificationGlobalRecipe> getAllRecipes(Level world)
    {
        return world.getRecipeManager().getAllRecipesFor(UpgradeModificationGlobalRecipe.Type.INSTANCE);
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
        int unmatching = 0;
        for(int i=1;i< inv.getContainerSize();i++)
        {
            if(!(infusionInputIngredients.test(inv.getItem(i))))
            {
                unmatching++;
            }
        }

        /*System.out.println("NEW RECIPE");
        ItemStack stacks[] = infusionInputIngredients.getItems();
        //Immediate fail if input container is less then recipe size needed.
        if(inv.getContainerSize()<stacks.length)return false;
        int matchCounter = 0;
        int unmatchCounter = 0;
        for(int i=0;i< stacks.length;i++)
        {
            System.out.println(stacks[i].getDisplayName().getString());
            System.out.println(inv.getItem(i+1).getDisplayName().getString());
            if(stacks[i].isEmpty() && inv.getItem(i+1).isEmpty()){
                System.out.println("EMPTY MATCH");
                matchCounter++;
            }
            else if(stacks[i].is(inv.getItem(i+1).getItem())){
                System.out.println("MATCHING");
                matchCounter++;
            }
            else if(!stacks[i].isEmpty() && inv.getItem(i+1).isEmpty()){
                System.out.println("UNMATCHING");
                unmatchCounter++;
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

        System.out.println((inv.getItem(0).getItem() instanceof ItemUpgradeBase) && matchCounter==stacks.length);
        return (inv.getItem(0).getItem() instanceof ItemUpgradeBase) && matchCounter==stacks.length && unmatchCounter<=0;*/
        if((inv.getItem(0).getItem() instanceof ItemUpgradeBase) && unmatching<=0)
        {
            System.out.println(infusionInputIngredients.toJson().getAsInt());
        }

        return (inv.getItem(0).getItem() instanceof ItemUpgradeBase) && unmatching<=0;
    }

    @Override
    public ItemStack assemble(Container inv)
    {
        return getResultItem().copy();
    }

    @Override
    public ItemStack getResultItem()
    {
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
            //System.out.println("recipe 1: "+baseUpgrade.canAddModifierToUpgrade(inputUpgradeStack,getResultModificationName()));
            if(baseUpgrade.canAddModifierToUpgrade(inputUpgradeStack,getResultModificationName()))
            {

                int value = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,inputUpgradeStack.getOrCreateTag(),getResultModificationName());
                if(value>=getResultModificationMaxAmount())
                {
                    return false;
                }
                int newValue = value + getResultModificationAmount();
                if(newValue > getResultModificationMaxAmount())
                {
                    newValue = getResultModificationMaxAmount();
                }
                /*System.out.println("recipe v2R: "+getResultModificationAmount());
                System.out.println("recipe v2: "+value);
                System.out.println("recipe v3R: "+getResultModificationMaxAmount());
                System.out.println("recipe v3: "+newValue);*/

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
        return UpgradeModificationGlobalRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return UpgradeModificationGlobalRecipe.Type.INSTANCE;
    }

    public static class Type implements RecipeType<UpgradeModificationGlobalRecipe> {
        private Type() { }
        public static final UpgradeModificationGlobalRecipe.Type INSTANCE = new UpgradeModificationGlobalRecipe.Type();
        public static final String ID = "upgrademodification_global";
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

    public static class Serializer implements RecipeSerializer<UpgradeModificationGlobalRecipe> {
        public static final UpgradeModificationGlobalRecipe.Serializer INSTANCE = new UpgradeModificationGlobalRecipe.Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID,"upgrademodification_global");

        protected UpgradeModificationGlobalRecipe createRecipe(ResourceLocation recipeId, String group, Ingredient upgradeInput, Ingredient infusionInputIngredients, int inputIngredientsCount, String resultModificationName, int resultModificationValue, int resultModificationMaxValue)
        {
            return new UpgradeModificationGlobalRecipe(recipeId, group, upgradeInput, infusionInputIngredients, inputIngredientsCount, resultModificationName, resultModificationValue, resultModificationMaxValue);
        }

        @Override
        public UpgradeModificationGlobalRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient upgradeInput = json.has("upgradeInput") ? CraftingHelper.getIngredient(json.get("upgradeInput")) : null;
            Ingredient infusionInputIngredients = json.has("ingredients") ? CraftingHelper.getIngredient(json.get("ingredients")) : null;
            int inputIngredientsCount = json.has("inputIngredientsCount") ? GsonHelper.getAsInt(json,"inputIngredientsCount") : (0);
            String resultModificationName = GsonHelper.getAsString(json, "resultModificationName", "");
            int resultModificationValue = json.has("resultModificationValue") ? GsonHelper.getAsInt(json,"resultModificationValue") : (0);
            int resultModificationMaxValue = json.has("resultModificationMaxValue") ? GsonHelper.getAsInt(json,"resultModificationMaxValue") : (0);
            return createRecipe(recipeId, group, upgradeInput, infusionInputIngredients, inputIngredientsCount, resultModificationName, resultModificationValue, resultModificationMaxValue);
        }

        @Override
        public UpgradeModificationGlobalRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);
            boolean hasUpgradeInput = buffer.readBoolean();
            Ingredient upgradeInput = hasUpgradeInput ? Ingredient.fromNetwork(buffer) : null;
            boolean hasInfusionInputIngredients = buffer.readBoolean();
            Ingredient infusionInputIngredients = hasInfusionInputIngredients ? Ingredient.fromNetwork(buffer) : null;
            int inputIngredientsCount = buffer.readInt();
            String resultModificationName = buffer.readUtf(32767);
            int resultModificationValue = buffer.readInt();
            int resultModificationMaxValue = buffer.readInt();
            return createRecipe(recipeId, group, upgradeInput, infusionInputIngredients, inputIngredientsCount, resultModificationName, resultModificationValue, resultModificationMaxValue);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, UpgradeModificationGlobalRecipe recipe)
        {
            buffer.writeUtf(recipe.group);
            boolean hasUpgradeInput = recipe.upgradeInput != null;
            buffer.writeBoolean(hasUpgradeInput);
            if (hasUpgradeInput) recipe.upgradeInput.toNetwork(buffer);
            boolean hasInfusionInputIngredients = recipe.infusionInputIngredients != null;
            buffer.writeBoolean(hasInfusionInputIngredients);
            if (hasInfusionInputIngredients) recipe.infusionInputIngredients.toNetwork(buffer);
            buffer.writeInt(recipe.inputIngredientsCount);
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
            return UpgradeModificationGlobalRecipe.Serializer.castClass(RecipeSerializer.class);
        }

        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>)cls;
        }
    }
}