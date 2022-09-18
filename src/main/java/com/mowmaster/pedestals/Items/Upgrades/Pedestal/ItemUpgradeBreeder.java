package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.lang.ref.WeakReference;
import java.util.List;

public class ItemUpgradeBreeder extends ItemUpgradeBase implements ISelectableArea
{
    public ItemUpgradeBreeder(Properties p_41383_) {
        super(new Properties());
    }

    //Requires energy
    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_breeder_baseEnergyCost.get(); }
    @Override
    public boolean energyDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_breeder_energy_distance_multiplier.get();}
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_breeder_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_breeder_baseXpCost.get(); }
    @Override
    public boolean xpDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_breeder_xp_distance_multiplier.get();}
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_breeder_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_breeder_dustColor.get(),PedestalConfig.COMMON.upgrade_breeder_baseDustAmount.get()); }
    @Override
    public boolean dustDistanceAsModifier() {return PedestalConfig.COMMON.upgrade_breeder_dust_distance_multiplier.get();}
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_breeder_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_breeder_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_breeder_selectedMultiplier.get(); }

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
        //boolean damage = false;

        if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),posOfPedestal), true))
        {
            WeakReference<FakePlayer> getPlayer = pedestal.fakePedestalPlayer(pedestal);
            AABB getArea = getAABBonUpgrade(coinInPedestal);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, getArea);
            ItemStack toolStack = (pedestal.hasItem())?(pedestal.getItemInPedestal()):(pedestal.getToolStack());
            getPlayer.get().setItemInHand(InteractionHand.MAIN_HAND,toolStack.copy());

            /*if(PedestalConfig.COMMON.breeder_DamageTools.get())
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
                        if(animal.isFood(toolStack))
                        {
                            if(animal.getAge() == 0 && animal.canFallInLove())
                            {
                                InteractionResult result = animal.mobInteract(getPlayer.get(), InteractionHand.MAIN_HAND);
                                //System.out.println(result.toString());
                                if(result == InteractionResult.SUCCESS)
                                {
                                    if(removeFuelForAction(pedestal, getDistanceBetweenPoints(pedestal.getPos(),getEntityPos), false))
                                    {
                                        pedestal.removeItem(1,false);
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
