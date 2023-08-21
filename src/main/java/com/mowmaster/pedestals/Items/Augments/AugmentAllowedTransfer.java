package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.mowlib.Items.BaseUseInteractionItem;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Items.Upgrades.Pedestal.ItemUpgradeBase;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class AugmentAllowedTransfer extends BaseUseInteractionItem
{
    public AugmentAllowedTransfer(Properties p_41383_) {
        super(p_41383_);
    }

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

    /*
    MODES

    0 - Items
    1 - Fluids
    2 - Energy
    3 - XP
    4 - Dust
     */

    public static void writeTransportModeToNBT(ItemStack filterStack, int mode, boolean allowed) {
        CompoundTag compound = new CompoundTag();
        if(filterStack.hasTag())
        {
            compound = filterStack.getTag();
        }
        compound.putBoolean(MODID + "_" + getModeStringFromInt(mode)+"_transport_mode",allowed);
        filterStack.setTag(compound);
    }

    public static boolean getTransportModeFromNBT(ItemStack filterStack, int mode) {
        boolean allowed = true;
        if(filterStack.hasTag())
        {
            CompoundTag getCompound = filterStack.getTag();
            String tag = MODID + "_" + getModeStringFromInt(mode)+"_transport_mode";
            if(filterStack.getTag().contains(tag))
            {
                allowed = getCompound.getBoolean(tag);
            }
        }
        return allowed;
    }

    public void toggleTransportMode(Player player, ItemStack heldItem, InteractionHand hand) {
        if(heldItem.getItem() instanceof ItemUpgradeBase baseUpgrade)
        {
            int mode = getUpgradeMode(heldItem);
            boolean getTransportMode = getTransportModeFromNBT(heldItem,mode);
            writeTransportModeToNBT(heldItem, mode, !getTransportMode);
            player.setItemInHand(hand,heldItem);

            ChatFormatting colorChange = (!getTransportMode)?(ChatFormatting.WHITE):(ChatFormatting.BLACK);
            MowLibMessageUtils.messagePopup(player,colorChange,MODID + ((!getTransportMode)?(".transport_mode_changed_true"):(".transport_mode_changed_false")));
        }
    }

    public void incrementUpgradeMode(Player player, ItemStack heldItem, InteractionHand hand)
    {
        if(heldItem.getItem() instanceof ItemUpgradeBase baseUpgrade)
        {
            int mode = getUpgradeMode(heldItem)+1;
            int setNewMode = (mode<=4)?(mode):(0);
            saveUpgradeModeToNBT(heldItem,setNewMode);
            player.setItemInHand(hand,heldItem);

            ChatFormatting colorChange = getModeColorFormat(setNewMode);
            String typeString = getModeLocalizedString(setNewMode);

            List<String> listed = new ArrayList<>();
            listed.add(MODID + typeString);
            MowLibMessageUtils.messagePopupWithAppend(MODID, player,colorChange,MODID + ".mode_changed",listed);
        }
    }

    public static void saveUpgradeModeToNBT(ItemStack augment, int mode)
    {
        CompoundTag compound = new CompoundTag();
        if(augment.hasTag())
        {
            compound = augment.getTag();
        }
        compound.putInt(MODID+"_upgrade_mode",mode);
        augment.setTag(compound);
    }

    public static int readUpgradeModeFromNBT(ItemStack augment) {
        if(augment.hasTag())
        {
            CompoundTag getCompound = augment.getTag();
            return getCompound.getInt(MODID+"_upgrade_mode");
        }
        return 0;
    }

    public static int getUpgradeMode(ItemStack stack) {

        return readUpgradeModeFromNBT(stack);
    }

    public static int getUpgradeModeForRender(ItemStack stack) {

        int mode = readUpgradeModeFromNBT(stack);
        boolean type = getTransportModeFromNBT(stack,mode);
        return (type)?(mode):(mode+5);
    }

    public static MutableComponent getUpgradeModeComponentFromInt(int mode) {

        switch(mode)
        {
            case 0: return Component.translatable(MODID + ".item_mode_component");
            case 1: return Component.translatable(MODID + ".fluid_mode_component");
            case 2: return Component.translatable(MODID + ".energy_mode_component");
            case 3: return Component.translatable(MODID + ".xp_mode_component");
            case 4: return Component.translatable(MODID + ".dust_mode_component");
            default: return Component.translatable(MODID + ".item_mode_component");
        }
    }
}
