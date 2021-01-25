package com.mowmaster.pedestals.item.pedestalFilters;

import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeEntityMinecart;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;


public class ItemFilterBase extends Item
{
    public boolean filterType = false;
    public ItemFilterBase(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    public boolean getFilterType()
    {
        //state 0|
        //state 1|false = whitelist
        //state 2|true = blacklist
        return filterType;
    }

    public boolean getFilterType(ItemStack filterItem)
    {
        //false = whitelist
        //true = blacklist
        getFilterTypeFromNBT(filterItem);
        return getFilterType();
    }

    public void setFilterType(ItemStack filterItem, boolean filterSet)
    {
        filterType = filterSet;
        writeFilterTypeToNBT(filterItem);
    }

    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        return true;
    }

    public boolean canSendItem(PedestalTileEntity tile)
    {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World p_77659_1_, PlayerEntity p_77659_2_, Hand p_77659_3_) {
        //Thankyou past self: https://github.com/Mowmaster/Ensorcelled/blob/main/src/main/java/com/mowmaster/ensorcelled/enchantments/handlers/HandlerAOEMiner.java#L53
        //RayTraceResult result = player.pick(player.getLookVec().length(),0,false); results in MISS type returns
        RayTraceResult result = p_77659_2_.pick(5,0,false);
        if(result != null)
        {
            //Assuming it it hits a block it wont work???
            if(result.getType() == RayTraceResult.Type.MISS)
            {
                if(p_77659_2_.isCrouching())
                {
                    ItemStack itemInHand = p_77659_2_.getHeldItem(p_77659_3_);
                    if(itemInHand.getItem() instanceof ItemFilterBase)
                    {
                        boolean getCurrentType = getFilterType(itemInHand);
                        setFilterType(itemInHand,!getCurrentType);
                        TranslationTextComponent changed = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterchange");
                        changed.mergeStyle(TextFormatting.GREEN);
                        TranslationTextComponent white = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterwhite");
                        TranslationTextComponent black = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterblack");
                        changed.append((!getCurrentType)?(black):(white));
                        p_77659_2_.sendStatusMessage(changed,true);
                        return ActionResult.resultSuccess(p_77659_2_.getHeldItem(p_77659_3_));
                    }
                    return ActionResult.resultFail(p_77659_2_.getHeldItem(p_77659_3_));
                }
            }
            //Assuming it it hits a block it wont work???
            if(result.getType() == RayTraceResult.Type.BLOCK)
            {
                if(p_77659_2_.isCrouching())
                {
                    ItemStack itemInHand = p_77659_2_.getHeldItem(p_77659_3_);
                    if(itemInHand.getItem() instanceof ItemFilterBase)
                    {
                        ItemUseContext context = new ItemUseContext(p_77659_2_,p_77659_3_,((BlockRayTraceResult) result));
                        BlockRayTraceResult res = new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(), false);
                        BlockPos posBlock = res.getPos();

                        List<ItemStack> buildQueue = buildFilterQueue(p_77659_1_,posBlock);

                        if(buildQueue.size() > 0)
                        {
                            writeFilterQueueToNBT(itemInHand,buildQueue);
                            return ActionResult.resultSuccess(p_77659_2_.getHeldItem(p_77659_3_));
                        }
                        return ActionResult.resultFail(p_77659_2_.getHeldItem(p_77659_3_));
                    }
                }
            }
        }

