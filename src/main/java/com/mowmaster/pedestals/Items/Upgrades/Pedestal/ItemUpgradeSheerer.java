package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.Items.Filters.IItemMode;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

public class ItemUpgradeSheerer extends ItemUpgradeBase implements ISelectableArea
{
    public ItemUpgradeSheerer(Properties p_41383_) {
        super(new Properties());
    }

    //Requires energy
    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_sheerer_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_sheerer_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_sheerer_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_sheerer_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_sheerer_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_sheerer_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_sheerer_dustColor.get(),PedestalConfig.COMMON.upgrade_sheerer_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_sheerer_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_sheerer_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_sheerer_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_sheerer_selectedMultiplier.get(); }

    @Override
    public ItemStack getUpgradeDefaultTool() {
        return new ItemStack(Items.SHEARS);
    }

    @Override
    public void updateAction(Level world, BasePedestalBlockEntity pedestal) {
        int configSpeed = PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get();
        int speed = configSpeed;
        if(pedestal.hasSpeed())speed = PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get() - pedestal.getTicksReduced();
        //Make sure speed has at least a value of 1
        if(speed<=0)speed = 1;
        if(world.getGameTime()%speed == 0 )
        {
            if(hasTwoPointsSelected(pedestal.getCoinOnPedestal()))upgradeAction(pedestal, world,pedestal.getPos(),pedestal.getCoinOnPedestal());

        }
    }

    public void upgradeAction(BasePedestalBlockEntity pedestal, Level level, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        boolean canRun = true;
        boolean damage = false;

        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),posOfPedestal), true))
        {
            WeakReference<FakePlayer> getPlayer = pedestal.fakePedestalPlayer(pedestal);
            AABB getArea = getAABBonUpgrade(coinInPedestal);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, getArea);
            ItemStack toolStack = (pedestal.hasItem())?(pedestal.getItemInPedestal()):(pedestal.getToolStack());

            if(PedestalConfig.COMMON.sheerer_DamageTools.get())
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
                            canRun = false;
                        }
                    }
                    else
                    {
                        if(pedestal.canSpawnParticles()) MowLibPacketHandler.sendToNearby(level,pedestalPos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,pedestalPos.getX(),pedestalPos.getY()+1.0f,pedestalPos.getZ(),255,255,255));
                        canRun = false;
                    }
                }
            }

            if(canRun)
            {
                for (LivingEntity getEntity : entities)
                {
                    if(getEntity == null)continue;

                    if(getEntity instanceof IForgeShearable shearMe)
                    {
                        BlockPos posSheerMe = getEntity.getOnPos();
                        if(shearMe.isShearable(toolStack.copy(),level,posSheerMe))
                        {
                            if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),posSheerMe), false))
                            {
                                int fortune = (EnchantmentHelper.getEnchantments(toolStack).containsKey(Enchantments.BLOCK_FORTUNE))?(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,toolStack)):(0);
                                List<ItemStack> getReturns = shearMe.onSheared(getPlayer.get(),toolStack,level,posSheerMe,fortune);

                                if(getReturns.size()>0)
                                {
                                    for (ItemStack itemstack : getReturns) {
                                        if(!itemstack.isEmpty()) MowLibItemUtils.spawnItemStack(level,posSheerMe.getX(),posSheerMe.getY(),posSheerMe.getZ(),itemstack);
                                    }
                                    if(damage)
                                    {
                                        if(toolStack.getItem().isDamageable(toolStack) && toolStack.getMaxStackSize()<=1)
                                        {
                                            pedestal.damageTool(toolStack,1,false);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
