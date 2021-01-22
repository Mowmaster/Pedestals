package com.mowmaster.pedestals.item.pedestalFilters;

import com.mowmaster.pedestals.enchants.EnchantmentRegistry;
import com.mowmaster.pedestals.item.pedestalUpgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static net.minecraft.state.properties.BlockStateProperties.FACING;


public class ItemFilterBase extends Item
{
    public boolean filterType = false;
    public ItemFilterBase(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    public boolean getFilterType()
    {
        //false = whitelist
        //true = blacklist
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

    public List<ItemStack> buildFilterQueue(PedestalTileEntity pedestal, BlockPos invBlock)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        ItemStack coin = pedestal.getCoinOnPedestal();
        BlockState pedestalState = world.getBlockState(pedestalPos);


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

    public void getFilterTypeFromNBT(ItemStack stack)
    {
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            this.filterType = getCompound.getBoolean("filter_type");
        }
    }

    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        /*ItemStack stack = pedestal.getCoinOnPedestal();
        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".chat_name");
        TranslationTextComponent name2 = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.appendString(name2.getString());
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name, Util.DUMMY_UUID);*/
    }

    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        /*TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString();
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);*/
    }
}
