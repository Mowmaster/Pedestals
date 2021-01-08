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
    public Boolean canAcceptAdvanced() {
        return true;
    }

    public int getGridSize(ItemStack itemStack)
    {
        int gridSize = 0;
        if(itemStack.getItem().equals(ItemUpgradeCompactingCrafter.COMPACTOR_TWO)){gridSize = 2;}
        else if(itemStack.getItem().equals(ItemUpgradeCompactingCrafter.COMPACTOR_THREE)){gridSize = 3;}
        else{gridSize = 1;}

        return gridSize;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();

        if(!world.isRemote)
        {
            int speed = getOperationSpeed(coinInPedestal);

            if(!world.isBlockPowered(pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
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
        int gridSlots = gridSize*gridSize;
        ItemStack itemFromInv = ItemStack.EMPTY;
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        //Since we'll be checking each slot for a compacting recipe
        int intGridCount = 1;
        int intItemsUsedCount = gridSize*gridSize;
        int intBatchCraftingSize = getItemTransferRate(coinInPedestal);


        //Dont bother unless pedestal is empty
        //Yes i'm being lazy here...

        if(itemInPedestal.isEmpty())
        {
            LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);

            if(!isInventoryEmpty(cap))
            {
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

                    //Should Allow Using a pedestal when it has the advanced enchant
                    if (((hasAdvancedInventoryTargeting(coinInPedestal) && invToPullFrom instanceof PedestalTileEntity)||!(invToPullFrom instanceof PedestalTileEntity))?(false):(true)) {
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
                                if(intGetNextIteration >= intInventorySlotCount)
                                {
                                    intGetNextIteration = 0;
                                }

                                int slotToCheck = intGetNextIteration;
                                ItemStack stackItemInSlot = handler.getStackInSlot(slotToCheck);
                                if(!stackItemInSlot.isEmpty())
                                {
                                    //System.out.println("SLOT: "+slotToCheck);
                                    //System.out.println("Item In Slot: "+stackItemInSlot);
                                    //System.out.println("Items To Use: "+intItemsUsedCount);
                                    if(stackItemInSlot.getCount() > intItemsUsedCount)
                                    {
                                        for(int i=0; i< gridSlots;i++)
                                        {
                                            //next check to make sure we have more than enough to craft the recipe
                                            if(stackItemInSlot.getCount() > (intBatchCraftingSize*intItemsUsedCount))
                                            {
                                                //System.out.println("SET ITEM FOR SLOT");
                                                craft.setInventorySlotContents(i,stackItemInSlot);
                                            }
                                            else
                                            {
                                                intBatchCraftingSize = ((stackItemInSlot.getCount()/intItemsUsedCount)-1);
                                                if(intBatchCraftingSize>0)craft.setInventorySlotContents(i,stackItemInSlot);
                                            }
                                            craftAvailable.setInventorySlotContents(i,stackItemInSlot);
                                        }
                                    }

                                    //Checks to make sure we have enough slots set for out recipe
                                    if(craft.getSizeInventory() >= intGridCount)
                                    {

                                        IRecipe recipe = findRecipe(craft,world);
                                        //This is the set recipe, which might differ from the recipe given current available inputs
                                        IRecipe setRecipe = findRecipe(craftAvailable,world);

                                        //if(recipe != null)System.out.println("RECIPE: "+recipe.getRecipeOutput());

                                        if(recipe  != null &&  recipe.matches(craft, world) && recipe.matches(craftAvailable,world)) {
                                            //Set ItemStack with recipe result
                                            ItemStack stackRecipeResult = recipe.getCraftingResult(craft);
                                            int intRecipeResultCount = stackRecipeResult.getCount();
                                            int intBatchCraftedAmount = stackRecipeResult.getCount() * intBatchCraftingSize;

                                            //Check if pedestal can hold the crafting result, if not then set the batch to be small enough that it can fit
                                            if(intBatchCraftedAmount > 64)
                                            {
                                                intBatchCraftingSize = 64/intRecipeResultCount;
                                            }

                                            //Remove items from slotquar
                                            int intGetActualSlot = intGetNextIteration;
                                            handler.extractItem(intGetActualSlot,(intBatchCraftingSize*intItemsUsedCount),false);

                                            stackRecipeResult.setCount(intBatchCraftedAmount);
                                            world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                            addToPedestal(world,posOfPedestal,stackRecipeResult);
                                        }
                                    }
                                }

                                setIntValueToPedestal(world,posOfPedestal,(intGetNextIteration+1));
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
