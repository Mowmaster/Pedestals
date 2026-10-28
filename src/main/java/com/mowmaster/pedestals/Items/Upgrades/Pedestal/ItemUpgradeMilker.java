package com.mowmaster.pedestals.Items.Upgrades.Pedestal;

import com.mowmaster.pedestals.Blocks.Pedestal.BasePedestalBlockEntity;
import com.mowmaster.pedestals.Blocks.Pedestal.PedestalFakePlayer;
import com.mowmaster.pedestals.Config;
import com.mowmaster.pedestals.Items.Upgrades.ItemUpgradeBase;
import com.mowmaster.pedestals.Items.Upgrades.UpgradeModifiers;
import com.mowmaster.pedestals.Transport.PedestalTransportTypes;
import com.mowmaster.pedestals.api.upgrade.WorkCardType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// Milks animals in the work card's area, one per operation (all with super speed):
//   with buckets (or bowls, for mooshroom stew...) in the pedestal: uses one, and the filled item drops by the animal
//   with nothing in the pedestal: an empty bucket is used, and the milk goes into the pedestal's tank, if a milk
//   fluid exists (NeoForge's milk fluid is only there when a mod turns it on)
public class ItemUpgradeMilker extends ItemUpgradeBase
{
    public ItemUpgradeMilker(Properties properties)
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
    protected @Nullable Config.FuelCost getFuelCost()
    {
        return Config.MILKER_FUEL;
    }

    @Override
    protected void upgradeAction(ServerLevel level, BasePedestalBlockEntity pedestal, ItemStack upgrade)
    {
        Optional<AABB> area = pedestal.getWorkArea();
        if (area.isEmpty()) return;
        ItemStack fromPedestal = pedestal.getItemInPedestal();
        boolean usingItems = !fromPedestal.isEmpty() && pedestal.machineFilterAllows(fromPedestal);
        boolean superSpeed = hasModifier(upgrade, UpgradeModifiers.SUPER_SPEED);
        PedestalFakePlayer player = pedestal.getFakePlayer(level);

        for (Animal animal : level.getEntitiesOfClass(Animal.class, area.get(), Animal::isAlive))
        {
            ItemStack container = usingItems ? pedestal.getItemInPedestal().copyWithCount(1) : new ItemStack(Items.BUCKET);
            if (container.isEmpty()) return;
            int distance = distance(pedestal.getBlockPos(), animal.blockPosition());
            if (!useFuel(pedestal, distance, true)) return;

            player.setItemInHand(InteractionHand.MAIN_HAND, container);
            try
            {
                if (!animal.mobInteract(player, InteractionHand.MAIN_HAND).consumesAction()) continue;
                // The filled item is now in the hand (or the inventory, if the hand still held some)
                ItemStack filled = player.getMainHandItem() != container ? player.getMainHandItem() : findFilled(player, container);
                if (filled.isEmpty()) continue;
                if (usingItems)
                {
                    pedestal.removeItem(ItemResource.of(fromPedestal), 1, false);
                    Block.popResource(level, animal.blockPosition(), filled.copy());
                }
                else if (!intoTank(pedestal, filled))
                {
                    continue; // no milk fluid (or no room): nothing was taken from the pedestal
                }
                useFuel(pedestal, distance, false);
            }
            finally
            {
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.getInventory().clearContent();
            }
            if (!superSpeed) return;
        }
    }

    private static ItemStack findFilled(PedestalFakePlayer player, ItemStack container)
    {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
        {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack != container) return stack;
        }
        return ItemStack.EMPTY;
    }

    // The filled bucket's fluid goes into the pedestal's tank (all of it, or none)
    private static boolean intoTank(BasePedestalBlockEntity pedestal, ItemStack filled)
    {
        FluidStack fluid = FluidUtil.getFirstStackContained(filled);
        if (fluid.isEmpty()) return false;
        try (Transaction transaction = Transaction.openRoot())
        {
            if (pedestal.getStorage(PedestalTransportTypes.FLUIDS.get()).insert(FluidResource.of(fluid), fluid.getAmount(), transaction) < fluid.getAmount()) return false;
            transaction.commit();
            return true;
        }
    }
}
