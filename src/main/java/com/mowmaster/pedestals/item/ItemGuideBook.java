package com.mowmaster.pedestals.item;

import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemGuideBook extends Item {

    public ItemGuideBook() {
        super(new Properties().maxStackSize(64).group(PEDESTALS_TAB));
    }

    public static final Item DEFAULT = new ItemGuideBook().setRegistryName(new ResourceLocation(MODID, "bookguide"));

    /*@Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

        if(worldIn.isRemote)
        {
            TranslationTextComponent link = new TranslationTextComponent(getTranslationKey() + ".link");
            link.mergeStyle(TextFormatting.WHITE);
            playerIn.sendMessage(link, Util.DUMMY_UUID);
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }*/

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(DEFAULT);
    }




}
