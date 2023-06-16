package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.lang.ref.WeakReference;
import java.util.*;

public class ItemUpgradeMaterialGenerator extends ItemUpgradeBase {

    public ItemUpgradeMaterialGenerator(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyItemCapacity(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public int getUpgradeWorkRange(ItemStack coinUpgrade) { return 0; }

    @Override
    public ItemStack getUpgradeDefaultTool() {
        return new ItemStack(Items.STONE_PICKAXE);
    }

    public List<ItemStack> getItemsToGenerate(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack blockToBreak) {
        Block generatedBlock = Block.byItem(blockToBreak.getItem());
        if(generatedBlock != Blocks.AIR) {
            ItemStack toolStack = pedestal.getToolStack();

            LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                    .withRandom(level.random)
                    .withParameter(LootContextParams.ORIGIN, new Vec3(pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ()))
                    .withParameter(LootContextParams.TOOL, toolStack);

            WeakReference<FakePlayer> fakePlayerReference = pedestal.getPedestalPlayer(pedestal);
            if (fakePlayerReference != null && fakePlayerReference.get() != null) {
                builder.withOptionalParameter(LootContextParams.THIS_ENTITY, fakePlayerReference.get());
            }
            return generatedBlock.defaultBlockState().getDrops(builder);
        }

        return new ArrayList<>();
    }

    public void resetCachedRecipe(ItemStack upgrade) {
        CompoundTag tag = upgrade.getOrCreateTag();
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, tag, "_cobblegen_result");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, tag, "_fluidStack");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, tag, "_energyNeeded");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, tag, "_xpNeeded");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MowLibReferences.MODID, tag, "_dustMagicColor");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MowLibReferences.MODID, tag, "_dustMagicAmount");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, tag, "_cobblegen_cached");
        // TODO [1.20]: get rid of these lines as they remove the previous NBT tags used.
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, tag, "_stackList");
    }

    public void lookupAndCacheCobbleGenResult(Level level, ItemStack input, ItemStack upgrade) {
        Container container = new SimpleContainer(input);
        Optional<CobbleGenRecipe> result = level.getRecipeManager().getRecipeFor(CobbleGenRecipe.Type.INSTANCE, container, level);
        CompoundTag tag = upgrade.getOrCreateTag();
        if (result.isPresent()) {
            CobbleGenRecipe recipe = result.get();
            MowLibCompoundTagUtils.writeItemStackToNBT(References.MODID, tag, recipe.getResultItem(),"_cobblegen_result");
            MowLibCompoundTagUtils.writeFluidStackToNBT(References.MODID, tag, recipe.getResultFluidNeeded(),"_fluidStack");
            MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID, tag, recipe.getResultEnergyNeeded(), "_energyNeeded");
            MowLibCompoundTagUtils.writeIntegerToNBT(References.MODID, tag, recipe.getResultExperienceNeeded(), "_xpNeeded");
            DustMagic.setDustMagicInTag(tag, recipe.getResultDustNeeded());
        }
        // even if there was no recipe, denote we've done the lookup since we already reset this when the block below changes
        MowLibCompoundTagUtils.writeBooleanToNBT(References.MODID, tag, true, "_cobblegen_cached");
    }

    @Override
    public void actionOnNeighborBelowChange(BasePedestalBlockEntity pedestal, BlockPos belowBlock) {
        resetCachedRecipe(pedestal.getCoinOnPedestal());
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        resetCachedRecipe(coinInPedestal);
    }

    private boolean needsFuel(FluidStack getFluidStackNeeded, int getEnergyNeeded, int getExperienceNeeded, DustMagic getDustNeeded)
    {
        if(!getFluidStackNeeded.isEmpty())return true;
        if(getEnergyNeeded>0)return true;
        if(getExperienceNeeded>0)return true;
        if(!getDustNeeded.isEmpty())return true;
        return false;
    }

    private boolean hasEnoughFuel(BasePedestalBlockEntity pedestal, FluidStack getFluidStackNeeded, int getEnergyNeeded, int getExperienceNeeded, DustMagic getDustNeeded,boolean toolDamage, int modifier)
    {
        FluidStack pedestalsFluid = pedestal.getStoredFluid();
        int pedestalEnergy = pedestal.getStoredEnergy();
        int pedestalExperience = pedestal.getStoredExperience();
        DustMagic pedestalDust = pedestal.getStoredDust();

        if(!getFluidStackNeeded.isEmpty())
        {
            int increase = getFluidStackNeeded.getAmount()*modifier;
            if(pedestalsFluid.getAmount()<increase)
            {
                return false;
            }
        }
        if(getEnergyNeeded>0)
        {
            int increase = pedestalEnergy*modifier;
            if(pedestalEnergy<increase)
            {
                return false;
            }
        }
        if(getExperienceNeeded>0)
        {
            int increase = pedestalExperience*modifier;
            if(pedestalExperience<increase)
            {
                return false;
            }
        }
        if(!getDustNeeded.isEmpty())
        {
            int increase = pedestalDust.getDustAmount()*modifier;
            if(pedestalDust.getDustAmount()<increase)
            {
                return false;
            }
        }
        if(toolDamage)
        {
            int increase = modifier;
            if(!pedestal.damageInsertedTool(increase,true))
            {
                return false;
            }
        }

        return true;
    }

    private boolean removeFuel(BasePedestalBlockEntity pedestal, FluidStack getFluidStackNeeded, int getEnergyNeeded, int getExperienceNeeded, DustMagic getDustNeeded,boolean toolDamage, int modifier)
    {
        boolean returner = false;
        boolean needsFuel = needsFuel(getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded);
        FluidStack neededFluid = getFluidStackNeeded.copy();
        int neededEnergy = getEnergyNeeded;
        int neededExperience = getExperienceNeeded;
        DustMagic neededDust = getDustNeeded.copy();

        //Should be enough to trust that we can just check and remove
        if(needsFuel)
        {
            if(hasEnoughFuel(pedestal,getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded,toolDamage,modifier))
            {
                if(!neededFluid.isEmpty())
                {
                    int increase = neededFluid.getAmount()*modifier;
                    neededFluid.setAmount(increase);
                    if(pedestal.removeFluid(neededFluid, IFluidHandler.FluidAction.SIMULATE).equals(neededFluid))
                    {
                        returner = true;
                        pedestal.removeFluid(neededFluid, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
                if(neededEnergy>0)
                {
                    int increase = neededEnergy*modifier;
                    if(pedestal.removeEnergy(increase, true)>0)
                    {
                        returner = true;
                        pedestal.removeEnergy(increase, false);
                    }
                }
                if(neededExperience>0)
                {
                    int increase = neededExperience*modifier;
                    if(pedestal.removeExperience(increase, true)>0)
                    {
                        returner = true;
                        pedestal.removeExperience(increase, false);
                    }
                }
                if(!neededDust.isEmpty())
                {
                    int increase = neededDust.getDustAmount()*modifier;
                    neededDust.setDustAmount(increase);
                    if(pedestal.removeDust(neededDust, IDustHandler.DustAction.SIMULATE).equals(neededDust))
                    {
                        returner = true;
                        pedestal.removeDust(neededDust, IDustHandler.DustAction.EXECUTE);
                    }
                }
                if(toolDamage)
                {
                    int increase = modifier;
                    pedestal.damageInsertedTool(increase,false);
                    returner = true;
                }
            }
        }
        else
        {
            returner = true;
        }

        return returner;
    }

    private ItemStack getGeneratorRecipeResult(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack upgrade) {
        CompoundTag tag = upgrade.getOrCreateTag();
        if (!MowLibCompoundTagUtils.readBooleanFromNBT(References.MODID, tag, "_cobblegen_cached")) {
            BlockPos posBelow = getPosOfBlockBelow(level, pedestalPos, 1);
            lookupAndCacheCobbleGenResult(level, new ItemStack(level.getBlockState(posBelow).getBlock().asItem()), upgrade);
        }
        return MowLibCompoundTagUtils.readItemStackFromNBT(References.MODID, tag, "_cobblegen_result");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        ItemStack recipeResult = getGeneratorRecipeResult(level, pedestal, pedestalPos, coin);
        if (recipeResult.isEmpty()) {
            if (pedestal.canSpawnParticles()) {
                MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), 50, 50, 50));
            }
            return;
        }

        List<ItemStack> getCobbleGenOutputs = getItemsToGenerate(level, pedestal, pedestalPos, recipeResult);
        if (getCobbleGenOutputs.isEmpty()) {
            return;
        }

        FluidStack getFluidStackNeeded = MowLibCompoundTagUtils.readFluidStackFromNBT(References.MODID,coin.getTag(),"_fluidStack");
        int getEnergyNeeded = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getTag(),"_energyNeeded");
        int getExperienceNeeded = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getTag(),"_xpNeeded");
        DustMagic getDustNeeded = DustMagic.getDustMagicInTag(coin.getTag());
        boolean needsFuel = needsFuel(getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded);
        boolean damage = false;
        int modifier = Math.max(getItemCapacityIncrease(coin), 1);

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

        for (int i=0; i < getCobbleGenOutputs.size(); i++)
        {
            if(getCobbleGenOutputs.get(i).isEmpty()) continue;
            ItemStack stacked = getCobbleGenOutputs.get(i);
            boolean hadSpacePreModifier = pedestal.hasSpaceForItem(stacked);
            if(hadSpacePreModifier)
            {
                ItemStack stackedCopy = stacked.copy();
                int modifierAmount = stacked.getCount() * modifier;
                if(modifier>1) { stackedCopy.setCount(modifierAmount); }
                ItemStack testStack = pedestal.addItemStack(stackedCopy, true);
                if(testStack.isEmpty())
                {
                    if(needsFuel)
                    {
                        if(hasEnoughFuel(pedestal,getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded,damage,modifier))
                        {
                            if(removeFuel(pedestal,getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded,damage,modifier))
                            {
                                pedestal.addItem(stackedCopy, false);
                            }
                        }
                    }
                    else
                    {
                        pedestal.addItem(stackedCopy, false);
                    }
                }
                else if(testStack.getCount()>0)
                {
                    stackedCopy.shrink(testStack.getCount());
                    if(needsFuel)
                    {
                        if(hasEnoughFuel(pedestal,getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded,damage,modifier))
                        {
                            if(removeFuel(pedestal,getFluidStackNeeded,getEnergyNeeded,getExperienceNeeded,getDustNeeded,damage,modifier))
                            {
                                pedestal.addItem(stackedCopy, false);
                            }
                        }
                    }
                    else
                    {
                        pedestal.addItem(stackedCopy, false);
                    }
                }
            }
        }
    }

}
