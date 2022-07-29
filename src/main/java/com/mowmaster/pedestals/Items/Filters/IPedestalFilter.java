package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IPedestalFilter
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
    boolean canAcceptItem(BasePedestalBlockEntity pedestal, ItemStack itemStackIn, int mode);

    boolean canAcceptItems(ItemStack filter, ItemStack incomingStack);

    boolean canAcceptFluids(ItemStack filter, FluidStack incomingFluidStack);

    boolean canAcceptEnergy(ItemStack filter, int incomingAmount);

    boolean canAcceptExperience(ItemStack filter, int incomingAmount);

    boolean canAcceptDust(ItemStack filter, DustMagic incomingDust);

    /**
     * @param pedestal pedestal tile filter is in
     * ---   Currently Not in Use   ---
     * When this pedestal is about to send an item it can prevent the item from being sent
     */
    boolean canSendItem(BasePedestalBlockEntity pedestal);

    /**
     * @param pedestal pedestal tile filter is in
     * @param itemStackIncoming incoming itemstack
     * @return itemstack count allowed to be insert
     * When this pedestal is going to receive an itemstack this is called.
     */
    int canAcceptCountItems(BasePedestalBlockEntity pedestal, ItemStack itemStackIncoming);
    int canAcceptCountFluids(BasePedestalBlockEntity pedestal, FluidStack incomingFluidStack);
    int canAcceptCountEnergy(BasePedestalBlockEntity pedestal, int incomingEnergyAmount);
    int canAcceptCountExperience(BasePedestalBlockEntity pedestal, int incomingExperienceAmount);
    int canAcceptCountDust(BasePedestalBlockEntity pedestal, DustMagic incomingDust);

    /**
     * @param filterStack
     * Used to remove the nbt "filterqueue"
     */
    void removeFilterQueueHandler(ItemStack filterStack);

    /**
     * @param filterStack
     * Used to check the size of "filterqueue"
     */
    int filterQueueSize(ItemStack filterStack, int mode);

    /**
     * @param world
     * @param invBlock the inventory used to set the filters 'queue'
     * @return a list of itemstacks
     * Called when Crouch + Right Clicking a filter item on an inventory in world.
     * this grabs the storage containers handler, and iterates through it to build the list
     */
    List<ItemStack> buildFilterQueue(Level world, BlockPos invBlock);

    /**
     * @param filterStack
     * @param builtFilterQueueList
     * writes the list to the filters NBT "filterqueue"
     */
    void writeFilterQueueToNBT(ItemStack filterStack, List<ItemStack> builtFilterQueueList, int mode);

    /**
     * @param filterStack
     * @return a list of itemstacks in the filters queue
     * reads the list of itemstacks from the filters NBT "filterqueue"
     */
    List<ItemStack> readFilterQueueFromNBT(ItemStack filterStack, int mode);




    /**
     * @param filterStack
     * writes the bool filtertype to the NBT "filter_type"
     */
    void writeFilterTypeToNBT(ItemStack filterStack, int mode);

    /**
     * @param filterStack
     * @return filtertype
     * writes the bool filtertype from the NBT "filter_type"
     */
    boolean getFilterTypeFromNBT(ItemStack filterStack, int mode);



    /**
     * @param player
     * @param pedestal
     * When a player right clicks the FilterTool item on a pedestal, this is called.
     * Generally this will output filter info to the players chat in game.
     */
    void chatDetails(Player player, BasePedestalBlockEntity pedestal);
}
