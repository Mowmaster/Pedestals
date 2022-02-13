package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

//https://www.youtube.com/watch?v=uOAeHJtZMZM
public class TooltipUtils
{

    public static void addTooltipMessage(List<Component> componentList, ItemStack stack, TranslatableComponent translatableComponent)
    {
        TranslatableComponent base = translatableComponent;
        componentList.add(base);
    }

    public static void addTooltipMessageWithStyle(List<Component> componentList, TranslatableComponent translatableComponent, ChatFormatting chatFormatting)
    {
        TranslatableComponent base = translatableComponent;
        base.withStyle(chatFormatting);
        componentList.add(base);
    }

    public static void addTooltipMessageWithStyle(List<Component> componentList, String localizationString, ChatFormatting chatFormatting)
    {
        TranslatableComponent base = new TranslatableComponent(localizationString);
        base.withStyle(chatFormatting);
        componentList.add(base);
    }

    public static void addTooltipShiftMessage(List<Component> componentList, ItemStack stack, TranslatableComponent translatableComponent)
    {
        if(!Screen.hasShiftDown())
        {
            TranslatableComponent base = new TranslatableComponent(MODID + ".description_shift");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            TranslatableComponent base = translatableComponent;
            componentList.add(base);
        }
    }

    public static void addTooltipShiftMessageWithStyle(List<Component> componentList, String localizationString, ChatFormatting chatFormatting)
    {
        if(!Screen.hasShiftDown())
        {
            TranslatableComponent base = new TranslatableComponent(MODID + ".description_shift");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            TranslatableComponent base = new TranslatableComponent(localizationString);
            base.withStyle(chatFormatting);
            componentList.add(base);
        }
    }

    public static void addTooltipShiftMessageMultiWithStyle(List<Component> componentList, List<String> localizationString, List<ChatFormatting> chatFormatting)
    {
        if(!Screen.hasShiftDown())
        {
            TranslatableComponent base = new TranslatableComponent(MODID + ".description_shift");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            for(int i=0;i<localizationString.size();i++)
            {
                TranslatableComponent base = new TranslatableComponent(localizationString.get(i));
                base.withStyle((chatFormatting.size()>=i)?(chatFormatting.get(i)):(chatFormatting.get(0)));
                componentList.add(base);
            }
        }
    }

    public static void addTooltipShiftMessageWithStyle(List<Component> componentList, TranslatableComponent translatableComponent, ChatFormatting chatFormatting)
    {
        if(!Screen.hasShiftDown())
        {
            TranslatableComponent base = new TranslatableComponent(MODID + ".description_shift");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            TranslatableComponent base = translatableComponent;
            base.withStyle(chatFormatting);
            componentList.add(base);
        }
    }

    public static void addTooltipAltMessage(List<Component> componentList, ItemStack stack, TranslatableComponent translatableComponent)
    {
        if(!Screen.hasAltDown())
        {
            TranslatableComponent base = new TranslatableComponent(MODID + ".description_alt");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            TranslatableComponent base = translatableComponent;
            componentList.add(base);
        }
    }

    public static void addTooltipAltMessageWithStyle(List<Component> componentList, String localizationString, ChatFormatting chatFormatting)
    {
        if(!Screen.hasAltDown())
        {
            TranslatableComponent base = new TranslatableComponent(MODID + ".description_alt");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            TranslatableComponent base = new TranslatableComponent(localizationString);
            base.withStyle(chatFormatting);
            componentList.add(base);
        }
    }

    public static void addTooltipAltMessageWithStyle(List<Component> componentList, TranslatableComponent translatableComponent, ChatFormatting chatFormatting)
    {
        if(!Screen.hasAltDown())
        {
            TranslatableComponent base = new TranslatableComponent(MODID + ".description_alt");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            TranslatableComponent base = translatableComponent;
            base.withStyle(chatFormatting);
            componentList.add(base);
        }
    }

    public static String getRomanNumeral(int value)
    {
        switch(value)
        {
            case 0:return "I";
            case 1:return "II";
            case 2:return "III";
            case 3:return "IV";
            case 4:return "V";
            case 5:return "VI";
            case 6:return "VII";
            case 7:return "VIII";
            case 8:return "IX";
            case 9:return "X";
            default: return "I";
        }
    }
}
