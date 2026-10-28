package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.IShearable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// Shears sheep, mooshrooms, snow golems and anything else shearable in the work card's area with the pedestal's tool
// (shears if none is in): one per operation, all of them with super speed. The drops fall where the mob is.
public class ItemUpgradeShearer extends ItemUpgradeBase
{
    public ItemUpgradeShearer(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.SUPER_SPEED, UpgradeModifiers.RANGE, UpgradeModifiers.AREA);
    }

    @Override
    public boolean canModify(ItemStack upgrade, String modifier)
    {
        if (modifier.equals(UpgradeModifiers.AREA)) return Config.REQUIRE_SIZED_AREA.getAsBoolean();
        return super.canModify(upgrade, modifier);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.AREA;
    }

    @Override
    public ItemStack getDefaultTool()
    {
        return new ItemStack(Items.SHEARS);
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.SHEARER_FUEL;
    }

    @Override
    protected @Nullable Config.ToolRules getToolRules()
    {
        return Config.SHEARER_TOOLS;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        Optional<AABB> area = pedestal.getWorkArea();
        if (area.isEmpty() || !canUseTool(level, pedestal)) return;
        boolean superSpeed = hasModifier(upgrade, UpgradeModifiers.SUPER_SPEED);

        PedestalFakePlayer player = playerWithTool(level, pedestal);
        try
        {
            ItemStack shears = player.getMainHandItem();
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area.get(), Entity::isAlive))
            {
                // NeoForge's IShearable: vanilla's shearable mobs have it too
                if (!(entity instanceof IShearable shearable) || !shearable.isShearable(player, shears, level, entity.blockPosition())) continue;
                int distance = distance(pedestal.getBlockPos(), entity.blockPosition());
                if (!useFuel(pedestal, distance, true)) return;
                if (wearsTool(pedestal) && shears.isDamageableItem() && shears.getMaxDamage() - shears.getDamageValue() <= 1) return;
                shearable.onSheared(player, shears, level, entity.blockPosition())
                        .forEach(drop -> shearable.spawnShearedDrop(level, entity.blockPosition(), drop));
                if (wearsTool(pedestal)) shears.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
                useFuel(pedestal, distance, false);
                if (!superSpeed) return;
            }
        }
        finally
        {
            finishWithTool(pedestal, player);
        }
    }
}
