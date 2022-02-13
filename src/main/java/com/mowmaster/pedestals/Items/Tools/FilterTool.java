package com.mowmaster.pedestals.Items.Tools;

import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class FilterTool extends BaseTool implements IPedestalTool
{
    public FilterTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level world = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack stackInHand = player.getItemInHand(hand);
        //Build Color List from NBT
        HitResult result = player.pick(5,0,false);
        BlockPos pos = new BlockPos(result.getLocation().x,result.getLocation().y,result.getLocation().z);
        if(result.getType().equals(HitResult.Type.MISS))
        {
            if(player.isCrouching())
            {
                if(stackInHand.getItem().equals(DeferredRegisterItems.TOOL_FILTERTOOL.get()))
                {
                    ItemStack newTool = new ItemStack(DeferredRegisterItems.TOOL_TAGTOOL.get());
                    player.setItemInHand(hand, newTool);

                    TranslatableComponent changed = new TranslatableComponent(getDescriptionId() + ".tool_change");
                    changed.withStyle(ChatFormatting.GREEN);
                    player.displayClientMessage(changed,true);
                    return InteractionResultHolder.success(stackInHand);
                }
            }
        }
        else if(result.getType().equals(HitResult.Type.BLOCK))
        {


        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return DeferredRegisterItems.TOOL_FILTERTOOL.get().getDefaultInstance();
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }
}
