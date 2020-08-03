package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.item.ItemCraftingPlaceholder;
import com.mowmaster.pedestals.tiles.TilePedestal;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeCrafter extends ItemUpgradeBaseMachine
{
    public ItemUpgradeCrafter(Item.Properties builder) {super(builder.group(PEDESTALS_TAB));}

    public int getGridSize(ItemStack itemStack)
    {
        int gridSize = 0;
        if(itemStack.getItem().equals(ItemUpgradeCrafter.CRAFTER_ONE)){gridSize = 1;}
        else if(itemStack.getItem().equals(ItemUpgradeCrafter.CRAFTER_TWO)){gridSize = 2;}
        else if(itemStack.getItem().equals(ItemUpgradeCrafter.CRAFTER_THREE)){gridSize = 3;}
        else{gridSize = 1;}

        return gridSize;
    }

    public void updateAction(int tick, World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {
        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);

            if(!world.isBlockPowered(pedestalPos))
            {
                if (tick%speed == 0) {
                    upgradeAction(world,itemInPedestal,coinInPedestal,pedestalPos);
                }
            }
        }

    }

    public static IRecipe<CraftingInventory> findRecipe(CraftingInventory inv, World world) {
        return world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inv, world).orElse(null);
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos posOfPedestal)
    {
        int gridSize = getGridSize(coinInPedestal);
        int intBatchCraftingSize = getItemTransferRate(coinInPedestal);
        ItemStack itemFromInv = ItemStack.EMPTY;
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        int intGridCount = gridSize*gridSize;

        //Dont bother unless pedestal is empty
        //Yes i'm being lazy here...

        if(itemInPedestal.isEmpty())
        {
            //if(world.getTileEntity(posInventory) != null)
            //{
            LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
                //Setup new Container for our Crafting Grid Size
                CraftingInventory craft = new CraftingInventory(new Container(null, -1) {
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
                    if (invToPullFrom instanceof TilePedestal) {
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
                                for(int i = intSlotToStartFrom; i < intSlotToEndBefore; i++) {
                                    ItemStack stackItemInSlot = handler.getStackInSlot(i);

                                    //If the item Stack has enough items to craft with
                                    //stack.getCount()>=2 ||  stack.getMaxStackSize()==1 ||
                                    if(stackItemInSlot.isEmpty() ||stackItemInSlot.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER))
                                    {
                                        craft.setInventorySlotContents(intCraftingSlot, ItemStack.EMPTY);
                                        intCraftingSlot++;
                                    }
                                    else if(stackItemInSlot.getMaxStackSize()==1 || stackItemInSlot.isDamageable())
                                    {
                                        //Since recipe has a container item we have to limit it to 1 craft
                                        intBatchCraftingSize = 1;
                                        craft.setInventorySlotContents(intCraftingSlot,stackItemInSlot);
                                        intCraftingSlot++;
                                    }
                                    //the +1 makes sure to leave 1 item in the inv as a placeholder
                                    else if(stackItemInSlot.getCount() > (intBatchCraftingSize))
                                    {
                                        craft.setInventorySlotContents(intCraftingSlot,stackItemInSlot);
                                        intCraftingSlot++;
                                    }
                                }


                                //Checks to make sure we have enough slots set for out recipe
                                if(craft.getSizeInventory() >= intGridCount)
                                {

                                    IRecipe recipe = findRecipe(craft,world);
                                    //for(IRecipe recipes : ForgeRegistries.RECIPE)
                                    //{
                                        //If recipe is valid and we can craft recipe and stick it in pedestal

                                        if(recipe  != null &&  recipe.matches(craft, world)) {
                                            //Set ItemStack with recipe result
                                            ItemStack stackRecipeResult = recipe.getCraftingResult(craft);
                                            int intRecipeResultCount = stackRecipeResult.getCount();
                                            int intBatchCraftedAmount = stackRecipeResult.getCount() * intBatchCraftingSize;

                                            //Check if pedestal can hold the crafting result, if not then set the batch to be small enough that it can fit
                                            if(intBatchCraftedAmount > 64)
                                            {
                                                intBatchCraftingSize = 64/intRecipeResultCount;
                                            }

                                            //Loop through inventory again to remove crafted materials used
                                            for(int i = 0; i < craft.getSizeInventory(); i++) {


                                                ItemStack stackInRecipe = craft.getStackInSlot(i);
                                                int intGetActualSlot = ((intGetNextIteration*intGridCount)-intGridCount)+i;
                                                ItemStack stackItemInSlot = handler.getStackInSlot(intGetActualSlot);

                                                if(stackInRecipe.isEmpty()  || stackInRecipe.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER))
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

                                            stackRecipeResult.setCount(intBatchCraftedAmount);
                                            //world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                            world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                            addToPedestal(world,posOfPedestal,stackRecipeResult);
                                        }
                                    //}
                                }
                                setIntValueToPedestal(world,posOfPedestal,(intGetNextIteration+1));
                            }
                        }
                    }
                }
            //}
        }
    }

    @Override
    public void actionOnCollideWithBlock(World world, TilePedestal tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(TilePedestal pedestal, BlockState stateIn, World world, BlockPos pos, Random rand)
    {

    }

    @Override
    public void chatDetails(PlayerEntity player, TilePedestal pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.func_240699_a_(TextFormatting.GOLD);
        player.sendMessage(name,player.getUniqueID());

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.func_240702_b_(""+getItemTransferRate(stack)+"");
        rate.func_240699_a_(TextFormatting.GRAY);
        player.sendMessage(rate,player.getUniqueID());

        //Display Speed Last Like on Tooltips
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".chat_speed");
        speed.func_240702_b_(getSmeltingSpeedString(stack));
        speed.func_240699_a_(TextFormatting.RED);
        player.sendMessage(speed,player.getUniqueID());
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
