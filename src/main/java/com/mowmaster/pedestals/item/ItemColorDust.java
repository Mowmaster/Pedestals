package com.mowmaster.pedestals.item;

import com.mowmaster.pedestals.pedestals;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemColorDust extends Item {
    public ItemColorDust(Properties builder) {
        super(builder.group(pedestals.ITEM_GROUP));
    }

    public static void handleItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return getColorFromNBT(itemstack);} else {return -1;}},DUST);

    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

            if(playerIn.isCrouching())
            {
                if(playerIn.getHeldItemMainhand().hasTag())
                {
                    if(playerIn.getHeldItemMainhand().getTag().contains(MODID+"color"))
                    {
                        System.out.println(getColorFromNBT(playerIn.getHeldItem(handIn)));
                    }
                }
            }
            else
            {
                if(playerIn.getHeldItemOffhand().getItem().equals(Items.RED_DYE))
                {
                    setColorToNBT(playerIn.getHeldItem(handIn),16711680 );
                }

                if(playerIn.getHeldItemOffhand().getItem().equals(Items.GREEN_DYE))
                {
                    setColorToNBT(playerIn.getHeldItem(handIn),65280 );
                }

                if(playerIn.getHeldItemOffhand().getItem().equals(Items.BLUE_DYE))
                {
                    setColorToNBT(playerIn.getHeldItem(handIn),255 );
                }

                if(playerIn.getHeldItemOffhand().getItem().equals(Items.WHITE_DYE))
                {
                    setColorToNBT(playerIn.getHeldItem(handIn),16777215 );
                }

                if(playerIn.getHeldItemOffhand().getItem().equals(Items.BLACK_DYE))
                {
                    setColorToNBT(playerIn.getHeldItem(handIn),0 );
                }
            }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public void setColorToNBT(ItemStack stack, int color)
    {
        stack.getOrCreateTag().putInt("color",color );
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

    public static boolean getCombineFromNBT(ItemStack stack)
    {
        if(!stack.hasTag())
        {
            return true;
        }
        if(!stack.getTag().contains("combine"))
        {
            return true;
        }

        return stack.getTag().getBoolean("combine");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String sr = "No";
        if(getCombineFromNBT(stack))
        {
            sr="Yes";
        }

        tooltip.add(new TranslationTextComponent("Can Mix With Other Colors: " + sr).applyTextStyle(TextFormatting.LIGHT_PURPLE));
        //tooltip.add(new TranslationTextComponent("mininggadgets.tooltip.item.break_cost", getEnergyCost(stack)).applyTextStyle(TextFormatting.RED));
    }

    public static final Item DUST = new ItemColorDust(new Properties().group(ItemGroup.MATERIALS)).setRegistryName(new ResourceLocation(MODID, "itemdust"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(DUST);
    }
}
