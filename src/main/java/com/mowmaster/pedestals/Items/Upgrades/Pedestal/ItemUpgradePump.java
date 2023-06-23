package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.Filters.BaseFilter;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradePump extends ItemUpgradeBase
{
    public ItemUpgradePump(Properties p_41383_) {
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
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_pump_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_pump_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_pump_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_pump_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_pump_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_pump_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_pump_dustColor.get(),PedestalConfig.COMMON.upgrade_pump_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_pump_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_pump_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_pump_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_pump_selectedMultiplier.get(); }

    public boolean removeWaterFromLoggedBlocks() { return PedestalConfig.COMMON.upgrade_pump_waterlogged.get(); }

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

        if(pedestal.spaceForFluid()>=1000) pumpAction(level, pedestal, allPositions);
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
        //TODO: make a modifier for this
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

    private boolean isToolHighEnoughLevelForBlock(ItemStack toolIn, BlockState getBlock)
    {
        if(toolIn.getItem() instanceof TieredItem tieredItem)
        {
            Tier toolTier = tieredItem.getTier();
            return TierSortingRegistry.isCorrectTierForDrops(toolTier,getBlock);
        }

        return false;
    }



    private boolean canMine(BasePedestalBlockEntity pedestal, BlockState canMineBlock, BlockPos canMinePos)
    {
        boolean returner = false;
        if(!ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation("pedestals","pedestals_cant_pump"))).stream().toList().contains(canMineBlock.getBlock()))
        {
            FluidState fluidState = pedestal.getLevel().getFluidState(canMinePos);
            //System.out.println("issource: "+fluidState.isSource());
            //System.out.println("isblock: "+(canMineBlock.getBlock() instanceof AbstractCauldronBlock));
            if(fluidState.isSource())
            {
                FluidStack fluidStack = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
                if(pedestal.canAcceptFluid(fluidStack))
                {
                    if(pedestal.spaceForFluid() >= FluidType.BUCKET_VOLUME)
                    {
                        returner = pedestal.addFluid(fluidStack, IFluidHandler.FluidAction.SIMULATE)>0;
                    }
                }
            }
            else if(canMineBlock.getBlock() instanceof AbstractCauldronBlock cauldronBlock)
            {
                //System.out.println("isfull: "+(cauldronBlock.isFull(canMineBlock)));
                if(cauldronBlock.isFull(canMineBlock))
                {
                    //System.out.println("islavaORwater: "+(canMineBlock.getBlock().equals(Blocks.LAVA_CAULDRON) || canMineBlock.getBlock().equals(Blocks.WATER_CAULDRON)));
                    if(canMineBlock.getBlock().equals(Blocks.LAVA_CAULDRON) || canMineBlock.getBlock().equals(Blocks.WATER_CAULDRON))
                    {
                        returner = true;
                    }
                }
            }
        }

        return returner;
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
                    FluidState fluidState = pedestal.getLevel().getFluidState(canMinePos);
                    if(fluidState.isSource() || canMineBlock.getBlock() instanceof AbstractCauldronBlock)
                    {
                        FluidStack fluidStack = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
                        if(canMineBlock.getBlock() instanceof AbstractCauldronBlock)
                        {
                            if(canMineBlock.getBlock().equals(Blocks.WATER_CAULDRON))
                            {
                                fluidStack = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
                            }
                            else if(canMineBlock.getBlock().equals(Blocks.LAVA_CAULDRON))
                            {
                                fluidStack = new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME);
                            }
                        }
                        return filter.canAcceptFluids(filterInPedestal,fluidStack);
                    }
                }
            }
        }

        return true;
    }

    private void dropXP(Level level, BasePedestalBlockEntity pedestal, BlockState blockAtPoint, BlockPos currentPoint)
    {
        int fortune = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.BLOCK_FORTUNE))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,pedestal.getToolStack())):(0);
        int silky = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.SILK_TOUCH))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,pedestal.getToolStack())):(0);
        int xpdrop = blockAtPoint.getBlock().getExpDrop(blockAtPoint,level, level.random,currentPoint,fortune,silky);
        if(xpdrop>0)blockAtPoint.getBlock().popExperience((ServerLevel)level,currentPoint,xpdrop);
    }

    private boolean canPlace(ItemStack itemToPlace) {
        Block possibleBlock = Block.byItem(itemToPlace.getItem());
        if(possibleBlock != Blocks.AIR)
        {
            if(!ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_place"))).stream().toList().contains(itemToPlace))
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

    private boolean passesFilterItems(BasePedestalBlockEntity pedestal, BlockState canMineBlock, BlockPos canMinePos)
    {
        if(pedestal.hasFilter())
        {
            ItemStack filterInPedestal = pedestal.getFilterInBlockEntity();
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

    public void pumpAction(Level level, BasePedestalBlockEntity pedestal, List<BlockPos> listed) {
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
                        AABB area = new AABB(MowLibBlockPosUtils.readBlockPosFromNBT(card,1),MowLibBlockPosUtils.readBlockPosFromNBT(card,2));
                        int maxY = (int)area.maxY;
                        int minY = (int)area.minY;
                        int ySpread = maxY - minY;
                        boolean minMaxHeight = (maxY - minY > 0) || listed.size()<=4;
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

                                    //System.out.println("Can Mine: "+ canMine(pedestal, blockAtPoint, adjustedPoint));
                                    //System.out.println("Passes Filter: "+ passesFilter(pedestal, blockAtPoint, adjustedPoint));
                                    if(!adjustedPoint.equals(pedestal.getPos()) && canMine(pedestal, blockAtPoint, adjustedPoint) && passesFilter(pedestal, blockAtPoint, adjustedPoint)) {
                                        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), true)) {
                                            FluidState fluidState = pedestal.getLevel().getFluidState(adjustedPoint);
                                            //System.out.println("source or cauldron: "+ (fluidState.isSource() || blockAtPoint.getBlock() instanceof AbstractCauldronBlock));
                                            if(fluidState.isSource() || blockAtPoint.getBlock() instanceof AbstractCauldronBlock) {
                                                FluidStack fluidStack = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
                                                if(blockAtPoint.getBlock() instanceof AbstractCauldronBlock) {
                                                    if(blockAtPoint.getBlock().equals(Blocks.WATER_CAULDRON)) {
                                                        fluidStack = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
                                                    } else if(blockAtPoint.getBlock().equals(Blocks.LAVA_CAULDRON)) {
                                                        fluidStack = new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME);
                                                    }
                                                }
                                                //System.out.println("can accept: "+ pedestal.canAcceptFluid(fluidStack));
                                                if (pedestal.canAcceptFluid(fluidStack) && pedestal.spaceForFluid() >= FluidType.BUCKET_VOLUME) {
                                                    if (removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(), adjustedPoint), false)) {
                                                        pedestal.addFluid(fluidStack, IFluidHandler.FluidAction.EXECUTE);

                                                        if (blockAtPoint.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                                            if (removeWaterFromLoggedBlocks()) {
                                                                level.setBlockAndUpdate(adjustedPoint, blockAtPoint.setValue(BlockStateProperties.WATERLOGGED, false));
                                                            }
                                                        } else if (blockAtPoint.getBlock() instanceof AbstractCauldronBlock) {
                                                            level.setBlockAndUpdate(adjustedPoint, Blocks.CAULDRON.defaultBlockState());
                                                        } else {
                                                            level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                                            ItemStack itemToPlace = pedestal.getItemInPedestal().copy();
                                                            if(!pedestal.removeItemStack(itemToPlace, true).isEmpty() && canPlace(itemToPlace) && passesFilterItems(pedestal, level.getBlockState(adjustedPoint), adjustedPoint) && !adjustedPoint.equals(pedestal.getPos())) {
                                                                ItemStack itemToRemove = itemToPlace.copy(); // UseOnContext modifies the passed in item stack when returning an InteractionResult.CONSUME, so we need a copy of it for removal.
                                                                UseOnContext blockContext = new UseOnContext(level,(getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()), InteractionHand.MAIN_HAND, itemToPlace, new BlockHitResult(Vec3.ZERO, getPedestalFacing(level,pedestal.getPos()), adjustedPoint, false));
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
