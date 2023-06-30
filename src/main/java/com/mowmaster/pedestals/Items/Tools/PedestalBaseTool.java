package com.mowmaster.pedestals.Items.Tools;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseFilterableBlock;
import com.mowmaster.mowlib.BlockEntities.MowLibBaseFilterableBlockEntity;
import com.mowmaster.mowlib.Items.Tools.BaseTool;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public class PedestalBaseTool extends BaseTool {
    public PedestalBaseTool(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder interactGetPedestalDetail(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(result.getLocation().x, result.getLocation().y, result.getLocation().z);
        BlockState getBlockState = level.getBlockState(pos);
        if (getBlockState.getBlock() instanceof BasePedestalBlock) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof BasePedestalBlockEntity pedestal) {
                getPedestalDetail(pedestal, player);
            }
        }
        super.interactGetPedestalDetail(level, player, hand, itemStackInHand, result);

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {}
}
