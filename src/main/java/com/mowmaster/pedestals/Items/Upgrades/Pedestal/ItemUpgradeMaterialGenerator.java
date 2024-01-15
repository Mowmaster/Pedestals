package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.PedestalUtils.References;
import com.mowmaster.pedestals.Recipes.CobbleGenRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.*;

public class ItemUpgradeMaterialGenerator extends ItemUpgradeBase {

    public ItemUpgradeMaterialGenerator(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyItemCapacity(ItemStack upgradeItemStack) { return true; }

    @Override
    public int getUpgradeWorkRange(ItemStack coinUpgrade) { return 0; }

    @Override
    public ItemStack getUpgradeDefaultTool() { return new ItemStack(Items.STONE_PICKAXE); }

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

    private ItemStack getGeneratorRecipeResult(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack upgrade) {
        CompoundTag tag = upgrade.getOrCreateTag();
        if (!MowLibCompoundTagUtils.readBooleanFromNBT(References.MODID, tag, "_cobblegen_cached")) {
            BlockPos posBelow = getPosOfBlockBelow(level, pedestalPos, 1);
            lookupAndCacheCobbleGenResult(level, new ItemStack(level.getBlockState(posBelow).getBlock().asItem()), upgrade);
        }
        return MowLibCompoundTagUtils.readItemStackFromNBT(References.MODID, tag, "_cobblegen_result");
    }

    private int getGeneratorMultiplier(BasePedestalBlockEntity pedestal, ItemStack upgrade, FluidStack fluidStackNeeded, int energyNeeded, int experienceNeeded, DustMagic dustNeeded) {
        int multiplier = Math.max(1, getItemCapacityIncrease(upgrade));
        boolean damage = canDamageTool(pedestal.getLevel(), pedestal, PedestalConfig.COMMON.cobbleGeneratorDamageTools.get());
        if (!fluidStackNeeded.isEmpty()) {
            FluidStack storedFluid = pedestal.getStoredFluid();
            if (!storedFluid.isFluidEqual(fluidStackNeeded)) {
                multiplier = 0;
            } else {
                multiplier = Math.min(multiplier, storedFluid.getAmount() / fluidStackNeeded.getAmount());
            }
        }
        if (energyNeeded > 0) {
            multiplier = Math.min(multiplier, pedestal.getStoredEnergy() / energyNeeded);
        }
        if (experienceNeeded > 0) {
            multiplier = Math.min(multiplier, pedestal.getStoredExperience() / experienceNeeded);
        }
        if (!dustNeeded.isEmpty()) {
            DustMagic storedDust = pedestal.getStoredDust();
            if (storedDust.getDustColor() != dustNeeded.getDustColor()) {
                multiplier = 0;
            } else {
                multiplier = Math.min(multiplier, storedDust.getDustAmount() / dustNeeded.getDustAmount());
            }
        }
        if (damage) {
            multiplier = Math.min(multiplier, pedestal.getDurabilityRemainingOnInsertedTool());
        }
        return multiplier;
    }

    private void consumeFuel(BasePedestalBlockEntity pedestal, FluidStack fluidStackNeeded, int energyNeeded, int experienceNeeded, DustMagic dustNeeded, int multiplier) {
        boolean damage = canDamageTool(pedestal.getLevel(), pedestal, PedestalConfig.COMMON.cobbleGeneratorDamageTools.get());
        if (!fluidStackNeeded.isEmpty()) {
            FluidStack toRemove = fluidStackNeeded.copy();
            toRemove.setAmount(fluidStackNeeded.getAmount() * multiplier);
            pedestal.removeFluid(toRemove, IFluidHandler.FluidAction.EXECUTE);
        }
        if (energyNeeded > 0) {
            pedestal.removeEnergy(energyNeeded * multiplier, false);
        }
        if (experienceNeeded > 0) {
            pedestal.removeExperience(experienceNeeded * multiplier, false);
        }
        if (!dustNeeded.isEmpty()) {
            DustMagic toRemove = dustNeeded.copy();
            toRemove.setDustAmount(dustNeeded.getDustColor() * multiplier);
            pedestal.removeDust(toRemove, IDustHandler.DustAction.EXECUTE);
        }
        if (damage) {
            pedestal.damageInsertedTool(multiplier, false);
        }
    }

    public boolean allowRun(BasePedestalBlockEntity pedestal, boolean damage)
    {
        if(PedestalConfig.COMMON.cobbleGeneratorRequireTools.get())
        {
            if(pedestal.hasTool())
            {
                if(damage)
                {
                    return pedestal.damageInsertedTool(1,true);
                }
                else return true;
            }
            else return false;
        }

        return true;
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        if(allowRun(pedestal, canDamageTool(pedestal.getLevel(), pedestal, PedestalConfig.COMMON.cobbleGeneratorDamageTools.get())))
        {
            ItemStack recipeResult = getGeneratorRecipeResult(level, pedestal, pedestalPos, coin);
            if (recipeResult.isEmpty()) {
                if (pedestal.canSpawnParticles()) {
                    MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, pedestalPos.getX(), pedestalPos.getY() + 1.0f, pedestalPos.getZ(), 50, 50, 50));
                }
                return;
            }

            List<ItemStack> getCobbleGenOutputs = getBlockDrops(level, pedestal, recipeResult.getItem());
            if (getCobbleGenOutputs.isEmpty()) {
                return;
            }

            FluidStack fluidStackNeeded = MowLibCompoundTagUtils.readFluidStackFromNBT(References.MODID,coin.getTag(),"_fluidStack");
            int energyNeeded = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getTag(),"_energyNeeded");
            int experienceNeeded = MowLibCompoundTagUtils.readIntegerFromNBT(References.MODID,coin.getTag(),"_xpNeeded");
            DustMagic dustNeeded = DustMagic.getDustMagicInTag(coin.getTag());

            int multiplier = getGeneratorMultiplier(pedestal, coin, fluidStackNeeded, energyNeeded, experienceNeeded, dustNeeded);
            if (multiplier == 0) {
                if(pedestal.canSpawnParticles()) {
                    MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, pedestalPos.getX(), pedestalPos.getY() + 1.0f, pedestalPos.getZ(), 255, 255, 255));
                }
                return;
            }

            for (ItemStack output : getCobbleGenOutputs) {
                if (output.isEmpty()) continue;

                if (pedestal.hasSpaceForItem(output)) {
                    int numToAdd = Math.min(output.getCount() * multiplier, output.getMaxStackSize());
                    ItemStack toAdd = output.copy();
                    toAdd.setCount(numToAdd);
                    ItemStack remainder = pedestal.addItemStack(toAdd, false);
                    int fuelConsumedMultiplier = (int)Math.ceil((double)(toAdd.getCount() - remainder.getCount()) / output.getCount());
                    consumeFuel(pedestal, fluidStackNeeded, energyNeeded, experienceNeeded, dustNeeded, fuelConsumedMultiplier);
                }
            }
        }
    }

}
