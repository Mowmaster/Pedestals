package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Recipes.UpgradeModificationGlobalRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.stream.Stream;

public class ItemUpgradeModifications extends ItemUpgradeBase {
    public ItemUpgradeModifications(Properties p_41383_) {
        super(p_41383_);
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
    public boolean needsWorkCard() { return true; }

    @Override
    public int getWorkCardType() { return 3; }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        resetCachedValidWorkCardPositions(coinInPedestal);
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        if (level.isClientSide()) return;

        List<BlockPos> allPositions = getValidWorkCardPositions(pedestal);
        if (allPositions.isEmpty()) return;

        modifierAction(level, pedestal, allPositions);
    }

    protected Optional<UpgradeModificationGlobalRecipe> getModificationGlobalRecipe(Level level, ItemStack upgradeInput, List<ItemStack> pedestalInputs) {
        List<ItemStack> filteredInputs = pedestalInputs.stream().filter(itemStack -> !itemStack.isEmpty()).toList();
        Container container = new SimpleContainer(filteredInputs.size() + 1);
        container.setItem(0, upgradeInput);
        for (int i = 0; i < filteredInputs.size(); i++) {
            container.setItem(i + 1, filteredInputs.get(i));
        }

        RecipeManager recipeManager = level.getRecipeManager();
        return recipeManager.getRecipeFor(UpgradeModificationGlobalRecipe.Type.INSTANCE, container, level);
    }

    public void modifierAction(Level level, BasePedestalBlockEntity pedestal, List<BlockPos> allPositions) {
        BlockPos inventoryPos = getPosOfBlockBelow(level,pedestal.getPos(),1);
        if (level.getBlockEntity(inventoryPos) instanceof BasePedestalBlockEntity) {
            return;
        }

        BlockPos modificationBlockPos = pedestal.getPos();
        BlockPos modificationParticleBlockPos = getPosOfBlockBelow(level, modificationBlockPos, -1);

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
            if(pedestal.canSpawnParticles()) {
                MowLibPacketHandler.sendToNearby(level, modificationBlockPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, modificationParticleBlockPos.getX(), modificationParticleBlockPos.getY(), modificationParticleBlockPos.getZ(), 255, 0, 0));
            }
            return;
        }

        List<ItemStack> ingredientList = ingredientPedestals.stream()
            .map(BasePedestalBlockEntity::getItemInPedestal).toList();

        MowLibItemUtils.findItemHandlerAtPos(level, inventoryPos, getPedestalFacing(level, modificationBlockPos), true).map(handler ->
            getFirstSlotWithNonFilteredItems(pedestal, handler).map(slot -> {
                ItemStack singleUpgradeItem = handler.getStackInSlot(slot).copy();
                singleUpgradeItem.setCount(1);
                int particleRed = 0, particleGreen = 0, particleBlue = 0;

                if (pedestal.addItem(singleUpgradeItem, true)) {
                    Optional<UpgradeModificationGlobalRecipe> globalRecipe = getModificationGlobalRecipe(level, singleUpgradeItem, ingredientList);
                    if (globalRecipe.isPresent()) {
                        if (globalRecipe.get().getResultingModifiedItem(singleUpgradeItem, false)) {
                            if (pedestal.addItem(singleUpgradeItem,false)) {
                                handler.extractItem(slot,1 ,false);
                                for (int i = 0; i < allPositions.size(); i++) {
                                    BasePedestalBlockEntity ingredientPedestal = ingredientPedestals.get(i);
                                    ItemStack toRemove = ingredientList.get(i).copy();
                                    if (!toRemove.isEmpty()) {
                                        toRemove.setCount(1);
                                        ingredientPedestal.removeItemStack(toRemove, false);
                                        if (pedestal.canSpawnParticles()) {
                                            BlockPos ingredientPedestalParticlePos = getPosOfBlockBelow(level, ingredientPedestal.getPos(), -1);
                                            MowLibPacketHandler.sendToNearby(level, ingredientPedestalParticlePos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, ingredientPedestalParticlePos.getX(), ingredientPedestalParticlePos.getY(), ingredientPedestalParticlePos.getZ(), 50, 200, 0));
                                        }
                                    }
                                }
                                particleGreen = 255;
                            }
                        }
                    }
                    if (pedestal.canSpawnParticles()) {
                        MowLibPacketHandler.sendToNearby(level, modificationParticleBlockPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, modificationParticleBlockPos.getX(), modificationParticleBlockPos.getY(), modificationParticleBlockPos.getZ(), particleRed, particleGreen, particleBlue));
                    }
                }
                return slot;
            })
        );

        /*
        Get Pedestals at each location
        (up to 9?)

        Upgrade Input is in the fist avail slot of the inv below the current pedestal(make sure it only takes one out at a time)

        The selected pedestals are the next 9 inputs
        (if there are more then 9, ignore them)

        Output item will be put in the current pedestal (to be transferred out)

        recipes in jei, have global ones and item specific ones.
        the base upgrade will be the 'input' for JEI to show all the global options
        for the item specific ones, let those inputs be how to find them in jei.

        Will need a text readout for what modification is applied and the rate of application and the max it can apply
         */

    }
}
