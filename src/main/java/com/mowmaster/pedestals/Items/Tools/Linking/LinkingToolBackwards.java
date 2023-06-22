package com.mowmaster.pedestals.Items.Tools.Linking;

import com.google.common.collect.Maps;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class LinkingToolBackwards extends BaseLinkingTool
{
    public List<BlockPos> storedPositionList2 = new ArrayList<>();

    public LinkingToolBackwards(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_LINKINGTOOLBACKWARDS.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_UPGRADETOOL.get().getDefaultInstance(); }

    @Override
    public InteractionResultHolder interactCrouchingTargetBlock(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(result.getLocation().x,result.getLocation().y,result.getLocation().z);
        BlockState getBlockState = level.getBlockState(pos);
        String linksuccess = MODID + ".tool_link_success_backwardslinkingtool";
        String linkstart = MODID + ".tool_link_start_backwardslinkingtool";
        String linkclear = MODID + ".tool_link_cleared";

        if(getBlockState.getBlock() instanceof BasePedestalBlock)
        {
            BlockEntity tile = level.getBlockEntity(pos);
            if(tile instanceof BasePedestalBlockEntity pedestal)
            {
                if(!itemStackInHand.isEnchanted())
                {
                    this.storedPositionList = pedestal.getLinkedLocations();
                    //Gets Pedestal Clicked on Pos
                    this.storedPosition = pos;
                    //Writes to NBT
                    writePosToNBT(itemStackInHand);
                    writePosListToNBT(itemStackInHand);
                    //Applies effect to wrench in hand
                    if(itemStackInHand.getItem() instanceof LinkingToolBackwards)
                    {
                        itemStackInHand.enchant(Enchantments.UNBREAKING,-1);
                    }

                    MowLibMessageUtils.messagePopup(player,ChatFormatting.AQUA,linkstart);

                    return InteractionResultHolder.success(itemStackInHand);
                }
                //If wrench has the compound stacks and has a position stored(is enchanted)
                else if(itemStackInHand.hasTag() && itemStackInHand.isEnchanted())
                {
                    BlockPos senderPos = getStoredPosition(itemStackInHand);
                    //Checks if clicked blocks is a Pedestal
                    if(level.getBlockState(pos).getBlock() instanceof BasePedestalBlock)
                    {
                        //Checks Tile at location to make sure its a TilePedestal
                        if (level.getBlockEntity(senderPos) instanceof BasePedestalBlockEntity senderPedestal) {
                            int previousLinkedCount = senderPedestal.getNumLinkedPedestals();
                            if (senderPedestal.attemptUpdateLink(pos, player, linksuccess)) {
                                // successfully updated link, clean-up the tool
                                Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                EnchantmentHelper.setEnchantments(enchantsNone,itemStackInHand);

                                // TODO: this maintains existing behavior of not clearing the `storedPosition` if
                                // a connection was removed, which might have just been a bug?
                                if (senderPedestal.getNumLinkedPedestals() > previousLinkedCount) {
                                    storedPosition = defaultPos;
                                    storedPositionList = new ArrayList<>();
                                    writePosToNBT(itemStackInHand);
                                    writePosListToNBT(itemStackInHand);
                                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 2);
                                }
                            }
                        }
                    }
                    return InteractionResultHolder.fail(itemStackInHand);
                }
            }
        }
        else
        {
            this.storedPosition = defaultPos;
            this.storedPositionList = new ArrayList<>();
            this.storedPositionList2 = new ArrayList<>();
            writePosToNBT(itemStackInHand);
            writePosListToNBT(itemStackInHand);
            writePosListToNBT2(itemStackInHand);
            level.sendBlockUpdated(pos,level.getBlockState(pos),level.getBlockState(pos),2);
            if(itemStackInHand.getItem() instanceof LinkingToolBackwards)
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

        return InteractionResultHolder.fail(itemStackInHand);
    }

    public List<BlockPos> getStoredPositionList2(ItemStack getWrenchItem)
    {
        getPosListFromNBT2(getWrenchItem);
        return storedPositionList2;
    }

    public void writePosListToNBT2(ItemStack stack)
    {
        CompoundTag compound = new CompoundTag();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }
        List<Integer> xval = new ArrayList<Integer>();
        List<Integer> yval = new ArrayList<Integer>();
        List<Integer> zval = new ArrayList<Integer>();
        for(int i=0;i<storedPositionList2.size();i++)
        {
            xval.add(i,storedPositionList2.get(i).getX());
            yval.add(i,storedPositionList2.get(i).getY());
            zval.add(i,storedPositionList2.get(i).getZ());
        }
        compound.putIntArray("storedlist2_x",xval);
        compound.putIntArray("storedlist2_y",yval);
        compound.putIntArray("storedlist2_z",zval);
        stack.setTag(compound);
    }

    public void getPosListFromNBT2(ItemStack stack)
    {
        List<BlockPos> posStored = new ArrayList<>();
        if(stack.hasTag())
        {
            CompoundTag getCompound = stack.getTag();
            int[] xval = getCompound.getIntArray("storedlist2_x");
            int[] yval = getCompound.getIntArray("storedlist2_y");
            int[] zval = getCompound.getIntArray("storedlist2_z");

            for(int i = 0;i<xval.length;i++)
            {
                posStored.add(i,new BlockPos(xval[i],yval[i],zval[i]));
            }
            this.storedPositionList2 = posStored;
        }
    }
}
