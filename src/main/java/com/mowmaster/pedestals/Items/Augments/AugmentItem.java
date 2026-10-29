package com.mowmaster.pedestals.Items.Augments;

import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.PedestalDataComponents;
import com.mowmaster.pedestals.PedestalReferences;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.transport.PedestalTransportType;
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

// A pedestal augment (1.20's AugmentTiered* and the round robin, render diffuser and no-collide augments). Put one in
// with it in the OFFHAND, right-clicking the pedestal; take them out with the matching augment tool in the offhand,
// left-clicking (sneak for all of a tiered kind). tier is 1-4 for the tiered kinds, 0 for the others.
//
// The particle & render augment (DIFFUSER) has a mode: sneak + right-click with it in hand to switch.
//   0 particles, 1 item, 2 upgrade, 3 particles + item, 4 particles + upgrade, 5 item + upgrade, 6 all three hidden
public class AugmentItem extends Item
{
    private static final String MODID = PedestalReferences.MODID;
    public static final int DIFFUSER_MODES = 7;

    private final AugmentType type;
    private final int tier;

    public AugmentItem(Properties properties, AugmentType type, int tier)
    {
        super(properties);
        this.type = type;
        this.tier = tier;
    }

    public AugmentType getType()
    {
        return type;
    }

    public int getTier()
    {
        return tier;
    }

    // How many of this one can go in a pedestal
    public int getInsertable()
    {
        return switch (type)
        {
            case SPEED -> Config.SPEED_AUGMENTS.get(tier - 1).insertable().getAsInt();
            case CAPACITY -> Config.CAPACITY_AUGMENTS.get(tier - 1).insertable().getAsInt();
            case STORAGE -> Config.STORAGE_AUGMENTS.get(tier - 1).insertable().getAsInt();
            case RANGE -> Config.RANGE_AUGMENTS.get(tier - 1).insertable().getAsInt();
            default -> 1;
        };
    }

    // What one does: ticks taken off (speed) or blocks added (range); 0 for the other kinds
    public int getAmount()
    {
        return switch (type)
        {
            case SPEED -> Config.SPEED_AUGMENTS.get(tier - 1).amount().getAsInt();
            case RANGE -> Config.RANGE_AUGMENTS.get(tier - 1).amount().getAsInt();
            default -> 0;
        };
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
        switch (type)
        {
            case SPEED -> tooltip.accept(Component.translatable(MODID + ".augments_speed_ticksreduced").append(String.valueOf(getAmount())).withStyle(ChatFormatting.YELLOW));
            case RANGE -> tooltip.accept(Component.translatable(MODID + ".augments_range_increase").append(String.valueOf(getAmount())).withStyle(ChatFormatting.YELLOW));
            case CAPACITY, STORAGE -> {
                tooltip.accept(Component.translatable(MODID + (type == AugmentType.CAPACITY ? ".augments_capacity_title" : ".augments_storage_title")).withStyle(ChatFormatting.YELLOW));
                for (PedestalTransportType<?> transportType : PedestalTransportTypes.REGISTRY)
                {
                    int amount = type == AugmentType.CAPACITY ? transportType.getCapacityAugmentBonus(tier) : transportType.getStorageAugmentBonus(tier);
                    if (amount <= 0) continue;
                    // pedestals.augment_amount.<capacity|storage>.<type namespace>.<type path>, e.g. "+%s mB"; plain "+N" if missing
                    String key = MODID + ".augment_amount." + type.key() + "." + transportType.getId().getNamespace() + "." + transportType.getId().getPath();
                    tooltip.accept(Component.literal(" ").append(transportType.getName()).append(": ")
                            .append(Component.translatableWithFallback(key, "+" + amount, amount).withStyle(ChatFormatting.WHITE)));
                }
            }
            case DIFFUSER -> tooltip.accept(Component.translatable(MODID + ".upgrade_tooltip_mode").append(modeName(getDiffuserMode(stack))).withStyle(ChatFormatting.GOLD));
            default -> {}
        }
        tooltip.accept(Component.translatable("item." + MODID + ".augment_" + type.key() + ".tooltip").withStyle(ChatFormatting.GRAY));
        if (type.tiered()) tooltip.accept(Component.translatable(MODID + ".augments_insertable").append(String.valueOf(getInsertable())).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
