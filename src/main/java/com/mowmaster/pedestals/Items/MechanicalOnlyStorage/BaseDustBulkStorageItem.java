package com.mowmaster.pedestals.Items.MechanicalOnlyStorage;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.BaseDustDropItem;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibTooltipUtils;
import com.mowmaster.mowlib.api.IDustStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class BaseDustBulkStorageItem extends BaseDustDropItem {
    public BaseDustBulkStorageItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        DustMagic stored = DustMagic.getDustMagicInItemStack(p_41421_);
        if(!stored.isEmpty())
        {
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.translatable(MODID + ".bulkstorage_dust"), ChatFormatting.GOLD);
            MowLibTooltipUtils.addTooltipMessageWithStyle(p_41423_,Component.literal(MowLibColorReference.getColorName(stored.getDustColor()) + ": " + stored.getDustAmount()), ChatFormatting.WHITE);
        }
    }
}
