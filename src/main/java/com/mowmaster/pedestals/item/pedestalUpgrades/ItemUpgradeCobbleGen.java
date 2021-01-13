package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.enchants.EnchantmentArea;
import com.mowmaster.pedestals.enchants.EnchantmentCapacity;
import com.mowmaster.pedestals.enchants.EnchantmentOperationSpeed;
import com.mowmaster.pedestals.enchants.EnchantmentRange;
import com.mowmaster.pedestals.recipes.CobbleGenRecipe;
import com.mowmaster.pedestals.recipes.CobbleGenSilkRecipe;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;
import static com.mowmaster.pedestals.references.Reference.MODID;

public class ItemUpgradeCobbleGen extends ItemUpgradeBase
{
    private int maxStored = 2000000000;

    public ItemUpgradeCobbleGen(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            if(getCobbleStored(pedestal)>0)
            {
                float f = (float)getCobbleStored(pedestal)/(float)maxCobbleStorage(pedestal);
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    public int getCobbleGenSpawnRate(ItemStack stack)
    {
        int intCobbleSpawned = 1;
        switch (getCapacityModifier(stack))
        {
            case 0:
                intCobbleSpawned = 1;
                break;
            case 1:
                intCobbleSpawned=4;
                break;
            case 2:
                intCobbleSpawned = 8;
                break;
            case 3:
                intCobbleSpawned = 16;
                break;
            case 4:
                intCobbleSpawned = 32;
                break;
            case 5:
                intCobbleSpawned=64;
                break;
            default: intCobbleSpawned=1;
        }

        return  intCobbleSpawned;
    }

    @Nullable
    protected CobbleGenRecipe getRecipeNormal(World world, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);
        return world == null ? null : world.getRecipeManager().getRecipe(CobbleGenRecipe.recipeType, inv, world).orElse(null);
    }

    @Nullable
    protected CobbleGenSilkRecipe getRecipeSilk(World world, ItemStack stackIn) {
        Inventory inv = new Inventory(stackIn);
        return world == null ? null : world.getRecipeManager().getRecipe(CobbleGenSilkRecipe.recipeType, inv, world).orElse(null);
    }

    protected Collection<ItemStack> getProcessResultsNormal(CobbleGenRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResult()));
    }

    protected Collection<ItemStack> getProcessResultsSilk(CobbleGenSilkRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResult()));
    }

    public Item getItemToSpawn(PedestalTileEntity pedestal)
    {
        World world = pedestal.getWorld();
        BlockPos pedestalPos = pedestal.getPos();
        BlockPos posBlockBelow = getPosOfBlockBelow(world,pedestalPos,1);
        BlockState getBlockBelow = world.getBlockState(posBlockBelow);
        ItemStack itemBlockBelow = new ItemStack(getBlockBelow.getBlock());
        ItemStack coinInPedestal = pedestal.getCoinOnPedestal();

        Collection<ItemStack> jsonResults = getProcessResultsNormal(getRecipeNormal(world,itemBlockBelow));
        ItemStack resultSmelted = (jsonResults.iterator().next().isEmpty())?(ItemStack.EMPTY):(jsonResults.iterator().next());
        Item getItem = resultSmelted.getItem();
        if(resultSmelted.isEmpty())
        {
            getItem = new ItemStack(Items.COBBLESTONE).getItem();
        }

        if(coinInPedestal.isEnchanted())
        {
            if(EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH,coinInPedestal)> 0)
            {
                jsonResults = getProcessResultsSilk(getRecipeSilk(world,itemBlockBelow));
                resultSmelted = (jsonResults.iterator().next().isEmpty())?(ItemStack.EMPTY):(jsonResults.iterator().next());
                getItem = resultSmelted.getItem();
                if(resultSmelted.isEmpty())
                {
                    getItem = new ItemStack(Items.STONE).getItem();
                }
            }
        }

        return getItem;
    }

    @Override
    public ItemStack customExtractItem(PedestalTileEntity pedestal, int amountOut, boolean simulate)
    {
        //Return stack that was extracted, (it cant be more then the amountOut or max size)
        ItemStack stackInPed = pedestal.getItemInPedestalOverride();
        ItemStack itemStackToExtract = new ItemStack(getItemToSpawn(pedestal));
        int cobbleToRemove = removeCobble(pedestal,amountOut,true);
        if(getCobbleStored(pedestal)<=0)
        {
            //Should default to normal pedestal pull out methods
            return new ItemStack(Items.COMMAND_BLOCK);
        }
        else if(cobbleToRemove==0)
        {
            itemStackToExtract.setCount(amountOut);
            if(!simulate)
            {
                removeCobble(pedestal,amountOut,false);
            }

            ItemStack toReturn = new ItemStack((getCobbleStored(pedestal)>0)?(itemStackToExtract.getItem()):(stackInPed.getItem()),(amountOut>itemStackToExtract.getMaxStackSize())?(itemStackToExtract.getMaxStackSize()):(amountOut));
            return (toReturn.getCount()>0 || toReturn.isEmpty())?(toReturn):(ItemStack.EMPTY);
            //Fm in e6 discord pinged me an issue where mek was spamming the console
            //#BlameMek
            //https://github.com/mekanism/Mekanism/blob/99f3b1e517a58f825349772cfd981d15f1c40e8f/src/main/java/mekanism/common/lib/inventory/TileTransitRequest.java#L67
            //return new ItemStack((getCobbleStored(pedestal)>0)?(itemStackToExtract.getItem()):(stackInPed.getItem()),(amountOut>itemStackToExtract.getMaxStackSize())?(itemStackToExtract.getMaxStackSize()):(amountOut));
        }
        else
        {
            itemStackToExtract.setCount(cobbleToRemove);
            if(!simulate)
            {
                removeCobble(pedestal,cobbleToRemove,false);
            }
            return itemStackToExtract;
        }
    }

    @Override
    public ItemStack customInsertItem(PedestalTileEntity pedestal, ItemStack stackIn, boolean simulate)
    {
        //Return stack to be inserted if nothing can be accepted, otherwise return Empty if All can be inserted
        if(stackIn.getItem().equals(getItemToSpawn(pedestal)))
        {
            int cobbleToAdd = addCobble(pedestal,stackIn.getCount(),true);
            if(availableCobbleStorageSpace(pedestal)>0)
            {
                if(cobbleToAdd==0)
                {
                    if(!simulate)
                    {
                        addCobble(pedestal,stackIn.getCount(),false);
                    }
                    return ItemStack.EMPTY;
                }
                else
                {
                    ItemStack copyStackIn = stackIn.copy();
                    copyStackIn.setCount(cobbleToAdd);
                    if(!simulate)
                    {
                        addCobble(pedestal,cobbleToAdd,false);
                    }
                    int currentIn = stackIn.getCount();
                    int diff = currentIn - cobbleToAdd;
                    copyStackIn.setCount(diff);
                    return copyStackIn;
                }
            }
        }

        return stackIn;
    }

    @Override
    public ItemStack customStackInSlot(PedestalTileEntity pedestal, ItemStack stackFromHandler)
    {
        if(!stackFromHandler.isEmpty())
        {
            Item getItem = getItemToSpawn(pedestal);
            ItemStack getItemStackInPedestal = pedestal.getItemInPedestalOverride();
            Item getItemInPedestal = getItemStackInPedestal.getItem();
            int stored = getCobbleStored(pedestal);
            if(getItemInPedestal.equals(getItem) || getItemStackInPedestal.isEmpty())
            {
                if(stored>0)
                {
                    int amount = getCobbleStored(pedestal)+pedestal.getItemInPedestalOverride().getCount();
                    ItemStack getStack = new ItemStack(getItem,amount);
                    return getStack;
                }
            }
            else
            {
                removeCobble(pedestal,stored,false);
                return getItemStackInPedestal;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int customSlotLimit(PedestalTileEntity pedestal)
    {
        return maxCobbleStorage(pedestal);
    }

    public int addCobble(PedestalTileEntity pedestal, int amountIn ,boolean simulate)
    {
        //Returns 0 if it can add all cobble, otherwise returns the amount you could add
        int space = availableCobbleStorageSpace(pedestal);
        if(space>=amountIn)
        {
            if(!simulate)
            {
                int current = getCobbleStored(pedestal);
                pedestal.setStoredValueForUpgrades((current+amountIn));
            }
            return 0;
        }
        return space;
    }

    public int removeCobbleBuffer(PedestalTileEntity pedestal, int amountOut ,boolean simulate)
    {
        //Returns 0 if it can remove all cobble, otherwise returns the amount you could remove
        int current = getCobbleStored(pedestal);
        if((current - amountOut)>=0)
        {
            if(!simulate)
            {
                pedestal.setStoredValueForUpgrades((current - amountOut));
            }
            return 0;
        }
        return current;
    }

    public int removeCobble(PedestalTileEntity pedestal, int amountOut ,boolean simulate)
    {
        //Returns 0 if it can remove all cobble, otherwise returns the amount you could remove
        int current = getCobbleStored(pedestal);
        int currentActual = current+pedestal.getItemInPedestalOverride().getCount();
        if((currentActual - amountOut)>=0)
        {
            if(!simulate)
            {
                if((current - amountOut)>=0)
                {
                    pedestal.setStoredValueForUpgrades((current - amountOut));
                }
                else
                {
                    int removeFromPedestal = amountOut - current;
                    pedestal.setStoredValueForUpgrades(0);
                    pedestal.removeItemOverride(removeFromPedestal);
                }

            }
            return 0;
        }
        return current;
    }

    public int getCobbleStored(PedestalTileEntity pedestal)
    {
        return pedestal.getStoredValueForUpgrades();
    }

    public int maxCobbleStorage(PedestalTileEntity pedestal)
    {
        return maxStored;
    }

    public int availableCobbleStorageSpace(PedestalTileEntity pedestal)
    {
        return maxCobbleStorage(pedestal)-getCobbleStored(pedestal);
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
                //Keep Pedestal Full at all times
                ItemStack stackInPed = pedestal.getItemInPedestalOverride();
                if(stackInPed.getCount() < stackInPed.getMaxStackSize())
                {
                    fillPedestalAction(pedestal);
                }
                //Cobble Gen Updates once per 20 ticks (to help prevent lag)
                if (world.getGameTime()%20 == 0) {
                    TileEntity tileCheckForPedestal = world.getTileEntity(pedestalPos);
                    int intSpawnRate = getCobbleGenSpawnRate(coinInPedestal);
                    int speedMultiplier = (int)(20/speed);
                    int addAmount = intSpawnRate * speedMultiplier;
                    if(availableCobbleStorageSpace(pedestal)>0)
                    {
                        int cobbleToAdd = addCobble(pedestal,addAmount,true);
                        if(cobbleToAdd==0)
                        {
                            this.addCobble(pedestal,addAmount,false);
                        }
                        else
                        {
                            this.addCobble(pedestal,cobbleToAdd,false);
                        }
                    }
                }
            }
        }
    }

    public void fillPedestalAction(PedestalTileEntity pedestal)
    {
        ItemStack itemInPedestal = pedestal.getItemInPedestalOverride();
        int intSpace = intSpaceLeftInStack(itemInPedestal);
        int cobbleStored = this.getCobbleStored(pedestal);
        ItemStack stackSpawnedItem = new ItemStack(getItemToSpawn(pedestal),intSpace);
        if(intSpace>0 && cobbleStored>0)
        {

            int cobbleToRemove = removeCobbleBuffer(pedestal,intSpace,true);
            if(cobbleToRemove==0)
            {
                this.removeCobbleBuffer(pedestal,intSpace,false);
                pedestal.addItemOverride(stackSpawnedItem);
            }
            else
            {
                stackSpawnedItem.setCount(cobbleToRemove);
                this.removeCobbleBuffer(pedestal,cobbleToRemove,false);
                pedestal.addItemOverride(stackSpawnedItem);
            }
        }
    }

    public void upgradeAction(World world, ItemStack itemInPedestal, ItemStack coinInPedestal, BlockPos pedestalPos)
    {

    }


    @Override
    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ItemEntity)
        {
            ItemStack coin = tilePedestal.getCoinOnPedestal();
            ItemStack stackCollidedItem = ((ItemEntity) entityIn).getItem();
            if(stackCollidedItem.getItem().equals(getItemToSpawn(tilePedestal)))
            {
                int addAmount = stackCollidedItem.getCount();
                if(availableCobbleStorageSpace(tilePedestal)>0)
                {
                    int cobbleToAdd = addCobble(tilePedestal,addAmount,true);
                    if(cobbleToAdd==0)
                    {
                        this.addCobble(tilePedestal,addAmount,false);
                        ((ItemEntity) entityIn).remove();
                    }
                    else
                    {
                        int setEntityStackSize = addAmount - cobbleToAdd;
                        ItemStack stackCollidedItemCopy = stackCollidedItem.copy();
                        stackCollidedItemCopy.setCount(setEntityStackSize);
                        ((ItemEntity) entityIn).setItem(stackCollidedItemCopy);
                        this.addCobble(tilePedestal,cobbleToAdd,false);
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


        TranslationTextComponent rate = new TranslationTextComponent(getTranslationKey() + ".chat_rate");
        rate.appendString("" +  getItemTransferRate(stack) + "");
        rate.mergeStyle(TextFormatting.GRAY);
        player.sendMessage(rate,Util.DUMMY_UUID);

        TranslationTextComponent stored = new TranslationTextComponent(getTranslationKey() + ".chat_stored");
        stored.appendString("" +  (getCobbleStored(pedestal)+pedestal.getItemInPedestalOverride().getCount()) + "");
        stored.mergeStyle(TextFormatting.GREEN);
        player.sendMessage(stored, Util.DUMMY_UUID);

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
        rate.appendString("" + getCobbleGenSpawnRate(stack) + "");
        TranslationTextComponent speed = new TranslationTextComponent(getTranslationKey() + ".tooltip_speed");
        speed.appendString(getOperationSpeedString(stack));

        rate.mergeStyle(TextFormatting.GRAY);
        tooltip.add(rate);

        speed.mergeStyle(TextFormatting.RED);
        tooltip.add(speed);
    }

    public static final Item COBBLE = new ItemUpgradeCobbleGen(new Properties().maxStackSize(64).group(PEDESTALS_TAB)).setRegistryName(new ResourceLocation(MODID, "coin/cobble"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(COBBLE);
    }


}