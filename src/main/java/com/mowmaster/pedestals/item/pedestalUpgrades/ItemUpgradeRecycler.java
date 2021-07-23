package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.google.common.collect.Maps;
import com.mowmaster.pedestals.api.upgrade.IUpgradeBase;
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
import net.minecraft.item.crafting.*;
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

public class ItemUpgradeRecycler extends ItemUpgradeBase
{

    public ItemUpgradeRecycler(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public boolean canAcceptCapacity() {
        return false;
    }

    @Override
    public boolean canAcceptAdvanced() {
        return true;
    }

    public void updateAction(World world, PedestalTileEntity pedestal)
    {
        if(!world.isRemote)
        {
            ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
            ItemStack itemInPedestal = pedestal.getItemInPedestal();
            BlockPos pedestalPos = pedestal.getPos();

            int speed = getOperationSpeed(coinInPedestal);
            if(!pedestal.isPedestalBlockPowered(world,pedestalPos))
            {
                if (world.getGameTime()%speed == 0) {
                    //Just does the unenchanting bit
                    if(itemInPedestal.isEmpty())
                    {
                        if(hasAdvancedInventoryTargeting(coinInPedestal))
                        {
                            recyclerActionAdvanced(pedestal);
                        }
                        else
                        {
                            doNormalAction(pedestal);
                        }
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

    @Nullable
    protected AbstractCookingRecipe getNormalRecipe(World world, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);

        if (world == null) return null;

        RecipeManager recipeManager = world.getRecipeManager();
        Optional<BlastingRecipe> optional = recipeManager.getRecipe(IRecipeType.BLASTING, inv, world);
        if (optional.isPresent()) return optional.get();

        Optional<FurnaceRecipe> optional1 = recipeManager.getRecipe(IRecipeType.SMELTING, inv, world);
        return optional1.orElse(null);
    }

    protected Collection<ItemStack> getNormalResults(AbstractCookingRecipe recipe, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getCraftingResult(inv)));
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

    public void doNormalAction(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos posOfPedestal = pedestal.getPos();
        //Need to null check invalid recipes
        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        ItemStack itemFromInv = ItemStack.EMPTY;
        ResourceLocation disabledRecycles = new ResourceLocation("pedestals", "recycler_blacklist");
        ITag<Item> BLACKLISTED = ItemTags.getCollection().get(disabledRecycles);
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
                                .filter(itemStack -> !itemStack.isEmpty())
                                .findFirst().orElse(ItemStack.EMPTY);

                        Collection<ItemStack> jsonResults = getProcessResultsRecycler(getRecipeRecycler(pedestal.getWorld(),nextItemToGrind));
                        ItemStack resultRecycler = (jsonResults.iterator().next().isEmpty())?(ItemStack.EMPTY):(jsonResults.iterator().next());
                        Item getItemResultRecycler = resultRecycler.getItem();
                        int slotItemToGrind = getSlotWithMatchingStackExact(cap,nextItemToGrind);
                        if(!nextItemToGrind.isEmpty() && (nextItemToGrind.getItem() instanceof IUpgradeBase || nextItemToGrind.getItem() instanceof TieredItem || nextItemToGrind.getItem() instanceof ArmorItem ||  !resultRecycler.isEmpty()) && !BLACKLISTED.contains(nextItemToGrind.getItem()))
                        {
                            Collection<ItemStack> smeltedResults = getNormalResults(getNormalRecipe(world,nextItemToGrind),nextItemToGrind);
                            //Make sure recipe output isnt empty
                            ItemStack resultSmelted = (smeltedResults.iterator().next().isEmpty())?(ItemStack.EMPTY):(smeltedResults.iterator().next());
                            if(!resultSmelted.isEmpty())
                            {
                                ItemStack toReturn = resultSmelted.copy();
                                toReturn.setCount(1);
                                if(!handler.extractItem(slotItemToGrind,toReturn.getCount(),true).isEmpty()){
                                    handler.extractItem(slotItemToGrind,toReturn.getCount(),false);
                                    if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                    pedestal.addItemOverride(toReturn);
                                }

                            }
                            else
                            {
                                ItemStack toReturn = nextItemToGrind.copy();
                                if(!handler.extractItem(slotItemToGrind,toReturn.getCount(),true).isEmpty()){
                                    handler.extractItem(slotItemToGrind,toReturn.getCount(),false);
                                    //if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                    pedestal.addItemOverride(toReturn);
                                }

                            }
                        }
                        else
                        {
                            ItemStack toReturn = nextItemToGrind.copy();
                            if(!handler.extractItem(slotItemToGrind,toReturn.getCount(),true).isEmpty()){
                                handler.extractItem(slotItemToGrind,toReturn.getCount(),false);
                                //if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                pedestal.addItemOverride(toReturn);
                            }

                        }
                    }
                }
            }
        }
    }

