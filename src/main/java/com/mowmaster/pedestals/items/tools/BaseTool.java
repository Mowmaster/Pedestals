package com.mowmaster.pedestals.items.tools;

import com.mowmaster.mowlib.Items.BaseUseInteractionItem;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlock.FACING;

import net.minecraft.world.phys.HitResult;

public class BaseTool extends BaseUseInteractionItem
{

    public BaseTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    public ItemStack getMainTool(){return Items.STICK.getDefaultInstance();}
    public ItemStack getSwappedTool(){return Items.BLAZE_ROD.getDefaultInstance();}

    /*public InteractionResultHolder interactCrouchingTargetAir(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result)
    { return interactSwapTool(level,player,hand,itemStackInHand, result, getMainTool().getItem(), getSwappedTool().getItem()); }
*/
    public InteractionResultHolder interactCrouchingTargetBlock(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result)
    { return interactGetPedestalDetail(level, player, hand, itemStackInHand, result); }


    //Default method of interactCrouchingTargetAir
   /* public InteractionResultHolder interactSwapTool(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result, Item mainTool, Item swapTool)
    {
        if(itemStackInHand.getItem().equals(mainTool))
        {
            ItemStack newTool = new ItemStack(swapTool,itemStackInHand.getCount(),itemStackInHand.getTag());
            player.setItemInHand(hand, newTool);

            MowLibMessageUtils.messagePopup(player, ChatFormatting.GREEN,"Pedestals.tool_change");
            return InteractionResultHolder.success(itemStackInHand);
        }

        return  InteractionResultHolder.pass(player.getItemInHand(hand));
    }*/

    //Default method of interactCrouchingTargetBlock
    public InteractionResultHolder interactGetPedestalDetail(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result)
    {
        BlockPos pos = new BlockPos(result.getLocation().x,result.getLocation().y,result.getLocation().z);
        BlockState getBlockState = level.getBlockState(pos);
        if(getBlockState.getBlock() instanceof BasePedestalBlock)
        {
            BlockEntity tile = level.getBlockEntity(pos);
            if(tile instanceof BasePedestalBlockEntity pedestal)
            {
                getPedestalDetail(pedestal, player);
            }
        }
        return  InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    //Default method of interactGetPedestalDetail which is in interactCrouchingTargetBlock
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {}

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return getMainTool();
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return !getMainTool().isEmpty();
    }

    public void spawnParticleAroundPedestalBase(Level world, BlockPos pos, int r, int g, int b)
    {
        double dx = (double)pos.getX();
        double dy = (double)pos.getY();
        double dz = (double)pos.getZ();

        BlockState state = world.getBlockState(pos);
        Direction enumfacing = Direction.UP;
        if(state.getBlock() instanceof BasePedestalBlock)
        {
            enumfacing = state.getValue(FACING);
        }

        switch (enumfacing)
        {
            case UP:
                if (world.getGameTime()%20 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.25D,dy+0.5D,dz+ 0.25D,r,g,b));
                if (world.getGameTime()%25 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.25D,dy+0.5D,dz+ 0.75D,r,g,b));
                if (world.getGameTime()%15 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.75D,dy+0.5D,dz+ 0.25D,r,g,b));
                if (world.getGameTime()%30 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.75D,dy+0.5D,dz+ 0.75D,r,g,b));
                return;
            case DOWN:
                if (world.getGameTime()%20 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.25D,dy+0.5D,dz+ 0.25D,r,g,b));
                if (world.getGameTime()%25 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.25D,dy+0.5D,dz+ 0.75D,r,g,b));
                if (world.getGameTime()%15 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.75D,dy+0.5D,dz+ 0.25D,r,g,b));
                if (world.getGameTime()%30 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.75D,dy+0.5D,dz+ 0.75D,r,g,b));
                return;
            case NORTH:
                if (world.getGameTime()%20 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.25D,dy+0.25D,dz+ 0.5D,r,g,b));
                if (world.getGameTime()%25 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.25D,dy+0.75D,dz+ 0.5D,r,g,b));
                if (world.getGameTime()%15 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.75D,dy+0.25D,dz+ 0.5D,r,g,b));
                if (world.getGameTime()%30 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.75D,dy+0.75D,dz+ 0.5D,r,g,b));
                return;
            case SOUTH:
                if (world.getGameTime()%20 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.25D,dy+0.25D,dz+ 0.5D,r,g,b));
                if (world.getGameTime()%25 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.25D,dy+0.75D,dz+ 0.5D,r,g,b));
                if (world.getGameTime()%15 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.75D,dy+0.25D,dz+ 0.5D,r,g,b));
                if (world.getGameTime()%30 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.75D,dy+0.75D,dz+ 0.5D,r,g,b));
                return;
            case EAST:
                if (world.getGameTime()%20 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.5D,dy+0.25D,dz+ 0.25D,r,g,b));
                if (world.getGameTime()%25 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.5D,dy+0.25D,dz+ 0.75D,r,g,b));
                if (world.getGameTime()%15 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.5D,dy+0.75D,dz+ 0.25D,r,g,b));
                if (world.getGameTime()%30 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.5D,dy+0.75D,dz+ 0.75D,r,g,b));
                return;
            case WEST:
                if (world.getGameTime()%20 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.5D,dy+0.25D,dz+ 0.25D,r,g,b));
                if (world.getGameTime()%25 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.5D,dy+0.25D,dz+ 0.75D,r,g,b));
                if (world.getGameTime()%15 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.5D,dy+0.75D,dz+ 0.25D,r,g,b));
                if (world.getGameTime()%30 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.5D,dy+0.75D,dz+ 0.75D,r,g,b));
                return;
            default:
                if (world.getGameTime()%30 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.25D,dy+0.5D,dz+ 0.25D,r,g,b));
                if (world.getGameTime()%35 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.25D,dy+0.5D,dz+ 0.75D,r,g,b));
                if (world.getGameTime()%25 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.75D,dy+0.5D,dz+ 0.25D,r,g,b));
                if (world.getGameTime()%30 == 0) MowLibPacketHandler.sendToNearby(world,pos,new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR,dx+ 0.75D,dy+0.5D,dz+ 0.75D,r,g,b));
                return;
        }
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
    }
}
