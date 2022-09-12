package com.mowmaster.pedestals.Items.Tools;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;

import net.minecraft.world.item.Item.Properties;

import java.awt.*;

public class DevTool extends BaseTool implements IPedestalTool
{
    public DevTool(Properties p_41383_) {
        super(p_41383_);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level level = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack stackInHand = player.getItemInHand(hand);
        ItemStack stackInMainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack stackInOffHand = player.getItemInHand(InteractionHand.OFF_HAND);
        //Build Color List from NBT
        HitResult result = player.pick(5,0,false);
        if(result.getType().equals(HitResult.Type.MISS))
        {
            if(stackInOffHand.hasTag())
            {
                MowLibMessageUtils.messagePlayerChatText(p_41433_, ChatFormatting.GOLD, stackInOffHand.getTag().toString());
            }
        }
        else if(result.getType().equals(HitResult.Type.BLOCK))
        {
            BlockPos pos = new BlockPos(result.getLocation().x,result.getLocation().y,result.getLocation().z);
            BlockState statedHit = level.getBlockState(pos);

            if(stackInOffHand.getItem().equals(Items.HONEYCOMB))
            {
                if (statedHit.is(BlockTags.BEEHIVES, (p_202454_) -> {
                    return p_202454_.hasProperty(BeehiveBlock.HONEY_LEVEL) && p_202454_.getBlock() instanceof BeehiveBlock;
                })) {
                    int i = statedHit.getValue(BeehiveBlock.HONEY_LEVEL);
                    if (i < 5) {
                        level.setBlockAndUpdate(pos,statedHit.setValue(BeehiveBlock.HONEY_LEVEL,5));
                    }
                }
            }
        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return DeferredRegisterItems.TOOL_DEVTOOL.get().getDefaultInstance();
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }
}
