package com.mowmaster.pedestals.Items.WorkCards;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.PedestalDataComponents;
import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.api.upgrade.IWorkCard;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Shared work card behaviour (1.20's MowLib WorkCardBase). Right-click blocks to select them, right-click the air to
// clear the card. Put it in a pedestal from the OFFHAND (right-click), take it out with the work tool in the
// offhand (left-click). The positions are the pedestals:work_positions component.
public abstract class WorkCardItem extends Item implements IWorkCard
{
    protected static final String MODID = PedestalReferences.MODID;

    protected WorkCardItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    public static List<BlockPos> getStoredPositions(ItemStack card)
    {
        return card.getOrDefault(PedestalDataComponents.WORK_POSITIONS.get(), List.of());
    }

    protected static void setStoredPositions(ItemStack card, List<BlockPos> positions)
    {
        if (positions.isEmpty()) card.remove(PedestalDataComponents.WORK_POSITIONS.get());
        else card.set(PedestalDataComponents.WORK_POSITIONS.get(), List.copyOf(positions));
    }

    // What clicking this block does to the card. Returns the message to show (null = nothing happened).
    protected abstract Component select(Level level, ItemStack card, BlockPos pos);

    // NeoForge runs onItemUseFirst before the clicked block's own right-click, so a work card clicked on a pedestal
    // selects it instead of being put in it (1.20 worked around this in the pedestal block)
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        Player player = context.getPlayer();
        if (player == null || context.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        Level level = context.getLevel();
        if (!level.isClientSide())
        {
            BlockPos pos = context.getClickedPos().immutable();
            Component message = select(level, stack, pos);
            if (message != null)
            {
                player.sendOverlayMessage(message);
                if (level instanceof ServerLevel serverLevel)
                {
                    serverLevel.sendParticles(new DustParticleOptions(0x00C800, 1.0F), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    // Right-click the air: clear the card
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        ItemStack card = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || getStoredPositions(card).isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide())
        {
            setStoredPositions(card, List.of());
            player.sendOverlayMessage(Component.translatable(MODID + ".workcard_blockpos_clear"));
        }
        return InteractionResult.SUCCESS;
    }

    // ---- For the subclasses ----

    // In range of the pedestal on every axis
    protected static boolean inRange(BasePedestalBlockEntity pedestal, BlockPos pos, int range)
    {
        return pedestal.isInRange(pos, range);
    }

    // Adds pos, or removes it if it's already there. true = added.
    protected static boolean toggle(ItemStack card, BlockPos pos)
    {
        List<BlockPos> positions = new ArrayList<>(getStoredPositions(card));
        boolean added = !positions.remove(pos);
        if (added) positions.add(pos);
        setStoredPositions(card, positions);
        return added;
    }

    protected static Component formatPos(BlockPos pos)
    {
        return Component.literal(pos.getX() + "x " + pos.getY() + "y " + pos.getZ() + "z").withStyle(ChatFormatting.GRAY);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        List<BlockPos> positions = getStoredPositions(stack);
        if (positions.isEmpty()) return;
        tooltip.accept(Component.translatable(MODID + ".workcard_tooltip_blockpos_title").withStyle(ChatFormatting.GOLD));
        for (BlockPos pos : positions) tooltip.accept(formatPos(pos));
    }
}
