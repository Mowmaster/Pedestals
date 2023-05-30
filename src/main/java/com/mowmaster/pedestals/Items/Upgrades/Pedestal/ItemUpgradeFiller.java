package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import com.mowmaster.pedestals.Items.ISelectableArea;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeFiller extends ItemUpgradeBase
{
    public ItemUpgradeFiller(Properties p_41383_) {
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
    public int getWorkCardType() { return 1; }

    //Requires energy

    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_filler_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_filler_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_filler_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_filler_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_filler_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_filler_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_filler_dustColor.get(),PedestalConfig.COMMON.upgrade_filler_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_filler_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_filler_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_filler_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_filler_selectedMultiplier.get(); }

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
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(),"_numposition");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(),"_numdelay");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(),"_numheight");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(),"_boolstop");

    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        List<BlockPos> allPositions = getValidWorkCardPositions(pedestal);
        if (allPositions.isEmpty()) return;

        if(pedestal.hasItem()) fillerAction(level, pedestal, allPositions);
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

    private boolean isToolHighEnoughLevelForBlock(ItemStack toolIn, BlockState getBlock)
    {
        if(toolIn.getItem() instanceof TieredItem tieredItem)
        {
            Tier toolTier = tieredItem.getTier();
            return TierSortingRegistry.isCorrectTierForDrops(toolTier,getBlock);
        }

        return false;
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
                    ItemStack blockToCheck = pedestal.getItemInPedestal();
                    if(Block.byItem(blockToCheck.getItem()) != Blocks.AIR)
                    {
                        return filter.canAcceptItems(filterInPedestal,blockToCheck);
                    }
                }
            }
        }

        return true;
    }

    private boolean canPlace(BasePedestalBlockEntity pedestal)
    {
        ItemStack getPlaceItem = pedestal.getItemInPedestal();
        Block possibleBlock = Block.byItem(getPlaceItem.getItem());
        if(possibleBlock != Blocks.AIR)
        {
            if(!ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_place"))).stream().toList().contains(getPlaceItem))
            {
                if(possibleBlock instanceof IPlantable && (
                        possibleBlock instanceof BushBlock ||
                                possibleBlock instanceof StemBlock ||
                                possibleBlock instanceof BonemealableBlock ||
                                possibleBlock instanceof ChorusFlowerBlock
                ))
                {
                    return false;
                }
            }
        }
        else
        {
            return false;
        }

        return true;
    }

    public void fillerAction(Level level, BasePedestalBlockEntity pedestal, List<BlockPos> listed) {
        if(!level.isClientSide())
        {
            if(pedestal.hasWorkCard())
            {
                ItemStack card = pedestal.getWorkCardInPedestal();
                if(card.getItem() instanceof WorkCardBase workCardBase)
                {
                    WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
                    if(getPlayer != null && getPlayer.get() != null)
                    {
                        int currentPosition = getCurrentPosition(pedestal);
                        BlockPos currentPoint = listed.get(currentPosition);
                        AABB area = new AABB(workCardBase.readBlockPosFromNBT(card,1),workCardBase.readBlockPosFromNBT(card,2));
                        int maxY = (int)area.maxY;
                        int minY = (int)area.minY;
                        int ySpread = maxY - minY;
                        boolean minMaxHeight = ySpread > 0;
                        if(ySpread>getHeightIteratorValue(pedestal))setCurrentHeight(pedestal,minY);

                        int currentYMin = getCurrentHeight(pedestal);
                        //int currentYMin = (minMaxHeight)?(0):(getCurrentHeight(pedestal));
                        int currentYMax = (minMaxHeight)?(0):(currentYMin+getHeightIteratorValue(pedestal));

                        int min = (minMaxHeight)?(minY):(currentYMin);
                        int max = (minMaxHeight)?((ySpread>getHeightIteratorValue(pedestal))?(minY+getHeightIteratorValue(pedestal)):(maxY)):(currentYMax);
                        int absoluteMax = (minMaxHeight)?(maxY):(level.getMaxBuildHeight());

                        boolean fuelRemoved = true;
                        //ToDo: make this a modifier for later
                        boolean runsOnce = true;
                        boolean stop = getStopped(pedestal);

                        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), true))
                        {
                            if(!stop)
                            {
                                for(int y=min;y<=max;y++)
                                {
                                    BlockPos adjustedPoint = new BlockPos(currentPoint.getX(),y,currentPoint.getZ());
                                    BlockState blockAtPoint = level.getBlockState(adjustedPoint);
                                    if(!pedestal.removeItem(1,true).isEmpty())
                                    {
                                        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), false))
                                        {
                                            if(canPlace(pedestal) && passesFilter(pedestal, blockAtPoint, adjustedPoint))
                                            {
                                                if(!adjustedPoint.equals(pedestal.getPos()) && blockAtPoint.getBlock() == Blocks.AIR)
                                                {
                                                    UseOnContext blockContext = new UseOnContext(level,(getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()), InteractionHand.MAIN_HAND, pedestal.getItemInPedestal().copy(), new BlockHitResult(Vec3.ZERO, getPedestalFacing(level,pedestal.getPos()), adjustedPoint, false));
                                                    InteractionResult result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                                                    if (result == InteractionResult.CONSUME) {
                                                        pedestal.removeItem(1,false);
                                                    }
                                                }
                                            }
                                        }
                                        else {
                                            fuelRemoved = false;
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
}
