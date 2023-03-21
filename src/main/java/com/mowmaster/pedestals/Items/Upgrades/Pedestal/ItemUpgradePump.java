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


    private void buildValidBlockList(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        if(pedestal.hasWorkCard())
        {
            ItemStack card = pedestal.getWorkCardInPedestal();
            if(card.getItem() instanceof WorkCardBase workCardBase)
            {
                List<BlockPos> listed = workCardBase.readBlockPosListFromNBT(card);
                List<BlockPos> valid = new ArrayList<>();
                for (BlockPos pos:listed) {
                    if(workCardBase.selectedPointWithinRange(pedestal, pos))
                    {
                        valid.add(pos);
                    }
                }

                saveBlockPosListCustomToNBT(coin,"_validlist",valid);
            }
        }
    }

    private void buildValidBlockListArea(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<BlockPos> valid = new ArrayList<>();
        if(pedestal.hasWorkCard())
        {
            ItemStack card = pedestal.getWorkCardInPedestal();
            if(card.getItem() instanceof WorkCardBase workCardBase)
            {
                AABB area = new AABB(workCardBase.readBlockPosFromNBT(card,1),workCardBase.readBlockPosFromNBT(card,2));

                int maxX = (int)area.maxX;
                int maxY = (int)area.maxY;
                int maxZ = (int)area.maxZ;

                int minX = (int)area.minX;
                int minY = (int)area.minY;
                int minZ = (int)area.minZ;

                BlockPos pedestalPos = pedestal.getPos();
                if(minY < pedestalPos.getY())
                {
                    for(int i=maxX;i>=minX;i--)
                    {
                        for(int j=maxZ;j>=minZ;j--)
                        {
                            for(int k=maxY;k>=minY;k--)
                            {
                                BlockPos newPoint = new BlockPos(i,k,j);
                                if(workCardBase.selectedPointWithinRange(pedestal, newPoint))
                                {
                                    valid.add(newPoint);
                                }
                            }
                        }
                    }
                }
                else
                {
                    for(int i= minX;i<=maxX;i++)
                    {
                        for(int j= minZ;j<=maxZ;j++)
                        {
                            for(int k= minY;k<=maxY;k++)
                            {
                                BlockPos newPoint = new BlockPos(i,k,j);
                                if(workCardBase.selectedPointWithinRange(pedestal, newPoint))
                                {
                                    valid.add(newPoint);
                                }
                            }
                        }
                    }
                }
            }
        }

        saveBlockPosListCustomToNBT(coin,"_validlist",valid);
    }

    private List<BlockPos> getValidList(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return readBlockPosListCustomFromNBT(coin,"_validlist");
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        removeBlockListCustomNBTTags(coinInPedestal, "_validlist");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numposition");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numdelay");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numheight");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_boolstop");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {

        if(pedestal.hasWorkCard())
        {
            ItemStack card = pedestal.getWorkCardInPedestal();
            if(card.getItem() instanceof WorkCardBase workCardBase)
            {
                boolean override = workCardBase.hasTwoPointsSelected(card);
                List<BlockPos> listed = getValidList(pedestal);

                if(override)
                {
                    if(listed.size()>0)
                    {
                        if(pedestal.spaceForFluid()>=1000)pumpAction(level,pedestal);
                    }
                    else if(workCardBase.selectedAreaWithinRange(pedestal) && !hasBlockListCustomNBTTags(coin,"_validlist"))
                    {
                        buildValidBlockListArea(pedestal);
                    }
                }
            }
        }
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

    private List<ItemStack> getBlockDrops(BasePedestalBlockEntity pedestal, BlockState blockTarget, BlockPos posTarget)
    {
        ItemStack getToolFromPedestal = (pedestal.getToolStack().isEmpty())?(new ItemStack(Items.IRON_AXE)):(pedestal.getToolStack());

        Level level = pedestal.getLevel();
        if(blockTarget.getBlock() != Blocks.AIR)
        {
            LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                    .withRandom(level.random)
                    .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                    .withParameter(LootContextParams.TOOL, getToolFromPedestal);

            return blockTarget.getBlock().getDrops(blockTarget,builder);
        }
        /*if(blockTarget.requiresCorrectToolForDrops())
        {
            if(isToolHighEnoughLevelForBlock(getToolFromPedestal, blockTarget))
            {
                Level level = pedestal.getLevel();
                if(blockTarget.getBlock() != Blocks.AIR)
                {
                    LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                            .withRandom(level.random)
                            .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                            .withParameter(LootContextParams.TOOL, getToolFromPedestal);

                    return blockTarget.getBlock().getDrops(blockTarget,builder);
                }
            }
        }
        else
        {
            Level level = pedestal.getLevel();
            if(blockTarget.getBlock() != Blocks.AIR)
            {
                LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                        .withRandom(level.random)
                        .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                        .withParameter(LootContextParams.TOOL, getToolFromPedestal);

                return blockTarget.getBlock().getDrops(blockTarget,builder);
            }
        }*/

        return new ArrayList<>();
    }

    private boolean canMine(BasePedestalBlockEntity pedestal, BlockState canMineBlock, BlockPos canMinePos)
    {
        boolean returner = false;
        if(!ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation("pedestals","pedestals_cant_pump"))).stream().toList().contains(canMineBlock.getBlock()))
        {
            FluidState fluidState = pedestal.getLevel().getFluidState(canMinePos);
            if(fluidState.isSource())
            {
                FluidStack fluidStack = new FluidStack(fluidState.getType(), 1000);
                if(pedestal.canAcceptFluid(fluidStack))
                {
                    if(pedestal.spaceForFluid() >= 1000)
                    {
                        returner = pedestal.addFluid(fluidStack, IFluidHandler.FluidAction.SIMULATE)>0;
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
            ItemStack filterInPedestal = pedestal.getFilterInPedestal();
            if(filterInPedestal.getItem() instanceof BaseFilter filter)
            {
                if(filter.getFilterDirection().neutral())
                {
                    FluidState fluidState = pedestal.getLevel().getFluidState(canMinePos);
                    if(fluidState.isSource())
                    {
                        FluidStack fluidStack = new FluidStack(fluidState.getType(), 1000);
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

    private boolean canPlace(BasePedestalBlockEntity pedestal)
    {
        ItemStack getPlaceItem = pedestal.getItemInPedestal();
        Block possibleBlock = Block.byItem(getPlaceItem.getItem());
        if(possibleBlock != Blocks.AIR)
        {
            if(!ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_place"))).stream().toList().contains(getPlaceItem))
            {
                if(possibleBlock instanceof IPlantable ||
                        possibleBlock instanceof BushBlock ||
                        possibleBlock instanceof StemBlock ||
                        possibleBlock instanceof BonemealableBlock ||
                        possibleBlock instanceof ChorusFlowerBlock
                )
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

    public void pumpAction(Level level, BasePedestalBlockEntity pedestal)
    {
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
                        List<BlockPos> listed = getValidList(pedestal);
                        int currentPosition = getCurrentPosition(pedestal);
                        BlockPos currentPoint = listed.get(currentPosition);
                        AABB area = new AABB(workCardBase.readBlockPosFromNBT(card,1),workCardBase.readBlockPosFromNBT(card,2));
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

                                    if(!adjustedPoint.equals(pedestal.getPos()))
                                    {
                                        if(canMine(pedestal, blockAtPoint, adjustedPoint))
                                        {
                                            if(passesFilter(pedestal, blockAtPoint, adjustedPoint))
                                            {
                                                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), true))
                                                {
                                                    FluidState fluidState = pedestal.getLevel().getFluidState(adjustedPoint);
                                                    if(fluidState.isSource())
                                                    {
                                                        FluidStack fluidStack = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
                                                        if(pedestal.canAcceptFluid(fluidStack))
                                                        {
                                                            if(pedestal.spaceForFluid() >= FluidType.BUCKET_VOLUME)
                                                            {
                                                                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), false))
                                                                {
                                                                    //Store Fluid
                                                                    pedestal.addFluid(fluidStack, IFluidHandler.FluidAction.EXECUTE);

                                                                    if(blockAtPoint.hasProperty(BlockStateProperties.WATERLOGGED))
                                                                    {
                                                                        if(removeWaterFromLoggedBlocks())level.setBlockAndUpdate(adjustedPoint,blockAtPoint.setValue(BlockStateProperties.WATERLOGGED,false));
                                                                    }
                                                                    else
                                                                    {
                                                                        if(!pedestal.removeItem(1,true).isEmpty())
                                                                        {
                                                                            level.setBlockAndUpdate(adjustedPoint,Blocks.AIR.defaultBlockState());
                                                                            if(canPlace(pedestal) && passesFilterItems(pedestal, level.getBlockState(adjustedPoint), adjustedPoint))
                                                                            {
                                                                                if(!adjustedPoint.equals(pedestal.getPos()))
                                                                                {
                                                                                    UseOnContext blockContext = new UseOnContext(level,(getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()), InteractionHand.MAIN_HAND, pedestal.getItemInPedestal().copy(), new BlockHitResult(Vec3.ZERO, getPedestalFacing(level,pedestal.getPos()), adjustedPoint, false));
                                                                                    InteractionResult resultPlace = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                                                                                    if (resultPlace == InteractionResult.CONSUME) {
                                                                                        pedestal.removeItem(1,false);
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                        else
                                                                        {
                                                                            level.setBlockAndUpdate(adjustedPoint,Blocks.AIR.defaultBlockState());
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
