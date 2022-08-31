package com.mowmaster.pedestals.Items.Upgrades.Pedestal.InProgressPorting;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibXpUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ISelectableArea;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ISelectablePoints;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class ItemUpgradeBlockBreaker extends ItemUpgradeBase implements ISelectablePoints, ISelectableArea
{
    public ItemUpgradeBlockBreaker(Properties p_41383_) {
        super(new Properties());
    }

    //Requires energy

    @Override
    public int baseEnergyCostPerDistance(){ return PedestalConfig.COMMON.upgrade_magnet_baseEnergyCost.get(); }
    @Override
    public double energyCostMultiplier(){ return PedestalConfig.COMMON.upgrade_magnet_energyMultiplier.get(); }

    @Override
    public int baseXpCostPerDistance(){ return PedestalConfig.COMMON.upgrade_magnet_baseXpCost.get(); }
    @Override
    public double xpCostMultiplier(){ return PedestalConfig.COMMON.upgrade_magnet_xpMultiplier.get(); }

    @Override
    public DustMagic baseDustCostPerDistance(){ return new DustMagic(PedestalConfig.COMMON.upgrade_magnet_dustColor.get(),PedestalConfig.COMMON.upgrade_magnet_baseDustAmount.get()); }
    @Override
    public double dustCostMultiplier(){ return PedestalConfig.COMMON.upgrade_magnet_dustMultiplier.get(); }

    @Override
    public boolean hasSelectedAreaModifier() { return PedestalConfig.COMMON.upgrade_magnet_selectedAllowed.get(); }
    @Override
    public double selectedAreaCostMultiplier(){ return PedestalConfig.COMMON.upgrade_magnet_selectedMultiplier.get(); }



    @Override
    public void updateAction(Level world, BasePedestalBlockEntity pedestal) {

        ItemStack coin = pedestal.getCoinOnPedestal();
        boolean override = hasTwoPointsSelected(coin);
        List<BlockPos> listed = readBlockPosListFromNBT(coin);

        if(override)
        {
            if(selectedAreaWithinRange(pedestal))
            {
                upgradeActionArea(pedestal, world,pedestal.getPos(),pedestal.getCoinOnPedestal());
            }
            else if(pedestal.getRenderRange() == false)
            {
                pedestal.setRenderRange(true);
            }
        }
        else
        {
            if(!override && listed.size()>0)
            {
                upgradeAction(pedestal, world,pedestal.getPos(),pedestal.getCoinOnPedestal());
            }
        }
    }

    public void upgradeActionArea(BasePedestalBlockEntity pedestal, Level world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        boolean needsEnergy = requiresFuelForUpgradeAction();
        boolean actionDone = false;
        //if(needsEnergy) { if(!removeFuelForAction(pedestal,getDistanceBetweenPoints(posOfPedestal,item.getOnPos()),true))break; }
    }

    public void upgradeAction(BasePedestalBlockEntity pedestal, Level world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        boolean needsEnergy = requiresFuelForUpgradeAction();
        boolean actionDone = false;
        //if(needsEnergy) { if(!removeFuelForAction(pedestal,getDistanceBetweenPoints(posOfPedestal,item.getOnPos()),true))break; }
    }
}
