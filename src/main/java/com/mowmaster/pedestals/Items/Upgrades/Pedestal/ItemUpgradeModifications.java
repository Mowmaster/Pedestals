package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ISelectableArea;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ISelectablePoints;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeModifications extends ItemUpgradeBase implements ISelectablePoints
{
    public ItemUpgradeModifications(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public boolean selectedPointWithinRange(BasePedestalBlockEntity pedestal, BlockPos posPoint)
    {
        if(isSelectionInRange(pedestal, posPoint))
        {
            Level level = pedestal.getLevel();
            if(level.getBlockState(posPoint).getBlock() instanceof BasePedestalBlock)return true;
        }

        return false;
    }

    private void buildValidBlockList(BasePedestalBlockEntity pedestal)
    {
        Level level = pedestal.getLevel();
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<BlockPos> listed = readBlockPosListFromNBT(coin);
        List<BlockPos> valid = new ArrayList<>();
        for (BlockPos pos:listed) {
            if(selectedPointWithinRange(pedestal, pos))
            {
                if(level.getBlockState(pos).getBlock() instanceof BasePedestalBlock)
                {
                    valid.add(pos);
                }
            }
        }

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
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {


        List<BlockPos> listed = getValidList(pedestal);

        List<BlockPos> getList = readBlockPosListFromNBT(coin);
        if(listed.size()>0)
        {
            modifierAction(level,pedestal);
        }
        else if(getList.size()>0)
        {
            if(!hasBlockListCustomNBTTags(coin,"_validlist"))
            {
                buildValidBlockList(pedestal);
            }
            else if(!pedestal.getRenderRange())
            {
                pedestal.setRenderRange(true);
            }
        }
    }








    public void modifierAction(Level level, BasePedestalBlockEntity pedestal)
    {
        if(!level.isClientSide())
        {
            /*
            Get Pedestals at each location
            (up to 9?)

            Upgrade Input is in the fist avail slot of the inv below the current pedestal(make sure it only takes one out at a time)

            The selected pedestals are the next 9 inputs
            (if there are more then 9, ignore them)

            Output item will be put in the current pedestal (to be transferred out)

            recipes in jei, have global ones and item specific ones.
            the base upgrade will be the 'input' for JEI to show all the global options
            for the item specific ones, let those inputs be how to find them in jei.

            Will need a text readout for what modification is applied and the rate of application and the max it can apply
             */

        }
    }
}