    public void recyclerActionAdvanced(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();
        BlockPos posOfPedestal = pedestal.getPos();

        BlockPos posInventory = getPosOfBlockBelow(world,posOfPedestal,1);
        ItemStack itemFromInv = ItemStack.EMPTY;
        ResourceLocation disabledRecycles = new ResourceLocation("pedestals", "recycler_blacklist");
        ITag<Item> BLACKLISTED = ItemTags.getCollection().get(disabledRecycles);
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
                                .filter(itemStack -> !itemStack.isEmpty())
                                .findFirst().orElse(ItemStack.EMPTY);

                        Item input = nextItemToGrind.getItem();
                        Collection<ItemStack> jsonResults = getProcessResultsRecycler(getRecipeRecycler(pedestal.getWorld(),nextItemToGrind));
                        ItemStack resultRecycler = (jsonResults.iterator().next().isEmpty())?(ItemStack.EMPTY):(jsonResults.iterator().next());
                        Item getItemResultRecycler = resultRecycler.getItem();
                        int slotItemToGrind = getSlotWithMatchingStackExact(cap,nextItemToGrind);


                        if((input instanceof TieredItem || !resultRecycler.isEmpty()) && !BLACKLISTED.contains(nextItemToGrind.getItem()))
                        {
                            //Assume its from the recipe, but if not, then set to another itemstack
                            Ingredient repairIngredient = Ingredient.fromStacks(resultRecycler);
                            int returnedMaxCount = resultRecycler.getCount();
                            if(resultRecycler.isEmpty())
                            {
                                if(input instanceof TieredItem)repairIngredient = ((TieredItem)input).getTier().getRepairMaterial();

                                if(input instanceof PickaxeItem || nextItemToGrind.getToolTypes().contains(ToolType.PICKAXE))returnedMaxCount=3;
                                else if(input instanceof AxeItem || nextItemToGrind.getToolTypes().contains(ToolType.AXE))returnedMaxCount=3;
                                else if(input instanceof SwordItem)returnedMaxCount=2;
                                else if(input instanceof HoeItem || nextItemToGrind.getToolTypes().contains(ToolType.HOE))returnedMaxCount=2;
                                else if(input instanceof ShovelItem || nextItemToGrind.getToolTypes().contains(ToolType.SHOVEL))returnedMaxCount=1;
                                else returnedMaxCount = 1;
                            }

                            ItemStack repairIngredientStack = (repairIngredient.getMatchingStacks().length>0)?(repairIngredient.getMatchingStacks()[0]):(new ItemStack(Items.STICK));
                            int maxdamage = nextItemToGrind.getMaxDamage();
                            int damage = nextItemToGrind.getDamage();
                            int durability = maxdamage - damage;
                            int devider = Math.floorDiv(maxdamage,returnedMaxCount);
                            int countToReturn = returnedMaxCount;
                            if(damage <= 0)
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
                                int estCount = Math.floorDiv(durability,devider);
                                countToReturn = (estCount==returnedMaxCount)?(estCount-1):(estCount);
                            }

                            ItemStack toReturn = repairIngredientStack.copy();
                            toReturn.setCount(countToReturn);
                            repairIngredientStack.setCount(countToReturn);
                            if(!handler.extractItem(slotItemToGrind,1,true).isEmpty()){
                                handler.extractItem(slotItemToGrind,1,false);
                                if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                pedestal.addItemOverride(toReturn);
                            }

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

                            ItemStack repairIngredientStack = (repairIngredient.getMatchingStacks().length>0)?(repairIngredient.getMatchingStacks()[0]):(new ItemStack(Items.PAPER).setDisplayName(new TranslationTextComponent(getTranslationKey() + ".cloth")));
                            int maxdamage = nextItemToGrind.getMaxDamage();
                            int damage = nextItemToGrind.getDamage();
                            int durability = maxdamage - damage;
                            int devider = Math.floorDiv(maxdamage,returnedMaxCount);
                            int countToReturn = returnedMaxCount;
                            if(damage <= 0)
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
                                int estCount = Math.floorDiv(durability,devider);
                                countToReturn = (estCount==returnedMaxCount)?(estCount-1):(estCount);
                            }

                            ItemStack toReturn = repairIngredientStack.copy();
                            toReturn.setCount(countToReturn);
                            repairIngredientStack.setCount(countToReturn);
                            if(!handler.extractItem(slotItemToGrind,1,true).isEmpty()){
                                handler.extractItem(slotItemToGrind,1,false);
                                if(!pedestal.hasMuffler())world.playSound((PlayerEntity) null, posOfPedestal.getX(), posOfPedestal.getY(), posOfPedestal.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                pedestal.addItemOverride(toReturn);
                            }

                        }
                        else
                        {
                            doNormalAction(pedestal);
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
