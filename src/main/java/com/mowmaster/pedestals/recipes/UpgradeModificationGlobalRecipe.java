package com.mowmaster.pedestals.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.pedestals.items.upgrades.pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.pedestalutils.References;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import net.minecraft.core.NonNullList;
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
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;

import java.util.Collection;

public class UpgradeModificationGlobalRecipe implements Recipe<Container>
{
    @ObjectHolder(registryName = "forge:recipe_serializer", value = MODID + ":upgrademodification_global")

    private final String group;
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputIngredients;
    private final String resultModificationName;
    private final int resultModificationValue;
    private final int resultModificationMinValue;
    private final int resultModificationMaxValue;



    //Someday add in a bool value to set as something fake players could do too or not???

    public UpgradeModificationGlobalRecipe(ResourceLocation id, String group, NonNullList<Ingredient> inputIngredients, String resultModificationName, int resultModificationValue, int resultModificationMinValue, int resultModificationMaxValue)
    {
        this.group = group;
        this.id = id;
        this.inputIngredients = inputIngredients;
        this.resultModificationName = resultModificationName;
        this.resultModificationValue = resultModificationValue;
        this.resultModificationMinValue = resultModificationMinValue;
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
        if(inputIngredients.size()>0)return inputIngredients;
        return allIngredients;
    }

    @Override
    public boolean matches(Container inv, Level worldIn)
    {
        int unmatching = 0;
        if(inputIngredients.size() >= inv.getContainerSize())
        {
            for(int i=0;i< inv.getContainerSize();i++)
            {
                if(!(inputIngredients.get(i).test(inv.getItem(i))))
                {
                    //System.out.println("UNMATCH: "+ inputIngredients.get(i) + " : " + inv.getItem(i));
                    unmatching++;
                }
            }

            if(inputIngredients.size() > inv.getContainerSize())
            {
                for(int i=inv.getContainerSize();i<inputIngredients.size();i++)
                {
                    if(!(inputIngredients.get(i).test(ItemStack.EMPTY)))
                    {
                        //System.out.println("UNMATCH");
                        unmatching++;
                    }
                }
            }
        }

        boolean hasUpgrade = (inv.getItem(0).getItem() instanceof ItemUpgradeBase) && (inputIngredients.get(0).getItems()[0].getItem() instanceof ItemUpgradeBase);
        boolean canAddModifier = false;
        if(hasUpgrade)
        {
            if(inv.getItem(0).getItem() instanceof ItemUpgradeBase baseUpgrade) {
                if (baseUpgrade.canAddModifierToUpgrade(inv.getItem(0), getResultModificationName())) {
                    canAddModifier = true;
                }
            }
        }
        //System.out.println("ingredients > Container: "+ (inputIngredients.size() >= inv.getContainerSize()));
        //System.out.println("Has upgrade: "+ hasUpgrade);
        //System.out.println("Unmatching: "+ unmatching);
        return inputIngredients.size() >= inv.getContainerSize() && canAddModifier && unmatching<=0;
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
    public int getResultModificationMinAmount()
    {
        return resultModificationMinValue;
    }
    public int getResultModificationMaxAmount()
    {
        return resultModificationMaxValue;
    }

    public boolean getResultingModifiedItem(ItemStack inputUpgradeStack, boolean simulate)
    {
        if(inputUpgradeStack.getItem() instanceof ItemUpgradeBase baseUpgrade)
        {
            //System.out.println("recipe 1: "+baseUpgrade.canAddModifierToUpgrade(inputUpgradeStack,getResultModificationName()));
            if(baseUpgrade.canAddModifierToUpgrade(inputUpgradeStack,getResultModificationName()))
            {

                int value = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,inputUpgradeStack.getOrCreateTag(),getResultModificationName());
                if(value>=getResultModificationMaxAmount()) { return false; }
                if(value<getResultModificationMinAmount()) { return false; }

                int newValue = value + getResultModificationAmount();
                if(newValue > getResultModificationMaxAmount())
                {
                    newValue = getResultModificationMaxAmount();
                }
                /*System.out.println("recipe v2R: "+getResultModificationAmount());
                System.out.println("recipe v2: "+value);
                System.out.println("recipe v3R: "+getResultModificationMinAmount());
                System.out.println("recipe v3: "+newValue);
                System.out.println("recipe v4R: "+getResultModificationMaxAmount());
                System.out.println("recipe v4: "+newValue);*/

                if(!simulate)MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID,inputUpgradeStack.getOrCreateTag(),newValue,getResultModificationName());
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

    public static class Serializer implements RecipeSerializer<UpgradeModificationGlobalRecipe> {
        public static final UpgradeModificationGlobalRecipe.Serializer INSTANCE = new UpgradeModificationGlobalRecipe.Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID,"upgrademodification_global");

        protected UpgradeModificationGlobalRecipe createRecipe(ResourceLocation recipeId, String group, NonNullList<Ingredient> inputIngredients, String resultModificationName, int resultModificationValue, int resultModificationMinValue, int resultModificationMaxValue)
        {
            return new UpgradeModificationGlobalRecipe(recipeId, group, inputIngredients, resultModificationName, resultModificationValue, resultModificationMinValue, resultModificationMaxValue);
        }

        @Override
        public UpgradeModificationGlobalRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");

            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            int sizeMax = (ingredients.size()>=10)?(10):(ingredients.size());
            NonNullList<Ingredient> inputIngredients = NonNullList.withSize(sizeMax, Ingredient.EMPTY);

            for (int i = 0; i < sizeMax; i++) {
                inputIngredients.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            String resultModificationName = GsonHelper.getAsString(json, "resultModificationName", "");
            int resultModificationValue = json.has("resultModificationValue") ? GsonHelper.getAsInt(json,"resultModificationValue") : (0);
            int resultModificationMinValue = json.has("resultModificationMinValue") ? GsonHelper.getAsInt(json,"resultModificationMinValue") : (0);
            int resultModificationMaxValue = json.has("resultModificationMaxValue") ? GsonHelper.getAsInt(json,"resultModificationMaxValue") : (0);
            return createRecipe(recipeId, group, inputIngredients, resultModificationName, resultModificationValue, resultModificationMinValue, resultModificationMaxValue);
        }

        @Override
        public UpgradeModificationGlobalRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);

            NonNullList<Ingredient> inputIngredients = NonNullList.withSize(buffer.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputIngredients.size(); i++) {
                inputIngredients.set(i, Ingredient.fromNetwork(buffer));
            }

            String resultModificationName = buffer.readUtf(32767);
            int resultModificationValue = buffer.readInt();
            int resultModificationMinValue = buffer.readInt();
            int resultModificationMaxValue = buffer.readInt();
            return createRecipe(recipeId, group, inputIngredients, resultModificationName, resultModificationValue, resultModificationMinValue, resultModificationMaxValue);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, UpgradeModificationGlobalRecipe recipe)
        {
            buffer.writeUtf(recipe.group);

            buffer.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buffer);
            }

            buffer.writeUtf(recipe.resultModificationName);
            buffer.writeInt(recipe.resultModificationValue);
            buffer.writeInt(recipe.resultModificationMinValue);
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