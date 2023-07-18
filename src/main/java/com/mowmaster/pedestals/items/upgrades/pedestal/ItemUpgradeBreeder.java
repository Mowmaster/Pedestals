package com.mowmaster.pedestals.items.upgrades.pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.configs.PedestalConfig;
import com.mowmaster.pedestals.items.workcards.WorkCardArea;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;

import java.lang.ref.WeakReference;
import java.util.List;

public class ItemUpgradeBreeder extends ItemUpgradeBase {
    public ItemUpgradeBreeder(Properties p_41383_) {
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
    public boolean canModifyArea(ItemStack upgradeItemStack) { return PedestalConfig.COMMON.upgrade_require_sized_selectable_area.get(); }

    @Override
    public boolean needsWorkCard() { return true; }

    @Override
    public int getWorkCardType() { return 1; }

    //Requires energy
    @Override
    public int baseEnergyCostPerDistance() { return PedestalConfig.COMMON.upgrade_breeder_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_breeder_energy_distance_multiplier.get(); }
    @Override
    public double energyCostMultiplier() { return PedestalConfig.COMMON.upgrade_breeder_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance() { return PedestalConfig.COMMON.upgrade_breeder_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_breeder_xp_distance_multiplier.get(); }
    @Override
    public double xpCostMultiplier() { return PedestalConfig.COMMON.upgrade_breeder_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance() { return new DustMagic(PedestalConfig.COMMON.upgrade_breeder_dustColor.get(),PedestalConfig.COMMON.upgrade_breeder_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_breeder_dust_distance_multiplier.get(); }
    @Override
    public double dustCostMultiplier() { return PedestalConfig.COMMON.upgrade_breeder_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_breeder_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier() { return PedestalConfig.COMMON.upgrade_breeder_selectedMultiplier.get(); }

    public boolean hasBreederLimit() { return PedestalConfig.COMMON.upgrade_breeder_entityBreedingLimit.get(); }
    public int getBreederLimitCount() { return PedestalConfig.COMMON.upgrade_breeder_entityLimitBreedingCount.get(); }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {
        List<String> messages = super.getUpgradeHUD(pedestal);

        if (messages.size() == 0) {
            if (baseEnergyCostPerDistance() > 0) {
                if (pedestal.getStoredEnergy() < baseEnergyCostPerDistance()) {
                    messages.add(ChatFormatting.RED + "Needs Energy");
                    messages.add(ChatFormatting.RED + "To Operate");
                }
            }
            if (baseXpCostPerDistance() > 0) {
                if (pedestal.getStoredExperience() < baseXpCostPerDistance()) {
                    messages.add(ChatFormatting.GREEN + "Needs Experience");
                    messages.add(ChatFormatting.GREEN + "To Operate");
                }
            }
            if (baseDustCostPerDistance().getDustAmount() > 0) {
                if (pedestal.getStoredEnergy() < baseEnergyCostPerDistance()) {
                    messages.add(ChatFormatting.LIGHT_PURPLE + "Needs Dust");
                    messages.add(ChatFormatting.LIGHT_PURPLE + "To Operate");
                }
            }
        }

        return messages;
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        WeakReference<FakePlayer> fakePlayerReference = pedestal.getPedestalPlayer(pedestal);
        if (fakePlayerReference != null && fakePlayerReference.get() != null) {
            FakePlayer fakePlayer = fakePlayerReference.get();
            ItemStack workCardItemStack = pedestal.getWorkCardInPedestal();
            if (workCardItemStack.getItem() instanceof WorkCardArea) {
                List<Animal> animals = WorkCardArea.getEntitiesInRangeOfUpgrade(level, Animal.class, workCardItemStack, pedestal);
                if (hasBreederLimit() && animals.size() >= getBreederLimitCount()) {
                    return;
                }

                for (Animal animal : animals) {
                    ItemStack stackInPedestal = pedestal.hasItem() ? pedestal.getItemInPedestal() : pedestal.getToolStack();
                    if (stackInPedestal.isEmpty()) {
                        return;
                    }

                    if (
                        animal.isFood(stackInPedestal) &&
                        animal.getAge() == 0 &&
                        animal.canFallInLove() &&
                        removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(), animal.getOnPos()), true)
                    ) {
                        ItemStack toFeed = stackInPedestal.copy();
                        toFeed.setCount(1);
                        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, toFeed.copy());
                        InteractionResult result = animal.mobInteract(fakePlayer, InteractionHand.MAIN_HAND);
                        if (result == InteractionResult.SUCCESS) {
                            removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(), animal.getOnPos()), false);
                            pedestal.removeItemStack(toFeed, false);
                        }
                    }
                }
            }
        }
    }
}
