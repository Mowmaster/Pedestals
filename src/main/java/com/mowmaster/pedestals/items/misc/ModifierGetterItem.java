package com.mowmaster.pedestals.items.misc;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.pedestals.items.upgrades.pedestal.ItemUpgradeBase;
import com.mowmaster.pedestals.pedestalutils.References;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;

public class ModifierGetterItem extends Item
{
    public ModifierGetterItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level world = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack stackInMainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack stackInOffHand = player.getItemInHand(InteractionHand.OFF_HAND);
        //Build Color List from NBT
        if(stackInMainHand.getItem() instanceof ModifierGetterItem)
        {
            if(player.isShiftKeyDown())
            {
                //Clear tags???
                clearTags(stackInMainHand);
                MowLibMessageUtils.messagePopup(p_41433_, ChatFormatting.RED, "Pedestals.modifiergetter.clear");
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

                        stackInMainHand.setHoverName(Component.translatable(References.MODID + ".upgrade_modification." + listy.get(nextSelected)));

                        MutableComponent comp = Component.translatable("pedestals.modifiergetter.changed_to");
                        comp.withStyle(ChatFormatting.GOLD);

                        MutableComponent modifiedComp = Component.translatable(References.MODID + ".upgrade_modification." + listy.get(nextSelected));
                        modifiedComp.withStyle(ChatFormatting.WHITE);
                        comp.append(modifiedComp);
                        MowLibMessageUtils.messagePopupWithoutStyle(player,comp);
                    }
                }
                else
                {
                    if(stackInOffHand.getItem() instanceof ItemUpgradeBase)
                    {
                        //Interaction: Right Click, Item in Main Hand, Item To get Tags From In Offhand.
                        //Gets Tags off item in offhand and saves them to the taggetter
                        List<String> getTags = getTagsFromItem(stackInOffHand);
                        if(getTags.size()>0)
                        {

                            stackInMainHand.setHoverName(Component.translatable(References.MODID + ".upgrade_modification." + getTags.get(0)));
                            MowLibCompoundTagUtils.writeStringToNBT(MODID,stackInMainHand.getOrCreateTag(), ForgeRegistries.ITEMS.getKey(stackInOffHand.getItem()).toString(),"_selecteditem");
                            setStringList(stackInMainHand,getTags);
                            setSelectedTag(stackInMainHand,0);
                            MutableComponent comp = Component.translatable("pedestals.modifiergetter.created");
                            comp.append(Component.literal(""+getTags.size()+""));
                            MowLibMessageUtils.messagePopup(p_41433_, comp, ChatFormatting.GREEN );
                        }
                    }
                }
            }
        }
        return super.use(p_41432_, p_41433_, p_41434_);
    }

    public static void clearTags(ItemStack stackTagGetter)
    {
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,stackTagGetter.getOrCreateTag(),"_selecteditem");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,stackTagGetter.getOrCreateTag(),"_selectedtag");
        if(stackTagGetter.getOrCreateTag().contains(MODID +"_stringlistsize"))
        {
            int listsize = stackTagGetter.getTag().getInt(MODID +"_stringlistsize");
            for (int i=0; i < listsize;i++)
            {
                MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,stackTagGetter.getTag(),"_stringlistvalue"+"_"+i+"");
            }
        }
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID,stackTagGetter.getOrCreateTag(),"_stringlistsize");
        stackTagGetter.resetHoverName();
    }



    public static void setSelectedTag(ItemStack stackTagGetter, int value)
    {
        CompoundTag stringListTag = stackTagGetter.getOrCreateTag();
        stringListTag.putInt(MODID +"_selectedtag",value);
    }

    public static int getSelectedTag(ItemStack stackTagGetter)
    {
        if(stackTagGetter.hasTag())
        {
            CompoundTag nbt = stackTagGetter.getTag();
            if(nbt.contains(MODID +"_selectedtag"))
            {
                int size = nbt.getInt(MODID +"_selectedtag");
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
            if(nbt.contains(MODID +"_selectedtag"))
            {
                selected = nbt.getInt(MODID +"_selectedtag");
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
        List<String> modifiers = new ArrayList<String>(Arrays.asList("upgradespeed", "upgradedamagecapacity",
                "upgradeblockcapacity", "upgradeitemcapacity", "upgradefluidcapacity", "upgradeenergycapacity", "upgradexpcapacity", "upgradedustcapacity",
                "upgradearea", "upgraderange", "upgrademagnet", "upgradegentle", "upgradesuperspeed", "upgradeentitystorage"));

        for(String nbtTag : modifiers)
        {
            if(stackToGetTagsFrom.getTag().contains(MODID + nbtTag))
            {
                list.add(nbtTag);
            }
        }

        return list;
    }

    public static void setStringList(ItemStack stackTagGetter, List<String> list)
    {
        CompoundTag stringListTag = stackTagGetter.getOrCreateTag();
        stringListTag.putInt(MODID + "_stringlistsize",list.size());
        for (int i=0; i < list.size();i++)
        {
            stringListTag.putString(MODID + "_stringlistvalue"+"_"+i+"", list.get(i));
        }

        stackTagGetter.setTag(stringListTag);
    }

    public static List<String> getStringList(ItemStack stackTagGetter)
    {
        if(stackTagGetter.hasTag())
        {
            CompoundTag nbt = stackTagGetter.getTag();
            if(nbt.contains(MODID +"_stringlistsize"))
            {
                List<String> listy = new ArrayList<String>();
                int size = nbt.getInt(MODID + "_stringlistsize");
                if(size <=0) return new ArrayList<String>();

                for(int i=0; i < size; i++)
                {
                    listy.add(nbt.getString(MODID + "_stringlistvalue"+"_"+i+""));
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
            MutableComponent comp = Component.translatable("pedestals.modifiergetter.tooltip_header");
            comp.append(Component.literal(MowLibCompoundTagUtils.readStringFromNBT(References.MODID,p_41421_.getOrCreateTag(),"_selecteditem")));
            comp.append(Component.translatable(References.MODID + ".modifiergetter.tooltip_header"));

            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,comp,ChatFormatting.WHITE);

            int currentlySelected = getSelectedTag(p_41421_);
            for(int i=0;i<listy.size();i++)
            {
                MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(References.MODID + ".upgrade_modification." + listy.get(i)),(i==currentlySelected)?(ChatFormatting.GOLD):(ChatFormatting.WHITE));
            }
        }
    }
}
