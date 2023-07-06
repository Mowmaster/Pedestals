package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.Filters.BaseFilter;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;
import static com.mowmaster.pedestals.PedestalUtils.References.isQuarkLoaded;

public class ItemUpgradeHarvester extends ItemUpgradeBase
{
    public ItemUpgradeHarvester(Properties p_41383_) {
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
    public boolean canModifySuperSpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifyGentleHarvest(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean needsWorkCard() { return true; }

    @Override
    public int getWorkCardType() { return 0; }

    //Requires energy

    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_harvester_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_harvester_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_harvester_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_harvester_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_harvester_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_harvester_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_harvester_dustColor.get(),PedestalConfig.COMMON.upgrade_harvester_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_harvester_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_harvester_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_harvester_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_harvester_selectedMultiplier.get(); }

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
    public ItemStack getUpgradeDefaultTool() {
        if(isQuarkLoaded()) { return new ItemStack(Items.STICK);}
        return new ItemStack(Items.STONE_HOE);
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        resetCachedValidWorkCardPositions(MODID, coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numposition");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        List<BlockPos> allPositions = getValidWorkCardPositions(pedestal, coin, getWorkCardType(),MODID);
        if (allPositions.isEmpty()) return;

        harvesterAction(level, pedestal, allPositions);
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
            ItemStack filterInPedestal = pedestal.getFilterInBlockEntity();
            if(filterInPedestal.getItem() instanceof BaseFilter filter)
            {
                if(filter.getFilterDirection().neutral())
                {
                    ItemStack blockToCheck = ItemStack.EMPTY;
                    if(canMineBlock.getBlock() instanceof Block)
                    {
                        blockToCheck = canMineBlock.getBlock().getCloneItemStack(pedestal.getLevel(),canMinePos,canMineBlock);
                    }

                    return filter.canAcceptItems(filterInPedestal,blockToCheck);
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

    private boolean canMine(BasePedestalBlockEntity pedestal, BlockState canMineBlock, BlockPos canMinePos)
    {
        boolean returner = false;
        IntegerProperty propInt = getBlockPropertyAge(canMineBlock);

        //KelpBlock == GrowingPlantBlock
        if (canMineBlock.getBlock() instanceof GrowingPlantBlock
                || canMineBlock.getBlock() instanceof BambooStalkBlock
                || canMineBlock.getBlock() instanceof BambooSaplingBlock
                || canMineBlock.getBlock() instanceof SugarCaneBlock
                || canMineBlock.getBlock() instanceof CactusBlock)
        {
            if(pedestal.getLevel().getBlockState(canMinePos.below()).getBlock().equals(canMineBlock.getBlock()))
            {
                returner = true;
            }
        }
        else if (canMineBlock.getBlock() instanceof StemGrownBlock)
        {
            returner = true;
        }
        else if (canMineBlock.getBlock() instanceof StemBlock)
        {
            returner = false;
        }
        else if (propInt == null || !(pedestal.getLevel() instanceof ServerLevel))
        {
            returner = false;
        }
        else
        {
            if(propInt != null)
            {
                int current = canMineBlock.getValue(propInt);
                //int min = Collections.min(propInt.getPossibleValues());
                int max = Collections.max(propInt.getPossibleValues());
                if(current == max)
                {
                    returner = true;
                }
            }
        }

        return returner;
    }

    private void dropXP(Level level, BasePedestalBlockEntity pedestal, BlockState blockAtPoint, BlockPos currentPoint)
    {
        int fortune = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.BLOCK_FORTUNE))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,pedestal.getToolStack())):(0);
        int silky = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.SILK_TOUCH))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,pedestal.getToolStack())):(0);
        int xpdrop = blockAtPoint.getBlock().getExpDrop(blockAtPoint,level, level.random,currentPoint,fortune,silky);
        if(xpdrop>0)blockAtPoint.getBlock().popExperience((ServerLevel)level,currentPoint,xpdrop);
    }
    
    private void updateHarvestedBlock(Level level, BasePedestalBlockEntity pedestal, BlockState blockAtPoint, BlockPos currentPoint, boolean hasGentle)
    {
        IntegerProperty propInt = getBlockPropertyAge(blockAtPoint);
        int min = (propInt == null)?(0):(Collections.min(propInt.getPossibleValues()));

        //KelpBlock == GrowingPlantBlock
        if (blockAtPoint.getBlock() instanceof GrowingPlantBlock
                || blockAtPoint.getBlock() instanceof BambooStalkBlock
                || blockAtPoint.getBlock() instanceof BambooSaplingBlock
                || blockAtPoint.getBlock() instanceof SugarCaneBlock
                || blockAtPoint.getBlock() instanceof CactusBlock)
        {
            level.setBlockAndUpdate(currentPoint, Blocks.AIR.defaultBlockState());
        }
        else
        {
            level.setBlockAndUpdate(currentPoint, (propInt != null && hasGentleHarvest(pedestal.getCoinOnPedestal()))?(blockAtPoint.setValue(propInt,min)):(Blocks.AIR.defaultBlockState()));
        }
    }

    public void harvesterAction(Level level, BasePedestalBlockEntity pedestal, List<BlockPos> listed) {
        if(!level.isClientSide())
        {
            WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
            if(getPlayer != null && getPlayer.get() != null)
            {
                boolean hasGentle = hasGentleHarvest(pedestal.getCoinOnPedestal());
                if(hasSuperSpeed(pedestal.getCoinOnPedestal()))
                {
                    for(BlockPos currentPoint:listed)
                    {
                        BlockState blockAtPoint = level.getBlockState(currentPoint);
                        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), true))
                        {
                            if(!blockAtPoint.getBlock().equals(Blocks.AIR) && blockAtPoint.getDestroySpeed(level,currentPoint)>=0)
                            {
                                if(passesFilter(pedestal, blockAtPoint, currentPoint) && (!ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_break"))).stream().toList().contains(blockAtPoint.getBlock())))
                                {
                                    if(canMine(pedestal,blockAtPoint,currentPoint))
                                    {
                                        if(ForgeEventFactory.doPlayerHarvestCheck((getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()), blockAtPoint, true)) {
                                            BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(level, currentPoint, blockAtPoint, (getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()));
                                            if (!MinecraftForge.EVENT_BUS.post(e)) {
                                                boolean damage = false;
                                                if(!currentPoint.equals(pedestal.getPos()))
                                                {
                                                    if(PedestalConfig.COMMON.blockBreakerDamageTools.get())
                                                    {
                                                        if(pedestal.hasTool())
                                                        {
                                                            BlockPos pedestalPos = pedestal.getPos();
                                                            if(pedestal.getDurabilityRemainingOnInsertedTool()>0)
                                                            {
                                                                if(pedestal.damageInsertedTool(1,true))
                                                                {
                                                                    damage = true;
                                                                }
                                                                else
                                                                {
                                                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                                                                    return;
                                                                }
                                                            }
                                                            else
                                                            {
                                                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                                                                return;
                                                            }
                                                        }
                                                    }

                                                    if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), false))
                                                    {
                                                        boolean canRemoveBlockEntities = PedestalConfig.COMMON.blockBreakerBreakEntities.get();
                                                        List<ItemStack> drops = getBlockDrops(level, pedestal, getPlayer.get(),blockAtPoint);
                                                        if(canRemoveBlockEntities){
                                                            if(level.getBlockEntity(currentPoint) !=null)
                                                            {
                                                                IntegerProperty propInt = getBlockPropertyAge(blockAtPoint);
                                                                int min = (propInt == null)?(0):(Collections.min(propInt.getPossibleValues()));
                                                                updateHarvestedBlock(level, pedestal, blockAtPoint, currentPoint, hasGentle);
                                                                //level.setBlockAndUpdate(currentPoint, (propInt != null && hasGentleHarvest(pedestal.getCoinOnPedestal()))?(blockAtPoint.setValue(propInt,min)):(Blocks.AIR.defaultBlockState()));
                                                                dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                                if(damage)pedestal.damageInsertedTool(1,false);
                                                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),255,246,0));
                                                            }
                                                        }
                                                        else
                                                        {
                                                            IntegerProperty propInt = getBlockPropertyAge(blockAtPoint);
                                                            int min = (propInt == null)?(0):(Collections.min(propInt.getPossibleValues()));
                                                            updateHarvestedBlock(level, pedestal, blockAtPoint, currentPoint, hasGentle);                                                            dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                            if(damage)pedestal.damageInsertedTool(1,false);
                                                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),255,246,0));
                                                        }

                                                        if(drops.size()>0)
                                                        {
                                                            for (ItemStack stack: drops) {
                                                                if(hasGentleHarvest(pedestal.getCoinOnPedestal()))
                                                                {
                                                                    if(blockAtPoint.getBlock() instanceof CropBlock cropBlock)
                                                                    {
                                                                        ItemStack seedStack = cropBlock.getCloneItemStack(level,currentPoint,blockAtPoint);
                                                                        if(doItemsMatch(seedStack,stack))
                                                                        {
                                                                            stack.shrink(1);
                                                                            MowLibItemUtils.spawnItemStack(level,currentPoint.getX(),currentPoint.getY(),currentPoint.getZ(),stack);
                                                                        }
                                                                        else
                                                                        {
                                                                            MowLibItemUtils.spawnItemStack(level,currentPoint.getX(),currentPoint.getY(),currentPoint.getZ(),stack);
                                                                        }
                                                                    }
                                                                    else
                                                                    {
                                                                        MowLibItemUtils.spawnItemStack(level,currentPoint.getX(),currentPoint.getY(),currentPoint.getZ(),stack);
                                                                    }
                                                                }
                                                                else
                                                                {
                                                                    MowLibItemUtils.spawnItemStack(level,currentPoint.getX(),currentPoint.getY(),currentPoint.getZ(),stack);
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
                        }
                    }
                }
                else
                {
                    int currentPosition = getCurrentPosition(pedestal);
                    BlockPos currentPoint = listed.get(currentPosition);
                    BlockState blockAtPoint = level.getBlockState(currentPoint);
                    boolean fuelRemoved = true;

                    if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), true))
                    {
                        if(!blockAtPoint.getBlock().equals(Blocks.AIR) && blockAtPoint.getDestroySpeed(level,currentPoint)>=0)
                        {
                            if(passesFilter(pedestal, blockAtPoint, currentPoint) && (!ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_break"))).stream().toList().contains(blockAtPoint.getBlock())))
                            {
                                if(canMine(pedestal,blockAtPoint,currentPoint))
                                {

                                    if(ForgeEventFactory.doPlayerHarvestCheck((getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()), blockAtPoint, true)) {
                                        BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(level, currentPoint, blockAtPoint, (getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()));
                                        if (!MinecraftForge.EVENT_BUS.post(e)) {
                                            boolean damage = false;
                                            if(!currentPoint.equals(pedestal.getPos()))
                                            {
                                                if(PedestalConfig.COMMON.blockBreakerDamageTools.get())
                                                {
                                                    if(pedestal.hasTool())
                                                    {
                                                        BlockPos pedestalPos = pedestal.getPos();
                                                        if(pedestal.getDurabilityRemainingOnInsertedTool()>0)
                                                        {
                                                            if(pedestal.damageInsertedTool(1,true))
                                                            {
                                                                damage = true;
                                                            }
                                                            else
                                                            {
                                                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                                                                return;
                                                            }
                                                        }
                                                        else
                                                        {
                                                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                                                            return;
                                                        }
                                                    }
                                                }

                                                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), false))
                                                {
                                                    boolean canRemoveBlockEntities = PedestalConfig.COMMON.blockBreakerBreakEntities.get();
                                                    List<ItemStack> drops = getBlockDrops(level, pedestal, getPlayer.get(), blockAtPoint);
                                                    if(level.getBlockEntity(currentPoint) !=null){
                                                        if(canRemoveBlockEntities)
                                                        {
                                                            //Making sure not to delete the old code until i know for a fact its working
                                                            IntegerProperty propInt = getBlockPropertyAge(blockAtPoint);
                                                            int min = (propInt == null)?(0):(Collections.min(propInt.getPossibleValues()));
                                                            updateHarvestedBlock(level, pedestal, blockAtPoint, currentPoint, hasGentle);                                                            dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                            if(damage)pedestal.damageInsertedTool(1,false);
                                                            if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),255,246,0));

                                                            /*
                                                            if(hasGentleHarvest(pedestal.getCoinOnPedestal()))
                                                            {
                                                                IntegerProperty propInt = getBlockPropertyAge(blockAtPoint);
                                                                if(propInt !=null)
                                                                {
                                                                    int min = Collections.min(propInt.getPossibleValues());
                                                                    dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                                    level.setBlockAndUpdate(currentPoint, blockAtPoint.setValue(propInt,min));
                                                                    if(damage)pedestal.damageInsertedTool(1,false);
                                                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),255,246,0));
                                                                }
                                                                else
                                                                {
                                                                    blockAtPoint.onRemove(level,currentPoint,blockAtPoint,true);
                                                                    dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                                    level.removeBlockEntity(currentPoint);
                                                                    level.setBlockAndUpdate(currentPoint, Blocks.AIR.defaultBlockState());
                                                                    if(damage)pedestal.damageInsertedTool(1,false);
                                                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),255,246,0));
                                                                }

                                                            }
                                                            else
                                                            {
                                                                blockAtPoint.onRemove(level,currentPoint,blockAtPoint,true);
                                                                dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                                level.removeBlockEntity(currentPoint);
                                                                //level.removeBlock(adjustedPoint, true);
                                                                level.setBlockAndUpdate(currentPoint, Blocks.AIR.defaultBlockState());
                                                                //level.playLocalSound(currentPoint.getX(), currentPoint.getY(), currentPoint.getZ(), blockAtPoint.getSoundType().getBreakSound(), SoundSource.BLOCKS,1.0F,1.0F,true);
                                                                if(damage)pedestal.damageInsertedTool(1,false);
                                                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),255,246,0));
                                                            }
                                                            */
                                                        }
                                                    }
                                                    else
                                                    {
                                                        IntegerProperty propInt = getBlockPropertyAge(blockAtPoint);
                                                        int min = (propInt == null)?(0):(Collections.min(propInt.getPossibleValues()));
                                                        updateHarvestedBlock(level, pedestal, blockAtPoint, currentPoint, hasGentle);                                                        dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                        if(damage)pedestal.damageInsertedTool(1,false);
                                                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),255,246,0));
                                                    }

                                                    if(drops.size()>0)
                                                    {
                                                        for (ItemStack stack: drops) {
                                                            if(hasGentleHarvest(pedestal.getCoinOnPedestal()))
                                                            {
                                                                if(blockAtPoint.getBlock() instanceof CropBlock cropBlock)
                                                                {
                                                                    ItemStack seedStack = cropBlock.getCloneItemStack(level,currentPoint,blockAtPoint);
                                                                    if(doItemsMatch(seedStack,stack))
                                                                    {
                                                                        stack.shrink(1);
                                                                        MowLibItemUtils.spawnItemStack(level,currentPoint.getX(),currentPoint.getY(),currentPoint.getZ(),stack);
                                                                    }
                                                                    else
                                                                    {
                                                                        MowLibItemUtils.spawnItemStack(level,currentPoint.getX(),currentPoint.getY(),currentPoint.getZ(),stack);
                                                                    }
                                                                }
                                                                else
                                                                {
                                                                    MowLibItemUtils.spawnItemStack(level,currentPoint.getX(),currentPoint.getY(),currentPoint.getZ(),stack);
                                                                }
                                                            }
                                                            else
                                                            {
                                                                MowLibItemUtils.spawnItemStack(level,currentPoint.getX(),currentPoint.getY(),currentPoint.getZ(),stack);
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
                                }
                            }
                        }

                        //System.out.println("CurrentPoint: "+ currentPosition);
                        //System.out.println("ListSize: "+ listed.size());
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
    }
}
