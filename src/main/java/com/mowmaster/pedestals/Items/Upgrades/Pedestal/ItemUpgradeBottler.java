package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Recipes.BottlerRecipe;
import com.mowmaster.pedestals.Recipes.PedestalRecipeInput;
import com.mowmaster.pedestals.Recipes.PedestalRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Optional;

// Fills items from the inventory below with what's in the pedestal (pedestals:bottler recipes): buckets with fluid,
// glass bottles with experience... The result goes into the pedestal. One item per operation.
public class ItemUpgradeBottler extends ItemUpgradeBase
{
    public ItemUpgradeBottler(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED);
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
            ItemStack item = resource.toStack(1);
            if (!pedestal.machineFilterAllows(item)) continue;

            Optional<RecipeHolder<BottlerRecipe>> recipe = level.recipeAccess().getRecipeFor(PedestalRecipes.BOTTLER.get(), new PedestalRecipeInput(item, pedestal), level);
            if (recipe.isEmpty()) continue;
            BottlerRecipe bottler = recipe.get().value();
            ItemStack result = bottler.result().create();
            try (Transaction transaction = Transaction.openRoot())
            {
                if (inventory.extract(index, resource, 1, transaction) != 1) continue;
                if (!bottler.cost().take(pedestal, 1, transaction)) continue;
                if (ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(result), result.getCount(), transaction) != result.getCount()) continue;
                transaction.commit();
                return;
            }
        }
    }
}
