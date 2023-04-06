package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
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
import net.minecraft.world.phys.AABB;
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
import java.util.stream.IntStream;

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
                        harvesterAction(level,pedestal);
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
                        harvesterAction(level,pedestal);
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
        ItemStack getToolFromPedestal = (pedestal.getToolStack().isEmpty())?(new ItemStack(Items.STONE_PICKAXE)):(pedestal.getToolStack());
        Level level = pedestal.getLevel();
        if(blockTarget.getBlock() != Blocks.AIR)
        {
            WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
            if(getPlayer != null && getPlayer.get() != null)
            {
                LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                        .withRandom(level.random)
                        .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                        .withParameter(LootContextParams.THIS_ENTITY, getPlayer.get())
                        .withParameter(LootContextParams.TOOL, getToolFromPedestal);

                return blockTarget.getDrops(builder);
                //return blockTarget.getBlock().getDrops(blockTarget,builder);
            }
            else
            {
                LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                        .withRandom(level.random)
                        .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                        .withParameter(LootContextParams.TOOL, getToolFromPedestal);

                return blockTarget.getDrops(builder);
                //return blockTarget.getBlock().getDrops(blockTarget,builder);
            }
        }

        /*if(blockTarget.requiresCorrectToolForDrops())
        {
            if(isToolHighEnoughLevelForBlock(getToolFromPedestal, blockTarget))
            {

            }
        }
        else
        {
            //Level level = pedestal.getLevel();
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

    private boolean passesFilter(BasePedestalBlockEntity pedestal, BlockState canMineBlock, BlockPos canMinePos)
    {
        if(pedestal.hasFilter())
        {
            ItemStack filterInPedestal = pedestal.getFilterInPedestal();
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

        if (canMineBlock.getBlock() instanceof KelpBlock)
        {
            returner = true;
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
            int current = canMineBlock.getValue(propInt);
            //int min = Collections.min(propInt.getPossibleValues());
            int max = Collections.max(propInt.getPossibleValues());
            if(current == max)
            {
                returner = true;
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

    public void harvesterAction(Level level, BasePedestalBlockEntity pedestal)
    {
        if(!level.isClientSide())
        {
            WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
            if(getPlayer != null && getPlayer.get() != null)
            {
                if(hasSuperSpeed(pedestal.getCoinOnPedestal()))
                {
                    List<BlockPos> listed = getValidList(pedestal);
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
                                                        List<ItemStack> drops = getBlockDrops(pedestal, blockAtPoint, currentPoint);
                                                        if(level.getBlockEntity(currentPoint) !=null){
                                                            if(canRemoveBlockEntities)
                                                            {
                                                                if(hasGentleHarvest(pedestal.getCoinOnPedestal()))
                                                                {
                                                                    IntegerProperty propInt = getBlockPropertyAge(blockAtPoint);
                                                                    int min = Collections.min(propInt.getPossibleValues());

                                                                    dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                                    level.setBlockAndUpdate(currentPoint, blockAtPoint.setValue(propInt,min));
                                                                    if(damage)pedestal.damageInsertedTool(1,false);
                                                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX()+0.5D,currentPoint.getY()+1.0f,currentPoint.getZ()+0.5D,255,246,0));
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
                                                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX()+0.5D,currentPoint.getY()+1.0f,currentPoint.getZ()+0.5D,255,246,0));
                                                                }
                                                            }
                                                        }
                                                        else
                                                        {
                                                            if(hasGentleHarvest(pedestal.getCoinOnPedestal()))
                                                            {
                                                                IntegerProperty propInt = getBlockPropertyAge(blockAtPoint);
                                                                int min = Collections.min(propInt.getPossibleValues());

                                                                dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                                level.setBlockAndUpdate(currentPoint, blockAtPoint.setValue(propInt,min));
                                                                if(damage)pedestal.damageInsertedTool(1,false);
                                                            }
                                                            else
                                                            {
                                                                dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                                level.setBlockAndUpdate(currentPoint, Blocks.AIR.defaultBlockState());
                                                                //level.playLocalSound(currentPoint.getX(), currentPoint.getY(), currentPoint.getZ(), blockAtPoint.getSoundType().getBreakSound(), SoundSource.BLOCKS,1.0F,1.0F,true);
                                                                if(damage)pedestal.damageInsertedTool(1,false);
                                                                //if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX()+0.5D,currentPoint.getY()+1.0f,currentPoint.getZ()+0.5D,255,246,0));
                                                            }
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
                    List<BlockPos> listed = getValidList(pedestal);
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
                                                    List<ItemStack> drops = getBlockDrops(pedestal, blockAtPoint, currentPoint);
                                                    if(level.getBlockEntity(currentPoint) !=null){
                                                        if(canRemoveBlockEntities)
                                                        {
                                                            if(hasGentleHarvest(pedestal.getCoinOnPedestal()))
                                                            {
                                                                IntegerProperty propInt = getBlockPropertyAge(blockAtPoint);
                                                                int min = Collections.min(propInt.getPossibleValues());

                                                                dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                                level.setBlockAndUpdate(currentPoint, blockAtPoint.setValue(propInt,min));
                                                                if(damage)pedestal.damageInsertedTool(1,false);
                                                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX()+0.5D,currentPoint.getY()+1.0f,currentPoint.getZ()+0.5D,255,246,0));
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
                                                                if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX()+0.5D,currentPoint.getY()+1.0f,currentPoint.getZ()+0.5D,255,246,0));
                                                            }
                                                        }
                                                    }
                                                    else
                                                    {
                                                        if(hasGentleHarvest(pedestal.getCoinOnPedestal()))
                                                        {
                                                            IntegerProperty propInt = getBlockPropertyAge(blockAtPoint);
                                                            int min = Collections.min(propInt.getPossibleValues());

                                                            dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                            level.setBlockAndUpdate(currentPoint, blockAtPoint.setValue(propInt,min));
                                                            if(damage)pedestal.damageInsertedTool(1,false);
                                                        }
                                                        else
                                                        {
                                                            dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                            level.setBlockAndUpdate(currentPoint, Blocks.AIR.defaultBlockState());
                                                            //level.playLocalSound(currentPoint.getX(), currentPoint.getY(), currentPoint.getZ(), blockAtPoint.getSoundType().getBreakSound(), SoundSource.BLOCKS,1.0F,1.0F,true);
                                                            if(damage)pedestal.damageInsertedTool(1,false);
                                                            //if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,currentPoint,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX()+0.5D,currentPoint.getY()+1.0f,currentPoint.getZ()+0.5D,255,246,0));
                                                        }
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
