package com.mowmaster.pedestals.Compat.JEI;

import com.mowmaster.mowlib.Compat.JEI.JEIRecipeTypes;
import com.mowmaster.mowlib.Compat.JEI.JEISettings;
import com.mowmaster.pedestals.Compat.JEI.recipes.CobbleGenRecipeCategory;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import com.mowmaster.pedestals.Registry.DeferredRegisterTileBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;


@mezz.jei.api.JeiPlugin
public class JeiPedestalsPlugin implements IModPlugin
{
    protected static IJeiRuntime runtime;

    public static IJeiRuntime getJeiRuntime() {
        return runtime;
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(References.MODID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CobbleGenRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        RecipeManager recipeManager = level.getRecipeManager();

        List<CobbleGenRecipe> cobbleGenRecipes = recipeManager.getAllRecipesFor(CobbleGenRecipe.Type.INSTANCE);
        registration.addRecipes(JEIPedestalsRecipeTypes.COBBLE_GEN_RECIPE, cobbleGenRecipes);




        this.registerIngredientDescription(registration, DeferredRegisterItems.TOOL_LINKINGTOOL.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_LINKINGTOOL.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.TOOL_LINKINGTOOLBACKWARDS.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_LINKINGTOOLBACKWARDS.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_UPGRADETOOL.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_TOOLSWAPPER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_FILTERTOOL.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_TAGTOOL.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_DEVTOOL.get());

        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_BASE.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_BASE.get());
        this.registerIngredientBase(registration, DeferredRegisterItems.FILTER_BASE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_ITEM.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_ITEM.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_ITEMSTACK.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_ITEMSTACK.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_DURABILITY.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_DURABILITY.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_ENCHANTED.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_ENCHANTED.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_ENCHANTED_COUNT.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_ENCHANTED_COUNT.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_ENCHANTED_FUZZY.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_ENCHANTED_FUZZY.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_ENCHANTED_EXACT.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_ENCHANTED_EXACT.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_FOOD.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_FOOD.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_MOD.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_MOD.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_RESTRICTED.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_RESTRICTED.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_TAG.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_TAG.get());

        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_EXACT.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_EXACT.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_FUZZY.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_FUZZY.get());


        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_NOCOLLIDE.get());
        this.registerIngredientCrafting(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_NOCOLLIDE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_ROUNDROBIN.get());
        this.registerIngredientCrafting(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_ROUNDROBIN.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_RENDERDIFFUSER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_RENDERDIFFUSER.get());
        this.registerIngredientCrafting(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_RENDERDIFFUSER.get());

        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T1_CAPACITY.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T2_CAPACITY.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T3_CAPACITY.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T4_CAPACITY.get());

        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T1_STORAGE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T2_STORAGE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T3_STORAGE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T4_STORAGE.get());

        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T1_SPEED.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T2_SPEED.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T3_SPEED.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T4_SPEED.get());

        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T1_RANGE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T2_RANGE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T3_RANGE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_T4_RANGE.get());



        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_BASE.get());
        this.registerIngredientBase(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_BASE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_IMPORT.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_EXPORT.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_COBBLEGEN.get());



        this.registerIngredientDescription(registration, DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        IStackHelper stackHelper = registration.getJeiHelpers().getStackHelper();
        IRecipeTransferHandlerHelper handlerHelper = registration.getTransferHelper();
        /*
        registration.addUniversalRecipeTransferHandler(new StorageControllerRecipeTransferHandler<>(
                StorageControllerContainer.class, handlerHelper));
        */
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_COBBLEGEN.get()),
                JEIPedestalsRecipeTypes.COBBLE_GEN_RECIPE);

    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JeiPedestalsPlugin.runtime = jeiRuntime;
        JEISettings.setJeiLoaded(true);
    }

    public void registerIngredientBase(IRecipeRegistration registration, ItemLike ingredient) {
        registration.addIngredientInfo(new ItemStack(ingredient.asItem()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei." + References.MODID + "." + ForgeRegistries.ITEMS.getKey(ingredient.asItem()).getPath() + ".base_description"));
    }

    public void registerIngredientDescription(IRecipeRegistration registration, ItemLike ingredient) {
        registration.addIngredientInfo(new ItemStack(ingredient.asItem()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei." + References.MODID + "." + ForgeRegistries.ITEMS.getKey(ingredient.asItem()).getPath() + ".description"));
    }

    public void registerIngredientInteraction(IRecipeRegistration registration, ItemLike ingredient) {
        registration.addIngredientInfo(new ItemStack(ingredient.asItem()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei." + References.MODID + "." + ForgeRegistries.ITEMS.getKey(ingredient.asItem()).getPath() + ".interaction"));
    }

    public void registerIngredientCrafting(IRecipeRegistration registration, ItemLike ingredient) {
        registration.addIngredientInfo(new ItemStack(ingredient.asItem()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei." + References.MODID + "." + ForgeRegistries.ITEMS.getKey(ingredient.asItem()).getPath() + ".crafting"));
    }

}
