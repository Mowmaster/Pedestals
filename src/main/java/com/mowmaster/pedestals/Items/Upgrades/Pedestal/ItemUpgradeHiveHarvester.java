package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.Filters.BaseFilter;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;
import static net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL;

public class ItemUpgradeHiveHarvester extends ItemUpgradeBase {
    public ItemUpgradeHiveHarvester(Properties p_41383_) {
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
    public int getWorkCardType() { return 0; }

    //Requires energy
    @Override
    public int baseEnergyCostPerDistance() { return PedestalConfig.COMMON.upgrade_hiveharvester_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_hiveharvester_energy_distance_multiplier.get(); }
    @Override
    public double energyCostMultiplier() { return PedestalConfig.COMMON.upgrade_hiveharvester_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance() { return PedestalConfig.COMMON.upgrade_hiveharvester_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_hiveharvester_xp_distance_multiplier.get(); }
    @Override
    public double xpCostMultiplier() { return PedestalConfig.COMMON.upgrade_hiveharvester_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance() { return new DustMagic(PedestalConfig.COMMON.upgrade_hiveharvester_dustColor.get(),PedestalConfig.COMMON.upgrade_hiveharvester_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_hiveharvester_dust_distance_multiplier.get(); }
    @Override
    public double dustCostMultiplier() { return PedestalConfig.COMMON.upgrade_hiveharvester_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_hiveharvester_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier() { return PedestalConfig.COMMON.upgrade_hiveharvester_selectedMultiplier.get(); }

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
    public ItemStack getUpgradeDefaultTool() { return new ItemStack(Items.SHEARS); }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        resetCachedValidWorkCardPositions(coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getOrCreateTag(), "_numposition");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        if(level.isClientSide()) return;

        List<BlockPos> allPositions = getValidWorkCardPositions(pedestal);
        if (allPositions.isEmpty()) return;

        int currentPosition = getCurrentPosition(coin);
        BlockPos targetPos = allPositions.get(currentPosition);
        if (harvesterAction(level, pedestal, targetPos)) {
            if (currentPosition + 1 == allPositions.size()) {
                setCurrentPosition(pedestal, 0);
            } else {
                setCurrentPosition(pedestal, currentPosition + 1);
            }
        }
    }

    private int getCurrentPosition(ItemStack upgrade) {
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, upgrade.getOrCreateTag(), "_numposition");
    }

    private void setCurrentPosition(BasePedestalBlockEntity pedestal, int num) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), num, "_numposition");
    }

    private boolean passesFilter(BasePedestalBlockEntity pedestal, Block targetBlock) {
        if (pedestal.hasFilter()) {
            ItemStack filterInPedestal = pedestal.getFilterInBlockEntity();
            if (filterInPedestal.getItem() instanceof BaseFilter filter && filter.getFilterDirection().neutral()) {
                ItemStack blockToCheck = new ItemStack(targetBlock);

                return filter.canAcceptItems(filterInPedestal, blockToCheck);
            }
        }

        return true;
    }

    private boolean canHarvest(Level level, BlockPos targetPos, BlockState targetBlockState, Block targetBlock) {
        List<Block> cannotBreakBlocks = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_break"))).stream().toList();

        return !cannotBreakBlocks.contains(targetBlock) &&
            targetBlockState.getDestroySpeed(level, targetPos) >= 0 &&
            targetBlockState.hasProperty(HONEY_LEVEL) &&
            targetBlockState.getValue(HONEY_LEVEL) >= 5;
    }

    public boolean harvesterAction(Level level, BasePedestalBlockEntity pedestal, BlockPos targetPos) {
        WeakReference<FakePlayer> fakePlayerReference = pedestal.getPedestalPlayer(pedestal);
        if (fakePlayerReference != null && fakePlayerReference.get() != null) {
            FakePlayer fakePlayer = fakePlayerReference.get();
            BlockPos pedestalPos = pedestal.getPos();
            BlockState targetBlockState = level.getBlockState(targetPos);
            Block targetBlock = targetBlockState.getBlock();
            if (
                removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestalPos, targetPos), true) &&
                    targetBlock instanceof BeehiveBlock beehiveBlock &&
                    canHarvest(level, targetPos, targetBlockState, targetBlock) &&
                    passesFilter(pedestal, targetBlock)
            ) {
                BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(level, targetPos, targetBlockState, fakePlayer);
                if (!MinecraftForge.EVENT_BUS.post(e)) {
                    ItemStack toolStack = pedestal.hasItem() ? pedestal.getItemInPedestal() : pedestal.getToolStack();
                    boolean toolStackIsDamageable = toolStack.getItem().isDamageable(toolStack) && toolStack.getMaxStackSize() <= 1;
                    if (PedestalConfig.COMMON.hiveharvester_DamageTools.get()) {
                        if (toolStackIsDamageable && !pedestal.damageTool(toolStack, 1, true)) {
                            if (pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, pedestalPos.getX(), pedestalPos.getY() + 1.0f, pedestalPos.getZ(), 255, 255, 255));
                            return false; // tool does not have sufficient durability
                        }
                    }

                    if (removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestalPos, targetPos), false)) {
                        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, toolStack.copy());
                        UseOnContext blockContext = new UseOnContext(level, fakePlayer, InteractionHand.MAIN_HAND, toolStack.copy(), new BlockHitResult(Vec3.ZERO, getPedestalFacing(level, pedestalPos), targetPos, false));
                        BlockHitResult result = new BlockHitResult(blockContext.getClickLocation(), blockContext.getClickedFace(), blockContext.getClickedPos(), blockContext.isInside());
                        if (result.getType() == HitResult.Type.BLOCK) {
                            beehiveBlock.use(targetBlockState, level, targetPos, fakePlayer, InteractionHand.MAIN_HAND, result);
                            for (ItemStack stackInPlayer : fakePlayer.getInventory().items) {
                                if (!stackInPlayer.isEmpty() && !stackInPlayer.is(toolStack.getItem())) {
                                    MowLibItemUtils.spawnItemStack(level, targetPos.getX(), targetPos.getY(), targetPos.getZ(), stackInPlayer);
                                    if (!toolStackIsDamageable) {
                                        ItemStack toRemove = toolStack.copy();
                                        toRemove.setCount(1);
                                        pedestal.removeItemStack(toRemove, false);
                                    }
                                }
                            }
                            if (toolStackIsDamageable && PedestalConfig.COMMON.hiveharvester_DamageTools.get()) {
                                pedestal.damageTool(toolStack, 1, false);
                            }
                            if (pedestal.canSpawnParticles()) {
                                MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, targetPos.getX(), targetPos.getY()+1.0f, targetPos.getZ(), 255, 246, 0));
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}