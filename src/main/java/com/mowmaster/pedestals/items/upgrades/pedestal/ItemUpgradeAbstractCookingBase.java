package com.mowmaster.pedestals.items.upgrades.pedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.pedestalutils.MoveToMowLibUtils;
import com.mowmaster.pedestals.pedestalutils.References;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.*;

public abstract class ItemUpgradeAbstractCookingBase<T extends AbstractCookingRecipe> extends ItemUpgradeBase {
    private final RecipeType<T> recipeType;

    public ItemUpgradeAbstractCookingBase(Properties p_41383_, RecipeType<T> rType) {
        super(p_41383_);
        recipeType = rType;
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) { return true; }

    @Override
    public int getMaxSpeed(ItemStack upgradeItemStack) { return 200; }

    @Override
    public int baseEnergyCostPerDistance() { return baseEnergyCost(); }

    abstract public int baseEnergyCost();

    @Override
    public int getUpgradeWorkRange(ItemStack coinUpgrade) { return 0; }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {
        List<String> messages = new ArrayList<>();

        if (pedestal.getItemInPedestal().isEmpty()) {
            if (baseEnergyCost() > 0) {
                boolean hasItem = MowLibCompoundTagUtils.readBooleanFromNBT(References.MODID,pedestal.getCoinOnPedestal().getOrCreateTag(),"hasitem");
                if (pedestal.getStoredEnergy() < baseEnergyCost() && hasItem) {
                    messages.add(ChatFormatting.RED + "Needs Energy");
                }
            }
        }

        return messages;
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        MoveToMowLibUtils.resetCachedAbstractCooking(References.MODID, coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getOrCreateTag(), "hasitem");

        // TODO [1.20]: get rid of these lines as they remove the previous NBT tags used.
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getOrCreateTag(), "input");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getOrCreateTag(), "output");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getOrCreateTag(), "cooktime");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getOrCreateTag(), "xpamount");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID, coinInPedestal.getOrCreateTag(), "_numdelay");
    }

    private void updateHasItemNBT(BasePedestalBlockEntity pedestal, boolean state) {
        CompoundTag tag = pedestal.getCoinOnPedestal().getOrCreateTag();
        if (MowLibCompoundTagUtils.readBooleanFromNBT(References.MODID, tag, "hasitem") != state) {
            MowLibCompoundTagUtils.writeBooleanToNBT(References.MODID, tag, state, "hasitem");
            pedestal.update();
        }
    }

    @Override
    public void updateAction(Level level, BasePedestalBlockEntity pedestal) {
        BlockPos pedestalPos = pedestal.getPos();
        BlockPos inventoryPos = getPosOfBlockBelow(level, pedestalPos,1);
        if (level.getBlockEntity(inventoryPos) instanceof BasePedestalBlockEntity) {
            updateHasItemNBT(pedestal, false);
            return;
        }

        MowLibItemUtils.findItemHandlerAtPos(level, inventoryPos, getPedestalFacing(level, pedestalPos), true).map(handler ->
            getFirstSlotWithNonMachineFilteredItems(pedestal, handler).map(slot -> {
                ItemStack upgrade = pedestal.getCoinOnPedestal();
                ItemStack inputItem = handler.getStackInSlot(slot);

                ItemStack cookResult = MoveToMowLibUtils.getAbstractCookingResult(References.MODID, level, upgrade, inputItem, recipeType);
                if (!cookResult.isEmpty()) {
                    int startingElapsedCookTime = MoveToMowLibUtils.getCookTimeElapsed(References.MODID, upgrade);
                    int newElapsedCookTime = startingElapsedCookTime;
                    int requiredCookTime = MoveToMowLibUtils.getCookTimeRequired(References.MODID, upgrade);
                    boolean spawnParticles = false;
                    int particleRed = 0, particleGreen = 0, particleBlue = 0;

                    // 1: Advance Cook Time
                    if (newElapsedCookTime < requiredCookTime) { // skip if we didn't fully output items in a previous run
                        int timeToElapseBasedOnSpeed = Math.max(1, getSpeedTicksReduced(upgrade));
                        int timeToElapseBasedOnAvailableEnergy = pedestal.getStoredEnergy() / baseEnergyCost();
                        int timeToElapse = Math.min(timeToElapseBasedOnSpeed, timeToElapseBasedOnAvailableEnergy);

                        int energyCost = baseEnergyCost() * timeToElapse;
                        if (energyCost > 0 && pedestal.removeEnergy(energyCost, true) == energyCost) {
                            pedestal.removeEnergy(energyCost, false);
                            newElapsedCookTime += timeToElapse;
                            spawnParticles = true;
                        }
                    }

                    // 2: Output Cooked items
                    if (newElapsedCookTime >= requiredCookTime) {
                        int amountAvailableToCook = inputItem.getCount();
                        int amountCompletedCooks = newElapsedCookTime / requiredCookTime; // integer division already floors
                        int amountToAdd = Math.min(amountAvailableToCook, amountCompletedCooks);

                        cookResult.setCount(amountToAdd);

                        ItemStack leftover = pedestal.addItemStack(cookResult, false);
                        int amountAdded = amountToAdd - leftover.getCount();
                        handler.extractItem(slot, amountAdded, false);
                        pedestal.addExperience(MoveToMowLibUtils.getXpGainFromCachedRecipe(References.MODID, upgrade) * amountAdded, false); // excess experience is currently lost
                        newElapsedCookTime -= requiredCookTime * amountAdded;

                        if (amountAdded > 0) {
                            particleRed = 255; particleGreen = 150;
                            spawnParticles = true;
                        }
                    }

                    // 3: Update Necessary State
                    if (newElapsedCookTime != startingElapsedCookTime) {
                        MoveToMowLibUtils.setCookTimeElapsed(References.MODID, upgrade, newElapsedCookTime);
                    }

                    // 4: Spawn Particles
                    if (spawnParticles && pedestal.canSpawnParticles()) {
                        MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, pedestalPos.getX(), pedestalPos.getY() + 0.75D, pedestalPos.getZ(), particleRed, particleGreen, particleBlue));
                    }
                }
                return slot;
            })
        ).ifPresentOrElse(
            maybeSlot -> updateHasItemNBT(pedestal, maybeSlot.isPresent()),
            () -> updateHasItemNBT(pedestal, false)
        );
    }
}
