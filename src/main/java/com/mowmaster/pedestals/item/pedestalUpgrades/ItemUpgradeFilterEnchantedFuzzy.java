package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

//Filters by number of enchants on an item
public class ItemUpgradeFilterEnchantedFuzzy extends ItemUpgradeBaseFilter
{
    public ItemUpgradeFilterEnchantedFuzzy(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptAdvanced() {return true;}

    public void updateAction(PedestalTileEntity pedestal)
    {

    }

    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        boolean returner = false;
        if(itemStackIn.isEnchanted() || itemStackIn.getItem().equals(Items.ENCHANTED_BOOK))
        {
            if(world.getTileEntity(posPedestal) instanceof PedestalTileEntity)
            {
                PedestalTileEntity pedestal = (PedestalTileEntity)world.getTileEntity(posPedestal);
                ItemStack coin = pedestal.getCoinOnPedestal();
                List<ItemStack> stackCurrent = readFilterQueueFromNBT(coin);
                if(!(stackCurrent.size()>0))
                {
                    stackCurrent = buildFilterQueue(pedestal);
                    writeFilterQueueToNBT(coin,stackCurrent);
                }

                int range = stackCurrent.size();
                Map<Enchantment, Integer> mapIncomming = EnchantmentHelper.getEnchantments(itemStackIn);

                for(Map.Entry<Enchantment, Integer> entry : mapIncomming.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    ItemStack itemFromInv = ItemStack.EMPTY;
                    itemFromInv = IntStream.range(0,range)//Int Range
                            .mapToObj((stackCurrent)::get)//Function being applied to each interval
                            //Check to make sure filter item is enchanted
                            .filter(itemStack -> itemStack.isEnchanted() || itemStack.getItem().equals(Items.ENCHANTED_BOOK))
                            //Check if filter item has any enchant that the item in the pedestal has
                            .filter(itemStack -> EnchantmentHelper.getEnchantments(itemStack).containsKey(enchantment))
                            .findFirst().orElse(ItemStack.EMPTY);

                    if(!itemFromInv.isEmpty())
                    {
                        return true;
                    }
                }
            }
        }
        /*int countAnyMatches = 0;
        if(itemStackIn.isEnchanted() || itemStackIn.getItem().equals(Items.ENCHANTED_BOOK))
        {
            BlockPos posInventory = getPosOfBlockBelow(world, posPedestal, 1);

            LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posPedestal),true);
            if(cap.isPresent())
            {
                IItemHandler handler = cap.orElse(null);
                if(handler != null)
                {
                    int range = handler.getSlots();
                    ItemStack itemFromInv = ItemStack.EMPTY;
                    Map<Enchantment, Integer> mapIncomming = EnchantmentHelper.getEnchantments(itemStackIn);

                    for(Map.Entry<Enchantment, Integer> entry : mapIncomming.entrySet()) {
                        Enchantment enchantment = entry.getKey();
                        itemFromInv = IntStream.range(0,range)//Int Range
                                .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                                //Check to make sure filter item is enchanted
                                .filter(itemStack -> itemStack.isEnchanted() || itemStack.getItem().equals(Items.ENCHANTED_BOOK))
                                //Check if filter item has any enchant that the item in the pedestal has
                                .filter(itemStack -> EnchantmentHelper.getEnchantments(itemStack).containsKey(enchantment))
                                .findFirst().orElse(ItemStack.EMPTY);

                        if(!itemFromInv.isEmpty())
                        {
                            return true;
                        }
                    }
                }
            }
        }*/

        return false;
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {

    }

    @Override
    public void onPedestalNeighborChanged(PedestalTileEntity pedestal) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<ItemStack> stackIn = buildFilterQueue(pedestal);
        if(filterQueueSize(coin)>0)
        {
            List<ItemStack> stackCurrent = readFilterQueueFromNBT(coin);
            if(!doesFilterAndQueueMatch(stackIn,stackCurrent))
            {
                writeFilterQueueToNBT(coin,stackIn);
            }
        }
        else
        {
            writeFilterQueueToNBT(coin,stackIn);
        }
    }

    public static final Item ENCHANTEDFUZZY = new ItemUpgradeFilterEnchantedFuzzy(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedfuzzy"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ENCHANTEDFUZZY);
    }
}
