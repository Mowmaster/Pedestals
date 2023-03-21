package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import com.mowmaster.pedestals.Items.ISelectableArea;
import com.mowmaster.pedestals.Items.ISelectablePoints;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeBlockPlacer extends ItemUpgradeBase
{
    public ItemUpgradeBlockPlacer(Properties p_41383_) {
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
    public boolean needsWorkCard() { return true; }

    @Override
    public int getWorkCardType() { return 0; }

    //Requires energy

    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_blockplacer_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_blockplacer_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_blockplacer_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_blockplacer_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_blockplacer_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_blockplacer_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_blockplacer_dustColor.get(),PedestalConfig.COMMON.upgrade_blockplacer_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_blockplacer_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_blockplacer_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_blockplacer_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_blockplacer_selectedMultiplier.get(); }

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
                                //System.out.println("points2: "+ newPoint);
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
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {
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
                        if(pedestal.hasItem())placerAction(level,pedestal);
                    }
                    else if(workCardBase.selectedAreaWithinRange(pedestal) && !hasBlockListCustomNBTTags(coin,"_validlist"))
                    {
                        buildValidBlockListArea(pedestal);
                    }
                }
                else
                {
                    List<BlockPos> getList = workCardBase.readBlockPosListFromNBT(card);
                    if(!override && listed.size()>0)
                    {
                        placerAction(level,pedestal);
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
                    }
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

    public void placerAction(Level level, BasePedestalBlockEntity pedestal)
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
                boolean fuelRemoved = true;
                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), true))
                {
                    if(!pedestal.removeItem(1,true).isEmpty())
                    {
                        if(canPlace(pedestal) && passesFilter(pedestal, blockAtPoint, currentPoint))
                        {
                            if(!currentPoint.equals(pedestal.getPos()) && level.getBlockState(currentPoint).getBlock() == Blocks.AIR)
                            {
                                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), false))
                                {
                                    UseOnContext blockContext = new UseOnContext(level,(getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()), InteractionHand.MAIN_HAND, pedestal.getItemInPedestal().copy(), new BlockHitResult(Vec3.ZERO, getPedestalFacing(level,pedestal.getPos()), currentPoint, false));
                                    InteractionResult result = ForgeHooks.onPlaceItemIntoWorld(blockContext);
                                    if (result == InteractionResult.CONSUME) {
                                        pedestal.removeItem(1,false);
                                    }
                                }
                                else {
                                    fuelRemoved = false;
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
                        if(fuelRemoved){
                            iterateCurrentPosition(pedestal);
                        }
                    }
                }

            /*
            //Wither Skull Placement
            if (level.isEmptyBlock(blockpos) && WitherSkullBlock.canSpawnMob(level, blockpos, p_123434_)) {
               level.setBlock(blockpos, Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, Integer.valueOf(direction.getAxis() == Direction.Axis.Y ? 0 : direction.getOpposite().get2DDataValue() * 4)), 3);
               level.gameEvent((Entity)null, GameEvent.BLOCK_PLACE, blockpos);
               BlockEntity blockentity = level.getBlockEntity(blockpos);
               if (blockentity instanceof SkullBlockEntity) {
                  WitherSkullBlock.checkSpawn(level, blockpos, (SkullBlockEntity)blockentity);
               }

               p_123434_.shrink(1);
               this.setSuccess(true);
            }
             */


            }
        }
    }
}
