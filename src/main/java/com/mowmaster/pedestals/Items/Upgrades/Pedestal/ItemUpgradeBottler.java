package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMultiContainer;
import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Recipes.BottlerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.*;

public class ItemUpgradeBottler extends ItemUpgradeBase
{
    public ItemUpgradeBottler(Properties p_41383_) {
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
    protected BottlerRecipe getNormalRecipe(BasePedestalBlockEntity pedestal, ItemStack stackIn) {
        Level level = pedestal.getLevel();
        MowLibMultiContainer cont = MowLibContainerUtils.getMultiContainer(1);
        cont.setItem(-1,stackIn);
        if(pedestal.hasFluid())cont.setFluidStack(pedestal.getStoredFluid());
        if(pedestal.hasEnergy())cont.setEnergy(pedestal.getStoredEnergy());
        if(pedestal.hasExperience())cont.setExperience(pedestal.getStoredExperience());
        if(pedestal.hasDust())cont.setDustMagic(pedestal.getStoredDust());

        if (level == null) return null;
        RecipeManager recipeManager = level.getRecipeManager();
        Optional<BottlerRecipe> optional = recipeManager.getRecipeFor(BottlerRecipe.Type.INSTANCE, cont, level);
        if (optional.isPresent())
        {
            return optional.orElse(null);
        }

        return null;
    }

    protected Collection<ItemStack> getNormalResults(BottlerRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResultItem()));
    }

    protected FluidStack getFluidNeeded(BottlerRecipe recipe) {
        return (recipe == null)?(FluidStack.EMPTY):(recipe.getFluidNeeded());
    }

    protected int getEnergyNeeded(BottlerRecipe recipe) {
        return (recipe == null)?(0):(recipe.getEnergyNeeded());
    }

    protected int getExperienceNeeded(BottlerRecipe recipe) {
        return (recipe == null)?(0):(recipe.getExperienceNeeded());
    }

    protected DustMagic getDustNeeded(BottlerRecipe recipe) {
        return (recipe == null)?(DustMagic.EMPTY):(recipe.getDustNeeded());
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        BlockPos inventoryPos = getPosOfBlockBelow(level, pedestalPos,1);
        if (level.getBlockEntity(inventoryPos) instanceof BasePedestalBlockEntity) {
            return;
        }

        MowLibItemUtils.findItemHandlerAtPos(level, inventoryPos, getPedestalFacing(level, pedestalPos), true).ifPresent(handler ->
            getFirstSlotWithNonMachineFilteredItems(pedestal, handler).ifPresent(slot -> {
                ItemStack itemFromInv = handler.getStackInSlot(slot);
                ItemStack copyIncoming = itemFromInv.copy();
                copyIncoming.setCount(1);
                BottlerRecipe recipe = getNormalRecipe(pedestal, copyIncoming);
                ItemStack returnedStack = getNormalResults(recipe).stream().findFirst().orElse(ItemStack.EMPTY);
                if(!returnedStack.isEmpty()) {
                    boolean fluid = true;
                    FluidStack getRequiredFluid = getFluidNeeded(recipe);
                    boolean energy = true;
                    int getRequiredEnergy = getEnergyNeeded(recipe);
                    boolean exp = true;
                    int getRequiredExperience = getExperienceNeeded(recipe);
                    boolean dust = true;
                    DustMagic getRequiredDust = getDustNeeded(recipe);

                    if(!getRequiredFluid.isEmpty() && pedestal.removeFluid(getRequiredFluid, IFluidHandler.FluidAction.SIMULATE).isEmpty())fluid = false;
                    if(getRequiredEnergy > 0 && pedestal.removeEnergy(getRequiredEnergy, true)<=0)energy = false;
                    if(getRequiredExperience > 0 && pedestal.removeExperience(getRequiredExperience, true)<=0)exp = false;
                    if(!getRequiredDust.isEmpty() && pedestal.removeDust(getRequiredDust, IDustHandler.DustAction.SIMULATE).isEmpty())dust = false;
                    boolean hasRequiredStuff = fluid && energy && exp && dust;

                    if(!handler.extractItem(slot,1 ,true ).isEmpty() && pedestal.addItem(returnedStack, true) && hasRequiredStuff) {
                        handler.extractItem(slot,1 ,false );
                        if(!getRequiredFluid.isEmpty())pedestal.removeFluid(getRequiredFluid, IFluidHandler.FluidAction.EXECUTE);
                        if(getRequiredEnergy > 0)pedestal.removeEnergy(getRequiredEnergy, false);
                        if(getRequiredExperience > 0)pedestal.removeExperience(getRequiredExperience, false);
                        if(!getRequiredDust.isEmpty())pedestal.removeDust(getRequiredDust, IDustHandler.DustAction.EXECUTE);
                        pedestal.addItem(returnedStack, false);
                    }
                }
            })
        );
    }
}
