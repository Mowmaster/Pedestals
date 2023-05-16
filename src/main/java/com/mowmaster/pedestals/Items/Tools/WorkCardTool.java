package com.mowmaster.pedestals.Items.Tools;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public class WorkCardTool extends BaseTool implements IPedestalTool
{
    public WorkCardTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {

        Level world = p_41432_;
        if(!world.isClientSide())
        {
            Player player = p_41433_;
            InteractionHand hand = p_41434_;
            ItemStack stackInHand = player.getItemInHand(hand);
            //Build Color List from NBT
            HitResult result = player.pick(5,0,false);
            BlockPos pos = new BlockPos(result.getLocation().x,result.getLocation().y,result.getLocation().z);
            if(result.getType().equals(HitResult.Type.MISS))
            {
                if(player.isShiftKeyDown())
                {
                    if(stackInHand.getItem().equals(DeferredRegisterItems.TOOL_WORKTOOL.get()))
                    {
                        ItemStack newTool = new ItemStack(DeferredRegisterItems.TOOL_TAGTOOL.get(),stackInHand.getCount(),stackInHand.getTag());
                        player.setItemInHand(hand, newTool);

                        MowLibMessageUtils.messagePopup(player,ChatFormatting.GREEN,"pedestals.tool_change");
                        return InteractionResultHolder.success(stackInHand);
                    }
                }
            }
        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return DeferredRegisterItems.TOOL_WORKTOOL.get().getDefaultInstance();
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }
}
