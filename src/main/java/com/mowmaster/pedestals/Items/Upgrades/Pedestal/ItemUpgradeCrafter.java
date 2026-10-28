package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// A crafting table made of pedestals: the pedestals on a pedestals work card are the grid, in the order they were
// selected (4 pedestals = 2x2, 9 = 3x3; empty pedestals are empty slots). Uses one item from each, leaves container
// items (empty buckets...) behind on their pedestal, and puts the result in this pedestal.
public class ItemUpgradeCrafter extends ItemUpgradeBase
{
    public ItemUpgradeCrafter(Properties properties)
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

        List<BasePedestalBlockEntity> grid = new ArrayList<>();
        List<ItemStack> ingredients = new ArrayList<>();
        for (BlockPos pos : positions)
        {
            if (!(level.getBlockEntity(pos) instanceof BasePedestalBlockEntity ingredientPedestal))
            {
                pedestal.spawnParticles(level, pedestal.getPosAbove(), 0xFF0000); // a selected pedestal is gone
                return;
            }
            grid.add(ingredientPedestal);
            ingredients.add(ingredientPedestal.getItemInPedestal().copyWithCount(1));
        }

        int size = (int) Math.ceil(Math.sqrt(ingredients.size()));
        while (ingredients.size() < size * size) ingredients.add(ItemStack.EMPTY);
        CraftingInput input = CraftingInput.of(size, size, ingredients);
        Optional<RecipeHolder<CraftingRecipe>> recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (recipe.isEmpty()) return;
        ItemStack result = recipe.get().value().assemble(input);
        if (result.isEmpty()) return;

        try (Transaction transaction = Transaction.openRoot())
        {
            if (ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(result), result.getCount(), transaction) != result.getCount())
            {
                pedestal.spawnParticles(level, pedestal.getPosAbove(), 0x323232); // no room for the result
                return;
            }
            for (int index = 0; index < grid.size(); index++)
            {
                ItemStack used = ingredients.get(index);
                if (used.isEmpty()) continue;
                BasePedestalBlockEntity ingredientPedestal = grid.get(index);
                if (ingredientPedestal.getItemStorage().extract(ItemResource.of(used), 1, transaction) != 1) return;
                ItemStackTemplate remainder = used.getCraftingRemainder();
                if (remainder != null)
                {
                    ItemStack left = remainder.create();
                    ResourceHandlerUtil.insertStacking(ingredientPedestal.getItemStorage(), ItemResource.of(left), left.getCount(), transaction);
                }
            }
            transaction.commit();
        }
        grid.forEach(ingredientPedestal -> pedestal.spawnParticles(level, ingredientPedestal.getPosAbove(), 0x32C800));
        pedestal.spawnParticles(level, pedestal.getPosAbove(), 0x00FF00);
    }
}
