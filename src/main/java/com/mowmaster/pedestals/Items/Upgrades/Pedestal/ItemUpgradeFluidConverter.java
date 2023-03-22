package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMultiContainer;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Recipes.FluidConverterRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class ItemUpgradeFluidConverter extends ItemUpgradeBase
{
    public ItemUpgradeFluidConverter(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public int getUpgradeWorkRange(ItemStack coinUpgrade) { return 0; }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        //remove NBT saved on upgrade here

    }

    @Nullable
    protected FluidConverterRecipe getNormalRecipe(BasePedestalBlockEntity pedestal) {
        Level level = pedestal.getLevel();
        MowLibMultiContainer cont = MowLibContainerUtils.getMultiContainer(0);
        cont.setFluidStack(pedestal.getStoredFluid());

        if (level == null) return null;
        RecipeManager recipeManager = level.getRecipeManager();
        Optional<FluidConverterRecipe> optional = recipeManager.getRecipeFor(FluidConverterRecipe.Type.INSTANCE, cont, level);
        if (optional.isPresent())
        {
            return optional.orElse(null);
        }

        return null;
    }
    protected FluidStack getFluidRequired(FluidConverterRecipe recipe) {
        return (recipe == null)?(FluidStack.EMPTY):(recipe.getFluidRequired());
    }

    protected Collection<ItemStack> getNormalResults(FluidConverterRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResultItem()));
    }

    protected int getEnergyResult(FluidConverterRecipe recipe) {
        return (recipe == null)?(0):(recipe.getEnergyReturned());
    }

    protected int getExperienceResult(FluidConverterRecipe recipe) {
        return (recipe == null)?(0):(recipe.getExperienceReturned());
    }

    protected DustMagic getDustResult(FluidConverterRecipe recipe) {
        return (recipe == null)?(DustMagic.EMPTY):(recipe.getDustReturned());
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {
        if(pedestal.hasFluid())
        {
            FluidConverterRecipe recipe = getNormalRecipe(pedestal);
            FluidStack fluidRequired = getFluidRequired(recipe);

            ItemStack returnedStack = getNormalResults(recipe).stream().findFirst().orElse(ItemStack.EMPTY);
            boolean energy = true;
            int getReturnedEnergy = getEnergyResult(recipe);
            boolean exp = true;
            int getReturnedExperience = getExperienceResult(recipe);
            boolean dust = true;
            DustMagic getReturnedDust = getDustResult(recipe);

            //Make sure at least one returned a value
            if(!(getReturnedEnergy<=0 && getReturnedExperience<=0 && getReturnedDust.isEmpty() && returnedStack.isEmpty()))
            {
                if(!pedestal.removeFluid(fluidRequired, IFluidHandler.FluidAction.SIMULATE).isEmpty())
                {
                    boolean allowed = true;
                    if(!returnedStack.isEmpty())
                    {
                        if(!pedestal.addItem(returnedStack, true))
                        {
                            allowed = false;
                        }
                    }
                    if(getReturnedEnergy > 0)
                    {
                        if(pedestal.addEnergy(getReturnedEnergy, true) < getReturnedEnergy)allowed = false;
                    }
                    if(getReturnedExperience > 0)
                    {
                        if(pedestal.addExperience(getReturnedExperience, true)<getReturnedExperience)allowed = false;
                    }
                    if(!getReturnedDust.isEmpty())
                    {
                        if(pedestal.addDust(getReturnedDust, IDustHandler.DustAction.SIMULATE)<getReturnedDust.getDustAmount())allowed = false;
                    }

                    if(allowed && !pedestal.removeFluid(fluidRequired, IFluidHandler.FluidAction.EXECUTE).isEmpty())
                    {
                        if(getReturnedEnergy > 0)pedestal.addEnergy(getReturnedEnergy, false);
                        if(getReturnedExperience > 0)pedestal.addExperience(getReturnedExperience, false);
                        if(!getReturnedDust.isEmpty())pedestal.addDust(getReturnedDust, IDustHandler.DustAction.EXECUTE);
                        if(!returnedStack.isEmpty())pedestal.addItem(returnedStack, false);
                    }
                }
            }
        }
    }
}
