package com.mowmaster.pedestals.Items.Tools;

import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock.FACING;

import net.minecraft.world.item.Item.Properties;

public class BaseTool extends Item
{

    public BaseTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
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
        /*TooltipUtils.addTooltipShiftMessageWithStyle(p_41423_,getDescriptionId() + ".description",ChatFormatting.LIGHT_PURPLE);
        TooltipUtils.addTooltipAltMessageWithStyle(p_41423_,getDescriptionId() + ".description_use",ChatFormatting.AQUA);
*/
    }
}
