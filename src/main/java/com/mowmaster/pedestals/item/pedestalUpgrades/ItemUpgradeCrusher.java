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

public class ItemUpgradeCrusher extends ItemUpgradeBaseMachine
{
    public ItemUpgradeCrusher(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    @Nullable
    protected CrusherRecipeAdvanced getRecipeAdvanced(World world, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);
        //System.out.println(world == null ? null : world.getRecipeManager().getRecipe(CrusherRecipe.recipeType, inv, world).orElse(null));
        return world == null ? null : world.getRecipeManager().getRecipe(CrusherRecipeAdvanced.recipeType, inv, world).orElse(null);
    }

    protected Collection<ItemStack> getProcessResultsAdvanced(CrusherRecipeAdvanced recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResult()));
    }

    @Nullable
    protected CrusherRecipe getRecipe(World world, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);
        //System.out.println(world == null ? null : world.getRecipeManager().getRecipe(CrusherRecipe.recipeType, inv, world).orElse(null));
        return world == null ? null : world.getRecipeManager().getRecipe(CrusherRecipe.recipeType, inv, world).orElse(null);
    }

    protected Collection<ItemStack> getProcessResults(CrusherRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResult()));
    }


    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int getMaxFuelValue = getFuelBuffer(coinInPedestal);
            if(!hasMaxFuelSet(coinInPedestal) || readMaxFuelFromNBT(coinInPedestal) != getMaxFuelValue) {setMaxFuel(coinInPedestal, getMaxFuelValue);}

            int speed = getSmeltingSpeed(coinInPedestal);

            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal, world,pedestalPos,coinInPedestal);
                }
            }
        }
    }

    public void upgradeAction(PedestalTileEntity pedestal, World world, BlockPos posOfPedestal, ItemStack coinInPedestal)
    {
        Boolean isAdvanced = hasAdvancedInventoryTargeting(coinInPedestal);
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        int itemsPerSmelt = getItemTransferRate(coinInPedestal);

        ItemStack itemFromInv = ItemStack.EMPTY;
        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(isAdvanced)cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(!isInventoryEmpty(cap))
        {
            if(cap.isPresent())
            {
                IItemHandler handler = cap.orElse(null);
                TileEntity invToPullFrom = world.getTileEntity(posInventory);
                if (((isAdvanced && invToPullFrom instanceof PedestalTileEntity)||!(invToPullFrom instanceof PedestalTileEntity))?(false):(true)) {
                    itemFromInv = ItemStack.EMPTY;
                }
                else {
                    if(handler != null)
                    {
                        int i = getNextSlotWithItemsCapFiltered(pedestal,cap,getStackInPedestal(world,posOfPedestal));
                        if(i>=0)
                        {
                            int maxInSlot = handler.getSlotLimit(i);
                            itemFromInv = handler.getStackInSlot(i);
                            //Should work without catch since we null check this in our GetNextSlotFunction
                            Collection<ItemStack> jsonResults = (isAdvanced && getProcessResultsAdvanced(getRecipeAdvanced(world,itemFromInv)).size()>0)?(getProcessResultsAdvanced(getRecipeAdvanced(world,itemFromInv))):(getProcessResults(getRecipe(world,itemFromInv)));
                            //Just check to make sure our recipe output isnt air
                            ItemStack resultSmelted = (jsonResults.iterator().next().isEmpty())?(ItemStack.EMPTY):(jsonResults.iterator().next());
                            ItemStack itemFromPedestal = getStackInPedestal(world,posOfPedestal);
                            if(!resultSmelted.equals(ItemStack.EMPTY))
                            {
                                //Null check our slot again, which is probably redundant
                                if(handler.getStackInSlot(i) != null && !handler.getStackInSlot(i).isEmpty() && handler.getStackInSlot(i).getItem() != Items.AIR)
                                {
                                    int roomLeftInPedestal = 64-itemFromPedestal.getCount();
                                    if(itemFromPedestal.isEmpty() || itemFromPedestal.equals(ItemStack.EMPTY)) roomLeftInPedestal = 64;

                                    //Upgrade Determins ammout of items to smelt, but space count is determined by how much the item smelts into
                                    int itemInputsPerSmelt = itemsPerSmelt;
                                    int itemsOutputWhenStackSmelted = (itemInputsPerSmelt*resultSmelted.getCount());
                                    //Checks to see if pedestal can accept as many items as will be returned on smelt, if not reduce items being smelted
                                    if(roomLeftInPedestal < itemsOutputWhenStackSmelted)
                                    {
                                        itemInputsPerSmelt = Math.floorDiv(roomLeftInPedestal, resultSmelted.getCount());
                                    }
                                    //Checks to see how many items are left in the slot IF ITS UNDER the allowedTransferRate then sent the max rate to that.
                                    if(itemFromInv.getCount() < itemInputsPerSmelt) itemInputsPerSmelt = itemFromInv.getCount();

                                    itemsOutputWhenStackSmelted = (itemInputsPerSmelt*resultSmelted.getCount());
                                    ItemStack copyIncoming = resultSmelted.copy();
                                    copyIncoming.setCount(itemsOutputWhenStackSmelted);
                                    int fuelToConsume = burnTimeCostPerItemSmelted * itemInputsPerSmelt;
                                    TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                                    if(pedestalInv instanceof PedestalTileEntity) {
                                        PedestalTileEntity ped = ((PedestalTileEntity) pedestalInv);
                                        //Checks to make sure we have fuel to smelt everything
                                        if(removeFuel(ped,fuelToConsume,true))
                                        {
                                            handler.extractItem(i,itemInputsPerSmelt ,false );
                                            removeFuel(ped,fuelToConsume,false);
                                            if(!ped.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                            ped.addItem(copyIncoming);
                                        }
                                        //If we done have enough fuel to smelt everything then reduce size of smelt
                                        else
                                        {
                                            //gets fuel left
                                            int fuelLeft = getFuelStored(coinInPedestal);
                                            if(fuelLeft>0)
                                            {
                                                //this = a number over 1 unless fuelleft < burnTimeCostPeritemSmelted
                                                itemInputsPerSmelt = Math.floorDiv(fuelLeft,burnTimeCostPerItemSmelted );
                                                if(itemInputsPerSmelt >=1)
                                                {
                                                    //System.out.println(itemInputsPerSmelt);
                                                    fuelToConsume = burnTimeCostPerItemSmelted * itemInputsPerSmelt;
                                                    itemsOutputWhenStackSmelted = (itemInputsPerSmelt*resultSmelted.getCount());
                                                    copyIncoming.setCount(itemsOutputWhenStackSmelted);

                                                    handler.extractItem(i,itemInputsPerSmelt ,false );
                                                    removeFuel(ped,fuelToConsume,false);
                                                    if(!ped.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                                    ped.addItem(copyIncoming);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else
                            {
                                TileEntity pedestalInv = world.getTileEntity(posOfPedestal);
                                if(pedestalInv instanceof PedestalTileEntity) {
                                    PedestalTileEntity ped = ((PedestalTileEntity) pedestalInv);
                                    if(ped.getItemInPedestal().equals(ItemStack.EMPTY))
                                    {
                                        ItemStack copyItemFromInv = itemFromInv.copy();
                                        handler.extractItem(i,itemFromInv.getCount(),false);
                                        ped.addItem(copyItemFromInv);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static final Item CRUSHER = new ItemUpgradeCrusher(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/crusher"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(CRUSHER);
    }
}
