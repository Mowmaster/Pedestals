package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.item.ItemCraftingPlaceholder;
import com.mowmaster.pedestals.item.pedestalFilters.ItemFilterBase;
import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
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
import java.util.*;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;


/*
THIS CRAFTER WILL USE A FILTER AS THE SETUP RECIPES
If a filter exists itll check the filter for the recipes, then never need to check for recipes ever again.
Will need a new method that can get all ingredients for a recipe, and calculate what is needed, if they exist to be extracted,
then craft the recipe, otherwise move on to the next recipe.

This means you just need to put the inputs in the chest, and inv size wouldnt matter, just needs to be able to extract the items.

Smart Crafter???
 */
public class ItemUpgradeCraftermk2 extends ItemUpgradeBaseMachine
{
    public ItemUpgradeCraftermk2(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    public int getGridSize(ItemStack itemStack)
    {
        int gridSize = 0;
        if(itemStack.getItem().equals(ItemUpgradeCraftermk2.CRAFTER_ONE)){gridSize = 1;}
        else if(itemStack.getItem().equals(ItemUpgradeCraftermk2.CRAFTER_TWO)){gridSize = 2;}
        else if(itemStack.getItem().equals(ItemUpgradeCraftermk2.CRAFTER_THREE)){gridSize = 3;}
        else{gridSize = 1;}

        return gridSize;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            BlockPos pedestalPos = pedestal.getPos();
            int speed = getOperationSpeed(coinInPedestal);

            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                //Dont run if theres nothing queued
                if (world.getGameTime()%speed == 0) {
                    upgradeAction(pedestal);
                }
            }
        }
    }

    public static IRecipe<CraftingInventory> findRecipe(CraftingInventory inv, World world) {
        return world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inv, world).orElse(null);
    }

    public void removeAllOfThisItemFromInventory(LazyOptional<IItemHandler> cap,ItemStack stackToRemove,int countToRemove)
    {

        int counter = countToRemove;
        if(cap.isPresent()) {
            IItemHandler handler = cap.orElse(null);
            while(counter>0)
            {
                int slotToFind = getSlotWithMatchingStack(cap,stackToRemove);
                int inSlotCount = handler.getStackInSlot(slotToFind).getCount();
                if(inSlotCount>=counter)
                {
                    handler.extractItem(slotToFind,counter,false);
                    break;
                }
                else
                {
                    handler.extractItem(slotToFind,inSlotCount,false);
                    counter -= inSlotCount;
                    continue;
                }
            }
        }

        /*if(stackToRemove.hasContainerItem())
        {
            if(stackToRemove.getItem().equals(stackToRemove.getContainerItem().getItem()))
            {
                //Process durability if needed
                if(stackToRemove.isDamageable())
                {
                    if(stackToRemove.getDamage() > stackToRemove.getMaxDamage())
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
            else {
                ItemStack containerReturned = stackToRemove.getContainerItem();
                containerReturned.setCount(countToRemove);
                return containerReturned;
            }
        }

        return ItemStack.EMPTY;*/
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

        if(getFilterChangeStatus(pedestal.getCoinOnPedestal()))
        {
            buildOutputIngredientMapFromPattern(pedestal);
            setFilterChangeUpdated(pedestal.getCoinOnPedestal());
        }

        if(pedestal.hasFilter())
        {
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
                                //Slot || count to remove
                                Map<Integer, Integer> itemExtractionMap = Maps.<Integer, Integer>newLinkedHashMap();
                                Map<ItemStack, List<ItemStack>> craftingMap = readOutputIngredientMapFromNBT(coin);
                                if(!(craftingMap.size()>0))
                                {
                                    buildOutputIngredientMapFromPattern(pedestal);
                                    setFilterChangeUpdated(pedestal.getCoinOnPedestal());
                                    craftingMap = readOutputIngredientMapFromNBT(coin);
                                }

                                int counter = 0;
                                for(Map.Entry<ItemStack, List<ItemStack>> entry : craftingMap.entrySet())
                                {
                                    if(counter>=craftingMap.size())break;
                                    int intGetNextIteration = getStoredInt(coin);
                                    if(intGetNextIteration>=craftingMap.size())intGetNextIteration = 0;
                                    if(intGetNextIteration==counter)
                                    {
                                        ItemStack result = entry.getKey();
                                        if(result != null) {
                                            List<ItemStack> ingredientList = entry.getValue();
                                            int hasIngredents = 0;

                                            if(intBatchCraftingSize > 0)
                                            {
                                                int intRecipeResultCount = result.getCount();
                                                int intBatchCraftedAmount = intRecipeResultCount * intBatchCraftingSize;

                                                //Check if pedestal can hold the crafting result, if not then set the batch to be small enough that it can fit
                                                if (intBatchCraftedAmount > 64) {
                                                    intBatchCraftingSize = Math.floorDiv(64, intRecipeResultCount);
                                                }

                                                for(int ing=0;ing<ingredientList.size();ing++)
                                                {
                                                    //int intGetActualSlot = ((intGetNextIteration * intGridCount) - intGridCount) + s;//Love How Good this formula works!
                                                    ItemStack stackIngredent = ingredientList.get(ing);

                                                    //Skip over empty slots in recipes, but still count them as part of the recipe
                                                    if (stackIngredent.isEmpty() || stackIngredent.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER)) {
                                                        hasIngredents++;
                                                        continue;
                                                    }
                                                    //WIP Dont Process Container Items
                                                    else if(stackIngredent.hasContainerItem()) break;
                                                    else
                                                    {
                                                        int sizeNeeded = intBatchCraftingSize*ingredientList.get(ing).getCount();
                                                        if(hasEnoughInInv(cap,stackIngredent,sizeNeeded)>=sizeNeeded)
                                                        {
                                                            hasIngredents++;
                                                            continue;
                                                        }
                                                        else break;
                                                    }
                                                }

                                                if(hasIngredents==ingredientList.size())
                                                {
                                                    //Remove items if all ingredients exist
                                                    for(int extract=0;extract<ingredientList.size();extract++)
                                                    {
                                                        ItemStack stackIngredent = ingredientList.get(extract);
                                                        int countItems = stackIngredent.getCount();
                                                        if(stackIngredent.isEmpty() || stackIngredent.getItem().equals(ItemCraftingPlaceholder.PLACEHOLDER))continue;
                                                        else removeAllOfThisItemFromInventory(cap,stackIngredent,stackIngredent.getCount());
                                                    }

                                                    result.setCount(intBatchCraftedAmount);
                                                    //itemExtractionMap.forEach((integer, integer2) -> handler.extractItem(integer,integer2,false));
                                                    if (!pedestal.hasMuffler()) world.playSound((PlayerEntity) null, pedestalPos.getX(), pedestalPos.getY(), pedestalPos.getZ(), SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                                    addToPedestalOverride(world, pedestalPos, result);
                                                    writeStoredIntToNBT(coin, intGetNextIteration + 1);
                                                    break;
                                                }
                                                else {
                                                    writeStoredIntToNBT(coin, intGetNextIteration + 1);
                                                }
                                            }
                                        }
                                    }
                                    else counter++;
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

        TranslationTextComponent WIP = new TranslationTextComponent(Reference.MODID + ".wip");
        WIP.mergeStyle(TextFormatting.YELLOW);
        tooltip.add(WIP);

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

    public static final Item CRAFTER_ONE = new ItemUpgradeCraftermk2(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/crafter1mk2"));
    public static final Item CRAFTER_TWO = new ItemUpgradeCraftermk2(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/crafter2mk2"));
    public static final Item CRAFTER_THREE = new ItemUpgradeCraftermk2(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/crafter3mk2"));


    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(CRAFTER_ONE);
        event.getRegistry().register(CRAFTER_TWO);
        event.getRegistry().register(CRAFTER_THREE);
    }

}
