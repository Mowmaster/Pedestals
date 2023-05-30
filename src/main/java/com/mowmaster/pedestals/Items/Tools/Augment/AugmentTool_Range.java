package com.mowmaster.pedestals.Items.Tools.Augment;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Augments.AugmentTieredRange;
import com.mowmaster.pedestals.Items.Augments.AugmentTieredSpeed;
import com.mowmaster.pedestals.Items.Tools.BaseTool;
import com.mowmaster.pedestals.Items.Tools.IPedestalTool;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class AugmentTool_Range extends BaseTool implements IPedestalTool
{
    public AugmentTool_Range(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_AUGMENTS_RANGE.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_AUGMENTS_ROUNDROBIN.get().getDefaultInstance(); }

    @Override
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {

        MutableComponent separator2 = Component.translatable(MODID + ".text.separator.space");
        MutableComponent separator3 = Component.translatable(MODID + ".text.separator.slash");

        if(pedestal.hasRange())
        {
            ItemStack getCurrentAugments = pedestal.currentRangeAugments();
            int currentInsertAmount = getCurrentAugments.getCount();
            int maxInsertAmount = AugmentTieredRange.getAllowedInsertAmount(getCurrentAugments);

            int currentRange = pedestal.getLinkingRange();


            //"[ItemNameThing] Inserted: "
            MutableComponent tierLabel = getCurrentAugments.getDisplayName().copy();
            MutableComponent insertedText = Component.translatable(MODID + ".tool.augment.inserted");
            tierLabel.append(separator2);
            insertedText.withStyle(ChatFormatting.AQUA);
            tierLabel.append(insertedText);
            //"##/##"
            MutableComponent speedAmount = Component.literal(""+currentInsertAmount+"");
            MutableComponent speedMax = Component.literal(""+maxInsertAmount+"");
            speedAmount.append(separator3);
            speedAmount.append(speedMax);
            speedAmount.withStyle(ChatFormatting.WHITE);
            //"ItemNameThing - ##/##"
            tierLabel.append(speedAmount);

            //"Current Linking Range: "
            MutableComponent rangeText = Component.translatable(MODID + ".tool.augment.range");
            rangeText.withStyle(ChatFormatting.YELLOW);
            MutableComponent currentLinkingRange = Component.literal(""+currentRange+"");
            //"Current Transfer Speed: ##/##"
            currentLinkingRange.withStyle(ChatFormatting.WHITE);
            rangeText.append(currentLinkingRange);


            player.displayClientMessage(tierLabel, false);
            player.displayClientMessage(rangeText, false);
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.GOLD,MODID + ".tool.augment.range_not");
        }
    }
}
