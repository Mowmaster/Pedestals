package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.recipes.CrusherRecipe;
import com.mowmaster.pedestals.recipes.CrusherRecipeAdvanced;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeEnergyCrusher extends ItemUpgradeBaseEnergyMachine {
    // public final int rfCostPerItemSmelted = 2500; (From Base Machine Energy Class)
    public ItemUpgradeEnergyCrusher(Properties builder) {
        super(builder.group(PEDESTALS_TAB));
    }

    @Override
    public boolean canAcceptAdvanced() {
        return true;
    }

    //Speed - Default
    //Capacity - Increase Items Smelted at once
    @Override
    public boolean canAcceptCapacity() {
        return true;
    }


    @Nullable
    protected CrusherRecipeAdvanced getRecipeAdvanced(World world, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);
        return world == null ? null : world.getRecipeManager().getRecipe(CrusherRecipeAdvanced.recipeType, inv, world).orElse(null);
    }

    protected Collection<ItemStack> getProcessResultsAdvanced(CrusherRecipeAdvanced recipe) {
        return (recipe == null) ? (Arrays.asList(ItemStack.EMPTY)) : (Collections.singleton(recipe.getResult()));
    }

    //Recipe Bits like normal crusher
    @Nullable
    protected CrusherRecipe getRecipe(World world, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);
        return world == null ? null : world.getRecipeManager().getRecipe(CrusherRecipe.recipeType, inv, world).orElse(null);
    }

    //Recipe Bits like normal crusher
    protected Collection<ItemStack> getProcessResults(CrusherRecipe recipe) {
        return (recipe == null) ? (Arrays.asList(ItemStack.EMPTY)) : (Collections.singleton(recipe.getResult()));
    }

    public void updateAction(World world, PedestalTileEntity pedestal) {
        if (!world.isRemote) {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            int speed = getSmeltingSpeed(coinInPedestal);

            if (world.getGameTime() % speed == 0) {
                BlockPos pedestalPos = pedestal.getPos();
                BlockPos posInventory = getPosOfBlockBelow(world, pedestalPos, 1);
                boolean isAdvanced = hasAdvancedInventoryTargeting(coinInPedestal);
                LazyOptional<IItemHandler> cap = isAdvanced ?
                        findItemHandlerAtPosAdvanced(world, posInventory, getPedestalFacing(world, pedestalPos), true)
                        : findItemHandlerAtPos(world, posInventory, getPedestalFacing(world, pedestalPos), true);

                if (isInventoryEmpty(cap) || !cap.isPresent()) {
                    return;
                }

                if (!pedestal.isPedestalBlockPowered(world, pedestalPos)) {
                    //Just receives Energy, then exports it to machines, not other pedestals
                    upgradeAction(pedestal, world, pedestalPos, coinInPedestal, cap);
                }
            }
        }
    }

    //Crusher Normal Action
    public void upgradeAction(PedestalTileEntity pedestal, World world, BlockPos posOfPedestal, ItemStack coinInPedestal, LazyOptional<IItemHandler> cap) {
        boolean isAdvanced = hasAdvancedInventoryTargeting(coinInPedestal);
        //Set Default Energy Buffer
        int getMaxEnergyValue = getEnergyBuffer(coinInPedestal);
        if (!hasMaxEnergySet(coinInPedestal) || readMaxEnergyFromNBT(coinInPedestal) != getMaxEnergyValue) {
            setMaxEnergy(coinInPedestal, getMaxEnergyValue);
        }

        BlockPos posInventory = getPosOfBlockBelow(world, posOfPedestal, 1);
        int itemsPerSmelt = getItemTransferRate(coinInPedestal);

        ItemStack itemFromInv;

        IItemHandler handler = cap.orElse(null);
        TileEntity invToPullFrom = world.getTileEntity(posInventory);
        if (isAdvanced || !(invToPullFrom instanceof PedestalTileEntity)) {
            int i = getNextSlotWithItemsCapFiltered(pedestal, cap, getStackInPedestal(world, posOfPedestal));
            if (i >= 0) {
                int maxInSlot = handler.getSlotLimit(i);
                itemFromInv = handler.getStackInSlot(i);
                //Should work without catch since we null check this in our GetNextSlotFunction
                Collection<ItemStack> jsonResults = getProcessResults(getRecipe(world, itemFromInv));
                Collection<ItemStack> jsonResultsAdvanced = getProcessResultsAdvanced(getRecipeAdvanced(world, itemFromInv));

                //Just check to make sure our recipe output isnt air
                ItemStack resultSmelted = (jsonResults.iterator().next().isEmpty()) ? (ItemStack.EMPTY) : (jsonResults.iterator().next());
                ItemStack resultSmeltedAdvanced = (jsonResultsAdvanced.iterator().next().isEmpty()) ? (ItemStack.EMPTY) : (jsonResultsAdvanced.iterator().next());
                if (isAdvanced && !resultSmeltedAdvanced.equals(ItemStack.EMPTY))
                    resultSmelted = resultSmeltedAdvanced;
                ItemStack itemFromPedestal = getStackInPedestal(world, posOfPedestal);
                if (!resultSmelted.equals(ItemStack.EMPTY)) {
                    //Null check our slot again, which is probably redundant
                    handler.getStackInSlot(i);
                    if (!handler.getStackInSlot(i).isEmpty() && handler.getStackInSlot(i).getItem() != Items.AIR) {
                        int roomLeftInPedestal = 64 - itemFromPedestal.getCount();
                        if (itemFromPedestal.isEmpty() || itemFromPedestal.equals(ItemStack.EMPTY))
                            roomLeftInPedestal = 64;

                        //Upgrade Determins ammout of items to smelt, but space count is determined by how much the item smelts into
                        int itemInputsPerSmelt = itemsPerSmelt;
                        int itemsOutputWhenStackSmelted = (itemInputsPerSmelt * resultSmelted.getCount());
                        //Checks to see if pedestal can accept as many items as will be returned on smelt, if not reduce items being smelted
                        if (roomLeftInPedestal < itemsOutputWhenStackSmelted) {
                            itemInputsPerSmelt = Math.floorDiv(roomLeftInPedestal, resultSmelted.getCount());
                        }
                        //Checks to see how many items are left in the slot IF ITS UNDER the allowedTransferRate then sent the max rate to that.
                        if (itemFromInv.getCount() < itemInputsPerSmelt)
                            itemInputsPerSmelt = itemFromInv.getCount();

                        itemsOutputWhenStackSmelted = (itemInputsPerSmelt * resultSmelted.getCount());
                        ItemStack copyIncoming = resultSmelted.copy();
                        copyIncoming.setCount(itemsOutputWhenStackSmelted);
                        //RFFuel Cost
                        int fuelToConsume = rfCostPerItemSmelted * itemInputsPerSmelt;

                        //Checks to make sure we have rffuel to smelt everything
                        int fuelLeft = getEnergyStored(pedestal.getCoinOnPedestal());
                        if (hasEnergy(coinInPedestal) && removeEnergyFuel(pedestal, fuelToConsume, true) >= 0) {
                            if (!handler.extractItem(i, itemInputsPerSmelt, true).isEmpty()) {
                                handler.extractItem(i, itemInputsPerSmelt, false);
                                removeEnergyFuel(pedestal, fuelToConsume, false);
                                if (!pedestal.hasMuffler())
                                    world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                pedestal.addItemOverride(copyIncoming);
                            }
                        }
                        //If we done have enough fuel to smelt everything then reduce size of smelt
                        else {
                            //this = a number over 1 unless fuelleft < burnTimeCostPeritemSmelted
                            itemInputsPerSmelt = Math.floorDiv(fuelLeft, rfCostPerItemSmelted);
                            if (itemInputsPerSmelt >= 1) {
                                fuelToConsume = rfCostPerItemSmelted * itemInputsPerSmelt;
                                itemsOutputWhenStackSmelted = (itemInputsPerSmelt * resultSmelted.getCount());
                                copyIncoming.setCount(itemsOutputWhenStackSmelted);

                                if (!handler.extractItem(i, itemInputsPerSmelt, true).isEmpty()) {
                                    handler.extractItem(i, itemInputsPerSmelt, false);
                                    removeEnergyFuel(pedestal, fuelToConsume, false);
                                    if (!pedestal.hasMuffler())
                                        world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                    pedestal.addItemOverride(copyIncoming);
                                }
                            }
                        }
                    }
                } else {

                    if (pedestal.getItemInPedestal().equals(ItemStack.EMPTY)) {
                        ItemStack copyItemFromInv = itemFromInv.copy();
                        if (!handler.extractItem(i, itemFromInv.getCount(), true).isEmpty()) {
                            handler.extractItem(i, itemFromInv.getCount(), false);
                            pedestal.addItemOverride(copyItemFromInv);
                        }
                    }

                }
            }
        }
    }

    public static final Item RFCRUSHER = new ItemUpgradeEnergyCrusher(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/rfcrusher"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(RFCRUSHER);
    }
}
