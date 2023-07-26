package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardArea;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.List;

public class ItemUpgradeMilker extends ItemUpgradeBase {
    public ItemUpgradeMilker(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifySuperSpeed(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyRange(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyArea(ItemStack upgradeItemStack) { return PedestalConfig.COMMON.upgrade_require_sized_selectable_area.get(); }

    @Override
    public boolean needsWorkCard(ItemStack upgradeItemStack) { return true; }

    @Override
    public int getWorkCardType() { return 1; }

    //Requires energy
    @Override
    public int baseEnergyCostPerDistance() { return PedestalConfig.COMMON.upgrade_milker_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_milker_energy_distance_multiplier.get(); }
    @Override
    public double energyCostMultiplier() { return PedestalConfig.COMMON.upgrade_milker_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance() { return PedestalConfig.COMMON.upgrade_milker_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_milker_xp_distance_multiplier.get(); }
    @Override
    public double xpCostMultiplier() { return PedestalConfig.COMMON.upgrade_milker_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance() { return new DustMagic(PedestalConfig.COMMON.upgrade_milker_dustColor.get(),PedestalConfig.COMMON.upgrade_milker_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_milker_dust_distance_multiplier.get(); }
    @Override
    public double dustCostMultiplier() { return PedestalConfig.COMMON.upgrade_milker_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_milker_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier() { return PedestalConfig.COMMON.upgrade_milker_selectedMultiplier.get(); }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {
        List<String> messages = super.getUpgradeHUD(pedestal);
        if (messages.isEmpty()) {
            if(baseEnergyCostPerDistance() > 0 && pedestal.getStoredEnergy() < baseEnergyCostPerDistance()) {
                messages.add(ChatFormatting.RED + "Needs Energy");
                messages.add(ChatFormatting.RED + "To Operate");
            }
            if (baseXpCostPerDistance() > 0 && pedestal.getStoredExperience() < baseXpCostPerDistance()) {
                messages.add(ChatFormatting.GREEN + "Needs Experience");
                messages.add(ChatFormatting.GREEN + "To Operate");
            }
            if (baseDustCostPerDistance().getDustAmount() > 0 && pedestal.getStoredEnergy() < baseEnergyCostPerDistance()) {
                messages.add(ChatFormatting.LIGHT_PURPLE + "Needs Dust");
                messages.add(ChatFormatting.LIGHT_PURPLE + "To Operate");
            }
        }

        return messages;
    }

    @Override
    public ItemStack getUpgradeDefaultTool() {
        return new ItemStack(Items.BUCKET);
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        if (level.isClientSide()) return;
        if (!removeFuelForAction(pedestal, 0, true)) return;

        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        boolean usingItem = !itemInPedestal.isEmpty();
        ItemStack toolStack;
        if (usingItem) {
            toolStack = itemInPedestal;
        } else {
            toolStack = pedestal.getToolStack();
            Fluid milk = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("minecraft:milk"));
            if (milk != null && !milk.isSame(Fluids.EMPTY)) return; // no mods have called `ForgeMod.enableMilkFluid()`, and thus the milk fluid does not exist.
            if (pedestal.spaceForFluid() < FluidType.BUCKET_VOLUME) return;
        }

        WeakReference<FakePlayer> fakePlayerReference = pedestal.getPedestalPlayer(pedestal);
        if (fakePlayerReference != null && fakePlayerReference.get() != null) {
            FakePlayer fakePlayer = fakePlayerReference.get();
            ItemStack workCardItemStack = pedestal.getWorkCardInPedestal();
            if (workCardItemStack.getItem() instanceof WorkCardArea) {
                List<Animal> animals = WorkCardArea.getEntitiesInRangeOfUpgrade(level, Animal.class, workCardItemStack, pedestal);
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, toolStack.copy());
                for (Animal animal : animals) {
                    BlockPos animalPos = animal.getOnPos();
                    InteractionResult result = animal.mobInteract(fakePlayer, InteractionHand.MAIN_HAND);
                    if (result == InteractionResult.CONSUME) {
                        for (ItemStack stackInPlayer : fakePlayer.getInventory().items) {
                            if (!stackInPlayer.isEmpty() && !toolStack.getItem().equals(stackInPlayer.getItem())) {
                                if (removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestalPos, animalPos), false)) {
                                    if (usingItem) {
                                        ItemStack toRemove = toolStack.copy();
                                        toRemove.setCount(1);
                                        if (!pedestal.removeItemStack(toRemove, false).isEmpty()) {
                                            MowLibItemUtils.spawnItemStack(level, animalPos.getX(), animalPos.getY(), animalPos.getZ(), stackInPlayer);
                                            if (!hasSuperSpeed(coin)) return;
                                        }
                                    } else if (stackInPlayer.getItem() instanceof BucketItem bucket) {
                                        if (!bucket.getFluid().isSame(Fluids.EMPTY)) {
                                            FluidStack fluidStack = new FluidStack(bucket.getFluid(), FluidType.BUCKET_VOLUME);
                                            if (pedestal.addFluid(fluidStack, IFluidHandler.FluidAction.EXECUTE) > 0) {
                                                if (!hasSuperSpeed(coin)) return;
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
}
