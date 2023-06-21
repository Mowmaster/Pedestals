package com.mowmaster.pedestals.Items.Tools.Linking;


import com.google.common.collect.Maps;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class BaseLinkingTool extends BaseTool {
    public static final BlockPos defaultPos = new BlockPos(0,-2000,0);
    public BlockPos storedPosition = defaultPos;
    public List<BlockPos> storedPositionList = new ArrayList<>();

    public BaseLinkingTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public InteractionResultHolder interactSwapTool(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result, Item mainTool, Item swapTool) {
        String toolchange = MODID + ".tool_change";
        String linkclear = MODID + ".tool_link_cleared";
        BlockPos pos = new BlockPos(result.getLocation().x,result.getLocation().y,result.getLocation().z);

        if(itemStackInHand.is(getMainTool().getItem()))
        {
            /*ItemStack newTool = new ItemStack(getSwappedTool().getItem(),itemStackInHand.getCount(),itemStackInHand.getTag());
            player.setItemInHand(hand, newTool);
            if(itemStackInHand.isEnchanted())
            {
                writePosToNBT(newTool);
                writePosListToNBT(newTool);
            }

            MowLibMessageUtils.messagePopup(player,ChatFormatting.GREEN,toolchange);
            return InteractionResultHolder.success(itemStackInHand);*/


            if(itemStackInHand.isEnchanted())
            {
                this.storedPosition = defaultPos;
                this.storedPositionList = new ArrayList<>();
                writePosToNBT(itemStackInHand);
                writePosListToNBT(itemStackInHand);
                level.sendBlockUpdated(pos,level.getBlockState(pos),level.getBlockState(pos),2);
                if(itemStackInHand.getItem() instanceof BaseLinkingTool)
                {
                    if(itemStackInHand.isEnchanted())
                    {
                        Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                        EnchantmentHelper.setEnchantments(enchantsNone,itemStackInHand);
                        MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,linkclear);
                        return InteractionResultHolder.success(itemStackInHand);
                    }
                }
            }
            else {
                ItemStack newTool = new ItemStack(getSwappedTool().getItem(),itemStackInHand.getCount(),itemStackInHand.getTag());
                player.setItemInHand(hand, newTool);

                MowLibMessageUtils.messagePopup(player,ChatFormatting.GREEN,toolchange);
                return InteractionResultHolder.success(itemStackInHand);
            }
        }

        return InteractionResultHolder.fail(itemStackInHand);
    }

    public BlockPos getStoredPosition(ItemStack getWrenchItem)
    {
        getPosFromNBT(getWrenchItem);
        return storedPosition;
    }

    public List<BlockPos> getStoredPositionList(ItemStack getWrenchItem)
    {
        getPosListFromNBT(getWrenchItem);
        return storedPositionList;
    }

    public void writePosToNBT(ItemStack stack)
    {
        CompoundTag compound = new CompoundTag();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }
        compound.putInt("stored_x",this.storedPosition.getX());
        compound.putInt("stored_y",this.storedPosition.getY());
        compound.putInt("stored_z",this.storedPosition.getZ());
        stack.setTag(compound);
    }

    public void writePosListToNBT(ItemStack stack)
    {
        CompoundTag compound = new CompoundTag();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }
        List<Integer> xval = new ArrayList<Integer>();
        List<Integer> yval = new ArrayList<Integer>();
        List<Integer> zval = new ArrayList<Integer>();
        for(int i=0;i<storedPositionList.size();i++)
        {
            xval.add(i,storedPositionList.get(i).getX());
            yval.add(i,storedPositionList.get(i).getY());
            zval.add(i,storedPositionList.get(i).getZ());
        }
        compound.putIntArray("storedlist_x",xval);
        compound.putIntArray("storedlist_y",yval);
        compound.putIntArray("storedlist_z",zval);
        stack.setTag(compound);
    }

    public void getPosFromNBT(ItemStack stack)
    {
        if(stack.hasTag())
        {
            CompoundTag getCompound = stack.getTag();
            int x = getCompound.getInt("stored_x");
            int y = getCompound.getInt("stored_y");
            int z = getCompound.getInt("stored_z");
            this.storedPosition = new BlockPos(x,y,z);
        }
    }

    public void getPosListFromNBT(ItemStack stack)
    {
        List<BlockPos> posStored = new ArrayList<>();
        if(stack.hasTag())
        {
            CompoundTag getCompound = stack.getTag();
            int[] xval = getCompound.getIntArray("storedlist_x");
            int[] yval = getCompound.getIntArray("storedlist_y");
            int[] zval = getCompound.getIntArray("storedlist_z");

            for(int i = 0;i<xval.length;i++)
            {
                posStored.add(i,new BlockPos(xval[i],yval[i],zval[i]));
            }
            this.storedPositionList = posStored;
        }
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        MutableComponent selected = Component.translatable(MODID + ".tool_tip_block_selected");
        MutableComponent unselected = Component.translatable(MODID + ".tool_tip_block_unselected");
        MutableComponent cordX = Component.translatable(MODID + ".tool_tip_X");
        MutableComponent cordY = Component.translatable(MODID + ".tool_tip_Y");
        MutableComponent cordZ = Component.translatable(MODID + ".tool_tip_Z");
        if(p_41421_.getItem() instanceof BaseLinkingTool || p_41421_.getItem() instanceof LinkingToolBackwards) {
            if (p_41421_.hasTag()) {
                if (p_41421_.isEnchanted()) {
                    selected.append("" + this.getStoredPosition(p_41421_).getX() + "");
                    selected.append(cordX.getString());
                    selected.append("" + this.getStoredPosition(p_41421_).getY() + "");
                    selected.append(cordY.getString());
                    selected.append("" + this.getStoredPosition(p_41421_).getZ() + "");
                    selected.append(cordZ.getString());
                    p_41423_.add(selected);
                } else p_41423_.add(unselected);
            } else p_41423_.add(unselected);
        }

        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
    }

}
