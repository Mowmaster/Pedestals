package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.Filters.MowLibBaseFilter;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class BaseFilter extends MowLibBaseFilter
{
    public boolean filterType = false;
    public FilterDirection filterableDirection;

    public BaseFilter(Properties p_41383_, FilterDirection direction) {
        super(p_41383_,direction);
        this.filterableDirection = direction;
    }

    @Override
    public int canAcceptCountItems(MowLibBaseBlockEntity filterableBlockEntity, ItemStack filterStack, int maxSpaceSize, int spaceAvailable, ItemStack itemStackIncoming) {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.ITEMS);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof FilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            FilterRestricted filterRestricted = (FilterRestricted)itemFromInv.getItem();
            return filterRestricted.canAcceptCountItems(filterableBlockEntity,filterStack,itemStackIncoming.getMaxStackSize(),spaceAvailable,itemStackIncoming);
        }

        return Math.min(spaceAvailable, itemStackIncoming.getMaxStackSize());
    }

    @Override
    public int canAcceptCountFluids(MowLibBaseBlockEntity filterableBlockEntity, ItemStack filterStack, int maxSpaceSize, int spaceAvailable, FluidStack incomingFluidStack)
    {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.FLUIDS);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof FilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            FilterRestricted filterRestricted = (FilterRestricted)itemFromInv.getItem();
            return filterRestricted.canAcceptCountFluids(filterableBlockEntity,filterStack,maxSpaceSize,spaceAvailable,incomingFluidStack);
        }

        return Math.min(spaceAvailable, incomingFluidStack.getAmount());
    }

    @Override
    public int canAcceptCountEnergy(MowLibBaseBlockEntity filterableBlockEntity, ItemStack filterStack, int maxSpaceSize, int spaceAvailable, int incomingEnergyAmount)
    {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.ENERGY);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof FilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            FilterRestricted filterRestricted = (FilterRestricted)itemFromInv.getItem();
            return filterRestricted.canAcceptCountEnergy(filterableBlockEntity,filterStack,maxSpaceSize,spaceAvailable,incomingEnergyAmount);
        }

        return Math.min(spaceAvailable, incomingEnergyAmount);
    }

    @Override
    public int canAcceptCountExperience(MowLibBaseBlockEntity filterableBlockEntity, ItemStack filterStack, int maxSpaceSize, int spaceAvailable, int incomingExperienceAmount)
    {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.EXPERIENCE);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof FilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            FilterRestricted filterRestricted = (FilterRestricted)itemFromInv.getItem();
            return filterRestricted.canAcceptCountExperience(filterableBlockEntity,filterStack,maxSpaceSize,spaceAvailable,incomingExperienceAmount);
        }

        return Math.min(spaceAvailable, incomingExperienceAmount);
    }

    @Override
    public int canAcceptCountDust(MowLibBaseBlockEntity filterableBlockEntity, ItemStack filterStack, int maxSpaceSize, int spaceAvailable, DustMagic incomingDust)
    {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filterStack,ItemTransferMode.DUST);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof FilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            FilterRestricted filterRestricted = (FilterRestricted)itemFromInv.getItem();
            return filterRestricted.canAcceptCountDust(filterableBlockEntity,filterStack,maxSpaceSize,spaceAvailable,incomingDust);
        }

        return Math.min(spaceAvailable, incomingDust.getDustAmount());
    }

    @Override
    public void chatDetails(Player player, MowLibBaseBlockEntity pedestal, ItemStack filterStack) {
        if(!filterStack.getItem().equals(DeferredRegisterItems.FILTER_BASE.get()))
        {
            List<String> listed = new ArrayList<>();
            MowLibMessageUtils.messagePlayerChatWithAppend(MODID, player,ChatFormatting.GOLD,filterStack.getDisplayName().getString(), listed);

            //For each Mode
            for (ItemTransferMode mode:ItemTransferMode.values())
            {
                List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,mode);
                if(filterQueue.size()>0)
                {
                    MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,MODID + ".filters.tooltip_filterlist");

                    List<String> enchantList = new ArrayList<>();
                    for(int j=0;j<filterQueue.size();j++) {

                        if(!filterQueue.get(j).isEmpty())
                        {
                            enchantList.add(filterQueue.get(j).getDisplayName().getString() + ", ");
                        }
                    }
                    MowLibMessageUtils.messagePlayerChatWithAppend(MODID, player,ChatFormatting.GRAY,filterStack.getDisplayName().getString(), enchantList);
                }
            }
            /*for(int i=0; i < ItemTransferMode.values().length; i++)
            {
                List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,get);
                if(filterQueue.size()>0)
                {
                    MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,MODID + ".filters.tooltip_filterlist");

                    List<String> enchantList = new ArrayList<>();
                    for(int j=0;j<filterQueue.size();j++) {

                        if(!filterQueue.get(j).isEmpty())
                        {
                            enchantList.add(filterQueue.get(j).getDisplayName().getString() + ", ");
                        }
                    }
                    MowLibMessageUtils.messagePlayerChatWithAppend(MODID, player,ChatFormatting.GRAY,filterStack.getDisplayName().getString(), enchantList);
                }
            }*/
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.DARK_RED,MODID + ".baseItem");
        }
    }
}
