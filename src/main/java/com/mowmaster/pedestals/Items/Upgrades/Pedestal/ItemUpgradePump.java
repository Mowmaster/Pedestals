package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardBase;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.List;

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
    public boolean needsWorkCard() { return true; }

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
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        resetCachedValidWorkCardPositions(coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numposition");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numdelay");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numheight");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_boolstop");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        if (level.isClientSide()) return;

        List<BlockPos> allPositions = getValidWorkCardPositions(pedestal);
        if (allPositions.isEmpty()) return;

        if (pedestal.spaceForFluid()>=1000) pumpAction(level, pedestal, pedestalPos, allPositions);
    }

    private boolean getStopped(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return MowLibCompoundTagUtils.readBooleanFromNBT(MODID, coin.getOrCreateTag(), "_boolstop");
    }

    private void setStopped(BasePedestalBlockEntity pedestal, boolean value)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeBooleanToNBT(MODID, coin.getOrCreateTag(),value, "_boolstop");
    }

    private int getHeightIteratorValue(BasePedestalBlockEntity pedestal)
    {
        return PedestalConfig.COMMON.upgrade_pump_baseBlocksPumped.get() + getBlockCapacityIncrease(pedestal.getCoinOnPedestal());
    }

    private int getCurrentHeight(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return (coin.getOrCreateTag().contains(MODID + "_numheight"))?(MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coin.getOrCreateTag(), "_numheight")):(pedestal.getLevel().getMinBuildHeight());
    }

    private void setCurrentHeight(BasePedestalBlockEntity pedestal, int num)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), num, "_numheight");
    }

    private void iterateCurrentHeight(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        int current = getCurrentHeight(pedestal);
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), (current+getHeightIteratorValue(pedestal)), "_numheight");
    }

    private int getCurrentDelay(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coin.getOrCreateTag(), "_numdelay");
    }

    private void setCurrentDelay(BasePedestalBlockEntity pedestal, int num)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), num, "_numdelay");
    }

    private void iterateCurrentDelay(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        int current = getCurrentDelay(pedestal);
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), (current+1+getSpeedTicksReduced(pedestal.getCoinOnPedestal())), "_numdelay");
    }

    private int getCurrentPosition(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coin.getOrCreateTag(), "_numposition");
    }

    private void setCurrentPosition(BasePedestalBlockEntity pedestal, int num)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), num, "_numposition");
    }

    private void iterateCurrentPosition(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        int current = getCurrentPosition(pedestal);
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), (current+1), "_numposition");
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

    public void pumpAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, List<BlockPos> listed) {
        WeakReference<FakePlayer> fakePlayerReference = pedestal.getPedestalPlayer(pedestal);
        if(fakePlayerReference != null && fakePlayerReference.get() != null) {
            FakePlayer fakePlayer = fakePlayerReference.get();

            if(pedestal.hasWorkCard()) {
                ItemStack card = pedestal.getWorkCardInPedestal();
                if (card.getItem() instanceof WorkCardBase) {
                    int currentPosition = getCurrentPosition(pedestal);
                    BlockPos currentPoint = listed.get(currentPosition);
                    AABB area = new AABB(WorkCardBase.readBlockPosFromNBT(card,1), WorkCardBase.readBlockPosFromNBT(card,2));
                    int maxY = (int)area.maxY;
                    int minY = (int)area.minY;
                    int ySpread = maxY - minY;
                    boolean minMaxHeight = (maxY - minY > 0) || listed.size()<=4;
                    if(ySpread>getHeightIteratorValue(pedestal))setCurrentHeight(pedestal,minY);
                    int currentYMin = getCurrentHeight(pedestal);
                    int currentYMax = (minMaxHeight)?(0):(currentYMin+getHeightIteratorValue(pedestal));
                    int min = (minMaxHeight)?(minY):(currentYMin);
                    int max = (minMaxHeight)?((ySpread>getHeightIteratorValue(pedestal))?(minY+getHeightIteratorValue(pedestal)):(maxY)):(currentYMax);
                    int absoluteMax = (minMaxHeight)?(maxY):(level.getMaxBuildHeight());

                    boolean fuelRemoved = true;
                    //ToDo: make this a modifier for later
                    boolean runsOnce = true;
                    boolean stop = getStopped(pedestal);

                    if(removeFuelForActionMultiple(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint),getHeightIteratorValue(pedestal), true)) {
                        if(!stop) {
                            for(int y = min; y <= max; y++) {
                                BlockPos adjustedPoint = new BlockPos(currentPoint.getX(),y,currentPoint.getZ());
                                if (adjustedPoint.equals(pedestalPos)) {
                                    continue;
                                }
                                BlockState targetBlockState = level.getBlockState(adjustedPoint);
                                Block targetBlock = targetBlockState.getBlock();
                                FluidStack targetFluidStack = getFluidStackForTarget(targetBlock, level.getFluidState(adjustedPoint));

                                if (canPump(pedestal, targetBlock, targetFluidStack) && passesMachineFilterFluids(pedestal, targetFluidStack)) {
                                    if(removeFuelForActionMultiple(pedestal, getDistanceBetweenPoints(pedestal.getPos(), adjustedPoint),getHeightIteratorValue(pedestal), true)) {
                                        if (pedestal.canAcceptFluid(targetFluidStack) && pedestal.spaceForFluid() >= FluidType.BUCKET_VOLUME) {
                                            if (removeFuelForActionMultiple(pedestal, getDistanceBetweenPoints(pedestal.getPos(), adjustedPoint), getHeightIteratorValue(pedestal), false)) {
                                                pedestal.addFluid(targetFluidStack, IFluidHandler.FluidAction.EXECUTE);

                                                if (targetBlockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                                    if (removeWaterFromLoggedBlocks()) {
                                                        level.setBlockAndUpdate(adjustedPoint, targetBlockState.setValue(BlockStateProperties.WATERLOGGED, false));
                                                    }
                                                } else if (targetBlock instanceof AbstractCauldronBlock) {
                                                    level.setBlockAndUpdate(adjustedPoint, Blocks.CAULDRON.defaultBlockState());
                                                } else {
                                                    level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                                    ItemStack itemToPlace = pedestal.getItemInPedestal().copy();
                                                    if(!pedestal.removeItemStack(itemToPlace, true).isEmpty() && canPlace(itemToPlace) && passesMachineFilterItems(pedestal, itemToPlace)) {
                                                        ItemStack itemToRemove = itemToPlace.copy(); // UseOnContext modifies the passed in item stack when returning an InteractionResult.CONSUME, so we need a copy of it for removal.
                                                        UseOnContext blockContext = new UseOnContext(level, fakePlayer, InteractionHand.MAIN_HAND, itemToPlace, new BlockHitResult(Vec3.ZERO, getPedestalFacing(level,pedestal.getPos()), adjustedPoint, false));
                                                        InteractionResult resultPlace = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                                                        if (resultPlace == InteractionResult.CONSUME) {
                                                            itemToRemove.setCount(1); // only remove 1 item
                                                            pedestal.removeItemStack(itemToRemove,false);
                                                        }
                                                    }
                                                }
                                            } else {
                                                fuelRemoved = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestal.getPos().getX(),pedestal.getPos().getY()+1.0f,pedestal.getPos().getZ(),55,55,55));
                        }

                        if((currentPosition+1)>=listed.size() && currentYMax >= absoluteMax)
                        {
                            if(runsOnce)
                            {
                                //ToDo: Make this 1200 value a config
                                int delay = listed.size() * Math.abs((((minMaxHeight)?(maxY):(level.getMaxBuildHeight()))-((minMaxHeight)?(maxY):(level.getMinBuildHeight()))));
                                if(getCurrentDelay(pedestal)>=delay)
                                {
                                    setCurrentPosition(pedestal,0);
                                    setStopped(pedestal,false);
                                    setCurrentDelay(pedestal,0);
                                }
                                else
                                {
                                    iterateCurrentDelay(pedestal);
                                    setStopped(pedestal,true);
                                }
                            }
                            else
                            {
                                setCurrentPosition(pedestal,0);
                            }
                        }
                        else if((currentPosition+1)>=listed.size())
                        {
                            setCurrentPosition(pedestal,0);
                            iterateCurrentHeight(pedestal);
                        }
                        else
                        {
                            if(fuelRemoved){
                                iterateCurrentPosition(pedestal);
                            }
                        }
                    }
                }
            }
        }
    }
}
