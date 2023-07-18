package com.mowmaster.pedestals.items.upgrades.pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.recipes.UnBottlerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class ItemUpgradeUnBottler extends ItemUpgradeBase
{
    public ItemUpgradeUnBottler(Properties p_41383_) {
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
    protected UnBottlerRecipe getNormalRecipe(BasePedestalBlockEntity pedestal, ItemStack stackIn) {
        Level level = pedestal.getLevel();
        Container cont = MowLibContainerUtils.getContainer(1);
        cont.setItem(-1,stackIn);

        if (level == null) return null;
        RecipeManager recipeManager = level.getRecipeManager();
        Optional<UnBottlerRecipe> optional = recipeManager.getRecipeFor(UnBottlerRecipe.Type.INSTANCE, cont, level);
        if (optional.isPresent())
        {
            return optional.orElse(null);
        }

        return null;
    }

    protected Collection<ItemStack> getNormalResults(UnBottlerRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResultItem()));
    }

    protected FluidStack getFluidResult(UnBottlerRecipe recipe) {
        return (recipe == null)?(FluidStack.EMPTY):(recipe.getFluidReturned());
    }

    protected int getEnergyResult(UnBottlerRecipe recipe) {
        return (recipe == null)?(0):(recipe.getEnergyReturned());
    }

    protected int getExperienceResult(UnBottlerRecipe recipe) {
        return (recipe == null)?(0):(recipe.getExperienceReturned());
    }

    protected DustMagic getDustResult(UnBottlerRecipe recipe) {
        return (recipe == null)?(DustMagic.EMPTY):(recipe.getDustReturned());
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {
        BlockPos posInventory = getPosOfBlockBelow(level,pedestalPos,1);
        ItemStack itemFromInv = ItemStack.EMPTY;
        LazyOptional<IItemHandler> cap = MowLibItemUtils.findItemHandlerAtPos(level,posInventory,getPedestalFacing(level, pedestalPos),true);
        if(!isInventoryEmpty(cap)) {
            if (cap.isPresent()) {
                IItemHandler handler = cap.orElse(null);
                BlockEntity invToPullFrom = level.getBlockEntity(posInventory);
                if(invToPullFrom instanceof BasePedestalBlockEntity) {
                    itemFromInv = ItemStack.EMPTY;

                }
                else {
                    if (handler != null) {
                        //items
                        //Handle last, just incase any items match the other options
                        int i = getNextSlotWithItemsCapFiltered(pedestal,cap);
                        if(i>=0)
                        {
                            itemFromInv = handler.getStackInSlot(i);
                            ItemStack copyIncoming = itemFromInv.copy();
                            copyIncoming.setCount(1);
                            if(itemFromInv != null && !itemFromInv.isEmpty())
                            {
                                UnBottlerRecipe recipe = getNormalRecipe(pedestal, copyIncoming);
                                ItemStack returnedStack = getNormalResults(recipe).stream().findFirst().orElse(ItemStack.EMPTY);
                                boolean fluid = true;
                                FluidStack getReturnedFluid = getFluidResult(recipe);
                                boolean energy = true;
                                int getReturnedEnergy = getEnergyResult(recipe);
                                boolean exp = true;
                                int getReturnedExperience = getExperienceResult(recipe);
                                boolean dust = true;
                                DustMagic getReturnedDust = getDustResult(recipe);

                                //Make sure at least one returned a value
                                if(!(getReturnedFluid.isEmpty() && getReturnedEnergy<=0 && getReturnedExperience<=0 && getReturnedDust.isEmpty()))
                                {
                                    if(!handler.extractItem(i,1 ,true ).isEmpty())
                                    {
                                        boolean allowed = true;
                                        if(!returnedStack.isEmpty())
                                        {
                                            if(!pedestal.addItem(returnedStack, true))
                                            {
                                                allowed = false;
                                            }
                                        }
                                        if(!getReturnedFluid.isEmpty())
                                        {
                                            if(pedestal.addFluid(getReturnedFluid, IFluidHandler.FluidAction.SIMULATE)<getReturnedFluid.getAmount())allowed = false;
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

                                        if(allowed)
                                        {
                                            handler.extractItem(i,1 ,false );
                                            if(!getReturnedFluid.isEmpty())pedestal.addFluid(getReturnedFluid, IFluidHandler.FluidAction.EXECUTE);
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
                }
            }
        }
    }
}
