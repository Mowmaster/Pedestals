package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFilterFood extends ItemUpgradeBaseFilter
{
    public ItemUpgradeFilterFood(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {

    }

    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        boolean returner = false;

        if(itemStackIn.isFood())
        {
            returner = true;
        }

        return returner;
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {

    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".chat_name");
        TranslationTextComponent name2 = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.appendString(name2.getString());
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,player.getUniqueID());
    }

    public static final Item FOOD = new ItemUpgradeFilterFood(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterfood"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FOOD);
    }



}
