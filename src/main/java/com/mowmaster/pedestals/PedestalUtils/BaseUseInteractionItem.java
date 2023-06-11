package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class BaseUseInteractionItem extends Item {

    public BaseUseInteractionItem(Properties p_41383_) {
        super(p_41383_);
    }

    public InteractionResultHolder interactCrouchingTargetAir(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result)
    { return InteractionResultHolder.pass(player.getItemInHand(hand)); }
    public InteractionResultHolder interactTargetAir(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result)
    { return  InteractionResultHolder.pass(player.getItemInHand(hand)); }

    public InteractionResultHolder interactCrouchingTargetBlock(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result)
    { return InteractionResultHolder.pass(player.getItemInHand(hand)); }
    public InteractionResultHolder interactTargetBlock(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result)
    { return  InteractionResultHolder.pass(player.getItemInHand(hand)); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {

        Level level = p_41432_;
        if(!level.isClientSide())
        {
            Player player = p_41433_;
            InteractionHand hand = p_41434_;
            ItemStack itemStackInHand = player.getItemInHand(hand);
            HitResult result = player.pick(5,0,false);
            BlockPos pos = new BlockPos(result.getLocation().x,result.getLocation().y,result.getLocation().z);
            if(result.getType().equals(HitResult.Type.MISS))
            {
                if(player.isShiftKeyDown())
                {
                    interactCrouchingTargetAir(level, player, hand, itemStackInHand, result);
                }
                else
                {
                    interactTargetAir(level, player, hand, itemStackInHand, result);
                }
            }
            else if(result.getType().equals(HitResult.Type.BLOCK))
            {
                if(player.isShiftKeyDown())
                {
                    interactCrouchingTargetBlock(level, player, hand, itemStackInHand, result);
                }
                else
                {
                    interactTargetBlock(level, player, hand, itemStackInHand, result);
                }
            }
        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }
}