        return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
    }

    public void removeFilterQueueHandler(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("filterqueue"))
            {
                compound.remove("filterqueue");
                stack.setTag(compound);
            }
        }
    }

    public int filterQueueSize(ItemStack coin)
    {
        int filterQueueSize = 0;
        if(coin.hasTag())
        {
            CompoundNBT getCompound = coin.getTag();
            if(getCompound.contains("filterqueue"))
            {
                getCompound.get("filterqueue");
                ItemStackHandler handler = new ItemStackHandler();
                handler.deserializeNBT(getCompound);
                return handler.getSlots();
            }
        }

        return filterQueueSize;
    }

    public List<ItemStack> buildFilterQueue(World world, BlockPos invBlock)
    {
        List<ItemStack> filterQueue = new ArrayList<>();

        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,invBlock,true);
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

    public void writeFilterQueueToNBT(ItemStack stack, List<ItemStack> listIn)
    {
        CompoundNBT compound = new CompoundNBT();
        CompoundNBT compoundStorage = new CompoundNBT();
        if(stack.hasTag()){compound = stack.getTag();}

        ItemStackHandler handler = new ItemStackHandler();
        handler.setSize(listIn.size());

        for(int i=0;i<handler.getSlots();i++) {handler.setStackInSlot(i,listIn.get(i));}

        compoundStorage = handler.serializeNBT();
        compound.put("filterqueue",compoundStorage);
        stack.setTag(compound);
    }

    public List<ItemStack> readFilterQueueFromNBT(ItemStack coin)
    {
        List<ItemStack> filterQueue = new ArrayList<>();
        if(coin.hasTag())
        {
            CompoundNBT getCompound = coin.getTag();
            if(getCompound.contains("filterqueue"))
            {
                CompoundNBT invTag = getCompound.getCompound("filterqueue");
                ItemStackHandler handler = new ItemStackHandler();
                ((INBTSerializable<CompoundNBT>) handler).deserializeNBT(invTag);

                for(int i=0;i<handler.getSlots();i++) {filterQueue.add(handler.getStackInSlot(i));}
            }
        }

        return filterQueue;
    }

    public static LazyOptional<IItemHandler> findItemHandlerAtPos(World world, BlockPos pos, boolean allowCart)
    {
        TileEntity neighbourTile = world.getTileEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            if(AbstractRailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof IForgeEntityMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {
                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof BoatEntity);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
        }
        return LazyOptional.empty();
    }

    public boolean doesFilterAndQueueMatch(List<ItemStack> filterIn, List<ItemStack> queueMatch)
    {
        int matching = 0;
        if(filterIn.size() == queueMatch.size())
        {
            for(int i=0;i<filterIn.size();i++)
            {
                if(doItemsMatchWithEmpty(filterIn.get(i),queueMatch.get(i)))
                {
                    matching++;
                    continue;
                }
                else
                {
                    break;
                }
            }
        }

        return matching == filterIn.size();
    }

    public boolean doItemsMatchWithEmpty(ItemStack stackPedestal, ItemStack itemStackIn)
    {
        if(stackPedestal.isEmpty() && itemStackIn.isEmpty())return true;

        return ItemHandlerHelper.canItemStacksStack(stackPedestal,itemStackIn);
    }

    public void writeFilterTypeToNBT(ItemStack stack)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }
        compound.putBoolean("filter_type",this.filterType);
        stack.setTag(compound);
    }

    public boolean getFilterTypeFromNBT(ItemStack stack)
    {
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            this.filterType = getCompound.getBoolean("filter_type");
        }
        return filterType;
    }

    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack filterStack = pedestal.getFilterInPedestal();

        List<ItemStack> filterQueue = readFilterQueueFromNBT(filterStack);
        if(filterQueue.size()>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterlist");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant, Util.DUMMY_UUID);

            for(int i=0;i<filterQueue.size();i++) {

                if(!filterQueue.get(i).isEmpty())
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(filterQueue.get(i).getDisplayName().getString());
                    enchants.mergeStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants, Util.DUMMY_UUID);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        boolean filterType = getFilterType(stack);
        TranslationTextComponent filterList = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filtertype");
        TranslationTextComponent white = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterwhite");
        TranslationTextComponent black = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterblack");
        filterList.append((filterType)?(black):(white));
        filterList.mergeStyle(TextFormatting.GOLD);
        tooltip.add(filterList);

        List<ItemStack> filterQueue = readFilterQueueFromNBT(stack);
        if(filterQueue.size()>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(Reference.MODID + ".filters.tooltip_filterlist");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            tooltip.add(enchant);

            for(int i=0;i<filterQueue.size();i++) {

                if(!filterQueue.get(i).isEmpty())
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(filterQueue.get(i).getDisplayName().getString());
                    enchants.mergeStyle(TextFormatting.GRAY);
                    tooltip.add(enchants);
                }
            }
        }

        /*TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString();
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);*/
    }
}
