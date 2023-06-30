package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.Filters.BaseFilter;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeDrain extends ItemUpgradeBase
{
    public ItemUpgradeDrain(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyBlockCapacity(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyRange(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyArea(ItemStack upgradeItemStack) {
        return PedestalConfig.COMMON.upgrade_require_sized_selectable_area.get();
    }

    @Override
    public boolean needsWorkCard() { return true; }

    @Override
    public int getWorkCardType() { return 0; }

    //Requires energy

    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_drain_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_drain_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_drain_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_drain_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_drain_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_drain_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_drain_dustColor.get(),PedestalConfig.COMMON.upgrade_drain_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_drain_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_drain_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_drain_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_drain_selectedMultiplier.get(); }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {

        List<String> messages = super.getUpgradeHUD(pedestal);

        if(messages.size()<=0)
        {
            if(baseEnergyCostPerDistance()>0)
            {
                if(pedestal.getStoredEnergy()<baseEnergyCostPerDistance())
                {
                    messages.add(ChatFormatting.RED + "Needs Energy");
                    messages.add(ChatFormatting.RED + "To Operate");
                }
            }
            if(baseXpCostPerDistance()>0)
            {
                if(pedestal.getStoredExperience()<baseXpCostPerDistance())
                {
                    messages.add(ChatFormatting.GREEN + "Needs Experience");
                    messages.add(ChatFormatting.GREEN + "To Operate");
                }
            }
            if(baseDustCostPerDistance().getDustAmount()>0)
            {
                if(pedestal.getStoredEnergy()<baseEnergyCostPerDistance())
                {
                    messages.add(ChatFormatting.LIGHT_PURPLE + "Needs Dust");
                    messages.add(ChatFormatting.LIGHT_PURPLE + "To Operate");
                }
            }
        }

        return messages;
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        resetCachedValidWorkCardPositions(MODID, coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numposition");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numdelay");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numheight");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_boolstop");

    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        List<BlockPos> allPositions = getValidWorkCardPositions(pedestal, coin, getWorkCardType(),MODID);
        if (allPositions.isEmpty()) return;

        if(pedestal.hasFluid()) drainAction(level, pedestal, allPositions);
    }

    private int getHeightIteratorValue(BasePedestalBlockEntity pedestal)
    {
        return PedestalConfig.COMMON.upgrade_filler_baseBlocksPlaced.get() + getBlockCapacityIncrease(pedestal.getCoinOnPedestal());
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

    private boolean passesFilter(BasePedestalBlockEntity pedestal, BlockState canMineBlock, BlockPos canMinePos)
    {
        if(pedestal.hasFilter())
        {
            ItemStack filterInPedestal = pedestal.getFilterInBlockEntity();
            if(filterInPedestal.getItem() instanceof BaseFilter filter)
            {
                if(filter.getFilterDirection().neutral())
                {
                    FluidStack fluidInPed = pedestal.getStoredFluid();
                    if(!fluidInPed.isEmpty())
                    {
                        return filter.canAcceptFluids(filterInPedestal,fluidInPed);
                    }
                }
            }
        }

        return true;
    }

    private boolean canPlace(BasePedestalBlockEntity pedestal, BlockState getBlock, BlockPos currentPos)
    {
        FluidStack fluidInPed = pedestal.getStoredFluid();
        if(!fluidInPed.isEmpty())
        {
            if(!ForgeRegistries.FLUIDS.tags().getTag(FluidTags.create(new ResourceLocation(MODID, "pedestals_cannot_place_fluid"))).stream().toList().contains(fluidInPed.getFluid()))
            {
                if(getBlock.getBlock() == Blocks.AIR || !getBlock.getFluidState().isSource() || getBlock.hasProperty(BlockStateProperties.WATERLOGGED))
                {
                    if(getBlock.getBlock() == Blocks.AIR)
                    {
                        return true;
                    }
                    else if(getBlock.hasProperty(BlockStateProperties.WATERLOGGED) && fluidInPed.getFluid().equals(Fluids.WATER))
                    {
                        if(getBlock.getValue(BlockStateProperties.WATERLOGGED) == false)
                        {
                            return true;
                        }
                    }
                    else if(!getBlock.getFluidState().isSource() && !getBlock.getFluidState().isEmpty())
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void drainAction(Level level, BasePedestalBlockEntity pedestal, List<BlockPos> listed)
    {
        if(!level.isClientSide())
        {
            WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
            if(getPlayer != null && getPlayer.get() != null)
            {
                int currentPosition = getCurrentPosition(pedestal);
                BlockPos currentPoint = listed.get(currentPosition);
                BlockState blockAtPoint = level.getBlockState(currentPoint);
                boolean fuelRemoved = true;

                if(pedestal.removeFluid(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE).getAmount() == FluidType.BUCKET_VOLUME)
                {
                    if(removeFuelForActionMultiple(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint),getHeightIteratorValue(pedestal), true))
                    {
                        if(canPlace(pedestal,blockAtPoint,currentPoint) && passesFilter(pedestal, blockAtPoint, currentPoint))
                        {
                            FluidStack stackInPedestal = pedestal.getStoredFluid().copy();
                            if(stackInPedestal.getFluid().defaultFluidState() != null && stackInPedestal.getFluid().defaultFluidState().createLegacyBlock() != null)
                            {
                                UseOnContext blockContext = new UseOnContext(level,getPlayer.get(), InteractionHand.MAIN_HAND, FluidUtil.getFilledBucket(stackInPedestal), new BlockHitResult(Vec3.ZERO, getPedestalFacing(level,pedestal.getPos()), currentPoint, false));
                                InteractionResult result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                                if (result == InteractionResult.PASS) {
                                    if(removeFuelForActionMultiple(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint),getHeightIteratorValue(pedestal), false))
                                    {
                                        if(blockAtPoint.hasProperty(BlockStateProperties.WATERLOGGED))
                                        {
                                            if(blockAtPoint.getValue(BlockStateProperties.WATERLOGGED)==false)
                                            {
                                                if(pedestal.removeFluid(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).getFluid().equals(Fluids.WATER))
                                                {
                                                    level.setBlockAndUpdate(currentPoint,blockAtPoint.setValue(BlockStateProperties.WATERLOGGED,true));
                                                }
                                            }
                                        }
                                        else
                                        {
                                            if(!pedestal.removeFluid(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).isEmpty())
                                            {
                                                level.setBlockAndUpdate(currentPoint,stackInPedestal.getFluid().defaultFluidState().createLegacyBlock());
                                            }
                                        }
                                    }
                                    else {
                                        fuelRemoved = false;
                                    }
                                }
                            }
                        }
                    }
                    else {
                        fuelRemoved = false;
                    }
                }

                if((currentPosition+1)>=listed.size())
                {
                    setCurrentPosition(pedestal,0);
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
