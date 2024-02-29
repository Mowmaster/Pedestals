package com.mowmaster.pedestals.Compat.JEI;

import com.mowmaster.mowlib.Compat.JEI.JEIRecipeTypes;
import com.mowmaster.mowlib.Compat.JEI.JEISettings;
import com.mowmaster.pedestals.Compat.JEI.recipes.*;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Recipes.*;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import com.mowmaster.pedestals.Registry.DeferredRegisterTileBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
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
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
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
        registration.addRecipeCategories(new BottlerRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new UnBottlerRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FluidConverterRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new UpgradeModifierCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        RecipeManager recipeManager = level.getRecipeManager();

        List<CobbleGenRecipe> cobbleGenRecipes = recipeManager.getAllRecipesFor(CobbleGenRecipe.Type.INSTANCE);
        registration.addRecipes(JEIPedestalsRecipeTypes.COBBLE_GEN_RECIPE, cobbleGenRecipes);
        List<BottlerRecipe> bottlerRecipes = recipeManager.getAllRecipesFor(BottlerRecipe.Type.INSTANCE);
        registration.addRecipes(JEIPedestalsRecipeTypes.BOTTLER_RECIPE, bottlerRecipes);
        List<UnBottlerRecipe> unbottlerRecipes = recipeManager.getAllRecipesFor(UnBottlerRecipe.Type.INSTANCE);
        registration.addRecipes(JEIPedestalsRecipeTypes.UNBOTTLER_RECIPE, unbottlerRecipes);
        List<FluidConverterRecipe> fluidconverterRecipes = recipeManager.getAllRecipesFor(FluidConverterRecipe.Type.INSTANCE);
        registration.addRecipes(JEIPedestalsRecipeTypes.FLUIDCONVERTER_RECIPE, fluidconverterRecipes);
        List<UpgradeModificationGlobalRecipe> upgradeModGlobalRecipes = recipeManager.getAllRecipesFor(UpgradeModificationGlobalRecipe.Type.INSTANCE);
        registration.addRecipes(JEIPedestalsRecipeTypes.UPGRADEMODIFICATIONGLOBAL_RECIPE, upgradeModGlobalRecipes);
        /*
        List<UpgradeModificationRecipe> upgradeModRecipes = recipeManager.getAllRecipesFor(UpgradeModificationRecipe.Type.INSTANCE);
        registration.addRecipes(JEIPedestalsRecipeTypes.UPGRADEMODIFICATION_RECIPE, upgradeModRecipes);

        */



        this.registerIngredientDescription(registration, DeferredRegisterItems.TOOL_LINKINGTOOL.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_LINKINGTOOL.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.TOOL_LINKINGTOOLBACKWARDS.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_LINKINGTOOLBACKWARDS.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_UPGRADETOOL.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_TOOLSWAPPER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_FILTERTOOL.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_WORKTOOL.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_TAGTOOL.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_DEVTOOL.get());

        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_AUGMENTS_SPEED.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_AUGMENTS_CAPACITY.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_AUGMENTS_STORAGE.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_AUGMENTS_RANGE.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_AUGMENTS_DIFFUSER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_AUGMENTS_ROUNDROBIN.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_AUGMENTS_COLLIDE.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_AUGMENTS_TRANSFERTOGGLE.get());

        this.registerIngredientDescription(registration, DeferredRegisterItems.TOOL_MANIFEST.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TOOL_MANIFEST.get());

        this.registerIngredientDescription(registration, DeferredRegisterItems.WORKCARD_AREA.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.WORKCARD_LOCATIONS.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.WORKCARD_PEDESTALS.get());

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

        this.registerIngredientDescription(registration, DeferredRegisterItems.TAG_GETTER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.TAG_GETTER.get());

        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_TAG_MACHINE.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_TAG_MACHINE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_EXACT.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_EXACT.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_FUZZY.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_FUZZY.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_ITEM_MACHINE.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_ITEM_MACHINE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.FILTER_ITEMSTACK_MACHINE.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.FILTER_ITEMSTACK_MACHINE.get());


        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_NOCOLLIDE.get());
        this.registerIngredientCrafting(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_NOCOLLIDE.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_ROUNDROBIN.get());
        this.registerIngredientCrafting(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_ROUNDROBIN.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_RENDERDIFFUSER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_RENDERDIFFUSER.get());
        this.registerIngredientCrafting(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_RENDERDIFFUSER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_TRANSFERTOGGLE.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.AUGMENT_PEDESTAL_TRANSFERTOGGLE.get());

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
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_EXPORT.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_COBBLEGEN.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_MAGNET.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_MAGNET.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_PACKAGER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_UNPACKAGER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_CRAFTER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_CRAFTER.get());

        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_BREAKER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_BREAKER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_PLACER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_PLACER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_FILLER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_FILLER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_QUARRY.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_QUARRY.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_CHOPPER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_CHOPPER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_HARVESTER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_HARVESTER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_PLANTER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_PLANTER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_FERTILIZER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_FERTILIZER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_HIVEHARVESTER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_HIVEHARVESTER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_VOID.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_VOID.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_RECYCLER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_RECYCLER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_FLUIDCONVERTER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_FLUIDCONVERTER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_DROPPER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_DROPPER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_PUMP.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_PUMP.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_DRAIN.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_DRAIN.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_SHEERER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_SHEERER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_MILKER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_MILKER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_BREEDER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_BREEDER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_ATTACKER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_ATTACKER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_FAN.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_FAN.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_BOTTLER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_BOTTLER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_UNBOTTLER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_UNBOTTLER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_MODIFICATIONS.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_MODIFICATIONS.get());


        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_SMELTER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_SMELTER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_SMOKER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_SMOKER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_BLASTER.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_BLASTER.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_RFGENERATOR.get());
        this.registerIngredientInteraction(registration, DeferredRegisterItems.PEDESTAL_UPGRADE_RFGENERATOR.get());

        this.registerIngredientDescription(registration, DeferredRegisterItems.MECHANICAL_STORAGE_ITEM.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.MECHANICAL_STORAGE_FLUID.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.MECHANICAL_STORAGE_ENERGY.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.MECHANICAL_STORAGE_XP.get());
        this.registerIngredientDescription(registration, DeferredRegisterItems.MECHANICAL_STORAGE_DUST.get());


        this.registerIngredientDescription(registration, DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get());
        this.registerIngredientDescription(registration, DeferredRegisterTileBlocks.BLOCK_OMEGAPEDESTAL.get());
        this.registerIngredientDescription(registration, DeferredRegisterTileBlocks.BLOCK_RATSTATUE_PEDESTAL.get());
        this.registerIngredientDescription(registration, DeferredRegisterTileBlocks.BLOCK_CATSTATUE_PEDESTAL.get());
        this.registerIngredientDescription(registration, DeferredRegisterTileBlocks.BLOCK_GOBLINSTATUE_PEDESTAL.get());
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
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_BOTTLER.get()),
                JEIPedestalsRecipeTypes.BOTTLER_RECIPE);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_UNBOTTLER.get()),
                JEIPedestalsRecipeTypes.UNBOTTLER_RECIPE);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_FLUIDCONVERTER.get()),
                JEIPedestalsRecipeTypes.FLUIDCONVERTER_RECIPE);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_MODIFICATIONS.get()),
                JEIPedestalsRecipeTypes.UPGRADEMODIFICATIONGLOBAL_RECIPE);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_ATTACKER.get()),
                JEIRecipeTypes.MOB_FILTER);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_FAN.get()),
                JEIRecipeTypes.MOB_FILTER);

        //Vanilla Recipe things
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_CRAFTER.get()),
                RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_SMELTER.get()),
                RecipeTypes.SMELTING);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_BLASTER.get()),
                RecipeTypes.BLASTING);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.PEDESTAL_UPGRADE_SMOKER.get()),
                RecipeTypes.SMOKING);


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
