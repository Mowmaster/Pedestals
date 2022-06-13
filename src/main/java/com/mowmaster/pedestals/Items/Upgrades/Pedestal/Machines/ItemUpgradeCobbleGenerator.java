/*
package com.mowmaster.pedestals.Items.Upgrades.Pedestal.Machines;

import com.mowmaster.mowlib.MowLibUtils.ContainerUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.PedestalUtils.UpgradeUtils;
import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ItemUpgradeCobbleGenerator extends ItemUpgradeBase {

    public ItemUpgradeCobbleGenerator(Properties p_41383_) {
        super(p_41383_);
    }



    public int getCobbleGenSpawnRate(ItemStack stack)
    {
        int capacity = UpgradeUtils.getCapacityOnItem(stack);
        return (capacity==0)?(1):(capacity*PedestalConfig.COMMON.cobbleGeneratorMultiplier.get());
    }

    @Nullable
    public CobbleGenRecipe getRecipe(Level level, ItemStack stackIn) {
        Container cont = ContainerUtils.getContainer(1);
        cont.setItem(-1,stackIn);
        List<CobbleGenRecipe> recipes = level.getRecipeManager().getRecipesFor(CobbleGenRecipe.COBBLE_GENERATOR,cont,level);
        return recipes.size() > 0 ? level.getRecipeManager().getRecipesFor(CobbleGenRecipe.COBBLE_GENERATOR,cont,level).get(0) : null;
    }

    protected Collection<ItemStack> getGeneratedItem(CobbleGenRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResultItem()));
    }

    protected int getEnergyRequiredForGeneration(CobbleGenRecipe recipe) {
        return (recipe == null)?(0):(recipe.getResultEnergyNeeded());
    }

    protected int getExperienceRequiredForGeneration(CobbleGenRecipe recipe) {
        return (recipe == null)?(0):(recipe.getResultExperienceNeeded());
    }

    protected FluidStack getFluidRequiredForGeneration(CobbleGenRecipe recipe) {
        return (recipe == null)?(FluidStack.EMPTY):(recipe.getResultFluidNeeded());
    }

    public ItemStack getItemToGenerate(BasePedestalBlockEntity pedestal, ItemStack blockBelow)
    {
        Level level = pedestal.getLevel();
        CobbleGenRecipe getRecipeForGeneration = getRecipe(level,blockBelow);
        ItemStack getInitialGeneratedItem = getGeneratedItem(getRecipeForGeneration).stream().findFirst().get();

        Block generatedBlock = Block.byItem(getInitialGeneratedItem.getItem());
        if(generatedBlock != Blocks.AIR)
        {
            //TODO: Get tool from pedestal
            ItemStack getToolFromPedestal = (pedestal.getItemInPedestal().isEmpty())?(new ItemStack(Items.STONE_PICKAXE)):(pedestal.getItemInPedestal());
            //ItemStack getToolFromPedestal = new ItemStack(Items.STONE_PICKAXE);

            LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                    .withRandom(level.random)
                    .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                    .withParameter(LootContextParams.TOOL, getToolFromPedestal);

            List<ItemStack> getPossibleDrops = generatedBlock.defaultBlockState().getDrops(builder);
            if(getPossibleDrops.size()>0)return getPossibleDrops.get(0);
        }

        return getInitialGeneratedItem;
    }

    @Override
    public int getComparatorRedstoneLevel(Level worldIn, BlockPos pos) {
        return PedestalUtilities.getRedstoneLevelPedestal(worldIn, pos);
    }

    @Override
    public void updateAction(Level world, BasePedestalBlockEntity pedestal) {
        //TODO: get this from the pedestal somehow
        ItemStack blockBelow = ItemStack.EMPTY;

        Level level = pedestal.getLevel();
        CobbleGenRecipe getRecipeForGeneration = getRecipe(level,blockBelow);
        int getEnergyNeeded = getEnergyRequiredForGeneration(getRecipeForGeneration);
        int getExperienceNeeded = getExperienceRequiredForGeneration(getRecipeForGeneration);
        FluidStack getFluidStackNeeded = getFluidRequiredForGeneration(getRecipeForGeneration);

        System.out.println(getItemToGenerate(pedestal, ItemStack.EMPTY));
    }

    @Override
    public void actionOnCollideWithBlock(BasePedestalBlockEntity pedestal, Entity entityIn) {

    }
}
*/
