package com.mowmaster.pedestals.Items.Tools;

import com.google.common.collect.Maps;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

import net.minecraft.world.item.Item.Properties;

public class LinkingToolBackwards extends BaseTool implements IPedestalTool
{
    public static final BlockPos defaultPos = new BlockPos(0,-2000,0);
    public BlockPos storedPosition = defaultPos;
    public List<BlockPos> storedPositionList = new ArrayList<>();
    public List<BlockPos> storedPositionList2 = new ArrayList<>();

    public LinkingToolBackwards(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level world = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack stackInHand = player.getItemInHand(hand);

        String linksucess = MODID + ".tool_link_success_backwardslinkingtool";
        String linkstart = MODID + ".tool_link_start_backwardslinkingtool";
        String linkclear = MODID + ".tool_link_cleared";
        String toolchange = MODID + ".tool_change";


        if(!world.isClientSide())
        {
            //Build Color List from NBT
            HitResult result = player.pick(5,0,false);
            BlockPos pos = new BlockPos(result.getLocation().x,result.getLocation().y,result.getLocation().z);
            if(result.getType().equals(HitResult.Type.MISS))
            {
                if(player.isShiftKeyDown())
                {
                    if(stackInHand.is(DeferredRegisterItems.TOOL_LINKINGTOOLBACKWARDS.get()))
                    {
                        ItemStack newTool = new ItemStack(DeferredRegisterItems.TOOL_LINKINGTOOL.get(),stackInHand.getCount(),stackInHand.getTag());
                        if(stackInHand.isEnchanted())
                        {
                            writePosToNBT(newTool);
                            writePosListToNBT(newTool);
                        }
                        player.setItemInHand(hand, newTool);

                        MowLibMessageUtils.messagePopup(player,ChatFormatting.GREEN,toolchange);
                        return InteractionResultHolder.success(stackInHand);
                    }
                }
            }
            else if(result.getType().equals(HitResult.Type.BLOCK))
            {
                BlockState getBlockState = world.getBlockState(pos);
                if(player.isShiftKeyDown())
                {
                    if(getBlockState.getBlock() instanceof BasePedestalBlock)
                    {
                        if(!stackInHand.isEnchanted())
                        {
                            BlockEntity tile = world.getBlockEntity(pos);
                            if(tile instanceof BasePedestalBlockEntity)
                            {
                                BasePedestalBlockEntity ped = ((BasePedestalBlockEntity)tile);
                                this.storedPositionList = ped.getLinkedLocations();
                            }
                            //Gets Pedestal Clicked on Pos
                            this.storedPosition = pos;
                            //Writes to NBT
                            writePosToNBT(stackInHand);
                            writePosListToNBT(stackInHand);
                            //Applies effect to wrench in hand
                            if(stackInHand.getItem() instanceof LinkingToolBackwards)
                            {
                                stackInHand.enchant(Enchantments.UNBREAKING,-1);
                            }

                            MowLibMessageUtils.messagePopup(player,ChatFormatting.AQUA,linkstart);

                            return InteractionResultHolder.success(stackInHand);
                        }
                        //If wrench has the compound stacks and has a position stored(is enchanted)
                        else if(stackInHand.hasTag() && stackInHand.isEnchanted())
                        {
                            BlockPos senderPos = getStoredPosition(stackInHand);
                            //Checks if clicked blocks is a Pedestal
                            if(world.getBlockState(pos).getBlock() instanceof BasePedestalBlock)
                            {
                                //Checks Tile at location to make sure its a TilePedestal
                                if (world.getBlockEntity(senderPos) instanceof BasePedestalBlockEntity senderPedestal) {
                                    int previousLinkedCount = senderPedestal.getNumLinkedPedestals();
                                    if (senderPedestal.attemptUpdateLink(pos, player, linksucess)) {
                                        // successfully updated link, clean-up the tool
                                        Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                        EnchantmentHelper.setEnchantments(enchantsNone,stackInHand);

                                        // TODO: this maintains existing behavior of not clearing the `storedPosition` if
                                        // a connection was removed, which might have just been a bug?
                                        if (senderPedestal.getNumLinkedPedestals() > previousLinkedCount) {
                                            storedPosition = defaultPos;
                                            storedPositionList = new ArrayList<>();
                                            writePosToNBT(stackInHand);
                                            writePosListToNBT(stackInHand);
                                            world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                                        }
                                    }
                                }
                            }
                            return InteractionResultHolder.fail(stackInHand);
                        }
                    }
                    else
                    {
                        this.storedPosition = defaultPos;
                        this.storedPositionList = new ArrayList<>();
                        this.storedPositionList2 = new ArrayList<>();
                        writePosToNBT(stackInHand);
                        writePosListToNBT(stackInHand);
                        writePosListToNBT2(stackInHand);
                        world.sendBlockUpdated(pos,world.getBlockState(pos),world.getBlockState(pos),2);
                        if(stackInHand.getItem() instanceof LinkingToolBackwards)
                        {
                            if(stackInHand.isEnchanted())
                            {
                                Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                EnchantmentHelper.setEnchantments(enchantsNone,stackInHand);
                                MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,linkclear);
                                return InteractionResultHolder.success(stackInHand);
                            }
                        }
                    }

                    return InteractionResultHolder.fail(stackInHand);
                }
            }
        }

