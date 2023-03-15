package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import com.mowmaster.pedestals.Items.ISelectableArea;
import com.mowmaster.pedestals.Items.ISelectablePoints;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.IFluidBlock;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeDropper extends ItemUpgradeBase implements IHasModeTypes, ISelectablePoints, ISelectableArea
{
    public ItemUpgradeDropper(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyItemCapacity(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyXPCapacity(ItemStack upgradeItemStack) {
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

    public boolean canDropBolts(){ return PedestalConfig.COMMON.upgrade_dropper_canDropBolt.get(); }
    public int baseEnergyCostPerDrop(){ return PedestalConfig.COMMON.upgrade_dropper_costPerBolt.get(); }

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
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin){

        boolean override = hasTwoPointsSelected(coin);
        List<BlockPos> listed = getValidList(pedestal);

        if(override)
        {
            if(listed.size()>0)
            {
                //System.out.println("RunAction");
                dropperAction(level,pedestal);
            }
            else if(selectedAreaWithinRange(pedestal) && !hasBlockListCustomNBTTags(coin,"_validlist"))
            {
                buildValidBlockListArea(pedestal);
                //System.out.println("ListBuilt: "+ getValidList(pedestal));
            }
            else if(!pedestal.getRenderRangeUpgrade())
            {
                pedestal.setRenderRangeUpgrade(true);
            }
        }
        else
        {
            List<BlockPos> getList = readBlockPosListFromNBT(coin);
            if(!override && listed.size()>0)
            {
                dropperAction(level,pedestal);
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
                else if(!pedestal.getRenderRangeUpgrade())
                {
                    pedestal.setRenderRangeUpgrade(true);
                }
            }
        }
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
                    return filter.canAcceptItems(filterInPedestal,pedestal.getItemInPedestal());
                }
            }
        }

        return true;
    }

    public void dropperAction(Level level, BasePedestalBlockEntity pedestal)
    {
        if(!level.isClientSide())
        {
            WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
            if(getPlayer != null && getPlayer.get() != null)
            {
                List<BlockPos> listed = getValidList(pedestal);
                int currentPosition = getCurrentPosition(pedestal);
                BlockPos currentPoint = listed.get(currentPosition);
                BlockState blockAtPoint = level.getBlockState(currentPoint);
                ItemStack coinUpgrade = pedestal.getCoinOnPedestal();

                if(canTransferItems(coinUpgrade) && pedestal.hasItem())
                {
                    if(passesFilter(pedestal, blockAtPoint, currentPoint))
                    {
                        if(!currentPoint.equals(pedestal.getPos()) && (level.getBlockState(currentPoint).getBlock() == Blocks.AIR || level.getBlockState(currentPoint).getBlock() instanceof IFluidBlock))
                        {
                            ItemStack itemToDrop = pedestal.getItemInPedestal().copy();
                            int baseRate = PedestalConfig.COMMON.pedestal_baseItemTransferRate.get();
                            int maxRate = baseRate + getItemCapacityIncrease(pedestal.getCoinOnPedestal());
                            int countToDrop = (maxRate>=itemToDrop.getMaxStackSize())?(itemToDrop.getMaxStackSize()):(maxRate);
                            if(!pedestal.removeItem(countToDrop,true).isEmpty())
                            {
                                ItemStack dropMe = pedestal.removeItem(countToDrop,false);
                                ItemEntity itementity = new ItemEntity(level, (double)currentPoint.getX(), (double)currentPoint.getY(), (double)currentPoint.getZ(), dropMe);
                                itementity.setDefaultPickUpDelay();
                                itementity.setDeltaMovement(0.0,0.0,0.0);
                                itementity.moveTo(Vec3.atCenterOf(currentPoint));
                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,currentPoint.getX()+0.5D,currentPoint.getY()+0.5D,currentPoint.getZ()+0.5D,255,255,255));
                                level.addFreshEntity(itementity);
                            }
                        }
                    }
                }

                if(canTransferEnergy(coinUpgrade) && canDropBolts() && pedestal.getStoredEnergy() >= baseEnergyCostPerDrop())
                {
                    if(pedestal.removeEnergy(baseEnergyCostPerDrop(),true)>0)
                    {
                        Random rand = new Random();
                        LightningBolt lightningbolt = (LightningBolt) EntityType.LIGHTNING_BOLT.create(level);
                        lightningbolt.moveTo(Vec3.atBottomCenterOf(currentPoint));
                        lightningbolt.setCause(getPlayer.get());
                        pedestal.removeEnergy(baseEnergyCostPerDrop(),false);
                        level.addFreshEntity(lightningbolt);
                        level.playSound((Player)null, currentPoint, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
                    }
                }

                if(canTransferXP(coinUpgrade) && pedestal.hasExperience())
                {
                    int baseValue = PedestalConfig.COMMON.pedestal_baseXpTransferRate.get();
                    int maxValue = baseValue + getXPCapacityIncrease(pedestal.getCoinOnPedestal());
                    int getxpAmountToDrop = (maxValue >= pedestal.getStoredExperience())?(pedestal.getStoredExperience()):(maxValue);
                    if(pedestal.removeExperience(getxpAmountToDrop,true)>0)
                    {
                        ExperienceOrb xpEntity = new ExperienceOrb(level, (double)currentPoint.getX(), (double)currentPoint.getY(), (double)currentPoint.getZ(), pedestal.removeExperience(getxpAmountToDrop,false));
                        xpEntity.moveTo(Vec3.atCenterOf(currentPoint));
                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(pedestal.getLevel(),pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,currentPoint.getX()+0.5D,currentPoint.getY()+0.5D,currentPoint.getZ()+0.5D,0,255,50));
                        level.addFreshEntity(xpEntity);
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
