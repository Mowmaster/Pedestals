package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Recipes.PedestalRecipeInput;
import com.mowmaster.pedestals.Recipes.PedestalRecipes;
import com.mowmaster.pedestals.Recipes.UnbottlerRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Optional;

// Empties items from the inventory below into the pedestal (pedestals:unbottler recipes): water buckets give water and
// an empty bucket, bottles o' enchanting give experience... One item per operation, only if everything fits.
public class ItemUpgradeUnbottler extends ItemUpgradeBase
{
    public ItemUpgradeUnbottler(Properties properties)
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

            Optional<RecipeHolder<UnbottlerRecipe>> recipe = level.recipeAccess().getRecipeFor(PedestalRecipes.UNBOTTLER.get(), new PedestalRecipeInput(item, pedestal), level);
            if (recipe.isEmpty()) continue;
            UnbottlerRecipe unbottler = recipe.get().value();
            try (Transaction transaction = Transaction.openRoot())
            {
                if (inventory.extract(index, resource, 1, transaction) != 1) continue;
                if (!unbottler.returned().give(pedestal, transaction)) continue;
                if (unbottler.result().isPresent())
                {
                    ItemStack result = unbottler.result().get().create();
                    if (ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(result), result.getCount(), transaction) != result.getCount()) continue;
                }
                transaction.commit();
                return;
            }
        }
    }
}
