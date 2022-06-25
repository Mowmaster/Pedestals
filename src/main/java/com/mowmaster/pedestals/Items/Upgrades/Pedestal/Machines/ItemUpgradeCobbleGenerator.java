package com.mowmaster.pedestals.Items.Upgrades.Pedestal.Machines;

import com.mowmaster.mowlib.MowLibUtils.ContainerUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemUpgradeCobbleGenerator extends ItemUpgradeBase {

    public ItemUpgradeCobbleGenerator(Properties p_41383_) {
        super(p_41383_);
    }

    public int getCobbleGenSpawnRate(ItemStack stack)
    {
        int capacity = UpgradeUtils.getCapacityOnItem(stack);
        return 1;
    }

    @Nullable
    public CobbleGenRecipe getRecipe(Level level, ItemStack stackIn) {
        Container cont = ContainerUtils.getContainer(1);
        cont.setItem(-1,stackIn);
        List<CobbleGenRecipe> recipes = level.getRecipeManager().getRecipesFor(CobbleGenRecipe.Type.INSTANCE,cont,level);
        return recipes.size() > 0 ? level.getRecipeManager().getRecipesFor(CobbleGenRecipe.Type.INSTANCE,cont,level).get(0) : null;
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

    public List<ItemStack> getItemToGenerate(BasePedestalBlockEntity pedestal, ItemStack blockBelow)
    {
        ItemStack getInitialGeneratedItem = ItemStack.EMPTY;
        Level level = pedestal.getLevel();
        //ItemStack itemBlockBelow = new ItemStack(pedestal.getLevel().getBlockState(belowBlock).getBlock().asItem());
        if(getRecipe(pedestal.getLevel(),blockBelow) == null)return new ArrayList<>();
        getInitialGeneratedItem = getGeneratedItem(getRecipe(pedestal.getLevel(),blockBelow)).stream().findFirst().get();

        Block generatedBlock = Block.byItem(getInitialGeneratedItem.getItem());
        if(generatedBlock != Blocks.AIR)
        {
            ItemStack getToolFromPedestal = (pedestal.getToolStack().isEmpty())?(new ItemStack(Items.STONE_PICKAXE)):(pedestal.getToolStack());
//System.out.println(getToolFromPedestal);

            LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                    .withRandom(level.random)
                    .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                    .withParameter(LootContextParams.TOOL, getToolFromPedestal);

            return generatedBlock.defaultBlockState().getDrops(builder);
        }

        return new ArrayList<>();
    }

    @Override
    public int getComparatorRedstoneLevel(Level worldIn, BlockPos pos) {
        return PedestalUtilities.getRedstoneLevelPedestal(worldIn, pos);
    }

    @Override
    public void actionOnNeighborBelowChange(BasePedestalBlockEntity pedestal, BlockPos belowBlock) {
        //Update Block Below data
/*System.out.println("UPDATE");
        ItemStack itemBlockBelow = new ItemStack(pedestal.getLevel().getBlockState(belowBlock).getBlock().asItem());
        getCobbleRecipe = getRecipe(pedestal.getLevel(),itemBlockBelow);
        getCobbleGenOutput = getItemToGenerate(pedestal, itemBlockBelow);*/
    }

    @Override
    public void updateAction(Level world, BasePedestalBlockEntity pedestal) {
        BlockPos pedestalPos = pedestal.getPos();
        BlockPos posBelow = getPosOfBlockBelow(world,pedestalPos,1);
        ItemStack itemBlockBelow = new ItemStack(world.getBlockState(posBelow).getBlock().asItem());

        //TODO: Add in Capacity modifier for generation.
        //int modifier = getCobbleGenSpawnRate(pedestal.getCoinOnPedestal());

        Level level = pedestal.getLevel();
        CobbleGenRecipe getCobbleRecipe = getRecipe(pedestal.getLevel(),itemBlockBelow);;
        if(getCobbleRecipe == null) getCobbleRecipe = getRecipe(level,itemBlockBelow);
        if(getCobbleRecipe == null)return;

        int getEnergyNeeded = getEnergyRequiredForGeneration(getCobbleRecipe);
        int getExperienceNeeded = getExperienceRequiredForGeneration(getCobbleRecipe);
        FluidStack getFluidStackNeeded = getFluidRequiredForGeneration(getCobbleRecipe);
        boolean fluid = false;
        boolean energy = false;
        boolean xp = false;

        if(!getFluidStackNeeded.isEmpty())
        {
            if(!pedestal.removeFluid(getFluidStackNeeded, IFluidHandler.FluidAction.SIMULATE).isEmpty())
            {
                fluid = true;
            }
            else return;
        }

        if(getEnergyNeeded>0)
        {
            if(pedestal.removeEnergy(getEnergyNeeded, true)>0)
            {
                energy = true;
            }
            else return;
        }


        if(getExperienceNeeded>0)
        {
            if(pedestal.removeExperience(getExperienceNeeded, true)>0)
            {
                xp = true;
            }
            else return;
        }

        if(PedestalConfig.COMMON.cobbleGeneratorDamageTools.get())
        {
            if(pedestal.hasTool())
            {
                if(pedestal.getDurabilityRemainingOnInsertedTool()>0)
                {
                    if(pedestal.damageInsertedTool(1,true))
                    {
                        pedestal.damageInsertedTool(1,false);
                    }
                    else
                    {
                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY(),pedestalPos.getZ(),255,255,255));
                        return;
                    }
                }
                else
                {
                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY(),pedestalPos.getZ(),255,255,255));

                    return;
                }
            }
        }

        List<ItemStack>getCobbleGenOutput = getItemToGenerate(pedestal,itemBlockBelow);
        if(getCobbleGenOutput == null)getCobbleGenOutput = getItemToGenerate(pedestal, itemBlockBelow);
        if(getCobbleGenOutput.size()<=0)return;
//System.out.println(getCobbleGenOutput);
        if(fluid)pedestal.removeFluid(getFluidStackNeeded, IFluidHandler.FluidAction.EXECUTE);
        if(energy)pedestal.removeEnergy(getEnergyNeeded, false);
        if(xp)pedestal.removeExperience(getExperienceNeeded, false);
        for (int i=0; i < getCobbleGenOutput.size(); i++)
        {
            if(pedestal.addItem(getCobbleGenOutput.get(i), true))pedestal.addItem(getCobbleGenOutput.get(i), false);
        }
    }

    @Override
    public void actionOnCollideWithBlock(BasePedestalBlockEntity pedestal, Entity entityIn) {
        return;
    }
}
