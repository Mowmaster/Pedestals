package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.item.ItemCraftingPlaceholder;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import com.mowmaster.pedestals.references.Reference;
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
import java.util.ArrayList;
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

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int storedTwo = readStoredIntTwoFromNBT(coinInPedestal);
            int craftingCount = readCraftingQueueFromNBT(coinInPedestal).size();
            int getMaxFluidValue = getFluidbuffer(coinInPedestal);
            if(!hasMaxFluidSet(coinInPedestal) || readMaxFluidFromNBT(coinInPedestal) != getMaxFluidValue) {setMaxFluid(coinInPedestal, getMaxFluidValue);}

            int speed = getOperationSpeed(coinInPedestal);

            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                //Dont run if theres nothing queued
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal);
                }

                //Basically if our crafting queue has been empty for a while, every 5 seconds refresh it
                if(storedTwo>=craftingCount)
                {
                    if (world.getGameTime()%100 == 0) {
                        onPedestalNeighborChanged(pedestal);
                    }
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
        BlockPos pedestalPos = pedestal.getPos();
        ItemStack coin = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        int gridSize = getGridSize(coin);
        int intBatchCraftingSize = getItemTransferRate(coin);
        ItemStack itemFromInv = ItemStack.EMPTY;
        BlockPos posInventory = getPosOfBlockBelow(world,pedestalPos,1);
        int intGridCount = gridSize*gridSize;

        List<ItemStack> stackCurrent = readInventoryQueueFromNBT(coin);
        List<ItemStack> craftingCurrent = readCraftingQueueFromNBT(coin);

        //Dont bother unless pedestal is empty
        //Yes i'm being lazy here...

        if(itemInPedestal.isEmpty())
        {
            LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, pedestalPos),true);

            if(!isInventoryEmpty(cap))
            {
                //Get Inventory Below
                if(cap.isPresent()) {
                    IItemHandler handler = cap.orElse(null);
                    TileEntity invToPullFrom = world.getTileEntity(posInventory);
                    int intInventorySlotCount = handler.getSlots();//normal chests return value of 1-27
                    if (((hasAdvancedInventoryTargeting(coin) && invToPullFrom instanceof PedestalTileEntity)||!(invToPullFrom instanceof PedestalTileEntity))?(false):(true)) {
                        itemFromInv = ItemStack.EMPTY;
                    }
                    else
                    {
                        if(handler != null)
                        {
                            //If they Dont Match, this should force that
                            //System.out.println("CurrentStackSizeOfInv: "+stackCurrent.size());
                            if(stackCurrent.size() != intInventorySlotCount)
                            {
                                List<ItemStack> stackIn = buildInventoryQueue(pedestal);
                                writeInventoryQueueToNBT(coin,stackIn);
                                buildAndWriteCraftingQueue(pedestal,stackIn);
                            }

                            //System.out.println("CurrentCraftingSize: "+craftingCurrent.size());
                            if(intInventorySlotCount >= intGridCount)
                            {
                                // Get Next iteration to craft
                                int intGetNextIteration = getStoredInt(coin);//Default value is 0
                                if (intGetNextIteration == 0)
                                {
                                    intGetNextIteration = 1;//Start at 1 since thats the start for the number of recipes we have}
                                }
                                //Makes sure Out Estimated and Actual Inventories Match AND we have more slots then the recipe requires
                                if(craftingCurrent.size() > 0)
                                {

                                    int intSlotToStartFrom = (intGetNextIteration * intGridCount) - intGridCount;//(Recipe# * GridSize) - GridSize
                                    //If starting slot will be bigger then our inventory size
                                    int intSlotToEndBefore = (intGetNextIteration * intGridCount);//use i < intSlotToEndBefore in for-loop
                                    //if ending slot is > then total slots then it would error, so reset things
                                    if (intSlotToEndBefore > intInventorySlotCount) {
                                        //reset back to 1
                                        intGetNextIteration = 1;
                                        intSlotToStartFrom = (intGetNextIteration * intGridCount) - intGridCount;//use int i= intSlotToStartFrom in for-loop
                                        intSlotToEndBefore = (intGetNextIteration * intGridCount);//use i < intSlotToEndBefore in for-loop
                                    }

                                    //System.out.println("WhichRecipeIndex: "+intGetNextIteration);
                                    //System.out.println("StartingSlot: "+intSlotToStartFrom);
                                    //System.out.println("EndingSlot: "+intSlotToEndBefore);

                                    //copy so we dont set the stack size any higher
                                    ItemStack getRecipe = craftingCurrent.get(intGetNextIteration-1).copy();

                                    //System.out.println("GetRecipe: "+getRecipe);

                                    if(!getRecipe.isEmpty())
                                    {
                                        int fluidBucketsNeeded = 0;
                                        //Calc max stack size craftable
                                        for(int s=intSlotToStartFrom;s<intSlotToEndBefore;s++)
                                        {
                                            ItemStack getIngredientStack = stackCurrent.get(s);
                                            int stackSize = getIngredientStack.getCount();

                                            if(getIngredientStack.getItem().hasContainerItem(getIngredientStack))
                                            {
                                                if(doItemsMatch(getIngredientStack.getItem().getContainerItem(getIngredientStack),getIngredientStack))
                                                {
                                                    if(getIngredientStack.isDamageable())
                                                    {
                                                        int maxdamage = getIngredientStack.getMaxDamage();
                                                        int damage = getIngredientStack.getDamage();
                                                        int durabilityCurrent = maxdamage-damage;
                                                        if(durabilityCurrent<intBatchCraftingSize)
                                                        {
                                                            intBatchCraftingSize = durabilityCurrent;
                                                        }
                                                    }
                                                }
                                                else
                                                {
                                                    if(getIngredientStack.getMaxStackSize() < intBatchCraftingSize)
                                                    {
                                                        intBatchCraftingSize = getIngredientStack.getMaxStackSize();
                                                    }
                                                    else if(stackSize < intBatchCraftingSize)
                                                    {
                                                        intBatchCraftingSize = stackSize-1;//Gotta set the min - 1 to keep the 'pattern'
                                                    }
                                                }
                                            }
                                            if(getIngredientStack.isEmpty() || getIngredientStack.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER))
                                            {
                                                continue;
                                            }
                                            else if(getIngredientStack.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER_BUCKET))
                                            {
                                                fluidBucketsNeeded++;
                                                continue;
                                            }
                                            else if(stackSize < intBatchCraftingSize)
                                            {
                                                intBatchCraftingSize = (stackSize-1);//Gotta set the min - 1 to keep the 'pattern'
                                            }
                                            else if(stackSize==1)
                                            {
                                                intBatchCraftingSize = 0;//Gotta set the min - 1 to keep the 'pattern'
                                            }
                                        }

                                        int fluidStoredAmount = getFluidStored(coin).getAmount();
                                        if((FluidAttributes.BUCKET_VOLUME * fluidBucketsNeeded * intBatchCraftingSize) > fluidStoredAmount)
                                        {
                                            intBatchCraftingSize = Math.floorDiv(fluidStoredAmount,(FluidAttributes.BUCKET_VOLUME * fluidBucketsNeeded));
                                        }

                                        //System.out.println("BatchCraftingSize: "+intBatchCraftingSize);

                                        //Means there is something to craft, realistically since getRecipe.isEmpty is checked already, this should never be < 0
                                        if(intBatchCraftingSize > 0)
                                        {

                                            List<ItemStack> extractedItemsList = new ArrayList<>();
                                            ItemStack returnedStack = ItemStack.EMPTY;
                                            int intRecipeResultCount = getRecipe.getCount();
                                            int intBatchCraftedAmount = intRecipeResultCount * intBatchCraftingSize;

                                            //Check if pedestal can hold the crafting result, if not then set the batch to be small enough that it can fit
                                            if (intBatchCraftedAmount > 64) {
                                                intBatchCraftingSize = Math.floorDiv(64, intRecipeResultCount);
                                            }

                                            //Loop through inventory again to remove crafted materials used
                                            for (int s=intSlotToStartFrom;s<intSlotToEndBefore;s++) {

                                                //int intGetActualSlot = ((intGetNextIteration * intGridCount) - intGridCount) + s;//Love How Good this formula works!
                                                ItemStack stackInHandler = handler.getStackInSlot(s);
                                                ItemStack stackInRecipe = stackCurrent.get(s);

                                                if (stackInRecipe.isEmpty() || stackInRecipe.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER))
                                                {
                                                    continue;
                                                }
                                                else if(stackInRecipe.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER_BUCKET))
                                                {
                                                    removeFluid(pedestal,coin,(fluidBucketsNeeded*FluidAttributes.BUCKET_VOLUME*intBatchCraftingSize),false);
                                                    continue;
                                                }
                                                else if (stackInRecipe.getItem().hasContainerItem(stackInRecipe)) {
                                                    //Will Now Hold All Container items if set with advanced enchant
                                                    ItemStack container = stackInRecipe.getItem().getContainerItem(stackInRecipe);
                                                    if (!hasAdvancedInventoryTargeting(coin)) {
                                                        if (!world.isRemote) {
                                                            world.addEntity(new ItemEntity(world, getPosOfBlockBelow(world, pedestalPos, -1).getX() + 0.5, getPosOfBlockBelow(world, pedestalPos, -1).getY() + 0.5, getPosOfBlockBelow(world, pedestalPos, -1).getZ() + 0.5, container));
                                                        }

                                                        ItemStack queueStack = stackCurrent.get(s);
                                                        queueStack.shrink(intBatchCraftingSize);
                                                        stackCurrent.set(s,queueStack);
                                                        returnedStack = handler.extractItem(s, intBatchCraftingSize, false);
                                                        if(!returnedStack.isEmpty())extractedItemsList.add(returnedStack);
                                                    }
                                                    else
                                                    {
                                                        if(!(container.getItem().equals(stackInRecipe.getItem())))
                                                        {
                                                            if (!world.isRemote) {
                                                                world.addEntity(new ItemEntity(world, getPosOfBlockBelow(world, pedestalPos, -1).getX() + 0.5, getPosOfBlockBelow(world, pedestalPos, -1).getY() + 0.5, getPosOfBlockBelow(world, pedestalPos, -1).getZ() + 0.5, container));
                                                            }

                                                            ItemStack queueStack = stackCurrent.get(s);
                                                            queueStack.shrink(intBatchCraftingSize);
                                                            stackCurrent.set(s,queueStack);
                                                            returnedStack = handler.extractItem(s, intBatchCraftingSize, false);
                                                            if(!returnedStack.isEmpty())extractedItemsList.add(returnedStack);
                                                        }

                                                        ItemStack queueStack = stackCurrent.get(s);
                                                        if(queueStack.isDamageable())
                                                        {
                                                            if(queueStack.getDamage() > queueStack.getMaxDamage())
                                                            {
                                                                stackCurrent.set(s,ItemStack.EMPTY);
                                                                returnedStack = handler.extractItem(s, intBatchCraftingSize, false);
                                                                if(!returnedStack.isEmpty())extractedItemsList.add(returnedStack);
                                                            }
                                                            else {
                                                                int damage = queueStack.getDamage();
                                                                queueStack.setDamage(damage+1);
                                                                stackCurrent.set(s,queueStack);
                                                                handler.getStackInSlot(s).setDamage(queueStack.getDamage());
                                                            }
                                                        }
                                                    }
                                                }
                                                else
                                                {
                                                    ItemStack queueStack = stackCurrent.get(s);
                                                    queueStack.shrink(intBatchCraftingSize);
                                                    stackCurrent.set(s,queueStack);
                                                    returnedStack = handler.extractItem(s, intBatchCraftingSize, false);
                                                    if(!returnedStack.isEmpty())extractedItemsList.add(returnedStack);
                                                }
                                            }

                                            if(extractedItemsList.size()>0)
                                            {
                                                getRecipe.setCount(intBatchCraftedAmount);
                                                if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                                addToPedestal(world, pedestalPos, getRecipe);
                                                onPedestalNeighborChanged(pedestal);
                                                writeStoredIntToNBT(coin,intGetNextIteration+1);
                                            }
                                        }
                                        else
                                        {
                                            writeStoredIntToNBT(coin,intGetNextIteration+1);
                                            writeStoredIntTwoToNBT(coin,readStoredIntTwoFromNBT(coin)+1);
                                        }
                                    }
                                    else
                                    {
                                        writeStoredIntToNBT(coin,intGetNextIteration+1);
                                        writeStoredIntTwoToNBT(coin,readStoredIntTwoFromNBT(coin)+1);
                                    }
                                }
                                else
                                {
                                    writeStoredIntToNBT(coin,intGetNextIteration+1);
                                    writeStoredIntTwoToNBT(coin,readStoredIntTwoFromNBT(coin)+1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPedestalNeighborChanged(PedestalTileEntity pedestal) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<ItemStack> stackIn = buildInventoryQueue(pedestal);
        List<ItemStack> stackCurrent = readInventoryQueueFromNBT(coin);
        if(!doInventoryQueuesMatch(stackIn,stackCurrent))
        {
            writeInventoryQueueToNBT(coin,stackIn);
            writeStoredIntTwoToNBT(coin,0);
            buildAndWriteCraftingQueue(pedestal,stackIn);
        }
        else {
            writeInventoryQueueToNBT(coin,stackIn);
            writeStoredIntTwoToNBT(coin,0);
        }
    }

    @Override
    public void notifyTransferUpdate(PedestalTileEntity receiverTile)
    {
        ItemStack coin = receiverTile.getCoinOnPedestal();
        if(readStoredIntTwoFromNBT(coin)>0)
        {
            writeStoredIntTwoToNBT(coin,0);
            List<ItemStack> invQue = readInventoryQueueFromNBT(coin);
            buildAndWriteCraftingQueue(receiverTile,invQue);
        }
    }

    //Write recipes in order as they appear in the inventory, and since we check for changed, we should be able to get which recipe is which
    @Override
    public void buildAndWriteCraftingQueue(PedestalTileEntity pedestal, List<ItemStack> inventoryQueue)
    {
        World world = pedestal.getWorld();
        ItemStack coin = pedestal.getCoinOnPedestal();
        int gridSize = getGridSize(coin);
        int intGridCount = gridSize*gridSize;
        List<ItemStack> invQueue = inventoryQueue;
        int recipeCount = Math.floorDiv(invQueue.size(),intGridCount);

        List<ItemStack> recipeQueue = new ArrayList<>();

        CraftingInventory craft = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean canInteractWith(PlayerEntity playerIn) {
                return false;
            }
        }, gridSize, gridSize);

        for(int r=1;r<= recipeCount; r++)
        {
            for(int s=0;s<intGridCount; s++)
            {
                int getActualIndex = ((r*intGridCount)-intGridCount)+s;
                ItemStack getStack = invQueue.get(getActualIndex);
                //If the item Stack has enough items to craft with
                //stack.getCount()>=2 ||  stack.maxStackSize()==1 ||
                if(getStack.isEmpty() || getStack.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER))
                {
                    craft.setInventorySlotContents(s, ItemStack.EMPTY);
                    continue;
                }
                else if(getStack.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER_BUCKET))
                {
                    //Make the recipe invalid if no fluid exists???
                    ItemStack bucketStack = new ItemStack(Items.BARRIER);
                    if(removeFluid(pedestal,coin,FluidAttributes.BUCKET_VOLUME,true))
                    {
                        FluidStack fluidToBucket = getFluidStored(coin);
                        Item bucket = fluidToBucket.getFluid().getFilledBucket();
                        bucketStack = new ItemStack(bucket);
                    }
                    else
                    {
                        writeStoredIntTwoToNBT(coin,1);
                    }

                    craft.setInventorySlotContents(s, bucketStack);
                    continue;
                }
                else if(getStack.getMaxStackSize()==1 || getStack.isDamageable())
                {
                    craft.setInventorySlotContents(s,getStack);
                    continue;
                }
                else if(getStack.getCount() > 0)
                {
                    craft.setInventorySlotContents(s,getStack);
                    continue;
                }
            }
            //Checks to make sure we have enough slots set for out recipe
            if(craft.getSizeInventory() >= intGridCount)
            {
                IRecipe recipe = findRecipe(craft,world);
                if(recipe  != null &&  recipe.matches(craft, world)) {
                    //Set ItemStack with recipe result
                    ItemStack stackRecipeResult = recipe.getCraftingResult(craft);
                    recipeQueue.add(stackRecipeResult);
                }
                else
                {
                    recipeQueue.add(ItemStack.EMPTY);
                }
            }
        }
        writeCraftingQueueToNBT(coin, recipeQueue);
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

        List<ItemStack> list = readCraftingQueueFromNBT(stack);
        if(list.size()>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getTranslationKey() + ".chat_recipes");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,Util.DUMMY_UUID);

            for(int i=0;i<list.size();i++) {

                TranslationTextComponent enchants = new TranslationTextComponent((list.get(i).isEmpty())?(" - "):(" - " + list.get(i).getDisplayName().getString()));
                enchants.mergeStyle(TextFormatting.GRAY);
                player.sendMessage(enchants,Util.DUMMY_UUID);
            }
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

        if(getAdvancedModifier(stack)<=0 && (intOperationalSpeedOver(stack) >5 || getCapacityModifierOver(stack) >5 || getAreaModifierUnRestricted(stack) >5 || getRangeModifier(stack) >5))
        {
            TranslationTextComponent warning = new TranslationTextComponent(Reference.MODID + ".advanced_warning");
            warning.mergeStyle(TextFormatting.RED);
            tooltip.add(warning);
        }

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
