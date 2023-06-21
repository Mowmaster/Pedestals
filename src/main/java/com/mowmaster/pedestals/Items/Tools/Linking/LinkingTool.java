package com.mowmaster.pedestals.Items.Tools.Linking;


import com.google.common.collect.Maps;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import java.util.Map;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class LinkingTool extends BaseLinkingTool
{
    public LinkingTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_LINKINGTOOL.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_LINKINGTOOLBACKWARDS.get().getDefaultInstance(); }

    @Override
    public InteractionResultHolder interactCrouchingTargetBlock(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {
        BlockPos pos = new BlockPos((int)result.getLocation().x,(int)result.getLocation().y,(int)result.getLocation().z);
        BlockState getBlockState = level.getBlockState(pos);
        String linksuccess = MODID + ".tool_link_success_linkingtool";
        String linkstart = MODID + ".tool_link_start_linkingtool";
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
                    if(itemStackInHand.getItem() instanceof LinkingTool)
                    {
                        itemStackInHand.enchant(Enchantments.UNBREAKING,-1);
                    }

                    MowLibMessageUtils.messagePopup(player,ChatFormatting.AQUA,linkstart);
                }
                //If wrench has the compound stacks and has a position stored(is enchanted)
                else if(itemStackInHand.hasTag() && itemStackInHand.isEnchanted())
                {
                    //Checks if clicked blocks is a Pedestal
                    if(level.getBlockState(pos).getBlock() instanceof BasePedestalBlock)
                    {
                        //Checks Tile at location to make sure its a TilePedestal
                        if (level.getBlockEntity(pos) instanceof BasePedestalBlockEntity senderPedestal) {
                            BlockPos receivingPos = getStoredPosition(itemStackInHand);
                            int previousLinkedCount = senderPedestal.getNumLinkedPedestals();
                            if (senderPedestal.attemptUpdateLink(receivingPos, player, linksuccess)) {
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
                }
            }
        }
        else
        {
            this.storedPosition = defaultPos;
            this.storedPositionList = new ArrayList<>();
            writePosToNBT(itemStackInHand);
            writePosListToNBT(itemStackInHand);
            level.sendBlockUpdated(pos,level.getBlockState(pos),level.getBlockState(pos),2);
            if(itemStackInHand.getItem() instanceof LinkingTool)
            {
                if(itemStackInHand.isEnchanted())
                {
                    Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                    EnchantmentHelper.setEnchantments(enchantsNone,itemStackInHand);
                    MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,linkclear);
                }
            }
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
