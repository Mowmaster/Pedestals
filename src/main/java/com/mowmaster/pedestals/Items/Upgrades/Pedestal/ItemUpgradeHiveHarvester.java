package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.NonNullList;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
import java.util.Map;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;
import static net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL;

public class ItemUpgradeHiveHarvester extends ItemUpgradeBase implements ISelectablePoints, ISelectableArea
{
    public ItemUpgradeHiveHarvester(Properties p_41383_) {
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

    //Requires energy
    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_hiveharvester_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_hiveharvester_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_hiveharvester_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_hiveharvester_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_hiveharvester_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_hiveharvester_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_hiveharvester_dustColor.get(),PedestalConfig.COMMON.upgrade_hiveharvester_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_hiveharvester_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_hiveharvester_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_hiveharvester_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_hiveharvester_selectedMultiplier.get(); }

    @Override
    public ItemStack getUpgradeDefaultTool() {
        return new ItemStack(Items.SHEARS);
    }

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
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        boolean override = hasTwoPointsSelected(coin);
        List<BlockPos> listed = getValidList(pedestal);

        if(override)
        {
            if(listed.size()>0)
            {
                //System.out.println("RunAction");
                harvesterAction(level,pedestal);
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
                else if(!pedestal.getRenderRange())
                {
                    pedestal.setRenderRange(true);
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
        if (canMineBlock.is(BlockTags.BEEHIVES, (p_202454_) -> {
            return p_202454_.hasProperty(HONEY_LEVEL) && p_202454_.getBlock() instanceof BeehiveBlock;
        })) {
            int i = canMineBlock.getValue(HONEY_LEVEL);
            if (i >= 5) {
                return true;
            }
        }

        return false;
    }

    public void harvesterAction(Level level, BasePedestalBlockEntity pedestal)
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
                                                BlockSourceImpl p_123444_ = new BlockSourceImpl((ServerLevel)level, currentPoint);
                                                if (blockAtPoint.is(BlockTags.BEEHIVES, (p_123442_) -> {
                                                    return p_123442_.hasProperty(HONEY_LEVEL) && p_123442_.getBlock() instanceof BeehiveBlock;
                                                }) && blockAtPoint.getValue(HONEY_LEVEL) >= 5) {
                                                    ItemStack toolStack = (pedestal.hasItem())?(pedestal.getItemInPedestal()):(pedestal.getToolStack());
                                                    getPlayer.get().setItemInHand(InteractionHand.MAIN_HAND,toolStack);
                                                    UseOnContext blockContext = new UseOnContext(level,getPlayer.get(), InteractionHand.MAIN_HAND, toolStack.copy(), new BlockHitResult(Vec3.ZERO, getPedestalFacing(level,pedestal.getPos()), currentPoint, false));
                                                    BlockHitResult resulted = new BlockHitResult(blockContext.getClickLocation(),blockContext.getClickedFace(),blockContext.getClickedPos(),blockContext.isInside());
                                                    if(resulted.getType() == HitResult.Type.BLOCK)
                                                    {
                                                        ((BeehiveBlock)blockAtPoint.getBlock()).use(blockAtPoint,level,currentPoint, getPlayer.get(), InteractionHand.MAIN_HAND,resulted);
                                                        NonNullList<ItemStack> getItemsInPlayer = getPlayer.get().getInventory().items;
                                                        for(int i=0;i<getItemsInPlayer.size();i++)
                                                        {
                                                            ItemStack stackInPlayer = getItemsInPlayer.get(i);
                                                            if(!stackInPlayer.isEmpty() && !toolStack.getItem().equals(stackInPlayer.getItem()))
                                                            {
                                                                MowLibItemUtils.spawnItemStack(level,currentPoint.getX(),currentPoint.getY(),currentPoint.getZ(),stackInPlayer);
                                                                if(toolStack.getItem().isDamageable(toolStack) && toolStack.getMaxStackSize()<=1)
                                                                {
                                                                    if(damage)pedestal.damageTool(toolStack,1,false);
                                                                }
                                                                else
                                                                {
                                                                    pedestal.removeItem(1,false);
                                                                }
                                                            }
                                                        }
                                                        if(damage)pedestal.damageInsertedTool(1,false);
                                                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestal.getPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,currentPoint.getX(),currentPoint.getY()+1.0f,currentPoint.getZ(),255,246,0));
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