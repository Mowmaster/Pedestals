package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.PedestalUtils.PedestalModesAndTypes;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.world.item.Item.Properties;

public class FilterTag extends BaseFilter{
    public FilterTag(Properties p_41383_) {
        super(p_41383_, IPedestalFilter.FilterDirection.INSERT);
    }

    @Override
    public boolean canModeUseInventoryAsFilter(int mode) {
        switch (mode)
        {
            case 0: return true;
            case 1: return true;
            case 2: return false;
            case 3: return false;
            case 4: return false;
            default: return false;
        }
    }

    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) {
        boolean filterBool = super.canAcceptItems(filter, incomingStack);

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,0);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation(itemStack.getDisplayName().getString()))).stream().toList().contains(incomingStack.getItem()))
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            return filterBool;
        }
        else return !filterBool;

    }

    @Override
    public boolean canAcceptFluids(ItemStack filter, FluidStack incomingFluidStack) {
        boolean filterBool = super.canAcceptFluids(filter, incomingFluidStack);

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,1);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> ForgeRegistries.FLUIDS.tags().getTag(FluidTags.create(new ResourceLocation(itemStack.getDisplayName().getString()))).stream().toList().contains(incomingFluidStack.getFluid()))
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            return filterBool;
        }
        else return !filterBool;
    }

    @Override
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        if(!filterStack.getItem().equals(DeferredRegisterItems.FILTER_BASE.get()))
        {
            MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.GOLD,filterStack.getDisplayName().getString());

            //For each Mode
            for(int i=0;i<4;i++)
            {
                List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,i);
                if(filterQueue.size()>0)
                {
                    MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,MODID + ".filters.tooltip_filterlist");

                    for(int j=0;j<filterQueue.size();j++) {

                        if(!filterQueue.get(j).isEmpty())
                        {
                            MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.GRAY,filterQueue.get(j).getDisplayName().getString());
                        }
                    }
                }
            }
        }
    }
}
