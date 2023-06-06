package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
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


import net.minecraft.world.item.Item.Properties;

public class AugmentRenderDiffuser extends AugmentBase
{

    public AugmentRenderDiffuser(Properties p_41383_) {
        super(p_41383_);
    }

    public static int getAugmentMode(ItemStack stack) {

        // 0 - No Particles
        // 1 - No Render Item
        // 2 - No Render Upgrade
        // 3 - No Particles/No Render Item
        // 4 - No Particles/No Render Upgrade
        // 5 - No Render Item/No Render Upgrade
        // 6 - No Particles/No Render Item/No Render Upgrade

        return readModeFromNBT(stack);
    }

    public static void saveModeToNBT(ItemStack augment, int mode)
    {
        CompoundTag compound = new CompoundTag();
        if(augment.hasTag())
        {
            compound = augment.getTag();
        }
        compound.putInt(MODID+"_augment_mode",mode);
        augment.setTag(compound);
    }

    public static int readModeFromNBT(ItemStack augment) {
        if(augment.hasTag())
        {
            CompoundTag getCompound = augment.getTag();
            return getCompound.getInt(MODID+"_augment_mode");
        }
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level world = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack itemInOffhand = player.getOffhandItem();


        if(itemInOffhand.isEmpty())
        {

        }
        else
        {
            if(itemInOffhand.getItem() instanceof IPedestalAugment)
            {
                HitResult result = player.pick(5,0,false);
                if(result.getType().equals(HitResult.Type.MISS))
                {
                    if(player.isShiftKeyDown())
                    {
                        int mode = getAugmentMode(itemInOffhand)+1;
                        int setNewMode = (mode<=6)?(mode):(0);
                        saveModeToNBT(itemInOffhand,setNewMode);
                        player.setItemInHand(p_41434_,itemInOffhand);

                        List<String>typeStringList = new ArrayList<String>(
                                Arrays.asList(
                                        ".mode_augment_particle",
                                        ".mode_augment_item",
                                        ".mode_augment_upgrade",
                                        ".mode_augment_pi",
                                        ".mode_augment_pu",
                                        ".mode_augment_iu",
                                        ".mode_augment_piu"));

                        List<ChatFormatting>chatColorList = new ArrayList<ChatFormatting>(
                                Arrays.asList(
                                        ChatFormatting.LIGHT_PURPLE,
                                        ChatFormatting.AQUA,
                                        ChatFormatting.GOLD,
                                        ChatFormatting.WHITE,
                                        ChatFormatting.LIGHT_PURPLE,
                                        ChatFormatting.AQUA,
                                        ChatFormatting.GOLD));

                        MoveToMowLibUtils.modeBasedTextOutputPopup(player, setNewMode, true, MODID, typeStringList, chatColorList);
                    }
                }
            }
        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }




    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        List<String>typeStringList = new ArrayList<String>(
                Arrays.asList(
                        ".mode_augment_particle",
                        ".mode_augment_item",
                        ".mode_augment_upgrade",
                        ".mode_augment_pi",
                        ".mode_augment_pu",
                        ".mode_augment_iu",
                        ".mode_augment_piu"));
        MoveToMowLibUtils.modeBasedTextOutputTooltip(getAugmentMode(p_41421_), true, MODID, typeStringList, ChatFormatting.GOLD, p_41423_);

        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
    }

}
