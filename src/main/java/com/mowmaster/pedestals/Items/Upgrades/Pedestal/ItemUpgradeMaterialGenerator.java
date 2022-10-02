package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.PedestalUtils.UpgradeUtils;
import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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

public class ItemUpgradeMaterialGenerator extends ItemUpgradeBase {

    public ItemUpgradeMaterialGenerator(Properties p_41383_) {
        super(p_41383_);
    }

    public int getCobbleGenSpawnRate(ItemStack stack)
    {
        int capacity = UpgradeUtils.getCapacityOnItem(stack);
        return 1;
    }

    @Nullable
    public CobbleGenRecipe getRecipe(Level level, ItemStack stackIn) {
        Container cont = MowLibContainerUtils.getContainer(1);
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

    public List<ItemStack> getItemToGenerate(BasePedestalBlockEntity pedestal, CobbleGenRecipe recipe)
    {
        ItemStack getInitialGeneratedItem = ItemStack.EMPTY;
        Level level = pedestal.getLevel();
        //ItemStack itemBlockBelow = new ItemStack(pedestal.getLevel().getBlockState(belowBlock).getBlock().asItem());
        if(recipe == null)return new ArrayList<>();
        getInitialGeneratedItem = getGeneratedItem(recipe).stream().findFirst().get();

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

    public ItemStack getItemStackToGenerate(BasePedestalBlockEntity pedestal, CobbleGenRecipe recipe)
    {
        ItemStack getInitialGeneratedItem = ItemStack.EMPTY;
        Level level = pedestal.getLevel();
        //ItemStack itemBlockBelow = new ItemStack(pedestal.getLevel().getBlockState(belowBlock).getBlock().asItem());
        if(recipe == null)return ItemStack.EMPTY;
        getInitialGeneratedItem = getGeneratedItem(recipe).stream().findFirst().get();

        return getInitialGeneratedItem;
    }

    public List<ItemStack> getItemsToGenerate(BasePedestalBlockEntity pedestal, ItemStack blockToBreak)
    {
        Level level = pedestal.getLevel();
        Block generatedBlock = Block.byItem(blockToBreak.getItem());
        if(generatedBlock != Blocks.AIR)
        {
            ItemStack getToolFromPedestal = (pedestal.getToolStack().isEmpty())?(new ItemStack(Items.STONE_PICKAXE)):(pedestal.getToolStack());

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
        CobbleGenRecipe recipe = getRecipe(pedestal.getLevel(),new ItemStack(pedestal.getLevel().getBlockState(belowBlock).getBlock().asItem()));
        ItemStack getOutput = getItemStackToGenerate(pedestal,recipe);
        List<ItemStack> getStackList = new ArrayList<>();
        getStackList.add(getOutput);
        int getEnergyNeeded = getEnergyRequiredForGeneration(recipe);
        int getExperienceNeeded = getExperienceRequiredForGeneration(recipe);
        FluidStack getFluidStackNeeded = getFluidRequiredForGeneration(recipe);
        CompoundTag tagCoin = new CompoundTag();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        if(coinInPedestal.hasTag()) { tagCoin = coinInPedestal.getTag(); }
        tagCoin = MowLibCompoundTagUtils.writeItemStackListToNBT(References.MODID, tagCoin, getStackList,"_stackList");
        tagCoin = MowLibCompoundTagUtils.writeFluidStackToNBT(References.MODID, tagCoin, getFluidStackNeeded,"_fluidStack");
        tagCoin = MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID, tagCoin,getEnergyNeeded, "_energyNeeded");
        tagCoin = MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID, tagCoin,getExperienceNeeded, "_xpNeeded");

        coinInPedestal.setTag(tagCoin);
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        //remove NBT saved on upgrade here
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(),"_stackList");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(),"_fluidStack");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(), "_energyNeeded");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getTag(), "_xpNeeded");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {
        BlockPos posBelow = getPosOfBlockBelow(level,pedestalPos,1);
        ItemStack itemBlockBelow = new ItemStack(level.getBlockState(posBelow).getBlock().asItem());

        //TODO: Add in Capacity modifier for generation.
        //int modifier = getCobbleGenSpawnRate(pedestal.getCoinOnPedestal());

        //if itemstacklist is null, populate nbt. then rely on block below updates to modify things.
        if(MowLibCompoundTagUtils.readItemStackListFromNBT(References.MODID,coin.getTag(),"_stackList") == null)actionOnNeighborBelowChange(pedestal, posBelow);

        ItemStack getCobbleGenOutput = MowLibCompoundTagUtils.readItemStackListFromNBT(References.MODID,coin.getTag(),"_stackList").get(0);
        if(!getCobbleGenOutput.isEmpty())
        {
            List<ItemStack> getCobbleGenOutputs = getItemsToGenerate(pedestal,getCobbleGenOutput);
            FluidStack getFluidStackNeeded = MowLibCompoundTagUtils.readFluidStackFromNBT(References.MODID,coin.getTag(),"_fluidStack");
            int getEnergyNeeded = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getTag(),"_energyNeeded");
            int getExperienceNeeded = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getTag(),"_xpNeeded");

            boolean fluid = false;
            boolean energy = false;
            boolean xp = false;
            boolean damage = false;

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
                            damage = true;
                        }
                        else
                        {
                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                            return;
                        }
                    }
                    else
                    {
                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                        return;
                    }
                }
            }

            if(getCobbleGenOutputs.size()<=0){return;}
            else
            {
                boolean itemsInserted = false;

                for (int i=0; i < getCobbleGenOutputs.size(); i++)
                {
                    if(getCobbleGenOutputs.get(i).isEmpty()) continue;

                    if(pedestal.addItem(getCobbleGenOutputs.get(i), true))
                    {
                        pedestal.addItem(getCobbleGenOutputs.get(i), false);
                        itemsInserted = true;
                    }
                }

                if(itemsInserted)
                {
                    if(fluid)pedestal.removeFluid(getFluidStackNeeded, IFluidHandler.FluidAction.EXECUTE);
                    if(energy)pedestal.removeEnergy(getEnergyNeeded, false);
                    if(xp)pedestal.removeExperience(getExperienceNeeded, false);
                    if(damage)pedestal.damageInsertedTool(1,false);
                }
            }
        }
    }

}
