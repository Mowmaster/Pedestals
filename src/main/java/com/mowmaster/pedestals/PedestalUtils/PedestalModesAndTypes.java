package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class PedestalModesAndTypes
{
    /*public static int getModeFromStack(ItemStack stack) {
        return readModeFromNBT(stack);
    }

    //Change for new Modes


    //Change for new Modes
    public static String getModeStringFromStack(ItemStack stack) {

        switch(getModeFromStack(stack))
        {
            case 0: return "item";
            case 1: return "fluid";
            case 2: return "energy";
            case 3: return "xp";
            case 4: return "dust";
            default: return "item";
        }
    }

    //Change for new Modes
    public static MutableComponent getModeComponentFromStack(ItemStack stack) {

        switch(getModeFromStack(stack))
        {
            case 0: return Component.translatable(MODID + ".item_mode_component");
            case 1: return Component.translatable(MODID + ".fluid_mode_component");
            case 2: return Component.translatable(MODID + ".energy_mode_component");
            case 3: return Component.translatable(MODID + ".xp_mode_component");
            case 4: return Component.translatable(MODID + ".dust_mode_component");
            default: return Component.translatable(MODID + ".item_mode_component");
        }
    }

    public static void saveModeToNBT(ItemStack stack, int mode)
    {
        CompoundTag compound = new CompoundTag();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }
        compound.putByte(MODID+"_transfer_mode",(byte)mode);
        stack.setTag(compound);
    }

    public static int readModeFromNBT(ItemStack stack) {
        if(stack.hasTag())
        {
            CompoundTag getCompound = stack.getTag();
            return getCompound.getByte(MODID+"_transfer_mode");
        }
        return 0;
    }

    //Change for new Modes
    public static ChatFormatting getModeColorFormat(ItemStack stack)
    {
        ChatFormatting color;
        switch (getModeFromStack(stack))
        {
            case 0: color = ChatFormatting.GOLD; break;
            case 1: color = ChatFormatting.BLUE; break;
            case 2: color = ChatFormatting.RED; break;
            case 3: color = ChatFormatting.GREEN; break;
            case 4: color = ChatFormatting.LIGHT_PURPLE; break;
            default: color = ChatFormatting.WHITE; break;
        }

        return color;
    }

    //Change for new Modes
    public static ChatFormatting getModeDarkColorFormat(int mode)
    {
        ChatFormatting colorChange;
        switch (mode)
        {
            case 0: colorChange = ChatFormatting.GOLD; break;
            case 1: colorChange = ChatFormatting.DARK_BLUE; break;
            case 2: colorChange = ChatFormatting.RED; break;
            case 3: colorChange = ChatFormatting.DARK_GREEN; break;
            case 4: colorChange = ChatFormatting.DARK_PURPLE; break;
            default: colorChange = ChatFormatting.DARK_RED; break;
        }

        return colorChange;
    }

    //Change for new Modes
    */
    public static ChatFormatting getModeColorFormat(int mode)
    {
        ChatFormatting color;
        switch (mode)
        {
            case 0: color = ChatFormatting.GOLD; break;
            case 1: color = ChatFormatting.BLUE; break;
            case 2: color = ChatFormatting.RED; break;
            case 3: color = ChatFormatting.GREEN; break;
            case 4: color = ChatFormatting.LIGHT_PURPLE; break;
            default: color = ChatFormatting.WHITE; break;
        }

        return color;
    }

    public static String getModeStringFromInt(int mode) {

        switch(mode)
        {
            case 0: return "item";
            case 1: return "fluid";
            case 2: return "energy";
            case 3: return "xp";
            case 4: return "dust";
            default: return "item";
        }
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
            case 4: typeString = ".mode_dust"; break;
            default: typeString = ".error"; break;
        }

        return typeString;
    }


}
