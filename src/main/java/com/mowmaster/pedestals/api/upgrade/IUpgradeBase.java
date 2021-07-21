package com.mowmaster.pedestals.api.upgrade;

//Chat with InfinityRader about API's
//https://github.com/AgriCraft/AgriCraft/blob/master/src/main/java/com/infinityraider/agricraft/api/v1/crop/IAgriCrop.java
//https://discord.com/channels/230701400080777227/230709955324280832/866772319312543764
//https://discord.com/channels/230701400080777227/230709955324280832/866776324420272158

//Chat with Loth about API's
//https://discord.com/channels/749302798797242449/749302799644229715/866807219550814238

import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public interface IUpgradeBase
{

    boolean canAcceptOpSpeed();

    boolean canAcceptCapacity();

    boolean canAcceptMagnet();

    boolean canAcceptRange();

    boolean canAcceptAdvanced();

    boolean canAcceptArea();
    // ======================================
    // CUSTOM INVENTORY METHODS - These methods make the cobble gen and Item Tank Possible, Its a huge hack and may change in the future.
    // ======================================
    boolean customIsValid(PedestalTileEntity pedestal, int slot, @Nonnull ItemStack incomingItemStack);

    //defaults to items max stack size or pedestals slot limit, if pedestal exists
    int canAcceptCount(World world, BlockPos pos, ItemStack itemStackCountInPedestal, ItemStack incomingItemStack);

    //Defaults top -1
    int customSlotLimit(PedestalTileEntity pedestal);

    //Defaults to Items.COMMAND_BLOCK
    ItemStack customStackInSlot(PedestalTileEntity pedestal, ItemStack stackInSlot );

    ItemStack customInsertItem(PedestalTileEntity pedestal, ItemStack stackToInsert, boolean simulate);

    ItemStack customExtractItem(PedestalTileEntity pedestal, int extractAmount, boolean simulate);

    boolean canSendItem(PedestalTileEntity pedestal);




    // ============================================
    // END CUSTOM INVENTORY METHODS
    // ============================================
    //@Override

    //Not Currenty In Use
    //Used when Entities Collide with Pedestal
    void actionOnCollideWithBlock(PedestalTileEntity tilePedestal, Entity entityIn);

    // Adds Player UUID onto the Upgrade for security/greifing protection stuff.
    void setPlayerOnCoin(ItemStack upgradeStack, PlayerEntity playerEntity);

    //Removes some data from upgrades on item extract
    void removePlayerFromCoin(ItemStack upgradeItem);
    void removeWorkQueueFromCoin(ItemStack upgradeItem);
    void removeWorkQueueTwoFromCoin(ItemStack upgradeItem);
    void removeStoredIntFromCoin(ItemStack upgradeItem);
    void removeStoredIntTwoFromCoin(ItemStack upgradeItem);
    void removeFilterQueueHandler(ItemStack upgradeItem);
    void removeFilterBlock(ItemStack upgradeItem);
    void removeInventoryQueue(ItemStack upgradeItem);
    void removeCraftingQueue(ItemStack upgradeItem);
    void removeOutputIngredientMap(ItemStack upgradeItem);
    void removeFilterChangeUpdated(ItemStack upgradeItem);
    void removeToolChangeUpdated(ItemStack upgradeItem);

    void setFilterChangeUpdate(ItemStack upgradeStack);

    boolean canAcceptItem(World world, BlockPos receiverPedestal, ItemStack incomingItemStack);

    //Main Method Call that allows the upgrade to do things
    void updateAction(World world, PedestalTileEntity pedestal);

    //Used to display particles on a pedestal
    void onRandomDisplayTick(PedestalTileEntity pedestal, int ticker, BlockState pedestalState, World world, BlockPos pedestalPos, Random randomNumber);
}
