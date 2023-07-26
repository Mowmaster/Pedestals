package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardArea;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardBase;
import com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradePump extends ItemUpgradeBase {
    public ItemUpgradePump(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyBlockCapacity(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyRange(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean canModifyArea(ItemStack upgradeItemStack) { return PedestalConfig.COMMON.upgrade_require_sized_selectable_area.get(); }

    @Override
    public boolean canModifyOperateToBedrock(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean needsWorkCard(ItemStack upgradeItemStack) { return true; }

    @Override
    public int getWorkCardType() { return 1; }

    //Requires energy

    @Override
    public int baseEnergyCostPerDistance() { return PedestalConfig.COMMON.upgrade_pump_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_pump_energy_distance_multiplier.get(); }
    @Override
    public double energyCostMultiplier() { return PedestalConfig.COMMON.upgrade_pump_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance() { return PedestalConfig.COMMON.upgrade_pump_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_pump_xp_distance_multiplier.get(); }
    @Override
    public double xpCostMultiplier() { return PedestalConfig.COMMON.upgrade_pump_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance() { return new DustMagic(PedestalConfig.COMMON.upgrade_pump_dustColor.get(),PedestalConfig.COMMON.upgrade_pump_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_pump_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier() { return PedestalConfig.COMMON.upgrade_pump_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_pump_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier() { return PedestalConfig.COMMON.upgrade_pump_selectedMultiplier.get(); }

    public boolean removeWaterFromLoggedBlocks() { return PedestalConfig.COMMON.upgrade_pump_waterlogged.get(); }

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
        resetCachedValidWorkCardPositions(MODID, coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numposition");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numheight");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_boolstop");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_miny");
        // TODO: remove at some point? (it's fine if the stale NBT sticks around)
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numdelay");
    }

    private boolean getStopped(ItemStack upgrade) {
        return MowLibCompoundTagUtils.readBooleanFromNBT(MODID, upgrade.getOrCreateTag(), "_boolstop");
    }

    private void setStopped(ItemStack upgrade) {
        MowLibCompoundTagUtils.writeBooleanToNBT(MODID, upgrade.getOrCreateTag(), true, "_boolstop");
    }

    private int getNumBlocksPerOperation(ItemStack upgrade) {
        return PedestalConfig.COMMON.upgrade_pump_baseBlocksPumped.get() + getBlockCapacityIncrease(upgrade);
    }

    private int getCurrentHeight(ItemStack upgrade) {
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, upgrade.getOrCreateTag(), "_numheight");
    }

    private void setCurrentHeight(ItemStack upgrade, int num) {
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, upgrade.getOrCreateTag(), num, "_numheight");
    }

    private int getMinY(ItemStack upgrade) {
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, upgrade.getOrCreateTag(), "_miny");
    }

    private void setMinY(ItemStack upgrade, int minY) {
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, upgrade.getOrCreateTag(), minY, "_miny");
    }

    private int getCurrentPosition(ItemStack upgrade) {
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, upgrade.getOrCreateTag(), "_numposition");
    }

    private void setCurrentPosition(ItemStack upgrade, int num) {
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, upgrade.getOrCreateTag(), num, "_numposition");
    }

    private boolean canPump(BasePedestalBlockEntity pedestal, Block targetBlock, FluidStack targetFluidStack) {
        List<Block> cannotPumpBlocks = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cant_pump"))).stream().toList();

        return !cannotPumpBlocks.contains(targetBlock) &&
            pedestal.addFluid(targetFluidStack, IFluidHandler.FluidAction.SIMULATE) > 0;
    }

    private FluidStack getFluidStackForTarget(Block targetBlock, FluidState targetFluidState) {
        if (targetBlock instanceof AbstractCauldronBlock) {
            if (targetBlock.equals(Blocks.WATER_CAULDRON)) {
                return new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
            } else if (targetBlock.equals(Blocks.LAVA_CAULDRON)) {
                return new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME);
            }
        }
        if (targetFluidState.isSource()) {
            return new FluidStack(targetFluidState.getType(), FluidType.BUCKET_VOLUME);
        }
        return FluidStack.EMPTY;
    }

    private boolean canPlace(ItemStack toPlace) {
        Block possibleBlock = Block.byItem(toPlace.getItem());
        List<Block> cannotPlaceBlocks = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_place"))).stream().toList();

        return possibleBlock != Blocks.AIR &&
            !cannotPlaceBlocks.contains(possibleBlock) &&
            !( // prevent everything supported by `ItemUpgradePlanter`
                possibleBlock instanceof IPlantable ||
                possibleBlock instanceof ChorusFlowerBlock ||
                possibleBlock instanceof GrowingPlantBlock
            );
    }

    public List<BlockPos> getValidWorkCardPositionsPump(Level level, BasePedestalBlockEntity pedestal, ItemStack upgrade) {
        List<BlockPos> cached = readBlockPosListCustomFromNBT(MODID, "_validlist", upgrade);
        if (cached.size() == 0) {
            // Optimization to construct the validlist only once. The NBT tag should be reset when the WorkCard/Upgrade
            // is removed (as that is the only way to invalidate the cached list) by calling `resetCachedValidWorkCardPositions`.
            if (!hasBlockListCustomNBTTags(MODID, "_validlist", upgrade) && pedestal.hasWorkCard()) {
                ItemStack workCardItemStack = pedestal.getWorkCardInPedestal();
                if (workCardItemStack.getItem() instanceof WorkCardArea) {
                    cached = WorkCardArea.getAABBIfDefinedAndInRange(workCardItemStack, pedestal, getUpgradeWorkRange(upgrade))
                        .map(area -> {
                            List<BlockPos> locations = new ArrayList<>();
                            int minY = (int)area.minY;
                            int maxY = (int)area.maxY;
                            if (hasOperateToBedrock(upgrade)) {
                                if (minY != maxY) { // invalid selection
                                    return locations;
                                }
                                setCurrentHeight(upgrade, maxY);
                                setMinY(upgrade, level.getMinBuildHeight());
                            } else {
                                setMinY(upgrade, minY);
                            }
                            for (int x = (int)area.maxX; x >= (int)area.minX; x--) {
                                for (int z = (int)area.maxZ; z >= (int)area.minZ; z--) {
                                    for (int y = maxY; y >= minY; y -= getNumBlocksPerOperation(upgrade)) {
                                        locations.add(new BlockPos(x, y, z));
                                    }
                                }
                            }
                            return locations;
                        }).orElse(List.of());
                }
                saveBlockPosListCustomToNBT(MODID, "_validlist", upgrade, cached);
            }
        }
        return cached;
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        if (level.isClientSide()) return;
        if (getStopped(coin)) {
            if (pedestal.canSpawnParticles()) {
                MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, pedestalPos.getX(), pedestalPos.getY() + 1.0f, pedestalPos.getZ(), 255, 55, 55));
            }
            return;
        }

        List<BlockPos> allPositions = getValidWorkCardPositionsPump(level, pedestal, coin);
        if (allPositions.isEmpty()) return;

        int currentPosition = getCurrentPosition(coin);
        BlockPos currentPoint = allPositions.get(currentPosition);
        int currentY = hasOperateToBedrock(coin) ? getCurrentHeight(coin) : currentPoint.getY();
        int currentMinY = Math.max(currentY - getNumBlocksPerOperation(coin) + 1, getMinY(coin));
        List<BlockPos> adjustedPoints = IntStream.rangeClosed(currentMinY, currentY)
            .mapToObj(y -> new BlockPos(currentPoint.getX(), y, currentPoint.getZ())).toList();
        if (pedestal.spaceForFluid() >= (FluidType.BUCKET_VOLUME * adjustedPoints.size()) && pumpAction(level, pedestal, pedestalPos, adjustedPoints)) {
            if (currentPosition + 1 >= allPositions.size()) {
                setCurrentPosition(coin, 0);
                if (hasOperateToBedrock(coin)) {
                    setCurrentHeight(coin, currentMinY - 1);
                    if (currentMinY <= getMinY(coin)) {
                        setStopped(coin);
                    }
                }
            } else {
                setCurrentPosition(coin, currentPosition + 1);
            }
        }
    }

    public boolean pumpAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, List<BlockPos> adjustedPoints) {
        WeakReference<FakePlayer> fakePlayerReference = pedestal.getPedestalPlayer(pedestal);
        if(fakePlayerReference != null && fakePlayerReference.get() != null) {
            FakePlayer fakePlayer = fakePlayerReference.get();

            List<Integer> distances = adjustedPoints.stream().map(point -> getDistanceBetweenPoints(pedestalPos, point)).toList();
            if (removeFuelForActionMultiple(pedestal, distances, true)) {
                for (BlockPos adjustedPoint : adjustedPoints) {
                    if (adjustedPoint.equals(pedestalPos)) {
                        continue;
                    }
                    BlockState targetBlockState = level.getBlockState(adjustedPoint);
                    Block targetBlock = targetBlockState.getBlock();
                    FluidStack targetFluidStack = getFluidStackForTarget(targetBlock, level.getFluidState(adjustedPoint));

                    if (canPump(pedestal, targetBlock, targetFluidStack) && passesMachineFilterFluids(pedestal, targetFluidStack)) {
                        if (pedestal.canAcceptFluid(targetFluidStack) && pedestal.spaceForFluid() >= FluidType.BUCKET_VOLUME) {
                            if (removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestalPos, adjustedPoint), false)) {
                                pedestal.addFluid(targetFluidStack, IFluidHandler.FluidAction.EXECUTE);

                                if (targetBlockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                    if (removeWaterFromLoggedBlocks()) {
                                        level.setBlockAndUpdate(adjustedPoint, targetBlockState.setValue(BlockStateProperties.WATERLOGGED, false));
                                    }
                                } else if (targetBlock instanceof AbstractCauldronBlock) {
                                    level.setBlockAndUpdate(adjustedPoint, Blocks.CAULDRON.defaultBlockState());
                                } else {
                                    level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                    ItemStack toPlace = pedestal.getItemInPedestal().copy();
                                    if (!pedestal.removeItemStack(toPlace, true).isEmpty() && canPlace(toPlace) && passesMachineFilterItems(pedestal, toPlace)) {
                                        ItemStack toRemove = toPlace.copy(); // UseOnContext modifies the passed in item stack when returning an InteractionResult.CONSUME, so we need a copy of it for removal.
                                        UseOnContext blockContext = new UseOnContext(level, fakePlayer, InteractionHand.MAIN_HAND, toPlace, new BlockHitResult(Vec3.ZERO, getPedestalFacing(level, pedestalPos), adjustedPoint, false));
                                        InteractionResult resultPlace = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                                        if (resultPlace == InteractionResult.CONSUME) {
                                            toRemove.setCount(1); // only remove 1 item
                                            pedestal.removeItemStack(toRemove,false);
                                        }
                                    }
                                }
                            } else {
                                return false; // should be impossible
                            }
                        }
                    }
                }
                return true;
            } else {
                if (pedestal.canSpawnParticles()) {
                    MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, pedestalPos.getX(), pedestalPos.getY() + 1.0f, pedestalPos.getZ(), 55, 55, 55));
                }
                return false;
            }
        }
        return false;
    }
}
