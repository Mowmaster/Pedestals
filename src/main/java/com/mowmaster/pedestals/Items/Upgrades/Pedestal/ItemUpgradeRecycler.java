package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.google.common.collect.Maps;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Items.Filters.IItemMode;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Configs.PedestalConfig;
import com.mowmaster.pedestals.Items.Filters.BaseFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mowmaster.pedestals.PedestalUtils.References.MODID;

public class ItemUpgradeRecycler extends ItemUpgradeBase implements IHasModeTypes
{
    public ItemUpgradeRecycler(Properties p_41383_) {
        super(new Properties());
    }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        //remove NBT saved on upgrade here
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_storedExpUnderOne");
    }

    private boolean canProcess(ItemStack stackIn)
    {
        if(!stackIn.isEmpty())
        {
            boolean tagged = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation("pedestals","pedestals_cannot_recycle"))).stream().toList().contains(stackIn);
            if(!tagged && (stackIn.getItem() instanceof ArmorItem || stackIn.getItem() instanceof TieredItem))
            {
                return true;
            }
        }
        else
        {
            return false;
        }

        return false;
    }

    public int getNextSlotWithItemsCapFilteredAndPasses(BasePedestalBlockEntity pedestal, LazyOptional<IItemHandler> cap)
    {
        AtomicInteger slot = new AtomicInteger(-1);
        if(cap.isPresent()) {

            cap.ifPresent(itemHandler -> {
                int range = itemHandler.getSlots();
                for(int i=0;i<range;i++)
                {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                    //find a slot with items
                    if(!stackInSlot.isEmpty())
                    {
                        //check if it could pull the item out or not
                        if(!itemHandler.extractItem(i,1 ,true ).equals(ItemStack.EMPTY))
                        {
                            //If pedestal is empty accept any items
                            if(passesItemFilter(pedestal,stackInSlot) && canProcess(stackInSlot))
                            {
                                ItemStack itemFromPedestal = pedestal.getMatchingItemInPedestalOrEmptySlot(stackInSlot);
                                if(itemFromPedestal.isEmpty())
                                {
                                    slot.set(i);
                                    break;
                                }
                                //if stack in pedestal matches items in slot
                                else if(doItemsMatch(itemFromPedestal,stackInSlot))
                                {
                                    slot.set(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }

        return slot.get();
    }

    @javax.annotation.Nullable
    protected AbstractCookingRecipe getNormalRecipe(Level level, ItemStack stackIn) {
        Container cont = MowLibContainerUtils.getContainer(1);
        cont.setItem(-1,stackIn);

        if (level == null) return null;

        RecipeManager recipeManager = level.getRecipeManager();
        Optional<BlastingRecipe> optional = recipeManager.getRecipeFor(RecipeType.BLASTING, cont, level);
        if (optional.isPresent()) return optional.get();

        Optional<SmeltingRecipe> optional1 = recipeManager.getRecipeFor(RecipeType.SMELTING, cont, level);
        return optional1.orElse(null);
    }

    protected Collection<ItemStack> getNormalResults(AbstractCookingRecipe recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResultItem()));
    }

    protected float getNormalResultsExp(AbstractCookingRecipe recipe) {
        return (recipe == null)?(0.0F):(recipe.getExperience());
    }

    private boolean canAddExperienceFromProcessing(BasePedestalBlockEntity pedestal, float experience)
    {
        ItemStack coin = pedestal.getCoinOnPedestal();
        int getExperienceStored = MowLibCompoundTagUtils.readIntegerFromNBT(MODID, coin.getTag(), "_storedExpUnderOne");
        int convertFloatDecimal = (int)(experience * 100);
        int sum = getExperienceStored + convertFloatDecimal;
        int expToAdd = 0;
        int remainingXP = 0;
        boolean returner = false;
        if(sum>=100)
        {
            while(sum>=100)
            {
                expToAdd++;
                sum -=100;
            }

            if(pedestal.addExperience(expToAdd,true)>0)
            {
                pedestal.addExperience(expToAdd,false);
                remainingXP = sum;
                returner = true;
            }
        }
        else {
            remainingXP = sum;
        }

        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, coin.getTag(), remainingXP, "_storedExpUnderOne");

        return returner;
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin)
    {
        BlockPos posInventory = getPosOfBlockBelow(level,pedestalPos,1);
        ItemStack itemFromInv = ItemStack.EMPTY;
        LazyOptional<IItemHandler> cap = MowLibItemUtils.findItemHandlerAtPos(level,posInventory,getPedestalFacing(level, pedestalPos),true);
        if(!isInventoryEmpty(cap)) {
            if (cap.isPresent()) {
                IItemHandler handler = cap.orElse(null);
                BlockEntity invToPullFrom = level.getBlockEntity(posInventory);
                if(invToPullFrom instanceof BasePedestalBlockEntity) {
                    itemFromInv = ItemStack.EMPTY;

                }
                else {
                    if (handler != null) {
                        //XP
                        //Grindstone stuff basically
                        if(canTransferXP(coin))
                        {
                            int i = getNextSlotWithItemsCapFiltered(pedestal,cap);
                            if(i>=0)
                            {
                                itemFromInv = handler.getStackInSlot(i);
                                ItemStack copyIncoming = itemFromInv.copy();
                                if(itemFromInv != null && !itemFromInv.isEmpty())
                                {
                                    if(copyIncoming.isEnchanted() || copyIncoming.getItem() instanceof EnchantedBookItem)
                                    {
                                        int getXPSpace = pedestal.spaceForExperience();
                                        int getGrindedXpCount = getItemsExpDisenchantAmount(copyIncoming);
                                        if(getXPSpace >= getGrindedXpCount)
                                        {
                                            Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                            ItemStack stackToReturn = (copyIncoming.getItem() instanceof EnchantedBookItem)?(new ItemStack(Items.BOOK,1)):(copyIncoming);
                                            EnchantmentHelper.setEnchantments(enchantsNone,stackToReturn);

                                            if(!stackToReturn.isEmpty())
                                            {
                                                if(canTransferItems(coin))
                                                {
                                                    if(pedestal.addExperience(getGrindedXpCount,true)>0)
                                                    {
                                                        EnchantmentHelper.setEnchantments(enchantsNone,itemFromInv);
                                                        pedestal.addExperience(getGrindedXpCount,false);
                                                    }
                                                }
                                                else
                                                {
                                                    if(!handler.extractItem(i,1 ,true ).isEmpty() && pedestal.addItem(stackToReturn, true) && pedestal.addExperience(getGrindedXpCount,true)>0)
                                                    {
                                                        handler.extractItem(i,1 ,false );
                                                        pedestal.addExperience(getGrindedXpCount,false);
                                                        pedestal.addItem(stackToReturn, false);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        //Items
                        //Handle last, just incase any items match the other options
                        if(canTransferItems(coin))
                        {
                            int i = getNextSlotWithItemsCapFilteredAndPasses(pedestal,cap);
                            if(i>=0)
                            {
                                itemFromInv = handler.getStackInSlot(i);
                                ItemStack copyIncoming = itemFromInv.copy();
                                copyIncoming.setCount(1);
                                if(itemFromInv != null && !itemFromInv.isEmpty())
                                {
                                    AbstractCookingRecipe recipe = getNormalRecipe(pedestal.getLevel(), copyIncoming);
                                    float getEXP = getNormalResultsExp(recipe);
                                    ItemStack returnedStack = getNormalResults(recipe).stream().findFirst().orElse(ItemStack.EMPTY);
                                    if(!returnedStack.isEmpty())
                                    {
                                        if(!handler.extractItem(i,1 ,true ).isEmpty() && pedestal.addItem(returnedStack, true))
                                        {
                                            handler.extractItem(i,1 ,false );
                                            if(getEXP > 0.0F){canAddExperienceFromProcessing(pedestal,getEXP);}
                                            pedestal.addItem(returnedStack, false);
                                        }
                                    }
                                    else
                                    {
                                        if(!handler.extractItem(i,1 ,true ).isEmpty() && pedestal.addItem(copyIncoming, true))
                                        {
                                            handler.extractItem(i,1 ,false );
                                            pedestal.addItem(copyIncoming, false);
                                        }
                                    }
                                }
                            }
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

            exp += enchantment.getMinCost(integer.intValue());
        }
        return exp*stack.getCount();
    }
}
