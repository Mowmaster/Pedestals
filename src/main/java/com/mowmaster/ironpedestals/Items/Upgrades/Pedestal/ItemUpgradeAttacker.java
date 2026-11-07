package com.mowmaster.ironpedestals.Items.Upgrades.Pedestal;

import com.mowmaster.mowlib.workcard.WorkCardType;
import com.mowmaster.ironpedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.ironpedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.ironpedestals.Config;
import com.mowmaster.ironpedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.ironpedestals.Items.Upgrades.UpgradeModifiers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

// Attacks every mob in the work card's area each operation, as a player swinging the pedestal's tool (a stick if
// none is in): the tool's damage, enchantments, sweeping and wear all work like a player's attack (better tools and
// enchantments are how it hits harder; there's no damage modifier). The block under the pedestal can limit which
// mobs (see EntityTargets). Players are never attacked.
public class ItemUpgradeAttacker extends ItemUpgradeBase
{
    public ItemUpgradeAttacker(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED);
    }

    @Override
    public WorkCardType getWorkCardType(ItemStack upgrade)
    {
        return WorkCardType.AREA;
    }

    @Override
    public ItemStack getDefaultTool()
    {
        return new ItemStack(Items.STICK);
    }

    @Override
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.ATTACKER_FUEL;
    }

    @Override
    protected @Nullable Config.ToolRules getToolRules()
    {
        return Config.ATTACKER_TOOLS;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        Optional<AABB> area = pedestal.getWorkArea();
        if (area.isEmpty() || !canUseTool(level, pedestal)) return;
        Predicate<Entity> targets = EntityTargets.forPedestal(level, pedestal);

        PedestalFakePlayer player = playerWithTool(level, pedestal);
        try
        {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area.get(), entity -> entity.isAlive() && !(entity instanceof Player)))
            {
                if (!targets.test(target)) continue;
                int distance = distance(pedestal.getBlockPos(), target.blockPosition());
                if (!useFuel(pedestal, distance, true)) return;
                if (wearsTool(pedestal) && pedestal.getTool().isDamageableItem()
                        && pedestal.getTool().getMaxDamage() - pedestal.getTool().getDamageValue() <= 1) return;
                // Turn to face it, so knockback and sweeping go the right way
                player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
                player.attack(target);
                useFuel(pedestal, distance, false);
            }
        }
        finally
        {
            finishWithTool(pedestal, player);
        }
    }
}
