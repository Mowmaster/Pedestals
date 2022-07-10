package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.MowLibUtils.MessageUtils;
import com.mowmaster.pedestals.PedestalUtils.PedestalModesAndTypes;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;


import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item.Properties;

public class FilterEnchantedFuzzy extends BaseFilter{
    public FilterEnchantedFuzzy(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public boolean canAcceptItem(BasePedestalBlockEntity pedestal, ItemStack itemStackIn, int mode) {
        boolean filterBool=getFilterType(pedestal.getFilterInPedestal(),mode);

        if(mode==0)
        {
            if(itemStackIn.isEnchanted() || itemStackIn.getItem().equals(Items.ENCHANTED_BOOK))
            {
                ItemStack filter = pedestal.getFilterInPedestal();
                List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,mode);
                int range = stackCurrent.size();

                Map<Enchantment, Integer> mapIncomming = EnchantmentHelper.getEnchantments(itemStackIn);

                for(Map.Entry<Enchantment, Integer> entry : mapIncomming.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    ItemStack itemFromInv = ItemStack.EMPTY;
                    itemFromInv = IntStream.range(0,range)//Int Range
                            .mapToObj((stackCurrent)::get)//Function being applied to each interval
                            //Check to make sure filter item is enchanted
                            .filter(itemStack -> itemStack.isEnchanted() || itemStack.getItem().equals(Items.ENCHANTED_BOOK))
                            //Check if filter item has any enchant that the item in the pedestal has
                            .filter(itemStack -> EnchantmentHelper.getEnchantments(itemStack).containsKey(enchantment))
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!itemFromInv.isEmpty())
                    {
                        return !filterBool;
                    }
                }
            }
        }
        else return !filterBool;

        return filterBool;
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
                            setFilterTypeWhiteBlacklist(player,itemInOffhand);
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

                            if(buildQueue.size() > 0 && PedestalModesAndTypes.getModeFromStack(itemInOffhand)<=0)
                            {
                                this.writeFilterQueueToNBT(itemInOffhand,buildQueue, PedestalModesAndTypes.getModeFromStack(itemInOffhand));
                                ChatFormatting color = PedestalModesAndTypes.getModeColorFormat(itemInOffhand);

                                MessageUtils.messagePopup(player,color,MODID + ".filter_changed");
                            }
                        }
                    }
                }
                else if(itemInOffhand.getItem() instanceof IPedestalFilter && itemInMainhand.getItem() instanceof IPedestalFilter){
                    MessageUtils.messagePopup(player,ChatFormatting.RED,MODID + ".filter.message_twohanded");
                }
            }
        }

        return InteractionResultHolder.fail(p_41433_.getItemInHand(p_41434_));
    }

    @Override
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        MessageUtils.messagePlayerChatText(player,ChatFormatting.GOLD,filterStack.getDisplayName().getString());

        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,PedestalModesAndTypes.getModeFromStack(filterStack));
        if(filterQueue.size()>0)
        {
            MessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,MODID + ".filters.tooltip_filterlist");

            for(int i=0;i<filterQueue.size();i++) {

                if(!filterQueue.get(i).isEmpty())
                {
                    Map<Enchantment, Integer> mapIncomming = EnchantmentHelper.getEnchantments(filterQueue.get(i));
                    for(Map.Entry<Enchantment, Integer> entry : mapIncomming.entrySet()) {
                        MutableComponent enchants = Component.literal(entry.getKey().getDescriptionId());
                        enchants.withStyle(ChatFormatting.GRAY);
                        player.displayClientMessage(enchants, false);
                    }
                }
                MutableComponent enchants = Component.literal("--------------------");
                enchants.withStyle(ChatFormatting.GRAY);
                player.displayClientMessage(enchants, false);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        if(!p_41421_.getItem().equals(DeferredRegisterItems.FILTER_BASE))
        {
            boolean filterType = getFilterType(p_41421_,PedestalModesAndTypes.getModeFromStack(p_41421_));
            int filterMode = PedestalModesAndTypes.getModeFromStack(p_41421_);

            MutableComponent filterList = Component.translatable(MODID + ".filter_type");
MutableComponent white = Component.translatable(MODID + ".filter_type_whitelist");
MutableComponent black = Component.translatable(MODID + ".filter_type_blacklist");
filterList.append((filterType)?(black):(white));
filterList.withStyle(ChatFormatting.WHITE);
p_41423_.add(filterList);

            MutableComponent changed = Component.translatable(MODID + ".tooltip_mode");
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
            MutableComponent type = Component.translatable(MODID + typeString);
            changed.append(type);
            p_41423_.add(changed);
        }
    }

    /*@Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        ItemStack filterStack = p_41421_;
        TranslatableComponent filterList = new TranslatableComponent(filterStack.getDisplayName().getString());
        filterList.withStyle(ChatFormatting.GOLD);
        p_41423_.add(filterList);

        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,PedestalModesAndTypes.getModeFromStack(filterStack));
        if(filterQueue.size()>0)
        {
            TranslatableComponent enchant = new TranslatableComponent(MODID + ".filters.tooltip_filterlist");
            enchant.withStyle(ChatFormatting.LIGHT_PURPLE);
            p_41423_.add(enchant);

            for(int i=0;i<filterQueue.size();i++) {

                if(!filterQueue.get(i).isEmpty())
                {
                    Map<Enchantment, Integer> mapIncomming = EnchantmentHelper.getEnchantments(filterQueue.get(i));
                    for(Map.Entry<Enchantment, Integer> entry : mapIncomming.entrySet()) {
                        TranslatableComponent enchants = new TranslatableComponent(entry.getKey().getDescriptionId());
                        enchants.withStyle(ChatFormatting.GRAY);
                        p_41423_.add(enchants);
                    }
                }
                TranslatableComponent enchants = new TranslatableComponent("--------------------");
                enchants.withStyle(ChatFormatting.GRAY);
                p_41423_.add(enchants);
            }
        }
    }*/
}
