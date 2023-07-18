package com.mowmaster.pedestals.items.tools.augment;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.items.augments.AugmentTieredStorage;
import com.mowmaster.pedestals.items.tools.BaseTool;
import com.mowmaster.pedestals.items.tools.IPedestalTool;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;

public class AugmentTool_Storage extends BaseTool implements IPedestalTool
{
    public AugmentTool_Storage(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_AUGMENTS_STORAGE.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_AUGMENTS_RANGE.get().getDefaultInstance(); }

    @Override
    public void getPedestalDetail(BasePedestalBlockEntity pedestal, Player player) {

        MutableComponent separator2 = Component.translatable(MODID + ".text.separator.space");
        MutableComponent separator3 = Component.translatable(MODID + ".text.separator.slash");

        //Need to show item/fluid/energy/xp/dust(when applicable) changes/maxes

        if(pedestal.hasStorage())
        {
            ItemStack getCurrentAugments = pedestal.currentStorageAugments();
            int currentInsertAmount = getCurrentAugments.getCount();
            int maxInsertAmount = AugmentTieredStorage.getAllowedInsertAmount(getCurrentAugments);
            /*int maxTicksToTransfer = PedestalConfig.COMMON.pedestal_maxTicksToTransfer.get();
            int currentTickReduction = pedestal.getTicksReduced();*/


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

            /*//"Current Transfer Speed: "
            MutableComponent speedText = Component.translatable(MODID + ".tool.augment.speed");
            speedText.withStyle(ChatFormatting.AQUA);
            //"##/##"
            MutableComponent currentSpeedAmount = Component.literal(""+currentTickReduction+"");
            MutableComponent currentSpeedMax = Component.literal(""+maxTicksToTransfer+"");
            currentSpeedAmount.append(separator3);
            currentSpeedAmount.append(currentSpeedMax);
            currentSpeedAmount.withStyle(ChatFormatting.WHITE);
            //"Current Transfer Speed: ##/##"
            speedText.append(currentSpeedAmount);*/


            player.displayClientMessage(tierLabel, false);
            //player.displayClientMessage(speedText, false);
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.DARK_AQUA,MODID + ".tool.augment.storage_not");
        }
    }
}
