package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class MoveToMowLibUtils
{
    public static void modeBasedTextOutputPopup(Player player, int mode, boolean localized, String modid, List<String> modeTextList, List<ChatFormatting> modeColorList)
    {
        modeTextList.add(".error");
        modeColorList.add(ChatFormatting.DARK_RED);
        int getMode = (mode>=modeTextList.size())?(modeTextList.size()-1):(mode);
        MutableComponent type;
        if(localized) { type = Component.translatable(modid + modeTextList.get(getMode)); }
        else { type = Component.literal(modeTextList.get(getMode)); }
        type.withStyle(modeColorList.get(getMode));
        player.displayClientMessage(type, true);
    }

    public static void modeBasedTextOutputTooltip(int mode, boolean localized, String modid, List<String> modeTextList, ChatFormatting textColor, List<Component> comp)
    {
        modeTextList.add(".error");
        int getMode = (mode>=modeTextList.size())?(modeTextList.size()-1):(mode);
        MutableComponent type;
        if(localized) { type = Component.translatable(modid + modeTextList.get(getMode)); }
        else { type = Component.literal(modeTextList.get(getMode)); }
        type.withStyle(textColor);
        comp.add(type);
    }
}
