package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

// Feeds the pedestal's food to grown-up animals in the work card's area so they breed (wheat for cows, seeds for
// chickens...). With the config limit on, it stops when the area has too many animals.
public class ItemUpgradeBreeder extends ItemUpgradeBase
{
    public ItemUpgradeBreeder(Properties properties)
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
        return Config.BREEDER_FUEL;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        Optional<AABB> area = pedestal.getWorkArea();
        if (area.isEmpty()) return;
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, area.get(), Animal::isAlive);
        if (Config.BREEDER_LIMIT.getAsBoolean() && animals.size() >= Config.BREEDER_LIMIT_COUNT.getAsInt()) return;
        PedestalFakePlayer player = pedestal.getFakePlayer(level);

        for (Animal animal : animals)
        {
            ItemStack food = pedestal.getItemInPedestal();
            if (food.isEmpty()) return;
            if (!animal.isFood(food) || animal.getAge() != 0 || !animal.canFallInLove()) continue;
            int distance = distance(pedestal.getBlockPos(), animal.blockPosition());
            if (!useFuel(pedestal, distance, true)) return;

            ItemStack held = food.copyWithCount(1);
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
            try
            {
                if (animal.mobInteract(player, InteractionHand.MAIN_HAND).consumesAction())
                {
                    pedestal.removeItem(ItemResource.of(food), 1, false);
                    useFuel(pedestal, distance, false);
                }
            }
            finally
            {
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
        }
    }
}
