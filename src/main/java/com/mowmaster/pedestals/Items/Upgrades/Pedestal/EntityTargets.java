package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Recipes.EntityFilterRecipe;
import com.mowmaster.pedestals.Recipes.PedestalRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;

import java.util.Optional;
import java.util.function.Predicate;

// Which mobs the attacker and fan pick: the pedestals:entity_filter recipe for the block under the pedestal, or every
// mob if no recipe matches that block (1.20's allowEntity)
public class EntityTargets
{
    public static Predicate<Entity> forPedestal(ServerLevel level, BasePedestalBlockEntity pedestal)
    {
        ItemStack below = new ItemStack(level.getBlockState(pedestal.getPosBelow()).getBlock().asItem());
        if (below.isEmpty()) return entity -> true;
        Optional<RecipeHolder<EntityFilterRecipe>> recipe = level.recipeAccess().getRecipeFor(PedestalRecipes.ENTITY_FILTER.get(), new SingleRecipeInput(below), level);
        return recipe.<Predicate<Entity>>map(holder -> holder.value()::allows).orElse(entity -> true);
    }
}
