package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Recipes.PedestalRecipes;
import com.mowmaster.pedestals.Recipes.UpgradeModificationRecipe;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Adds modifiers to upgrades (pedestals:upgrademodification_global recipes). The upgrade to modify comes from the
// inventory below; the ingredients are the items on the pedestals selected with a pedestals work card, in the order
// they were selected. One of each ingredient is used up, and the modified upgrade goes into this pedestal.
public class ItemUpgradeModifications extends ItemUpgradeBase
{
    public ItemUpgradeModifications(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.RANGE);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.PEDESTALS;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        List<BlockPos> positions = pedestal.getWorkPositions();
        if (positions.isEmpty()) return;
        ResourceHandler<ItemResource> inventory = getInventoryBelow(level, pedestal);
        if (inventory == null) return;

        // Every selected position must still be a pedestal
        List<BasePedestalBlockEntity> ingredientPedestals = new ArrayList<>();
        for (BlockPos pos : positions)
        {
            if (!(level.getBlockEntity(pos) instanceof BasePedestalBlockEntity ingredientPedestal))
            {
                pedestal.spawnParticles(level, pedestal.getPosAbove(), 0xFF0000);
                return;
            }
            if (ingredientPedestal.hasItem()) ingredientPedestals.add(ingredientPedestal);
        }

        // The first upgrade in the inventory below
        for (int index = 0; index < inventory.size(); index++)
        {
            ItemResource resource = inventory.getResource(index);
            if (resource.isEmpty() || !(resource.getItem() instanceof ItemUpgradeBase)) continue;
            ItemStack toModify = resource.toStack(1);
            if (!pedestal.machineFilterAllows(toModify)) continue;

            List<ItemStack> items = new ArrayList<>();
            items.add(toModify);
            ingredientPedestals.forEach(ingredientPedestal -> items.add(ingredientPedestal.getItemInPedestal().copyWithCount(1)));
            Optional<RecipeHolder<UpgradeModificationRecipe>> recipe = level.recipeAccess()
                    .getRecipeFor(PedestalRecipes.UPGRADE_MODIFICATION.get(), new UpgradeModificationRecipe.Input(items), level);
            if (recipe.isEmpty())
            {
                pedestal.spawnParticles(level, pedestal.getPosAbove(), 0x000000);
                return;
            }

            ItemStack modified = recipe.get().value().apply(toModify);
            try (Transaction transaction = Transaction.openRoot())
            {
                if (inventory.extract(index, resource, 1, transaction) != 1) return;
                if (ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(modified), 1, transaction) != 1) return;
                for (int ingredient = 0; ingredient < ingredientPedestals.size(); ingredient++)
                {
                    ItemStack used = items.get(ingredient + 1);
                    BasePedestalBlockEntity ingredientPedestal = ingredientPedestals.get(ingredient);
                    if (ingredientPedestal.getItemStorage().extract(ItemResource.of(used), 1, transaction) != 1) return;
                }
                transaction.commit();
            }
            ingredientPedestals.forEach(ingredientPedestal -> pedestal.spawnParticles(level, ingredientPedestal.getPosAbove(), 0x32C800));
            pedestal.spawnParticles(level, pedestal.getPosAbove(), 0x00FF00);
            return;
        }
    }
}
