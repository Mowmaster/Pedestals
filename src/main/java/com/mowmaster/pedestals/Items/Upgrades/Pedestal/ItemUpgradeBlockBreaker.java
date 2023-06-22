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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeBlockBreaker extends ItemUpgradeBase
{
    public ItemUpgradeBlockBreaker(Properties p_41383_) {
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
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_blockbreaker_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_blockbreaker_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_blockbreaker_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_blockbreaker_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_blockbreaker_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_blockbreaker_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_blockbreaker_dustColor.get(),PedestalConfig.COMMON.upgrade_blockbreaker_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_blockbreaker_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_blockbreaker_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_blockbreaker_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_blockbreaker_selectedMultiplier.get(); }

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
        return new ItemStack(Items.STONE_PICKAXE);
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        super.actionOnRemovedFromPedestal(pedestal, coinInPedestal);
        resetCachedValidWorkCardPositions(coinInPedestal);
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numposition");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numdelay");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_numheight");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_boolstop");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        List<BlockPos> allPositions = getValidWorkCardPositions(pedestal);
        if (allPositions.isEmpty()) return;

        breakerAction(level, pedestal, allPositions);
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

    private void dropXP(Level level, BasePedestalBlockEntity pedestal, BlockState blockAtPoint, BlockPos currentPoint)
    {
        int fortune = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.BLOCK_FORTUNE))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,pedestal.getToolStack())):(0);
        int silky = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.SILK_TOUCH))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,pedestal.getToolStack())):(0);
        int xpdrop = blockAtPoint.getBlock().getExpDrop(blockAtPoint,level, level.random,currentPoint,fortune,silky);
        if(xpdrop>0)blockAtPoint.getBlock().popExperience((ServerLevel)level,currentPoint,xpdrop);
    }

    public void breakerAction(Level level, BasePedestalBlockEntity pedestal, List<BlockPos> listed)
    {
        if(!level.isClientSide())
        {
            WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
            if(getPlayer != null && getPlayer.get() != null)
            {
                int currentPosition = getCurrentPosition(pedestal);
                BlockPos currentPoint = listed.get(currentPosition);
                BlockState blockAtPoint = level.getBlockState(currentPoint);

                boolean fuelRemoved = true;

                if(!blockAtPoint.getBlock().equals(Blocks.AIR) && blockAtPoint.getDestroySpeed(level,currentPoint)>=0)
                {
                    if(passesFilter(pedestal, blockAtPoint, currentPoint) && (!ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_break"))).stream().toList().contains(blockAtPoint.getBlock())))
                    {
                        if(ForgeEventFactory.doPlayerHarvestCheck((getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()), blockAtPoint, true)) {
                            BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(level, currentPoint, blockAtPoint, (getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()));
                            if (!MinecraftForge.EVENT_BUS.post(e)) {
                                boolean damage = false;
                                if(!currentPoint.equals(pedestal.getPos()))
                                {
                                    if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), true))
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
                                                    blockAtPoint.onRemove(level,currentPoint,blockAtPoint,true);
                                                    dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                    level.removeBlockEntity(currentPoint);
                                                    //level.removeBlock(adjustedPoint, true);
                                                    level.setBlockAndUpdate(currentPoint, Blocks.AIR.defaultBlockState());
                                                    //level.playLocalSound(currentPoint.getX(), currentPoint.getY(), currentPoint.getZ(), blockAtPoint.getSoundType().getBreakSound(), SoundSource.BLOCKS,1.0F,1.0F,true);
                                                    if(damage)pedestal.damageInsertedTool(1,false);

                                                    if(drops.size()>0)
                                                    {
                                                        for (ItemStack stack: drops) {
                                                            MowLibItemUtils.spawnItemStack(level,currentPoint.getX(),currentPoint.getY(),currentPoint.getZ(),stack);
                                                        }
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                dropXP(level, pedestal, blockAtPoint, currentPoint);
                                                level.setBlockAndUpdate(currentPoint, Blocks.AIR.defaultBlockState());
                                                //level.playLocalSound(currentPoint.getX(), currentPoint.getY(), currentPoint.getZ(), blockAtPoint.getSoundType().getBreakSound(), SoundSource.BLOCKS,1.0F,1.0F,true);
                                                if(damage)pedestal.damageInsertedTool(1,false);

                                                if(drops.size()>0)
                                                {
                                                    for (ItemStack stack: drops) {
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
