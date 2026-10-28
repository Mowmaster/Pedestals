package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Transport.EnergyTransportType;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Optional;

// Burns furnace fuel from the pedestal's items into energy: generator energy per tick of burn time (the speed
// modifier burns faster for more energy per tick). Buckets of lava leave their empty bucket in the pedestal.
// Runs every tick, like a furnace.
public class ItemUpgradeGenerator extends ItemUpgradeBase
{
    private static final String BURN_TIME = "burn_time";

    public ItemUpgradeGenerator(Properties properties)
    {
        super(properties, UpgradeModifiers.SPEED);
    }

    @Override
    public void tick(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        if (!pedestal.isPowered()) upgradeAction(level, pedestal, upgrade);
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        int burnLeft = pedestal.getUpgradeData().getIntOr(BURN_TIME, 0);
        if (burnLeft > 0)
        {
            int ticks = Math.min(burnLeft, 1 + getModifier(upgrade, UpgradeModifiers.SPEED));
            int energy = ticks * Config.GENERATOR_ENERGY.getAsInt();
            try (Transaction transaction = Transaction.openRoot())
            {
                // Only burns when all the energy fits, so none is wasted
                if (pedestal.getStorage(PedestalTransportTypes.ENERGY.get()).insert(EnergyTransportType.ENERGY, energy, transaction) < energy) return;
                transaction.commit();
            }
            setBurnTime(pedestal, burnLeft - ticks);
            if (level.getGameTime() % 20 == 0) pedestal.spawnParticles(level, pedestal.getPosAbove(), 0xFF0000);
            return;
        }

        ItemStack fuel = pedestal.getItemInPedestal();
        if (fuel.isEmpty()) return;
        int burnTime = getBurnTime(level, pedestal, fuel.copyWithCount(1));
        if (burnTime <= 0) return;
        ItemStackTemplate remainder = fuel.getCraftingRemainder();
        try (Transaction transaction = Transaction.openRoot())
        {
            if (pedestal.getItemStorage().extract(ItemResource.of(fuel), 1, transaction) != 1) return;
            if (remainder != null)
            {
                ItemStack left = remainder.create();
                if (ResourceHandlerUtil.insertStacking(pedestal.getItemStorage(), ItemResource.of(left), left.getCount(), transaction) != left.getCount())
                {
                    pedestal.spawnParticles(level, pedestal.getPosAbove(), 0x808080);
                    return;
                }
            }
            transaction.commit();
        }
        setBurnTime(pedestal, burnTime);
    }

    private static void setBurnTime(BasePedestalBlockEntity pedestal, int burnTime)
    {
        pedestal.getUpgradeData().putInt(BURN_TIME, burnTime);
        pedestal.upgradeDataChanged();
    }

    // In 26.3 fuel is a data component whose burn time can depend on where it burns, so it's read through a loot
    // context, the same one a furnace builds (BaseContainerBlockEntity#getLootContext). The container is a stand-in.
    private static int getBurnTime(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack fuel)
    {
        if (!fuel.has(DataComponents.COOKING_FUEL)) return 0;
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.BLOCK_STATE, pedestal.getBlockState())
                .withParameter(LootContextParams.BLOCK_ENTITY, pedestal)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pedestal.getBlockPos()))
                .withParameter(LootContextParams.CONTAINER, new SimpleContainer(1))
                .create(LootContextParamSets.CONTAINER_PROCESS);
        LootContext context = new LootContext.Builder(params).create(Optional.empty());
        return ResolvableInt.getFromItem(fuel, DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0);
    }
}
