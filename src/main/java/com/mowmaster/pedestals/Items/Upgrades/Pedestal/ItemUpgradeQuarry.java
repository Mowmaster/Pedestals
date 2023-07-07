package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.Filters.BaseFilter;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardBase;
import com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeQuarry extends ItemUpgradeBase {
    public ItemUpgradeQuarry(Properties p_41383_) {
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
    public int baseEnergyCostPerDistance() { return PedestalConfig.COMMON.upgrade_quarry_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_quarry_energy_distance_multiplier.get(); }
    @Override
    public double energyCostMultiplier() { return PedestalConfig.COMMON.upgrade_quarry_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance() { return PedestalConfig.COMMON.upgrade_quarry_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_quarry_xp_distance_multiplier.get(); }
    @Override
    public double xpCostMultiplier() { return PedestalConfig.COMMON.upgrade_quarry_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance() { return new DustMagic(PedestalConfig.COMMON.upgrade_quarry_dustColor.get(),PedestalConfig.COMMON.upgrade_quarry_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() { return PedestalConfig.COMMON.upgrade_quarry_dust_distance_multiplier.get(); }
    @Override
    public double dustCostMultiplier() { return PedestalConfig.COMMON.upgrade_quarry_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_quarry_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier() { return PedestalConfig.COMMON.upgrade_quarry_selectedMultiplier.get(); }

    @Override
    public List<String> getUpgradeHUD(BasePedestalBlockEntity pedestal) {
        List<String> messages = super.getUpgradeHUD(pedestal);
        if(messages.isEmpty()) {
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
    public ItemStack getUpgradeDefaultTool() {
        return new ItemStack(Items.IRON_PICKAXE);
    }

    private void buildValidBlockListArea(BasePedestalBlockEntity pedestal) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<BlockPos> valid = new ArrayList<>();
        if(pedestal.hasWorkCard())
        {
            ItemStack card = pedestal.getWorkCardInPedestal();
            if(card.getItem() instanceof WorkCardBase workCardBase)
            {
                AABB area = new AABB(MowLibBlockPosUtils.readBlockPosFromNBT(card,1), MowLibBlockPosUtils.readBlockPosFromNBT(card,2));

                int maxX = (int)area.maxX;
                int maxY = (int)area.maxY;
                int maxZ = (int)area.maxZ;


                int minX = (int)area.minX;
                int minY = (int)area.minY;
                int minZ = (int)area.minZ;


                BlockPos pedestalPos = pedestal.getPos();
                for(int i=maxX;i>=minX;i--)
                {
                    for(int j=maxZ;j>=minZ;j--)
                    {
                        BlockPos newPoint = new BlockPos(i,pedestalPos.getY(),j);
                        if(MowLibBlockPosUtils.selectedPointWithinRange(pedestal, newPoint, getUpgradeWorkRange(coin)))
                        {
                            valid.add(newPoint);
                        }
                    }
                }

                saveBlockPosListCustomToNBT(MODID,"_validlist",coin,valid);
            }
        }
    }

    private List<BlockPos> getValidList(BasePedestalBlockEntity pedestal) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        return readBlockPosListCustomFromNBT(MODID,"_validlist",coin);
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        removeBlockListCustomNBTTags(MODID,"_validlist",coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numposition");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numdelay");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numheight");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_boolstop");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        if (level.isClientSide()) return;

        if(pedestal.hasWorkCard())
        {
            ItemStack card = pedestal.getWorkCardInPedestal();
            boolean override = hasTwoPointsSelected(card);
            List<BlockPos> listed = getValidList(pedestal);

            if(override)
            {
                if(listed.size()>0)
                {
                    quarryAction(level, pedestal, pedestalPos);
                }
                else if(MowLibBlockPosUtils.selectedAreaWithinRange(pedestal, getUpgradeWorkRange(coin)) && !hasBlockListCustomNBTTags(MODID,"_validlist",coin))
                {
                    buildValidBlockListArea(pedestal);
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
        return PedestalConfig.COMMON.upgrade_quarry_baseBlocksMined.get() + getBlockCapacityIncrease(pedestal.getCoinOnPedestal());
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
    private boolean canMine(Block targetBlock) {
        List<Block> canChopCannotQuarryBlocks = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_can_chop_cant_quarry"))).stream().toList();
        List<Block> cannotBreakBlocks = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_break"))).stream().toList();

        return !targetBlock.equals(Blocks.AIR) &&
            !canChopCannotQuarryBlocks.contains(targetBlock) &&
            !cannotBreakBlocks.contains(targetBlock);
    }

    private void dropXP(Level level, BasePedestalBlockEntity pedestal, BlockState blockAtPoint, BlockPos currentPoint) {
        int fortune = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.BLOCK_FORTUNE))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,pedestal.getToolStack())):(0);
        int silky = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.SILK_TOUCH))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,pedestal.getToolStack())):(0);
        int xpdrop = blockAtPoint.getBlock().getExpDrop(blockAtPoint,level, level.random,currentPoint,fortune,silky);
        if(xpdrop>0)blockAtPoint.getBlock().popExperience((ServerLevel)level,currentPoint,xpdrop);
    }

    public void quarryAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos) {
        WeakReference<FakePlayer> fakePlayerReference = pedestal.getPedestalPlayer(pedestal);
        if(fakePlayerReference != null && fakePlayerReference.get() != null) {
            FakePlayer fakePlayer = fakePlayerReference.get();
            if(pedestal.hasWorkCard())
            {
                ItemStack card = pedestal.getWorkCardInPedestal();
                if(card.getItem() instanceof WorkCardBase)
                {
                    List<BlockPos> listed = getValidList(pedestal);
                    int currentPosition = getCurrentPosition(pedestal);
                    BlockPos currentPoint = listed.get(currentPosition);
                    AABB area = new AABB(MowLibBlockPosUtils.readBlockPosFromNBT(card,1), MowLibBlockPosUtils.readBlockPosFromNBT(card,2));
                    int maxY = (int)area.maxY;
                    int minY = (int)area.minY;
                    int ySpread = maxY - minY;
                    boolean minMaxHeight = ySpread > 0;
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

                    List<BlockPos> adjustedPoints = IntStream.range(min, max).mapToObj(y -> new BlockPos(currentPoint.getX(), y, currentPoint.getZ())).toList();
                    List<Integer> distances = adjustedPoints.stream().map(point -> getDistanceBetweenPoints(pedestalPos, point)).toList();
                    if (removeFuelForActionMultiple(pedestal, distances, true)) {
                        if(!stop) {
                            for (BlockPos adjustedPoint : adjustedPoints) {
                                if (adjustedPoint.equals(pedestalPos)) {
                                    continue;
                                }
                                BlockState blockAtPoint = level.getBlockState(adjustedPoint);
                                Block targetBlock = blockAtPoint.getBlock();
                                //blockAtPoint.requiresCorrectToolForDrops();

                                if(blockAtPoint.getDestroySpeed(level,currentPoint)>=0)
                                {
                                    if(ForgeEventFactory.doPlayerHarvestCheck(fakePlayer, blockAtPoint, true))
                                    {
                                        BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(level, adjustedPoint, blockAtPoint, fakePlayer);
                                        if(!MinecraftForge.EVENT_BUS.post(e))
                                        {
                                            if(canMine(targetBlock) && passesMachineFilterItems(pedestal, blockAtPoint.getBlock().getCloneItemStack(level, adjustedPoint, blockAtPoint))) {
                                                //ToDo: config option
                                                boolean damage = false;

                                                if(PedestalConfig.COMMON.quarryDamageTools.get()) {
                                                    if(pedestal.hasTool()) {
                                                        if(pedestal.getDurabilityRemainingOnInsertedTool() > 0) {
                                                            if(pedestal.damageInsertedTool(1,true)) {
                                                                damage = true;
                                                            }
                                                        }
                                                        if (!damage) {
                                                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                                                            return;
                                                        }
                                                    }
                                                }

                                                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), false)) {
                                                    boolean canRemoveBlockEntities = PedestalConfig.COMMON.blockBreakerBreakEntities.get();
                                                    List<ItemStack> drops = getBlockDrops(level, pedestal, fakePlayer, blockAtPoint);
                                                    if (level.getBlockEntity(adjustedPoint) != null) {
                                                        if(canRemoveBlockEntities) {
                                                            blockAtPoint.onRemove(level, adjustedPoint, blockAtPoint,true);
                                                            level.removeBlockEntity(adjustedPoint);
                                                            dropXP(level, pedestal, blockAtPoint, adjustedPoint);
                                                            level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                                            if (damage) pedestal.damageInsertedTool(1, false);

                                                            for (ItemStack drop: drops) {
//                                                                    MowLibItemUtils.spawnItemStack(level, adjustedPoint.getX(), adjustedPoint.getY(), adjustedPoint.getZ(), drop);
                                                                MowLibItemUtils.spawnItemStack(level, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), drop);
                                                            }
                                                        }
                                                    } else {
                                                        dropXP(level, pedestal, blockAtPoint, adjustedPoint);
                                                        level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                                        if (damage) pedestal.damageInsertedTool(1, false);

                                                        for (ItemStack drop: drops) {
//                                                                MowLibItemUtils.spawnItemStack(level, adjustedPoint.getX(), adjustedPoint.getY(), adjustedPoint.getZ(), drop);
                                                            MowLibItemUtils.spawnItemStack(level, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), drop);
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
                        } else {
                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level, pedestalPos, new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED, pedestalPos.getX(), pedestalPos.getY()+1.0f, pedestalPos.getZ(),55,55,55));
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
