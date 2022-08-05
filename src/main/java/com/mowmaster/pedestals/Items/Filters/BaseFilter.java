package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Client.ItemTooltipComponent;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

//import static com.mowmaster.pedestals.PedestalUtils.PedestalModesAndTypes.getModeLocalizedString;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class BaseFilter extends Item implements IPedestalFilter
{
    public boolean filterType = false;
    public FilterDirection filterableDirection;

    public BaseFilter(Properties p_41383_, FilterDirection direction) {
        super(p_41383_);
        this.filterableDirection = direction;
    }

    @Override
    public FilterDirection getFilterDirection()
    {
        return filterableDirection;
    }

    @Override
    public ItemTransferMode getItemTransportMode(ItemStack stackIn) {
        return ItemTransferMode.ITEMS.getTransferModeFromStack(stackIn);
    }

    public static int getFilterModeForRender(ItemStack stackIn)
    {
        return ItemTransferMode.ITEMS.getTransferModeIntFromStack(stackIn);
    }

    public boolean doItemsMatch(ItemStack stackPedestal, ItemStack itemStackIn)
    {
        return ItemHandlerHelper.canItemStacksStack(stackPedestal,itemStackIn);
    }

    public FluidStack getFluidStackFromItemStack(ItemStack stackIn)
    {
        if(stackIn.getItem() instanceof BucketItem bucket)
        {
            Fluid bucketFluid = bucket.getFluid();
            return new FluidStack(bucketFluid,1000);
        }

        return FluidStack.EMPTY;
    }

    public boolean canModeUseInventoryAsFilter(ItemTransferMode mode)
    {
        return mode.ordinal()<=1;
    }

    public boolean canSetFilterMode(ItemTransferMode mode)
    {
        return true;
    }

    public boolean canSetFilterType(ItemTransferMode mode)
    {
        return true;
    }

    public void setFilterType(Player player, ItemStack heldItem)
    {
        setFilterTypeCustom(player, heldItem, ".filter_type_blacklist", ".filter_type_whitelist", ChatFormatting.BLACK, ChatFormatting.WHITE, ".filter_type_changed");
    }

    //Left Click
    //No methods exist without using a client one and needing to send packets back and forth, BLEH

    //Right Click
    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level world = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack itemInMainhand = player.getMainHandItem();
        ItemStack itemInOffhand = player.getOffhandItem();
        HitResult result = player.pick(5,0,false);

        if(!world.isClientSide())
        {
            //Disable Filter Base
            if(!(itemInOffhand.getItem().equals(DeferredRegisterItems.FILTER_BASE.get())) || !(itemInMainhand.getItem().equals(DeferredRegisterItems.FILTER_BASE.get())))
            {
                //Check for Offhand Only Filter
                if(itemInOffhand.getItem() instanceof IPedestalFilter && !(itemInMainhand.getItem() instanceof IPedestalFilter))
                {
                    if(result.getType().equals(HitResult.Type.MISS))
                    {
                        //int mode = PedestalModesAndTypes.getModeFromStack(itemInOffhand);
                        if(player.isCrouching())
                        {
                            if(canSetFilterMode(getItemTransportMode(itemInOffhand)))setFilterMode(player,itemInOffhand,InteractionHand.OFF_HAND);
                            //return InteractionResultHolder.success(itemInOffhand);
                        }
                        else
                        {
                            if(canSetFilterType(getItemTransportMode(itemInOffhand)))setFilterType(player,itemInOffhand);
                            //return InteractionResultHolder.success(itemInOffhand);
                        }
                    }
                    else if(result.getType().equals(HitResult.Type.BLOCK))
                    {
                        if(player.isCrouching())
                        {
                            UseOnContext context = new UseOnContext(player,hand,((BlockHitResult) result));
                            BlockHitResult res = new BlockHitResult(context.getClickLocation(), context.getHorizontalDirection(), context.getClickedPos(), false);
                            BlockPos posBlock = res.getBlockPos();

                            List<ItemStack> buildQueue = this.buildFilterQueue(world,posBlock);

                            if(buildQueue.size() > 0 && canModeUseInventoryAsFilter(getItemTransportMode(itemInOffhand)))
                            {
                                this.writeFilterQueueToNBT(itemInOffhand,buildQueue, getItemTransportMode(itemInOffhand));
                                MowLibMessageUtils.messagePopup(player,getItemTransportMode(itemInOffhand).getModeColorFormat(),MODID + ".filter_changed");
                            }
                        }
                    }
                }
                else if(itemInOffhand.getItem() instanceof IPedestalFilter && itemInMainhand.getItem() instanceof IPedestalFilter){
                    MowLibMessageUtils.messagePopup(player,ChatFormatting.RED,MODID + ".filter.message_twohanded");
                }
            }
        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }

    public void setFilterTypeCustom(Player player, ItemStack heldItem, String firstLocalization, String secondLocalization, ChatFormatting firstColor, ChatFormatting secondColor, String chatLocalization)
    {
        if(heldItem.getItem() instanceof BaseFilter) {
            BaseFilter filterItem = ((BaseFilter) heldItem.getItem());

            boolean getCurrentType = filterItem.getFilterType(heldItem,getItemTransportMode(heldItem));
            filterItem.setFilterType(heldItem,!getCurrentType);
            String second = MODID + secondLocalization;
            String first = MODID + firstLocalization;
            List<String> listed = new ArrayList<>();
            listed.add((!getCurrentType)?(first):(second));
            MowLibMessageUtils.messagePopupWithAppend(MODID, player,(!getCurrentType)?(firstColor):(secondColor),MODID + chatLocalization,listed);
        }
    }

    //Change for new Modes
    public void setFilterMode(Player player, ItemStack heldItem, InteractionHand hand)
    {
        if(heldItem.getItem() instanceof BaseFilter filterItem)
        {
            IPedestalFilter iFilter = (IPedestalFilter)filterItem;
            /*int mode = PedestalModesAndTypes.getModeFromStack(heldItem)+1;
            int setNewMode = (mode<=4)?(mode):(0);
            PedestalModesAndTypes.saveModeToNBT(heldItem,setNewMode);*/

            //New Enum should set new value and serialize it as well

            getItemTransportMode(heldItem).iterateTransferMode(heldItem);
            //Change the whitelist/blacklist for the next modes filter
            MowLibColorReference.addColorToItemStack(heldItem,filterItem.getFilterTypeColor(heldItem));
            //Set item in hand so it reflects the changes
            player.setItemInHand(hand,heldItem);


            //TODO: Need to add this to MowLib
            MutableComponent message = getItemTransportMode(heldItem).componentTransferMode().copy();
            message.withStyle(getItemTransportMode(heldItem).getModeColorFormat());
            player.displayClientMessage(message, true);

            //ChatFormatting colorChange = PedestalModesAndTypes.getModeColorFormat(setNewMode);
            //String typeString = getModeLocalizedString(setNewMode);
            //List<String> listed = new ArrayList<>();
            //listed.add(MODID + typeString);
            //MowLibMessageUtils.messagePopupWithAppend(MODID, player,colorChange,MODID + ".mode_changed",listed);
        }
    }

    @Override
    public boolean getFilterType() {
        //state 0|
        //state 1|false = whitelist
        //state 2|true = blacklist
        return filterType;
    }

    @Override
    public boolean getFilterType(ItemStack filterItem) {
        //false = whitelist
        //true = blacklist
        getFilterTypeFromNBT(filterItem,getItemTransportMode(filterItem));
        return getFilterType();
    }

    public boolean getFilterType(ItemStack filterItem, ItemTransferMode mode) {
        //false = whitelist
        //true = blacklist
        getFilterTypeFromNBT(filterItem,mode);
        return getFilterType();
    }

    @Override
    public void setFilterType(ItemStack filterItem, boolean filterSet) {
        filterType = filterSet;
        if(filterSet) { MowLibColorReference.addColorToItemStack(filterItem,2763306); }
        else MowLibColorReference.addColorToItemStack(filterItem,16777215);
        writeFilterTypeToNBT(filterItem,getItemTransportMode(filterItem));
    }

    public int getFilterTypeColor(ItemStack filterItem)
    {
        return (getFilterType(filterItem))?(2763306):(16777215);
    }

    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) { return !getFilterType(filter,ItemTransferMode.ITEMS); }

    @Override
    public boolean canAcceptFluids(ItemStack filter, FluidStack incomingFluidStack) { return !getFilterType(filter,ItemTransferMode.FLUIDS); }

    @Override
    public boolean canAcceptEnergy(ItemStack filter, int incomingAmount) { return !getFilterType(filter,ItemTransferMode.ENERGY); }

    @Override
    public boolean canAcceptExperience(ItemStack filter, int incomingAmount)
    {
        return !getFilterType(filter,ItemTransferMode.EXPERIENCE);
    }

    @Override
    public boolean canAcceptDust(ItemStack filter, DustMagic incomingDust) { return !getFilterType(filter,ItemTransferMode.DUST); }


    @Override
    public boolean canSendItem(BasePedestalBlockEntity pedestal) {
        return true;
    }

    //Change for new Modes
    @Override
    public int canAcceptCountItems(BasePedestalBlockEntity pedestal, ItemStack itemStackIncoming)
    {
        ItemStack filter = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.ITEMS);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof FilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            FilterRestricted filterRestricted = (FilterRestricted)itemFromInv.getItem();
            return filterRestricted.canAcceptCountItems(pedestal,itemStackIncoming);
        }

        return Math.min(pedestal.getSlotSizeLimit(), itemStackIncoming.getMaxStackSize());
    }

    @Override
    public int canAcceptCountFluids(BasePedestalBlockEntity pedestal, FluidStack incomingFluidStack)
    {
        ItemStack filter = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.FLUIDS);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof FilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            FilterRestricted filterRestricted = (FilterRestricted)itemFromInv.getItem();
            return filterRestricted.canAcceptCountFluids(pedestal,incomingFluidStack);
        }

        return Math.min(pedestal.spaceForFluid(), incomingFluidStack.getAmount());
    }

    @Override
    public int canAcceptCountEnergy(BasePedestalBlockEntity pedestal, int incomingEnergyAmount)
    {
        ItemStack filter = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.ENERGY);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof FilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            FilterRestricted filterRestricted = (FilterRestricted)itemFromInv.getItem();
            return filterRestricted.canAcceptCountEnergy(pedestal,incomingEnergyAmount);
        }

        return Math.min(pedestal.spaceForEnergy(), incomingEnergyAmount);
    }

    @Override
    public int canAcceptCountExperience(BasePedestalBlockEntity pedestal, int incomingExperienceAmount)
    {
        ItemStack filter = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.EXPERIENCE);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof FilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            FilterRestricted filterRestricted = (FilterRestricted)itemFromInv.getItem();
            return filterRestricted.canAcceptCountExperience(pedestal,incomingExperienceAmount);
        }

        return Math.min(pedestal.spaceForExperience(), incomingExperienceAmount);
    }

    @Override
    public int canAcceptCountDust(BasePedestalBlockEntity pedestal, DustMagic incomingDust)
    {
        ItemStack filter = pedestal.getFilterInPedestal();
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(filter,ItemTransferMode.DUST);
        int range = stackCurrent.size();

        ItemStack itemFromInv = ItemStack.EMPTY;
        itemFromInv = IntStream.range(0,range)//Int Range
                .mapToObj((stackCurrent)::get)//Function being applied to each interval
                .filter(itemStack -> itemStack.getItem() instanceof FilterRestricted)
                .findFirst().orElse(ItemStack.EMPTY);

        if(!itemFromInv.isEmpty())
        {
            FilterRestricted filterRestricted = (FilterRestricted)itemFromInv.getItem();
            return filterRestricted.canAcceptCountDust(pedestal,incomingDust);
        }

        return Math.min(pedestal.spaceForDust(), incomingDust.getDustAmount());
    }

    @Override
    public void removeFilterQueueHandler(ItemStack filterStack) {
        CompoundTag compound = new CompoundTag();
        if(filterStack.hasTag())
        {
            compound = filterStack.getTag();
            for(int i=0; i<4;i++)
            {

                if(compound.contains(getItemTransportMode(filterStack).stringTransferMode()+"_filterqueue"))
                {
                    compound.remove(getItemTransportMode(filterStack).stringTransferMode()+"_filterqueue");
                    filterStack.setTag(compound);
                }
            }
        }
    }

    @Override
    public int filterQueueSize(ItemStack filterStack, ItemTransferMode mode) {
        int filterQueueSize = 0;
        if(filterStack.hasTag())
        {
            CompoundTag getCompound = filterStack.getTag();
            if(getCompound.contains(mode.stringTransferMode()+"_filterqueue"))
            {
                getCompound.get(mode.stringTransferMode()+"_filterqueue");
                ItemStackHandler handler = new ItemStackHandler();
                handler.deserializeNBT(getCompound);
                return handler.getSlots();
            }
        }

        return filterQueueSize;
    }

    @Override
    public List<ItemStack> buildFilterQueue(Level world, BlockPos invBlock) {
        List<ItemStack> filterQueue = new ArrayList<>();

        LazyOptional<IItemHandler> cap = PedestalUtilities.findItemHandlerAtPos(world,invBlock,true);
        if(cap.isPresent())
        {
            IItemHandler handler = cap.orElse(null);
            if(handler != null)
            {
                int range = handler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = handler.getStackInSlot(i);
                    if(!stackInSlot.isEmpty()) {filterQueue.add(stackInSlot);}
                }
            }
        }

        return filterQueue;
    }

    @Override
    public void writeFilterQueueToNBT(ItemStack filterStack, List<ItemStack> builtFilterQueueList, ItemTransferMode mode) {
        CompoundTag compound = new CompoundTag();
        CompoundTag compoundStorage = new CompoundTag();
        if(filterStack.hasTag()){compound = filterStack.getTag();}

        ItemStackHandler handler = new ItemStackHandler();
        handler.setSize(builtFilterQueueList.size());

        for(int i=0;i<handler.getSlots();i++) {handler.setStackInSlot(i,builtFilterQueueList.get(i));}

        compoundStorage = handler.serializeNBT();
        compound.put(mode.stringTransferMode()+"_filterqueue",compoundStorage);
        filterStack.setTag(compound);
    }

    @Override
    public List<ItemStack> readFilterQueueFromNBT(ItemStack filterStack, ItemTransferMode mode) {
        List<ItemStack> filterQueue = new ArrayList<>();
        if(filterStack.hasTag())
        {
            CompoundTag getCompound = filterStack.getTag();
            if(getCompound.contains(mode.stringTransferMode()+"_filterqueue"))
            {
                CompoundTag invTag = getCompound.getCompound(mode.stringTransferMode()+"_filterqueue");
                ItemStackHandler handler = new ItemStackHandler();
                ((INBTSerializable<CompoundTag>) handler).deserializeNBT(invTag);

                for(int i=0;i<handler.getSlots();i++) {filterQueue.add(handler.getStackInSlot(i));}
            }
        }

        return filterQueue;
    }

    @Override
    public void writeFilterTypeToNBT(ItemStack filterStack, ItemTransferMode mode) {
        CompoundTag compound = new CompoundTag();
        if(filterStack.hasTag())
        {
            compound = filterStack.getTag();
        }
        compound.putBoolean(mode.stringTransferMode()+"_filter_type",this.filterType);
        filterStack.setTag(compound);
    }

    @Override
    public boolean getFilterTypeFromNBT(ItemStack filterStack, ItemTransferMode mode) {
        if(filterStack.hasTag())
        {
            CompoundTag getCompound = filterStack.getTag();
            this.filterType = getCompound.getBoolean(mode.stringTransferMode()+"_filter_type");
        }
        return filterType;
    }



    @Override
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
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

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(stack,getItemTransportMode(stack));
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        stackCurrent.forEach(nonnulllist::add);

        return Optional.of(new ItemTooltipComponent(nonnulllist));
    }

    public Component filterTypeTooltip(ItemTransferMode mode, boolean filterType)
    {
        MutableComponent filterList = Component.translatable(MODID + ".filter_type");
        MutableComponent white = Component.translatable(MODID + ".filter_type_whitelist");
        MutableComponent black = Component.translatable(MODID + ".filter_type_blacklist");
        filterList.append((filterType)?(black):(white));
        filterList.withStyle(ChatFormatting.WHITE);

        return filterList;
    }

    public Component filterModeTooltip(ItemTransferMode mode, boolean filterType)
    {
        MutableComponent changed = Component.translatable(MODID + ".tooltip_mode");
        String typeString = mode.stringTransferMode();
        changed.withStyle(ChatFormatting.GOLD);
        MutableComponent type = Component.translatable(MODID + typeString);
        changed.append(type);

        return changed;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        MutableComponent filterDirection = Component.translatable(MODID + ".filter.tooltip_filterdirection");
        filterDirection.append(getFilterDirection().componentDirection());
        p_41423_.add(filterDirection);

        if(!p_41421_.getItem().equals(DeferredRegisterItems.FILTER_BASE.get()))
        {
            boolean filterType = getFilterType(p_41421_,getItemTransportMode(p_41421_));

            if(canSetFilterType(getItemTransportMode(p_41421_)))p_41423_.add(filterTypeTooltip(getItemTransportMode(p_41421_), filterType));

            if(canSetFilterMode(getItemTransportMode(p_41421_)))p_41423_.add(filterModeTooltip(getItemTransportMode(p_41421_), filterType));
        }
        else
        {
            MutableComponent base = Component.translatable(getDescriptionId() + ".base_description");
            base.withStyle(ChatFormatting.DARK_RED);
            p_41423_.add(base);
        }
    }
}
