package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.Filters.IPedestalFilter;
import com.mowmaster.mowlib.MowLibUtils.MowLibFluidUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class FilterBlocksByClickFuzzy extends BaseFilter {
    public FilterBlocksByClickFuzzy(Properties p_41383_) {
        super(p_41383_, FilterDirection.NEUTRAL);
    }

    @Override
    public boolean canModeUseInventoryAsFilter(ItemTransferMode mode) {
        return false;
    }

    @Override
    public boolean canSetFilterType(ItemTransferMode mode) {
        return true;
    }

    @Override
    public boolean canSetFilterMode(ItemTransferMode mode) {
        return true;
    }

    @Override
    public boolean showFilterDirection()
    {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        ItemStack itemInMainhand = p_41433_.getMainHandItem();
        ItemStack itemInOffhand = p_41433_.getOffhandItem();
        HitResult result = p_41433_.pick(5.0D, 0.0F, false);
        if (!p_41432_.isClientSide()) {
            if (itemInOffhand.getItem() instanceof IPedestalFilter && !(itemInMainhand.getItem() instanceof IPedestalFilter)) {
                if (result.getType().equals(HitResult.Type.MISS)) {
                    if (p_41433_.isCrouching()) {
                        setFilterMode(p_41433_, itemInOffhand, InteractionHand.OFF_HAND);
                    } else {
                        setFilterType(p_41433_, itemInOffhand);
                    }
                } else if (result.getType().equals(HitResult.Type.BLOCK) && p_41433_.isCrouching()) {
                    UseOnContext context = new UseOnContext(p_41433_, p_41434_, (BlockHitResult)result);
                    BlockHitResult res = new BlockHitResult(context.getClickLocation(), context.getHorizontalDirection(), context.getClickedPos(), false);
                    BlockPos posBlock = res.getBlockPos();
                    ItemStack clickedBlock = p_41432_.getBlockState(posBlock).getCloneItemStack(res,p_41432_,posBlock,p_41433_);
                    List<ItemStack> currentQueue = readFilterQueueFromNBT(itemInOffhand,getItemTransportMode(itemInOffhand));
                    int currentQueueSize = currentQueue.size();
                    if (!clickedBlock.isEmpty()) {
                        List<ItemStack> adjustedQueue = addNonMatchRemoveMatching(currentQueue, clickedBlock);
                        MowLibMessageUtils.messagePopup(p_41433_, getItemTransportMode(itemInOffhand).getModeColorFormat(), (currentQueueSize < adjustedQueue.size())?(MODID + ".filter_changed_added"):(MODID + ".filter_changed_removed"));
                        writeFilterQueueToNBT(itemInOffhand, adjustedQueue, getItemTransportMode(itemInOffhand));
                    }
                }
            } else if (itemInOffhand.getItem() instanceof IPedestalFilter && itemInMainhand.getItem() instanceof IPedestalFilter) {
                MowLibMessageUtils.messagePopup(p_41433_, ChatFormatting.RED, "mowlib.filter.message_twohanded");
            }
        }

        return InteractionResultHolder.fail(p_41433_.getItemInHand(p_41434_));
    }

    private List<ItemStack> addNonMatchRemoveMatching(List<ItemStack> list, ItemStack stackToCheck)
    {
        boolean canAdd = true;
        for(int i=0;i<list.size();i++)
        {
            if(list.get(i).getItem().equals(stackToCheck.getItem()))
            {
                list.remove(i);
                canAdd = false;
                break;
            }
        }

        if(canAdd)list.add(stackToCheck);

        return list;
    }

    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.ITEMS);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem().equals(incomingStack.getItem()))
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            return true;
        }
        else return false;
    }

    @Override
    public boolean canAcceptFluids(ItemStack filter, FluidStack incomingFluidStack) {
        boolean filterBool = super.canAcceptFluids(filter, incomingFluidStack);

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.FLUIDS);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> !MowLibFluidUtils.getFluidStackFromItemStack(itemStack).isEmpty())
                .filter(itemStack -> MowLibFluidUtils.getFluidStackFromItemStack(itemStack).getFluid().equals(incomingFluidStack.getFluid()))
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            return filterBool;
        }
        else return !filterBool;
    }

    @Override
    public boolean canAcceptDust(ItemStack filter, DustMagic incomingDust) {
        boolean filterBool = super.canAcceptDust(filter, incomingDust);

        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.DUST);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> !DustMagic.getDustMagicInItemStack(itemStack).isEmpty())
                .filter(itemStack -> DustMagic.getDustMagicInItemStack(itemStack).isDustEqual(incomingDust))
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            return filterBool;
        }
        else return !filterBool;
    }
}
