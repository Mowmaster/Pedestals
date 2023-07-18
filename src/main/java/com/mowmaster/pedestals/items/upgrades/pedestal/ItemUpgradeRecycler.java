package com.mowmaster.pedestals.items.upgrades.pedestal;

import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.pedestals.blocks.pedestal.BasePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

import static com.mowmaster.pedestals.pedestalutils.References.MODID;

public class ItemUpgradeRecycler extends ItemUpgradeBase implements IHasModeTypes {
    public ItemUpgradeRecycler(Properties p_41383_) { super(p_41383_); }

    @Override
    public boolean canModifySpeed(ItemStack upgradeItemStack) { return true; }

    @Override
    public int getUpgradeWorkRange(ItemStack coinUpgrade) { return 0; }

    @Override
    public void actionOnRemovedFromPedestal(BasePedestalBlockEntity pedestal, ItemStack coinInPedestal) {
        MowLibCompoundTagUtils.removeCustomTagFromNBT(MODID, coinInPedestal.getTag(), "_storedExpUnderOne");
    }

    private boolean canProcess(ItemStack toProcess) {
        List<Item> cannotProcessItems = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation(MODID, "pedestals_cannot_recycle"))).stream().toList();
        Item toProcessItem = toProcess.getItem();
        return !toProcess.isEmpty() &&
            !cannotProcessItems.contains(toProcessItem) &&
            (
                toProcess.isEnchanted() ||
                toProcessItem instanceof EnchantedBookItem ||
                toProcessItem instanceof ArmorItem ||
                toProcessItem instanceof TieredItem
            );
    }

    public Optional<Integer> getFirstSlotWithItemThatCanBeProcessed(BasePedestalBlockEntity pedestal, IItemHandler itemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(i);
            if (canProcess(stackInSlot)) {
                if (!itemHandler.extractItem(i,1 ,true).equals(ItemStack.EMPTY)) {
                    if (passesItemFilter(pedestal, stackInSlot) && canProcess(stackInSlot)) {
                        return Optional.of(i);
                    }
                }
            }
        }
        return Optional.empty();
    }

    protected Optional<? extends AbstractCookingRecipe> getNormalRecipe(Level level, ItemStack input) {
        Container container = new SimpleContainer(input);

        RecipeManager recipeManager = level.getRecipeManager();
        Optional<? extends AbstractCookingRecipe> blastingRecipeMaybe = recipeManager.getRecipeFor(RecipeType.BLASTING, container, level);
        if (blastingRecipeMaybe.isPresent()) {
            return blastingRecipeMaybe;
        } else {
            return recipeManager.getRecipeFor(RecipeType.SMELTING, container, level);
        }
    }

    private void addExperienceFromProcessing(BasePedestalBlockEntity pedestal, ItemStack upgrade, float experience) {
        int getExperienceStored = MowLibCompoundTagUtils.readIntegerFromNBT(MODID, upgrade.getOrCreateTag(), "_storedExpUnderOne");
        int convertFloatDecimal = (int)(experience * 100);
        int sum = getExperienceStored + convertFloatDecimal;
        int expToAdd = sum / 100; // integer division floors
        int remainingExp = sum % 100;
        pedestal.addExperience(expToAdd,false);

        MowLibCompoundTagUtils.writeIntegerToNBT(MODID, upgrade.getTag(), remainingExp, "_storedExpUnderOne");
    }

    @Override
    public void upgradeAction(Level level, BasePedestalBlockEntity pedestal, BlockPos pedestalPos, ItemStack coin) {
        BlockPos inventoryPos = getPosOfBlockBelow(level,pedestal.getPos(),1);
        BlockEntity invToPullFrom = level.getBlockEntity(inventoryPos);
        if (invToPullFrom instanceof BasePedestalBlockEntity) {
            return;
        }

        MowLibItemUtils.findItemHandlerAtPos(level, inventoryPos, getPedestalFacing(level, pedestalPos), true).ifPresent(handler -> {
            // Handle enchants.
            Optional<Integer> slotToHandle = getFirstSlotWithItemThatCanBeProcessed(pedestal, handler);
            if (canTransferXP(coin)) {
                boolean shouldStop = slotToHandle.map(i -> {
                    ItemStack itemFromInv = handler.getStackInSlot(i);
                    if (itemFromInv.getItem() instanceof EnchantedBookItem) {
                        int grindedExpAmount = getItemsExpDisenchantAmount(itemFromInv);
                        if (grindedExpAmount <= pedestal.spaceForExperience()) {
                            ItemStack stackToReturn = new ItemStack(Items.BOOK, itemFromInv.getCount());
                            if (!handler.extractItem(i, 1, true).isEmpty() && pedestal.addItem(stackToReturn, true) && pedestal.addExperience(grindedExpAmount, true) > 0) {
                                handler.extractItem(i, 1, false);
                                pedestal.addExperience(grindedExpAmount, false);
                                pedestal.addItem(stackToReturn, false);
                                return true;
                            }
                        }
                    } else if (itemFromInv.isEnchanted()) {
                        int grindedExpAmount = getItemsExpDisenchantAmount(itemFromInv);
                        if (grindedExpAmount <= pedestal.spaceForExperience()) {
                            if (canTransferItems(coin)) {
                                if (pedestal.addExperience(grindedExpAmount,true) > 0) {
                                    EnchantmentHelper.setEnchantments(Map.of(), itemFromInv);
                                    pedestal.addExperience(grindedExpAmount,false);
                                }
                            } else {
                                ItemStack stackToReturn = itemFromInv.copy();
                                EnchantmentHelper.setEnchantments(Map.of(), stackToReturn);
                                if (!handler.extractItem(i,1 ,true).isEmpty() && pedestal.addItem(stackToReturn, true) && pedestal.addExperience(grindedExpAmount,true) > 0) {
                                    handler.extractItem(i,1 ,false);
                                    pedestal.addExperience(grindedExpAmount,false);
                                    pedestal.addItem(stackToReturn, false);
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }).orElse(false);
                if (shouldStop) {
                    slotToHandle = Optional.empty();
                }
            }

            // Handle recycling metal tools etc.
            if (canTransferItems(coin)) {
                slotToHandle.ifPresent(i -> {
                    ItemStack itemFromInv = handler.getStackInSlot(i);
                    if (!itemFromInv.isEmpty()) {
                        Optional<? extends AbstractCookingRecipe> recipeMaybe = getNormalRecipe(level, itemFromInv);
                        ItemStack returnedStack = recipeMaybe.map(AbstractCookingRecipe::getResultItem).orElse(ItemStack.EMPTY);
                        if (!returnedStack.isEmpty()) {
                            if (!handler.extractItem(i,1 ,true).isEmpty() && pedestal.addItem(returnedStack, true)) {
                                float expAmount = recipeMaybe.map(AbstractCookingRecipe::getExperience).orElse(0.0F);
                                if (expAmount > 0.0F) { addExperienceFromProcessing(pedestal, coin, expAmount); }
                                handler.extractItem(i,1 ,false);
                                pedestal.addItem(returnedStack, false);
                            }
                        } else {
                            ItemStack toAdd = itemFromInv.copy();
                            toAdd.setCount(1);
                            if (!handler.extractItem(i,1 ,true).isEmpty() && pedestal.addItem(toAdd, true)) {
                                handler.extractItem(i,1 ,false);
                                pedestal.addItem(toAdd, false);
                            }
                        }
                    }
                });
            }
        });
    }

    public int getItemsExpDisenchantAmount(ItemStack stack) {
        int exp = 0;
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer integer = entry.getValue();

            exp += enchantment.getMinCost(integer);
        }
        return exp * stack.getCount();
    }
}
