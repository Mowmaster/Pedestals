package com.mowmaster.pedestals.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
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

public class ItemDevTool extends Item {

    public ItemDevTool() {
        super(new Properties().maxStackSize(1).containerItem(DEV).group(PEDESTALS_TAB));
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

        if(!p_77659_1_.isRemote)
        {
            if(!p_77659_2_.getHeldItemOffhand().isEmpty())
            {
                //int getAmp = PotionUtils.getEffectsFromStack(p_77659_2_.getHeldItemOffhand()).get(0).getAmplifier();
                //TranslationTextComponent name = new TranslationTextComponent(""+getAmp+"");
                TranslationTextComponent name = new TranslationTextComponent(""+p_77659_2_.getHeldItemOffhand().getTag().toString()+"");
                name.mergeStyle(TextFormatting.GOLD);
                p_77659_2_.sendMessage(name,p_77659_2_.getUniqueID());
            }
        }

        return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
    }

    public static final Item DEV = new ItemDevTool().setRegistryName(new ResourceLocation(MODID, "devtool"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        //event.getRegistry().register(DEV);
    }




}
