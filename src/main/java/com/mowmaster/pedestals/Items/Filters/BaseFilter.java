package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Client.ItemTooltipComponent;
import com.mowmaster.pedestals.PedestalUtils.PedestalModesAndTypes;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
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

import static com.mowmaster.pedestals.PedestalUtils.PedestalModesAndTypes.getModeLocalizedString;
import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


import net.minecraft.world.item.Item.Properties;

public class BaseFilter extends Item implements IPedestalFilter
{
    public boolean filterType = false;

    public BaseFilter(Properties p_41383_) {
        super(p_41383_);
    }

    public boolean doItemsMatch(ItemStack stackPedestal, ItemStack itemStackIn)
    {
        return ItemHandlerHelper.canItemStacksStack(stackPedestal,itemStackIn);
    }

    public boolean canModeUseInventoryAsFilter(int mode)
    {
        return mode<=1;
    }

    public boolean canSetFilterMode(int mode)
    {
        return true;
    }

    public boolean canSetFilterType(int mode)
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
                        if(player.isCrouching())
                        {
                            if(canSetFilterMode(-1))setFilterMode(player,itemInOffhand,InteractionHand.OFF_HAND);
                            //return InteractionResultHolder.success(itemInOffhand);
                        }
                        else
                        {
                            if(canSetFilterType(-1))setFilterType(player,itemInOffhand);
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

                            if(buildQueue.size() > 0 && canModeUseInventoryAsFilter(PedestalModesAndTypes.getModeFromStack(itemInOffhand)))
                            {
                                this.writeFilterQueueToNBT(itemInOffhand,buildQueue, PedestalModesAndTypes.getModeFromStack(itemInOffhand));
                                MowLibMessageUtils.messagePopup(player,PedestalModesAndTypes.getModeColorFormat(itemInOffhand),MODID + ".filter_changed");
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

            boolean getCurrentType = filterItem.getFilterType(heldItem,PedestalModesAndTypes.getModeFromStack(heldItem));
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
        if(heldItem.getItem() instanceof BaseFilter)
        {
            BaseFilter filterItem = ((BaseFilter)heldItem.getItem());

            int mode = PedestalModesAndTypes.getModeFromStack(heldItem)+1;
            int setNewMode = (mode<=4)?(mode):(0);
            PedestalModesAndTypes.saveModeToNBT(heldItem,setNewMode);
            MowLibColorReference.addColorToItemStack(heldItem,filterItem.getFilterTypeColor(heldItem));
            player.setItemInHand(hand,heldItem);

            ChatFormatting colorChange = PedestalModesAndTypes.getModeDarkColorFormat(setNewMode);
            String typeString = getModeLocalizedString(setNewMode);

            List<String> listed = new ArrayList<>();
            listed.add(MODID + typeString);
            MowLibMessageUtils.messagePopupWithAppend(MODID, player,colorChange,MODID + ".mode_changed",listed);
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
        getFilterTypeFromNBT(filterItem,PedestalModesAndTypes.getModeFromStack(filterItem));
        return getFilterType();
    }

    public boolean getFilterType(ItemStack filterItem, int mode) {
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
        writeFilterTypeToNBT(filterItem,PedestalModesAndTypes.getModeFromStack(filterItem));
    }

    public int getFilterTypeColor(ItemStack filterItem)
    {
        return (getFilterType(filterItem))?(2763306):(16777215);
    }

    //Change for new Modes
    @Override
    public boolean canAcceptItem(BasePedestalBlockEntity pedestal, ItemStack itemStackIn, int mode) {
        return true;
    }

    @Override
    public boolean canAcceptItems(ItemStack filter, ItemStack incomingStack) { return !getFilterType(filter,0); }

    @Override
    public boolean canAcceptFluids(ItemStack filter, FluidStack incomingFluidStack) { return !getFilterType(filter,1); }

    @Override
    public boolean canAcceptEnergy(ItemStack filter, int incomingAmount)
    {
        return !getFilterType(filter,2);
    }

    @Override
    public boolean canAcceptXP(ItemStack filter, int incomingAmount)
    {
        return !getFilterType(filter,3);
    }

    @Override
    public boolean canAcceptDust(ItemStack filter, DustMagic incomingDust)
    {
        return !getFilterType(filter,4);
    }


    @Override
    public boolean canSendItem(BasePedestalBlockEntity pedestal) {
        return true;
    }

    //Change for new Modes
    @Override
    public int canAcceptCount(BasePedestalBlockEntity pedestal, ItemStack itemStackIncoming, int mode) {
        switch (mode)
        {
            case 0: return canAcceptCount(pedestal, pedestal.getLevel(), pedestal.getPos(), pedestal.getItemInPedestal(), itemStackIncoming, mode);
            case 1: return pedestal.getFluidTransferRate();
            case 2: return pedestal.getEnergyTransferRate();
            case 3: return pedestal.getExperienceTransferRate();
            case 4: return pedestal.getDustTransferRate();
            default: return -1;
        }
    }

    //Change for new Modes
    @Override
    public int canAcceptCount(BasePedestalBlockEntity pedestal, Level world, BlockPos pos, ItemStack itemInPedestal, ItemStack itemStackIncoming, int mode) {
        switch (mode)
        {
            case 0: return Math.min(pedestal.getSlotSizeLimit(), itemStackIncoming.getMaxStackSize());
            case 1: return pedestal.getFluidTransferRate();
            case 2: return pedestal.getEnergyTransferRate();
            case 3: return pedestal.getExperienceTransferRate();
            case 4: return pedestal.getDustTransferRate();
            default: return -1;
        }
    }

    @Override
    public void removeFilterQueueHandler(ItemStack filterStack) {
        CompoundTag compound = new CompoundTag();
        if(filterStack.hasTag())
        {
            compound = filterStack.getTag();
            for(int i=0; i<4;i++)
            {

                if(compound.contains(PedestalModesAndTypes.getModeStringFromInt(i)+"filterqueue"))
                {
                    compound.remove(PedestalModesAndTypes.getModeStringFromInt(i)+"filterqueue");
                    filterStack.setTag(compound);
                }
            }
        }
    }

    @Override
    public int filterQueueSize(ItemStack filterStack, int mode) {
        int filterQueueSize = 0;
        if(filterStack.hasTag())
        {
            CompoundTag getCompound = filterStack.getTag();
            if(getCompound.contains(PedestalModesAndTypes.getModeStringFromInt(mode)+"filterqueue"))
            {
                getCompound.get(PedestalModesAndTypes.getModeStringFromInt(mode)+"filterqueue");
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
    public void writeFilterQueueToNBT(ItemStack filterStack, List<ItemStack> builtFilterQueueList, int mode) {
        CompoundTag compound = new CompoundTag();
        CompoundTag compoundStorage = new CompoundTag();
        if(filterStack.hasTag()){compound = filterStack.getTag();}

        ItemStackHandler handler = new ItemStackHandler();
        handler.setSize(builtFilterQueueList.size());

        for(int i=0;i<handler.getSlots();i++) {handler.setStackInSlot(i,builtFilterQueueList.get(i));}

        compoundStorage = handler.serializeNBT();
        compound.put(PedestalModesAndTypes.getModeStringFromInt(mode)+"filterqueue",compoundStorage);
        filterStack.setTag(compound);
    }

    @Override
    public List<ItemStack> readFilterQueueFromNBT(ItemStack filterStack, int mode) {
        List<ItemStack> filterQueue = new ArrayList<>();
        if(filterStack.hasTag())
        {
            CompoundTag getCompound = filterStack.getTag();
            if(getCompound.contains(PedestalModesAndTypes.getModeStringFromInt(mode)+"filterqueue"))
            {
                CompoundTag invTag = getCompound.getCompound(PedestalModesAndTypes.getModeStringFromInt(mode)+"filterqueue");
                ItemStackHandler handler = new ItemStackHandler();
                ((INBTSerializable<CompoundTag>) handler).deserializeNBT(invTag);

                for(int i=0;i<handler.getSlots();i++) {filterQueue.add(handler.getStackInSlot(i));}
            }
        }

        return filterQueue;
    }

    @Override
    public void writeFilterTypeToNBT(ItemStack filterStack, int mode) {
        CompoundTag compound = new CompoundTag();
        if(filterStack.hasTag())
        {
            compound = filterStack.getTag();
        }
        compound.putBoolean(PedestalModesAndTypes.getModeStringFromInt(mode)+"filter_type",this.filterType);
        filterStack.setTag(compound);
    }

    @Override
    public boolean getFilterTypeFromNBT(ItemStack filterStack, int mode) {
        if(filterStack.hasTag())
        {
            CompoundTag getCompound = filterStack.getTag();
            this.filterType = getCompound.getBoolean(PedestalModesAndTypes.getModeStringFromInt(mode)+"filter_type");
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
            for(int i=0;i<4;i++)
            {
                List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,i);
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
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.DARK_RED,MODID + ".baseItem");
        }
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(stack,PedestalModesAndTypes.getModeFromStack(stack));
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        stackCurrent.forEach(nonnulllist::add);

        return Optional.of(new ItemTooltipComponent(nonnulllist));
    }

    public Component filterTypeTooltip(int filterMode, boolean filterType)
    {
        MutableComponent filterList = Component.translatable(MODID + ".filter_type");
        MutableComponent white = Component.translatable(MODID + ".filter_type_whitelist");
        MutableComponent black = Component.translatable(MODID + ".filter_type_blacklist");
        filterList.append((filterType)?(black):(white));
        filterList.withStyle(ChatFormatting.WHITE);

        return filterList;
    }

    public Component filterModeTooltip(int filterMode, boolean filterType)
    {
        MutableComponent changed = Component.translatable(MODID + ".tooltip_mode");
        String typeString = PedestalModesAndTypes.getModeLocalizedString(filterMode);
        changed.withStyle(ChatFormatting.GOLD);
        MutableComponent type = Component.translatable(MODID + typeString);
        changed.append(type);

        return changed;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        if(!p_41421_.getItem().equals(DeferredRegisterItems.FILTER_BASE.get()))
        {
            int filterMode = PedestalModesAndTypes.getModeFromStack(p_41421_);
            boolean filterType = getFilterType(p_41421_,filterMode);

            if(canSetFilterType(filterMode))p_41423_.add(filterTypeTooltip(filterMode, filterType));

            if(canSetFilterMode(filterMode))p_41423_.add(filterModeTooltip(filterMode, filterType));
        }
        else
        {
            MutableComponent base = Component.translatable(getDescriptionId() + ".base_description");
            base.withStyle(ChatFormatting.DARK_RED);
            p_41423_.add(base);
        }
    }
}
