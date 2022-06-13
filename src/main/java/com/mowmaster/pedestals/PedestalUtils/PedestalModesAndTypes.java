package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class PedestalModesAndTypes
{
    public static int getModeFromStack(ItemStack stack) {
        return readModeFromNBT(stack);
    }

    public static String getModeStringFromInt(int mode) {

        switch(mode)
        {
            case 0: return "item";
            case 1: return "fluid";
            case 2: return "energy";
            case 3: return "xp";
            default: return "item";
        }
    }

    public static String getModeStringFromStack(ItemStack stack) {

        switch(getModeFromStack(stack))
        {
            case 0: return "item";
            case 1: return "fluid";
            case 2: return "energy";
            case 3: return "xp";
            default: return "item";
        }
    }

    public static void saveModeToNBT(ItemStack stack, int mode)
    {
        CompoundTag compound = new CompoundTag();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }
        compound.putInt(MODID+"_transfer_mode",mode);
        stack.setTag(compound);
    }

    public static int readModeFromNBT(ItemStack stack) {
        if(stack.hasTag())
        {
            CompoundTag getCompound = stack.getTag();
            return getCompound.getInt(MODID+"_transfer_mode");
        }
        return 0;
    }

    public static ChatFormatting getModeColorFormat(ItemStack stack)
    {
        ChatFormatting color;
        switch (getModeFromStack(stack))
        {
            case 0: color = ChatFormatting.GOLD; break;
            case 1: color = ChatFormatting.BLUE; break;
            case 2: color = ChatFormatting.RED; break;
            case 3: color = ChatFormatting.GREEN; break;
            default: color = ChatFormatting.WHITE; break;
        }

        return color;
    }

    public static ChatFormatting getModeDarkColorFormat(int mode)
    {
        ChatFormatting colorChange;
        switch (mode)
        {
            case 0: colorChange = ChatFormatting.GOLD; break;
            case 1: colorChange = ChatFormatting.DARK_BLUE; break;
            case 2: colorChange = ChatFormatting.RED; break;
            case 3: colorChange = ChatFormatting.DARK_GREEN; break;
            default: colorChange = ChatFormatting.DARK_RED; break;
        }

        return colorChange;
    }

    public static String getModeLocalizedString(int mode)
    {
        String typeString = "";
        switch(mode)
        {
            case 0: typeString = ".mode_items"; break;
            case 1: typeString = ".mode_fluids"; break;
            case 2: typeString = ".mode_energy"; break;
            case 3: typeString = ".mode_experience"; break;
            default: typeString = ".error"; break;
        }

        return typeString;
    }


}
