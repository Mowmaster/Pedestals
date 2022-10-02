package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.MechanicalOnlyStorage.BaseFluidBulkStorageItem;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.lang.ref.WeakReference;
import java.util.List;

public class ItemUpgradeMilker extends ItemUpgradeBase implements ISelectableArea
{
    public ItemUpgradeMilker(Properties p_41383_) {
        super(new Properties());
    }

    //Requires energy
    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_milker_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_milker_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_milker_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_milker_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_milker_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_milker_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_milker_dustColor.get(),PedestalConfig.COMMON.upgrade_milker_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_milker_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_milker_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_milker_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_milker_selectedMultiplier.get(); }

    @Override
    public ItemStack getUpgradeDefaultTool() {
        return new ItemStack(Items.BUCKET);
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {
        if(hasTwoPointsSelected(pedestal.getCoinOnPedestal()))
        {
            boolean canRun = true;
            //boolean damage = false;

            if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),pedestalPos), true))
            {
                WeakReference<FakePlayer> getPlayer = pedestal.fakePedestalPlayer(pedestal);
                AABB getArea = getAABBonUpgrade(coin);
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, getArea);
                ItemStack toolStack = (pedestal.hasItem())?(pedestal.getItemInPedestal()):(pedestal.getToolStack());
                getPlayer.get().setItemInHand(InteractionHand.MAIN_HAND,toolStack.copy());

            /*if(PedestalConfig.COMMON.milker_DamageTools.get())
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
            }*/

                if(canRun)
                {
                    for (LivingEntity getEntity : entities)
                    {
                        if(getEntity == null)continue;

                        BlockPos getEntityPos = getEntity.getOnPos();
                        if(getEntity instanceof Animal animal)
                        {
                            InteractionResult result = animal.mobInteract(getPlayer.get(), InteractionHand.MAIN_HAND);
                            if(result == InteractionResult.CONSUME)
                            {
                                NonNullList<ItemStack> getItemsInPlayer = getPlayer.get().getInventory().items;
                                for(int i=0;i<getItemsInPlayer.size();i++)
                                {
                                    ItemStack stackInPlayer = getItemsInPlayer.get(i);
                                    //System.out.println(stackInPlayer.getItem());
                                    if(!stackInPlayer.isEmpty() && !toolStack.getItem().equals(stackInPlayer.getItem()))
                                    {
                                        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),getEntityPos), false))
                                        {
                                            //System.out.println(stackInPlayer.getItem() instanceof BucketItem);
                                            if(stackInPlayer.getItem() instanceof BucketItem bucket)
                                            {

                                                //System.out.println(bucket.getFluid());
                                                if(!bucket.getFluid().equals(Fluids.EMPTY))
                                                {
                                                    FluidStack getFluid = new FluidStack(bucket.getFluid(),FluidType.BUCKET_VOLUME);
                                                    if(pedestal.addFluid(getFluid, IFluidHandler.FluidAction.SIMULATE)>0)
                                                    {
                                                        pedestal.addFluid(getFluid, IFluidHandler.FluidAction.EXECUTE);
                                                        break;
                                                    }
                                                }
                                            }
                                            else
                                            {

                                                //System.out.println(pedestal.getItemInPedestal().getItem());
                                                if(!pedestal.getItemInPedestal().isEmpty())
                                                {
                                                    MowLibItemUtils.spawnItemStack(level,getEntityPos.getX(),getEntityPos.getY(),getEntityPos.getZ(),stackInPlayer);
                                                    pedestal.removeItem(1,false);
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
            }
        }
    }
}
