package com.mowmaster.pedestals.item.pedestalFilters;

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        boolean returner = false;

        if(world.getTileEntity(posPedestal) instanceof PedestalTileEntity)
        {
            PedestalTileEntity pedestal = (PedestalTileEntity)world.getTileEntity(posPedestal);
            ItemStack coin = pedestal.getCoinOnPedestal();
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(coin);
            int range = stackCurrent.size();

            ItemStack itemFromInv = ItemStack.EMPTY;
            itemFromInv = IntStream.range(0,range)//Int Range
                    .mapToObj((stackCurrent)::get)//Function being applied to each interval
                    .filter(itemStack -> itemStack.getItem().equals(itemStackIn.getItem()))
                    .findFirst().orElse(ItemStack.EMPTY);

            if(!itemFromInv.isEmpty())
            {
                returner = true;
            }
        }

        return returner;
    }

    public static final Item ITEMFILTER = new ItemFilterItem(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "filter/item"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITEMFILTER);
    }
}
