package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.FoodOnAStickItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;


import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.fluids.FluidStack;

public class FilterFood extends BaseFilter{
    public FilterFood(Properties p_41383_) {
        super(p_41383_, IPedestalFilter.FilterDirection.INSERT);
    }

    @Override
    public boolean canModeUseInventoryAsFilter(int mode) {
        switch (mode)
        {
            case 0: return true;
            case 1: return false;
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
                .filter(itemStack -> itemStack.getItem().getFoodProperties().getNutrition()>0)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            return filterBool;
        }
        else return !filterBool;

    }
}
