package com.mowmaster.pedestals.items.filters;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.items.misc.TagGetterItem;
import com.mowmaster.pedestals.registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;


public class FilterTagMachine extends BaseFilter{
    public FilterTagMachine(Properties p_41383_) {
        super(p_41383_, FilterDirection.NEUTRAL);
    }

    @Override
    public boolean canModeUseInventoryAsFilter(ItemTransferMode mode) {
        switch (mode)
        {
            case ITEMS:         return true;
            case FLUIDS:        return true;
            case ENERGY:        return false;
            case EXPERIENCE:    return false;
            case DUST:          return false;
            default:            return false;
        }
    }

    private static String[] decompose(String p_135833_, char p_135834_) {
        String[] astring = new String[]{"minecraft", p_135833_};
        int i = p_135833_.indexOf(p_135834_);
        if (i >= 0) {
            astring[1] = p_135833_.substring(i + 1, p_135833_.length());
            if (i >= 1) {
                astring[0] = p_135833_.substring(0, i);
            }
        }

        return astring;
    }

    protected ResourceLocation getLocationFromStringName(String input)
    {
        String[] getStringy = decompose(input, ':');
        String one = getStringy[0];
        String two = getStringy[1];

        if(one.contains("["))
        {
            one = getStringy[0].substring(1,one.length());
        }

        if(two.contains("]"))
        {
            two = getStringy[1].substring(0,two.length()-1);
        }

        return new ResourceLocation(one,two);
    }


    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) {
        boolean filterBool = super.canAcceptItems(filter, incomingStack);

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.ITEMS);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof TagGetterItem)
                .filter(itemStack -> ((TagGetterItem)itemStack.getItem()).getSelectedTagString(itemStack) != "")
                .filter(itemStack -> ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation(((TagGetterItem)itemStack.getItem()).getSelectedTagString(itemStack)))).stream().toList().contains(incomingStack.getItem()))
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

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.FLUIDS);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof TagGetterItem)
                .filter(itemStack -> ((TagGetterItem)itemStack.getItem()).getSelectedTagString(itemStack) != "")
                .filter(itemStack -> ForgeRegistries.FLUIDS.tags().getTag(FluidTags.create(new ResourceLocation(((TagGetterItem)itemStack.getItem()).getSelectedTagString(itemStack)))).stream().toList().contains(incomingFluidStack.getFluid()))
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            return filterBool;
        }
        else return !filterBool;
    }

    @Override
    public void chatDetails(Player player, MowLibBaseBlockEntity pedestal, ItemStack filterStack) {
        if(!filterStack.getItem().equals(DeferredRegisterItems.FILTER_BASE.get()))
        {
            MowLibMessageUtils.messagePlayerChatText(player,ChatFormatting.GOLD,filterStack.getDisplayName().getString());

            //For each Mode
            for (ItemTransferMode mode: ItemTransferMode.values())
            {
                List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,mode);
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
            /*for(int i=0;i<4;i++)
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
            }*/
        }
    }
}
