package com.mowmaster.pedestals.item.pedestalUpgrades;


import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import java.util.Map;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

//Filters by number of enchants on an item
public class ItemUpgradeFilterEnchantedFuzzyBlacklist extends ItemUpgradeBaseFilter
{
    public ItemUpgradeFilterEnchantedFuzzyBlacklist(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {

    }

    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        boolean returner = true;
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
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public void upgradeAction(World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {

    }

    public static final Item ENCHANTEDFUZZYB = new ItemUpgradeFilterEnchantedFuzzyBlacklist(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/filterenchantedfuzzyb"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ENCHANTEDFUZZYB);
    }
}
