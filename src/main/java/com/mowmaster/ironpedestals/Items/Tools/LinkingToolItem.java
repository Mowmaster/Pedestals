package com.mowmaster.ironpedestals.Items.Tools;

import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import com.mowmaster.ironpedestals.Items.PedestalDataComponents;
import com.mowmaster.mowlib.client.MowLibKeyMappings;
import com.mowmaster.mowlib.keys.IActivatable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

// The linking tool (the only one now; the old backwards tool is an alias of it, see ItemRegistry).
//   Right-click a pedestal:          save it for one link (used up by the next link)
//   Sneak + right-click a pedestal:  save it for many links, until sneak + right-clicked again (the same pedestal or
//                                    the air clears it; another pedestal is saved instead)
//   Right-click another pedestal with one saved: link them (again removes the link). Which way depends on the mode:
//     Send To      (default)  the saved pedestal sends to the clicked one
//     Receive From            the clicked pedestal sends to the saved one
//   The activation key (MowLib's, V by default) switches the mode (IActivatable; mowlib:activated = Receive From).
// While it's held, link beams show between pedestals, and from the saved pedestal to the tool (ClientSide/PedestalBeams).
// Holding it in the OFFHAND with a saved pedestal while placing a pedestal links the new one (BasePedestalBlock).
// The saved pedestal is the ironpedestals:linked_pos component, ironpedestals:link_sticky marks a many-links save,
// and the glint shows something is saved.
public class LinkingToolItem extends Item implements IActivatable
{
    private static final String MODID = IronPedestalReferences.MODID;

    public LinkingToolItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    // ---- What's saved ----

    public static @Nullable BlockPos getSelected(ItemStack tool)
    {
        return tool.get(PedestalDataComponents.LINKED_POS.get());
    }

    public static boolean isSticky(ItemStack tool)
    {
        return tool.has(PedestalDataComponents.LINK_STICKY.get());
    }

    // Receive From mode (the activation key's on/off state)
    public static boolean isReceiving(ItemStack tool)
    {
        return IActivatable.isActive(tool);
    }

    public static void select(ItemStack tool, BlockPos pos, boolean sticky)
    {
        tool.set(PedestalDataComponents.LINKED_POS.get(), pos.immutable());
        if (sticky) tool.set(PedestalDataComponents.LINK_STICKY.get(), Unit.INSTANCE);
        else tool.remove(PedestalDataComponents.LINK_STICKY.get());
        tool.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static void clear(ItemStack tool)
    {
        tool.remove(PedestalDataComponents.LINKED_POS.get());
        tool.remove(PedestalDataComponents.LINK_STICKY.get());
        tool.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    // ---- Linking ----

    // Links the saved pedestal and the one at pos, the way the mode says (again: removes the link). A one-link save is
    // used up when something changed. Returns false if nothing is saved.
    public boolean linkWith(Level level, Player player, ItemStack tool, BlockPos pos)
    {
        BlockPos selected = getSelected(tool);
        if (selected == null) return false;
        boolean receiving = isReceiving(tool);
        BlockPos senderPos = receiving ? pos : selected;
        BlockPos receiverPos = receiving ? selected : pos;
        if (!(level.getBlockEntity(senderPos) instanceof BasePedestalBlockEntity sender))
        {
            // The saved pedestal was broken
            clear(tool);
            player.sendOverlayMessage(Component.translatable(MODID + ".tool_link_cleared"));
            return true;
        }
        BasePedestalBlockEntity.LinkResult result = sender.toggleLink(receiverPos);
        player.sendOverlayMessage(result.message(receiving));
        if (result.changed() && !isSticky(tool)) clear(tool);
        return true;
    }

    // Clicking a block
    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        ItemStack tool = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        BlockPos selected = getSelected(tool);

        // Not a pedestal: sneaking clears the save, otherwise nothing happens
        if (!(level.getBlockEntity(pos) instanceof BasePedestalBlockEntity))
        {
            if (!player.isShiftKeyDown() || selected == null) return InteractionResult.PASS;
            if (!level.isClientSide()) clearWithMessage(player, tool);
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown())
        {
            // Sneak: save for many links, or clear if this pedestal is already saved that way
            if (selected != null && isSticky(tool) && selected.equals(pos)) clearWithMessage(player, tool);
            else saveWithMessage(player, tool, pos, true);
        }
        else if (selected == null)
        {
            saveWithMessage(player, tool, pos, false);
        }
        else if (selected.equals(pos))
        {
            // The saved pedestal itself: a one-link save is cancelled; a pedestal can't link to itself
            if (isSticky(tool)) player.sendOverlayMessage(BasePedestalBlockEntity.LinkResult.SELF.message(false));
            else clearWithMessage(player, tool);
        }
        else
        {
            linkWith(level, player, tool, pos);
        }
        return InteractionResult.SUCCESS;
    }

    // Sneak + right-click the air: clear the save
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        ItemStack tool = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || getSelected(tool) == null) return super.use(level, player, hand);
        if (!level.isClientSide()) clearWithMessage(player, tool);
        return InteractionResult.SUCCESS;
    }

    private static void saveWithMessage(Player player, ItemStack tool, BlockPos pos, boolean sticky)
    {
        select(tool, pos, sticky);
        player.sendOverlayMessage(Component.translatable(MODID + (sticky ? ".tool_link_saved_sticky" : ".tool_link_saved_once"),
                pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.AQUA));
    }

    private static void clearWithMessage(Player player, ItemStack tool)
    {
        clear(tool);
        player.sendOverlayMessage(Component.translatable(MODID + ".tool_link_cleared"));
    }

    // ---- The activation key: Send To <-> Receive From ----

    @Override
    public boolean canActivate(ItemStack stack)
    {
        return true;
    }

    // Runs on the server. toggle() flips mowlib:activated, plays a sound and shows the message (the item's name is %s).
    @Override
    public void activate(ItemStack stack, ServerPlayer player)
    {
        IActivatable.toggle(stack, player, MODID + ".tool_link_mode_receive", MODID + ".tool_link_mode_send");
    }

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(MODID + (isReceiving(stack) ? ".tool_tip_link_receive" : ".tool_tip_link_send")).withStyle(ChatFormatting.GOLD));
        BlockPos selected = getSelected(stack);
        tooltip.accept(selected == null
                ? Component.translatable(MODID + ".tool_tip_block_unselected").withStyle(ChatFormatting.GRAY)
                : Component.translatable(MODID + (isSticky(stack) ? ".tool_tip_block_selected_sticky" : ".tool_tip_block_selected"),
                        selected.getX(), selected.getY(), selected.getZ()).withStyle(ChatFormatting.GRAY));
        // Component.keybind: the key's current name (whatever the player bound it to)
        tooltip.accept(Component.translatable(MODID + ".tool_tip_link_howto", Component.keybind(MowLibKeyMappings.ACTIVATE_NAME)).withStyle(ChatFormatting.DARK_GRAY));
    }
}
