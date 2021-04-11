package com.mowmaster.pedestals.item.pedestalFilters;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;


public class ItemFilterItem extends ItemFilterBase
{
    public ItemFilterItem(Properties builder) {super(builder.group(PEDESTALS_TAB));}

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
                .filter(itemStack -> itemStack.getItem().equals(itemStackIn.getItem()))
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            returner = !filterBool;
        }

        return returner;
    }

    public static void handleItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1){return getColorFromNBT(itemstack);} else {return -1;}},ITEMFILTER);
    }

    public static final Item ITEMFILTER = new ItemFilterItem(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "filter/filteritem"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITEMFILTER);
    }
}
