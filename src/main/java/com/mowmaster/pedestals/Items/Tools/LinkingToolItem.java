package com.mowmaster.pedestals.Items.Tools;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.PedestalDataComponents;
import com.mowmaster.pedestals.PedestalReferences;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

// Sneak + right-click a pedestal to select it, then sneak + right-click another pedestal to link them
// (doing it again removes the link). Sneak + right-click anything else clears the selection.
//   Linking tool:           the second pedestal SENDS to the first (the first is the receiver)
//   Backwards linking tool: the first pedestal SENDS to the second
// Holding a selected tool in the offhand while placing a pedestal links it straight away (BasePedestalBlock#setPlacedBy).
// The selection is the pedestals:linked_pos component; the glint shows something is selected (1.20 used an enchantment).
public class LinkingToolItem extends Item
{
    private final boolean backwards;

    public LinkingToolItem(Properties properties, boolean backwards)
    {
        super(properties.stacksTo(1));
        this.backwards = backwards;
    }

    public boolean isBackwards()
    {
        return backwards;
    }

    public static BlockPos getSelected(ItemStack tool)
    {
        return tool.get(PedestalDataComponents.LINKED_POS.get());
    }

    public static void select(ItemStack tool, BlockPos pos)
    {
        tool.set(PedestalDataComponents.LINKED_POS.get(), pos);
        tool.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static void clear(ItemStack tool)
    {
        tool.remove(PedestalDataComponents.LINKED_POS.get());
        tool.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    // Links sender -> receiver using the tool's selection and the pedestal at pos. Returns false if nothing happened.
    public boolean linkWith(Level level, Player player, ItemStack tool, BlockPos pos)
    {
        BlockPos selected = getSelected(tool);
        if (selected == null) return false;
        BlockPos senderPos = backwards ? selected : pos;
        BlockPos receiverPos = backwards ? pos : selected;
        if (!(level.getBlockEntity(senderPos) instanceof BasePedestalBlockEntity sender)) return false;

        BasePedestalBlockEntity.LinkResult result = sender.toggleLink(receiverPos);
        player.sendOverlayMessage(result.message(backwards));
        if (result.changed()) clear(tool);
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;

        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack tool = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof BasePedestalBlockEntity))
        {
            if (getSelected(tool) != null)
            {
                clear(tool);
                player.sendOverlayMessage(Component.translatable(PedestalReferences.MODID + ".tool_link_cleared"));
            }
            return InteractionResult.SUCCESS;
        }

        if (getSelected(tool) == null)
        {
            select(tool, pos);
            player.sendOverlayMessage(Component.translatable(PedestalReferences.MODID
                    + (backwards ? ".tool_link_start_backwardslinkingtool" : ".tool_link_start_linkingtool")).withStyle(ChatFormatting.AQUA));
        }
        else
        {
            linkWith(level, player, tool, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        BlockPos selected = getSelected(stack);
        tooltip.accept(selected == null
                ? Component.translatable(PedestalReferences.MODID + ".tool_tip_block_unselected").withStyle(ChatFormatting.GRAY)
                : Component.translatable(PedestalReferences.MODID + ".tool_tip_block_selected")
                        .append(selected.getX() + ", " + selected.getY() + ", " + selected.getZ()).withStyle(ChatFormatting.GRAY));
    }
}
