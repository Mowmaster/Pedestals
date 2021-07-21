package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.item.ItemCraftingPlaceholder;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
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
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

/*
Basically it checks a single slot in an inventory for a compactable recipe,
if none exist, or it cant leave 1 item in the slot, it will skip and move to the next slot
 */
public class ItemUpgradeCompactingCrafter extends ItemUpgradeBaseMachine
{
    public ItemUpgradeCompactingCrafter(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canAcceptAdvanced() {
        return true;
    }

    public int getGridSize(ItemStack itemStack)
    {
        int gridSize = 0;
        if(itemStack.getItem().equals(ItemUpgradeCompactingCrafter.COMPACTOR_TWO)){gridSize = 2;}
        else if(itemStack.getItem().equals(ItemUpgradeCompactingCrafter.COMPACTOR_THREE)){gridSize = 3;}
        else{gridSize = 2;}

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

                            int intGetNextIteration = getStoredInt(coin);//Default value is 0
                            if(intGetNextIteration >= intInventorySlotCount)
                            {
                                intGetNextIteration = 0;
                            }
                            //System.out.println("CurrentCraftingSize: "+craftingCurrent.size());
                            if(craftingCurrent.size() > 0)
                            {
                                // Get Next iteration to craft

                                int slotToCheck = intGetNextIteration;
                                ItemStack stackItemInSlot = stackCurrent.get(slotToCheck);
                                if(!stackItemInSlot.isEmpty())
                                {
                                    //copy so we dont set the stack size any higher
                                    ItemStack getRecipe = craftingCurrent.get(intGetNextIteration).copy();

                                    //System.out.println("GetRecipe: "+getRecipe);

                                    if(!getRecipe.isEmpty())
                                    {
                                        //Calc max stack size craftable
                                        ItemStack getIngredientStack = stackCurrent.get(intGetNextIteration);
                                        int stackSize = getIngredientStack.getCount();

                                        if((stackSize-1) < intGridCount*intBatchCraftingSize)
                                        {
                                            intBatchCraftingSize = Math.floorDiv(stackSize,intGridCount);
                                            //Because im paranoid???
                                            if(intGridCount*intBatchCraftingSize>=stackSize)intBatchCraftingSize--;
                                        }
                                        else if(getIngredientStack.isEmpty())
                                        {
                                            intBatchCraftingSize=0;
                                        }

                                        //Means there is something to craft, realistically since getRecipe.isEmpty is checked already, this should never be < 0
                                        if(intBatchCraftingSize > 0)
                                        {
                                            int intRecipeResultCount = getRecipe.getCount();
                                            int intBatchCraftedAmount = intRecipeResultCount * intBatchCraftingSize;

                                            //Check if pedestal can hold the crafting result, if not then set the batch to be small enough that it can fit
                                            if (intBatchCraftedAmount > 64) {
                                                intBatchCraftingSize = Math.floorDiv(64, intRecipeResultCount);
                                            }

                                            int itemsToRemove = intBatchCraftingSize*intGridCount;
                                            int itemsToInsertToPedestal = intBatchCraftingSize*intRecipeResultCount;

                                            ItemStack queueStack = stackCurrent.get(intGetNextIteration);
                                            queueStack.shrink(itemsToRemove);
                                            stackCurrent.set(intGetNextIteration,queueStack);
                                            ItemStack processExtract = handler.extractItem(intGetNextIteration, itemsToRemove, false);
                                            if(!processExtract.isEmpty())
                                            {
                                                if (queueStack.getItem().hasContainerItem(queueStack)) {
                                                    ItemStack container = queueStack.getItem().getContainerItem(queueStack);
                                                    container.setCount(intBatchCraftingSize*intGridCount);
                                                    if (!world.isRemote) {
                                                        world.addEntity(new ItemEntity(world, getPosOfBlockBelow(world, pedestalPos, -1).getX() + 0.5, getPosOfBlockBelow(world, pedestalPos, -1).getY() + 0.5, getPosOfBlockBelow(world, pedestalPos, -1).getZ() + 0.5, container));
                                                    }
                                                }

                                                getRecipe.setCount(itemsToInsertToPedestal);
                                                if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                                addToPedestalOverride(world, pedestalPos, getRecipe);
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

    //Write recipes in order as they appear in the inventory, and since we check for changed, we should be able to get which recipe is which
    @Override
    public void buildAndWriteCraftingQueue(PedestalTileEntity pedestal, List<ItemStack> inventoryQueue)
    {
        World world = pedestal.getWorld();
        ItemStack coin = pedestal.getCoinOnPedestal();
        int gridSize = getGridSize(coin);
        int intGridCount = gridSize*gridSize;
        List<ItemStack> invQueue = inventoryQueue;

        List<ItemStack> recipeQueue = new ArrayList<>();

        CraftingInventory craft = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean canInteractWith(PlayerEntity playerIn) {
                return false;
            }
        }, gridSize, gridSize);

        //System.out.println("THIS IS THE GRID COUNT FOR THE RECIPE!!!"+ intGridCount);

        //For Each stack in the invBuffer
        for(int s=0; s<invQueue.size(); s++)
        {
            ItemStack getStack = invQueue.get(s).copy();
            getStack.setCount(1);
            //If the item Stack has enough items to craft with
            //stack.getCount()>=2 ||  stack.maxStackSize()==1 ||
            if(!getStack.isEmpty())
            {
                for(int r=0;r < intGridCount;r++)
                {
                    craft.setInventorySlotContents(r,getStack);
                }
            }
            else
            {
                recipeQueue.add(ItemStack.EMPTY);
                continue;
            }
            //Checks to make sure we have enough slots set for out recipe
            if(craft.getSizeInventory() >= intGridCount)
            {
                IRecipe recipe = findRecipe(craft,world);
                if(recipe  != null &&  recipe.matches(craft, world)) {
                    //Set ItemStack with recipe result
                    ItemStack stackRecipeResult = recipe.getCraftingResult(craft);
                    recipeQueue.add(stackRecipeResult);
                    continue;
                }
                else
                {
                    recipeQueue.add(ItemStack.EMPTY);
                    continue;
                }
            }
        }
        writeCraftingQueueToNBT(coin, recipeQueue);
    }

    @Override
    public void onPedestalNeighborChanged(PedestalTileEntity pedestal) {
        ItemStack coin = pedestal.getCoinOnPedestal();
        List<ItemStack> stackIn = buildInventoryQueue(pedestal);
        List<ItemStack> stackCurrent = readInventoryQueueFromNBT(coin);
        if(!doInventoryQueuesMatch(stackIn,stackCurrent))
        {
            writeInventoryQueueToNBT(coin,stackIn);
            //Reset value on 'neighbor' update
            writeStoredIntTwoToNBT(coin,0);
            buildAndWriteCraftingQueue(pedestal,stackIn);

        }
        else {
            writeInventoryQueueToNBT(coin,stackIn);
            writeStoredIntTwoToNBT(coin,0);
        }
    }

    @Override
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {

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
                if(list.get(i).isEmpty())continue;
                TranslationTextComponent enchants = new TranslationTextComponent(i +": " + list.get(i).getDisplayName().getString());
                enchants.mergeStyle(TextFormatting.GRAY);
                player.sendMessage(enchants,Util.DUMMY_UUID);
            }
        }

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString(""+getItemTransferRate(stack)+"");
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        if(map.size() > 0 && getNumNonPedestalEnchants(map)>0)
        {
            TranslationTextComponent enchant = new TranslationTextComponent(getTranslationKey() + ".chat_enchants");
            enchant.mergeStyle(TextFormatting.LIGHT_PURPLE);
            player.sendMessage(enchant,Util.DUMMY_UUID);

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment enchantment = entry.getKey();
                Integer integer = entry.getValue();
                if(!(enchantment instanceof EnchantmentCapacity) && !(enchantment instanceof EnchantmentRange) && !(enchantment instanceof EnchantmentOperationSpeed) && !(enchantment instanceof EnchantmentArea))
                {
                    TranslationTextComponent enchants = new TranslationTextComponent(" - " + enchantment.getDisplayName(integer).getString());
                    enchants.mergeStyle(TextFormatting.GRAY);
                    player.sendMessage(enchants,Util.DUMMY_UUID);
                }
            }
        }

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

        ResourceLocation disabled = new ResourceLocation("pedestals", "enchant_limits/advanced_blacklist");
        ITag<Item> BLACKLISTED = ItemTags.getCollection().get(disabled);

        if(BLACKLISTED !=null)
        {
            //if item isnt in blacklist tag
            if(!BLACKLISTED.contains(stack.getItem()))
            {
                //if any of the enchants are over level 5
                if(intOperationalSpeedOver(stack) >5 || getCapacityModifierOver(stack) >5 || getAreaModifierUnRestricted(stack) >5 || getRangeModifier(stack) >5)
                {
                    //if it doesnt have advanced
                    if(getAdvancedModifier(stack)<=0)
                    {
                        TranslationTextComponent warning = new TranslationTextComponent(Reference.MODID + ".advanced_warning");
                        warning.mergeStyle(TextFormatting.RED);
                        tooltip.add(warning);
                    }
                }
            }
            //if it is
            else
            {
                //Advanced disabled warning only shows after upgrade has advanced on it, isnt great, but alerts the user it wont work unfortunately
                if(getAdvancedModifier(stack)>0)
                {
                    TranslationTextComponent disabled_warning = new TranslationTextComponent(Reference.MODID + ".advanced_disabled_warning");
                    disabled_warning.mergeStyle(TextFormatting.DARK_RED);
                    tooltip.add(disabled_warning);
                }
            }
        }

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

    public static final Item COMPACTOR_TWO = new ItemUpgradeCompactingCrafter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/compactor2"));
    public static final Item COMPACTOR_THREE = new ItemUpgradeCompactingCrafter(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/compactor3"));


    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(COMPACTOR_TWO);
        event.getRegistry().register(COMPACTOR_THREE);
    }

}
