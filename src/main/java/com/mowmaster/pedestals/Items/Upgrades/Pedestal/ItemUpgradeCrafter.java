package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.PedestalUtils.References;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.*;
import java.util.stream.Stream;

public class ItemUpgradeCrafter extends ItemUpgradeBase {
    public ItemUpgradeCrafter(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyRange(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean needsWorkCard(ItemStack upgradeItemStack) { return true; }

    @Override
    public int getWorkCardType() { return 3; }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        resetCachedValidWorkCardPositions(MODID, coinInPedestal);
        resetCachedRecipe(MODID, coinInPedestal);

        // TODO [1.20]: get rid of these lines as they removes the previous NBT tags used. All 3 were switched off of to ensure
        // nothing was broken by moving off of `output` (we had to ignore that a cached recipe existed and used that old NBT)
        CompoundTag tag = coinInPedestal.getOrCreateTag();
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, tag, "input");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, tag, "output");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, tag, "hasrecipe");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        List<BlockPos> allPositions = getValidWorkCardPositions(pedestal, coin, getWorkCardType(),MODID);
        if (allPositions.isEmpty()) return;

        crafterAction(level, pedestal, allPositions);
    }

    public void resetCachedRecipe(String modID, ItemStack stackToStoreNBT) {
        CompoundTag tag = stackToStoreNBT.getOrCreateTag();
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "craft_ingredients");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "craft_result");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "craft_cached");
    }

    private ItemStack getAndCacheCraftingResult(String modID, Level level, ItemStack stackToStoreNBT, List<ItemStack> ingredients) {
        int numIngredients = ingredients.size();
        int dimension = (int)Math.ceil(Math.sqrt(numIngredients));
        CraftingContainer craftingContainer = new TransientCraftingContainer(MowLibContainerUtils.getAbstractContainerMenu(40), dimension, dimension);
        for (int i = 0; i < numIngredients; i++) {
            craftingContainer.setItem(i, ingredients.get(i));
        }

        RecipeManager recipeManager = level.getRecipeManager();
        Optional<CraftingRecipe> result = recipeManager.getRecipeFor(RecipeType.CRAFTING, craftingContainer, level);
        if (result.isPresent()) {
            CraftingRecipe recipe = result.get();
            ItemStack resultItem;
            if (recipe instanceof RepairItemRecipe) {
                resultItem = recipe.assemble(craftingContainer,level.registryAccess());
            } else {
                resultItem = recipe.getResultItem(level.registryAccess());
            }
            CompoundTag tag = stackToStoreNBT.getOrCreateTag();
            MowLibCompoundTagUtils.writeItemStackListToNBT(modID, tag, ingredients, "craft_ingredients");
            MowLibCompoundTagUtils.writeItemStackToNBT(modID, tag, resultItem,"craft_result");
            MowLibCompoundTagUtils.writeBooleanToNBT(modID, tag, true,"craft_cached");
            return resultItem;
        } else {
            MowLibCompoundTagUtils.writeBooleanToNBT(modID, stackToStoreNBT.getOrCreateTag(), false,"craft_cached");
            return ItemStack.EMPTY;
        }
    }

    private boolean hasCachedRecipe(String modID, ItemStack stackToStoreNBT) {
        return MowLibCompoundTagUtils.readBooleanFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "craft_cached");
    }

    private boolean cachedRecipeHasSameInputs(String modID, ItemStack stackToStoreNBT, List<ItemStack> ingredients) {
        List<ItemStack> cachedIngredients = MowLibCompoundTagUtils.readItemStackListFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "craft_ingredients");
        if (ingredients.size() == cachedIngredients.size()) {
            for (int i = 0; i < ingredients.size(); i++) {
                ItemStack ingredient = ingredients.get(i);
                ItemStack cachedIngredient = cachedIngredients.get(i);
                if (!ItemHandlerHelper.canItemStacksStack(ingredient, cachedIngredient)) {
                    if (!(ingredient.isEmpty() && cachedIngredient.isEmpty())) { // ItemStack.EMPTY doesn't "stack" but we want to consider them a match
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    // TODO: move this (and `hasCachedRecipe`, `cachedRecipeHasSameInputs`, `getAndCacheCraftingResult` and `resetCachedRecipe`) to MowLib?
    public ItemStack getCraftingResult(String modID, Level level, ItemStack stackToStoreNBT, List<ItemStack> ingredients) {
        if (hasCachedRecipe(modID, stackToStoreNBT) && cachedRecipeHasSameInputs(modID, stackToStoreNBT, ingredients)) {
            return MowLibCompoundTagUtils.readItemStackFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "craft_result");
        }
        return getAndCacheCraftingResult(modID, level, stackToStoreNBT, ingredients);
    }

    public void crafterAction(Level level, BasePedestalBlockEntity crafterPedestal, List<BlockPos> allPositions) {
        if(!level.isClientSide()) {
            BlockPos crafterBlockPos = crafterPedestal.getPos();
            BlockPos crafterParticleBlockPos = getPosOfBlockBelow(level, crafterBlockPos, -1);

            List<BasePedestalBlockEntity> ingredientPedestals = allPositions.stream()
                .map(level::getBlockEntity)
                .flatMap(entity -> {
                    if (entity instanceof BasePedestalBlockEntity pedestalEntity) {
                        return Stream.of(pedestalEntity);
                    } else {
                        return Stream.empty();
                    }
                })
                .toList();
            if (ingredientPedestals.size() != allPositions.size()) { // some locations weren't pedestals
                if(crafterPedestal.canSpawnParticles()) {
                    MowLibPacketHandler.sendToNearby(level, crafterBlockPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, crafterParticleBlockPos.getX(), crafterParticleBlockPos.getY(), crafterParticleBlockPos.getZ(), 255, 0, 0));
                }
                return;
            }

            List<ItemStack> ingredientList = ingredientPedestals.stream()
                .map(BasePedestalBlockEntity::getItemInPedestal).toList();
            ItemStack craftingResult = getCraftingResult(MODID, level, crafterPedestal.getCoinOnPedestal(), ingredientList);
            if (!craftingResult.isEmpty()) {
                if (crafterPedestal.addItem(craftingResult, true)) {
                    for (BasePedestalBlockEntity ingredientPedestal: ingredientPedestals) {
                        ItemStack toRemove = ingredientPedestal.getItemInPedestal().copy();
                        toRemove.setCount(1);

                        ingredientPedestal.removeItemStack(toRemove, false);
                        if (toRemove.hasCraftingRemainingItem()) {
                            ingredientPedestal.addItem(toRemove.getCraftingRemainingItem(), false);
                        }
                        if (ingredientPedestal.canSpawnParticles()) {
                            BlockPos particleBlockPos = getPosOfBlockBelow(level, ingredientPedestal.getPos(), -1);
                            MowLibPacketHandler.sendToNearby(level, ingredientPedestal.getPos(), new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, particleBlockPos.getX(), particleBlockPos.getY(), particleBlockPos.getZ(), 50, 200, 0));
                        }
                    }

                    if(crafterPedestal.addItem(craftingResult,true)) {
                        crafterPedestal.addItem(craftingResult,false);
                    } else {
                        BlockPos pedestalPos = getPosOfBlockBelow(level,crafterBlockPos,-1);
                        ItemEntity itemEntity = new ItemEntity(level, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), craftingResult);
                        itemEntity.setDefaultPickUpDelay();
                        itemEntity.setDeltaMovement(0.0, 0.0, 0.0);
                        itemEntity.moveTo(Vec3.atCenterOf(pedestalPos));
                        level.addFreshEntity(itemEntity);
                    }

                    if(crafterPedestal.canSpawnParticles()) {
                        MowLibPacketHandler.sendToNearby(level, crafterBlockPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, crafterParticleBlockPos.getX(), crafterParticleBlockPos.getY(), crafterParticleBlockPos.getZ(), 0, 255, 0));
                    }
                } else {
                    if(crafterPedestal.canSpawnParticles()) {
                        MowLibPacketHandler.sendToNearby(level, crafterBlockPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, crafterParticleBlockPos.getX(), crafterParticleBlockPos.getY(), crafterParticleBlockPos.getZ(), 50, 50, 50));
                    }
                }
            }
        }
    }
}