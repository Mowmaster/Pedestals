package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.pedestals.PedestalUtils.MoveToMowLibUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;
import static com.mowmaster.pedestals.PedestalUtils.References.isDustLoaded;

public class AugmentTransferToggle extends AugmentBase
{

    public AugmentTransferToggle(Properties p_41383_) {
        super(p_41383_);
    }

    public static int getAugmentMode(ItemStack stack)
    {
        /*
        Selected Type(s)
        0 - Items
        1 - Fluids
        2 - Energy
        3 - Xp
        4 - Dust

        5 - Items off
        6 - Fluids off
        7 - Energy off
        8 - Xp off
        9 - Dust off
        */

        //true = off
        if(getTransferToggle(getTransferType(stack), stack))
        {
            return getTransferType(stack)+5;
        }
        else
        {
            return getTransferType(stack);
        }
    }


    public static int getTransferType(ItemStack stack) {
        /*
        Selected Type(s)
        0 - Items
        1 - Fluids
        2 - Energy
        3 - Xp
        4 - Dust
        Toggle Mode
        - False=On
        - True=Off

        */
        return MowLibCompoundTagUtils.readIntegerFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_type");
    }

    public static boolean getTransferToggle(int type, ItemStack stack) {

        switch(type)
        {
            case 0:
                return MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_toggleitem");
            case 1:
                return MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_togglefluid");
            case 2:
                return MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_toggleenergy");
            case 3:
                return MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_togglexp");
            case 4:
                return MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_toggledust");
            default:
                return false;
        }
    }

    public boolean canSend(int type, ItemStack stack)
    {
        /*
        Toggle Mode
        - False=On
        - True=Off
        */
        switch(type)
        {
            case 0:
                return !MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_toggleitem");
            case 1:
                return !MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_togglefluid");
            case 2:
                return !MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_toggleenergy");
            case 3:
                return !MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_togglexp");
            case 4:
                return !MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_toggledust");
            default:
                return true;
        }
    }

    public void toggleTransferType(int type, ItemStack stack)
    {
        switch(type)
        {
            case 0:
                MowLibCompoundTagUtils.writeBooleanToNBT(MODID,stack.getOrCreateTag(), !MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_toggleitem"),MODID+"_augment_transfertoggle_toggleitem");
            case 1:
                MowLibCompoundTagUtils.writeBooleanToNBT(MODID,stack.getOrCreateTag(), !MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_togglefluid"),MODID+"_augment_transfertoggle_togglefluid");
            case 2:
                MowLibCompoundTagUtils.writeBooleanToNBT(MODID,stack.getOrCreateTag(), !MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_toggleenergy"),MODID+"_augment_transfertoggle_toggleenergy");
            case 3:
                MowLibCompoundTagUtils.writeBooleanToNBT(MODID,stack.getOrCreateTag(), !MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_togglexp"),MODID+"_augment_transfertoggle_togglexp");
            case 4:
                MowLibCompoundTagUtils.writeBooleanToNBT(MODID,stack.getOrCreateTag(), !MowLibCompoundTagUtils.readBooleanFromNBT(MODID,stack.getOrCreateTag(),MODID+"_augment_transfertoggle_toggledust"),MODID+"_augment_transfertoggle_toggledust");
            default:
        }
    }

    public void iterateTransferType(ItemStack stack)
    {
        //0->3
        int maxType = 3;
        if(isDustLoaded())
        {
            maxType = 4;
        }

        int currentType = getTransferType(stack);
        MowLibCompoundTagUtils.writeIntegerToNBT(MODID,stack.getOrCreateTag(), (currentType+1 > maxType)?(0):(currentType+1),MODID+"_augment_transfertoggle_type");
    }

    @Override
    public InteractionResultHolder interactTargetAir(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {
        toggleTransferType(getTransferType(itemStackInHand), itemStackInHand);

        boolean currentToggle = getTransferToggle(getTransferType(itemStackInHand), itemStackInHand);
        String typeStringList = (currentToggle)?(".augment_transfertoggle_off"):(".augment_transfertoggle_on");
        ChatFormatting chatColorList = (currentToggle)?(ChatFormatting.BLACK):(ChatFormatting.WHITE);
        MowLibMessageUtils.messagePopup(player,chatColorList,MODID+typeStringList);
        return super.interactCrouchingTargetAir(level, player, hand, itemStackInHand, result);
    }

    @Override
    public InteractionResultHolder interactCrouchingTargetAir(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {
        iterateTransferType(itemStackInHand);

        int currentType = getTransferType(itemStackInHand);
        List<String>typeStringList = new ArrayList<String>(
                Arrays.asList(
                        ".augment_transfertoggle_item",
                        ".augment_transfertoggle_fluid",
                        ".augment_transfertoggle_energy",
                        ".augment_transfertoggle_xp",
                        ".augment_transfertoggle_dust"));
        List<ChatFormatting>chatColorList = new ArrayList<ChatFormatting>(
                Arrays.asList(
                        ChatFormatting.GOLD,
                        ChatFormatting.AQUA,
                        ChatFormatting.RED,
                        ChatFormatting.GREEN,
                        ChatFormatting.LIGHT_PURPLE));

        MowLibMessageUtils.messagePopup(player,chatColorList.get(currentType),MODID+typeStringList.get(currentType));
        return super.interactTargetAir(level, player, hand, itemStackInHand, result);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        int maxType = 3;
        if(isDustLoaded()){maxType = 4;}
        List<String>typeStringList = new ArrayList<String>(
                Arrays.asList(
                        ".augment_transfertoggle_item",
                        ".augment_transfertoggle_fluid",
                        ".augment_transfertoggle_energy",
                        ".augment_transfertoggle_xp",
                        ".augment_transfertoggle_dust"));
        List<ChatFormatting>chatColorList = new ArrayList<ChatFormatting>(
                Arrays.asList(
                        ChatFormatting.GOLD,
                        ChatFormatting.AQUA,
                        ChatFormatting.RED,
                        ChatFormatting.GREEN,
                        ChatFormatting.LIGHT_PURPLE));


        for(int i=0;i<=maxType;i++)
        {
            boolean currentToggle = getTransferToggle(i, p_41421_);
            String typeStringListBool = (currentToggle)?(".augment_transfertoggle_off"):(".augment_transfertoggle_on");

            MutableComponent componentColon = Component.literal(": ");
            MutableComponent component = Component.translatable(MODID + typeStringList.get(i));
            component.withStyle(chatColorList.get(i));
            MutableComponent componentBool = Component.translatable(MODID + typeStringListBool);
            componentBool.withStyle(ChatFormatting.WHITE);

            component.append(componentColon);
            component.append(componentBool);

            MowLibTooltipUtils.addTooltipMessage(p_41423_,p_41421_,component);
        }

        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
    }

}
