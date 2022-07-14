package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ItemUpgradeMagnet extends ItemUpgradeBase implements IHasModeTypes, ISelectableArea
{
    public ItemUpgradeMagnet(Properties p_41383_) {
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

        if(hasTwoPointsSelected(pedestal.getCoinOnPedestal()))
        {
            if(selectedAreaWithinRange(pedestal))
            {
                upgradeAction(pedestal, world,pedestal.getPos(),pedestal.getCoinOnPedestal());
            }
            else
            {
                if(!pedestal.getRenderRange())
                {
                    pedestal.setRenderRange(true);
                }
            }
        }
    }

    public void upgradeAction(BasePedestalBlockEntity pedestal, Level world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        AABB aabb = getAABBonUpgrade(coinInPedestal);

        //Item
        if(canTransferItems(coinInPedestal))
        {
            List<ItemEntity> list = world.getEntitiesOfClass(ItemEntity.class, aabb);
            for (ItemEntity item : list)
            {

            }
        }
        //Fluid
        if(canTransferFluids(coinInPedestal))
        {
            //get only fluid containers and buckets and maybe custom fluid items
            List<ItemEntity> list = world.getEntitiesOfClass(ItemEntity.class, aabb);
            for (ItemEntity item : list)
            {

            }
        }
        //Energy
        if(canTransferEnergy(coinInPedestal))
        {
            //get only energy containers and maybe custom fluid items
            List<ItemEntity> list = world.getEntitiesOfClass(ItemEntity.class, aabb);
            for (ItemEntity item : list)
            {

            }
        }
        //XP
        if(canTransferXP(coinInPedestal))
        {
            //maybe also get xp custom items???
            List<ExperienceOrb> list = world.getEntitiesOfClass(ExperienceOrb.class, aabb);
            for (ExperienceOrb orb : list)
            {

            }
        }
        //Dust
        if(canTransferDust(coinInPedestal))
        {
            //get custom dust items only, and maybe jars???
            List<ItemEntity> list = world.getEntitiesOfClass(ItemEntity.class, aabb);
            for (ItemEntity item : list)
            {

            }
        }
    }
}
