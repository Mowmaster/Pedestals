package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

// Blows mobs (and players who aren't sneaking) in the work card's area the way the pedestal faces, every 2 ticks.
// Faster with the speed modifier. The block under the pedestal can limit which mobs (see EntityTargets).
public class ItemUpgradeFan extends ItemUpgradeBase
{
    public ItemUpgradeFan(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED, UpgradeModifiers.RANGE, UpgradeModifiers.AREA);
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
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.FAN_FUEL;
    }

    // Every 2 ticks whatever the speed (1.20 behaviour); the speed modifier makes the push stronger instead
    @Override
    public void tick(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        if (!pedestal.isPowered() && level.getGameTime() % 2 == 0) upgradeAction(level, pedestal, upgrade);
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        Optional<AABB> area = pedestal.getWorkArea();
        if (area.isEmpty() || !useFuel(pedestal, 0, false)) return;
        Predicate<Entity> targets = EntityTargets.forPedestal(level, pedestal);
        Direction facing = pedestal.getFacing();
        double strength = (facing == Direction.UP ? 0.2 : 0.1)
                + (double) Math.max(1, getModifier(upgrade, UpgradeModifiers.SPEED)) / Config.TICKS_TO_TRANSFER.getAsInt();
        Vec3 push = facing.getUnitVec3().scale(strength);

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area.get(), LivingEntity::isAlive))
        {
            if (entity instanceof Player player && player.isShiftKeyDown()) continue;
            if (!(entity instanceof Player) && !targets.test(entity)) continue;
            entity.setDeltaMovement(entity.getDeltaMovement().add(push));
            entity.needsSync = true;
            // Players move themselves, so they need to be told
            if (entity instanceof ServerPlayer serverPlayer) serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }
    }
}
