package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.ISelectableArea;
import com.mowmaster.pedestals.Items.WorkCards.WorkCardBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.util.FakePlayer;

import java.lang.ref.WeakReference;
import java.util.List;

public class ItemUpgradeSheerer extends ItemUpgradeBase
{
    public ItemUpgradeSheerer(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    @Override
    public boolean canModifySuperSpeed(ItemStack upgradeItemStack) {
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
    public int getWorkCardType() { return 1; }

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
        return new ItemStack(Items.SHEARS);
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {
        if(pedestal.hasWorkCard())
        {
            ItemStack card = pedestal.getWorkCardInPedestal();
            if(card.getItem() instanceof WorkCardBase workCardBase)
            {
                if(workCardBase.hasTwoPointsSelected(card) && workCardBase.selectedAreaWithinRange(pedestal))
                {
                    boolean canRun = true;
                    boolean damage = false;

                    if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),pedestalPos), true))
                    {
                        WeakReference<FakePlayer> getPlayer = pedestal.getPedestalPlayer(pedestal);
                        if(getPlayer != null && getPlayer.get() != null)
                        {
                            AABB getArea = workCardBase.getAABBonUpgrade(card);
                            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, getArea);
                            ItemStack toolStack = (pedestal.hasItem())?(pedestal.getItemInPedestal()):(pedestal.getToolStack());

                            if(PedestalConfig.COMMON.sheerer_DamageTools.get())
                            {
                                if(pedestal.hasTool())
                                {
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
                                                List<ItemStack> getReturns = shearMe.onSheared((getPlayer.get() == null)?(pedestal.getPedestalPlayer(pedestal).get()):(getPlayer.get()),toolStack,level,posSheerMe,fortune);

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
                                                    if(!hasAdvancedOne(coin))break;
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
