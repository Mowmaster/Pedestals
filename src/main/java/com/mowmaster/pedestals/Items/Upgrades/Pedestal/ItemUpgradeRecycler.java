package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.PedestalTags;
import com.mowmaster.pedestals.Transport.ExperienceTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Optional;

// Takes enchanted things, tools and armour from the inventory below, one at a time.
// Experience mode: strips enchantments into experience (enchanted books become books).
// Items mode: smelts what's left in a blast furnace / furnace recipe (iron sword -> iron nugget), or just moves it.
// Turn the modes on and off like the transport upgrades (only items and experience do anything).
public class ItemUpgradeRecycler extends ItemUpgradeBase
{
    // Recipe experience below one point is kept here (1.20's _storedExpUnderOne), in hundredths
    private static final String XP_FRACTION = "xp_fraction";

    public ItemUpgradeRecycler(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED);
    }

    @Override
    public boolean usesTransportModes()
    {
        return true;
    }

    private static boolean canProcess(ItemStack stack)
    {
        if (stack.isEmpty() || stack.is(PedestalTags.CANNOT_RECYCLE)) return false;
        return stack.isEnchanted() || stack.has(DataComponents.STORED_ENCHANTMENTS)
                || stack.has(DataComponents.EQUIPPABLE) || stack.has(DataComponents.TOOL) || stack.has(DataComponents.WEAPON);
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        ResourceHandler<ItemResource> inventory = getInventoryBelow(level, pedestal);
        if (inventory == null) return;
        boolean items = canTransport(upgrade, PedestalTransportTypes.ITEMS.get());
        boolean experience = canTransport(upgrade, PedestalTransportTypes.EXPERIENCE.get());

        for (int index = 0; index < inventory.size(); index++)
        {
            ItemResource resource = inventory.getResource(index);
            if (resource.isEmpty()) continue;
            ItemStack input = resource.toStack(1);
            if (!canProcess(input)) continue;

            // Experience mode: strip the enchantments
            int points = 0;
            ItemStack output = input;
            if (experience && (input.isEnchanted() || input.has(DataComponents.STORED_ENCHANTMENTS)))
            {
                points = disenchantValue(input);
                output = input.is(Items.ENCHANTED_BOOK) ? new ItemStack(Items.BOOK) : stripped(input);
            }

            // Items mode: smelt what's left (books just come back as books)
            int recipeHundredths = 0;
            if (items && !output.is(Items.BOOK))
            {
                Optional<RecipeHolder<? extends AbstractCookingRecipe>> recipe = findRecipe(level, output);
                if (recipe.isPresent())
                {
                    SingleRecipeInput recipeInput = new SingleRecipeInput(output);
                    recipeHundredths = Math.round(recipe.get().value().experience() * 100);
                    output = assemble(recipe.get().value(), recipeInput);
                }
            }
            else if (!items && points == 0)
            {
                continue; // nothing this mode can do with it
            }

            if (process(pedestal, inventory, index, resource, output, points))
            {
                if (recipeHundredths > 0) addRecipeExperience(pedestal, recipeHundredths);
                return; // one item per operation
            }
        }
    }

    // In one transaction: take the item, put the output and experience in the pedestal. Nothing happens unless all fit.
    private static boolean process(BasePedestalBlockEntity pedestal, ResourceHandler<ItemResource> inventory, int index,
                                   ItemResource resource, ItemStack output, int points)
    {
        try (Transaction transaction = Transaction.openRoot())
        {
            if (inventory.extract(index, resource, 1, transaction) != 1) return false;
            if (!output.isEmpty() && ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(output), output.getCount(), transaction) != output.getCount()) return false;
            if (points > 0 && pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get()).insert(ExperienceTransportType.EXPERIENCE, points, transaction) != points) return false;
            transaction.commit();
            return true;
        }
    }

    // Blast furnace first (ores, tools, armour), then furnace
    private static Optional<RecipeHolder<? extends AbstractCookingRecipe>> findRecipe(ServerLevel level, ItemStack stack)
    {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        Optional<RecipeHolder<? extends AbstractCookingRecipe>> blasting = level.recipeAccess().getRecipeFor(RecipeType.BLASTING, input, level).map(holder -> holder);
        if (blasting.isPresent()) return blasting;
        return level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level).map(holder -> holder);
    }

    private static ItemStack assemble(AbstractCookingRecipe recipe, SingleRecipeInput input)
    {
        return recipe.assemble(input);
    }

    private static ItemStack stripped(ItemStack stack)
    {
        ItemStack copy = stack.copy();
        EnchantmentHelper.setEnchantments(copy, ItemEnchantments.EMPTY);
        return copy;
    }

    // Each enchantment is worth its minimum enchanting cost at its level (as in 1.20)
    private static int disenchantValue(ItemStack stack)
    {
        int points = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet())
        {
            points += entry.getKey().value().getMinCost(entry.getIntValue());
        }
        return points * stack.getCount();
    }

    // Furnace recipes give fractions of a point: keep the remainder for next time
    private static void addRecipeExperience(BasePedestalBlockEntity pedestal, int hundredths)
    {
        int total = pedestal.getUpgradeData().getIntOr(XP_FRACTION, 0) + hundredths;
        int points = total / 100;
        if (points > 0)
        {
            try (Transaction transaction = Transaction.openRoot())
            {
                pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get()).insert(ExperienceTransportType.EXPERIENCE, points, transaction);
                transaction.commit();
            }
        }
        pedestal.getUpgradeData().putInt(XP_FRACTION, total % 100);
        pedestal.upgradeDataChanged();
    }
}
