package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.item.ItemCraftingPlaceholder;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeFluidCrafter extends ItemUpgradeBaseFluid
{
    public ItemUpgradeFluidCrafter(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public int getItemTransferRate(ItemStack stack)
    {
        int itemsPerSmelt = 1;
        switch (getCapacityModifier(stack))
        {
            case 0:
                itemsPerSmelt = 1;
                break;
            case 1:
                itemsPerSmelt=2;
                break;
            case 2:
                itemsPerSmelt = 4;
                break;
            case 3:
                itemsPerSmelt = 8;
                break;
            case 4:
                itemsPerSmelt = 12;
                break;
            case 5:
                itemsPerSmelt=16;
                break;
            default: itemsPerSmelt=1;
        }

        return  itemsPerSmelt;
    }

    public int getGridSize(ItemStack itemStack)
    {
        int gridSize = 0;
        if(itemStack.getItem().equals(ItemUpgradeFluidCrafter.FLUIDCRAFTER_ONE)){gridSize = 1;}
        else if(itemStack.getItem().equals(ItemUpgradeFluidCrafter.FLUIDCRAFTER_TWO)){gridSize = 2;}
        else if(itemStack.getItem().equals(ItemUpgradeFluidCrafter.FLUIDCRAFTER_THREE)){gridSize = 3;}
        else{gridSize = 1;}

        return gridSize;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos pedestalPos = pedestal.getPos();

        if(!world.isRemote)
        {
            int getMaxFluidValue = getFluidbuffer(coinInPedestal);
            if(!hasMaxFluidSet(coinInPedestal) || readMaxFluidFromNBT(coinInPedestal) != getMaxFluidValue) {setMaxFluid(coinInPedestal, getMaxFluidValue);}

            int speed = getOperationSpeed(coinInPedestal);

            if(!world.isBlockPowered(pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal);
                }
            }
        }

    }

    public static IRecipe<CraftingInventory> findRecipe(CraftingInventory inv, World world) {
        return world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inv, world).orElse(null);
    }

    public void upgradeAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos posOfPedestal = pedestal.getPos();
        int gridSize = getGridSize(coinInPedestal);
        int intBatchCraftingSize = getItemTransferRate(coinInPedestal);
        ItemStack itemFromInv = ItemStack.EMPTY;
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        int intGridCount = gridSize*gridSize;

        //Dont bother unless pedestal is empty
        //Yes i'm being lazy here...

        if(itemInPedestal.isEmpty())
        {
            LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
            if(hasAdvancedInventoryTargeting(coinInPedestal))cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
            //Setup new Container for our Crafting Grid Size
            CraftingInventory craft = new CraftingInventory(new Container(null, -1) {
                @Override
                public boolean canInteractWith(PlayerEntity playerIn) {
                    return false;
                }
            }, gridSize, gridSize);

            CraftingInventory craftAvailable = new CraftingInventory(new Container(null, -1) {
                @Override
                public boolean canInteractWith(PlayerEntity playerIn) {
                    return false;
                }
            }, gridSize, gridSize);

            //Get Inventory Below
            if(cap.isPresent()) {
                IItemHandler handler = cap.orElse(null);
                TileEntity invToPullFrom = world.getTileEntity(posInventory);
                int intInventorySlotCount = handler.getSlots();//normal chests return value of 1-27
                if (invToPullFrom instanceof PedestalTileEntity) {
                    itemFromInv = ItemStack.EMPTY;
                }
                else
                {
                    if(handler != null)
                    {
                        //Makes sure we have more slots then the recipe requires
                        if(intInventorySlotCount>=intGridCount)
                        {
                            // Get Next iteration to craft
                            int intGetNextIteration = getIntValueFromPedestal(world,posOfPedestal);//Default value is 0
                            if(intGetNextIteration == 0)
                            {
                                intGetNextIteration = 1;
                            }
                            int intSlotToStartFrom = (intGetNextIteration*intGridCount)-intGridCount;//use int i= intSlotToStartFrom in for-loop
                            //If starting slot will be bigger then our inventory size
                            int intSlotToEndBefore = (intGetNextIteration*intGridCount);//use i < intSlotToEndBefore in for-loop
                            //if ending slot is > then total slots then it would error, so reset things
                            if(intSlotToEndBefore > intInventorySlotCount)
                            {
                                //reset back to 1
                                intGetNextIteration = 1;
                                intSlotToStartFrom = (intGetNextIteration*intGridCount)-intGridCount;//use int i= intSlotToStartFrom in for-loop
                                intSlotToEndBefore = (intGetNextIteration*intGridCount);//use i < intSlotToEndBefore in for-loop
                            }

                            int intCraftingSlot = 0;
                            int intCurrentEstFluidUsed = 0;
                            for(int i = intSlotToStartFrom; i < intSlotToEndBefore; i++) {
                                ItemStack stackItemInSlot = handler.getStackInSlot(i);

                                //If the item Stack has enough items to craft with
                                if(stackItemInSlot.isEmpty() ||stackItemInSlot.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER))
                                {
                                    craft.setInventorySlotContents(intCraftingSlot, ItemStack.EMPTY);
                                    craftAvailable.setInventorySlotContents(intCraftingSlot, ItemStack.EMPTY);
                                    intCraftingSlot++;
                                }
                                else if(stackItemInSlot.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER_BUCKET))
                                {
                                    //Make the recipe invalid if no fluid exists???
                                    ItemStack bucketStack = new ItemStack(Items.BARRIER);
                                    intCurrentEstFluidUsed+=FluidAttributes.BUCKET_VOLUME;
                                    if(removeFluid(pedestal,coinInPedestal,intCurrentEstFluidUsed,true))
                                    {
                                        FluidStack fluidToBucket = getFluidStored(coinInPedestal);
                                        Item bucket = fluidToBucket.getFluid().getFilledBucket();
                                        bucketStack = new ItemStack(bucket);
                                    }
                                    craft.setInventorySlotContents(intCraftingSlot, bucketStack);
                                    craftAvailable.setInventorySlotContents(intCraftingSlot, bucketStack);
                                    intCraftingSlot++;
                                }
                                else if(stackItemInSlot.getMaxStackSize()==1 || stackItemInSlot.isDamageable())
                                {
                                    //Since recipe has a container item we have to limit it to 1 craft
                                    intBatchCraftingSize = 1;
                                    craft.setInventorySlotContents(intCraftingSlot,stackItemInSlot);
                                    craftAvailable.setInventorySlotContents(intCraftingSlot,stackItemInSlot);
                                    intCraftingSlot++;
                                }
                                //first check if an item is in the slot
                                else if(stackItemInSlot.getCount() > 0)
                                {
                                    //next check to make sure we have more than enough to craft the recipe
                                    if(stackItemInSlot.getCount() > (intBatchCraftingSize))
                                    {
                                        craft.setInventorySlotContents(intCraftingSlot,stackItemInSlot);
                                    }
                                    else
                                    {
                                        intBatchCraftingSize = (stackItemInSlot.getCount()-1);
                                        if(intBatchCraftingSize>0)craft.setInventorySlotContents(intCraftingSlot,stackItemInSlot);
                                    }
                                    craftAvailable.setInventorySlotContents(intCraftingSlot,stackItemInSlot);
                                    intCraftingSlot++;
                                }
                            }
                            //Checks to make sure we have enough slots set for out recipe
                            if(craft.getSizeInventory() >= intGridCount)
                            {

                                IRecipe recipe = findRecipe(craft,world);
                                //This is the set recipe, which might differ from the recipe given current available inputs
                                IRecipe setRecipe = findRecipe(craftAvailable,world);
                                //If recipe is valid and we can craft recipe and stick it in pedestal
                                if(recipe  != null &&  recipe.matches(craft, world) && recipe.matches(craftAvailable,world)) {
                                    //Set ItemStack with recipe result
                                    ItemStack stackRecipeResult = recipe.getCraftingResult(craft);
                                    int intRecipeResultCount = stackRecipeResult.getCount();

                                    //Check if pedestal can hold the crafting result, if not then set the batch to be small enough that it can fit
                                    if((stackRecipeResult.getCount() * intBatchCraftingSize) > 64)
                                    {
                                        intBatchCraftingSize = 64/intRecipeResultCount;
                                    }

                                    if((intCurrentEstFluidUsed*intBatchCraftingSize)>getFluidStored(coinInPedestal).getAmount())
                                    {
                                        intBatchCraftingSize = getFluidStored(coinInPedestal).getAmount()/intCurrentEstFluidUsed;
                                    }

                                    //Loop through inventory again to remove crafted materials used
                                    for(int i = 0; i < craft.getSizeInventory(); i++) {


                                        ItemStack stackInRecipe = craft.getStackInSlot(i);
                                        int intGetActualSlot = ((intGetNextIteration*intGridCount)-intGridCount)+i;
                                        ItemStack stackItemInSlot = handler.getStackInSlot(intGetActualSlot);
                                        FluidStack fluidToBucket = getFluidStored(coinInPedestal);
                                        Item bucket = fluidToBucket.getFluid().getFilledBucket();
                                        if(stackInRecipe.getItem().equals(bucket))
                                        {
                                            if(removeFluid(pedestal,coinInPedestal,FluidAttributes.BUCKET_VOLUME*intBatchCraftingSize,true))
                                            {
                                                removeFluid(pedestal,coinInPedestal,FluidAttributes.BUCKET_VOLUME*intBatchCraftingSize,false);
                                                continue;
                                            }
                                        }

                                        if(stackInRecipe.isEmpty()  || stackInRecipe.getItem() instanceof ItemCraftingPlaceholder)
                                            continue;

                                        if(stackInRecipe.getItem().hasContainerItem(stackInRecipe))
                                        {
                                            //System.out.println(stackInRecipe.getDisplayName());
                                            ItemStack container = stackInRecipe.getItem().getContainerItem(stackInRecipe);
                                            if(!world.isRemote)
                                            {
                                                world.addEntity(new ItemEntity(world,getPosOfBlockBelow(world,posOfPedestal,-1).getX() + 0.5,getPosOfBlockBelow(world,posOfPedestal,-1).getY()+ 0.5,getPosOfBlockBelow(world,posOfPedestal,-1).getZ()+ 0.5,container));
                                            }
                                            handler.extractItem(intGetActualSlot,intBatchCraftingSize,false);
                                        }
                                        else
                                        {
                                            handler.extractItem(intGetActualSlot,intBatchCraftingSize,false);
                                        }
                                    }

                                    int intBatchCraftedAmount = stackRecipeResult.getCount() * intBatchCraftingSize;
                                    stackRecipeResult.setCount(intBatchCraftedAmount);
                                    world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                    addToPedestal(world,posOfPedestal,stackRecipeResult);
                                }
                            }
                            setIntValueToPedestal(world,posOfPedestal,(intGetNextIteration+1));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        FluidStack fluidStored = getFluidStored(stack);
        TranslationTextComponent fluidLabel = new TranslationTextComponent(getTranslationKey() + ".chat_fluidlabel");
        if(!fluidStored.isEmpty())
        {
            TranslationTextComponent fluid = new TranslationTextComponent(getTranslationKey() + ".chat_fluid");
            TranslationTextComponent fluidSplit = new TranslationTextComponent(getTranslationKey() + ".chat_fluidseperator");
            fluid.appendString("" + fluidStored.getDisplayName().getString() + "");
            fluid.appendString(fluidSplit.getString());
            fluid.appendString("" + fluidStored.getAmount() + "");
            fluid.appendString(fluidLabel.getString());
            fluid.mergeStyle(TextFormatting.BLUE);
            player.sendMessage(fluid,Util.DUMMY_UUID);
        }

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString("" +  getItemTransferRate(stack) + "");
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed, Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        FluidStack fluidStored = getFluidStored(stack);
        TranslationTextComponent fluidLabel = new TranslationTextComponent(getTranslationKey() + ".chat_fluidlabel");
        if(!fluidStored.isEmpty())
        {
            TranslationTextComponent fluid = new TranslationTextComponent(getTranslationKey() + ".chat_fluid");
            TranslationTextComponent fluidSplit = new TranslationTextComponent(getTranslationKey() + ".chat_fluidseperator");
            fluid.appendString("" + fluidStored.getDisplayName().getString() + "");
            fluid.appendString(fluidSplit.getString());
            fluid.appendString("" + fluidStored.getAmount() + "");
            fluid.appendString(fluidLabel.getString());
            fluid.mergeStyle(TextFormatting.BLUE);
            tooltip.add(fluid);
        }

        TranslationTextComponent fluidcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_fluidcapacity");
        fluidcapacity.appendString(""+ getFluidbuffer(stack) +"");
        fluidcapacity.appendString(fluidLabel.getString());
        fluidcapacity.mergeStyle(TextFormatting.AQUA);
        tooltip.add(fluidcapacity);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString("" + getItemTransferRate(stack) + "");
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item FLUIDCRAFTER_ONE = new ItemUpgradeFluidCrafter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluidcrafter1"));
    public static final Item FLUIDCRAFTER_TWO = new ItemUpgradeFluidCrafter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluidcrafter2"));
    public static final Item FLUIDCRAFTER_THREE = new ItemUpgradeFluidCrafter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/fluidcrafter3"));


    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(FLUIDCRAFTER_ONE);
        event.getRegistry().register(FLUIDCRAFTER_TWO);
        event.getRegistry().register(FLUIDCRAFTER_THREE);
    }

}
