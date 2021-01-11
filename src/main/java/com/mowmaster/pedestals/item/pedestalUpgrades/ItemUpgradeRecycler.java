package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.recipes.RecyclerRecipe;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
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
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeRecycler extends ItemUpgradeBaseExp
{

    public ItemUpgradeRecycler(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public Boolean canAcceptAdvanced() {
        return true;
    }

    public void updateAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        ItemStack itemInPedestal = pedestal.getItemInPedestal();
        BlockPos pedestalPos = pedestal.getPos();
        if(!world.isRemote)
        {
            if(!hasMaxXpSet(coinInPedestal)) {setMaxXP(coinInPedestal,getExpCountByLevel(getExpBuffer(coinInPedestal)));}
            upgradeActionSendExp(pedestal);

            int speed = getOperationSpeed(coinInPedestal);
            if(!world.isBlockPowered(pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    //Just does the unenchanting bit
                    if(itemInPedestal.isEmpty())
                    {
                        grindstoneAction(pedestal);
                    }
                }
            }
        }
    }

    public int getItemsExpDisenchantAmount(ItemStack stack)
    {
        int exp = 0;
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer integer = entry.getValue();

            exp += enchantment.getMinEnchantability(integer.intValue());
        }
        return exp*stack.getCount();
    }

    public void grindstoneAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos posOfPedestal = pedestal.getPos();

        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        ItemStack itemFromInv = ItemStack.EMPTY;
        LazyOptional<IItemHandler> cap = findItemHandlerAtPos(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(hasAdvancedInventoryTargeting(coinInPedestal))cap = findItemHandlerAtPosAdvanced(world,posInventory,getPedestalFacing(world, posOfPedestal),true);
        if(!isInventoryEmpty(cap))
        {
            if(cap.isPresent())
            {
                IItemHandler handler = cap.orElse(null);
                TileEntity invToPullFrom = world.getTileEntity(posInventory);
                if (((hasAdvancedInventoryTargeting(coinInPedestal) && invToPullFrom instanceof PedestalTileEntity)||!(invToPullFrom instanceof PedestalTileEntity))?(false):(true)) {
                    itemFromInv = ItemStack.EMPTY;
                }
                else {
                    if(handler != null)
                    {
                        int range = handler.getSlots();
                        ItemStack nextItemToGrind = ItemStack.EMPTY;
                        nextItemToGrind = IntStream.range(0,range)//Int Range
                                .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                                .filter(itemStack -> itemStack.isEnchanted() || itemStack.getItem() instanceof EnchantedBookItem)
                                .findFirst().orElse(ItemStack.EMPTY);

                        if(!nextItemToGrind.isEmpty())
                        {
                            int slotItemToGrind = getSlotWithMatchingStackExact(cap,nextItemToGrind);
                            itemFromInv = handler.getStackInSlot(slotItemToGrind);
                            int maxXp = readMaxXpFromNBT(coinInPedestal);
                            int currentlyStoredExp = getXPStored(coinInPedestal);
                            int xpDisenchant = getItemsExpDisenchantAmount(itemFromInv);
                            if(maxXp - currentlyStoredExp >= xpDisenchant)
                            {
                                //Code Here
                                Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                ItemStack stackToReturn = (itemFromInv.getItem() instanceof EnchantedBookItem)?(new ItemStack(Items.BOOK,1)):(itemFromInv.copy());
                                EnchantmentHelper.setEnchantments(enchantsNone,stackToReturn);
                                if(!stackToReturn.isEmpty())
                                {
                                    int getExpLeftInPedestal = currentlyStoredExp + xpDisenchant;
                                    setXPStored(coinInPedestal,getExpLeftInPedestal);
                                    recyclerAction(pedestal,handler,slotItemToGrind,stackToReturn);
                                }
                            }
                        }
                        else
                        {
                            nextItemToGrind = IntStream.range(0,range)//Int Range
                                    .mapToObj((handler)::getStackInSlot)//Function being applied to each interval
                                    .filter(itemStack -> !itemStack.isEmpty())
                                    .findFirst().orElse(ItemStack.EMPTY);
                            int slotItemToGrind = getSlotWithMatchingStackExact(cap,nextItemToGrind);
                            recyclerAction(pedestal,handler,slotItemToGrind,nextItemToGrind);
                        }
                    }
                }
            }
        }
    }

    @Nullable
    protected RecyclerRecipe getRecipeRecycler(World world, ItemStack stackIn)
    {
        Inventory inv = new Inventory(stackIn);
        return world == null ? null : world.getRecipeManager().getRecipe(RecyclerRecipe.recipeType, inv, world).orElse(null);
    }

    protected Collection<ItemStack> getProcessResultsRecycler(RecyclerRecipe recipe)
    {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResult()));
    }

    public void recyclerAction(PedestalTileEntity pedestal, IItemHandler handler,int slot, ItemStack itemJustExpGround)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos posOfPedestal = pedestal.getPos();
        if(hasAdvancedInventoryTargeting(coinInPedestal))
        {
            Item input = itemJustExpGround.getItem();
            Collection<ItemStack> jsonResults = getProcessResultsRecycler(getRecipeRecycler(pedestal.getWorld(),itemJustExpGround));
            ItemStack resultRecycler = (jsonResults.iterator().next().isEmpty())?(ItemStack.EMPTY):(jsonResults.iterator().next());
            Item getItemResultRecycler = resultRecycler.getItem();

            if((input instanceof TieredItem || !resultRecycler.isEmpty()) && !getItemResultRecycler.equals(Items.BARRIER))
            {
                //Assume its from the recipe, but if not, then set to another itemstack
                Ingredient repairIngredient = Ingredient.fromStacks(resultRecycler);
                int returnedMaxCount = resultRecycler.getCount();
                if(resultRecycler.isEmpty())
                {
                    if(input instanceof TieredItem)repairIngredient = ((TieredItem)input).getTier().getRepairMaterial();

                    if(input instanceof PickaxeItem || itemJustExpGround.getToolTypes().contains(ToolType.PICKAXE))returnedMaxCount=3;
                    else if(input instanceof AxeItem || itemJustExpGround.getToolTypes().contains(ToolType.AXE))returnedMaxCount=3;
                    else if(input instanceof SwordItem)returnedMaxCount=2;
                    else if(input instanceof HoeItem || itemJustExpGround.getToolTypes().contains(ToolType.HOE))returnedMaxCount=2;
                    else if(input instanceof ShovelItem || itemJustExpGround.getToolTypes().contains(ToolType.SHOVEL))returnedMaxCount=1;
                    else returnedMaxCount = 1;
                }

                ItemStack repairIngredientStack = repairIngredient.getMatchingStacks()[0];
                int maxdamage = itemJustExpGround.getMaxDamage();
                int damage = itemJustExpGround.getDamage();
                int durability = maxdamage - damage;
                int devider = maxdamage/returnedMaxCount;
                int countToReturn = returnedMaxCount;
                if(damage == 0)
                {
                    countToReturn = returnedMaxCount;
                }
                else if(durability<devider)
                {
                    repairIngredientStack = new ItemStack(Items.STICK);
                    countToReturn=1;
                }
                else
                {
                    countToReturn = Math.floorDiv(durability,devider);
                }

                ItemStack toReturn = repairIngredientStack.copy();
                repairIngredientStack.setCount(countToReturn);
                handler.extractItem(slot,toReturn.getCount(),false);
                world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                pedestal.addItem(toReturn);
            }
            else if((input instanceof ArmorItem || !resultRecycler.isEmpty()) && !getItemResultRecycler.equals(Items.BARRIER))
            {
                Ingredient repairIngredient = Ingredient.fromStacks(resultRecycler);
                int returnedMaxCount = resultRecycler.getCount();
                if(resultRecycler.isEmpty())
                {
                    repairIngredient = ((ArmorItem)input).getArmorMaterial().getRepairMaterial();
                    if(((ArmorItem)input).getEquipmentSlot().equals(EquipmentSlotType.HEAD))returnedMaxCount = 5;
                    else if(((ArmorItem)input).getEquipmentSlot().equals(EquipmentSlotType.CHEST))returnedMaxCount = 8;
                    else if(((ArmorItem)input).getEquipmentSlot().equals(EquipmentSlotType.LEGS))returnedMaxCount = 7;
                    else if(((ArmorItem)input).getEquipmentSlot().equals(EquipmentSlotType.FEET))returnedMaxCount = 4;
                    else returnedMaxCount = 1;
                }

                ItemStack repairIngredientStack = repairIngredient.getMatchingStacks()[0];
                int maxdamage = itemJustExpGround.getMaxDamage();
                int damage = itemJustExpGround.getDamage();
                int durability = maxdamage - damage;
                int devider = maxdamage/returnedMaxCount;
                int countToReturn = returnedMaxCount;
                if(damage == 0)
                {
                    countToReturn = returnedMaxCount;
                }
                else if(durability<devider)
                {
                    repairIngredientStack = new ItemStack(Items.PAPER).setDisplayName(new TranslationTextComponent(getTranslationKey() + ".cloth"));
                    countToReturn=1;
                }
                else
                {
                    countToReturn = Math.floorDiv(durability,devider);
                }

                ItemStack toReturn = repairIngredientStack.copy();
                repairIngredientStack.setCount(countToReturn);
                handler.extractItem(slot,toReturn.getCount(),false);
                world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                pedestal.addItem(toReturn);
            }
            else
            {
                ItemStack toReturn = itemJustExpGround.copy();
                handler.extractItem(slot,toReturn.getCount(),false);
                world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                pedestal.addItem(toReturn);
            }
        }
        else
        {
            ItemStack toReturn = itemJustExpGround.copy();
            handler.extractItem(slot,toReturn.getCount(),false);
            world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
            pedestal.addItem(toReturn);
        }
    }

    public int getExpBuffer(ItemStack stack)
    {
        return  30;
    }

    @Override
    public void chatDetails(PlayerEntity player, PedestalTileEntity pedestal)
    {
        ItemStack stack = pedestal.getCoinOnPedestal();

        TranslationTextComponent name = new TranslationTextComponent(getTranslationKey() + ".tooltip_name");
        name.mergeStyle(TextFormatting.GOLD);
        player.sendMessage(name,Util.DUMMY_UUID);

        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".chat_xp");
        xpstored.appendString(""+ getExpLevelFromCount(getXPStored(stack)) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(xpstored,Util.DUMMY_UUID);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString(getExpTransferRateString(stack));
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate, Util.DUMMY_UUID);

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
        super.addInformation(stack, worldIn, tooltip, flagIn);

        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".tooltip_rate");
        rate.appendString(getExpTransferRateString(stack));
        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));
        speed.mergeStyle(TextFormatting.RED);

        tooltip.add(speed);
    }

    public static final Item RECYCLER = new ItemUpgradeRecycler(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/recycler"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(RECYCLER);
    }


}
