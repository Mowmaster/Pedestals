package com.mowmaster.pedestals.Items.Filters;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.PedestalUtils.IItemMode;
import com.mowmaster.pedestals.PedestalUtils.References;
import cpw.mods.modlauncher.EnumerationHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.EnumUtils;

import java.util.List;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public interface IPedestalFilter extends IItemMode
{
    public boolean filterType = false;

    /**
     * @return
     * state 0|
     * state 1|false = whitelist
     * state 2|true = blacklist
     */

    boolean getFilterType();

    FilterDirection getFilterDirection();
    
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
     * @param filter pedestals filter
     * @param incomingStack incoming ItemStack
     * When this pedestal is about to receive an item, this method is called,
     * if it returns false the ItemStack will not be accepted.
     */
    boolean canAcceptItems(ItemStack filter, ItemStack incomingStack);
    boolean canAcceptFluids(ItemStack filter, FluidStack incomingFluidStack);
    boolean canAcceptEnergy(ItemStack filter, int incomingAmount);
    boolean canAcceptExperience(ItemStack filter, int incomingAmount);
    boolean canAcceptDust(ItemStack filter, DustMagic incomingDust);

    /**
     * @param pedestal pedestal tile filter is in
     * @param itemStackIncoming incoming ItemStack
     * @return ItemStack count allowed to be insert
     * When this pedestal is going to receive an ItemStack this is called.
     */
    int canAcceptCountItems(BasePedestalBlockEntity pedestal, ItemStack itemStackIncoming);
    int canAcceptCountFluids(BasePedestalBlockEntity pedestal, FluidStack incomingFluidStack);
    int canAcceptCountEnergy(BasePedestalBlockEntity pedestal, int incomingEnergyAmount);
    int canAcceptCountExperience(BasePedestalBlockEntity pedestal, int incomingExperienceAmount);
    int canAcceptCountDust(BasePedestalBlockEntity pedestal, DustMagic incomingDust);

    /**
     * @param pedestal pedestal tile filter is in
     * ---   Currently Not in Use   ---
     * When this pedestal is about to send an item it can prevent the item from being sent
     */
    boolean canSendItem(BasePedestalBlockEntity pedestal);

    /**
     * @param filterStack
     * Used to remove the nbt "filterqueue"
     */
    void removeFilterQueueHandler(ItemStack filterStack);

    /**
     * @param filterStack
     * Used to check the size of "filterqueue"
     */
    int filterQueueSize(ItemStack filterStack, ItemTransferMode mode);

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
    void writeFilterQueueToNBT(ItemStack filterStack, List<ItemStack> builtFilterQueueList, ItemTransferMode mode);

    /**
     * @param filterStack
     * @return a list of itemstacks in the filters queue
     * reads the list of itemstacks from the filters NBT "filterqueue"
     */
    List<ItemStack> readFilterQueueFromNBT(ItemStack filterStack, ItemTransferMode mode);




    /**
     * @param filterStack
     * writes the bool filtertype to the NBT "filter_type"
     */
    void writeFilterTypeToNBT(ItemStack filterStack, ItemTransferMode mode);

    /**
     * @param filterStack
     * @return filtertype
     * writes the bool filtertype from the NBT "filter_type"
     */
    boolean getFilterTypeFromNBT(ItemStack filterStack, ItemTransferMode mode);



    /**
     * @param player
     * @param pedestal
     * When a player right clicks the FilterTool item on a pedestal, this is called.
     * Generally this will output filter info to the players chat in game.
     */
    void chatDetails(Player player, BasePedestalBlockEntity pedestal);

    public static enum FilterDirection {
        INSERT,
        EXTRACT,
        NEUTRAL;

        private FilterDirection() {
        }

        public boolean insert() {
            return this == INSERT;
        }

        public boolean extract() {
            return this == EXTRACT;
        }

        public boolean neutral() {
            return this == NEUTRAL;
        }

        public Component componentDirection()
        {
            if(this.insert())return Component.translatable(MODID + ".enum.filterdirection_insert");
            if(this.extract())return Component.translatable(MODID + ".enum.filterdirection_extract");
            if(this.neutral())return Component.translatable(MODID + ".enum.filterdirection_neutral");
            return Component.translatable(MODID + ".enum.filterdirection_none");
        }
    }
}
