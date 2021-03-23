package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.item.ItemCraftingPlaceholder;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
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
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class ItemUpgradeCrafter extends ItemUpgradeBaseMachine
{
    public ItemUpgradeCrafter(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    public int getGridSize(ItemStack itemStack)
    {
        int gridSize = 0;
        if(itemStack.getItem().equals(ItemUpgradeCrafter.CRAFTER_ONE)){gridSize = 1;}
        else if(itemStack.getItem().equals(ItemUpgradeCrafter.CRAFTER_TWO)){gridSize = 2;}
        else if(itemStack.getItem().equals(ItemUpgradeCrafter.CRAFTER_THREE)){gridSize = 3;}
        else{gridSize = 1;}

        return gridSize;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            BlockPos pedestalPos = pedestal.getPos();
            int storedTwo = readStoredIntTwoFromNBT(coinInPedestal);
            int craftingCount = readCraftingQueueFromNBT(coinInPedestal).size();

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
                                int intGetNextIteration = getStoredInt(coin);//Default value is 0
                                if (intGetNextIteration == 0)
                                {
                                    intGetNextIteration = 1;//Start at 1 since thats the start for the number of recipes we have}
                                }
                                //Makes sure Out Estimated and Actual Inventories Match AND we have more slots then the recipe requires
                                if(craftingCurrent.size() > 0)
                                {
                                    // Get Next iteration to craft

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
                                            else if(getIngredientStack.isEmpty() || getIngredientStack.getItem() instanceof ItemCraftingPlaceholder)
                                            {
                                                continue;//skip to the next one
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

                                        //System.out.println("BatchCraftingSize: "+intBatchCraftingSize);

                                        //Means there is something to craft, realistically since getRecipe.isEmpty is checked already, this should never be < 0
                                        if(intBatchCraftingSize > 0)
                                        {

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
                                                    continue;

                                                if (stackInRecipe.getItem().hasContainerItem(stackInRecipe)) {
                                                    //Will Now Hold All Container items if set with advanced enchant
                                                    ItemStack container = stackInRecipe.getItem().getContainerItem(stackInRecipe);
                                                    if (!hasAdvancedInventoryTargeting(coin)) {

                                                        if (!world.isRemote) {
                                                            world.addEntity(new ItemEntity(world, getPosOfBlockBelow(world, pedestalPos, -1).getX() + 0.5, getPosOfBlockBelow(world, pedestalPos, -1).getY() + 0.5, getPosOfBlockBelow(world, pedestalPos, -1).getZ() + 0.5, container));
                                                        }

                                                        ItemStack queueStack = stackCurrent.get(s);
                                                        queueStack.shrink(intBatchCraftingSize);
                                                        stackCurrent.set(s,queueStack);
                                                        handler.extractItem(s, intBatchCraftingSize, false);
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
                                                            handler.extractItem(s, intBatchCraftingSize, false);
                                                        }
                                                        ItemStack queueStack = stackCurrent.get(s);
                                                        if(queueStack.isDamageable())
                                                        {
                                                            if(queueStack.getDamage() > queueStack.getMaxDamage())
                                                            {
                                                                stackCurrent.set(s,ItemStack.EMPTY);
                                                                handler.extractItem(s, intBatchCraftingSize, false);
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
                                                    handler.extractItem(s, intBatchCraftingSize, false);
                                                }
                                            }

                                            getRecipe.setCount(intBatchCraftedAmount);
                                            world.playSound((PlayerEntity) null, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                            addToPedestal(world, pedestalPos, getRecipe);
                                            onPedestalNeighborChanged(pedestal);
                                            writeStoredIntToNBT(coin,intGetNextIteration+1);
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
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {

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

    public String getOperationSpeedString(ItemStack stack)
    {
        TranslationTextComponent normal = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_0");
        TranslationTextComponent twox = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_1");
        TranslationTextComponent fourx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_2");
        TranslationTextComponent sixx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_3");
        TranslationTextComponent tenx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_4");
        TranslationTextComponent twentyx = new TranslationTextComponent(Reference.MODID + ".upgrade_tooltips" + ".speed_5");
        String str = normal.getString();
        switch (intOperationalSpeedModifier(stack))
        {
            case 0:
                str = normal.getString();//normal speed
                break;
            case 1:
                str = twox.getString();//2x faster
                break;
            case 2:
                str = fourx.getString();//4x faster
                break;
            case 3:
                str = sixx.getString();//6x faster
                break;
            case 4:
                str = tenx.getString();//10x faster
                break;
            case 5:
                str = twentyx.getString();//20x faster
                break;
            default: str = normal.getString();;
        }

        return  str;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {

    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name, Util.DUMMY_UUID);

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
        rate.appendString(""+getItemTransferRate(stack)+"");
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);
        player.sendMessage(speed,Util.DUMMY_UUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        TranslationTextComponent t = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        t.mergeStyle(TextFormatting.GOLD);
        tooltip.add(t);

        int s2 = getItemTransferRate(stack);
        String trr = getOperationSpeedString(stack);
        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString(""+s2+"");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(trr);

        rate.mergeStyle(TextFormatting.GRAY);

        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(rate);
        tooltip.add(speed);
    }

    public static final Item CRAFTER_ONE = new ItemUpgradeCrafter(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/crafter1"));
    public static final Item CRAFTER_TWO = new ItemUpgradeCrafter(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/crafter2"));
    public static final Item CRAFTER_THREE = new ItemUpgradeCrafter(new Item.Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/crafter3"));


    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(CRAFTER_ONE);
        event.getRegistry().register(CRAFTER_TWO);
        event.getRegistry().register(CRAFTER_THREE);
    }

}
