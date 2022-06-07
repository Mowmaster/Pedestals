package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.MowLibUtils.ColorReference;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Client.ItemTooltipComponent;
import com.mowmaster.pedestals.PedestalUtils.PedestalUtilities;
import com.mowmaster.pedestals.Registry.DeferredRegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;


public class BaseFilter extends Item implements IPedestalFilter
{
    public boolean filterType = false;

    public BaseFilter(Properties p_41383_) {
        super(p_41383_);
    }

    public static int getFilterMode(ItemStack stack) {
        return readModeFromNBT(stack);
    }

    public String getFilterModeString(int mode) {

        switch(mode)
        {
            case 0: return "item";
            case 1: return "fluid";
            case 2: return "energy";
            case 3: return "xp";
            default: return "item";
        }
    }

    public String getFilterModeString(ItemStack stack) {

        switch(getFilterMode(stack))
        {
            case 0: return "item";
            case 1: return "fluid";
            case 2: return "energy";
            case 3: return "xp";
            default: return "item";
        }
    }

    public static void saveModeToNBT(ItemStack filter, int mode)
    {
        CompoundTag compound = new CompoundTag();
        if(filter.hasTag())
        {
            compound = filter.getTag();
        }
        compound.putInt(MODID+"_filter_mode",mode);
        filter.setTag(compound);
    }

    public static int readModeFromNBT(ItemStack filter) {
        if(filter.hasTag())
        {
            CompoundTag getCompound = filter.getTag();
            return getCompound.getInt(MODID+"_filter_mode");
        }
        return 0;
    }

