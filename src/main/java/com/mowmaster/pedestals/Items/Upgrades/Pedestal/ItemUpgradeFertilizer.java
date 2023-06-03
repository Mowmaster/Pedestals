package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import com.mowmaster.pedestals.Items.ISelectableArea;
import com.mowmaster.pedestals.Items.ISelectablePoints;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeFertilizer extends ItemUpgradeBase
{
    public ItemUpgradeFertilizer(Properties p_41383_) {
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
    public boolean canModifyArea(ItemStack upgradeItemStack) {
        return PedestalConfig.COMMON.upgrade_require_sized_selectable_area.get();
    }

    @Override
    public boolean canModifySuperSpeed(ItemStack upgradeItemStack) { return true; }

    @Override
    public boolean needsWorkCard() { return true; }

    @Override
    public int getWorkCardType() { return 0; }

    //Requires energy

    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_fertilizer_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_fertilizer_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_fertilizer_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_fertilizer_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_fertilizer_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_fertilizer_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_fertilizer_dustColor.get(),PedestalConfig.COMMON.upgrade_fertilizer_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_fertilizer_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_fertilizer_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_fertilizer_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_fertilizer_selectedMultiplier.get(); }

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
        resetCachedValidWorkCardPositions(coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numposition");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        List<BlockPos> allPositions = getValidWorkCardPositions(pedestal);
        if (allPositions.isEmpty()) return;

        fertilizerAction(level, pedestal, allPositions);
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
            ItemStack filterInPedestal = pedestal.getFilterInPedestal();
            if(filterInPedestal.getItem() instanceof BaseFilter filter)
            {
                if(filter.getFilterDirection().neutral())
                {
                    ItemStack itemToCheck = pedestal.getItemInPedestal();
                    return filter.canAcceptItems(filterInPedestal,itemToCheck);
                }
            }
        }

        return true;
    }

    public static IntegerProperty getBlockPropertyAge(BlockState blockState) {
        for (Property<?> prop : blockState.getProperties()) {
            if (prop != null && prop.getName() != null && prop instanceof IntegerProperty && prop.getName().equalsIgnoreCase("age")) {
                return (IntegerProperty) prop;
            }
        }
        return null;
    }

    private boolean canUseOn(BasePedestalBlockEntity pedestal, BlockState useOnState, BlockPos useOnPosition, boolean bonemealUsed)
    {
        //ItemStack getItem = pedestal.getItemInPedestal();
        Block getBlockToUseOn = useOnState.getBlock();
        if(bonemealUsed)
        {
            if(getBlockToUseOn instanceof IPlantable || getBlockToUseOn instanceof BonemealableBlock)
            {
                IntegerProperty propInt = getBlockPropertyAge(useOnState);
                if(useOnState.hasProperty(propInt))
                {
                    int current = useOnState.getValue(propInt);
                    //int min = Collections.min(propInt.getPossibleValues());
                    int max = Collections.max(propInt.getPossibleValues());
                    if(max>0)
                    {
                        if(current == max)
                        {
                            return false;
                        }
                        else
                        {
                            return true;
                        }
                    }
                }
                else
                {
                    return true;
                }
            }
        }
        else if(getBlockToUseOn.isRandomlyTicking(useOnState))
        {
            /*
            getBlockToUseOn instanceof StemGrownBlock ||
                getBlockToUseOn instanceof ChorusFlowerBlock ||
                getBlockToUseOn instanceof BuddingAmethystBlock ||
                getBlockToUseOn instanceof PointedDripstoneBlock ||
                getBlockToUseOn instanceof IPlantable ||
                getBlockToUseOn instanceof BonemealableBlock
             */
            return true;
        }



        return false;
    }

    public BlockState getState(Block getBlock, ItemStack itemForBlock)
    {
        BlockState stated = Blocks.AIR.defaultBlockState();

        //Redstone
        if(itemForBlock.getItem() == Items.REDSTONE)
        {
            stated = Blocks.REDSTONE_WIRE.defaultBlockState();
        }
        else
        {
            stated = getBlock.defaultBlockState();
        }

        return stated;
    }

    private BlockPos getPosBasedOnPedestalDirection(BasePedestalBlockEntity pedestalBlockEntity, BlockPos pos)
    {
        Direction ofPedestal = getPedestalFacing(pedestalBlockEntity.getLevel(),pedestalBlockEntity.getPos());
        switch (ofPedestal)
        {
            case UP: return pos.below();
            case DOWN: return pos.above();
            case NORTH: return pos.south();
            case EAST: return pos.west();
            case SOUTH: return pos.north();
            case WEST: return pos.east();
            default: return pos.below();
        }
    }


    public void fertilizerAction(Level level, BasePedestalBlockEntity pedestal, List<BlockPos> listed) {
        if(!level.isClientSide())
        {
            WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
            if(getPlayer != null && getPlayer.get() != null)
            {
                if(hasSuperSpeed(pedestal.getCoinOnPedestal()))
                {
                    ItemStack stackInPed = pedestal.getItemInPedestal();
                    for(BlockPos currentPoint:listed)
                    {
                        BlockState blockAtPoint = level.getBlockState(currentPoint);
                        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), true))
                        {
                            if(passesFilter(pedestal, blockAtPoint, currentPoint))
                            {
                                if(!currentPoint.equals(pedestal.getPos()) && level.getBlockState(currentPoint).getBlock() != Blocks.AIR)
                                {
                                    if(!pedestal.removeItem(1,true).isEmpty() && blockAtPoint.getBlock() instanceof BonemealableBlock)
                                    {
                                        if(stackInPed.getItem() instanceof BoneMealItem bonerItem)
                                        {
                                            if(canUseOn(pedestal,blockAtPoint,currentPoint,true))
                                            {
                                                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), false))
                                                {
                                                    UseOnContext blockContext = new UseOnContext(level,(getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()), InteractionHand.MAIN_HAND, pedestal.getItemInPedestal().copy(), new BlockHitResult(Vec3.ZERO, getPedestalFacing(level,pedestal.getPos()), currentPoint, false));
                                                    InteractionResult result = bonerItem.useOn(blockContext);
                                                    if (result == InteractionResult.CONSUME) {

                                                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),0,255,0));
                                                        pedestal.removeItem(1,false);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else if(canUseOn(pedestal, blockAtPoint, currentPoint,false))
                                    {
                                        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), false))
                                        {
                                            blockAtPoint.randomTick((ServerLevel) level,currentPoint, RandomSource.create());
                                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),240,240,240));
                                            //level.markAndNotifyBlock(currentPoint, level.getChunkAt(currentPoint),blockAtPoint,blockAtPoint,2,2);
                                        }
                                    }
                                }
                            }
                        }
                    }



                    /*int currentPosition = getCurrentPosition(pedestal);
                    BlockPos currentPoint = listed.get(currentPosition);
                    */

                }
                else
                {
                    int currentPosition = getCurrentPosition(pedestal);
                    BlockPos currentPoint = listed.get(currentPosition);
                    BlockState blockAtPoint = level.getBlockState(currentPoint);
                    ItemStack stackInPed = pedestal.getItemInPedestal();

                    if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), true))
                    {
                        if(passesFilter(pedestal, blockAtPoint, currentPoint))
                        {
                            if(!currentPoint.equals(pedestal.getPos()) && level.getBlockState(currentPoint).getBlock() != Blocks.AIR)
                            {

                                if(!pedestal.removeItem(1,true).isEmpty() && blockAtPoint.getBlock() instanceof BonemealableBlock)
                                {
                                    if(stackInPed.getItem() instanceof BoneMealItem bonerItem)
                                    {
                                        if(canUseOn(pedestal,blockAtPoint,currentPoint,true))
                                        {
                                            if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), false))
                                            {
                                                UseOnContext blockContext = new UseOnContext(level,(getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()), InteractionHand.MAIN_HAND, pedestal.getItemInPedestal().copy(), new BlockHitResult(Vec3.ZERO, getPedestalFacing(level,pedestal.getPos()), currentPoint, false));
                                                InteractionResult result = bonerItem.useOn(blockContext);
                                                if (result == InteractionResult.CONSUME) {

                                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),0,255,0));
                                                    pedestal.removeItem(1,false);
                                                }
                                            }
                                        }
                                    }
                                }
                                else if(canUseOn(pedestal, blockAtPoint, currentPoint,false))
                                {
                                    if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), false))
                                    {
                                        blockAtPoint.randomTick((ServerLevel) level,currentPoint, RandomSource.create());
                                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),240,240,240));
                                        //level.markAndNotifyBlock(currentPoint, level.getChunkAt(currentPoint),blockAtPoint,blockAtPoint,2,2);
                                    }
                                }
                            }
                        }

                        if((currentPosition+1)>=listed.size())
                        {
                            setCurrentPosition(pedestal,0);
                        }
                        else
                        {
                            iterateCurrentPosition(pedestal);
                        }
                    }
                }
            }
        }
    }
}
