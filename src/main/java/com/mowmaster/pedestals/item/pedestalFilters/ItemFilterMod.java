package com.mowmaster.pedestals.item.pedestalFilters;

import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;


public class ItemFilterMod extends ItemFilterBase
{
    public ItemFilterMod(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public int canAcceptCount(PedestalTileEntity pedestal, World world, BlockPos posPedestal, ItemStack inPedestal, ItemStack itemStackIncoming) {

        ItemStack filter = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof ItemFilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            if(inPedestal.isEmpty())
            {
                List<ItemStack> stackCurrentRestricted = readFilterQueueFromNBT(itemFromInv);
                int rangeRestricted = stackCurrentRestricted.size();
                int count = 0;
                int maxIncomming = itemStackIncoming.getMaxStackSize();
                for(int i=0;i<rangeRestricted;i++)
                {
                    count +=stackCurrent.get(i).getCount();
                    if(count>=maxIncomming)break;
                }

                return (count>0)?((count>maxIncomming)?(maxIncomming):(count)):(1);
            }

            return 0;
        }

        return super.canAcceptCount(pedestal, world, posPedestal, inPedestal, itemStackIncoming);
    }

    @Override
    public boolean canAcceptItem(PedestalTileEntity pedestal, ItemStack itemStackIn)
    {
        boolean filterBool=getFilterType(pedestal.getFilterInPedestal());
        boolean returner = filterBool;

        ItemStack filter = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem().getRegistryName().getNamespace()==itemStackIn.getItem().getRegistryName().getNamespace())
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            returner = !filterBool;
        }

        return returner;
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        TranslationTextComponent filterList = new TranslationTextComponent(filterStack.getDisplayName().getString());
        filterList.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(filterList, Util.DUMMY_UUID);

        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack);
        if(filterQueue.size()>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterlist");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant, Util.DUMMY_UUID);

            for(int i=0;i<filterQueue.size();i++) {

                if(!filterQueue.get(i).isEmpty())
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(filterQueue.get(i).getItem().getRegistryName().getNamespace());
                    enchants.mergeStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants, Util.DUMMY_UUID);
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        boolean filterType = getFilterType(stack);
        TranslationTextComponent filterList = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filtertype");
        TranslationTextComponent white = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterwhite");
        TranslationTextComponent black = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterblack");
        filterList.append((filterType)?(black):(white));
        filterList.mergeStyle(TextFormatting.GOLD);
        tooltip.add(filterList);

        List<ItemStack> filterQueue = readFilterQueueFromNBT(stack);
        if(filterQueue.size()>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterlist");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            tooltip.add(enchant);

            for(int i=0;i<filterQueue.size();i++) {

                if(!filterQueue.get(i).isEmpty())
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(filterQueue.get(i).getItem().getRegistryName().getNamespace());
                    enchants.mergeStyle(TextFormatting.GRAY);
                    tooltip.add(enchants);
                }
            }
        }
    }

    public static void handleItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return getColorFromNBT(itemstack);} else {return -1;}},MODFILTER);
    }

    public static final Item MODFILTER = new ItemFilterMod(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "filter/filtermod"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(MODFILTER);
    }
}
