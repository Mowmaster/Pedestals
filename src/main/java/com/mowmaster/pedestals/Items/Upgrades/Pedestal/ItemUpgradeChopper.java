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
import com.mowmaster.pedestals.Items.WorkCards.WorkCardBase;
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
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeChopper extends ItemUpgradeBase
{
    public ItemUpgradeChopper(Properties p_41383_) {
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
    public boolean needsWorkCard() { return true; }

    @Override
    public int getWorkCardType() { return 1; }

    //Requires energy

    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_chopper_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_chopper_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_chopper_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_chopper_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_chopper_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_chopper_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_chopper_dustColor.get(),PedestalConfig.COMMON.upgrade_chopper_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_chopper_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_chopper_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_chopper_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_chopper_selectedMultiplier.get(); }

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
        return new ItemStack(Items.IRON_AXE);
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
                for(int i=maxX;i>=minX;i--)
                {
                    for(int j=maxZ;j>=minZ;j--)
                    {
                        BlockPos newPoint = new BlockPos(i,pedestalPos.getY(),j);
                        //System.out.println("points: "+ newPoint);
                        if(workCardBase.selectedPointWithinRange(pedestal, newPoint))
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
                        chopperAction(level,pedestal);
                    }
                    else if(workCardBase.selectedAreaWithinRange(pedestal) && !hasBlockListCustomNBTTags(coin,"_validlist"))
                    {
                        buildValidBlockListArea(pedestal);
                    }
                }
            }
        }
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
        int current = getCurrentPosition(pedestal);
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
        ItemStack getToolFromPedestal = (pedestal.getToolStack().isEmpty())?(getUpgradeDefaultTool()):(pedestal.getToolStack());

        Level level = pedestal.getLevel();
        if(blockTarget.getBlock() != Blocks.AIR)
        {
            WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
            if(getPlayer != null && getPlayer.get() != null)
            {
                LootContext.Builder builder = new LootContext.Builder((ServerLevel) level)
                        .withRandom(level.random)
                        .withParameter(LootContextParams.ORIGIN, new Vec3(pedestal.getPos().getX(),pedestal.getPos().getY(),pedestal.getPos().getZ()))
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, getPlayer.get())
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

    //This is for Chopper, so we'll test to make sure the blocks arnt part of this
    private boolean canMine(BasePedestalBlockEntity pedestal, BlockState canMineBlock, BlockPos canMinePos)
    {
        return ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation("pedestals:pedestals_can_chop_cant_quarry"))).stream().toList().contains(canMineBlock.getBlock());
    }

    private void dropXP(Level level, BasePedestalBlockEntity pedestal, BlockState blockAtPoint, BlockPos currentPoint)
    {
        int fortune = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.BLOCK_FORTUNE))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,pedestal.getToolStack())):(0);
        int silky = (EnchantmentHelper.getEnchantments(pedestal.getToolStack()).containsKey(Enchantments.SILK_TOUCH))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,pedestal.getToolStack())):(0);
        int xpdrop = blockAtPoint.getBlock().getExpDrop(blockAtPoint,level, level.random,currentPoint,fortune,silky);
        if(xpdrop>0)blockAtPoint.getBlock().popExperience((ServerLevel)level,currentPoint,xpdrop);
    }

    public void chopperAction(Level level, BasePedestalBlockEntity pedestal)
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
                        if(hasSuperSpeed(pedestal.getCoinOnPedestal()))
                        {
                            List<BlockPos> listed = getValidList(pedestal);
                            AABB area = new AABB(workCardBase.readBlockPosFromNBT(card,1),workCardBase.readBlockPosFromNBT(card,2));
                            int maxY = (int)area.maxY;
                            int minY = (int)area.minY;
                            for(BlockPos currentPoint:listed)
                            {
                                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), true))
                                {
                                    for(int y=minY;y<=maxY;y++)
                                    {
                                        BlockPos adjustedPoint = new BlockPos(currentPoint.getX(),y,currentPoint.getZ());
                                        BlockState blockAtPoint = level.getBlockState(adjustedPoint);
                                        //blockAtPoint.requiresCorrectToolForDrops();

                                        if(!blockAtPoint.getBlock().equals(Blocks.AIR) && blockAtPoint.getDestroySpeed(level,currentPoint)>=0)
                                        {
                                            if(ForgeEventFactory.doPlayerHarvestCheck((getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()), blockAtPoint, true)) {
                                                BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(level, adjustedPoint, blockAtPoint, (getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()));
                                                if (!MinecraftForge.EVENT_BUS.post(e)) {
                                                    if(canMine(pedestal, blockAtPoint, adjustedPoint))
                                                    {
                                                        if(passesFilter(pedestal, blockAtPoint, adjustedPoint) && (!ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_break"))).stream().toList().contains(blockAtPoint.getBlock())))
                                                        {
                                                            //ToDo: config option

                                                            boolean damage = false;

                                                            if(!adjustedPoint.equals(pedestal.getPos()))
                                                            {
                                                                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), true))
                                                                {
                                                                    if(PedestalConfig.COMMON.chopperDamageTools.get())
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

                                                                    if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), false))
                                                                    {
                                                                        boolean canRemoveBlockEntities = PedestalConfig.COMMON.blockBreakerBreakEntities.get();
                                                                        List<ItemStack> drops = getBlockDrops(pedestal, blockAtPoint, adjustedPoint);
                                                                        if(level.getBlockEntity(adjustedPoint) !=null){
                                                                            if(canRemoveBlockEntities)
                                                                            {
                                                                                blockAtPoint.onRemove(level,adjustedPoint,blockAtPoint,true);
                                                                                dropXP(level, pedestal, blockAtPoint, adjustedPoint);
                                                                                level.removeBlockEntity(adjustedPoint);
                                                                                //level.removeBlock(adjustedPoint, true);
                                                                                level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                                                                //level.playLocalSound(currentPoint.getX(), currentPoint.getY(), currentPoint.getZ(), blockAtPoint.getSoundType().getBreakSound(), SoundSource.BLOCKS,1.0F,1.0F,true);
                                                                                if(damage)pedestal.damageInsertedTool(1,false);

                                                                                if(drops.size()>0)
                                                                                {
                                                                                    for (ItemStack stack: drops) {
                                                                                        MowLibItemUtils.spawnItemStack(level,adjustedPoint.getX(),adjustedPoint.getY(),adjustedPoint.getZ(),stack);
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                        else
                                                                        {
                                                                            dropXP(level, pedestal, blockAtPoint, adjustedPoint);
                                                                            level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                                                            //level.playLocalSound(currentPoint.getX(), currentPoint.getY(), currentPoint.getZ(), blockAtPoint.getSoundType().getBreakSound(), SoundSource.BLOCKS,1.0F,1.0F,true);
                                                                            if(damage)pedestal.damageInsertedTool(1,false);

                                                                            if(drops.size()>0)
                                                                            {
                                                                                for (ItemStack stack: drops) {
                                                                                    MowLibItemUtils.spawnItemStack(level,adjustedPoint.getX(),adjustedPoint.getY(),adjustedPoint.getZ(),stack);
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
                            }
                        }
                        else
                        {
                            List<BlockPos> listed = getValidList(pedestal);
                            int currentPosition = getCurrentPosition(pedestal);
                            BlockPos currentPoint = listed.get(currentPosition);
                            AABB area = new AABB(workCardBase.readBlockPosFromNBT(card,1),workCardBase.readBlockPosFromNBT(card,2));
                            int maxY = (int)area.maxY;
                            int minY = (int)area.minY;
                            boolean fuelRemoved = true;



                            //ToDo: make this a modifier for later
                            boolean runsOnce = true;
                            boolean stop = getStopped(pedestal);

                            if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),currentPoint), true))
                            {
                                if(!stop)
                                {
                                    for(int y=minY;y<=maxY;y++)
                                    {
                                        BlockPos adjustedPoint = new BlockPos(currentPoint.getX(),y,currentPoint.getZ());
                                        BlockState blockAtPoint = level.getBlockState(adjustedPoint);
                                        //blockAtPoint.requiresCorrectToolForDrops();

                                        if(!blockAtPoint.getBlock().equals(Blocks.AIR) && blockAtPoint.getDestroySpeed(level,currentPoint)>=0)
                                        {
                                            if(ForgeEventFactory.doPlayerHarvestCheck((getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()), blockAtPoint, true)) {
                                                BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(level, adjustedPoint, blockAtPoint, (getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()));
                                                if (!MinecraftForge.EVENT_BUS.post(e)) {
                                                    if(canMine(pedestal, blockAtPoint, adjustedPoint))
                                                    {
                                                        if(passesFilter(pedestal, blockAtPoint, adjustedPoint) && (!ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation(MODID, "pedestals_cannot_break"))).stream().toList().contains(blockAtPoint.getBlock())))
                                                        {
                                                            //ToDo: config option

                                                            boolean damage = false;

                                                            if(!adjustedPoint.equals(pedestal.getPos()))
                                                            {
                                                                if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), true))
                                                                {
                                                                    if(PedestalConfig.COMMON.chopperDamageTools.get())
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

                                                                    if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),adjustedPoint), false))
                                                                    {
                                                                        boolean canRemoveBlockEntities = PedestalConfig.COMMON.blockBreakerBreakEntities.get();
                                                                        List<ItemStack> drops = getBlockDrops(pedestal, blockAtPoint, adjustedPoint);
                                                                        if(level.getBlockEntity(adjustedPoint) !=null){
                                                                            if(canRemoveBlockEntities)
                                                                            {
                                                                                blockAtPoint.onRemove(level,adjustedPoint,blockAtPoint,true);
                                                                                dropXP(level, pedestal, blockAtPoint, adjustedPoint);
                                                                                level.removeBlockEntity(adjustedPoint);
                                                                                //level.removeBlock(adjustedPoint, true);
                                                                                level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                                                                //level.playLocalSound(currentPoint.getX(), currentPoint.getY(), currentPoint.getZ(), blockAtPoint.getSoundType().getBreakSound(), SoundSource.BLOCKS,1.0F,1.0F,true);
                                                                                if(damage)pedestal.damageInsertedTool(1,false);

                                                                                if(drops.size()>0)
                                                                                {
                                                                                    for (ItemStack stack: drops) {
                                                                                        MowLibItemUtils.spawnItemStack(level,adjustedPoint.getX(),adjustedPoint.getY(),adjustedPoint.getZ(),stack);
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                        else
                                                                        {
                                                                            dropXP(level, pedestal, blockAtPoint, adjustedPoint);
                                                                            level.setBlockAndUpdate(adjustedPoint, Blocks.AIR.defaultBlockState());
                                                                            //level.playLocalSound(currentPoint.getX(), currentPoint.getY(), currentPoint.getZ(), blockAtPoint.getSoundType().getBreakSound(), SoundSource.BLOCKS,1.0F,1.0F,true);
                                                                            if(damage)pedestal.damageInsertedTool(1,false);

                                                                            if(drops.size()>0)
                                                                            {
                                                                                for (ItemStack stack: drops) {
                                                                                    MowLibItemUtils.spawnItemStack(level,adjustedPoint.getX(),adjustedPoint.getY(),adjustedPoint.getZ(),stack);
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
                                        }
                                    }
                                }
                                else
                                {
                                    if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestal.getPos().getX(),pedestal.getPos().getY()+1.0f,pedestal.getPos().getZ(),55,55,55));
                                }

                                if((currentPosition+1)>=listed.size())
                                {
                                    if(runsOnce)
                                    {
                                        //ToDo: Make this 1200 value a config
                                        int delay = Math.floorDiv((listed.size() * Math.abs((maxY-minY))),20);
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
}
