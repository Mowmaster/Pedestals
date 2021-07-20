package com.mowmaster.pedestals.api.filter;

import com.mowmaster.pedestals.item.pedestalFilters.ItemFilterBase;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
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

public interface IFilterBase
{
    public boolean filterType = false;

    /**
     * @return
     * state 0|
     * state 1|false = whitelist
     * state 2|true = blacklist
     */

    boolean getFilterType();

    /**
     * @param filterItem
     * @return the value from the NBT stored on the Item
     */
    boolean getFilterType(ItemStack filterItem);

    /**
     * @param filterItem
     * @param filterSet bool value of getFilterType()
     * set the items NBT value
     */
    void setFilterType(ItemStack filterItem, boolean filterSet);

    /**
     * @param pedestal pedestal tile filter is in
     * @param itemStackIn incoming itemstack
     * When this pedestal is about to receive an item, this method is called,
     * if it returns false the itemstack will not be accepted.
     */
    boolean canAcceptItem(PedestalTileEntity pedestal, ItemStack itemStackIn);

    /**
     * @param pedestal pedestal tile filter is in
     * ---   Currently Not in Use   ---
     * When this pedestal is about to send an item it can prevent the item from being sent
     */
    boolean canSendItem(PedestalTileEntity pedestal);

    /**
     * @param pedestal pedestal tile filter is in
     * @param itemStackIncoming incoming itemstack
     * @return itemstack count allowed to be insert
     * When this pedestal is going to receive an itemstack this is called.
     */
    int canAcceptCount(PedestalTileEntity pedestal, ItemStack itemStackIncoming);

    /**
     * @param filterStack
     * Used to remove the nbt "filterqueue"
     */
    void removeFilterQueueHandler(ItemStack filterStack);

    /**
     * @param filterStack
     * Used to check the size of "filterqueue"
     */
    int filterQueueSize(ItemStack filterStack);

    /**
     * @param world
     * @param invBlock the inventory used to set the filters 'queue'
     * @return a list of itemstacks
     * Called when Crouch + Right Clicking a filter item on an inventory in world.
     * this grabs the storage containers handler, and iterates through it to build the list
     */
    List<ItemStack> buildFilterQueue(World world, BlockPos invBlock);

    /**
     * @param filterStack
     * @param builtFilterQueueList
     * writes the list to the filters NBT "filterqueue"
     */
    void writeFilterQueueToNBT(ItemStack filterStack, List<ItemStack> builtFilterQueueList);

    /**
     * @param filterStack
     * @return a list of itemstacks in the filters queue
     * reads the list of itemstacks from the filters NBT "filterqueue"
     */
    List<ItemStack> readFilterQueueFromNBT(ItemStack filterStack);




    /**
     * @param filterStack
     * writes the bool filtertype to the NBT "filter_type"
     */
    void writeFilterTypeToNBT(ItemStack filterStack);

    /**
     * @param filterStack
     * @return filtertype
     * writes the bool filtertype from the NBT "filter_type"
     */
    boolean getFilterTypeFromNBT(ItemStack filterStack);



    /**
     * @param player
     * @param pedestal
     * When a player right clicks the FilterTool item on a pedestal, this is called.
     * Generally this will output filter info to the players chat in game.
     */
    void chatDetails(PlayerEntity player, PedestalTileEntity pedestal);
}
