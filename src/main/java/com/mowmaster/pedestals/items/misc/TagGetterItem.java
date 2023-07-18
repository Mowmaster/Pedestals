package com.mowmaster.pedestals.items.misc;

import com.mowmaster.mowlib.MowLibUtils.*;
import com.mowmaster.pedestals.pedestalutils.References;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TagGetterItem extends Item
{
    public TagGetterItem(Properties p_41383_) {
        super(p_41383_);
    }

    //Crafting???
    //some way to clear tags



    //
    // DEFAULT rename on getting tags
    // MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.GRAY,"-----> " + ForgeRegistries.ITEMS.getKey(offhand.getItem()).toString() + " <-----");

    // HOW TO GET THE TAGS
    // offhand.getTags().forEach(tagKey -> MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.WHITE,tagKey.location().toString()));

    // HOW WE USE THE TAGS
    // ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(getLocationFromStringName(itemStack.getDisplayName().getString()))).stream().toList().contains(incomingStack.getItem())



    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level world = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack stackInMainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack stackInOffHand = player.getItemInHand(InteractionHand.OFF_HAND);
        //Build Color List from NBT
        if(stackInMainHand.getItem() instanceof TagGetterItem)
        {
            if(player.isShiftKeyDown())
            {
                //Clear tags???
                clearTags(stackInMainHand);
                MowLibMessageUtils.messagePopup(p_41433_, ChatFormatting.RED, "Pedestals.taggetter.clear");
            }
            else
            {
                if(stackInOffHand.isEmpty())
                {
                    //Interaction: Right Click Item in Main Hand, Empty OffHand.
                    //Cycles Tags on taggetter

                    List<String> listy = getStringList(stackInMainHand);
                    if(listy.size()>0)
                    {
                        int currentlySelected = getSelectedTag(stackInMainHand);
                        int nextSelected = (currentlySelected+1 >= listy.size())?(0):(currentlySelected+1);
                        setSelectedTag(stackInMainHand,nextSelected);
                        stackInMainHand.setHoverName(Component.literal(listy.get(nextSelected)));

                        MutableComponent comp = Component.translatable("pedestals.taggetter.changed_to");
                        comp.withStyle(ChatFormatting.GOLD);
                        MutableComponent modifiedComp = Component.literal(listy.get(nextSelected));
                        modifiedComp.withStyle(ChatFormatting.WHITE);
                        comp.append(modifiedComp);
                        MowLibMessageUtils.messagePopupWithoutStyle(player,comp);
                    }
                }
                else
                {
                    //Interaction: Right Click, Item in Main Hand, Item To get Tags From In Offhand.
                    //Gets Tags off item in offhand and saves them to the taggetter
                    List<String> getTags = getTagsFromItem(stackInOffHand);
                    if(getTags.size()>0)
                    {
                        stackInMainHand.setHoverName(Component.literal(getTags.get(0)));
                        MowLibCompoundTagUtils.writeStringToNBT(References.MODID,stackInMainHand.getOrCreateTag(), ForgeRegistries.ITEMS.getKey(stackInOffHand.getItem()).toString(),"_selecteditem");
                        setStringList(stackInMainHand,getTags);
                        setSelectedTag(stackInMainHand,0);
                        MutableComponent comp = Component.translatable("pedestals.taggetter.created");
                        comp.append(Component.literal(""+getTags.size()+""));
                        MowLibMessageUtils.messagePopup(p_41433_, comp, ChatFormatting.GREEN );
                    }
                }
            }
        }
        else if(stackInOffHand.getItem() instanceof TagGetterItem)
        {
            HitResult result = player.pick(5,0,false);
            if(player.isShiftKeyDown())
            {
                if(result.getType().equals(HitResult.Type.MISS))
                {
                    //Interaction: Crouch Right Click Air, Item in Off Hand. (Same as mode change)
                    //Cycles through the tags on the item.

                    List<String> listy = getStringList(stackInOffHand);
                    if(listy.size()>0)
                    {
                        int currentlySelected = getSelectedTag(stackInOffHand);
                        int nextSelected = (currentlySelected+1 >= listy.size())?(0):(currentlySelected+1);
                        setSelectedTag(stackInOffHand,nextSelected);
                        stackInOffHand.setHoverName(Component.literal(listy.get(nextSelected)));

                        MutableComponent comp = Component.translatable("pedestals.taggetter.changed_to");
                        comp.withStyle(ChatFormatting.GOLD);
                        MutableComponent modifiedComp = Component.literal(listy.get(nextSelected));
                        modifiedComp.withStyle(ChatFormatting.WHITE);
                        comp.append(modifiedComp);
                        MowLibMessageUtils.messagePopupWithoutStyle(player,comp);
                    }
                }
                else if(result.getType().equals(HitResult.Type.BLOCK))
                {
                    //Interaction: Crouch Right Click Block, Item in Off Hand. (Same as Block Filter, to select blocks)
                    //Gets the Block, and saves its tags to the item.
                    UseOnContext context = new UseOnContext(p_41433_, p_41434_, (BlockHitResult)result);
                    BlockHitResult res = new BlockHitResult(context.getClickLocation(), context.getHorizontalDirection(), context.getClickedPos(), false);
                    BlockPos posBlock = res.getBlockPos();
                    ItemStack clickedBlock = p_41432_.getBlockState(posBlock).getCloneItemStack(res,p_41432_,posBlock,p_41433_);
                    List<String> getTags = getTagsFromItem(clickedBlock);
                    if(getTags.size()>0)
                    {
                        MowLibCompoundTagUtils.writeStringToNBT(References.MODID,stackInOffHand.getOrCreateTag(), ForgeRegistries.ITEMS.getKey(clickedBlock.getItem()).toString(),"_selecteditem");
                        stackInOffHand.setHoverName(Component.literal(getTags.get(0)));
                        setStringList(stackInOffHand,getTags);
                        setSelectedTag(stackInOffHand,0);
                        MutableComponent comp = Component.translatable("pedestals.taggetter.created");
                        comp.append(Component.literal(""+getTags.size()+""));
                        MowLibMessageUtils.messagePopup(p_41433_, comp, ChatFormatting.GREEN );
                    }
                }
            }
        }
        return super.use(p_41432_, p_41433_, p_41434_);
    }

    public static void clearTags(ItemStack stackTagGetter)
    {
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,stackTagGetter.getOrCreateTag(),"_selecteditem");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,stackTagGetter.getOrCreateTag(),"_selectedtag");
        if(stackTagGetter.getOrCreateTag().contains(References.MODID +"_stringlistsize"))
        {
            int listsize = stackTagGetter.getTag().getInt(References.MODID +"_stringlistsize");
            for (int i=0; i < listsize;i++)
            {
                MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,stackTagGetter.getTag(),"_stringlistvalue"+"_"+i+"");
            }
        }
        MowLibCompoundTagUtils.removeCustomTagFromNBT(References.MODID,stackTagGetter.getOrCreateTag(),"_stringlistsize");
        stackTagGetter.resetHoverName();
    }



    public static void setSelectedTag(ItemStack stackTagGetter, int value)
    {
        CompoundTag stringListTag = stackTagGetter.getOrCreateTag();
        stringListTag.putInt(References.MODID +"_selectedtag",value);
    }

    public static int getSelectedTag(ItemStack stackTagGetter)
    {
        if(stackTagGetter.hasTag())
        {
            CompoundTag nbt = stackTagGetter.getTag();
            if(nbt.contains(References.MODID +"_selectedtag"))
            {
                int size = nbt.getInt(References.MODID +"_selectedtag");
                return Math.max(0,size);
            }
        }

        return 0;
    }

    public static String getSelectedTagString(ItemStack stackTagGetter)
    {
        int selected = 0;
        if(stackTagGetter.hasTag())
        {
            CompoundTag nbt = stackTagGetter.getTag();
            if(nbt.contains(References.MODID +"_selectedtag"))
            {
                selected = nbt.getInt(References.MODID +"_selectedtag");
            }

            List<String> listy = getStringList(stackTagGetter);
            if(listy.size()>0)
            {
                return listy.get(selected);
            }
        }

        return "";
    }

    public static List<String> getTagsFromItem(ItemStack stackToGetTagsFrom)
    {
        List<String> list = new ArrayList<>();

        stackToGetTagsFrom.getTags().forEach(itemTagKey -> {
            list.add(itemTagKey.location().toString());
        });

        return list;
    }

    public static void setStringList(ItemStack stackTagGetter, List<String> list)
    {
        CompoundTag stringListTag = stackTagGetter.getOrCreateTag();
        stringListTag.putInt(References.MODID + "_stringlistsize",list.size());
        for (int i=0; i < list.size();i++)
        {
            stringListTag.putString(References.MODID + "_stringlistvalue"+"_"+i+"", list.get(i));
        }

        stackTagGetter.setTag(stringListTag);
    }

    public static List<String> getStringList(ItemStack stackTagGetter)
    {
        if(stackTagGetter.hasTag())
        {
            CompoundTag nbt = stackTagGetter.getTag();
            if(nbt.contains(References.MODID +"_stringlistsize"))
            {
                List<String> listy = new ArrayList<String>();
                int size = nbt.getInt(References.MODID + "_stringlistsize");
                if(size <=0) return new ArrayList<String>();

                for(int i=0; i < size; i++)
                {
                    listy.add(nbt.getString(References.MODID + "_stringlistvalue"+"_"+i+""));
                }

                return listy;
            }
        }
        return new ArrayList<String>();
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        List<String> listy = getStringList(p_41421_);
        if(listy.size()>0)
        {
            MutableComponent comp = Component.translatable("pedestals.taggetter.tooltip_header");
            comp.append(Component.literal(MowLibCompoundTagUtils.readStringFromNBT(References.MODID,p_41421_.getOrCreateTag(),"_selecteditem")));
            comp.append(Component.translatable("pedestals.taggetter.tooltip_header"));

            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,comp,ChatFormatting.WHITE);


            int currentlySelected = getSelectedTag(p_41421_);
            for(int i=0;i<listy.size();i++)
            {
                MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.literal(listy.get(i)),(i==currentlySelected)?(ChatFormatting.GOLD):(ChatFormatting.WHITE));
            }
        }
    }
}
