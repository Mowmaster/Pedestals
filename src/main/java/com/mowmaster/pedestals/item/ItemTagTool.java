package com.mowmaster.pedestals.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemTagTool extends Item {

    public ItemTagTool() {
        super(new Properties().stacksTo(1).containerItem(TAG).tab(PEDESTALS_TAB));
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(this.getItem());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World p_77659_1_, PlayerEntity p_77659_2_, Hand p_77659_3_) {
        String tags = p_77659_2_.getHeldItemOffhand().getItem().getTags().toString();
        if(p_77659_1_.isClientSide)
        {
            if(!p_77659_2_.getHeldItemOffhand().isEmpty())
            {
                TranslationTextComponent output = new TranslationTextComponent("Tags: ");
                output.append(tags);
                output.withStyle(TextFormatting.WHITE);
                p_77659_2_.sendMessage(output,p_77659_2_.getUniqueID());
            }
        }

        return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
    }

    public static final Item TAG = new ItemTagTool().setRegistryName(new ResourceLocation(MODID, "tagtool"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(TAG);
    }




}