    public boolean doItemsMatch(ItemStack stackPedestal, ItemStack itemStackIn)
    {
        return ItemHandlerHelper.canItemStacksStack(stackPedestal,itemStackIn);
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
                            setFilterMode(player,itemInOffhand,InteractionHand.OFF_HAND);
                            //return InteractionResultHolder.success(itemInOffhand);
                        }
                        else
                        {
                            setFilterTypeWhiteBlacklist(player,itemInOffhand);
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

                            if(buildQueue.size() > 0 && this.getFilterMode(itemInOffhand)<=1)
                            {
                                this.writeFilterQueueToNBT(itemInOffhand,buildQueue, this.getFilterMode(itemInOffhand));
                                ChatFormatting color;
                                switch (getFilterMode(itemInOffhand))
                                {
                                    case 0: color = ChatFormatting.GOLD; break;
                                    case 1: color = ChatFormatting.BLUE; break;
                                    case 2: color = ChatFormatting.RED; break;
                                    case 3: color = ChatFormatting.GREEN; break;
                                    default: color = ChatFormatting.WHITE; break;
                                }
                                TranslatableComponent filterChanged = new TranslatableComponent(MODID + ".filter_changed");
                                filterChanged.withStyle(color);
                                player.displayClientMessage(filterChanged,true);
                            }
                        }
                    }
                }
                else if(itemInOffhand.getItem() instanceof IPedestalFilter && itemInMainhand.getItem() instanceof IPedestalFilter){
                    TranslatableComponent pedestalFluid = new TranslatableComponent(MODID + ".filter.message_twohanded");
                    pedestalFluid.withStyle(ChatFormatting.RED);
                    player.displayClientMessage(pedestalFluid,true);
                }
            }
        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }

    public void setFilterTypeWhiteBlacklist(Player player, ItemStack heldItem)
    {
        boolean getCurrentType = getFilterType(heldItem,getFilterMode(heldItem));
        this.setFilterType(heldItem,!getCurrentType);
        TranslatableComponent changed = new TranslatableComponent(MODID + ".filter_type_changed");
        changed.withStyle((!getCurrentType)?(ChatFormatting.BLACK):(ChatFormatting.WHITE));
        TranslatableComponent white = new TranslatableComponent(MODID + ".filter_type_whitelist");
        TranslatableComponent black = new TranslatableComponent(MODID + ".filter_type_blacklist");
        changed.append((!getCurrentType)?(black):(white));
        player.displayClientMessage(changed,true);
    }

    public void setFilterTypeAboveBelow(Player player, ItemStack heldItem)
    {
        if(heldItem.getItem() instanceof BaseFilter) {
            BaseFilter filterItem = ((BaseFilter) heldItem.getItem());

            boolean getCurrentType = filterItem.getFilterType(heldItem,getFilterMode(heldItem));
            filterItem.setFilterType(heldItem,!getCurrentType);
            TranslatableComponent changed = new TranslatableComponent(MODID + ".filter_type_changed");
            changed.withStyle((!getCurrentType)?(ChatFormatting.BLACK):(ChatFormatting.WHITE));
            TranslatableComponent above = new TranslatableComponent(MODID + ".filter_type_above");
            TranslatableComponent below = new TranslatableComponent(MODID + ".filter_type_below");
            changed.append((!getCurrentType)?(below):(above));
            player.displayClientMessage(changed,true);
        }
    }

    public void setFilterMode(Player player, ItemStack heldItem, InteractionHand hand)
    {
        if(heldItem.getItem() instanceof BaseFilter)
        {
            BaseFilter filterItem = ((BaseFilter)heldItem.getItem());

            int mode = filterItem.getFilterMode(heldItem)+1;
            int setNewMode = (mode<=3)?(mode):(0);
            filterItem.saveModeToNBT(heldItem,setNewMode);
            ColorReference.addColorToItemStack(heldItem,filterItem.getFilterTypeColor(heldItem));
            player.setItemInHand(hand,heldItem);

            TranslatableComponent changed = new TranslatableComponent(MODID + ".mode_changed");
            ChatFormatting colorChange = ChatFormatting.BLACK;
            String typeString = "";
            switch(setNewMode)
            {
                case 0: typeString = ".mode_items"; colorChange = ChatFormatting.GOLD; break;
                case 1: typeString = ".mode_fluids"; colorChange = ChatFormatting.DARK_BLUE; break;
                case 2: typeString = ".mode_energy"; colorChange = ChatFormatting.RED; break;
                case 3: typeString = ".mode_experience"; colorChange = ChatFormatting.DARK_GREEN; break;
                default: typeString = ".error"; colorChange = ChatFormatting.DARK_RED; break;
            }
            changed.withStyle(colorChange);
            TranslatableComponent type = new TranslatableComponent(MODID + typeString);
            changed.append(type);
            player.displayClientMessage(changed,true);
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
        getFilterTypeFromNBT(filterItem,getFilterMode(filterItem));
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
        if(filterSet) { ColorReference.addColorToItemStack(filterItem,2763306); }
        else ColorReference.addColorToItemStack(filterItem,16777215);
        writeFilterTypeToNBT(filterItem,getFilterMode(filterItem));
    }

    public int getFilterTypeColor(ItemStack filterItem)
    {
        return (getFilterType(filterItem))?(2763306):(16777215);
    }

    @Override
    public boolean canAcceptItem(BasePedestalBlockEntity pedestal, ItemStack itemStackIn, int mode) {
        return true;
    }

    public boolean canTransferItems(ItemStack filter) { return !getFilterType(filter,0); }

    public boolean canTransferFluids(ItemStack filter) { return !getFilterType(filter,1); }

    public boolean canTransferEnergy(ItemStack filter)
    {
        return !getFilterType(filter,2);
    }

    public boolean canTransferXP(ItemStack filter)
    {
        return !getFilterType(filter,3);
    }

    @Override
    public boolean canSendItem(BasePedestalBlockEntity pedestal) {
        return true;
    }

    @Override
    public int canAcceptCount(BasePedestalBlockEntity pedestal, ItemStack itemStackIncoming, int mode) {
        switch (mode)
        {
            case 0: return canAcceptCount(pedestal, pedestal.getLevel(), pedestal.getPos(), pedestal.getItemInPedestal(), itemStackIncoming, mode);
            case 1: return pedestal.getFluidTransferRate();
            case 2: return pedestal.getEnergyTransferRate();
            case 3: return pedestal.getExperienceTransferRate();
            default: return -1;
        }
    }

    @Override
    public int canAcceptCount(BasePedestalBlockEntity pedestal, Level world, BlockPos pos, ItemStack itemInPedestal, ItemStack itemStackIncoming, int mode) {
        switch (mode)
        {
            case 0: return Math.min(pedestal.getSlotSizeLimit(), itemStackIncoming.getMaxStackSize());
            case 1: return pedestal.getFluidTransferRate();
            case 2: return pedestal.getEnergyTransferRate();
            case 3: return pedestal.getExperienceTransferRate();
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
                if(compound.contains(getFilterModeString(i)+"filterqueue"))
                {
                    compound.remove(getFilterModeString(i)+"filterqueue");
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
            if(getCompound.contains(getFilterModeString(mode)+"filterqueue"))
            {
                getCompound.get(getFilterModeString(mode)+"filterqueue");
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
        compound.put(getFilterModeString(mode)+"filterqueue",compoundStorage);
        filterStack.setTag(compound);
    }

    @Override
    public List<ItemStack> readFilterQueueFromNBT(ItemStack filterStack, int mode) {
        List<ItemStack> filterQueue = new ArrayList<>();
        if(filterStack.hasTag())
        {
            CompoundTag getCompound = filterStack.getTag();
            if(getCompound.contains(getFilterModeString(mode)+"filterqueue"))
            {
                CompoundTag invTag = getCompound.getCompound(getFilterModeString(mode)+"filterqueue");
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
        compound.putBoolean(getFilterModeString(mode)+"filter_type",this.filterType);
        filterStack.setTag(compound);
    }

    @Override
    public boolean getFilterTypeFromNBT(ItemStack filterStack, int mode) {
        if(filterStack.hasTag())
        {
            CompoundTag getCompound = filterStack.getTag();
            this.filterType = getCompound.getBoolean(getFilterModeString(mode)+"filter_type");
        }
        return filterType;
    }



    @Override
    public void chatDetails(Player player, BasePedestalBlockEntity pedestal) {
        ItemStack filterStack = pedestal.getFilterInPedestal();
        if(!filterStack.getItem().equals(DeferredRegisterItems.FILTER_BASE.get()))
        {
            TranslatableComponent filterList = new TranslatableComponent(filterStack.getDisplayName().getString());
            filterList.withStyle(ChatFormatting.GOLD);
            player.sendMessage(filterList, Util.NIL_UUID);

            //For each Mode
            for(int i=0;i<4;i++)
            {
                List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack,i);
                if(filterQueue.size()>0)
                {
                    TranslatableComponent enchant = new TranslatableComponent(MODID + ".filters.tooltip_filterlist");
                    enchant.withStyle(ChatFormatting.LIGHT_PURPLE);
                    player.sendMessage(enchant, Util.NIL_UUID);

                    for(int j=0;j<filterQueue.size();j++) {

                        if(!filterQueue.get(j).isEmpty())
                        {
                            TranslatableComponent enchants = new TranslatableComponent(filterQueue.get(j).getDisplayName().getString());
                            enchants.withStyle(ChatFormatting.GRAY);
                            player.sendMessage(enchants, Util.NIL_UUID);
                        }
                    }
                }
            }
        }
        else
        {
            TranslatableComponent base = new TranslatableComponent(MODID + ".baseItem");
            base.withStyle(ChatFormatting.DARK_RED);
            player.sendMessage(base, Util.NIL_UUID);
        }
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        List<ItemStack> stackCurrent = readFilterQueueFromNBT(stack,getFilterMode(stack));
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        stackCurrent.forEach(nonnulllist::add);

        return Optional.of(new ItemTooltipComponent(nonnulllist));
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {

        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        if(!p_41421_.getItem().equals(DeferredRegisterItems.FILTER_BASE.get()))
        {
            boolean filterType = getFilterType(p_41421_,getFilterMode(p_41421_));
            int filterMode = getFilterMode(p_41421_);

            TranslatableComponent filterList = new TranslatableComponent(MODID + ".filter_type");
            TranslatableComponent white = new TranslatableComponent(MODID + ".filter_type_whitelist");
            TranslatableComponent black = new TranslatableComponent(MODID + ".filter_type_blacklist");
            filterList.append((filterType)?(black):(white));
            filterList.withStyle(ChatFormatting.WHITE);
            p_41423_.add(filterList);

            TranslatableComponent changed = new TranslatableComponent(MODID + ".tooltip_mode");
            String typeString = "";
            switch(filterMode)
            {
                case 0: typeString = ".mode_items"; break;
                case 1: typeString = ".mode_fluids"; break;
                case 2: typeString = ".mode_energy"; break;
                case 3: typeString = ".mode_experience"; break;
                default: typeString = ".error"; break;
            }
            changed.withStyle(ChatFormatting.GOLD);
            TranslatableComponent type = new TranslatableComponent(MODID + typeString);
            changed.append(type);
            p_41423_.add(changed);
        }
        else
        {
            TranslatableComponent base = new TranslatableComponent(getDescriptionId() + ".base_description");
            base.withStyle(ChatFormatting.DARK_RED);
            p_41423_.add(base);
        }
    }
}