        return InteractionResultHolder.fail(stackInHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_41427_) {
        /*if(world.getBlockState(pos).getBlock() instanceof BasePedestalBlock) {
            //Checks Tile at location to make sure its a TilePedestal
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof BasePedestalBlockEntity) {
                BasePedestalBlockEntity tilePedestal = (BasePedestalBlockEntity) tileEntity;

                String rrobint = MODID + ".tool_chat_rrobin_true";
                String rrobinf = MODID + ".tool_chat_rrobin_false";
                List<String> listed = new ArrayList<>();
                listed.add(tilePedestal.hasRRobin()?(rrobint):(rrobinf));
                MowLibMessageUtils.messagePlayerChatWithAppend(MODID, player,ChatFormatting.LIGHT_PURPLE,MODID + ".tool_chat_rrobin",listed);

                            *//*if(tilePedestal.getSpeed()>0)
                            {
                                TranslatableComponent speed = new TranslatableComponent(MODID + ".tool_speed");
                                speed.append(""+tilePedestal.getSpeed()+"");
                                speed.withStyle(ChatFormatting.RED);
                                player.sendMessage(speed,Util.NIL_UUID);
                            }

                            if(tilePedestal.getCapacity()>0)
                            {
                                TranslatableComponent capacity = new TranslatableComponent(MODID + ".tool_capacity");
                                capacity.append(""+tilePedestal.getCapacity()+"");
                                capacity.withStyle(ChatFormatting.BLUE);
                                player.sendMessage(capacity,Util.NIL_UUID);
                            }*//*

                            *//*System.out.println("Stored Energy: "+pedestal.getStoredEnergy());
                            System.out.println("Stored Fluid: "+pedestal.getStoredFluid().getDisplayName().getString() +": "+ pedestal.getStoredFluid().getAmount());
                            System.out.println("Stored Exp: "+pedestal.getStoredExperience());*//*

                List<BlockPos> getLocations = tilePedestal.getLocationList();
                if(getLocations.size()>0)
                {
                    MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.GOLD,MODID + ".tool_chat_linked");

                    List<String> appends = new ArrayList<>();
                    for(int i = 0; i < getLocations.size();i++)
                    {
                        String seperator = MODID + ".tool_chat_seperator";
                        appends.add(seperator);
                        appends.add("" + getLocations.get(i).getY() + "");
                        appends.add(seperator);
                        appends.add("" + getLocations.get(i).getZ() + "");
                        MowLibMessageUtils.messagePlayerChatWithAppend(MODID,player, ChatFormatting.GRAY, "   " + getLocations.get(i).getX() + "", appends);
                    }
                }
            }
        }*/
        return super.useOn(p_41427_);
    }

    int ticker=0;

    /*@Override
    public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_) {
        if(p_41406_ instanceof Player)
        {
            Player player = ((Player)p_41406_);
            if(p_41404_.isEnchanted() && p_41408_)
            {
                if (p_41404_.hasTag()) {
                    this.getPosFromNBT(p_41404_);

                    List<BlockPos> storedRecievers = getStoredPositionList(p_41404_);
                    List<BlockPos> storedRecievers2 = getStoredPositionList2(p_41404_);
                    int locationsNum = storedRecievers.size();
                    int locationsNum2 = storedRecievers2.size();

                    if(storedPosition!=defaultPos)
                    {
                        if(p_41408_)
                        {
                            if(p_41405_.isClientSide())
                            {
                                ticker++;

                                for(int i=0;i<locationsNum;i++)
                                {
                                    List<Integer> color = MowLibColorReference.getIntColor(MowLibColorReference.ALL_COLORS.get(i));
                                    //if(storedPositionList.size()>i){spawnParticleAroundPedestalBase(p_41405_,ticker,storedPositionList.get(i),color.get(0),color.get(1),color.get(2));}
                                    //if(storedPositionList2.size()>i){spawnParticleAroundPedestalBase(p_41405_,ticker,storedPositionList2.get(i),color.get(0),color.get(1),color.get(2));}
                                }
                            }
                        }
                    }

                }
            }
        }
    }*/

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

    public List<BlockPos> getStoredPositionList2(ItemStack getWrenchItem)
    {
        getPosListFromNBT2(getWrenchItem);
        return storedPositionList2;
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


    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        MutableComponent selected = Component.translatable(MODID + ".tool_tip_block_selected");
        MutableComponent unselected = Component.translatable(MODID + ".tool_tip_block_unselected");
        MutableComponent cordX = Component.translatable(MODID + ".tool_tip_X");
        MutableComponent cordY = Component.translatable(MODID + ".tool_tip_Y");
        MutableComponent cordZ = Component.translatable(MODID + ".tool_tip_Z");
        if(p_41421_.getItem() instanceof LinkingTool || p_41421_.getItem() instanceof LinkingToolBackwards) {
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

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return DeferredRegisterItems.TOOL_LINKINGTOOLBACKWARDS.get().getDefaultInstance();
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }
}
