package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.Items.Filters.IItemMode;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibXpUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.IntStream;

public class ItemUpgradeVoid extends ItemUpgradeBase implements IHasModeTypes
{
    public ItemUpgradeVoid(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) {
        return true;
    }

    private boolean passesFilter(BasePedestalBlockEntity pedestal, @Nullable ItemStack stackIn, @Nullable FluidStack fluidIn, @Nullable DustMagic magicIn, int energy, int exp, IItemMode.ItemTransferMode mode)
    {
        if(pedestal.hasFilter())
        {
            ItemStack filterInPedestal = pedestal.getFilterInPedestal();
            if(filterInPedestal.getItem() instanceof BaseFilter filter)
            {
                if(filter.getFilterDirection().neutral())
                {
                    if(mode == IItemMode.ItemTransferMode.ITEMS) return filter.canAcceptItems(filterInPedestal,stackIn);
                    if(mode == IItemMode.ItemTransferMode.FLUIDS) return filter.canAcceptFluids(filterInPedestal,fluidIn);
                    if(mode == IItemMode.ItemTransferMode.ENERGY) return filter.canAcceptEnergy(filterInPedestal,energy);
                    if(mode == IItemMode.ItemTransferMode.EXPERIENCE) return filter.canAcceptExperience(filterInPedestal,exp);
                    if(mode == IItemMode.ItemTransferMode.DUST) return filter.canAcceptDust(filterInPedestal,magicIn);
                }
            }
        }

        return true;
    }



    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {
        //Items
        if(canTransferItems(coin))
        {
            if(pedestal.hasItem())
            {
                for(int i=0;i<pedestal.getItemStacks().size();i++)
                {
                    if(passesFilter(pedestal,pedestal.getItemInPedestal(i),null,null,0,0, IItemMode.ItemTransferMode.ITEMS))
                    {
                        if(!pedestal.removeItemStack(i,true).isEmpty())
                        {
                            pedestal.removeItemStack(i,false);
                        }
                    }
                }
            }
        }
        //Fluids
        if(canTransferFluids(coin))
        {
            if(pedestal.hasFluid() && passesFilter(pedestal,null,pedestal.getStoredFluid(),null,0,0, IItemMode.ItemTransferMode.FLUIDS))pedestal.removeFluid(pedestal.getStoredFluid(), IFluidHandler.FluidAction.EXECUTE);
        }
        //Energy
        if(canTransferEnergy(coin))
        {
            if(pedestal.hasEnergy() && passesFilter(pedestal,null,null,null,pedestal.getStoredEnergy(),0, IItemMode.ItemTransferMode.ENERGY))pedestal.removeEnergy(pedestal.getStoredEnergy(),false);
        }
        //XP
        if(canTransferXP(coin))
        {
            if(pedestal.hasExperience() && passesFilter(pedestal,null,null,null,0,pedestal.getStoredExperience(), IItemMode.ItemTransferMode.EXPERIENCE))pedestal.removeExperience(pedestal.getStoredExperience(),false);
        }
        //Dust
        if(canTransferDust(coin))
        {
            if(pedestal.hasDust() && passesFilter(pedestal,null,null,pedestal.getStoredDust(),0,0, IItemMode.ItemTransferMode.DUST))pedestal.removeDust(pedestal.getStoredDust(), IDustHandler.DustAction.EXECUTE);
        }
    }



    @Override
    public void onCollideAction(BasePedestalBlockEntity pedestal) {
        List<Entity> entitiesColliding = pedestal.getLevel().getEntitiesOfClass(Entity.class,new AABB(pedestal.getPos()));
        for(Entity getEntity : entitiesColliding)
        {
            if(getEntity instanceof ItemEntity itemEntity)
            {
                if(passesFilter(pedestal,itemEntity.getItem(),null,null,0,0, IItemMode.ItemTransferMode.ITEMS))
                {
                    itemEntity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
            else if (getEntity instanceof LivingEntity livingEntity)
            {
                livingEntity.kill();
            }
            else
            {
                getEntity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }
}
