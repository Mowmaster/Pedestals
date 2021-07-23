package com.mowmaster.pedestals.item.pedestalFilters;

import com.mowmaster.pedestals.api.filter.IFilterBase;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;


public class ItemFilterFood extends ItemFilterBase
{
    public ItemFilterFood(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canAcceptItem(PedestalTileEntity pedestal, ItemStack itemStackIn)
    {
        boolean filterBool=getFilterType(pedestal.getFilterInPedestal());
        boolean returner = filterBool;

        if(itemStackIn.isFood())
        {
            returner = !filterBool;
        }

        return returner;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World p_77659_1_, PlayerEntity p_77659_2_, Hand p_77659_3_) {
        //Thankyou past self: https://github.com/Mowmaster/Ensorcelled/blob/main/src/main/java/com/mowmaster/ensorcelled/enchantments/handlers/HandlerAOEMiner.java#L53
        //RayTraceResult result = player.pick(player.getLookVec().length(),0,false); results in MISS type returns
        RayTraceResult result = p_77659_2_.pick(5,0,false);
        if(result != null)
        {
            //Assuming it it hits a block it wont work???
            if(result.getType() == RayTraceResult.Type.MISS)
            {
                if(p_77659_2_.isCrouching())
                {
                    ItemStack itemInHand = p_77659_2_.getHeldItem(p_77659_3_);
                    if(itemInHand.getItem() instanceof IFilterBase)
                    {
                        boolean getCurrentType = getFilterType(itemInHand);
                        setFilterType(itemInHand,!getCurrentType);
                        TranslationTextComponent changed = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterchange");
                        changed.mergeStyle(TextFormatting.GREEN);
                        TranslationTextComponent white = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterwhite");
                        TranslationTextComponent black = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterblack");
                        changed.append((!getCurrentType)?(black):(white));
                        p_77659_2_.sendStatusMessage(changed,true);
                        return ActionResult.resultSuccess(p_77659_2_.getHeldItem(p_77659_3_));
                    }
                    return ActionResult.resultFail(p_77659_2_.getHeldItem(p_77659_3_));
                }
            }
            //Assuming it it hits a block it wont work???
            if(result.getType() == RayTraceResult.Type.BLOCK)
            {
                return ActionResult.resultFail(p_77659_2_.getHeldItem(p_77659_3_));
            }
        }

        return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
    }

    public static void handleItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return getColorFromNBT(itemstack);} else {return -1;}},FOODFILTER);
    }

    public static final Item FOODFILTER = new ItemFilterFood(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "filter/filterfood"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FOODFILTER);
    }
}
