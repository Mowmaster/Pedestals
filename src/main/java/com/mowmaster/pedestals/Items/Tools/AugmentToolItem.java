package com.mowmaster.pedestals.Items.Tools;

import com.mowmaster.pedestals.Items.Augments.AugmentType;
import com.mowmaster.pedestals.Items.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

// One per augment kind (1.20's AugmentTool_*). In the OFFHAND, left-click a pedestal: take that kind of augment out
// (sneak: all of a tiered kind). In the main hand, right-click a pedestal: what it has of that kind, in chat.
// Sneak + right-click the air: switch to the next kind's tool. It's also the base of that kind's augment recipes,
// and comes back out of the crafting grid.
public class AugmentToolItem extends PedestalToolItem
{
    private final AugmentType type;

    public AugmentToolItem(Properties properties, AugmentType type)
    {
        super(properties);
        this.type = type;
    }

    public AugmentType getType()
    {
        return type;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        if (!player.isShiftKeyDown()) return super.use(level, player, hand);
        if (!level.isClientSide())
        {
            Item next = ItemRegistry.augmentTool(type.next());
            player.setItemInHand(hand, new ItemStack(next));
            player.sendOverlayMessage(Component.translatable(next.getDescriptionId()));
        }
        return InteractionResult.SUCCESS;
    }

    // Stays in the crafting grid when used in a recipe (NeoForge's stack-aware version of craftRemainder)
    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance)
    {
        return new ItemStackTemplate(this);
    }
}
