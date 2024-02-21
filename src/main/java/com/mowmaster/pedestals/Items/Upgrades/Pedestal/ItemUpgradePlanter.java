package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradePlanter extends ItemUpgradeBase {
    public ItemUpgradePlanter(Properties p_41383_) {
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
    public boolean canModifySuperSpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyWorkPositionSkip(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean needsWorkCard(ItemStack upgradeItemStack) { return true; }

    @Override
    public int getWorkCardType() { return 0; }

    //Requires energy
    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_planter_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_planter_energy_distance_multiplier.get(); }
    @Override
    public double energyCostMultiplier() { return PedestalConfig.COMMON.upgrade_planter_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance() { return PedestalConfig.COMMON.upgrade_planter_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_planter_xp_distance_multiplier.get(); }
    @Override
    public double xpCostMultiplier() { return PedestalConfig.COMMON.upgrade_planter_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance() { return new DustMagic(PedestalConfig.COMMON.upgrade_planter_dustColor.get(),PedestalConfig.COMMON.upgrade_planter_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_planter_dust_distance_multiplier.get(); }
    @Override
    public double dustCostMultiplier() { return PedestalConfig.COMMON.upgrade_planter_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_planter_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier() { return PedestalConfig.COMMON.upgrade_planter_selectedMultiplier.get(); }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {
        List<String> messages = super.getUpgradeHUD(pedestal);
        if (messages.isEmpty()) {
            if (baseEnergyCostPerDistance() > 0 && pedestal.getStoredEnergy() < baseEnergyCostPerDistance()) {
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
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        resetCachedValidWorkCardPositions(coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numposition");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        if (level.isClientSide()) return;

        List<BlockPos> allPositions = getValidWorkCardPositions(pedestal);
        if (allPositions.isEmpty()) return;

        if (hasSuperSpeed(pedestal.getCoinOnPedestal())) {
            for (BlockPos plantAtPos: allPositions) {
                planterAction(level, pedestal, plantAtPos,!hasWorkPositionSkip(pedestal.getCoinOnPedestal()));
            }
        } else {
            int currentPosition = getCurrentPosition(pedestal);
            if (planterAction(level, pedestal, allPositions.get(currentPosition),!hasWorkPositionSkip(pedestal.getCoinOnPedestal()))) {
                if (currentPosition + 1 == allPositions.size()) {
                    setCurrentPosition(pedestal, 0);
                } else {
                    setCurrentPosition(pedestal, currentPosition + 1);
                }
            }
        }
    }

    private int getCurrentPosition(BasePedestalBlockEntity pedestal) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coin.getOrCreateTag(), "_numposition");
    }

    private void setCurrentPosition(BasePedestalBlockEntity pedestal, int num) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), num, "_numposition");
    }

    private boolean passesFilter(BasePedestalBlockEntity pedestal, ItemStack toPlant) {
        if (pedestal.hasFilter()) {
            ItemStack filterInPedestal = pedestal.getFilterInPedestal();
            if (pedestal.getFilterInPedestal().getItem() instanceof BaseFilter filter) {
                if (filter.getFilterDirection().neutral()) {
                    return filter.canAcceptItems(filterInPedestal, toPlant);
                }
            }
        }

        return true;
    }

    private boolean canPlace(Level level, Direction pedestalFacing, BlockPos plantAtPos, ItemStack toPlace) {
        Block possibleBlock = Block.byItem(toPlace.getItem());
        List<Block> cannotPlaceBlocks = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_place"))).stream().toList();
        BlockState destinationBlockState = level.getBlockState((pedestalFacing == Direction.DOWN) ? plantAtPos.above() : plantAtPos.below());

        return possibleBlock != Blocks.AIR &&
            !cannotPlaceBlocks.contains(possibleBlock) && (
                (
                    possibleBlock instanceof IPlantable &&
                    destinationBlockState.canSustainPlant(level, getPosBasedOnPedestalFacing(plantAtPos, pedestalFacing), pedestalFacing, (IPlantable) possibleBlock)
                ) || (
                    possibleBlock instanceof ChorusFlowerBlock &&
                    ((ChorusFlowerBlock) possibleBlock).canSurvive(destinationBlockState, level, plantAtPos)
                ) || (
                    possibleBlock instanceof GrowingPlantBlock
                    // canSurvive on GrowingPlantBlock seems to not work even when it is a valid placement, but it
                    // shouldn't really be required as we check the InteractionResult when attempting to plant anyway.
                )
            );
    }

    private BlockPos getPosBasedOnPedestalFacing(BlockPos pos, Direction pedestalFacing) {
        return switch (pedestalFacing) {
            case UP -> pos.below();
            case DOWN -> pos.above();
            case NORTH -> pos.south();
            case EAST -> pos.west();
            case SOUTH -> pos.north();
            case WEST -> pos.east();
        };
    }

    //Added Boolean to allow skipping if the condition is valid but it cant work.
    public boolean planterAction(Level level, BasePedestalBlockEntity pedestal, BlockPos targetPos, Boolean skip) {
        WeakReference<FakePlayer> fakePlayerReference = pedestal.getPedestalPlayer(pedestal);
        if (fakePlayerReference != null && fakePlayerReference.get() != null) {
            FakePlayer fakePlayer = fakePlayerReference.get();
            Direction pedestalFacing = getPedestalFacing(level, pedestal.getPos());
            ItemStack toPlant = pedestal.getItemInPedestal().copy();
            toPlant.setCount(1);

            if (
                removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(), targetPos), true) &&
                    !pedestal.removeItemStack(toPlant, true).isEmpty() &&
                    !targetPos.equals(pedestal.getPos()) &&
                    passesFilter(pedestal, toPlant)
            ) {
                if (canPlace(level, pedestalFacing, targetPos, toPlant)) {
                    UseOnContext blockContext = new UseOnContext(level, fakePlayer, InteractionHand.MAIN_HAND, toPlant.copy(), new BlockHitResult(Vec3.ZERO, pedestalFacing, targetPos, false));
                    InteractionResult result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                    if (result == InteractionResult.CONSUME) {
                        removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(), targetPos), false);
                        pedestal.removeItemStack(toPlant, false);
                        if (pedestal.canSpawnParticles()) {
                            MowLibPacketHandler.sendToNearby(pedestal.getLevel(), pedestal.getPos(), new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR, targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, 100, 255, 100));
                        }
                        return true;
                    }
                    else
                    {
                        return skip;
                    }
                } else {
                    return true; // iterate over spots that can't be planted on (needed for the water in the center of most crop farms)
                }
            }
        }

        return false;
    }
}
