package com.mowmaster.pedestals.Items.Tools.Augment;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Augments.AugmentTransferToggle;
import com.mowmaster.pedestals.Items.Tools.BaseTool;
import com.mowmaster.pedestals.Items.Tools.IPedestalTool;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;
import static com.mowmaster.pedestals.PedestalUtils.References.isDustLoaded;

public class AugmentTool_TransferToggle extends BaseTool implements IPedestalTool
{
    public AugmentTool_TransferToggle(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_AUGMENTS_TRANSFERTOGGLE.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_AUGMENTS_SPEED.get().getDefaultInstance(); }

    @Override
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {

        if(pedestal.hasTransferToggleAugment())
        {
            ItemStack augment = pedestal.getTransferToggleAugment();
            if(augment.getItem() instanceof AugmentTransferToggle augmentItem)
            {
                MutableComponent hasAugment = Component.translatable(MODID + ".tool.augment.transfertoggle");
                hasAugment.withStyle(ChatFormatting.LIGHT_PURPLE);


                int maxType = 3;
                if(isDustLoaded()){maxType = 4;}
                List<String> typeStringList = new ArrayList<String>(
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
                    boolean currentToggle = augmentItem.getTransferToggle(i, augment);
                    String typeStringListBool = (currentToggle)?(".augment_transfertoggle_off"):(".augment_transfertoggle_on");

                    MutableComponent componentColon = Component.literal(": ");
                    MutableComponent component = Component.translatable(MODID + typeStringList.get(i));
                    component.withStyle(chatColorList.get(i));
                    MutableComponent componentBool = Component.translatable(MODID + typeStringListBool);
                    componentBool.withStyle(ChatFormatting.WHITE);

                    component.append(componentColon);
                    component.append(componentBool);

                    player.displayClientMessage(component, false);
                }
            }
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.DARK_PURPLE,MODID + ".tool.augment.transfertoggle_not");
        }
    }
}
