package com.mowmaster.pedestals.Items.Tools;


import com.google.common.collect.Maps;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlock;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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

public class LinkingTool extends BaseTool implements IPedestalTool
{
    public static final BlockPos defaultPos = new BlockPos(0,-2000,0);
    public BlockPos storedPosition = defaultPos;
    public List<BlockPos> storedPositionList = new ArrayList<>();

    public LinkingTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level world = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack stackInHand = player.getItemInHand(hand);

        String linksucess = MODID + ".tool_link_success";
        String linkunsuccess = MODID + ".tool_link_unsucess";
        String linkremoved = MODID + ".tool_link_removed";
        String linkitsself = MODID + ".tool_link_itsself";
        String linknetwork = MODID + ".tool_link_network";
        String linkdistance = MODID + ".tool_link_distance";

        if(!world.isClientSide())
        {
            //Build Color List from NBT
            HitResult result = player.pick(5,0,false);
            BlockPos pos = new BlockPos(result.getLocation().x,result.getLocation().y,result.getLocation().z);
            if(result.getType().equals(HitResult.Type.MISS))
            {
                if(player.isCrouching())
                {
                    if(stackInHand.getItem().equals(DeferredRegisterItems.TOOL_LINKINGTOOL.get()))
                    {
                        ItemStack newTool = new ItemStack(DeferredRegisterItems.TOOL_LINKINGTOOLBACKWARDS.get(),stackInHand.getCount(),stackInHand.getTag());
                        if(stackInHand.isEnchanted())
                        {
                            writePosToNBT(newTool);
                            writePosListToNBT(newTool);
                        }
                        player.setItemInHand(hand, newTool);

                        MowLibMessageUtils.messagePopup(player,ChatFormatting.GREEN,"pedestals.tool_change");
                        return InteractionResultHolder.success(stackInHand);
                    }
                }
            }
            else if(result.getType().equals(HitResult.Type.BLOCK))
            {
                BlockState getBlockState = world.getBlockState(pos);
                if(player.isCrouching())
                {
                    if(getBlockState.getBlock() instanceof BasePedestalBlock)
                    {
                        if(stackInHand.isEnchanted()==false)
                        {
                            BlockEntity tile = world.getBlockEntity(pos);
                            if(tile instanceof BasePedestalBlockEntity)
                            {
                                BasePedestalBlockEntity ped = ((BasePedestalBlockEntity)tile);
                                this.storedPositionList = ped.getLocationList();
                            }
                            //Gets Pedestal Clicked on Pos
                            this.storedPosition = pos;
                            //Writes to NBT
                            writePosToNBT(stackInHand);
                            writePosListToNBT(stackInHand);
                            //Applies effect to wrench in hand
                            if(stackInHand.getItem() instanceof LinkingTool)
                            {
                                stackInHand.enchant(Enchantments.UNBREAKING,-1);
                            }
                        }
                        //If wrench has the compound stacks and has a position stored(is enchanted)
                        else if(stackInHand.hasTag() && stackInHand.isEnchanted())
                        {
                            //Checks if clicked blocks is a Pedestal
                            if(world.getBlockState(pos).getBlock() instanceof BasePedestalBlock)
                            {
                                //Checks Tile at location to make sure its a TilePedestal
                                BlockEntity tileEntity = world.getBlockEntity(pos);
                                if (tileEntity instanceof BasePedestalBlockEntity) {
                                    BasePedestalBlockEntity tilePedestal = (BasePedestalBlockEntity) tileEntity;

                                    //checks if connecting pedestal is out of range of the senderPedestal
                                    if(isPedestalInRange(tilePedestal,getStoredPosition(stackInHand)))
                                    {
                                        //Checks if pedestals to be linked are on same networks or if one is neutral
                                        if(tilePedestal.canLinkToPedestalNetwork(getStoredPosition(stackInHand)))
                                        {
                                            //If stored location isnt the same as the connecting pedestal
                                            if(!tilePedestal.isSamePedestal(getStoredPosition(stackInHand)))
                                            {
                                                //Checks if the conenction hasnt been made once already yet
                                                if(!tilePedestal.isAlreadyLinked(getStoredPosition(stackInHand)))
                                                {
                                                    //Checks if senderPedestal has locationSlots available
                                                    //System.out.println("Stored Locations: "+ tilePedestal.getNumberOfStoredLocations());
                                                    if(tilePedestal.storeNewLocation(getStoredPosition(stackInHand)))
                                                    {

                                                        //If slots are available then set wrench properties back to a default value
                                                        this.storedPosition = defaultPos;
                                                        this.storedPositionList = new ArrayList<>();
                                                        writePosToNBT(stackInHand);
                                                        writePosListToNBT(stackInHand);
                                                        world.sendBlockUpdated(pos,world.getBlockState(pos),world.getBlockState(pos),2);

                                                        if(stackInHand.getItem() instanceof LinkingTool)
                                                        {
                                                            if(stackInHand.isEnchanted())
                                                            {
                                                                Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                                                EnchantmentHelper.setEnchantments(enchantsNone,stackInHand);
                                                            }
                                                        }
                                                        MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.WHITE,linksucess);
                                                    }
                                                    else MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.WHITE,linkunsuccess);
                                                }
                                                else
                                                {
                                                    tilePedestal.removeLocation(getStoredPosition(stackInHand));

                                                    if(stackInHand.getItem() instanceof LinkingTool)
                                                    {
                                                        if(stackInHand.isEnchanted())
                                                        {
                                                            Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                                            EnchantmentHelper.setEnchantments(enchantsNone,stackInHand);
                                                        }
                                                    }
                                                    MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.WHITE,linkremoved);
                                                }
                                            }
                                            else MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.WHITE,linkitsself);
                                        }
                                        else MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.WHITE,linknetwork);
                                    }
                                    else MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.WHITE,linkdistance);
                                }
                            }
                        }
                    }
                    else
                    {
                        this.storedPosition = defaultPos;
                        this.storedPositionList = new ArrayList<>();
                        writePosToNBT(stackInHand);
                        writePosListToNBT(stackInHand);
                        world.sendBlockUpdated(pos,world.getBlockState(pos),world.getBlockState(pos),2);
                        if(stackInHand.getItem() instanceof LinkingTool)
                        {
                            if(stackInHand.isEnchanted())
                            {
                                Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                EnchantmentHelper.setEnchantments(enchantsNone,stackInHand);
                            }
                        }
                    }
                }
                else
                {
                    if(world.getBlockState(pos).getBlock() instanceof BasePedestalBlock) {
                        //Checks Tile at location to make sure its a TilePedestal
                        BlockEntity tileEntity = world.getBlockEntity(pos);
                        if (tileEntity instanceof BasePedestalBlockEntity) {
                            BasePedestalBlockEntity tilePedestal = (BasePedestalBlockEntity) tileEntity;

                            String rrobint = MODID + ".tool_chat_rrobin_true";
                            String rrobinf = MODID + ".tool_chat_rrobin_false";
                            List<String> listed = new ArrayList<>();
                            listed.add(tilePedestal.hasRRobin()?(rrobint):(rrobinf));
                            MowLibMessageUtils.messagePlayerChatWithAppend(MODID, player,ChatFormatting.LIGHT_PURPLE,MODID + ".tool_chat_rrobin",listed);

                            /*if(tilePedestal.getSpeed()>0)
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
                            }*/


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
                    }
                }
            }
        }

        return InteractionResultHolder.fail(stackInHand);
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
                    int locationsNum = storedRecievers.size();

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
                                    if(storedPositionList.size()>i){spawnParticleAroundPedestalBase(p_41405_,ticker,storedPositionList.get(i),color.get(0),color.get(1),color.get(2));}
                                }
                            }
                        }
                    }
                }
            }
        }
    }*/

    public boolean isPedestalInRange(BasePedestalBlockEntity pedestal, BlockPos pedestalToBeLinked)
    {
        int range = pedestal.getLinkingRange();
        int x = pedestalToBeLinked.getX();
        int y = pedestalToBeLinked.getY();
        int z = pedestalToBeLinked.getZ();
        int x1 = pedestal.getPos().getX();
        int y1 = pedestal.getPos().getY();
        int z1 = pedestal.getPos().getZ();
        int xF = Math.abs(Math.subtractExact(x,x1));
        int yF = Math.abs(Math.subtractExact(y,y1));
        int zF = Math.abs(Math.subtractExact(z,z1));

        if(xF>range || yF>range || zF>range)
        {
            return false;
        }
        else return true;
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
        return DeferredRegisterItems.TOOL_LINKINGTOOL.get().getDefaultInstance();
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }


}
