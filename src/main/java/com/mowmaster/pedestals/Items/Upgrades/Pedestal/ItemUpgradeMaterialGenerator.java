package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Recipes.MaterialGeneratorRecipe;
import com.mowmaster.pedestals.Recipes.PedestalRecipeInput;
import com.mowmaster.pedestals.Recipes.PedestalRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

// Makes blocks out of nothing, depending on the block under the pedestal (pedestals:cobblegen recipes): the pedestal
// gets what mining the recipe's block would drop with its tool (a stone pickaxe if none is in). The item capacity
// modifier makes more per operation (each one pays the recipe's fluid/energy/experience cost).
public class ItemUpgradeMaterialGenerator extends ItemUpgradeBase
{
    public ItemUpgradeMaterialGenerator(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, "upgradeitemcapacity");
    }

    @Override
    public ItemStack getDefaultTool()
    {
        return new ItemStack(Items.STONE_PICKAXE);
    }

    @Override
    protected @Nullable Config.ToolRules getToolRules()
    {
        return Config.MATERIAL_GENERATOR_TOOLS;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        if (!canUseTool(level, pedestal)) return;
        ItemStack below = new ItemStack(level.getBlockState(pedestal.getPosBelow()).getBlock().asItem());
        if (below.isEmpty()) return;
        Optional<RecipeHolder<MaterialGeneratorRecipe>> recipe = level.recipeAccess().getRecipeFor(PedestalRecipes.MATERIAL_GENERATOR.get(), new PedestalRecipeInput(below, pedestal), level);
        if (recipe.isEmpty())
        {
            pedestal.spawnParticles(level, pedestal.getBlockPos().getX() + 0.5, pedestal.getBlockPos().getY() + 1.0, pedestal.getBlockPos().getZ() + 0.5, 0x323232);
            return;
        }
        MaterialGeneratorRecipe generator = recipe.get().value();
        BlockState made = Block.byItem(generator.result().create().getItem()).defaultBlockState();
        if (made.isAir()) return;

        int times = generator.cost().timesAffordable(pedestal, Math.max(1, getModifier(upgrade, "upgradeitemcapacity")));
        if (times <= 0)
        {
            pedestal.spawnParticles(level, pedestal.getBlockPos().getX() + 0.5, pedestal.getBlockPos().getY() + 1.0, pedestal.getBlockPos().getZ() + 0.5, 0xFFFFFF);
            return;
        }

        PedestalFakePlayer player = playerWithTool(level, pedestal);
        List<ItemStack> drops = Block.getDrops(made, level, pedestal.getPosBelow(), null, player, player.getItemInHand(InteractionHand.MAIN_HAND));
        try (Transaction transaction = Transaction.openRoot())
        {
            boolean added = false;
            for (ItemStack drop : drops)
            {
                if (drop.isEmpty()) continue;
                // As many as fit, up to times the drop, and at most a stack
                int wanted = Math.min(drop.getCount() * times, drop.getMaxStackSize());
                int inserted = ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(drop), wanted, transaction);
                if (inserted <= 0) continue;
                // Pay for every run that made something (rounded up, like 1.20)
                int runs = (int) Math.ceil((double) inserted / drop.getCount());
                if (!generator.cost().take(pedestal, runs, transaction)) return;
                if (wearsTool(pedestal)) player.getMainHandItem().hurtAndBreak(runs, player, InteractionHand.MAIN_HAND);
                added = true;
            }
            if (added) transaction.commit();
        }
        finally
        {
            finishWithTool(pedestal, player);
        }
    }
}
