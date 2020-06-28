package com.mowmaster.pedestals.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.SuspiciousStewRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemColorPallet extends Item {
    public ItemColorPallet(Properties builder) {
        super(builder.group(PEDESTALS_TAB));
    }

    //Will Need Custom Recipe Handler to make this item: SuspiciousStewRecipe

    public static void handleItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return getColorFromNBT(itemstack);} else {return -1;}},COLORPALLET);
    }

    public static int getColorFromNBT(ItemStack stack)
    {
        if(!stack.hasTag())
        {
            return 0;
        }
        if(!stack.getTag().contains("color"))
        {
            return 0;
        }
        return stack.getTag().getInt("color");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public static final Item COLORPALLET_DEFAULT = new ItemColorPallet(new Properties().group(ItemGroup.MATERIALS)).setRegistryName(new ResourceLocation(MODID, "itemcolorpalletdefault"));
    public static final Item COLORPALLET = new ItemColorPallet(new Properties().group(ItemGroup.MATERIALS)).setRegistryName(new ResourceLocation(MODID, "itemcolorpallet"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(COLORPALLET_DEFAULT);
        event.getRegistry().register(COLORPALLET);
    }
}
