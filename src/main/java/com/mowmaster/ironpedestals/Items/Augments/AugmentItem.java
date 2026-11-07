package com.mowmaster.ironpedestals.Items.Augments;

import com.mowmaster.ironpedestals.Items.PedestalDataComponents;
import com.mowmaster.ironpedestals.IronPedestalReferences;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

// A pedestal augment (1.20's round robin, render diffuser and no-collide augments; the tiered speed, capacity, storage
// and range augments became pedestal tiers). Put one in with it in the OFFHAND, right-clicking the pedestal; take it
// out with the matching augment tool in the offhand, left-clicking. One of each kind per pedestal.
//
// The particle & render augment (DIFFUSER) has a mode: sneak + right-click with it in hand to switch.
//   0 particles, 1 item, 2 upgrade, 3 particles + item, 4 particles + upgrade, 5 item + upgrade, 6 all three hidden
public class AugmentItem extends Item
{
    private static final String MODID = IronPedestalReferences.MODID;
    public static final int DIFFUSER_MODES = 7;

    private final AugmentType type;

    public AugmentItem(Properties properties, AugmentType type)
    {
        super(properties);
        this.type = type;
    }

    public AugmentType getType()
    {
        return type;
    }

    // ---- Particle & render augment modes ----

    public static int getDiffuserMode(ItemStack stack)
    {
        return stack.getOrDefault(PedestalDataComponents.DIFFUSER_MODE.get(), 0);
    }

    public static boolean hidesParticles(int mode)
    {
        return mode == 0 || mode == 3 || mode == 4 || mode == 6;
    }

    public static boolean hidesItem(int mode)
    {
        return mode == 1 || mode == 3 || mode == 5 || mode == 6;
    }

    public static boolean hidesUpgrade(int mode)
    {
        return mode == 2 || mode == 4 || mode == 5 || mode == 6;
    }

    private static Component modeName(int mode)
    {
        String[] keys = {"particle", "item", "upgrade", "pi", "pu", "iu", "piu"};
        return Component.translatable(MODID + ".mode_augment_" + keys[mode]);
    }

    // Sneak + right-click: next mode (particle & render augment only)
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        if (type != AugmentType.DIFFUSER || !player.isShiftKeyDown()) return super.use(level, player, hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        int mode = (getDiffuserMode(stack) + 1) % DIFFUSER_MODES;
        stack.set(PedestalDataComponents.DIFFUSER_MODE.get(), mode);
        player.sendOverlayMessage(Component.translatable(MODID + ".mode_changed").append(modeName(mode)).withStyle(ChatFormatting.GOLD));
        return InteractionResult.SUCCESS;
    }

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (type == AugmentType.DIFFUSER)
        {
            tooltip.accept(Component.translatable(MODID + ".upgrade_tooltip_mode").append(modeName(getDiffuserMode(stack))).withStyle(ChatFormatting.GOLD));
        }
        tooltip.accept(Component.translatable("item." + MODID + ".augment_" + type.key() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
}
