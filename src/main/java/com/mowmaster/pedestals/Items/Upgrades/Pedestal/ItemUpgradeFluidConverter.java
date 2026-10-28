package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Recipes.FluidConverterRecipe;
import com.mowmaster.pedestals.Recipes.PedestalRecipeInput;
import com.mowmaster.pedestals.Recipes.PedestalRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Optional;

// Turns the pedestal's fluid into items, energy or experience (pedestals:fluidconverter recipes).
public class ItemUpgradeFluidConverter extends ItemUpgradeBase
{
    public ItemUpgradeFluidConverter(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED);
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        Optional<RecipeHolder<FluidConverterRecipe>> recipe = level.recipeAccess().getRecipeFor(PedestalRecipes.FLUID_CONVERTER.get(), new PedestalRecipeInput(ItemStack.EMPTY, pedestal), level);
        if (recipe.isEmpty()) return;
        FluidConverterRecipe converter = recipe.get().value();
        try (Transaction transaction = Transaction.openRoot())
        {
            if (!converter.cost().take(pedestal, 1, transaction)) return;
            if (!converter.returned().give(pedestal, transaction)) return;
            if (converter.result().isPresent())
            {
                ItemStack result = converter.result().get().create();
                if (ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(result), result.getCount(), transaction) != result.getCount()) return;
            }
            transaction.commit();
        }
    }
}
