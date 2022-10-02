package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.DispenseFluidContainer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeDrain extends ItemUpgradeBase implements  ISelectablePoints, ISelectableArea
{
    public ItemUpgradeDrain(Properties p_41383_) {
        super(new Properties());
    }

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

    /*private void buildValidBlockList(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<BlockPos> listed = readBlockPosListFromNBT(coin);
        List<BlockPos> valid = new ArrayList<>();
        for (BlockPos pos:listed) {
            if(selectedPointWithinRange(pedestal, pos))
            {
                valid.add(pos);
            }
        }

        saveBlockPosListCustomToNBT(coin,"_validlist",valid);
    }

    private void buildValidBlockListArea(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<BlockPos> valid = new ArrayList<>();
        AABB area = new AABB(readBlockPosFromNBT(pedestal.getCoinOnPedestal(),1),readBlockPosFromNBT(pedestal.getCoinOnPedestal(),2));

        int maxX = (int)area.maxX;
        int maxY = (int)area.maxY;
        int maxZ = (int)area.maxZ;

        //System.out.println("aabbMaxStuff: "+ maxX+","+maxY+","+maxZ);

        int minX = (int)area.minX;
        int minY = (int)area.minY;
        int minZ = (int)area.minZ;

        //System.out.println("aabbMinStuff: "+ minX+","+minY+","+minZ);

        BlockPos pedestalPos = pedestal.getPos();
        for(int i=maxX;i>=minX;i--)
        {
            for(int j=maxZ;j>=minZ;j--)
            {
                BlockPos newPoint = new BlockPos(i,pedestalPos.getY(),j);
                //System.out.println("points: "+ newPoint);
                if(selectedPointWithinRange(pedestal, newPoint))
                {
                    valid.add(newPoint);
                }
            }
        }

        //System.out.println("validList: "+ valid);
        saveBlockPosListCustomToNBT(coin,"_validlist",valid);
    }*/

    private void buildValidBlockList(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<BlockPos> listed = readBlockPosListFromNBT(coin);
        List<BlockPos> valid = new ArrayList<>();
        for (BlockPos pos:listed) {
            if(selectedPointWithinRange(pedestal, pos))
            {
                valid.add(pos);
            }
        }

        saveBlockPosListCustomToNBT(coin,"_validlist",valid);
    }

    private void buildValidBlockListArea(BasePedestalBlockEntity pedestal)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<BlockPos> valid = new ArrayList<>();
        AABB area = new AABB(readBlockPosFromNBT(pedestal.getCoinOnPedestal(),1),readBlockPosFromNBT(pedestal.getCoinOnPedestal(),2));

        int maxX = (int)area.maxX;
        int maxY = (int)area.maxY;
        int maxZ = (int)area.maxZ;

        //System.out.println("aabbMaxStuff: "+ maxX+","+maxY+","+maxZ);

        int minX = (int)area.minX;
        int minY = (int)area.minY;
        int minZ = (int)area.minZ;

        //System.out.println("aabbMinStuff: "+ minX+","+minY+","+minZ);

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
                        //System.out.println("points: "+ newPoint);
                        if(selectedPointWithinRange(pedestal, newPoint))
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
                        //System.out.println("points2: "+ newPoint);
                        if(selectedPointWithinRange(pedestal, newPoint))
                        {
                            valid.add(newPoint);
                        }
                    }
                }
            }
        }

        //System.out.println("validList: "+ valid);
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

        boolean override = hasTwoPointsSelected(coin);
        List<BlockPos> listed = getValidList(pedestal);

        if(override)
        {
            if(listed.size()>0)
            {
                //System.out.println("RunAction");
                if(pedestal.hasFluid())drainAction(level,pedestal);
            }
            else if(selectedAreaWithinRange(pedestal) && !hasBlockListCustomNBTTags(coin,"_validlist"))
            {
                buildValidBlockListArea(pedestal);
                //System.out.println("ListBuilt: "+ getValidList(pedestal));
            }
            else if(!pedestal.getRenderRange())
            {
                pedestal.setRenderRange(true);
            }
        }
        else
        {
            List<BlockPos> getList = readBlockPosListFromNBT(coin);
            if(!override && listed.size()>0)
            {
                if(pedestal.hasFluid())drainAction(level,pedestal);
            }
            else if(getList.size()>0)
            {
                if(!hasBlockListCustomNBTTags(coin,"_validlist"))
                {
                    BlockPos hasValidPos = IntStream.range(0,getList.size())//Int Range
                            .mapToObj((getList)::get)
                            .filter(blockPos -> selectedPointWithinRange(pedestal, blockPos))
                            .findFirst().orElse(BlockPos.ZERO);
                    if(!hasValidPos.equals(BlockPos.ZERO))
                    {
                        buildValidBlockList(pedestal);
                    }
                }
                else if(!pedestal.getRenderRange())
                {
                    pedestal.setRenderRange(true);
                }
            }
        }

        /*ItemStack coin = pedestal.getCoinOnPedestal();
        boolean override = hasTwoPointsSelected(coin);
        List<BlockPos> listed = getValidList(pedestal);

        if(override)
        {
            if(listed.size()>0)
            {
                if(pedestal.hasFluid())upgradeAction(level,pedestal);
            }
            else if(selectedAreaWithinRange(pedestal) && !hasBlockListCustomNBTTags(coin,"_validlist"))
            {
                buildValidBlockListArea(pedestal);
            }
            else if(!pedestal.getRenderRange())
            {
                pedestal.setRenderRange(true);
            }
        }*/
    }

    private int getHeightIteratorValue(BasePedestalBlockEntity pedestal)
    {
        //TODO: make a modifier for this
        return PedestalConfig.COMMON.upgrade_filler_baseBlocksPlaced.get() + pedestal.getItemTransferRateIncreaseFromCapacity();
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
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getOrCreateTag(), (current+1), "_numdelay");
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

    public void drainAction(Level level, BasePedestalBlockEntity pedestal)
    {
        if(!level.isClientSide())
        {
            List<BlockPos> listed = getValidList(pedestal);
            int currentPosition = getCurrentPosition(pedestal);
            BlockPos currentPoint = listed.get(currentPosition);
            BlockState blockAtPoint = level.getBlockState(currentPoint);
            WeakReference<FakePlayer> getPlayer = pedestal.fakePedestalPlayer(pedestal);
            boolean fuelRemoved = true;

            if(pedestal.removeFluid(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE).getAmount() == FluidType.BUCKET_VOLUME)
            {
                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), true))
                {
                    if(canPlace(pedestal,blockAtPoint,currentPoint) && passesFilter(pedestal, blockAtPoint, currentPoint))
                    {
                        FluidStack stackInPedestal = pedestal.getStoredFluid().copy();
                        if(stackInPedestal.getFluid().defaultFluidState() != null && stackInPedestal.getFluid().defaultFluidState().createLegacyBlock() != null)
                        {
                            UseOnContext blockContext = new UseOnContext(level,getPlayer.get(), InteractionHand.MAIN_HAND, FluidUtil.getFilledBucket(stackInPedestal), new BlockHitResult(Vec3.ZERO, getPedestalFacing(level,pedestal.getPos()), currentPoint, false));
                            InteractionResult result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                            if (result == InteractionResult.PASS) {
                                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), false))
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

            /*List<BlockPos> listed = getValidList(pedestal);
            int currentPosition = getCurrentPosition(pedestal);
            BlockPos currentPoint = listed.get(currentPosition);
            AABB area = new AABB(readBlockPosFromNBT(pedestal.getCoinOnPedestal(),1),readBlockPosFromNBT(pedestal.getCoinOnPedestal(),2));
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
            WeakReference<FakePlayer> getPlayer = pedestal.fakePedestalPlayer(pedestal);

            boolean fuelRemoved = true;
            //ToDo: make this a modifier for later
            boolean runsOnce = true;
            boolean stop = getStopped(pedestal);

            if(!stop)
            {
                for(int y=min;y<=max;y++)
                {
                    BlockPos adjustedPoint = new BlockPos(currentPoint.getX(),y,currentPoint.getZ());
                    BlockState blockAtPoint = level.getBlockState(adjustedPoint);
                    if(pedestal.removeFluid(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE).getAmount() == FluidType.BUCKET_VOLUME)
                    {
                        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), true))
                        {
                            if(canPlace(pedestal,blockAtPoint,adjustedPoint) && passesFilter(pedestal, blockAtPoint, adjustedPoint))
                            {
                                FluidStack stackInPedestal = pedestal.getStoredFluid().copy();
                                if(stackInPedestal.getFluid().defaultFluidState() != null && stackInPedestal.getFluid().defaultFluidState().createLegacyBlock() != null)
                                {
                                    UseOnContext blockContext = new UseOnContext(level,getPlayer.get(), InteractionHand.MAIN_HAND, FluidUtil.getFilledBucket(stackInPedestal), new BlockHitResult(Vec3.ZERO, getPedestalFacing(level,pedestal.getPos()), adjustedPoint, false));
                                    InteractionResult result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                                    if (result == InteractionResult.PASS) {
                                        System.out.println(blockAtPoint.getBlock());
                                        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), false))
                                        {
                                            if(blockAtPoint.hasProperty(BlockStateProperties.WATERLOGGED))
                                            {
                                                if(blockAtPoint.getValue(BlockStateProperties.WATERLOGGED)==false)
                                                {
                                                    if(pedestal.removeFluid(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).getFluid().equals(Fluids.WATER))
                                                    {
                                                        level.setBlockAndUpdate(adjustedPoint,blockAtPoint.setValue(BlockStateProperties.WATERLOGGED,true));
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                if(!pedestal.removeFluid(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).isEmpty())
                                                {
                                                    level.setBlockAndUpdate(adjustedPoint,stackInPedestal.getFluid().defaultFluidState().createLegacyBlock());
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
            }*/
        }
    }
}
