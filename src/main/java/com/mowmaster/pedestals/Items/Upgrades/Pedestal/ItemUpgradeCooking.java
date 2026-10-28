package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Transport.EnergyTransportType;
import com.mowmaster.pedestals.Transport.ExperienceTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Optional;
import java.util.function.IntSupplier;

// A furnace that uses energy (1.20's smelter, smoker and blast furnace upgrades are this one class with a different
// recipe type and energy config). Cooks the first item in the inventory below that has a recipe, every tick, and
// puts the results and the recipe's experience in the pedestal. The speed modifier cooks that many ticks per tick
// (up to 200); each tick of cooking costs the configured energy.
public class ItemUpgradeCooking<T extends AbstractCookingRecipe> extends ItemUpgradeBase
{
    private static final String COOK_TIME = "cook_time";
    private static final String LAST_RECIPE = "last_recipe";
    private static final String XP_FRACTION = "xp_fraction";

    private final RecipeType<T> recipeType;
    private final IntSupplier energyPerTick;

    public ItemUpgradeCooking(Properties properties, RecipeType<T> recipeType, IntSupplier energyPerTick)
    {
        super(properties, UpgradeModifiers.SPEED);
        this.recipeType = recipeType;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public void tick(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        if (!pedestal.isPowered()) upgradeAction(level, pedestal, upgrade);
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        ResourceHandler<ItemResource> inventory = getInventoryBelow(level, pedestal);
        if (inventory == null) return;

        for (int index = 0; index < inventory.size(); index++)
        {
            ItemResource resource = inventory.getResource(index);
            if (resource.isEmpty()) continue;
            ItemStack input = resource.toStack(1);
            if (!pedestal.machineFilterAllows(input)) continue;
            Optional<RecipeHolder<T>> recipe = findRecipe(level, pedestal, input);
            if (recipe.isEmpty()) continue;
            cook(level, pedestal, upgrade, inventory, index, resource, recipe.get());
            return;
        }
    }

    // The last recipe used is tried first (vanilla furnaces do the same), so the lookup is cheap every tick
    private Optional<RecipeHolder<T>> findRecipe(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack input)
    {
        Identifier last = Identifier.tryParse(pedestal.getUpgradeData().getStringOr(LAST_RECIPE, ""));
        ResourceKey<Recipe<?>> lastKey = last == null ? null : ResourceKey.create(Registries.RECIPE, last);
        return level.recipeAccess().getRecipeFor(recipeType, new SingleRecipeInput(input), level, lastKey);
    }

    private void cook(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade, ResourceHandler<ItemResource> inventory,
                      int index, ItemResource resource, RecipeHolder<T> recipe)
    {
        // A different recipe from last time starts again from 0
        String recipeId = recipe.id().identifier().toString();
        int elapsed = recipeId.equals(pedestal.getUpgradeData().getStringOr(LAST_RECIPE, "")) ? pedestal.getUpgradeData().getIntOr(COOK_TIME, 0) : 0;
        int required = Math.max(1, recipe.value().cookingTime());

        if (elapsed < required)
        {
            int ticks = Math.min(200, Math.max(1, getModifier(upgrade, UpgradeModifiers.SPEED)));
            int cost = energyPerTick.getAsInt();
            if (cost > 0)
            {
                int stored = pedestal.getStorage(PedestalTransportTypes.ENERGY.get()).getAmountAsInt(0);
                ticks = Math.min(ticks, stored / cost);
                if (ticks <= 0) return;
                pedestal.removeFuel(cost * ticks, 0, false);
            }
            elapsed += ticks;
        }

        if (elapsed >= required)
        {
            int done = Math.min(elapsed / required, inventory.getAmountAsInt(index));
            ItemStack output = recipe.value().assemble(new SingleRecipeInput(resource.toStack(1)));
            try (Transaction transaction = Transaction.openRoot())
            {
                int made = output.isEmpty() ? 0 : ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(output), output.getCount() * done, transaction);
                int cooked = output.isEmpty() ? 0 : made / output.getCount();
                if (cooked > 0 && inventory.extract(index, resource, cooked, transaction) == cooked)
                {
                    transaction.commit();
                    elapsed -= required * cooked;
                    addExperience(pedestal, Math.round(recipe.value().experience() * 100 * cooked));
                    pedestal.spawnParticles(level, pedestal.getPosAbove(), 0xFF9600);
                }
            }
        }

        pedestal.getUpgradeData().putString(LAST_RECIPE, recipeId);
        pedestal.getUpgradeData().putInt(COOK_TIME, elapsed);
        pedestal.upgradeDataChanged();
    }

    // Recipe experience in hundredths of a point, the remainder kept for next time
    private static void addExperience(BasePedestalBlockEntity pedestal, int hundredths)
    {
        int total = pedestal.getUpgradeData().getIntOr(XP_FRACTION, 0) + hundredths;
        if (total >= 100)
        {
            try (Transaction transaction = Transaction.openRoot())
            {
                pedestal.getStorage(PedestalTransportTypes.EXPERIENCE.get()).insert(ExperienceTransportType.EXPERIENCE, total / 100, transaction);
                transaction.commit();
            }
        }
        pedestal.getUpgradeData().putInt(XP_FRACTION, total % 100);
    }
}
