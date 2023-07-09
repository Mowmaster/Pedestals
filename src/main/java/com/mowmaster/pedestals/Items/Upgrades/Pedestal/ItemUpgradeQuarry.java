package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardArea;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeQuarry extends ItemUpgradeBase {
    public ItemUpgradeQuarry(Properties p_41383_) {
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
    public boolean needsWorkCard() { return true; }

    @Override
    public int getWorkCardType() { return 1; }

    //Requires energy
    @Override
    public int baseEnergyCostPerDistance() { return PedestalConfig.COMMON.upgrade_quarry_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_quarry_energy_distance_multiplier.get(); }
    @Override
    public double energyCostMultiplier() { return PedestalConfig.COMMON.upgrade_quarry_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance() { return PedestalConfig.COMMON.upgrade_quarry_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_quarry_xp_distance_multiplier.get(); }
    @Override
    public double xpCostMultiplier() { return PedestalConfig.COMMON.upgrade_quarry_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance() { return new DustMagic(PedestalConfig.COMMON.upgrade_quarry_dustColor.get(),PedestalConfig.COMMON.upgrade_quarry_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_quarry_dust_distance_multiplier.get(); }
    @Override
    public double dustCostMultiplier() { return PedestalConfig.COMMON.upgrade_quarry_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_quarry_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier() { return PedestalConfig.COMMON.upgrade_quarry_selectedMultiplier.get(); }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {
        List<String> messages = super.getUpgradeHUD(pedestal);
        if(messages.isEmpty()) {
            if(baseEnergyCostPerDistance() > 0 && pedestal.getStoredEnergy() < baseEnergyCostPerDistance()) {
                messages.add(ChatFormatting.RED + "Needs Energy");
                messages.add(ChatFormatting.RED + "To Operate");
            }
            if(baseXpCostPerDistance() > 0 && pedestal.getStoredExperience() < baseXpCostPerDistance()) {
                messages.add(ChatFormatting.GREEN + "Needs Experience");
                messages.add(ChatFormatting.GREEN + "To Operate");
            }
            if(baseDustCostPerDistance().getDustAmount() > 0 && pedestal.getStoredEnergy() < baseEnergyCostPerDistance()) {
                messages.add(ChatFormatting.LIGHT_PURPLE + "Needs Dust");
                messages.add(ChatFormatting.LIGHT_PURPLE + "To Operate");
            }
        }

        return messages;
    }

    @Override
    public ItemStack getUpgradeDefaultTool() {
        return new ItemStack(Items.IRON_PICKAXE);
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        removeBlockListCustomNBTTags(MODID,"_validlist",coinInPedestal);
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
    private boolean canMine(Block targetBlock) {
        List<Block> canChopCannotQuarryBlocks = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_can_chop_cant_quarry"))).stream().toList();
        List<Block> cannotBreakBlocks = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_break"))).stream().toList();

        return !targetBlock.equals(Blocks.AIR) &&
            !canChopCannotQuarryBlocks.contains(targetBlock) &&
            !cannotBreakBlocks.contains(targetBlock);
    }

    private void dropXP(Level level, BasePedestalBlockEntity pedestal, BlockState blockAtPoint, BlockPos currentPoint) {
        int fortune = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.BLOCK_FORTUNE))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,pedestal.getToolStack())):(0);
        int silky = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.SILK_TOUCH))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,pedestal.getToolStack())):(0);
        int xpdrop = blockAtPoint.getBlock().getExpDrop(blockAtPoint,level, level.random,currentPoint,fortune,silky);
        if(xpdrop>0)blockAtPoint.getBlock().popExperience((ServerLevel)level,currentPoint,xpdrop);
    }

    public List<BlockPos> getValidWorkCardPositionsQuarry(Level level, BasePedestalBlockEntity pedestal, ItemStack upgrade) {
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
        List<BlockPos> allPositions = getValidWorkCardPositionsQuarry(level, pedestal, coin);
        if (allPositions.isEmpty()) return;

        int currentPosition = getCurrentPosition(coin);
        BlockPos currentPoint = allPositions.get(currentPosition);
        int currentY = hasOperateToBedrock(coin) ? getCurrentHeight(coin) : currentPoint.getY();
        int currentMinY = Math.max(currentY - getNumBlocksPerOperation(coin) + 1, getMinY(coin));
        List<BlockPos> adjustedPoints = IntStream.rangeClosed(currentMinY, currentY)
            .mapToObj(y -> new BlockPos(currentPoint.getX(), y, currentPoint.getZ())).toList();
        if (quarryAction(level, pedestal, pedestalPos, adjustedPoints)) {
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

    public boolean quarryAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, List<BlockPos> adjustedPoints) {
        WeakReference<FakePlayer> fakePlayerReference = pedestal.getPedestalPlayer(pedestal);
        if(fakePlayerReference != null && fakePlayerReference.get() != null) {
            FakePlayer fakePlayer = fakePlayerReference.get();

            List<Integer> distances = adjustedPoints.stream().map(point -> getDistanceBetweenPoints(pedestalPos, point)).toList();
            boolean damageToolsEnabled = PedestalConfig.COMMON.quarryDamageTools.get();
            if (removeFuelForActionMultiple(pedestal, distances, true)) {
                if (!damageToolsEnabled || (pedestal.hasTool() && pedestal.damageInsertedTool(adjustedPoints.size(), true))) {
                    for (BlockPos adjustedPoint : adjustedPoints) {
                        if (adjustedPoint.equals(pedestalPos)) {
                            continue;
                        }
                        BlockState targetBlockState = level.getBlockState(adjustedPoint);
                        Block targetBlock = targetBlockState.getBlock();
                        //targetBlockState.requiresCorrectToolForDrops();

                        if (targetBlockState.getDestroySpeed(level, adjustedPoint) >= 0) {
                            if (ForgeEventFactory.doPlayerHarvestCheck(fakePlayer, targetBlockState, true)) {
                                BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(level, adjustedPoint, targetBlockState, fakePlayer);
                                if (!MinecraftForge.EVENT_BUS.post(e)) {
                                    if (canMine(targetBlock) && passesMachineFilterItems(pedestal, targetBlock.getCloneItemStack(level, adjustedPoint, targetBlockState))) {
                                        if (
                                            removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestalPos, adjustedPoint), false) &&
                                            (!damageToolsEnabled || pedestal.damageInsertedTool(1, false))
                                        ) {
                                            boolean targetIsBlockEntity = level.getBlockEntity(adjustedPoint) != null;
                                            if (targetIsBlockEntity) {
                                                if (!PedestalConfig.COMMON.blockBreakerBreakEntities.get()) {
                                                    continue; // we can't break block entities, so skip this
                                                }
                                                targetBlockState.onRemove(level, adjustedPoint, targetBlockState, true);
                                                level.removeBlockEntity(adjustedPoint);
                                            }
                                            dropXP(level, pedestal, targetBlockState, pedestalPos);
                                            level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                            List<ItemStack> drops = getBlockDrops(level, pedestal, fakePlayer, targetBlockState);
                                            for (ItemStack drop : drops) {
                                                MowLibItemUtils.spawnItemStack(level, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), drop);
                                            }
                                        } else {
                                            return false; // should be impossible
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return true;
                } else {
                    if (pedestal.canSpawnParticles()) {
                        MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, pedestalPos.getX(), pedestalPos.getY() + 1.0f, pedestalPos.getZ(), 255, 255, 255));
                    }
                    return false;
                }
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
