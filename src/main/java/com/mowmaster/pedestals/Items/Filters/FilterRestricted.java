package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.PedestalUtils.ColorReference;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.List;


public class FilterRestricted extends BaseFilter{
    public FilterRestricted(Properties p_41383_) {
        super(p_41383_);
    }

    public static int getColor(ItemStack filterIn)
    {
        return 65280;
    }

    @Override
    public int canAcceptCount(BasePedestalBlockEntity pedestal, Level world, BlockPos pos, ItemStack itemInPedestal, ItemStack itemStackIncoming, int mode) {
        if(itemInPedestal.isEmpty())
        {
            ItemStack filter = pedestal.getFilterInPedestal();
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,mode);
            int range = stackCurrent.size();
            int count = 0;
            int maxIncomming = itemStackIncoming.getMaxStackSize();
            for(int i=0;i<range;i++)
            {
                count +=stackCurrent.get(i).getCount();
                if(count>=maxIncomming)break;
            }
            if(mode==0)
            {
                return (count>0)?((count>maxIncomming)?(maxIncomming):(count)):(1);
            }
            else return count;
        }
        return 0;
    }

    //Right Click
    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level world = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack itemInMainhand = player.getMainHandItem();
        ItemStack itemInOffhand = player.getOffhandItem();
        HitResult result = player.pick(5,0,false);

        if(!world.isClientSide())
        {
            //Disable Filter Base
            if(!(itemInOffhand.getItem().equals(DeferredRegisterItems.FILTER_BASE.get())) || !(itemInMainhand.getItem().equals(DeferredRegisterItems.FILTER_BASE.get())))
            {
                //Check for Offhand Only Filter
                if(itemInOffhand.getItem() instanceof IPedestalFilter && !(itemInMainhand.getItem() instanceof IPedestalFilter))
                {
                    if(result.getType().equals(HitResult.Type.MISS))
                    {
                        if(player.isCrouching())
                        {
                            setFilterMode(player,itemInOffhand,InteractionHand.OFF_HAND);
                        }
                    }
                    else if(result.getType().equals(HitResult.Type.BLOCK))
                    {
                        if(player.isCrouching())
                        {
                            UseOnContext context = new UseOnContext(player,hand,((BlockHitResult) result));
                            BlockHitResult res = new BlockHitResult(context.getClickLocation(), context.getHorizontalDirection(), context.getClickedPos(), false);
                            BlockPos posBlock = res.getBlockPos();

                            List<ItemStack> buildQueue = this.buildFilterQueue(world,posBlock);

                            if(buildQueue.size() > 0 && this.getFilterMode(itemInOffhand)<=3)
                            {
                                this.writeFilterQueueToNBT(itemInOffhand,buildQueue, this.getFilterMode(itemInOffhand));
                                ChatFormatting color;
                                switch (getFilterMode(itemInOffhand))
                                {
                                    case 0: color = ChatFormatting.GOLD; break;
                                    case 1: color = ChatFormatting.BLUE; break;
                                    case 2: color = ChatFormatting.RED; break;
                                    case 3: color = ChatFormatting.GREEN; break;
                                    default: color = ChatFormatting.WHITE; break;
                                }
                                TranslatableComponent filterChanged = new TranslatableComponent(MODID + ".filter_changed");
                                filterChanged.withStyle(color);
                                player.displayClientMessage(filterChanged,true);
                            }
                        }
                    }
                }
                else if(itemInOffhand.getItem() instanceof IPedestalFilter && itemInMainhand.getItem() instanceof IPedestalFilter){
                    TranslatableComponent pedestalFluid = new TranslatableComponent(MODID + ".filter.message_twohanded");
                    pedestalFluid.withStyle(ChatFormatting.RED);
                    player.displayClientMessage(pedestalFluid,true);
                }
            }
        }

        return InteractionResultHolder.fail(p_41433_.getItemInHand(p_41434_));
    }

    @Override
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        TranslatableComponent filterList = new TranslatableComponent(filterStack.getDisplayName().getString());
        filterList.withStyle(ChatFormatting.GOLD);
        player.sendMessage(filterList, Util.NIL_UUID);

        TranslatableComponent enchant = new TranslatableComponent(MODID + ".filters.tooltip_filterlist_count");
        enchant.withStyle(ChatFormatting.LIGHT_PURPLE);
        player.sendMessage(enchant, Util.NIL_UUID);

        TranslatableComponent enchants = new TranslatableComponent("1");
        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,getFilterMode(filterStack));
        int range = filterQueue.size();
        if(range>0)
        {
            int count = 0;
            for(int i=0;i<range;i++)
            {
                count +=filterQueue.get(i).getCount();
                if(count>=64)break;
            }

            enchants = new TranslatableComponent(""+((count>0)?((count>64)?(64):(count)):(1))+"");
        }
        enchants.withStyle(ChatFormatting.GRAY);
        player.sendMessage(enchants, Util.NIL_UUID);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        if(!p_41421_.getItem().equals(DeferredRegisterItems.FILTER_BASE))
        {
            boolean filterType = getFilterType(p_41421_,getFilterMode(p_41421_));
            int filterMode = getFilterMode(p_41421_);

            TranslatableComponent filterList = new TranslatableComponent(MODID + ".filter_type");
            TranslatableComponent white = new TranslatableComponent(MODID + ".filter_type_whitelist");
            TranslatableComponent black = new TranslatableComponent(MODID + ".filter_type_blacklist");
            filterList.append((filterType)?(black):(white));
            filterList.withStyle(ChatFormatting.WHITE);
            p_41423_.add(filterList);

            TranslatableComponent changed = new TranslatableComponent(MODID + ".tooltip_mode");
            String typeString = "";
            switch(filterMode)
            {
                case 0: typeString = ".mode_items"; break;
                case 1: typeString = ".mode_fluids"; break;
                case 2: typeString = ".mode_energy"; break;
                case 3: typeString = ".mode_experience"; break;
                default: typeString = ".error"; break;
            }
            changed.withStyle(ChatFormatting.GOLD);
            TranslatableComponent type = new TranslatableComponent(MODID + typeString);
            changed.append(type);
            p_41423_.add(changed);
        }
    }

    /*@Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        TranslatableComponent enchant = new TranslatableComponent(MODID + ".filters.tooltip_filterlist_count");
        enchant.withStyle(ChatFormatting.LIGHT_PURPLE);
        p_41423_.add(enchant);

        TranslatableComponent enchants = new TranslatableComponent("1");
        List<ItemStack> filterQueue = readFilterQueueFromNBT(p_41421_,getFilterMode(p_41421_));
        int range = filterQueue.size();
        if(range>0)
        {
            int count = 0;
            for(int i=0;i<range;i++)
            {
                count +=filterQueue.get(i).getCount();
                if(count>=64)break;
            }

            enchants = new TranslatableComponent(""+((count>0)?((count>64)?(64):(count)):(1))+"");

        }
        enchants.withStyle(ChatFormatting.GRAY);
        p_41423_.add(enchants);
    }*/
}
