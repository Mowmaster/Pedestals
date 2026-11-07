package com.mowmaster.ironpedestals.Blocks.Pedestal;

import com.mowmaster.ironpedestals.Config;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

// A pedestal's item: the block item, with what its tier does in the tooltip (read from the config, so it shows the
// current values). The config is a LOCAL one, so on a server this is the player's own copy of it.
public class PedestalBlockItem extends BlockItem
{
    private static final String KEY = IronPedestalReferences.MODID + ".pedestal_tier";

    private final PedestalTier tier;

    public PedestalBlockItem(BasePedestalBlock block, Properties properties)
    {
        super(block, properties);
        this.tier = block.getTier();
    }

    public PedestalTier getTier()
    {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        Config.TierValues values = tier.config();
        int ticks = Math.max(1, Config.TICKS_TO_TRANSFER.getAsInt() - values.ticksReduced().getAsInt());

        tooltip.accept(Component.translatable(KEY, tier.tier()).withStyle(ChatFormatting.GOLD));
        tooltip.accept(line("speed", ticks));
        tooltip.accept(line("rate", values.itemRate().getAsInt(), values.fluidRate().getAsInt(),
                values.energyRate().getAsInt(), values.xpRate().getAsInt()));
        tooltip.accept(line("items", values.itemStacks().getAsInt(), values.itemStackCapacity().getAsInt()));
        tooltip.accept(line("fluids", values.fluidTanks().getAsInt(), values.fluidCapacity().getAsInt()));
        tooltip.accept(line("energy", values.energyCapacity().getAsInt()));
        tooltip.accept(line("experience", values.xpCapacity().getAsInt()));
        int linkRange = values.linkRange().getAsInt();
        int workRange = values.workRange().getAsInt();
        // Tier 0 has separate link and work card ranges; the other tiers have one range for both
        if (linkRange == workRange) tooltip.accept(line("range", linkRange));
        else tooltip.accept(line("ranges", linkRange, workRange));
    }

    // ironpedestals.pedestal_tier.<name> with the numbers filled in ("%,d": thousands separators, e.g. 100,000)
    private static Component line(String name, int... numbers)
    {
        Object[] args = new Object[numbers.length];
        for (int index = 0; index < numbers.length; index++) args[index] = String.format("%,d", numbers[index]);
        return Component.translatable(KEY + "." + name, args).withStyle(ChatFormatting.GRAY);
    }
}
