package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
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

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


public class FilterDurability extends BaseFilter
{
    public FilterDurability(Properties p_41383_) {
        super(p_41383_);
    }

    public int getPercentDamaged(ItemStack itemIn)
    {
        if(itemIn.isDamageableItem())
        {
            int maxDamage = itemIn.getMaxDamage();
            int damage = itemIn.getDamageValue();
            int durabilityCurrent = maxDamage-damage;
            int percentDurability = Math.floorDiv((durabilityCurrent*100),maxDamage);
            return percentDurability;
        }
        return 100;
    }

    public int getDurabilityTarget(ItemStack filter)
    {
        int returner = 0;
        List<ItemStack> filterQueue = readFilterQueueFromNBT(filter,getFilterMode(filter));
        if(filterQueue.size()>0)
        {
            for(int i=0;i<filterQueue.size();i++)
            {
                ItemStack stackGet = filterQueue.get(i);
                returner += getPercentDamaged(stackGet);
                if(returner>100)break;
            }
        }

        return (returner>100)?(100):(returner);
    }

    @Override
    public boolean canAcceptItem(BasePedestalBlockEntity pedestal, ItemStack itemStackIn, int mode) {
        boolean filterBool = getFilterType(pedestal.getFilterInPedestal(),mode);
        int durabilityTarget = getDurabilityTarget(pedestal.getFilterInPedestal());

        int percentDurabilityCurrent = getPercentDamaged(itemStackIn);
        int percentToBeat = durabilityTarget;

        if(mode==0)
        {
            if(filterBool)
            {
                return (itemStackIn.isDamageableItem())?((percentDurabilityCurrent<=percentToBeat)?(true):(false)):(false);
            }
            else
            {
                return (itemStackIn.isDamageableItem())?((percentDurabilityCurrent>=percentToBeat)?(true):(false)):(false);
            }
        }
        else return !filterBool;
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
                            //return InteractionResultHolder.success(itemInOffhand);
                        }
                        else
                        {
                            setFilterTypeAboveBelow(player,itemInOffhand);
                            //return InteractionResultHolder.success(itemInOffhand);
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

                            if(buildQueue.size() > 0 && this.getFilterMode(itemInOffhand)<=0)
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
        //return super.use(p_41432_, p_41433_, p_41434_);
    }

    @Override
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        if(!filterStack.getItem().equals(DeferredRegisterItems.FILTER_BASE.get()))
        {
            TranslatableComponent filterList = new TranslatableComponent(filterStack.getDisplayName().getString());
            filterList.withStyle(ChatFormatting.WHITE);
            player.sendMessage(filterList, Util.NIL_UUID);

            boolean filterType = getFilterType(filterStack,getFilterMode(filterStack));
            TranslatableComponent filterList2 = new TranslatableComponent(MODID + ".filters.tooltip_filtertype");
            TranslatableComponent above = new TranslatableComponent(MODID + ".filters.tooltip_filterabove");
            TranslatableComponent below = new TranslatableComponent(MODID + ".filters.tooltip_filterbelow");
            filterList2.append((filterType)?(below):(above));
            filterList2.withStyle(ChatFormatting.GOLD);
            player.sendMessage(filterList2, Util.NIL_UUID);

            List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,getFilterMode(filterStack));
            if(filterQueue.size()>0)
            {
                TranslatableComponent enchant = new TranslatableComponent(MODID + ".filters.tooltip_filterlist");
                enchant.withStyle(ChatFormatting.LIGHT_PURPLE);
                player.sendMessage(enchant, Util.NIL_UUID);

                TranslatableComponent enchants = new TranslatableComponent(""+getDurabilityTarget(pedestal.getFilterInPedestal())+"");
                enchants.withStyle(ChatFormatting.GRAY);
                player.sendMessage(enchants, Util.NIL_UUID);
            }
        }
        else
        {
            TranslatableComponent base = new TranslatableComponent(MODID + ".baseItem");
            base.withStyle(ChatFormatting.DARK_RED);
            player.sendMessage(base, Util.NIL_UUID);
        }
    }


    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        if(!p_41421_.getItem().equals(DeferredRegisterItems.FILTER_BASE))
        {
            boolean filterType = getFilterType(p_41421_,getFilterMode(p_41421_));
            int filterMode = getFilterMode(p_41421_);

            TranslatableComponent filterList = new TranslatableComponent(MODID + ".filter_type");
            TranslatableComponent white = new TranslatableComponent(MODID + ".filter_type_above");
            TranslatableComponent black = new TranslatableComponent(MODID + ".filter_type_below");
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


}
